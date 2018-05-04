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
package org.elbe.relations.internal.data;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.actions.RelationsPreferences;
import org.elbe.relations.internal.services.IDBController;
import org.elbe.relations.services.IDBConnectionConfig;

/**
 * Store for temporary DB settins.
 *
 * @author Luthiger
 */
public class TempSettings implements IDBSettings {

	private final String host;
	private final String catalog;
	private final String username;
	private final String password;
	private final IDBConnectionConfig dbConfig;

	/**
	 * TempSettings constructor.
	 *
	 * @param inPluginID
	 * @param inHost
	 * @param inCatalog
	 * @param inUsername
	 * @param inPassword
	 * @param inDBController
	 *            {@link IDBController}
	 */
	public TempSettings(final String inPluginID, final String inHost,
			final String inCatalog, final String inUsername,
			final String inPassword, final IDBController inDBController) {
		this(inHost, inCatalog, inUsername, inPassword, inDBController
				.getConfiguration(inPluginID));
	}

	/**
	 * TempSettings constructor.
	 *
	 * @param inHost
	 * @param inCatalog
	 * @param inUsername
	 * @param inPassword
	 * @param inDBConfig
	 *            {@link IDBConnectionConfig}
	 */
	public TempSettings(final String inHost, final String inCatalog,
			final String inUsername, final String inPassword,
			final IDBConnectionConfig inDBConfig) {
		this.dbConfig = inDBConfig;
		this.host = inHost;
		this.catalog = inCatalog;
		this.username = inUsername;
		this.password = inPassword;
	}

	@Override
	public IDBConnectionConfig getDBConnectionConfig() {
		return this.dbConfig;
	}

	@Override
	public String getCatalog() {
		return this.catalog;
	}

	@Override
	public String getHost() {
		return this.host;
	}

	@Override
	public String getUser() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	public void saveToPreferences() {
		final IEclipsePreferences lStore = RelationsPreferences
		        .getPreferences();
		lStore.put(RelationsConstants.KEY_DB_PLUGIN_ID, this.dbConfig.getName());
		lStore.put(RelationsConstants.KEY_DB_HOST, this.host);
		lStore.put(RelationsConstants.KEY_DB_CATALOG, this.catalog);
		lStore.put(RelationsConstants.KEY_DB_USER_NAME, this.username);
		lStore.put(RelationsConstants.KEY_DB_PASSWORD, this.password);
	}

	@Override
	public String getDBName() {
		if (getDBConnectionConfig().isEmbedded()) {
			return DBSettings.EMBEDDED + ": " + getCatalog(); //$NON-NLS-1$
		}
		return DBSettings.CLIENT_URL_PATTERN.format(new Object[] {
				getDBConnectionConfig().getSubprotocol(), getHost(),
				getCatalog() });
	}

}
