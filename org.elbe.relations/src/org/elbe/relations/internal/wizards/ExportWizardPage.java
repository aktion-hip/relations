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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.FormUtility;

/**
 * Base class for export wizard pages providing functionality for export wizard
 * pages.
 *
 * @author Luthiger
 */
public abstract class ExportWizardPage extends WizardPage
        implements IWizardPage {
	protected final static int STATUS_FIELD_EMPTY = 11;
	protected final static int STATUS_NO_OVERWRITE = 12;
	protected final static int STATUS_FILE_EXISTS = 13;
	protected final static int STATUS_FILE_NOT_EXISTS = 14;
	protected final static int STATUS_NO_ARCHIVE = 15;
	protected final static int STATUS_EMPTY_ARCHIVE = 16;
	private static final String PLUGIN_ID = Activator.getSymbolicName();

	protected IStatus nameEmpty = new Status(Status.ERROR, PLUGIN_ID,
	        STATUS_FIELD_EMPTY,
	        RelationsMessages.getString("PrintOutWizardPage.error.empty"), //$NON-NLS-1$
	        null);
	protected IStatus nameFileNoOverwrite = new Status(Status.ERROR, PLUGIN_ID,
	        STATUS_NO_OVERWRITE,
	        RelationsMessages.getString("ExportWizardPage.msg.nooverwrite"), //$NON-NLS-1$
	        null);
	protected IStatus nameFileExists = new Status(Status.WARNING, PLUGIN_ID,
	        STATUS_FILE_EXISTS,
	        RelationsMessages.getString("PrintOutWizardPage.warning.overwrite"), //$NON-NLS-1$
	        null);
	protected IStatus nameFileNotExists = new Status(Status.ERROR, PLUGIN_ID,
	        STATUS_FILE_NOT_EXISTS,
	        RelationsMessages.getString("ExportWizardPage.msg.notexist"), null); //$NON-NLS-1$
	protected IStatus nameFileNoArchive = new Status(Status.ERROR, PLUGIN_ID,
	        STATUS_NO_ARCHIVE,
	        RelationsMessages.getString("ExportWizardPage.msg.noarchive"), //$NON-NLS-1$
	        null);
	protected IStatus nameFileEmptyArchive = new Status(Status.ERROR, PLUGIN_ID,
	        STATUS_EMPTY_ARCHIVE,
	        RelationsMessages.getString("ExportWizardPage.msg.emptyarchive"), //$NON-NLS-1$
	        null);

	protected IStatus fileNameStatus;

	private String message = null;

	protected ExportWizardPage(final String inPageName) {
		super(inPageName);
	}

	private Label createLabel(final Composite inParent, final String inText) {
		final Label outLabel = new Label(inParent, SWT.NONE);
		outLabel.setText(inText);
		outLabel.setLayoutData(
		        new GridData(SWT.FILL, SWT.CENTER, false, false));
		return outLabel;
	}

	protected Label createLabel(final Composite inParent, final String inText,
	        final int inColSpan) {
		final Label outLabel = createLabel(inParent, inText);
		((GridData) outLabel.getLayoutData()).horizontalSpan = inColSpan;
		return outLabel;
	}

	protected Combo createLabelCombo(final Composite inParent,
	        final String inText, final int inComboType) {
		createLabel(inParent, inText);
		final Combo outCombo = new Combo(inParent, inComboType);
		outCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return outCombo;
	}

	/**
	 * Creates a <code>Combo</code> widget that has the required decoration.
	 *
	 * @param inParent
	 *            Composite
	 * @param inText
	 *            String label text
	 * @param inComboType
	 *            int Combo type
	 * @return Combo
	 */
	protected Combo createRequiredLabelCombo(final Composite inParent,
	        final String inText, final int inComboType) {
		createLabel(inParent, inText);
		final Combo outCombo = new Combo(inParent, inComboType);
		FormUtility.addDecorationRequired(outCombo);
		final GridData lLayout = new GridData(SWT.FILL, SWT.CENTER, true,
		        false);
		lLayout.horizontalIndent = FieldDecorationRegistry.getDefault()
		        .getMaximumDecorationWidth();
		outCombo.setLayoutData(lLayout);
		return outCombo;
	}

	protected void checkFileExists(final String inFileName) {
		checkFileExists(inFileName, nameFileExists);
	}

	protected void checkFileExists(final String inFileName,
	        final IStatus inStatusIfExists) {
		final File lFileToCheck = new File(inFileName);
		fileNameStatus = Status.OK_STATUS;
		if (lFileToCheck.isFile() && lFileToCheck.exists()) {
			fileNameStatus = inStatusIfExists;
		}
	}

	protected void checkFileNotExists(final String inFileName,
	        final IStatus inStatusIfNotExists) {
		final File lFileToCheck = new File(inFileName);
		fileNameStatus = inStatusIfNotExists;
		if (lFileToCheck.isFile() && lFileToCheck.exists()) {
			fileNameStatus = Status.OK_STATUS;
		}
	}

	protected Text createLabelText(final Composite inParent,
	        final String inLabel) {
		final Label lLabel = new Label(inParent, SWT.NONE);
		lLabel.setText(inLabel);
		lLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		final Text outText = new Text(inParent, SWT.BORDER | SWT.SINGLE);
		outText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return outText;
	}

	protected void addListeners(final Text inText) {
		inText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				focusGainedCheck(((Text) inEvent.widget).getText());
				updateStatus(fileNameStatus);
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				focusLostCheck(((Text) inEvent.widget).getText());
				updateStatus(fileNameStatus);
			}
		});
		inText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				modifiedCheck(((Text) inEvent.widget).getText());
				updateStatus(fileNameStatus);
			}
		});
	}

	protected void addListeners(final Combo inCombo) {
		addFocusListener(inCombo);
		addModifyListener(inCombo);
	}

	protected void addFocusListener(final Combo inCombo) {
		inCombo.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				focusGainedCheck(((Combo) inEvent.widget).getText());
				updateStatus(fileNameStatus);
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				focusLostCheck(((Combo) inEvent.widget).getText());
				updateStatus(fileNameStatus);
			}
		});
	}

	protected void focusGainedCheck(final String inText) {
		fileNameStatus = Status.OK_STATUS;
	}

	protected void focusLostCheck(final String inText) {
		if (inText.length() == 0) {
			fileNameStatus = nameEmpty;
		}
	}

	private void addModifyListener(final Combo inCombo) {
		inCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				modifiedCheck(((Combo) inEvent.widget).getText());
				updateStatus(fileNameStatus);
			}
		});
	}

	protected void modifiedCheck(final String inText) {
		checkFileExists(inText);
	}

	protected IStatus createErrorStatus(final String inMsg) {
		return new Status(Status.ERROR, PLUGIN_ID, 1, inMsg, null);
	}

	protected void updateStatus(final IStatus inStatus) {
		// handle messages
		if (inStatus.getSeverity() == IStatus.ERROR) {
			setErrorMessage(inStatus.getMessage());
		} else {
			setErrorMessage(null);
			if (inStatus.getSeverity() == Status.WARNING) {
				setMessage(inStatus.getMessage(), Status.WARNING);
			} else {
				setMessage(message);
			}
		}

		// page complete
		setPageComplete(getPageComplete());
	}

	abstract protected boolean getPageComplete();

	/**
	 * Creates a button to open a file dialog.
	 *
	 * @param inParent
	 *            Composite
	 * @param inButtonLabel
	 *            String the text displayed on the button
	 * @return Button
	 */
	protected Button createButtonFileDialog(final Composite inParent,
	        final String inButtonLabel) {
		final Button outButton = new Button(inParent, SWT.PUSH);
		outButton.setText(inButtonLabel);
		outButton.setLayoutData(
		        new GridData(SWT.FILL, SWT.CENTER, false, false));
		outButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent inEvent) {
				openFileDialog();
			}

			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				openFileDialog();
			}
		});
		return outButton;
	}

	protected void setInfoMessage(final String inMessage) {
		message = inMessage;
	}

	abstract protected void openFileDialog();

}
