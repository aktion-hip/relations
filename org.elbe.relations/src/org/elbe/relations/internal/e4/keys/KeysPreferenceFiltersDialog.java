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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.ViewSettingsDialog;

/**
 * Creates a dialog box for applying filter selection of When combo box in
 * NewKeysPreferencePage
 *
 * @since 3.3
 */
public class KeysPreferenceFiltersDialog extends ViewSettingsDialog {

	private Button actionSetFilterCheckBox;
	private Button internalFilterCheckBox;
	private Button uncategorizedFilterCheckBox;

	private boolean filterActionSet;
	private boolean filterInternal;
	private boolean filterUncategorized;
	private boolean filterShowUnboundCommands;

	void setFilterActionSet(final boolean inSetting) {
		filterActionSet = inSetting;
	}

	void setFilterInternal(final boolean inSetting) {
		filterInternal = inSetting;
	}

	void setFilterUncategorized(final boolean inSetting) {
		filterUncategorized = inSetting;
	}

	boolean getFilterActionSet() {
		return filterActionSet;
	}

	boolean getFilterInternal() {
		return filterInternal;
	}

	boolean getFilterUncategorized() {
		return filterUncategorized;
	}

	/**
	 * @param inParentShell
	 */
	public KeysPreferenceFiltersDialog(final Shell inParentShell) {
		super(inParentShell);
	}

	@Override
	protected void performDefaults() {
		actionSetFilterCheckBox.setSelection(true);
		internalFilterCheckBox.setSelection(true);
		uncategorizedFilterCheckBox.setSelection(true);
		super.performDefaults();
	}

	@Override
	protected Control createDialogArea(final Composite inParent) {
		final Composite outTopComposite = (Composite) super.createDialogArea(
		        inParent);
		final GridLayout lLayout = new GridLayout(1, false);
		outTopComposite.setLayout(lLayout);
		outTopComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
		actionSetFilterCheckBox = new Button(outTopComposite, SWT.CHECK);
		actionSetFilterCheckBox.setText(
		        NewKeysPreferenceMessages.ActionSetFilterCheckBox_Text);
		internalFilterCheckBox = new Button(outTopComposite, SWT.CHECK);
		internalFilterCheckBox
		        .setText(NewKeysPreferenceMessages.InternalFilterCheckBox_Text);
		uncategorizedFilterCheckBox = new Button(outTopComposite, SWT.CHECK);
		uncategorizedFilterCheckBox.setText(
		        NewKeysPreferenceMessages.UncategorizedFilterCheckBox_Text);

		actionSetFilterCheckBox.setSelection(filterActionSet);
		internalFilterCheckBox.setSelection(filterInternal);
		uncategorizedFilterCheckBox.setSelection(filterUncategorized);
		applyDialogFont(outTopComposite);

		return outTopComposite;
	}

	@Override
	protected void okPressed() {
		filterActionSet = actionSetFilterCheckBox.getSelection();
		filterInternal = internalFilterCheckBox.getSelection();
		filterUncategorized = uncategorizedFilterCheckBox.getSelection();
		super.okPressed();
	}

	@Override
	protected void configureShell(final Shell inShell) {
		super.configureShell(inShell);
		inShell.setText(
		        NewKeysPreferenceMessages.KeysPreferenceFilterDialog_Title);
	}

	boolean getFilterShowUnboundCommands() {
		return filterShowUnboundCommands;
	}

	void setFilterUnboundCommands(final boolean filterUnboundCommands) {
		this.filterShowUnboundCommands = filterUnboundCommands;
	}

}
