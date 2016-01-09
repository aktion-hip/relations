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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Category provides for hierarchical grouping of elements registered in the
 * registry. One extension normally defines a category, and other reference it
 * via its ID.
 * <p>
 * A category may specify its parent category in order to achieve hierarchy.
 * </p>
 */
@SuppressWarnings("restriction")
public class Category
        implements IWorkbenchAdapter, IPluginContribution, IAdaptable {

	/**
	 * Identifier of the miscellaneous category
	 */
	public final static String MISC_ID = "org.eclipse.ui.internal.otherCategory"; //$NON-NLS-1$

	private final String id;

	private String name;

	private String[] parentPath;

	private ArrayList<Object> elements;

	private IConfigurationElement configurationElement;

	private String pluginId;

	/**
	 * Creates an instance of <code>Category</code> as a miscellaneous category.
	 */
	public Category() {
		id = MISC_ID;
		name = "other";
		pluginId = MISC_ID; // TODO: remove hack for bug 55172
	}

	/**
	 * Creates an instance of <code>Category</code> with an ID and label.
	 *
	 * @param id
	 *            the unique identifier for the category
	 * @param label
	 *            the presentation label for this category
	 */
	public Category(final String id, final String label) {
		this.id = id;
		name = label;
	}

	/**
	 * Creates an instance of <code>Category</code> using the information from
	 * the specified configuration element.
	 *
	 * @param configElement
	 *            the <code>IConfigurationElement<code> containing the ID,
	 *            label, and optional parent category path.
	 * @throws WorkbenchException
	 *             if the ID or label is <code>null</code
	 */
	public Category(final IConfigurationElement configElement)
	        throws WorkbenchException {
		id = configElement.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		configurationElement = configElement;
		if (id == null || getLabel() == null) {
			throw new WorkbenchException("Invalid category: " + id); //$NON-NLS-1$
		}
	}

	/**
	 * Add an element to this category.
	 *
	 * @param element
	 *            the element to add
	 */
	public void addElement(final Object element) {
		if (elements == null) {
			elements = new ArrayList<Object>(5);
		}
		elements.add(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return (T) this;
		} else if (adapter == IConfigurationElement.class) {
			return (T) configurationElement;
		} else {
			return null;
		}
	}

	@Override
	public Object[] getChildren(final Object o) {
		return getElements().toArray();
	}

	@Override
	public ImageDescriptor getImageDescriptor(final Object object) {
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}

	@Override
	public String getLabel(final Object o) {
		return getLabel();
	}

	/**
	 * Return the id for this category.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return the label for this category.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return configurationElement == null ? name
		        : configurationElement
		                .getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}

	/**
	 * Return the parent path for this category.
	 *
	 * @return the parent path
	 */
	public String[] getParentPath() {
		if (parentPath != null) {
			return parentPath;
		}

		final String unparsedPath = getRawParentPath();
		if (unparsedPath != null) {
			final StringTokenizer stok = new StringTokenizer(unparsedPath, "/"); //$NON-NLS-1$
			parentPath = new String[stok.countTokens()];
			for (int i = 0; stok.hasMoreTokens(); i++) {
				parentPath[i] = stok.nextToken();
			}
		}

		return parentPath;
	}

	/**
	 * Return the unparsed parent path. May be <code>null</code>.
	 *
	 * @return the unparsed parent path or <code>null</code>
	 */
	public String getRawParentPath() {
		return configurationElement == null ? null
		        : configurationElement.getAttribute(
		                IWorkbenchRegistryConstants.ATT_PARENT_CATEGORY);
	}

	/**
	 * Return the root path for this category.
	 *
	 * @return the root path
	 */
	public String getRootPath() {
		final String[] path = getParentPath();
		if (path != null && path.length > 0) {
			return path[0];
		}

		return id;
	}

	/**
	 * Return the elements contained in this category.
	 *
	 * @return the elements
	 */
	public ArrayList<Object> getElements() {
		return elements;
	}

	/**
	 * Return whether a given object exists in this category.
	 *
	 * @param o
	 *            the object to search for
	 * @return whether the object is in this category
	 */
	public boolean hasElement(final Object o) {
		if (elements == null) {
			return false;
		}
		if (elements.isEmpty()) {
			return false;
		}
		return elements.contains(o);
	}

	/**
	 * Return whether this category has child elements.
	 *
	 * @return whether this category has child elements
	 */
	public boolean hasElements() {
		if (elements != null) {
			return !elements.isEmpty();
		}

		return false;
	}

	@Override
	public Object getParent(final Object o) {
		return null;
	}

	@Override
	public String getLocalId() {
		return id;
	}

	@Override
	public String getPluginId() {
		return configurationElement == null ? pluginId
		        : configurationElement.getNamespaceIdentifier();
	}

	/**
	 * Clear all elements from this category.
	 *
	 * @since 3.1
	 */
	public void clear() {
		if (elements != null) {
			elements.clear();
		}
	}
}
