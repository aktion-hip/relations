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
 * Interface containing various registry constants (tag and attribute names).
 *
 * @author Luthiger <br />
 *         see org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants
 */
public interface IWorkbenchRegistryConstants {
	/**
	 * Category tag. Value <code>category</code>.
	 */
	public static String TAG_CATEGORY = "category";//$NON-NLS-1$
	/**
	 * Primary wizard tag. Value <code>primaryWizard</code>.
	 */
	public static String TAG_PRIMARYWIZARD = "primaryWizard"; //$NON-NLS-1$
	/**
	 * Id attribute. Value <code>id</code>.
	 */
	public static String ATT_ID = "id"; //$NON-NLS-1$
	/**
	 * Wizard tag. Value <code>wizard</code>.
	 */
	public static String TAG_WIZARD = "wizard";//$NON-NLS-1$
	/**
	 * Class attribute. Value <code>class</code>.
	 */
	public static String ATT_CLASS = "class"; //$NON-NLS-1$
	/**
	 * Name attribute. Value <code>name</code>.
	 */
	public static String ATT_NAME = "name"; //$NON-NLS-1$
	/**
	 * Icon attribute. Value <code>icon</code>.
	 */
	public static String ATT_ICON = "icon"; //$NON-NLS-1$
	/**
	 * Description image attribute. Value <code>descriptionImage</code>.
	 */
	public static String ATT_DESCRIPTION_IMAGE = "descriptionImage"; //$NON-NLS-1$
	/**
	 * Attribute that specifies whether a wizard is immediately capable of
	 * finishing. Value <code>canFinishEarly</code>.
	 */
	public static String ATT_CAN_FINISH_EARLY = "canFinishEarly"; //$NON-NLS-1$
	/**
	 * Attribute that specifies whether a wizard has any pages. Value
	 * <code>hasPages</code>.
	 */
	public static String ATT_HAS_PAGES = "hasPages"; //$NON-NLS-1$
	/**
	 * Help url attribute. Value <code>helpHref</code>.
	 */
	public static String ATT_HELP_HREF = "helpHref"; //$NON-NLS-1$
	/**
	 * The name of the category attribute, which appears on a command
	 * definition.
	 */
	public static String ATT_CATEGORY = "category"; //$NON-NLS-1$

	/**
	 * Description element. Value <code>description</code>.
	 */
	public static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * View parent category attribute. Value <code>parentCategory</code>.
	 */
	public static String ATT_PARENT_CATEGORY = "parentCategory"; //$NON-NLS-1$

}
