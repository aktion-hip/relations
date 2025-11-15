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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.controller.PrintOutManager;
import org.elbe.relations.internal.controller.PrintServiceController;
import org.elbe.relations.internal.utility.WizardHelper;
import org.elbe.relations.services.IPrintOut;
import org.elbe.relations.utility.DialogSettingHandler;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

import jakarta.inject.Inject;

/**
 * Wizard part to collect the information for content print out.
 *
 * @author Luthiger Created on 15.01.2007
 */
@SuppressWarnings("restriction")
public class PrintOutWizardPage extends ExportWizardPage {
    public final static int SELECTED_ONLY = 0;
    public final static int SELECTED_RELATED = 1;
    public final static int SELECTED_WHOLE = 2;
    public final static int PROCESS_APPEND = 0;
    public final static int PROCESS_NEW = 1;

    private static final String DIALOG_SECTION = "RelationsPrint"; //$NON-NLS-1$
    private static final String DIALOG_TERM = "RecentRelationsPrint"; //$NON-NLS-1$

    @Inject
    private PrintServiceController printManager;

    @Inject
    private PrintOutManager printOutManager;

    @Inject
    @Preference(nodePath = RelationsConstants.PREFERENCE_NODE)
    private IEclipsePreferences preferences;

    @Inject
    private Logger log;

    private final DialogSettingHandler settings;
    public Combo printType;
    public Combo printFileName;
    private Button printOutReferences;

    private static final Integer[] radioScopeIDs = new Integer[] {
            Integer.valueOf(SELECTED_ONLY), Integer.valueOf(SELECTED_RELATED),
            Integer.valueOf(SELECTED_WHOLE) };
    private static final String[] radioScopeLabels = new String[] {
            RelationsMessages.getString("PrintOutWizardPage.scope.1"), //$NON-NLS-1$
            RelationsMessages.getString("PrintOutWizardPage.scope.2"), //$NON-NLS-1$
            RelationsMessages.getString("PrintOutWizardPage.scope.3") }; //$NON-NLS-1$
    private int selectedScope = SELECTED_RELATED;

    private static final Integer[] radioProcessIds = new Integer[] {
            Integer.valueOf(PROCESS_APPEND), Integer.valueOf(PROCESS_NEW) };
    private static final String[] radioProcessLabels = new String[] {
            RelationsMessages.getString("PrintOutWizardPage.lbl.further"), //$NON-NLS-1$
            RelationsMessages.getString("PrintOutWizardPage.lbl.new") }; //$NON-NLS-1$
    private int selectedProcess = PROCESS_APPEND;

    private String actualFileName = ""; //$NON-NLS-1$

    private final Collection<Control> controls1 = new ArrayList<Control>();
    private final Collection<Control> controls2 = new ArrayList<Control>();

    /**
     * PrintOutWizardPage constructor, must be called by DI.
     *
     */
    public PrintOutWizardPage() {
        super("PrintOutWizardPage"); //$NON-NLS-1$
        setTitle(
                RelationsMessages.getString("PrintOutWizardPage.dialog.title")); //$NON-NLS-1$
        this.settings = new DialogSettingHandler(DIALOG_SECTION, DIALOG_TERM);
    }

    @Override
    public void createControl(final Composite inParent) {
        final int lColumns = 3;
        // inParent.setLayout(new GridLayout());
        final Composite lComposite = WizardHelper.createComposite(inParent,
                lColumns);

        this.controls1.addAll(createAppendOrNew(lComposite));

        this.printType = createComboPrintType(lComposite);

        this.printFileName = createLabelCombo(lComposite,
                RelationsMessages.getString("PrintOutWizardPage.lbl.file"), //$NON-NLS-1$
                SWT.DROP_DOWN);

        this.controls2.add(createButtonFileDialog(lComposite,
                RelationsMessages.getString("PrintOutWizardPage.lbl.browse"))); //$NON-NLS-1$

        this.controls2.addAll(createPrintOutScope(lComposite));

        this.printOutReferences = createLayoutCheckbox(lComposite);

        this.controls2.add(this.printType);
        this.controls2.add(this.printFileName);
        this.controls2.add(this.printOutReferences);

        setControl(lComposite);
        initializeValues();
    }

