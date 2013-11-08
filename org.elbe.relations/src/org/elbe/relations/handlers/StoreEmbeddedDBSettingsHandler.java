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
package org.elbe.relations.handlers;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Execute;
import org.elbe.relations.RelationsConstants;

/**
 * Handler to store the settings of the embedded DB.
 * 
 * @author Luthiger
 */
public class StoreEmbeddedDBSettingsHandler {

	private final String dbCatalog;

	public StoreEmbeddedDBSettingsHandler(final String inDBCatalog) {
		dbCatalog = inDBCatalog;
	}

	@Execute
	public void execute(final IEclipsePreferences inPreferences) {
		inPreferences.put(RelationsConstants.KEY_DB_CATALOG, dbCatalog);
		inPreferences.put(RelationsConstants.KEY_DB_PLUGIN_ID,
				RelationsConstants.DFT_DBCONFIG_PLUGIN_ID);
		inPreferences.put(RelationsConstants.KEY_DB_USER_NAME, ""); //$NON-NLS-1$
		inPreferences.put(RelationsConstants.KEY_DB_PASSWORD, ""); //$NON-NLS-1$
	}

}
