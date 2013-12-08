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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.elbe.relations.RelationsConstants;

/**
 * Initializer for the Relations preferences: i.e. language selection, biblio
 * schema selection and DB connection.
 * 
 * @author Benno Luthiger
 */
public class RelationsPreferenceInitializer extends
		AbstractPreferenceInitializer {

	/**
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		final IEclipsePreferences lNode = DefaultScope.INSTANCE
				.getNode(RelationsConstants.PREFERENCE_NODE);

		lNode.put(RelationsConstants.KEY_DB_CATALOG,
				RelationsConstants.DFT_DB_EMBEDDED);
		lNode.put(RelationsConstants.KEY_DB_PLUGIN_ID,
				RelationsConstants.DFT_DBCONFIG_PLUGIN_ID);
		lNode.put(RelationsConstants.KEY_LANGUAGE_CONTENT,
				RelationsConstants.DFT_LANGUAGE);
		lNode.put(RelationsConstants.KEY_BIBLIO_SCHEMA,
				RelationsConstants.DFT_BIBLIO_SCHEMA_ID);
		lNode.put(RelationsConstants.KEY_PRINT_OUT_PLUGIN_ID,
				RelationsConstants.DFT_PRINT_OUT_PLUGIN_ID);
		lNode.putInt(RelationsConstants.KEY_TEXT_FONT_SIZE,
				RelationsConstants.DFT_TEXT_FONT_SIZE);
		lNode.putInt(RelationsConstants.KEY_MAX_SEARCH_HITS,
				RelationsConstants.DFT_MAX_SEARCH_HITS);
		lNode.putInt(RelationsConstants.KEY_MAX_LAST_CHANGED,
				RelationsConstants.DFT_MAX_LAST_CHANGED);
	}
}
