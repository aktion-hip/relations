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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.utility.ImportDropHelper;
import org.elbe.relations.internal.utility.ImportDropHelper.IModifyListener;
import org.elbe.relations.internal.utility.WizardHelper;
import org.elbe.relations.utility.DialogSettingHandler;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Page displaying the input field to enter the file name for the backup file
 * containing the state to restore.
 * 
 * @author Luthiger Created on 10.05.2007
 */
@SuppressWarnings("restriction")
public class RestoreEmbeddedPage extends ExportWizardPage {
	private final static MessageFormat LABEL = new MessageFormat(
	        RelationsMessages.getString("RestoreEmbeddedPage.msg.input")); //$NON-NLS-1$
	private static final String[] FILTER_EXTENSIONS = { "*.zip" }; //$NON-NLS-1$

	private Combo backupFileName;
	private Button reindexCheck;
	private final DialogSettingHandler settings;
	private final String catalog;
	private final Logger log;

	protected RestoreEmbeddedPage(final String inPageName,
	        final String inCatalog, final Logger inLog) {
		super(inPageName);
		catalog = inCatalog;
		log = inLog;
		setTitle(RelationsMessages.getString("RestoreEmbeddedPage.page.title")); //$NON-NLS-1$
		setInfoMessage(RelationsMessages
		        .getString("RestoreEmbeddedPage.page.msg")); //$NON-NLS-1$
		settings = new DialogSettingHandler(
		        "RestoreEmbedded", "RecentRestoreEmbedded"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void createControl(final Composite inParent) {
		final int lColumns = 3;
		final Composite lComposite = WizardHelper.createComposite(inParent,
		        lColumns);

		createLabel(lComposite, LABEL.format(new String[] { catalog }),
		        lColumns);

		backupFileName = createLabelCombo(
		        lComposite,
		        RelationsMessages.getString("RestoreEmbeddedPage.lbl.input"), SWT.DROP_DOWN); //$NON-NLS-1$
		FormUtility.addDecorationHint(backupFileName,
		        RelationsMessages.getString("ImportPage.hint.drop")); //$NON-NLS-1$		
		createButtonFileDialog(lComposite,
		        RelationsMessages.getString("PrintOutWizardPage.lbl.browse")); //$NON-NLS-1$
		ImportDropHelper.wrapFileDrop(backupFileName, FILTER_EXTENSIONS,
		        new IModifyListener() {
			        @Override
			        public void modifyText(final String inFileName) {
				        modifiedCheck(inFileName);
				        updateStatus(fileNameStatus);
			        }
		        });

		new Label(lComposite, SWT.NONE);
		reindexCheck = createCheckbox(lComposite,
		        RelationsMessages.getString("RestoreEmbeddedPage.lbl.check")); //$NON-NLS-1$
		new Label(lComposite, SWT.NONE);

		setControl(lComposite);
		initializeValues();
	}

	private Button createCheckbox(final Composite inComposite,
	        final String inString) {
		final Button outButton = new Button(inComposite, SWT.CHECK);
		outButton.setText(inString);
		return outButton;
	}

	private void initializeValues() {
		backupFileName.setItems(settings.getRecentValues());
		backupFileName.setFocus();
		addListeners(backupFileName);

		reindexCheck.setSelection(true);
	}

	@Override
	protected void focusGainedCheck(final String inText) {
		if (fileNameStatus != null
		        && fileNameStatus.getCode() != STATUS_NO_ARCHIVE) {
			fileNameStatus = Status.OK_STATUS;
		}
	}

	@Override
	protected void focusLostCheck(final String inText) {
		checkFileNotExists(inText, nameFileNotExists);
	}

	@Override
	protected void modifiedCheck(final String inText) {
		checkFileNotExists(inText, nameFileNotExists);
	}

	@Override
	protected void checkFileNotExists(final String inFileName,
	        final IStatus inStatusIfNotExists) {
		if (inFileName.length() == 0) {
			fileNameStatus = nameEmpty;
			return;
		}

		super.checkFileNotExists(inFileName, inStatusIfNotExists);
		if (!fileNameStatus.isOK())
			return;

		try {
			final ZipFile lFileToCheck = new ZipFile(inFileName);
			if (lFileToCheck.size() == 0) {
				fileNameStatus = nameFileEmptyArchive;
			}
			lFileToCheck.close();
		}
		catch (final IOException exc) {
			fileNameStatus = nameFileNoArchive;
		}
	}

	/**
	 * Friendly method to save the value entered in <code>exportFileName</code>
	 * to the dialog's history.
	 */
	void saveToHistory() {
		try {
			settings.saveToHistory(backupFileName.getText());
		}
		catch (final BackingStoreException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	@Override
	protected void openFileDialog() {
		fileNameStatus = Status.OK_STATUS;
		final FileDialog lDialog = new FileDialog(Display.getCurrent()
		        .getActiveShell(), SWT.OPEN);
		lDialog.setText(RelationsMessages
		        .getString("RestoreEmbeddedPage.filedlg.msg")); //$NON-NLS-1$
		lDialog.setFilterExtensions(FILTER_EXTENSIONS);
		lDialog.setFilterNames(new String[] { RelationsMessages
		        .getString("RestoreEmbeddedPage.filedlg.names") }); //$NON-NLS-1$
		final String lFileName = lDialog.open();
		if (lFileName == null) {
			fileNameStatus = nameEmpty;
		} else {
			backupFileName.setText(lFileName);
			checkFileNotExists(lFileName, nameFileNotExists);
		}
		updateStatus(fileNameStatus);
		backupFileName.setFocus();
	}

	/**
	 * @return String Name of the Zip archive containing the database status to
	 *         restore.
	 */
	public String getFileName() {
		return backupFileName.getText();
	}

	/**
	 * Returns the flag of the reindex checkbox.
	 * 
	 * @return boolean <code>true</code> if the reindex checkbox is checked.
	 */
	public boolean getReindex() {
		return reindexCheck.getSelection();
	}

	@Override
	protected boolean getPageComplete() {
		final String lFileName = backupFileName.getText();
		if (lFileName.length() == 0)
			return false;

		final File lFileToCheck = new File(lFileName);
		if (!lFileToCheck.isFile() || !lFileToCheck.exists()) {
			return false;
		}
		try {
			final ZipFile lZipToCheck = new ZipFile(lFileToCheck);
			final boolean outOk = (lZipToCheck.size() != 0);
			lZipToCheck.close();
			return outOk;
		}
		catch (final IOException exc) {
			return false;
		}
	}

}
