/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.internal.e4.wizards.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.internal.ISelectionConversionService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.elbe.relations.Activator;

/**
 * Instances represent registered wizards.
 *
 * @author Luthiger <br />
 *         see org.eclipse.ui.internal.dialogs.WorkbenchWizardElement
 */
@SuppressWarnings("restriction")
public class WorkbenchWizardElement extends WorkbenchAdapter
        implements IAdaptable, IWizardDescriptor {

	private static final String[] EMPTY_TAGS = new String[0];

	private ImageDescriptor imageDescriptor;
	private final IConfigurationElement configuration;
	private final String id;
	private ImageDescriptor descriptionImage;

	private SelectionEnabler selectionEnabler;

	private WizardCollectionElement parentCategory;

	private final IEclipseContext context;

	/**
	 * WorkbenchWizardElement constructor.
	 *
	 * @param inConfiguration
	 *            {@link IConfigurationElement}
	 * @param inContext
	 *            {@link IEclipseContext}
	 */
	public WorkbenchWizardElement(final IConfigurationElement inConfiguration,
	        final IEclipseContext inContext) {
		configuration = inConfiguration;
		context = inContext;
		id = configuration.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
	}

	private String getAttributeChecked(final String inAttributeName) {
		final String out = configuration.getAttribute(inAttributeName);
		return out == null ? "" : out;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return getLabel(this);
	}

	@Override
	public String getLabel(final Object inObject) {
		return getAttributeChecked(IWorkbenchRegistryConstants.ATT_NAME);
	}

	@Override
	public Object getAdapter(final Class inAdapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(final Object inElement) {
		return getImageDescriptor();
	}

	/**
	 * Answer the icon of this element.
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		if (imageDescriptor == null) {
			final String iconName = configuration
			        .getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
			if (iconName == null) {
				return null;
			}
			imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
			        configuration.getNamespaceIdentifier(), iconName);
		}
		return imageDescriptor;
	}

	@Override
	public String getDescription() {
		return Util.getDescription(configuration);
	}

	@Override
	public ImageDescriptor getDescriptionImage() {
		if (descriptionImage == null) {
			final String descImage = configuration.getAttribute(
			        IWorkbenchRegistryConstants.ATT_DESCRIPTION_IMAGE);
			if (descImage == null) {
				return null;
			}
			descriptionImage = AbstractUIPlugin.imageDescriptorFromPlugin(
			        configuration.getNamespaceIdentifier(), descImage);
		}
		return descriptionImage;
	}

	@Override
	public boolean canFinishEarly() {
		return Boolean
		        .valueOf(configuration.getAttribute(
		                IWorkbenchRegistryConstants.ATT_CAN_FINISH_EARLY))
		        .booleanValue();
	}

	@Override
	public boolean hasPages() {
		final String hasPagesString = configuration
		        .getAttribute(IWorkbenchRegistryConstants.ATT_HAS_PAGES);
		// default value is true
		if (hasPagesString == null) {
			return true;
		}
		return Boolean.valueOf(hasPagesString).booleanValue();
	}

	@Override
	public IStructuredSelection adaptedSelection(
	        final IStructuredSelection inSelection) {
		if (canHandleSelection(inSelection)) {
			return inSelection;
		}

		final IStructuredSelection adaptedSelection = convertToResources(
		        inSelection);
		if (canHandleSelection(adaptedSelection)) {
			return adaptedSelection;
		}

		// Couldn't find one that works so just return
		return StructuredSelection.EMPTY;
	}

	/**
	 * Attempt to convert the elements in the passed selection into resources by
	 * asking each for its IResource property (if it isn't already a resource).
	 * If all elements in the initial selection can be converted to resources
	 * then answer a new selection containing these resources; otherwise answer
	 * an empty selection.
	 *
	 * @param inOriginalSelection
	 *            the original selection
	 * @return the converted selection or an empty selection
	 */
	private IStructuredSelection convertToResources(
	        final IStructuredSelection inOriginalSelection) {
		// TODO
		// final Object lSelectionService =
		// PlatformUI.getWorkbench().getService(
		// ISelectionConversionService.class);
		final Object lSelectionService = null;
		if (lSelectionService == null || inOriginalSelection == null) {
			return StructuredSelection.EMPTY;
		}
		return ((ISelectionConversionService) lSelectionService)
		        .convertToResources(inOriginalSelection);
	}

	/**
	 * Answer a boolean indicating whether the receiver is able to handle the
	 * passed selection
	 *
	 * @return boolean
	 * @param inSelection
	 *            IStructuredSelection
	 */
	public boolean canHandleSelection(final IStructuredSelection inSelection) {
		return getSelectionEnabler().isEnabledForSelection(inSelection);
	}

	/**
	 * Answer self's action enabler, creating it first iff necessary
	 */
	protected SelectionEnabler getSelectionEnabler() {
		if (selectionEnabler == null) {
			selectionEnabler = new SelectionEnabler(configuration);
		}

		return selectionEnabler;
	}

	@Override
	public String[] getTags() {
		return EMPTY_TAGS;
	}

	@Override
	public String getHelpHref() {
		return configuration
		        .getAttribute(IWorkbenchRegistryConstants.ATT_HELP_HREF);
	}

	public void setParent(final WizardCollectionElement inParent) {
		parentCategory = inParent;
	}

	@Override
	public Object getParent(final Object object) {
		return parentCategory;
	}

	@Override
	public IWizardCategory getCategory() {
		return (IWizardCategory) getParent(this);
	}

	public void consolidateCategory(final WizardCollectionElement inWizards) {
		final WizardCollectionElement lCategory = inWizards
		        .findCategory(configuration.getAttribute(
		                IWorkbenchRegistryConstants.ATT_CATEGORY));
		lCategory.add(this);
		setParent(lCategory);
	}

	/**
	 * Keyword are not supported (for the moment).
	 *
	 * @return String[]
	 */
	public String[] getKeywordLabels() {
		return EMPTY_TAGS;
		// if (keywordLabels == null) {
		// final IConfigurationElement[] lChildren = configuration
		// .getChildren(IWorkbenchRegistryConstants.TAG_KEYWORD_REFERENCE);
		// keywordLabels = new String[lChildren.length];
		// final KeywordRegistry lRegistry = KeywordRegistry.getInstance();
		// for (int i = 0; i < lChildren.length; i++) {
		// final String lId = lChildren[i]
		// .getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		// keywordLabels[i] = lRegistry.getKeywordLabel(lId);
		// }
		// }
		// return keywordLabels;
	}

	@Override
	public IWizard createWizard() throws CoreException {
		return (IWizard) createExecutableExtension();
	}

	/**
	 * Create an the instance of the object described by the configuration
	 * element. That is, create the instance of the class the isv supplied in
	 * the extension point.
	 *
	 * @return Object the new object
	 * @throws CoreException
	 */
	public Object createExecutableExtension() throws CoreException {
		return createExtension(configuration,
		        IWorkbenchRegistryConstants.ATT_CLASS);
	}

	private Object createExtension(final IConfigurationElement inElement,
	        final String inClassAttribute) throws CoreException {
		try {
			if (BundleUtility.isActivated(inElement.getDeclaringExtension()
			        .getNamespaceIdentifier())) {
				return createExtension(inElement, inClassAttribute, context);
			}

			final Object[] out = new Object[1];
			final CoreException[] exc = new CoreException[1];
			BusyIndicator.showWhile(null, new Runnable() {
				@Override
				public void run() {
					try {
						out[0] = createExtension(inElement, inClassAttribute,
			                    context);
					}
					catch (final CoreException e) {
						exc[0] = e;
					}
				}
			});
			if (exc[0] != null) {
				throw exc[0];
			}
			return out[0];
		}
		catch (final CoreException exc) {
			throw exc;
		}
		catch (final Exception exc) {
			throw new CoreException(
			        new Status(IStatus.ERROR, Activator.getSymbolicName(),
			                IStatus.ERROR, "Cannot create extension", exc));
		}
	}

	private Object createExtension(final IConfigurationElement inElement,
	        final String inClassAttribute, final IEclipseContext inContext)
	                throws CoreException {
		final Object out = inElement
		        .createExecutableExtension(inClassAttribute);
		ContextInjectionFactory.inject(out, inContext);
		return out;
	}

	/**
	 * @return {@link IConfigurationElement}
	 */
	public IConfigurationElement getConfigurationElement() {
		return configuration;
	}

}
