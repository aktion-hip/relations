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
package org.elbe.relations.internal.wizards;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.IDBChange;
import org.elbe.relations.internal.forms.FieldStatusManager;
import org.elbe.relations.internal.forms.FormDBNew;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.utility.ImportDropHelper;
import org.elbe.relations.internal.utility.ImportDropHelper.IModifyListener;
import org.elbe.relations.internal.utility.WizardHelper;
import org.elbe.relations.utility.DialogSettingHandler;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Page displaying the fields to enter the name of the XML file to import and
 * the database catalog to create.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ImportFromXMLPage extends ExportWizardPage
        implements IUpdateListener {
	private static final String[] FILTER_EXTENSIONS = { "*.xml", "*.zip" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String DIALOG_SECTION = "RelationsXMLImport"; //$NON-NLS-1$
	private static final String DIALOG_TERM = "RecentRelationsXMLImport"; //$NON-NLS-1$
	private static final String BUNDLE_ID = Activator.getSymbolicName();

	private FormDBNew form;
	private Combo importFileName;
	private Button fileOpen;
	private final FieldStatusManager statusManager = new FieldStatusManager();

	final private IStatus importEmpty = FormUtility.createErrorStatus(
	        RelationsMessages.getString("ImportFromXMLPage.error.name.empty"), //$NON-NLS-1$
	        BUNDLE_ID);
	final private IStatus importInexistent = FormUtility.createErrorStatus(
	        RelationsMessages.getString("ImportFromXMLPage.error.file.exist"), //$NON-NLS-1$
	        BUNDLE_ID);
	final private IStatus importZipEmpty = FormUtility.createErrorStatus(
	        RelationsMessages.getString("ImportFromXMLPage.error.file.empty"), //$NON-NLS-1$
	        BUNDLE_ID);
	final private IStatus importZipNoArchive = FormUtility.createErrorStatus(
	        RelationsMessages.getString("ImportFromXMLPage.msg.error"), //$NON-NLS-1$
	        BUNDLE_ID);
	private final DialogSettingHandler settings;
	private final IEclipseContext context;
	private final Logger log;

	/**
	 * @param inPageName
	 *            String
	 */
	protected ImportFromXMLPage(final String inPageName,
	        final IEclipseContext inContext, final Logger inLog) {
		super(inPageName);
		context = inContext;
		log = inLog;
		setTitle(RelationsMessages.getString("ImportFromXMLPage.page.title")); //$NON-NLS-1$
		setImageDescriptor(RelationsImages.WIZARD_IMPORT_XML.getDescriptor());
		setMessage(RelationsMessages.getString("ImportFromXMLPage.page.msg")); //$NON-NLS-1$
		settings = new DialogSettingHandler(DIALOG_SECTION, DIALOG_TERM);
	}

	@Override
	public void createControl(final Composite inParent) {
		final int lColumns = 3;
		final Composite lComposite = WizardHelper.createComposite(inParent,
		        lColumns);

		importFileName = createRequiredLabelCombo(lComposite,
		        RelationsMessages.getString("ImportFromXMLPage.lbl.input.file"), //$NON-NLS-1$
		        SWT.DROP_DOWN);
		FormUtility.addDecorationHint(importFileName,
		        RelationsMessages.getString("ImportPage.hint.drop")); //$NON-NLS-1$
		statusManager.initialize(importFileName);
		importFileName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				statusManager.reset();
				notifyAboutUpdate(statusManager.getStati());
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				validateFile(importFileName.getText());
				notifyAboutUpdate(statusManager.getStati());
			}
		});
		ImportDropHelper.wrapFileDrop(importFileName, FILTER_EXTENSIONS,
		        new IModifyListener() {
			        @Override
			        public void modifyText(final String inFileName) {
				        setErrorMessage(null);
				        if (!validateFile(inFileName)) {
					        updateStatus(importZipNoArchive);
				        }
			        }
		        });
		fileOpen = createButtonFileDialog(lComposite,
		        RelationsMessages.getString("PrintOutWizardPage.lbl.browse")); //$NON-NLS-1$

		createSeparator(lComposite, lColumns);

		form = FormDBNew.createFormDBNew(lComposite, lColumns, context);
		form.addUpdateListener(this);
		setControl(lComposite);
		setPageComplete(false);

		final String[] lRecentValues = settings.getRecentValues();
		importFileName.setItems(lRecentValues);
	}

	private void createSeparator(final Composite inParent,
	        final int inColumns) {
		final Label lSeparator = new Label(inParent,
		        SWT.SEPARATOR | SWT.HORIZONTAL);
		lSeparator.setLayoutData(
		        new GridData(SWT.FILL, SWT.NONE, true, false, inColumns, 1));
	}

	@Override
	protected boolean getPageComplete() {
		return validateFile(importFileName.getText()) && form.getPageComplete();
	}

	private boolean validateFile(final String inFileName) {
		if (inFileName.length() == 0) {
			statusManager.set(importFileName, importEmpty);
			return false;
		}

		final File lFileToCheck = new File(inFileName);
		if (!lFileToCheck.isFile() || !lFileToCheck.exists()) {
			statusManager.set(importFileName, importInexistent);
			return false;
		}
		if (!inFileName.endsWith(".zip") && !inFileName.endsWith(".xml")) {
			return false; // $NON-NLS-1$ //$NON-NLS-2$
		}
		if (inFileName.endsWith(".zip")) { //$NON-NLS-1$
			try {
				final ZipFile lZipToCheck = new ZipFile(lFileToCheck);
				final boolean outOk = (lZipToCheck.size() != 0);
				lZipToCheck.close();
				statusManager.set(importFileName,
				        outOk ? Status.OK_STATUS : importZipEmpty);
				return outOk;
			}
			catch (final IOException exc) {
				statusManager.set(importFileName, importZipEmpty);
				return false;
			}
		}
		return true;
	}

	@Override
	protected void openFileDialog() {
		statusManager.reset();
		notifyAboutUpdate(statusManager.getStati());

		fileNameStatus = Status.OK_STATUS;
		final FileDialog lDialog = new FileDialog(
		        Display.getCurrent().getActiveShell(), SWT.OPEN);
		lDialog.setText(
		        RelationsMessages.getString("ImportFromXMLPage.dialog.text")); //$NON-NLS-1$
		lDialog.setFilterExtensions(FILTER_EXTENSIONS);
		lDialog.setFilterNames(new String[] {
		        RelationsMessages
		                .getString("ImportFromXMLPage.dialog.filter.plain"), //$NON-NLS-1$
		        RelationsMessages
		                .getString("ImportFromXMLPage.dialog.filter.zipped") }); //$NON-NLS-1$
		final String lFileName = lDialog.open();
		if (lFileName == null) {
			fileNameStatus = nameEmpty;
		} else {
			importFileName.setText(lFileName);
			checkFileNotExists(lFileName, nameFileNoOverwrite);
		}
		updateStatus(fileNameStatus);
		importFileName.setFocus();
	}

	private void notifyAboutUpdate(final IStatus[] inStati) {
		onUpdate(new MultiStatus(BUNDLE_ID, 1, inStati, "", null)); //$NON-NLS-1$
	}

	@Override
	public void onUpdate(final IStatus inStatus) {
		setErrorMessage(FormUtility.getErrorMessage(inStatus));
		setPageComplete(getPageComplete());
	}

	/**
	 * Friendly method to save the value entered in <code>printFileName</code>
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

	@Override
	public void dispose() {
		importFileName.dispose();
		fileOpen.dispose();
		form.removeUpdateListener(this);
		form.dispose();
		super.dispose();
	}

	/**
	 * @return String the name of the import file as entered by the user.
	 */
	public String getFileName() {
		return importFileName.getText();
	}

	/**
	 * @return {@link IDBChange} the db catalog creation handler based on the
	 *         user input.
	 */
	public IDBChange getResultObject() {
		return form.getResultObject();
	}
}
