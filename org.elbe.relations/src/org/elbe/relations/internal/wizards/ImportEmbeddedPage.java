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
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.utility.ImportDropHelper;
import org.elbe.relations.internal.utility.ImportDropHelper.IModifyListener;
import org.elbe.relations.internal.utility.WizardHelper;
import org.elbe.relations.utility.DialogSettingHandler;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Page displaying the input field to enter the file name for the Zip file
 * containing the database to import.
 * 
 * @author Luthiger Created on 15.10.2007
 */
@SuppressWarnings("restriction")
public class ImportEmbeddedPage extends ExportWizardPage {
	private static final String[] FILTER_EXTENSIONS = { "*.zip" }; //$NON-NLS-1$

	private Text newDatabaseName;
	private Combo importFileName;
	private Button reindexCheck;
	private final DialogSettingHandler settings;

	private IStatus importNameStatus = Status.OK_STATUS;

	private final String message;
	final EmbeddedCatalogHelper embedded = new EmbeddedCatalogHelper();
	private final Logger log;

	/**
	 * @param inPageName
	 * @param inLog
	 */
	public ImportEmbeddedPage(final String inPageName, final Logger inLog) {
		super(inPageName);
		log = inLog;
		setTitle(RelationsMessages.getString("ImportEmbeddedPage.wizard.title")); //$NON-NLS-1$
		message = RelationsMessages
				.getString("ImportEmbeddedPage.wizard.message"); //$NON-NLS-1$
		settings = new DialogSettingHandler(
				"ImportEmbedded", "RecentImportEmbedded"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void openFileDialog() {
		importNameStatus = Status.OK_STATUS;
		final FileDialog lDialog = new FileDialog(Display.getCurrent()
				.getActiveShell(), SWT.OPEN);
		lDialog.setText(RelationsMessages
				.getString("ImportEmbeddedPage.dlg.message")); //$NON-NLS-1$
		lDialog.setFilterExtensions(FILTER_EXTENSIONS);
		lDialog.setFilterNames(new String[] { RelationsMessages
				.getString("ImportEmbeddedPage.file.filter") }); //$NON-NLS-1$
		final String lFileName = lDialog.open();
		if (lFileName == null) {
			importNameStatus = nameEmpty;
		} else {
			importFileName.setText(lFileName);
			checkFileNotExists(lFileName, nameFileNotExists);
		}
		updateStatuses(getStatuses());
		importFileName.setFocus();
	}

	@Override
	public void createControl(final Composite inParent) {
		final int lColumns = 3;
		final Composite lComposite = WizardHelper.createComposite(inParent,
				lColumns);

		newDatabaseName = createLabelText(lComposite,
				RelationsMessages
						.getString("ImportEmbeddedPage.label.database")); //$NON-NLS-1$
		new Label(lComposite, SWT.NONE);

		importFileName = createLabelCombo(
				lComposite,
				RelationsMessages.getString("RestoreEmbeddedPage.lbl.input"), SWT.DROP_DOWN); //$NON-NLS-1$
		createButtonFileDialog(lComposite,
				RelationsMessages.getString("PrintOutWizardPage.lbl.browse")); //$NON-NLS-1$
		ImportDropHelper.wrapFileDrop(importFileName, FILTER_EXTENSIONS,
				new IModifyListener() {
					@Override
					public void modifyText(final String inFileName) {
						checkImportFileExists(inFileName, true);
						updateStatuses(getStatuses());
					}
				});

		new Label(lComposite, SWT.NONE);
		reindexCheck = createCheckbox(lComposite,
				RelationsMessages.getString("ImportEmbeddedPage.label.reindex")); //$NON-NLS-1$
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
		setPageComplete(false);
		reindexCheck.setSelection(true);

		newDatabaseName.setFocus();
		newDatabaseName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				checkValidDBName(((Text) inEvent.widget).getText(), false);
				updateStatuses(getStatuses());
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				checkValidDBName(((Text) inEvent.widget).getText(), true);
				updateStatuses(getStatuses());
			}
		});
		newDatabaseName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				checkValidDBName(((Text) inEvent.widget).getText(), true);
				updateStatuses(getStatuses());
			}
		});

		importFileName.setItems(settings.getRecentValues());
		importFileName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				checkImportFileExists(((Combo) inEvent.widget).getText(), false);
				updateStatuses(getStatuses());
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				checkImportFileExists(((Combo) inEvent.widget).getText(), true);
				updateStatuses(getStatuses());
			}
		});
		importFileName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				checkImportFileExists(((Combo) inEvent.widget).getText(), true);
				updateStatuses(getStatuses());
			}
		});
	}

	private void checkValidDBName(final String inText,
			final boolean inCheckEmpty) {
		if (inText.length() == 0) {
			fileNameStatus = Status.OK_STATUS;
			if (inCheckEmpty) {
				fileNameStatus = nameEmpty;
			}
		} else {
			fileNameStatus = embedded.validate(inText.toLowerCase());
		}
	}

	private void checkImportFileExists(final String inFileName,
			final boolean inCheckEmpty) {
		if (inFileName.length() == 0) {
			if (inCheckEmpty) {
				importNameStatus = nameEmpty;
			}
		} else {
			importNameStatus = nameFileNotExists;
			final File lFileToCheck = new File(inFileName);
			if (lFileToCheck.isFile() && lFileToCheck.exists()) {
				importNameStatus = Status.OK_STATUS;
				try {
					final ZipFile lZipToCheck = new ZipFile(inFileName);
					if (lZipToCheck.size() == 0) {
						importNameStatus = nameFileEmptyArchive;
					}
					lZipToCheck.close();
				}
				catch (final IOException exc) {
					importNameStatus = nameFileNoArchive;
				}
			}
		}
	}

	private IStatus[] getStatuses() {
		return new IStatus[] { fileNameStatus, importNameStatus };
	}

	private void updateStatuses(final IStatus[] inStatuses) {
		// handle error messages
		final MultiStatus lStatus = new MultiStatus(
				Activator.getSymbolicName(), 1, inStatuses, "", null); //$NON-NLS-1$
		setErrorMessage(FormUtility.getErrorMessage(lStatus));
		setMessage(message);
		//
		setPageComplete(getPageComplete());
	}

	/**
	 * The following conditions have to be met: - dbname and filename not empty
	 * - dbname not equal to existing embedded database - filename of existing
	 * file - filename of proper zip file - filename of zip file which is not
	 * empty
	 * 
	 * @return boolean
	 */
	@Override
	protected boolean getPageComplete() {
		final String lDatabaseName = newDatabaseName.getText();
		final String lImportFileName = importFileName.getText();

		if (lDatabaseName.length() * lImportFileName.length() == 0) {
			return false;
		}
		if (!embedded.validate(lDatabaseName.toLowerCase()).isOK()) {
			return false;
		}
		final File lFileToCheck = new File(lImportFileName);
		if (lFileToCheck.isFile() && lFileToCheck.exists()) {
			try {
				final ZipFile lZipToCheck = new ZipFile(lFileToCheck);
				if (lZipToCheck.size() == 0) {
					lZipToCheck.close();
					return false;
				}
				lZipToCheck.close();
			}
			catch (final IOException exc) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns the name of the selected Zip file.
	 * 
	 * @return String
	 */
	public String getArchiveName() {
		return importFileName.getText();
	}

	/**
	 * Returns the name of the embedded database that is filled with the
	 * imported values.
	 * 
	 * @return String
	 */
	public String getDBName() {
		return newDatabaseName.getText();
	}

	/**
	 * Returns the flag of the reindex checkbox.
	 * 
	 * @return boolean <code>true</code> if the reindex checkbox is checked.
	 */
	public boolean getReindex() {
		return reindexCheck.getSelection();
	}

	/**
	 * Friendly method to save the value entered in <code>exportFileName</code>
	 * to the dialog's history.
	 */
	void saveToHistory() {
		try {
			settings.saveToHistory(importFileName.getText());
		}
		catch (final BackingStoreException exc) {
			log.error(exc, exc.getMessage());
		}
	}

}
