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

import java.util.List;

import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.elbe.relations.RelationsMessages;

import jakarta.inject.Inject;

/**
 * Preference page to provide Eclipse theme switching.<br />
 * This is an Eclipse 3 preference page. To make it e4, let the values for the
 * annotated field be injected (instead of using the method init()).
 *
 * @author Luthiger
 */
public class AppearancePage extends AbstractPreferencePage {

    @Inject
    private IThemeManager themeManager;

    private Combo themes;
    private IThemeEngine themeEngine;
    private ThemeHelper themeHelper;

    @Override
    protected Control createContents(final Composite parent) {
        this.themeEngine = this.themeManager.getEngineForDisplay(parent.getDisplay());
        this.themeHelper = new ThemeHelper(this.themeEngine);

        final Composite outComposite = new Composite(parent, SWT.NONE);
        final int lColumns = 2;
        setLayout(outComposite, lColumns);
        outComposite.setFont(parent.getFont());

        createLabel(outComposite, RelationsMessages
                .getString("preferences.appearance.label.themes")); //$NON-NLS-1$
        this.themes = createCombo(outComposite, this.themeHelper.getThemeItems());
        this.themes.select(this.themeHelper.getActiveIndex());

        return outComposite;
    }

    @Override
    public boolean performOk() {
        saveTheme();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        saveTheme();
    }

    private void saveTheme() {
        if (this.themeEngine != null) {
            this.themeEngine.setTheme(
                    this.themeHelper.getTheme(this.themes.getSelectionIndex()), true);
        }
    }

    // ---

    private static class ThemeHelper {
        private static final String DFT_THEME = "Default"; //$NON-NLS-1$

        private final String[] themeItems;
        private int activeIndex = 0;
        private final List<ITheme> themes;

        ThemeHelper(final IThemeEngine themeEngine) {
            this.themes = themeEngine.getThemes();
            final String lActiveId = getActiveId(themeEngine.getActiveTheme(),
                    this.themes);
            this.themeItems = new String[this.themes.size()];
            int i = 0;
            for (final ITheme lTheme : this.themes) {
                if (lActiveId.equals(lTheme.getId())) {
                    this.activeIndex = i;
                }
                this.themeItems[i++] = lTheme.getLabel();
            }
        }

        private String getActiveId(final ITheme active, final List<ITheme> themes) {
            if (active != null) {
                return active.getId();
            }
            for (final ITheme theme : themes) {
                if (DFT_THEME.equals(theme.getLabel())) {
                    return theme.getId();
                }
            }
            return themes.get(0).getId();
        }

        protected String[] getThemeItems() {
            return this.themeItems;
        }

        protected int getActiveIndex() {
            return this.activeIndex;
        }

        protected ITheme getTheme(final int inIndex) {
            return this.themes.get(inIndex);
        }

    }

}
