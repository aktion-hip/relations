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

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.WizardHelper;

/**
 * Page displaying the input field to enter the file name for the data backup.
 * 
 * @author Luthiger Created on 25.04.2007
 */
public class BackupEmbeddedPage extends ExportWizardPage {

	private Text backupFileName;

	protected BackupEmbeddedPage(final String inName) {
		super(inName);
		setTitle(RelationsMessages.getString("BackupEmbeddedPage.page.title")); //$NON-NLS-1$
		setInfoMessage(RelationsMessages
				.getString("BackupEmbeddedPage.page.msg")); //$NON-NLS-1$
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
				RelationsMessages.getString("BackupEmbeddedPage.lbl.msg"), lColumns); //$NON-NLS-1$

		backupFileName = createLabelText(lComposite, getLabelText()); //$NON-NLS-1$
		createButtonFileDialog(lComposite,
				RelationsMessages.getString("PrintOutWizardPage.lbl.browse")); //$NON-NLS-1$

		setControl(lComposite);
		initializeValues();
	}

	protected String getLabelText() {
		return RelationsMessages.getString("BackupEmbeddedPage.lbl.input"); //$NON-NLS-1$
	}

	private void initializeValues() {
		setPageComplete(false);

		backupFileName.setFocus();
		addListeners(backupFileName);
	}

	@Override
	protected void focusGainedCheck(final String inText) {
		if (fileNameStatus != null
				&& fileNameStatus.getCode() != STATUS_NO_OVERWRITE) {
			fileNameStatus = Status.OK_STATUS;
		}
	}

	@Override
	protected void focusLostCheck(final String inText) {
		checkFileExists(inText, nameFileNoOverwrite);
	}

	@Override
	protected void modifiedCheck(final String inText) {
		checkFileExists(inText, nameFileNoOverwrite);
	}

	@Override
	protected void checkFileExists(final String inFileName,
			final IStatus inStatusIfExists) {
		if (inFileName.length() == 0) {
			fileNameStatus = nameEmpty;
			return;
		}

		super.checkFileExists(inFileName, inStatusIfExists);
		if (!fileNameStatus.isOK())
			return;

		if (!inFileName.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$
			fileNameStatus = nameFileNoArchive;
		}
	}

	@Override
	protected void openFileDialog() {
		fileNameStatus = Status.OK_STATUS;
		final FileDialog lDialog = new FileDialog(Display.getCurrent()
				.getActiveShell(), SWT.SAVE);
		lDialog.setText(RelationsMessages
				.getString("BackupEmbeddedPage.filedlg.msg")); //$NON-NLS-1$
		setFilterForDialog(lDialog);
		String lFileName = lDialog.open();
		if (lFileName == null) {
			fileNameStatus = nameEmpty;
		} else {
			lFileName = postProcessFileName(lFileName);
			backupFileName.setText(lFileName);
			checkFileExists(lFileName, nameFileNoOverwrite);
		}
		updateStatus(fileNameStatus);
		backupFileName.setFocus();
	}

	protected String postProcessFileName(final String inFileName) {
		return inFileName;
	}

	protected void setFilterForDialog(final FileDialog inDialog) {
		inDialog.setFilterExtensions(new String[] { "*.zip" }); //$NON-NLS-1$
		inDialog.setFilterNames(new String[] { RelationsMessages
				.getString("BackupEmbeddedPage.filedlg.names") }); //$NON-NLS-1$		
	}

	/**
	 * @return String the fully qualified name of the backup file.
	 */
	public String getFileName() {
		return backupFileName.getText();
	}

	@Override
	protected boolean getPageComplete() {
		final String lFileName = backupFileName.getText();
		if (lFileName.length() == 0)
			return false;

		final File lFileToCheck = new File(lFileName);
		if (lFileToCheck.isFile() && lFileToCheck.exists()) {
			return false;
		}
		if (!checkFileEndingCondition(lFileName)) {
			return false;
		}
		return true;
	}

	protected boolean checkFileEndingCondition(final String inFileName) {
		return inFileName.toLowerCase().endsWith(".zip"); //$NON-NLS-1$		
	}

}
