package org.elbe.relations.internal.wizards.e4.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Category provides for hierarchical grouping of elements registered in the
 * registry. One extension normally defines a category, and other reference it
 * via its ID.
 * <p>
 * A category may specify its parent category in order to achieve hierarchy.
 * </p>
 */
public class Category implements IWorkbenchAdapter, IPluginContribution,
		IAdaptable {

	/**
	 * Identifier of the miscellaneous category
	 */
	public final static String MISC_ID = "org.eclipse.ui.internal.otherCategory"; //$NON-NLS-1$

	private final String id;

	private String name;

	private String[] parentPath;

	private ArrayList elements;

	private IConfigurationElement configurationElement;

	private String pluginId;

	/**
	 * Creates an instance of <code>Category</code> as a miscellaneous category.
	 */
	public Category() {
		this.id = MISC_ID;
		this.name = "other";
		this.pluginId = MISC_ID; // TODO: remove hack for bug 55172
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
		this.name = label;
	}

	/**
	 * Creates an instance of <code>Category</code> using the information from
	 * the specified configuration element.
	 * 
	 * @param configElement
	 *            the <code>IConfigurationElement<code> containing
	 * 		the ID, label, and optional parent category path.
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
			elements = new ArrayList(5);
		}
		elements.add(element);
	}

	/*
	 * (non-Javadoc) Method declared on IAdaptable.
	 */
	@Override
	public Object getAdapter(final Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		} else if (adapter == IConfigurationElement.class) {
			return configurationElement;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchAdapter.
	 */
	@Override
	public Object[] getChildren(final Object o) {
		return getElements().toArray();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchAdapter.
	 */
	@Override
	public ImageDescriptor getImageDescriptor(final Object object) {
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchAdapter.
	 */
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
		return configurationElement == null ? name : configurationElement
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
		return configurationElement == null ? null : configurationElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_PARENT_CATEGORY);
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
	public ArrayList getElements() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(final Object o) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
	 */
	@Override
	public String getLocalId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
	 */
	@Override
	public String getPluginId() {
		return configurationElement == null ? pluginId : configurationElement
				.getNamespace();
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
