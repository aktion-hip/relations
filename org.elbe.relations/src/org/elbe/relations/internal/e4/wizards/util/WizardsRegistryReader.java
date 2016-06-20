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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class WizardsRegistryReader {

	// constants
	/**
	 * Examples wizard category id
	 */
	public final static String FULL_EXAMPLES_WIZARD_CATEGORY = "org.eclipse.ui.Examples";//$NON-NLS-1$
	/**
	 * General wizard category id
	 */
	final public static String GENERAL_WIZARD_CATEGORY = "org.eclipse.ui.Basic"; //$NON-NLS-1$

	private final static String CATEGORY_SEPARATOR = "/";//$NON-NLS-1$
	final public static String UNCATEGORIZED_WIZARD_CATEGORY = "org.eclipse.ui.Other"; //$NON-NLS-1$
	final private static String UNCATEGORIZED_WIZARD_CATEGORY_LABEL = "other"; //$NON-NLS-1$

	@SuppressWarnings("rawtypes")
	private static final Comparator comparer = new Comparator() {
		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(final Object inArg0, final Object inArg1) {
			final String lPath1 = ((CategoryNode) inArg0).getPath();
			final String lPath2 = ((CategoryNode) inArg1).getPath();
			return collator.compare(lPath1, lPath2);
		}
	};

	private final String plugin;
	private final String pluginPoint;
	private final IEclipseContext context;
	private final IExtensionRegistry registry;
	private final Logger log;

	private WizardCollectionElement wizardElements = null;
	private final boolean readAll = true;

	private Set<String> deferPrimary;
	private ArrayList<Category> deferCategories = null;
	private ArrayList<WorkbenchWizardElement> deferWizards = null;
	private WorkbenchWizardElement[] primaryWizards = new WorkbenchWizardElement[0];

	/**
	 * WizardsRegistryReader constructor.
	 *
	 * @param inPlugin
	 *            String the plugin id
	 * @param inPluginPoint
	 *            String the extension point id
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @param inRegistry
	 *            {@link IExtensionRegistry}
	 * @param inLog
	 *            {@link Logger}
	 */
	public WizardsRegistryReader(final String inPlugin,
	        final String inPluginPoint, final IEclipseContext inContext,
	        final IExtensionRegistry inRegistry, final Logger inLog) {
		context = inContext;
		registry = inRegistry;
		log = inLog;
		plugin = inPlugin;
		pluginPoint = inPluginPoint;
	}

	/**
	 * Returns whether the wizards have been read already.
	 *
	 * @return boolean
	 */
	private boolean areWizardsRead() {
		return wizardElements != null && readAll;
	}

	/**
	 * Returns a list of wizards, project and not.
	 *
	 * The return value for this method is cached since computing its value
	 * requires non-trivial work.
	 *
	 * @return {@link WizardCollectionElement} the wizard collection
	 */
	public WizardCollectionElement getWizardElements() {
		if (!areWizardsRead()) {
			readWizards();
		}
		return wizardElements;
	}

	/**
	 * Reads the wizards in a registry.
	 * <p>
	 * This implementation uses a defering strategy. All of the elements
	 * (categories, wizards) are read. The categories are created as the read
	 * occurs. The wizards are just stored for later addition after the read
	 * completes. This ensures that wizard categorization is performed after all
	 * categories have been read.
	 * </p>
	 */
	private void readWizards() {
		if (readAll) {
			if (!areWizardsRead()) {
				createEmptyWizardCollection();
				readRegistry(registry, plugin, pluginPoint);
			}
		}
		finishCategories();
		finishWizards();
		finishPrimary();
		if (wizardElements != null) {
			pruneEmptyCategories(wizardElements);
		}
	}

	private void finishPrimary() {
		if (deferPrimary != null) {
			final ArrayList<WorkbenchWizardElement> lPrimary = new ArrayList<WorkbenchWizardElement>();
			for (final Iterator<String> lIds = deferPrimary.iterator(); lIds
			        .hasNext();) {
				final String lId = lIds.next();
				final WorkbenchWizardElement lElement = wizardElements == null
				        ? null : wizardElements.findWizard(lId, true);
				if (lElement != null) {
					lPrimary.add(lElement);
				}
			}
			primaryWizards = lPrimary
			        .toArray(new WorkbenchWizardElement[lPrimary.size()]);
			deferPrimary = null;
		}
	}

	/**
	 * Removes the empty categories from a wizard collection.
	 */
	private void pruneEmptyCategories(final WizardCollectionElement inParent) {
		final Object[] lChildren = inParent.getChildren(null);
		for (int i = 0; i < lChildren.length; i++) {
			final WizardCollectionElement lChild = (WizardCollectionElement) lChildren[i];
			pruneEmptyCategories(lChild);
			final boolean lShouldPrune = lChild.getId()
			        .equals(FULL_EXAMPLES_WIZARD_CATEGORY);
			if (lChild.isEmpty() && lShouldPrune) {
				inParent.remove(lChild);
			}
		}
	}

	private void finishWizards() {
		if (deferWizards != null) {
			final Iterator<WorkbenchWizardElement> lWizards = deferWizards
			        .iterator();
			while (lWizards.hasNext()) {
				final WorkbenchWizardElement lWizard = lWizards.next();
				final IConfigurationElement lConfig = lWizard
				        .getConfigurationElement();
				finishWizard(lWizard, lConfig);
			}
			deferWizards = null;
		}
	}

	private void finishWizard(final WorkbenchWizardElement inElement,
	        final IConfigurationElement inConfig) {
		final StringTokenizer lFamilyTokenizer = new StringTokenizer(
		        getCategoryStringFor(inConfig), CATEGORY_SEPARATOR);

		// use the period-separated sections of the current Wizard's category
		// to traverse through the NamedSolution "tree" that was previously
		// created
		WizardCollectionElement lCurrentCollectionElement = wizardElements;
		boolean lMoveToOther = false;

		while (lFamilyTokenizer.hasMoreElements()) {
			final WizardCollectionElement lTempCollectionElement = getChildWithID(
			        lCurrentCollectionElement, lFamilyTokenizer.nextToken());
			if (lTempCollectionElement == null) {
				// can't find the path; bump it to uncategorized
				lMoveToOther = true;
				break;
			}
			lCurrentCollectionElement = lTempCollectionElement;
		}

		if (lMoveToOther) {
			moveElementToUncategorizedCategory(wizardElements, inElement);
		} else {
			lCurrentCollectionElement.add(inElement);
			inElement.setParent(lCurrentCollectionElement);
		}
	}

	private void moveElementToUncategorizedCategory(
	        final WizardCollectionElement inRoot,
	        final WorkbenchWizardElement inElement) {
		WizardCollectionElement lOtherCategory = getChildWithID(inRoot,
		        UNCATEGORIZED_WIZARD_CATEGORY);

		if (lOtherCategory == null) {
			lOtherCategory = createCollectionElement(inRoot,
			        UNCATEGORIZED_WIZARD_CATEGORY, null,
			        UNCATEGORIZED_WIZARD_CATEGORY_LABEL);
		}

		lOtherCategory.add(inElement);
		inElement.setParent(lOtherCategory);
	}

	/**
	 * Create and answer a new WizardCollectionElement, configured as a child of
	 * <code>parent</code>
	 *
	 * @return org.eclipse.ui.internal.model.WizardCollectionElement
	 * @param inParent
	 *            org.eclipse.ui.internal.model.WizardCollectionElement
	 * @param inId
	 *            the id of the new collection
	 * @param inPluginId
	 *            the originating plugin id of the collection, if any.
	 *            <code>null</code> otherwise.
	 * @param inLabel
	 *            java.lang.String
	 */
	private WizardCollectionElement createCollectionElement(
	        final WizardCollectionElement inParent, final String inId,
	        final String inPluginId, final String inLabel) {
		final WizardCollectionElement outElement = new WizardCollectionElement(
		        inId, inPluginId, inLabel, inParent);
		inParent.add(outElement);
		return outElement;
	}

	private String getCategoryStringFor(final IConfigurationElement inConfig) {
		String outResult = inConfig
		        .getAttribute(IWorkbenchRegistryConstants.TAG_CATEGORY);
		if (outResult == null) {
			outResult = UNCATEGORIZED_WIZARD_CATEGORY;
		}
		return outResult;
	}

	/**
	 * Finishes the addition of categories. The categories are sorted and added
	 * in a root to depth traversal.
	 */
	@SuppressWarnings("unchecked")
	private void finishCategories() {
		// If no categories just return.
		if (deferCategories == null) {
			return;
		}

		// Sort categories by flattened name.
		final CategoryNode[] lFlatArray = new CategoryNode[deferCategories
		        .size()];
		for (int i = 0; i < deferCategories.size(); i++) {
			lFlatArray[i] = new CategoryNode(deferCategories.get(i));
		}
		Collections.sort(Arrays.asList(lFlatArray), comparer);

		// Add each category.
		for (int nX = 0; nX < lFlatArray.length; nX++) {
			final Category lCategory = lFlatArray[nX].getCategory();
			finishCategory(lCategory);
		}

		// Cleanup.
		deferCategories = null;
	}

	private void finishCategory(final Category inCategory) {
		final String[] lCategoryPath = inCategory.getParentPath();
		WizardCollectionElement lParent = wizardElements; // ie.- root

		// Traverse down into parent category.
		if (lCategoryPath != null) {
			for (int i = 0; i < lCategoryPath.length; i++) {
				final WizardCollectionElement lTempElement = getChildWithID(
				        lParent, lCategoryPath[i]);
				if (lTempElement == null) {
					// The parent category is invalid. By returning here the
					// category will be dropped and any wizard within the
					// category
					// will be added to the "Other" category.
					return;
				}
				lParent = lTempElement;
			}
		}

		if (lParent != null) {
			// If another category already exists with the same id ignore this
			// one.
			final Object lTest = getChildWithID(lParent, inCategory.getId());
			if (lTest != null) {
				return;
			}

			createCollectionElement(lParent,
			        Util.getAdapter(inCategory, IConfigurationElement.class));
		}
	}

	private WizardCollectionElement createCollectionElement(
	        final WizardCollectionElement inParent,
	        final IConfigurationElement inElement) {
		final WizardCollectionElement outElement = new WizardCollectionElement(
		        inElement, inParent);

		inParent.add(outElement);
		return outElement;
	}

	private WizardCollectionElement getChildWithID(
	        final WizardCollectionElement inParent, final String inId) {
		final Object[] lChildren = inParent.getChildren(null);
		for (int i = 0; i < lChildren.length; ++i) {
			final WizardCollectionElement outCurrentChild = (WizardCollectionElement) lChildren[i];
			if (outCurrentChild.getId().equals(inId)) {
				return outCurrentChild;
			}
		}
		return null;
	}

	private void readRegistry(final IExtensionRegistry inRegistry,
	        final String inPlugin, final String inPluginPoint) {
		final IExtensionPoint lPoint = inRegistry.getExtensionPoint(inPlugin,
		        inPluginPoint);
		if (lPoint == null) {
			return;
		}
		IExtension[] lExtensions = lPoint.getExtensions();
		lExtensions = orderExtensions(lExtensions);
		for (int i = 0; i < lExtensions.length; i++) {
			readExtension(lExtensions[i]);
		}
	}

	private void readExtension(final IExtension inExtension) {
		readElements(inExtension.getConfigurationElements());
	}

	private void readElements(final IConfigurationElement[] inElements) {
		for (int i = 0; i < inElements.length; i++) {
			if (!readElement(inElements[i])) {
				logUnknownElement(inElements[i]);
			}
		}
	}

	private boolean readElement(final IConfigurationElement inElement) {
		if (inElement.getName()
		        .equals(IWorkbenchRegistryConstants.TAG_CATEGORY)) {
			deferCategory(inElement);
			return true;
		} else if (inElement.getName()
		        .equals(IWorkbenchRegistryConstants.TAG_PRIMARYWIZARD)) {
			if (deferPrimary == null) {
				deferPrimary = new HashSet<String>();
			}
			deferPrimary.add(
			        inElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID));
			return true;
		} else {
			if (!inElement.getName()
			        .equals(IWorkbenchRegistryConstants.TAG_WIZARD)) {
				return false;
			}
			final WorkbenchWizardElement lWizard = createWizardElement(
			        inElement);
			if (lWizard != null) {
				addNewElementToResult(lWizard, inElement);
			}
			return true;
		}
	}

	private void addNewElementToResult(final WorkbenchWizardElement inElement,
	        final IConfigurationElement inConfig) {
		deferWizard(inElement);
	}

	private void deferWizard(final WorkbenchWizardElement inElement) {
		if (deferWizards == null) {
			deferWizards = new ArrayList<WorkbenchWizardElement>(50);
		}
		deferWizards.add(inElement);
	}

	private WorkbenchWizardElement createWizardElement(
	        final IConfigurationElement inElement) {
		// WizardElements must have a name attribute
		if (inElement
		        .getAttribute(IWorkbenchRegistryConstants.ATT_NAME) == null) {
			logMissingAttribute(inElement,
			        IWorkbenchRegistryConstants.ATT_NAME);
			return null;
		}

		if (getClassValue(inElement,
		        IWorkbenchRegistryConstants.ATT_CLASS) == null) {
			logMissingAttribute(inElement,
			        IWorkbenchRegistryConstants.ATT_CLASS);
			return null;
		}
		return new WorkbenchWizardElement(inElement, context);
	}

	private String getClassValue(final IConfigurationElement inConfigElement,
	        final String inClassAttributeName) {
		final String outClassName = inConfigElement
		        .getAttribute(inClassAttributeName);
		if (outClassName != null) {
			return outClassName;
		}
		final IConfigurationElement[] lCandidateChildren = inConfigElement
		        .getChildren(inClassAttributeName);
		if (lCandidateChildren.length == 0) {
			return null;
		}

		return lCandidateChildren[0]
		        .getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
	}

	private void deferCategory(final IConfigurationElement inConfig) {
		// Create category.
		Category category = null;
		try {
			category = new Category(inConfig);
		}
		catch (final CoreException exc) {
			log.error(exc, "Cannot create category: " + exc.getStatus()); //$NON-NLS-1$
			return;
		}

		// Defer for later processing.
		if (deferCategories == null) {
			deferCategories = new ArrayList<Category>(20);
		}
		deferCategories.add(category);
	}

	/**
	 * Creates empty element collection. Overrider to fill initial elements, if
	 * needed.
	 */
	private void createEmptyWizardCollection() {
		wizardElements = new WizardCollectionElement("root", null, "root", //$NON-NLS-1$//$NON-NLS-2$
		        null);
	}

	/**
	 * Returns the list of wizards that are considered 'primary'.
	 *
	 * The return value for this method is cached since computing its value
	 * requires non-trivial work.
	 *
	 * @return the primary wizards
	 */
	public WorkbenchWizardElement[] getPrimaryWizards() {
		if (!areWizardsRead()) {
			readWizards();
		}
		return primaryWizards;
		// return (WorkbenchWizardElement[]) WorkbenchActivityHelper
		// .restrictArray(primaryWizards);
	}

	/**
	 * Apply a reproducible order to the list of extensions provided, such that
	 * the order will not change as extensions are added or removed.
	 *
	 * @param inExtensions
	 *            the extensions to order
	 * @return ordered extensions
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static IExtension[] orderExtensions(
	        final IExtension[] inExtensions) {
		// By default, the order is based on plugin id sorted
		// in ascending order. The order for a plugin providing
		// more than one extension for an extension point is
		// dependent in the order listed in the XML file.
		final IExtension[] lSortedExtension = new IExtension[inExtensions.length];
		System.arraycopy(inExtensions, 0, lSortedExtension, 0,
		        inExtensions.length);
		final Comparator lComparer = new Comparator() {
			@Override
			public int compare(final Object inArg0, final Object inArg1) {
				final String lId1 = ((IExtension) inArg0)
		                .getNamespaceIdentifier();
				final String lId2 = ((IExtension) inArg1)
		                .getNamespaceIdentifier();
				return lId1.compareToIgnoreCase(lId2);
			}
		};
		Collections.sort(Arrays.asList(lSortedExtension), lComparer);
		return lSortedExtension;
	}

	// --- log ---

	private void logError(final IConfigurationElement inElement,
	        final String inText) {
		final IExtension lExtension = inElement.getDeclaringExtension();
		final StringBuilder lMessage = new StringBuilder();
		lMessage.append("Plugin ").append(lExtension.getNamespaceIdentifier()) //$NON-NLS-1$
		        .append(", extension ") //$NON-NLS-1$
		        .append(lExtension.getExtensionPointUniqueIdentifier());
		// look for an ID if available - this should help debugging
		final String lId = inElement.getAttribute("id"); //$NON-NLS-1$
		if (lId != null) {
			lMessage.append(", id "); //$NON-NLS-1$
			lMessage.append(lId);
		}
		lMessage.append(": ").append(inText);//$NON-NLS-1$
		log.warn(new String(lMessage));
	}

	private void logUnknownElement(final IConfigurationElement inElement) {
		logError(inElement,
		        "Unknown extension tag found: " + inElement.getName());//$NON-NLS-1$
	}

	private void logMissingAttribute(final IConfigurationElement inElement,
	        final String inAttributeName) {
		logError(inElement,
		        "Required attribute '" + inAttributeName + "' not defined");//$NON-NLS-2$//$NON-NLS-1$
	}

	// --- inner classes ---

	private class CategoryNode {
		private final Category category;
		private String path;

		CategoryNode(final Category inCategory) {
			category = inCategory;
			path = ""; //$NON-NLS-1$
			final String[] lCategoryPath = category.getParentPath();
			if (lCategoryPath != null) {
				for (int lX = 0; lX < lCategoryPath.length; lX++) {
					path += lCategoryPath[lX] + '/';
				}
			}
			path += inCategory.getId();
		}

		String getPath() {
			return path;
		}

		Category getCategory() {
			return category;
		}
	}

}
