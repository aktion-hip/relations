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
package org.elbe.relations.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Base class for preference pages used in Relations.
 *
 * @author Luthiger
 */
public abstract class AbstractPreferencePage extends PreferencePage {

    protected AbstractPreferencePage() {
        super();
    }

    protected AbstractPreferencePage(final String title) {
        super(title);
    }

    protected AbstractPreferencePage(final String title, final ImageDescriptor image) {
        super(title, image);
    }

    /**
     * @see PreferencePage#doGetPreferenceStore
     */
    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return null;
        // return
        // InstanceScope.INSTANCE.getNode(RelationsConstants.PREFERENCE_NODE);
    }

    /** Utility method that creates a label instance and sets the default layout data.
     *
     * @param parent the inParent for the new label
     * @param text the text for the new label
     * @return the new label */
    protected Label createLabel(final Composite parent, final String text) {
        final Label outLabel = new Label(parent, SWT.LEFT);
        outLabel.setText(text);
        final GridData lData = new GridData();
        lData.horizontalAlignment = GridData.FILL;
        lData.widthHint = convertWidthInCharsToPixels(22);
        outLabel.setLayoutData(lData);
        return outLabel;
    }

    protected Combo createCombo(final Composite parent, final String[] items) {
        final Combo outCombo = new Combo(parent,
                SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
        outCombo.setItems(items);
        outCombo.setLayoutData(createGridData());
        return outCombo;
    }

    protected GridData createGridData() {
        return new GridData(SWT.FILL, SWT.CENTER, true, false);
    }

    protected Combo createLabelCombo(final Composite parent, final String label, final String[] items) {
        createLabel(parent, label);
        return createCombo(parent, items);
    }

    protected Text createLabelText(final Composite parent, final String label) {
        createLabel(parent, label);
        return createText(parent);
    }

    private Text createText(final Composite parent) {
        final Text outText = new Text(parent, SWT.BORDER | SWT.SINGLE);
        outText.setTextLimit(5);
        outText.setLayoutData(createGridData());
        return outText;
    }

    protected void createSeparator(final Composite parent, final int columns) {
        final Label lSeparator = new Label(parent,
                SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.setLayoutData(
                new GridData(SWT.FILL, SWT.NONE, true, false, columns, 1));
    }

    protected void setLayout(final Composite composite, final int columns) {
        final GridLayout lLayout = new GridLayout();
        lLayout.numColumns = columns;
        lLayout.marginHeight = 0;
        lLayout.marginWidth = 0;
        composite.setLayout(lLayout);
    }

}
