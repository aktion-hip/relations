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
package org.elbe.relations.internal.preferences;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.elbe.relations.RelationsConstants;

/**
 * 
 * @author Luthiger
 */
public class PreferenceHelper {

	private final IEclipsePreferences preferences;
	private final Locale appLocale;

	public PreferenceHelper(final IEclipsePreferences inPreferences) {
		preferences = inPreferences;
		appLocale = createLocale();
	}

	private Locale createLocale() {
		try {
			final String lNL = Platform.getNL();
			return new Locale(lNL.substring(0, 2), lNL.substring(3));
		}
		catch (final NullPointerException exc) {
			// for testing purpose
			return new Locale("en"); //$NON-NLS-1$
		}
	}

	/**
	 * @return {@link Collator} the language of the content (i.e. for
	 *         indexing/searching the database).
	 */
	public Collator getContentLanguage() {
		return Collator.getInstance(new Locale(preferences.get(
				RelationsConstants.KEY_LANGUAGE_CONTENT,
				Locale.ENGLISH.getLanguage())));
	}

	/**
	 * @return {@link Locale} the application's locale (i.e. for the labels to
	 *         display).
	 */
	public Locale getAppLocale() {
		return appLocale;
	}

}
