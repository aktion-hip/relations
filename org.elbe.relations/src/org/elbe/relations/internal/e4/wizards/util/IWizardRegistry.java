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

/**
 * A registry describing all wizard extensions known to the workbench.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWizardRegistry {

	/**
	 * Find a wizard with the given id.
	 *
	 * @param id
	 *            the id to search for
	 * @return the wizard descriptor matching the given id or <code>null</code>
	 */
	IWizardDescriptor findWizard(String id);

	/**
	 * Return the wizards that have been designated as "primary".
	 *
	 * @return the primary wizard descriptors. Never <code>null</code>.
	 */
	IWizardDescriptor[] getPrimaryWizards();

	/**
	 * Find the category with the given id.
	 *
	 * @param id
	 *            the id of the category to search for
	 * @return the category matching the given id or <code>null</code>
	 */
	IWizardCategory findCategory(String id);

	/**
	 * Return the root category.
	 *
	 * @return the root category. Never <code>null</code>.
	 */
	IWizardCategory getRootCategory();
}
