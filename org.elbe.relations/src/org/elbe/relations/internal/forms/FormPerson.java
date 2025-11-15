/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.RequiredText;
import org.xml.sax.SAXException;

/**
 * Input form to enter or edit the data of a person item.
 *
 * @author Benno Luthiger
 */
public class FormPerson extends AbstractEditForm {
    private RequiredText nameText;
    private Text firstnameText;
    private Text fromText;
    private Text toText;
    private IStatus nameFieldStatus;
    private boolean initialized = false;

    /** Factory method to create instances of <code>FormPerson</code>.
     *
     * @param parent {@link Composite}
     * @param editMode boolean <code>true</code> if an existing item is to be edited, <code>false</code> to create the
     *            content of a new item
     * @param context {@link IEclipseContext}
     * @return {@link FormPerson} */
    public static FormPerson createFormPerson(final Composite parent, final boolean editMode,
            final IEclipseContext context) {
        final FormPerson form = ContextInjectionFactory.make(FormPerson.class, context);
        form.setEditMode(editMode);
        form.initialize(parent);
        return form;
    }

    private void initialize(final Composite parent) {
        final int lNumColumns = 4;
        this.container = createComposite(parent, lNumColumns, 7);
        final int lLabelWidth = convertWidthInCharsToPixels(this.container, 6);

        final Composite lNameFill = createNameContainers(this.container, 2);
        final Label nameLabel = createLabel(RelationsMessages.getString("FormPerson.lbl.name"), lNameFill); //$NON-NLS-1$
        this.nameText = new RequiredText(lNameFill, 1);
        this.nameText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent inEvent) {
                FormPerson.this.nameFieldStatus = Status.OK_STATUS;
                FormPerson.this.nameText.setErrorDecoration(false);
                notifyAboutUpdate(getStatuses());
            }

            @Override
            public void focusLost(final FocusEvent event) {
                if (((Text) event.widget).getText().isEmpty()) {
                    FormPerson.this.nameFieldStatus = new Status(IStatus.ERROR, Activator
                            .getSymbolicName(), 1, RelationsMessages
                            .getString("FormPerson.missing.name"), null); //$NON-NLS-1$
                    FormPerson.this.nameText.setErrorDecoration(true);
                }
                notifyAboutUpdate(getStatuses());
            }
        });
        this.nameText.addModifyListener(event -> {
            if (!FormPerson.this.initialized) {
                return;
            }
            if (!((Text) event.widget).getText().isEmpty()) {
                notifyAboutUpdate(getStatuses());
            }
        });
        this.checkDirtyService.register(this.nameText);

        final Composite lFirstnameFill = createNameContainers(this.container, 2);
        createLabel(
                RelationsMessages.getString("FormPerson.lbl.firstname"), lFirstnameFill); //$NON-NLS-1$
        this.firstnameText = createText(lFirstnameFill, 1);
        this.checkDirtyService.register(this.firstnameText);

        WidgetCreator lCreator = new WidgetCreator(
                RelationsMessages.getString("FormPerson.lbl.from"), this.container, 2); //$NON-NLS-1$
        final Label fromLabel = lCreator.getLabel();
        setWidth(fromLabel, lLabelWidth);
        this.fromText = lCreator.getText();
        this.checkDirtyService.register(this.fromText);

        lCreator = new WidgetCreator(RelationsMessages.getString("FormPerson.lbl.to"), this.container, 2); //$NON-NLS-1$
        final Label toLabel = lCreator.getLabel();
        setWidth(toLabel, lLabelWidth);
        this.toText = lCreator.getText();
        this.checkDirtyService.register(this.toText);

        this.styledText = createStyledText(this.container, lNumColumns, 70);
        this.checkDirtyService.register(this.styledText);
        this.nameFieldStatus = Status.OK_STATUS;

        // we have to align some fields
        final int lIndent = FieldDecorationRegistry.getDefault()
                .getMaximumDecorationWidth();
        ((GridData) nameLabel.getLayoutData()).horizontalIndent = lIndent;
        ((GridData) fromLabel.getLayoutData()).horizontalIndent = lIndent;
        setIndent(lIndent);

        addCreatedLabel(this.container, lIndent, lNumColumns);
    }

    private Composite createNameContainers(final Composite inParent,
            final int inNumColumns) {
        final Composite outComposite = new Composite(inParent, SWT.NULL);
        final GridLayout lLayout = new GridLayout(1, false);
        lLayout.marginWidth = 0;
        outComposite.setLayout(lLayout);
        outComposite.setLayoutData(new GridData(SWT.FILL, SWT.NULL, false,
                false, inNumColumns, SWT.NULL));
        return outComposite;
    }

    /**
     * Initialize the input fields with the values to edit.
     *
     * @param inName
     *            String
     * @param inFirstName
     *            String
     * @param inFrom
     *            String
     * @param inTo
     *            String
     * @param inText
     *            String
     * @param inCreated
     *            String
     * @throws SAXException
     * @throws IOException
     */
    public void initialize(final String inName, final String inFirstName,
            final String inFrom, final String inTo, final String inText,
            final String inCreated) throws IOException, SAXException {
        this.nameText.setText(inName);
        this.firstnameText.setText(inFirstName);
        this.fromText.setText(inFrom);
        this.toText.setText(inTo);
        this.styledText.setTaggedText(inText);
        setCreatedInfo(inCreated);
        initialize();
    }

    @Override
    public void initialize() {
        this.nameText.setFocus();
        this.checkDirtyService.freeze();
        this.initialized = true;
    }

    public String getTextText() {
        return this.styledText.getTaggedText();
    }

    public String getPersonName() {
        return this.nameText.getText();
    }

    public String getPersonFirstname() {
        return this.firstnameText.getText();
    }

    public String getPersonFrom() {
        return this.fromText.getText();
    }

    public String getPersonTo() {
        return this.toText.getText();
    }

    @Override
    protected IStatus[] getStatuses() {
        return new IStatus[] { this.nameFieldStatus };
    }

    @Override
    public boolean getPageComplete() {
        return !this.nameText.getText().isEmpty();
    }

}
