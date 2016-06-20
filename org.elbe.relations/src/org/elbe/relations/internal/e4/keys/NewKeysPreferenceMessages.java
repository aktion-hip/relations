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
package org.elbe.relations.internal.e4.keys;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the New Keys Preference page.
 *
 * @since 3.2
 *
 */
public class NewKeysPreferenceMessages extends NLS {
	private static final String BUNDLE_NAME = "org.elbe.relations.internal.e4.keys.RelationsKeysPreferencePage";//$NON-NLS-1$

	public static String AddBindingButton_Text;
	public static String AddKeyButton_ToolTipText;
	public static String FiltersButton_Text;
	public static String ExportButton_Text;
	public static String BindingLabel_Text;
	public static String CommandNameColumn_Text;
	public static String CategoryColumn_Text;
	public static String UserColumn_Text;
	public static String CommandNameLabel_Text;
	public static String CommandDescriptionLabel_Text;
	// public static String DeleteSchemeButton_Text;
	public static String ConflictsLabel_Text;
	public static String RemoveBindingButton_Text;
	public static String RestoreBindingButton_Text;
	public static String SchemeLabel_Text;
	public static String TriggerSequenceColumn_Text;
	public static String WhenColumn_Text;
	public static String WhenLabel_Text;
	// public static String Asterisk_Text;

	// public static String GroupingCombo_Label;
	// public static String GroupingCombo_Category_Text;
	// public static String GroupingCombo_None_Text;
	// public static String GroupingCombo_When_Text;

	// public static String PreferenceStoreError_Message;
	// public static String PreferenceStoreError_Title;

	public static String RestoreDefaultsMessageBoxText;
	public static String RestoreDefaultsMessageBoxMessage;

	public static String Undefined_Command;
	public static String Unavailable_Category;
	// public static String Undefined_Context;

	public static String KeysPreferenceFilterDialog_Title;
	public static String ActionSetFilterCheckBox_Text;
	public static String InternalFilterCheckBox_Text;
	public static String UncategorizedFilterCheckBox_Text;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, NewKeysPreferenceMessages.class);
	}
}
