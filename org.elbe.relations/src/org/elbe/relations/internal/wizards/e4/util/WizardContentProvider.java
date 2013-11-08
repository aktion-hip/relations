package org.elbe.relations.internal.wizards.e4.util;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.AdaptableList;

/**
 * Provider used by the NewWizardNewPage.
 * 
 * @see org.eclipse.ui.internal.dialogs.WizardContentProvider
 */
public class WizardContentProvider implements ITreeContentProvider {

	private AdaptableList input;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		input = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof WizardCollectionElement) {
			final ArrayList list = new ArrayList();
			final WizardCollectionElement element = (WizardCollectionElement) parentElement;

			final Object[] childCollections = element.getChildren();
			for (int i = 0; i < childCollections.length; i++) {
				handleChild(childCollections[i], list);
			}

			final Object[] childWizards = element.getWizards();
			for (int i = 0; i < childWizards.length; i++) {
				handleChild(childWizards[i], list);
			}

			// flatten lists with only one category
			if (list.size() == 1
					&& list.get(0) instanceof WizardCollectionElement) {
				return getChildren(list.get(0));
			}

			return list.toArray();
		} else if (parentElement instanceof AdaptableList) {
			final AdaptableList aList = (AdaptableList) parentElement;
			final Object[] children = aList.getChildren();
			final ArrayList list = new ArrayList(children.length);
			for (int i = 0; i < children.length; i++) {
				handleChild(children[i], list);
			}
			// if there is only one category, return it's children directly
			// (flatten list)
			if (list.size() == 1
					&& list.get(0) instanceof WizardCollectionElement) {
				return getChildren(list.get(0));
			}

			return list.toArray();
		} else {
			return new Object[0];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
	 * )
	 */
	@Override
	public Object getParent(final Object element) {
		if (element instanceof WizardCollectionElement) {
			final Object[] children = input.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i].equals(element)) {
					return input;
				}
			}
			return ((WizardCollectionElement) element).getParent(element);
		}
		return null;
	}

	/**
	 * Adds the item to the list, unless it's a collection element without any
	 * children.
	 * 
	 * @param element
	 *            the element to test and add
	 * @param list
	 *            the <code>Collection</code> to add to.
	 * @since 3.0
	 */
	private void handleChild(final Object element, final ArrayList list) {
		if (element instanceof WizardCollectionElement) {
			if (hasChildren(element)) {
				list.add(element);
			}
		} else {
			list.add(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof WizardCollectionElement) {
			if (getChildren(element).length > 0) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput,
			final Object newInput) {
		input = (AdaptableList) newInput;
	}
}
