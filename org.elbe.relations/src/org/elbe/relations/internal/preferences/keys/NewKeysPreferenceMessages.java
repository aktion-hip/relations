package org.elbe.relations.internal.preferences.keys;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the New Keys Preference page.
 * 
 * @since 3.2
 * 
 */
public class NewKeysPreferenceMessages extends NLS {
	private static final String BUNDLE_NAME = "org.elbe.relations.internal.preferences.keys.RelationsKeysPreferencePage";//$NON-NLS-1$

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
	public static String DeleteSchemeButton_Text;
	public static String ConflictsLabel_Text;
	public static String RemoveBindingButton_Text;
	public static String RestoreBindingButton_Text;
	public static String SchemeLabel_Text;
	public static String TriggerSequenceColumn_Text;
	public static String WhenColumn_Text;
	public static String WhenLabel_Text;
	public static String Asterisk_Text;

	public static String GroupingCombo_Label;
	public static String GroupingCombo_Category_Text;
	public static String GroupingCombo_None_Text;
	public static String GroupingCombo_When_Text;

	public static String PreferenceStoreError_Message;
	public static String PreferenceStoreError_Title;

	public static String RestoreDefaultsMessageBoxText;
	public static String RestoreDefaultsMessageBoxMessage;

	public static String Undefined_Command;
	public static String Unavailable_Category;
	public static String Undefined_Context;

	public static String KeysPreferenceFilterDialog_Title;
	public static String ActionSetFilterCheckBox_Text;
	public static String InternalFilterCheckBox_Text;
	public static String UncategorizedFilterCheckBox_Text;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, NewKeysPreferenceMessages.class);
	}
}
