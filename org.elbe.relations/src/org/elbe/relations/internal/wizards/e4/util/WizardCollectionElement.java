/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
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
package org.elbe.relations.internal.wizards.e4.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Instances of this class are a collection of WizardCollectionElements, thereby
 * facilitating the definition of tree structures composed of these elements.
 * Instances also store a list of wizards.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class WizardCollectionElement extends AdaptableList implements
		IWizardDescriptor, IWizardCategory, IAdaptable {

	private IConfigurationElement configElement;
	private final String id;
	private final WizardCollectionElement parent;

	private final AdaptableList wizards = new AdaptableList();
	private String name;
	private String pluginId;

	/**
	 * WizardCollectionElement constructor.
	 * 
	 * @param inElement
	 *            {@link IConfigurationElement}
	 * @param inParent
	 *            {@link WizardCollectionElement} the parent, may be
	 *            <code>null</code>
	 */
	public WizardCollectionElement(final IConfigurationElement inElement,
			final WizardCollectionElement inParent) {
		configElement = inElement;
		id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		parent = inParent;
	}

	/**
	 * WizardCollectionElement constructor.
	 * 
	 * @param inId
	 *            String
	 * @param inPluginId
	 *            String
	 * @param inName
	 *            String
	 * @param inParent
	 *            {@link WizardCollectionElement} the parent, may be
	 *            <code>null</code>
	 */
	public WizardCollectionElement(final String inId, final String inPluginId,
			final String inName, final WizardCollectionElement inParent) {
		id = inId;
		name = inName;
		pluginId = inPluginId;
		parent = inParent;
	}

	/**
	 * Returns the unique ID of this element.
	 * 
	 * @return String
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Returns the logical parent of the given object in its tree.
	 */
	@Override
	public Object getParent(final Object inObject) {
		return parent;
	}

	/**
	 * @return {@link WizardCollectionElement}
	 */
	@Override
	public WizardCollectionElement getParent() {
		return parent;
	}

	/**
	 * Return the parent collection element.
	 * 
	 * @return {@link WizardCollectionElement} the parent
	 */
	public WizardCollectionElement getParentCollection() {
		return parent;
	}

	/**
	 * Return this wizards path. The segments of this path will correspond to
	 * category ids.
	 * 
	 * @return {@link IPath}
	 */
	@Override
	public IPath getPath() {
		if (parent == null) {
			return new Path(""); //$NON-NLS-1$
		}

		return parent.getPath().append(getId());
	}

	/**
	 * Find a wizard that has the provided id. This will search recursivly over
	 * this categories children.
	 * 
	 * @param inSearchId
	 *            String the id to search on
	 * @return {@link WorkbenchWizardElement} the element or <code>null</code>
	 */
	@Override
	public WorkbenchWizardElement findWizard(final String inSearchId) {
		return findWizard(inSearchId, true);
	}

	/**
	 * Returns this collection's associated wizard object corresponding to the
	 * passed id, or <code>null</code> if such an object could not be found.
	 * 
	 * @param inSearchId
	 *            String the id to search on
	 * @param inRecursive
	 *            boolean whether to search recursivly
	 * @return {@link WorkbenchWizardElement} the element or <code>null</code>
	 */
	@SuppressWarnings("rawtypes")
	public WorkbenchWizardElement findWizard(final String inSearchId,
			final boolean inRecursive) {

		final Object[] lWizards = getWizards();
		for (int i = 0; i < lWizards.length; ++i) {
			final WorkbenchWizardElement lCurrentWizard = (WorkbenchWizardElement) lWizards[i];
			if (lCurrentWizard.getId().equals(inSearchId)) {
				return lCurrentWizard;
			}
		}

		if (!inRecursive) {
			return null;
		}

		for (final Iterator lChildren = children.iterator(); lChildren
				.hasNext();) {
			final WizardCollectionElement lChild = (WizardCollectionElement) lChildren
					.next();
			final WorkbenchWizardElement lResult = lChild.findWizard(
					inSearchId, true);
			if (lResult != null) {
				return lResult;
			}
		}
		return null;
	}

	/**
	 * Returns the wizard category corresponding to the passed id, or
	 * <code>null</code> if such an object could not be found. This recurses
	 * through child categories.
	 * 
	 * @param inId
	 *            String the id for the child category
	 * @return {@link WizardCollectionElement} the category, or
	 *         <code>null</code> if not found
	 */
	public WizardCollectionElement findCategory(final String inId) {
		final Object[] lChildren = getChildren(null);
		for (int i = 0; i < lChildren.length; ++i) {
			final WizardCollectionElement lCurrentCategory = (WizardCollectionElement) lChildren[i];
			if (inId.equals(lCurrentCategory.getId())) {
				return lCurrentCategory;
			}
			final WizardCollectionElement lChildCategory = lCurrentCategory
					.findCategory(inId);
			if (lChildCategory != null) {
				return lChildCategory;
			}
		}
		return null;
	}

	/**
	 * Return the wizards in this category, minus the wizards which failed the
	 * Expressions check.
	 * 
	 * @return IWizardDescriptor[]
	 */
	@Override
	public IWizardDescriptor[] getWizards() {
		return (IWizardDescriptor[]) wizards
				.getTypedChildren(IWizardDescriptor.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.model.AdaptableList#add(org.eclipse.core.runtime.IAdaptable
	 * )
	 */
	@Override
	public AdaptableList add(final IAdaptable inAdaptable) {
		if (inAdaptable instanceof WorkbenchWizardElement) {
			wizards.add(inAdaptable);
		} else {
			return super.add(inAdaptable);
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.model.AdaptableList#remove(org.eclipse.core.runtime.IAdaptable
	 * )
	 */
	@Override
	public void remove(final IAdaptable inAdaptable) {
		if (inAdaptable instanceof WorkbenchWizardElement) {
			wizards.remove(inAdaptable);
		} else {
			super.remove(inAdaptable);
		}
	}

	/**
	 * @return String the id of the originating plugin. Can be null if this
	 *         contribution did not originate from a plugin.
	 */
	public String getPluginId() {
		return configElement == null ? pluginId : configElement
				.getNamespaceIdentifier();
	}

	@Override
	public String getLabel(final Object inObject) {
		return configElement == null ? name : configElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}

	/**
	 * @return WizardCollectionElement[] the immediate child categories.
	 */
	@Override
	public WizardCollectionElement[] getCategories() {
		return (WizardCollectionElement[]) getTypedChildren(WizardCollectionElement.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.util.IWizardDescriptor#getLabel()
	 */
	@Override
	public String getLabel() {
		return getLabel(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.util.IWizardDescriptor#getImageDescriptor
	 * ()
	 */
	@Override
	public ImageDescriptor getImageDescriptor(final Object inElement) {
		return getImageDescriptor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.util.IWizardDescriptor#getImageDescriptor
	 * ()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.AdaptableList#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(final Class inAdapter) {
		if (inAdapter == IWorkbenchAdapter.class) {
			return this;
		}
		return Platform.getAdapterManager().getAdapter(this, inAdapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.util.IWizardDescriptor#getDescription
	 * ()
	 */
	@Override
	public String getDescription() {
		return Util.getDescription(configElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.wizards.util.IWizardDescriptor#
	 * getDescriptionImage()
	 */
	@Override
	public ImageDescriptor getDescriptionImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.util.IWizardDescriptor#canFinishEarly
	 * ()
	 */
	@Override
	public boolean canFinishEarly() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.util.IWizardDescriptor#hasPages()
	 */
	@Override
	public boolean hasPages() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.wizards.IWizardDescriptor#adaptedSelection(org.eclipse
	 * .jface.viewers.IStructuredSelection)
	 */
	@Override
	public IStructuredSelection adaptedSelection(
			final IStructuredSelection inSelection) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getTags()
	 */
	@Override
	public String[] getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#createWizard()
	 */
	@Override
	public IWorkbenchWizard createWizard() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getHelpHref()
	 */
	@Override
	public String getHelpHref() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getCategory()
	 */
	@Override
	public IWizardCategory getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.wizards.IWizardCategory#findCategory(org.eclipse.core.
	 * runtime.IPath)
	 */
	@Override
	public IWizardCategory findCategory(final IPath inPath) {
		return findChildCollection(inPath);
	}

	/**
	 * Returns the wizard collection child object corresponding to the passed
	 * path (relative to this object), or <code>null</code> if such an object
	 * could not be found.
	 * 
	 * @param inPath
	 *            org.eclipse.core.runtime.IPath
	 * @return WizardCollectionElement
	 */
	public WizardCollectionElement findChildCollection(final IPath inPath) {
		final Object[] lChildren = getChildren(null);
		final String lSearchString = inPath.segment(0);
		for (int i = 0; i < lChildren.length; ++i) {
			final WizardCollectionElement lCurrentCategory = (WizardCollectionElement) lChildren[i];
			if (lCurrentCategory.getId().equals(lSearchString)) {
				if (inPath.segmentCount() == 1) {
					return lCurrentCategory;
				}

				return lCurrentCategory.findChildCollection(inPath
						.removeFirstSegments(1));
			}
		}
		return null;
	}

	/**
	 * Returns true if this element has no children and no wizards.
	 * 
	 * @return whether it is empty
	 */
	public boolean isEmpty() {
		return size() == 0 && wizards.size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.AdaptableList#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder("WizardCollection, "); //$NON-NLS-1$
		out.append(children.size());
		out.append(" children, "); //$NON-NLS-1$
		out.append(wizards.size());
		out.append(" wizards"); //$NON-NLS-1$
		return new String(out);
	}

	/**
	 * Return the wizards minus the wizards which failed the expressions check.
	 * 
	 * @return WorkbenchWizardElement[] the wizards
	 */
	public WorkbenchWizardElement[] getWorkbenchWizardElements() {
		return getWorkbenchWizardElementsExpression((WorkbenchWizardElement[]) wizards
				.getTypedChildren(WorkbenchWizardElement.class));
	}

	/**
	 * Takes an array of <code>WorkbenchWizardElement</code> and removes all
	 * entries which fail the Expressions check.
	 * 
	 * @param inWorkbenchWizardElements
	 *            Array of <code>WorkbenchWizardElement</code>.
	 * @return The array minus the elements which failed the Expressions check.
	 */
	private WorkbenchWizardElement[] getWorkbenchWizardElementsExpression(
			final WorkbenchWizardElement[] inWorkbenchWizardElements) {
		final int lSize = inWorkbenchWizardElements.length;
		final List<WorkbenchWizardElement> outResult = new ArrayList<WorkbenchWizardElement>(
				lSize);
		for (int i = 0; i < lSize; i++) {
			final WorkbenchWizardElement lElement = inWorkbenchWizardElements[i];
			if (!WorkbenchActivityHelper.restrictUseOf(lElement))
				outResult.add(lElement);
		}
		return outResult.toArray(new WorkbenchWizardElement[outResult.size()]);
	}

}
