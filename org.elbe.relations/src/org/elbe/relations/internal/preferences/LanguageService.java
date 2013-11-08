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

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.elbe.relations.RelationsConstants;

/**
 * Service for language settings.
 * 
 * @author Luthiger
 */
@Creatable
@SuppressWarnings("restriction")
public class LanguageService {

	private final Locale appLocale;
	private Locale contentLocale;

	/**
	 * LanguageService constructor.
	 * 
	 * @param inLanguageContent
	 *            String the content language (ISO 639 code)
	 */
	@Inject
	public LanguageService(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_LANGUAGE_CONTENT) final String inLanguageContent) {
		appLocale = createLocale();
		setContentLanguage(inLanguageContent);
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
	 * Tracker for changes of the content language.
	 * 
	 * @param inLanguageContent
	 *            String language ISO 639 code
	 */
	@Inject
	void setContentLanguage(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_LANGUAGE_CONTENT) final String inLanguageContent) {
		contentLocale = new Locale(
				inLanguageContent == null || inLanguageContent.isEmpty() ? RelationsConstants.DFT_LANGUAGE
						: inLanguageContent);
	}

	/**
	 * @return {@link Collator} the language of the content (i.e. for
	 *         indexing/searching the database).
	 */
	public Collator getContentLanguage() {
		return Collator.getInstance(contentLocale);
	}

	/**
	 * @return {@link Locale} the application's locale (i.e. for the labels to
	 *         display).
	 */
	public Locale getAppLocale() {
		return appLocale;
	}

	/**
	 * Convenience method: returns the content language from the preferences.
	 * 
	 * @return {@link Locale}
	 */
	public static Locale getContentLocale() {
		final IEclipsePreferences lPreferences = InstanceScope.INSTANCE
				.getNode(RelationsConstants.PREFERENCE_NODE);
		return new Locale(lPreferences.get(
				RelationsConstants.KEY_LANGUAGE_CONTENT,
				Locale.ENGLISH.getLanguage()));
	}

}
