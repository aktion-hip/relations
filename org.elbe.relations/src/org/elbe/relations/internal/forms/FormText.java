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
package org.elbe.relations.internal.forms;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.internal.utility.RequiredText;
import org.xml.sax.SAXException;

import jakarta.inject.Inject;

/**
 * Input form to enter or edit the data of a text item.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class FormText extends AbstractEditForm {
    private Combo typeCombo;
    private RequiredText authorText;
    private Text coauthorText;
    private Label coauthorLabel;
    private RequiredText titleText;
    private Text subtitleText;
    private Text yearText;
    private StyledText journalText;
    private Label journalLabel;
    private Text pagesText;
    private Text volumeText;
    private Text numberText;
    private Text publisherText;
    private Text locationText;
    private Label locationLabel;

    private IStatus titleFieldStatus;
    private IStatus authorFieldStatus;

    private StyledFieldHelper journalTextHelper;

    private final IStatus authorEmpty = createErrorStatus(RelationsMessages.getString("FormText.missing.author")); //$NON-NLS-1$
    private final IStatus titleEmpty = createErrorStatus(RelationsMessages.getString("FormText.missing.title")); //$NON-NLS-1$

    private boolean initialized = false;

    @Inject
    private Logger log;

    /** Factory method to create instances of <code>FormText</code>.
     *
     * @param parent {@link Composite}
     * @param editMode boolean <code>true</code> if an existing item is to be edited, <code>false</code> to create the
     *            content of a new item
     * @param context {@link IEclipseContext}
     * @return {@link FormText} */
    public static FormText createFormText(final Composite parent, final boolean editMode,
            final IEclipseContext context) {
        final FormText form = ContextInjectionFactory.make(FormText.class, context);
        form.setEditMode(editMode);
        form.initialize(parent);
        return form;
    }

    private void initialize(final Composite parent) {
        final int lNumColumns = 6;
        this.container = createComposite(parent, lNumColumns, 3);

        createLabel(RelationsMessages.getString("FormText.lbl.type"), this.container); //$NON-NLS-1$
        this.typeCombo = createTypeCombo(this.container, lNumColumns);
        this.checkDirtyService.register(this.typeCombo);

        this.authorText = new RequiredText(
                RelationsMessages.getString("FormText.lbl.author"), this.container, lNumColumns); //$NON-NLS-1$
        this.authorText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                FormText.this.authorFieldStatus = Status.OK_STATUS;
                FormText.this.authorText.setErrorDecoration(false);
                notifyAboutUpdate(getStatuses());
            }

            @Override
            public void focusLost(final FocusEvent event) {
                if (((Text) event.widget).getText().isEmpty()) {
                    FormText.this.authorFieldStatus = FormText.this.authorEmpty;
                    FormText.this.authorText.setErrorDecoration(true);
                }
                notifyAboutUpdate(getStatuses());
            }
        });
        this.authorText.addModifyListener(event -> {
            if (!FormText.this.initialized) {
                return;
            }
            if (!((Text) event.widget).getText().isEmpty()) {
                notifyAboutUpdate(getStatuses());
            }
        });
        this.checkDirtyService.register(this.authorText);

        WidgetCreator lCreator = new WidgetCreator(
                RelationsMessages.getString("FormText.lbl.coauthor"), this.container, lNumColumns); //$NON-NLS-1$
        this.coauthorLabel = lCreator.getLabel();
        this.coauthorText = lCreator.getText();
        this.checkDirtyService.register(this.coauthorText);

        this.titleText = new RequiredText(
                RelationsMessages.getString("FormText.lbl.title"), this.container, lNumColumns); //$NON-NLS-1$
        this.titleText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                FormText.this.titleFieldStatus = Status.OK_STATUS;
                FormText.this.titleText.setErrorDecoration(false);
                notifyAboutUpdate(getStatuses());
            }

            @Override
            public void focusLost(final FocusEvent event) {
                if (((Text) event.widget).getText().isEmpty()) {
                    FormText.this.titleFieldStatus = FormText.this.titleEmpty;
                    FormText.this.titleText.setErrorDecoration(true);
                }
                notifyAboutUpdate(getStatuses());
            }
        });
        this.titleText.addModifyListener(event -> {
            if (!FormText.this.initialized) {
                return;
            }
            if (!((Text) event.widget).getText().isEmpty()) {
                notifyAboutUpdate(getStatuses());
            }
        });
        this.checkDirtyService.register(this.titleText);

        lCreator = new WidgetCreator(
                RelationsMessages.getString("FormText.lbl.subtitle"), this.container, lNumColumns); //$NON-NLS-1$
        this.subtitleText = lCreator.getText();
        this.checkDirtyService.register(this.subtitleText);

        lCreator = new WidgetCreator(
                RelationsMessages.getString("FormText.lbl.year"), this.container, lNumColumns); //$NON-NLS-1$
        this.yearText = lCreator.getText();
        this.checkDirtyService.register(this.yearText);

        final StyledTextCreator lSTCreator = new StyledTextCreator(
                RelationsMessages.getString("FormText.lbl.journal"), this.container, lNumColumns); //$NON-NLS-1$
        this.journalLabel = lSTCreator.getLabel();
        this.journalText = lSTCreator.getText();
        this.journalTextHelper = new StyledFieldHelper(this.journalText, this.log);
        this.checkDirtyService.register(this.journalText);

        lCreator = new WidgetCreator(
                RelationsMessages.getString("FormText.lbl.pages"), this.container, 2); //$NON-NLS-1$
        this.pagesText = lCreator.getText();
        this.checkDirtyService.register(this.pagesText);
        lCreator = new WidgetCreator(
                RelationsMessages.getString("FormText.lbl.vol"), this.container, 2); //$NON-NLS-1$
        this.volumeText = lCreator.getText();
        this.checkDirtyService.register(this.volumeText);
        this.volumeText.addVerifyListener(event -> verifyNumeric(event));
        lCreator = new WidgetCreator(RelationsMessages.getString("FormText.lbl.no"), this.container, 2); //$NON-NLS-1$
        this.numberText = lCreator.getText();
        this.checkDirtyService.register(this.numberText);
        this.numberText.addVerifyListener(event -> verifyNumeric(event));
        lCreator = new WidgetCreator(RelationsMessages.getString("FormText.lbl.publisher"), this.container, //$NON-NLS-1$
                lNumColumns);
        this.publisherText = lCreator.getText();
        this.checkDirtyService.register(this.publisherText);
        lCreator = new WidgetCreator(RelationsMessages.getString("FormText.lbl.location"), this.container, lNumColumns); //$NON-NLS-1$
        this.locationLabel = lCreator.getLabel();
        this.locationText = lCreator.getText();
        this.checkDirtyService.register(this.locationText);

        this.styledText = createStyledText(this.container, lNumColumns, 70);
        this.checkDirtyService.register(this.styledText);
        this.authorFieldStatus = Status.OK_STATUS;
        this.titleFieldStatus = Status.OK_STATUS;

        // we have to align some fields
        final int lIndent = FieldDecorationRegistry.getDefault()
                .getMaximumDecorationWidth();
        ((GridData) this.typeCombo.getLayoutData()).horizontalIndent = lIndent;
        ((GridData) this.coauthorText.getLayoutData()).horizontalIndent = lIndent;
        ((GridData) this.subtitleText.getLayoutData()).horizontalIndent = lIndent;
        ((GridData) this.yearText.getLayoutData()).horizontalIndent = lIndent;
        ((GridData) this.journalText.getLayoutData()).horizontalIndent = lIndent;
        ((GridData) this.pagesText.getLayoutData()).horizontalIndent = lIndent;
        ((GridData) this.publisherText.getLayoutData()).horizontalIndent = lIndent;
        ((GridData) this.locationText.getLayoutData()).horizontalIndent = lIndent;

        addCreatedLabel(this.container, 0, lNumColumns);

        this.container.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent inEvent) {
                FormText.this.authorText.setFocus();
            }

            @Override
            public void focusLost(final FocusEvent inEvent) {
                // do nothing
            }
        });
    }

    private Combo createTypeCombo(final Composite container, final int numColumns) {
        final Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setItems(
                RelationsMessages.getString("FormText.entry.book"), //$NON-NLS-1$
                RelationsMessages.getString("FormText.entry.article"), //$NON-NLS-1$
                RelationsMessages.getString("FormText.entry.contribution"), //$NON-NLS-1$
                RelationsMessages.getString("FormText.entry.webpage")); //$NON-NLS-1$
        combo.select(0);
        combo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent inEvent) {
                setInitState();
                setDisablePattern(((Combo) inEvent.getSource())
                        .getSelectionIndex());
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent inEvent) {
                // Nothing to do.
            }
        });

        combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        final Label lLabel = new Label(container, SWT.NULL);
        final GridData lData = new GridData();
        lData.horizontalSpan = numColumns - 2;
        lLabel.setLayoutData(lData);

        return combo;
    }

    private void setInitState() {
        this.journalTextHelper.setEditable(true);
        this.pagesText.setEnabled(true);
        this.volumeText.setEnabled(true);
        this.numberText.setEnabled(true);
        this.publisherText.setEnabled(true);
        this.locationText.setEnabled(true);
        this.coauthorLabel.setText(RelationsMessages.getString("FormText.lbl.coauthor")); //$NON-NLS-1$
        this.journalLabel.setText(RelationsMessages.getString("FormText.lbl.journal")); //$NON-NLS-1$
        this.locationLabel.setText(RelationsMessages.getString("FormText.lbl.location")); //$NON-NLS-1$
    }

    private void setDisablePattern(final int index) {
        switch (index) {
            case AbstractText.TYPE_BOOK:
                this.journalTextHelper.setEditable(false);
                this.pagesText.setEnabled(false);
                this.volumeText.setEnabled(false);
                this.numberText.setEnabled(false);
                this.journalTextHelper.removeListeners();
                break;
            case AbstractText.TYPE_ARTICLE:
                this.publisherText.setEnabled(false);
                this.locationText.setEnabled(false);
                this.journalTextHelper.removeListeners();
                break;
            case AbstractText.TYPE_CONTRIBUTION:
                this.pagesText.setEnabled(false);
                this.volumeText.setEnabled(false);
                this.numberText.setEnabled(false);
                this.coauthorLabel.setText(RelationsMessages
                        .getString("FormText.lbl.editors")); //$NON-NLS-1$
                this.journalLabel.setText(RelationsMessages
                        .getString("FormText.lbl.booktitle")); //$NON-NLS-1$
                this.journalTextHelper.removeListeners();
                break;
            case AbstractText.TYPE_WEBPAGE:
                this.volumeText.setEnabled(false);
                this.numberText.setEnabled(false);
                this.publisherText.setEnabled(false);
                this.journalLabel.setText(RelationsMessages
                        .getString("FormText.lbl.webpage")); //$NON-NLS-1$
                this.locationLabel.setText(RelationsMessages
                        .getString("FormText.lbl.accessed")); //$NON-NLS-1$
                this.journalTextHelper.addListeners();
                break;
            default:
                this.journalTextHelper.setEditable(false);
                this.pagesText.setEnabled(false);
                this.volumeText.setEnabled(false);
                this.numberText.setEnabled(false);
                this.journalTextHelper.removeListeners();
                break;
        }
    }

    @Override
    protected IStatus[] getStatuses() {
        return new IStatus[] { this.authorFieldStatus, this.titleFieldStatus };
    }

    @Override
    public boolean getPageComplete() {
        return this.authorText.getText().length() * this.titleText.getText().length() != 0;
    }

    private void verifyNumeric(final VerifyEvent inEvent) {
        if (inEvent.keyCode < 32) {
            inEvent.doit = true;
            return;
        }

        final String lOld = ((Text) inEvent.widget).getText();
        final String lNew = lOld.substring(0, inEvent.start) + inEvent.text
                + lOld.substring(inEvent.end);
        try {
            Integer.parseInt(lNew);
            inEvent.doit = true;
        }
        catch (final Exception exc) {
            inEvent.doit = false;
        }
    }

    /** Initialize the input fields with the values to edit.
     *
     * @param type int
     * @param title String
     * @param text String
     * @param author String
     * @param coAuthor String
     * @param subTitle String
     * @param year String
     * @param journal String
     * @param pages String
     * @param volume String
     * @param number String
     * @param publisher String
     * @param location String
     * @param created String
     * @throws SAXException
     * @throws IOException */
    public void initialize(final int type, final String title,
            final String text, final String author,
            final String coAuthor, final String subTitle,
            final String year, final String journal, final String pages,
            final String volume, final String number,
            final String publisher, final String location,
            final String created) throws IOException, SAXException {
        setInitState();
        this.typeCombo.select(type);
        this.authorText.setText(author);
        this.coauthorText.setText(coAuthor);
        this.titleText.setText(title);
        this.subtitleText.setText(subTitle);
        this.yearText.setText(year);
        this.journalText.setText(journal);
        this.pagesText.setText(pages);
        this.volumeText.setText(volume);
        this.numberText.setText(number);
        this.publisherText.setText(publisher);
        this.locationText.setText(location);
        this.styledText.setTaggedText(text);
        setCreatedInfo(created);
        initialize();
    }

    @Override
    public void initialize() {
        setDisablePattern(this.typeCombo.getSelectionIndex());
        this.checkDirtyService.freeze();
        this.initialized = true;
    }

    public String getTextText() {
        return this.styledText.getTaggedText();
    }

    public String getTextTitle() {
        return this.titleText.getText();
    }

    public String getSubTitle() {
        return this.subtitleText.getText();
    }

    public String getAuthorName() {
        return this.authorText.getText();
    }

    public String getCoAuthorName() {
        return this.coauthorText.getText();
    }

    public String getYear() {
        return this.yearText.getText();
    }

    public String getJournal() {
        return this.journalText.getText();
    }

    public String getPages() {
        return this.pagesText.getText();
    }

    public int getArticleVolume() {
        try {
            return Integer.parseInt(this.volumeText.getText());
        }
        catch (final NumberFormatException exc) {
            return 0;
        }
    }

    public int getArticleNumber() {
        try {
            return Integer.parseInt(this.numberText.getText());
        }
        catch (final NumberFormatException exc) {
            return 0;
        }
    }

    public String getPublisher() {
        return this.publisherText.getText();
    }

    public String getLocation() {
        return this.locationText.getText();
    }

    public int getTextType() {
        return this.typeCombo.getSelectionIndex();
    }

}
