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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.RequiredText;
import org.xml.sax.SAXException;

/** Input form to enter or edit the data of a term item.
 *
 * @author Benno Luthiger */
public final class FormTerm extends AbstractEditForm {
    private RequiredText titleText;
    private IStatus titleFieldStatus;
    private final IStatus titleEmpty = createErrorStatus(RelationsMessages.getString("FormTerm.error.msg")); //$NON-NLS-1$
    private boolean initialized = false;

    /** Factory method to create instances of <code>FormTerm</code>.
     *
     * @param parent {@link Composite}
     * @param editMode boolean <code>true</code> if an existing item is to be edited, <code>false</code> to create the
     *            content of a new item
     * @param context {@link IEclipseContext}
     * @return {@link FormTerm} */
    public static FormTerm createFormTerm(final Composite parent, final boolean editMode,
            final IEclipseContext context) {
        final FormTerm form = ContextInjectionFactory.make(FormTerm.class, context);
        form.setEditMode(editMode);
        form.initialize(parent);
        return form;
    }

    private void initialize(final Composite inParent) {
        final int lNumColumns = 1;
        this.container = createComposite(inParent, lNumColumns, 9);

        this.titleText = new RequiredText(this.container, lNumColumns);
        this.titleText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent inEvent) {
                FormTerm.this.titleFieldStatus = Status.OK_STATUS;
                FormTerm.this.titleText.setErrorDecoration(false);
                notifyAboutUpdate(getStatuses());
            }

            @Override
            public void focusLost(final FocusEvent event) {
                if (((Text) event.widget).getText().isEmpty()) {
                    FormTerm.this.titleFieldStatus = FormTerm.this.titleEmpty;
                    FormTerm.this.titleText.setErrorDecoration(true);
                }
                notifyAboutUpdate(getStatuses());
            }
        });
        this.titleText.addModifyListener(event -> {
            if (!FormTerm.this.initialized) {
                return;
            }
            if (!((Text) event.widget).getText().isEmpty()) {
                notifyAboutUpdate(getStatuses());
            }
        });
        this.checkDirtyService.register(this.titleText);

        this.styledText = createStyledText(this.container);

        // we add indentation to align the text area with the required titleText
        final int lIndent = FieldDecorationRegistry.getDefault()
                .getMaximumDecorationWidth();
        setIndent(lIndent);

        this.titleFieldStatus = Status.OK_STATUS;
        this.checkDirtyService.register(this.styledText);

        addCreatedLabel(this.container, lIndent, lNumColumns);
    }

    public String getTermTitle() {
        return this.titleText.getText();
    }

    public String getTermText() {
        return this.styledText.getTaggedText();
    }

    /** @see org.elbe.relations.wizards.IEditForm#initialize() */
    @Override
    public void initialize() {
        this.titleText.setFocus();
        this.checkDirtyService.freeze();
        this.initialized = true;
    }

    /** Initialize the input fields with the values to edit.
     *
     * @param title String
     * @param text String
     * @param created String
     * @throws SAXException
     * @throws IOException */
    public void initialize(final String title, final String text, final String created)
            throws IOException, SAXException {
        this.titleText.setText(title);
        this.styledText.setTaggedText(text);
        setCreatedInfo(created);
        initialize();
    }

    @Override
    protected IStatus[] getStatuses() {
        return new IStatus[] { this.titleFieldStatus };
    }

    @Override
    public boolean getPageComplete() {
        return !this.titleText.getText().isEmpty();
    }

}
