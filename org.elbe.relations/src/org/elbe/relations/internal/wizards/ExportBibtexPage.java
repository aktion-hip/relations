/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
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
package org.elbe.relations.internal.wizards;

import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.WizardHelper;
import org.elbe.relations.utility.DialogSettingHandler;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Page for exporting the content of all text items to a file in BibTEX format.
 * 
 * @author Luthiger Created on 03.05.2007
 */
public class ExportBibtexPage extends ExportWizardPage {
	private Combo exportFileName;
	private final DialogSettingHandler settings;

	/**
	 * @param inPageName
	 */
	public ExportBibtexPage(final String inPageName) {
		super(inPageName);
		setTitle(RelationsMessages.getString("ExportBibtexPage.page.title")); //$NON-NLS-1$
		setInfoMessage(RelationsMessages.getString("ExportBibtexPage.page.msg")); //$NON-NLS-1$
		settings = new DialogSettingHandler(
				"ExportBibtex", "RecentExportBibtex"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createControl(final Composite inParent) {
		final int lColumns = 3;
		final Composite lComposite = WizardHelper.createComposite(inParent,
				lColumns);

		createLabel(
				lComposite,
				RelationsMessages.getString("ExportBibtexPage.lbl.messag"), lColumns); //$NON-NLS-1$

		exportFileName = createLabelCombo(
				lComposite,
				RelationsMessages.getString("ExportBibtexPage.lbl.input"), SWT.DROP_DOWN); //$NON-NLS-1$
		createButtonFileDialog(lComposite,
				RelationsMessages.getString("PrintOutWizardPage.lbl.browse")); //$NON-NLS-1$

		setControl(lComposite);
		initializeValues();
	}

	@Override
	protected void openFileDialog() {
		fileNameStatus = Status.OK_STATUS;
		final FileDialog lDialog = new FileDialog(Display.getCurrent()
				.getActiveShell(), SWT.SAVE);
		lDialog.setText(RelationsMessages
				.getString("ExportBibtexPage.filedlg.msg")); //$NON-NLS-1$
		lDialog.setFilterExtensions(new String[] { "*.bib" }); //$NON-NLS-1$
		lDialog.setFilterNames(new String[] { RelationsMessages
				.getString("ExportBibtexPage.filedlg.names") }); //$NON-NLS-1$
		final String lFileName = lDialog.open();
		if (lFileName == null) {
			fileNameStatus = nameEmpty;
		} else {
			exportFileName.setText(lFileName);
			checkFileExists(lFileName);
		}
		updateStatus(fileNameStatus);
		exportFileName.setFocus();
	}

	private void initializeValues() {
		exportFileName.setItems(settings.getRecentValues());
		exportFileName.setFocus();

		setPageComplete(false);

		addListeners(exportFileName);
	}

	@Override
	protected void modifiedCheck(final String inText) {
		focusLostCheck(inText);
	}

	/**
	 * Friendly method to save the value entered in <code>exportFileName</code>
	 * to the dialog's history.
	 * 
	 * @throws BackingStoreException
	 */
	void saveToHistory() throws BackingStoreException {
		settings.saveToHistory(exportFileName.getText());
	}

	/**
	 * @return String Name of the BibTEX file the content has to be exported to.
	 */
	public String getFileName() {
		return exportFileName.getText();
	}

	@Override
	protected boolean getPageComplete() {
		if (exportFileName.getText().length() == 0) {
			return false;
		}
		return true;
	}

}
