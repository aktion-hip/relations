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
package org.elbe.relations.internal.preferences;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.elbe.relations.RelationsMessages;

/**
 * Preference page to provide Eclipse theme switching.<br />
 * This is an Eclipse 3 preference page. To make it e4, let the values for the
 * annotated field be injected (instead of using the method init()).
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class AppearancePage extends AbstractPreferencePage {

	@Inject
	private IThemeManager themeManager;

	private Combo themes;
	private IThemeEngine themeEngine;
	private ThemeHelper themeHelper;

	@Override
	protected Control createContents(final Composite inParent) {
		themeEngine = themeManager.getEngineForDisplay(inParent.getDisplay());
		themeHelper = new ThemeHelper(themeEngine);

		final Composite outComposite = new Composite(inParent, SWT.NONE);
		final int lColumns = 2;
		setLayout(outComposite, lColumns);
		outComposite.setFont(inParent.getFont());

		createLabel(outComposite, RelationsMessages
		        .getString("preferences.appearance.label.themes")); //$NON-NLS-1$
		themes = createCombo(outComposite, themeHelper.getThemeItems());
		themes.select(themeHelper.getActiveIndex());

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
		if (themeEngine != null) {
			themeEngine.setTheme(
			        themeHelper.getTheme(themes.getSelectionIndex()), true);
		}
	}

	// ---

	private static class ThemeHelper {
		private static final String DFT_THEME = "Default";

		private final String[] themeItems;
		private int activeIndex = 0;
		private final List<ITheme> themes;

		ThemeHelper(final IThemeEngine inThemeEngine) {
			themes = inThemeEngine.getThemes();
			final String lActiveId = getActiveId(inThemeEngine.getActiveTheme(),
			        themes);
			themeItems = new String[themes.size()];
			int i = 0;
			for (final ITheme lTheme : themes) {
				if (lActiveId.equals(lTheme.getId())) {
					activeIndex = i;
				}
				themeItems[i++] = lTheme.getLabel();
			}
		}

		private String getActiveId(ITheme active, List<ITheme> themes) {
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
			return themeItems;
		}

		protected int getActiveIndex() {
			return activeIndex;
		}

		protected ITheme getTheme(final int inIndex) {
			return themes.get(inIndex);
		}

	}

}
