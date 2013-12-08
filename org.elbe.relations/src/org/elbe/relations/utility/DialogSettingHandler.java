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
package org.elbe.relations.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.elbe.relations.RelationsConstants;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Handler class offering the functionality to historize the widget input, i.e.
 * to make it available in further dialogs.
 * 
 * @author Luthiger
 */
public class DialogSettingHandler {
	private static final String SEP = ";"; //$NON-NLS-1$

	private final String dialogSection;
	private final String dialogTerm;
	private final Preferences settings;

	/**
	 * DialogSettingHandler constructor.
	 * 
	 * @param inSection
	 *            String the name of the section in the dialog settings.
	 * @param inTerm
	 *            String the name of the term within the section in the dialog
	 *            settings.
	 */
	public DialogSettingHandler(final String inSection, final String inTerm) {
		dialogSection = inSection;
		dialogTerm = inTerm;
		final IEclipsePreferences lPrerences = InstanceScope.INSTANCE
				.getNode(RelationsConstants.PREFERENCE_NODE_DIALOG);
		settings = lPrerences.node(dialogSection);
	}

	/**
	 * @return {@link Preferences}
	 */
	protected Preferences getSettings() {
		return settings;
	}

	/**
	 * @return String[] the recent values of the specified widget.
	 */
	public String[] getRecentValues() {
		final String lValues = settings.get(dialogTerm, ""); //$NON-NLS-1$
		return lValues.split(SEP);
	}

	/**
	 * Saves the specified value to the dialog's history.
	 * 
	 * @param inValue
	 *            String the value to put to the history.
	 * @throws BackingStoreException
	 */
	public void saveToHistory(final String inValue)
			throws BackingStoreException {
		if (settings == null) {
			return;
		}

		String[] lRecent = getRecentValues();
		lRecent = addToHistory(lRecent, inValue);
		settings.put(dialogTerm, flatten(lRecent));
		settings.flush();
	}

	private String flatten(final String[] inValues) {
		final StringBuilder out = new StringBuilder();
		boolean lFirst = true;
		for (final String lValue : inValues) {
			if (!lFirst) {
				out.append(SEP);
			}
			lFirst = false;
			out.append(lValue);
		}
		return new String(out);
	}

	private String[] addToHistory(final String[] inRecent, final String inNew) {
		final ArrayList<String> lRecent = new ArrayList<String>(
				Arrays.asList(inRecent));
		addToHistory(lRecent, inNew);
		final String[] outRecent = new String[lRecent.size()];
		lRecent.toArray(outRecent);
		return outRecent;
	}

	private void addToHistory(final List<String> inHistory, final String inNew) {
		inHistory.remove(inNew);
		inHistory.add(0, inNew);
		if (inHistory.size() > RelationsConstants.DIALOG_HISTORY_LENGTH) {
			inHistory.remove(RelationsConstants.DIALOG_HISTORY_LENGTH);
		}
	}

}
