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

import java.text.MessageFormat;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.services.IDBController;
import org.elbe.relations.services.IDBConnectionConfig;

/**
 * Object intended to be stored in the eclipse context providing the data to
 * access the database.<br />
 * An instance of this class is in the context as <code>DBSettings.class</code>.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
@Creatable
public class DBSettings implements IDBSettings {
	private final static MessageFormat CLIENT_URL_PATTERN = new MessageFormat(
			"jdbc:{0}://{1}/{2}"); //$NON-NLS-1$
	private final static String EMBEDDED = RelationsMessages
			.getString("DBHandler.embedded"); //$NON-NLS-1$

	private final IDBController dbController;
	private IDBConnectionConfig dbConnectionConfig;
	private String host;
	private String catalog;
	private String user;
	private String password;

	/**
	 * DBSettings constructor, called through DI.
	 * 
	 * @param inPreferences
	 * @param inDBController
	 */
	@Inject
	public DBSettings(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE) final IEclipsePreferences inPreferences,
			final IDBController inDBController) {
		dbController = inDBController;
		dbConnectionConfig = inDBController.getConfiguration(inPreferences.get(
				RelationsConstants.KEY_DB_PLUGIN_ID,
				RelationsConstants.DFT_DBCONFIG_PLUGIN_ID));
		host = inPreferences.get(RelationsConstants.KEY_DB_HOST, ""); //$NON-NLS-1$
		catalog = inPreferences.get(RelationsConstants.KEY_DB_CATALOG,
				RelationsConstants.DFT_DB_EMBEDDED);
		user = inPreferences.get(RelationsConstants.KEY_DB_USER_NAME, ""); //$NON-NLS-1$
		password = inPreferences.get(RelationsConstants.KEY_DB_PASSWORD, ""); //$NON-NLS-1$
	}

	/**
	 * @return String the DB host (empty for embedded DB)
	 */
	@Override
	public String getHost() {
		return host;
	}

	@Inject
	@Optional
	void trackHost(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_DB_HOST) final String inHost) {
		host = inHost == null ? "" : inHost; //$NON-NLS-1$
	}

	/**
	 * @return String the DB catalog or schema to be used in the application
	 */
	@Override
	public String getCatalog() {
		return catalog;
	}

	@Inject
	@Optional
	void trackCatalog(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_DB_CATALOG) final String inCatalog) {
		catalog = inCatalog == null ? "" : inCatalog; //$NON-NLS-1$
	}

	/**
	 * @return String the DB user (empty for embedded DB)
	 */
	@Override
	public String getUser() {
		return user;
	}

	@Inject
	@Optional
	void trackUser(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_DB_USER_NAME) final String inUser) {
		user = inUser == null ? "" : inUser; //$NON-NLS-1$
	}

	/**
	 * @return String the DB password (empty for embedded DB)
	 */
	@Override
	public String getPassword() {
		return password;
	}

	@Inject
	@Optional
	void trackPassword(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_DB_PASSWORD) final String inPassword) {
		password = inPassword == null ? "" : inPassword; //$NON-NLS-1$
	}

	/**
	 * @return {@link IDBConnectionConfig} the used DB's connection
	 *         configuration
	 */
	@Override
	public IDBConnectionConfig getDBConnectionConfig() {
		return dbConnectionConfig;
	}

	@Inject
	@Optional
	void trackDBConfiguration(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_DB_PLUGIN_ID) final String inPluginID) {
		if (inPluginID != null) {
			dbConnectionConfig = dbController.getConfiguration(inPluginID);
		}
	}

	/**
	 * Convenience method for StatusLine.
	 * 
	 * @return String e.g. <i>jdbc:mysql://localhost/relations</i>
	 */
	public String getDBName() {
		if (getDBConnectionConfig().isEmbedded()) {
			return EMBEDDED + ": " + getCatalog(); //$NON-NLS-1$
		}
		return CLIENT_URL_PATTERN.format(new Object[] {
				getDBConnectionConfig().getSubprotocol(), getHost(),
				getCatalog() });
	}

}
