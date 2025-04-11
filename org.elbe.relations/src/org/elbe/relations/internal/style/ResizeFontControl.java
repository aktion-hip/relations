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
package org.elbe.relations.internal.style;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.elbe.relations.RelationsConstants;

import jakarta.annotation.PostConstruct;

/**
 * Displays the combo to resize the font size.<br />
 * Instances of this class have to react on changes of the font size preference
 * <code>RelationsConstants.KEY_TEXT_FONT_SIZE</code>, e.g. made through the
 * PreferenceDialog. On the other side, changes made through this combo have to
 * be stored in the applications preferences.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ResizeFontControl {
    private Combo combo;
    private int initIndex = findIndexOf(Integer
            .toString(RelationsConstants.DFT_TEXT_FONT_SIZE));

    @Inject
    @Preference(nodePath = RelationsConstants.PREFERENCE_NODE)
    private IEclipsePreferences preferences;

    @PostConstruct
    public void createWidget(final Composite inParent) {
        this.combo = new Combo(inParent, SWT.DROP_DOWN);
        this.combo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(final SelectionEvent inEvent) {
                handleWidgetSelected();
            }

            @Override
            public void widgetSelected(final SelectionEvent inEvent) {
                handleWidgetSelected();
            }
        });
        this.combo.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent inEvent) {
                // do nothing
            }

            @Override
            public void focusLost(final FocusEvent inEvent) {
                // refresh();
            }
        });

        this.combo.setItems(RelationsConstants.INIT_SIZES);
        this.combo.select(this.initIndex);
        this.combo.setEnabled(false);
    }

    private void handleWidgetSelected() {
        Integer lFontSize = null;
        try {
            lFontSize = new Integer(this.combo.getText());
        }
        catch (final NumberFormatException exc) {
            // intentionally left empty
        }
        if (lFontSize != null) {
            this.preferences.putInt(RelationsConstants.KEY_TEXT_FONT_SIZE,
                    lFontSize.intValue());
        }
    }

    private void setValue(final String inValue) {
        final int lIndex = findIndexOf(inValue);
        if (this.combo != null && !this.combo.isDisposed()) {
            if (lIndex >= 0) {
                this.combo.select(lIndex);
            } else {
                this.combo.setText(inValue);
            }
        } else {
            if (lIndex >= 0) {
                this.initIndex = lIndex;
            }
        }
    }

    private int findIndexOf(final String inText) {
        for (int i = 0; i < RelationsConstants.INIT_SIZES.length; i++) {
            if (RelationsConstants.INIT_SIZES[i].equalsIgnoreCase(inText)) {
                return i;
            }
        }
        return -1;
    }

    @Inject
    public void setFontSize(
            @Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_TEXT_FONT_SIZE) final String inFontSize) {
        setValue(inFontSize);
    }

    @Inject
    @Optional
    public void updateEnablement(
            @UIEventTopic(RelationsConstants.TOPIC_STYLE_ITEMS_FORM) final Boolean inEnable) {
        if (this.combo != null && !this.combo.isDisposed() && !this.combo.isFocusControl()) {
            this.combo.setEnabled(inEnable.booleanValue());
        }
    }

    public void dispose() {
        if (this.combo != null) {
            this.combo.dispose();
        }
    }

    public Control getControl() {
        return this.combo;
    }

}
