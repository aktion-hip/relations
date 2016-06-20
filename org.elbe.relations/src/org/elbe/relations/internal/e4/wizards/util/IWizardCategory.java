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

import org.eclipse.core.runtime.IPath;

/**
 * A wizard category may contain other categories or wizard elements.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * <br />
 * see org.eclipse.ui.wizards.IWizardCategory
 */
public interface IWizardCategory {

	/**
	 * Returns the category child object corresponding to the passed path
	 * (relative to this object), or <code>null</code> if such an object could
	 * not be found. The segments of this path should correspond to category
	 * ids.
	 *
	 * @param path
	 *            the search path
	 * @return the category or <code>null</code>
	 */
	IWizardCategory findCategory(IPath path);

	/**
	 * Find a wizard that has the provided id. This will search recursivly over
	 * this categories children.
	 *
	 * @param id
	 *            the id to search for
	 * @return the wizard or <code>null</code>
	 */
	IWizardDescriptor findWizard(String id);

	/**
	 * Return the immediate child categories.
	 *
	 * @return the child categories. Never <code>null</code>.
	 */
	IWizardCategory[] getCategories();

	/**
	 * Return the identifier of this category.
	 *
	 * @return the identifier of this category
	 */
	String getId();

	/**
	 * Return the label for this category.
	 *
	 * @return the label for this category
	 */
	String getLabel();

	/**
	 * Return the parent category.
	 *
	 * @return the parent category. May be <code>null</code>.
	 */
	IWizardCategory getParent();

	/**
	 * Return this wizards path. The segments of this path will correspond to
	 * category ids.
	 *
	 * @return the path
	 */
	IPath getPath();

	/**
	 * Return the wizards in this category, minus the wizards which failed the
	 * Expressions check.
	 *
	 * @return the wizards in this category. Never <code>null</code>
	 */
	IWizardDescriptor[] getWizards();
}