    private Collection<Control> createAppendOrNew(final Composite inParent) {
        new Label(inParent, SWT.NULL);

        final Composite lSelectionPane = new Composite(inParent, SWT.NONE);
        final GridData lData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        lData.horizontalSpan = 2;
        lSelectionPane.setLayoutData(lData);
        lSelectionPane.setLayout(new RowLayout(SWT.VERTICAL));

        return createRadios(lSelectionPane, radioProcessIds, radioProcessLabels,
                PROCESS_APPEND, new ProcessSelectionListener());
    }

    private Combo createComboPrintType(final Composite inParent) {
        final Combo outCombo = createLabelCombo(inParent,
                RelationsMessages.getString("PrintOutWizardPage.lbl.type"), //$NON-NLS-1$
                SWT.READ_ONLY);
        new Label(inParent, SWT.NONE);
        return outCombo;
    }

    @Override
    protected void openFileDialog() {
        this.fileNameStatus = Status.OK_STATUS;
        this.printManager.setSelected(this.printType.getSelectionIndex());
        final FileDialog lDialog = new FileDialog(
                Display.getCurrent().getActiveShell(), SWT.SAVE);
        lDialog.setText(
                RelationsMessages.getString("PrintOutWizardPage.lbl.dialog")); //$NON-NLS-1$
        lDialog.setFilterExtensions(this.printManager.getFilterExtensions());
        lDialog.setFilterNames(this.printManager.getFilterNames());
        final String lFileName = lDialog.open();
        if (lFileName == null) {
            this.fileNameStatus = this.nameEmpty;
        } else {
            this.printFileName.setText(lFileName);
            checkFileExists(lFileName);
        }
        updateStatus(this.fileNameStatus);
        this.printFileName.setFocus();
    }

    private Collection<Control> createPrintOutScope(final Composite inParent) {
        final Label lSelectionLabel = new Label(inParent, SWT.NONE);
        lSelectionLabel
        .setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        lSelectionLabel.setText(RelationsMessages
                .getString("PrintOutWizardPage.lbl.selection")); //$NON-NLS-1$

        final Composite lSelectionPane = new Composite(inParent, SWT.NONE);
        lSelectionPane.setLayout(new RowLayout(SWT.VERTICAL));
        final Collection<Control> outRadios = createRadios(lSelectionPane,
                radioScopeIDs, radioScopeLabels,
                this.printOutManager.getContentSope(), new SetSelectionListener());

        new Label(inParent, SWT.NONE);
        return outRadios;
    }

    private Button createLayoutCheckbox(final Composite inParent) {
        final Label lCheckboxLabel = new Label(inParent, SWT.NONE);
        lCheckboxLabel.setText(
                RelationsMessages.getString("PrintOutWizardPage.lbl.output")); //$NON-NLS-1$

        final Button outButton = new Button(inParent, SWT.CHECK);
        outButton.setText(RelationsMessages
                .getString("PrintOutWizardPage.lbl.paragrahp")); //$NON-NLS-1$

        final GridData lData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        lData.horizontalSpan = 2;
        outButton.setLayoutData(lData);
        return outButton;
    }

    private void initializeValues() {
        this.printType.setItems(this.printManager.getTextProcessorNames());
        this.printType.select(this.printManager.getSelected());

        final String[] lRecentValues = this.settings.getRecentValues();
        if (lRecentValues.length > 0) {
            this.actualFileName = lRecentValues[0];
        }
        this.printFileName.setItems(lRecentValues);

        this.printOutReferences
        .setSelection(this.printOutManager.getPrintOutReferences());
        this.selectedScope = this.printOutManager.getContentSope();

        if (this.printOutManager.isPrinting()) {
            widgetEnablementSwitch(false);
            this.printFileName.setText(this.actualFileName);
            setMessage(null);
            setPageComplete(true);
        } else {
            this.selectedProcess = PROCESS_NEW;
            widgetVisible(false);
            setPageComplete(false);
        }

        addListeners(this.printFileName);
        this.printFileName.setFocus();
    }

    @Override
    protected void focusGainedCheck(final String inText) {
        if (this.fileNameStatus == null) {
            this.fileNameStatus = Status.OK_STATUS;
        } else {
            if (this.fileNameStatus.getCode() != STATUS_FILE_EXISTS) {
                this.fileNameStatus = Status.OK_STATUS;
            }
        }
    }

    @Override
    protected void focusLostCheck(final String inText) {
        checkFileExists(inText, this.nameFileExists);
    }

