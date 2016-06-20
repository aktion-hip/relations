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
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.AdaptableList;

/**
 * Provider used by the NewWizardNewPage.
 *
 * <br />
 * see org.eclipse.ui.internal.dialogs.WizardContentProvider
 */
public class WizardContentProvider implements ITreeContentProvider {

	private AdaptableList input;

	@Override
	public void dispose() {
		input = null;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof WizardCollectionElement) {
			final List<Object> list = new ArrayList<Object>();
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
			final List<Object> list = new ArrayList<Object>(children.length);
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

	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

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
	private void handleChild(final Object element, final List<Object> list) {
		if (element instanceof WizardCollectionElement) {
			if (hasChildren(element)) {
				list.add(element);
			}
		} else {
			list.add(element);
		}
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof WizardCollectionElement) {
			if (getChildren(element).length > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput,
	        final Object newInput) {
		input = (AdaptableList) newInput;
	}
}