    @Override
    protected void modifiedCheck(final String inText) {
        checkFileExists(inText, this.nameFileExists);
    }

    @Override
    protected void checkFileExists(final String inFileName,
            final IStatus inStatusIfExists) {
        super.checkFileExists(inFileName, inStatusIfExists);

        if (!this.fileNameStatus.isOK()) {
            return;
        }
        if (inFileName.length() == 0) {
            this.fileNameStatus = this.nameEmpty;
            return;
        }
    }

    private Collection<Control> createRadios(final Composite inParent,
            final Integer[] inIds, final String[] inLabels, final int inDefault,
            final SelectionListener inListener) {
        final Collection<Control> outRadios = new ArrayList<Control>();
        for (int i = 0; i < inIds.length; i++) {
            final Button lButton = new Button(inParent, SWT.RADIO);
            lButton.setData(inIds[i]);
            lButton.setText(inLabels[i]);
            lButton.setSelection(i == inDefault);
            lButton.addSelectionListener(inListener);
            outRadios.add(lButton);
        }
        return outRadios;
    }

    /**
     * @return IPrintOut the selected print out plug-in.
     */
    public IPrintOut getSelectedPrintOut() {
        this.printManager.setSelected(this.printType.getSelectionIndex());
        return this.printManager.getSelectedPrinter();
    }

    /**
     * @return String name of the file to print out the selected set of items.
     */
    public String getFileName() {
        return this.printFileName.getText();
    }

    /**
     * Returns a constant denoting the scope of items to be printed out
     *
     * @return int constant, e.g. <code>SELECTED_RELATED</code>
     */
    public int getPrintOutScope() {
        return this.selectedScope;
    }

    /**
     * Friendly method to save the value entered in <code>printFileName</code>
     * to the dialog's history.
     */
    void saveToHistory() {
        try {
            this.settings.saveToHistory(this.printFileName.getText());
            this.preferences.put(RelationsConstants.KEY_PRINT_OUT_PLUGIN_ID,
                    FrameworkUtil.getBundle(getSelectedPrintOut().getClass())
                    .getSymbolicName());
        }
        catch (final BackingStoreException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

    private void widgetEnablementSwitch(final boolean inEnable) {
        for (final Control lControl : this.controls2) {
            lControl.setEnabled(inEnable);
        }
    }

    private void widgetVisible(final boolean inVisible) {
        for (final Control lControl : this.controls1) {
            lControl.setVisible(inVisible);
        }
    }

    /**
     * Tells whether the user selected a new print out or chooses to proceed
     * with a print out already set up.
     *
     * @return boolean <code>true</code> if the user selected the
     *         <code>PROCESS_NEW</code> process.
     */
    public boolean isInitNew() {
        return this.selectedProcess == PROCESS_NEW;
    }

    /**
     * Returns the value of the printOutReferences checkbox.
     *
     * @return boolean <code>true</code> if checkbox is selected, i.e. the
     *         item's references have to be printed out.
     */
    public boolean getPrintOutReferences() {
        return this.printOutReferences.getSelection();
    }

    @Override
    protected boolean getPageComplete() {
        if (this.printFileName.getText().length() == 0) {
            return false;
        }
        return true;
    }

    // --- inner classes ---

    private class SetSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(final SelectionEvent inEvent) {
            if (((Button) inEvent.widget).getSelection()) {
                PrintOutWizardPage.this.selectedScope = ((Integer) inEvent.widget.getData()).intValue();
            }
        }
    }

    private class ProcessSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(final SelectionEvent inEvent) {
            if (((Button) inEvent.widget).getSelection()) {
                PrintOutWizardPage.this.selectedProcess = ((Integer) inEvent.widget.getData())
                        .intValue();
                final boolean lNewPrintOut = PrintOutWizardPage.this.selectedProcess == PROCESS_NEW;
                widgetEnablementSwitch(lNewPrintOut);
                if (lNewPrintOut) {
                    checkFileExists(PrintOutWizardPage.this.printFileName.getText());
                } else {
                    PrintOutWizardPage.this.printFileName.setText(PrintOutWizardPage.this.actualFileName);
                    PrintOutWizardPage.this.fileNameStatus = Status.OK_STATUS;
                }
                updateStatus(PrintOutWizardPage.this.fileNameStatus);
            }
        }
    }

}
