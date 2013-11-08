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
package org.elbe.relations.internal.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.db.IDBObjectCreator;
import org.elbe.relations.internal.services.IDBController;
import org.elbe.relations.services.IDBConnectionConfig;

/**
 * The OSGi service client to handle registrations of the
 * <code>org.elbe.relations.services.IDBConnectionConfig</code> service.
 * 
 * @author Luthiger
 * @see org.elbe.relations.services.IDBController
 */
@SuppressWarnings("restriction")
public class DBController implements IDBController {

	private final List<ConfigurationDecorator> dbConfigurations = new ArrayList<ConfigurationDecorator>();
	private boolean hasDftRegistration = false;
	private String dbSelection;

	/**
	 * OSGi bind
	 * 
	 * @param inDBConfig
	 *            {@link IDBConnectionConfig} bind the service implementation
	 */
	public void register(final IDBConnectionConfig inDBConfig) {
		final ConfigurationDecorator lDecorated = new ConfigurationDecorator(
				inDBConfig);
		// we want the list sorted, but the default configuration on top
		if (lDecorated.isDefault()) {
			dbConfigurations.add(0, lDecorated);
			hasDftRegistration = true;
		} else {
			if (hasDftRegistration) {
				final ConfigurationDecorator lDftConfiguration = dbConfigurations
						.remove(0);
				dbConfigurations.add(new ConfigurationDecorator(inDBConfig));
				Collections.sort(dbConfigurations);
				dbConfigurations.add(0, lDftConfiguration);
			} else {
				dbConfigurations.add(new ConfigurationDecorator(inDBConfig));
				Collections.sort(dbConfigurations);
			}
		}
	}

	/**
	 * OSGi unbind
	 * 
	 * @param inDBConfig
	 *            {@link IDBConnectionConfig} unbind the service implementation
	 */
	public void unregister(final IDBConnectionConfig inDBConfig) {
		dbConfigurations.remove(new ConfigurationDecorator(inDBConfig));
	}

	// ---

	@Inject
	@Optional
	void trackDBSelection(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_DB_PLUGIN_ID) final String inDBSelection) {
		dbSelection = inDBSelection;
	}

	/**
	 * Checks for an embedded database.
	 * 
	 * @return boolean <code>true</code> if there's at least on configuration
	 *         for an embedded database.
	 */
	@Override
	public boolean checkEmbedded() {
		for (final IDBConnectionConfig lService : dbConfigurations) {
			if (lService.isEmbedded()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the database configuration with the specified index
	 * configures is an embedded database.
	 * 
	 * @param inIndex
	 *            int
	 * @return boolean <code>true</code> if the selected configuration is an
	 *         embedded database.
	 */
	@Override
	public boolean checkEmbedded(final int inIndex) {
		return getConfiguration(inIndex).isEmbedded();
	}

	@Override
	public boolean isInitialEmbedded() {
		return dbConfigurations.get(getSelectedIndex()).isEmbedded();
	}

	/**
	 * Returns the configuration matching the specified name. If no one found
	 * with this name, return the default configuration, then the configuration
	 * of the first embedded, then the first configuration registered.
	 * 
	 * @param inConfigurationName
	 *            {@link IDBConnectionConfig}
	 * @return {@link IDBConnectionConfig}
	 */
	@Override
	public IDBConnectionConfig getConfiguration(final String inConfigurationName) {
		// first try the specified name
		for (final IDBConnectionConfig lService : dbConfigurations) {
			if (lService.getName().equals(inConfigurationName)) {
				return lService;
			}
		}
		// the try the default name
		for (final IDBConnectionConfig lService : dbConfigurations) {
			if (lService.getName().equals(
					RelationsConstants.DFT_DBCONFIG_PLUGIN_ID)) {
				return lService;
			}
		}
		// then try the first embedded
		for (final IDBConnectionConfig lService : dbConfigurations) {
			if (lService.isEmbedded()) {
				return lService;
			}
		}
		// the simply return the first
		return dbConfigurations.iterator().next();
	}

	/**
	 * Returns the configuration with the specified index in the list.
	 * 
	 * @param inIndex
	 *            int
	 * @return {@link IDBConnectionConfig}
	 */
	@Override
	public IDBConnectionConfig getConfiguration(final int inIndex) {
		return dbConfigurations.get(inIndex);
	}

	/**
	 * @return String[] array of DB names (labels) of the registered DB
	 *         configurations
	 */
	@Override
	public String[] getDBNames() {
		final String[] outDBNames = new String[dbConfigurations.size()];
		int i = 0;
		for (final ConfigurationDecorator lConfiguration : dbConfigurations) {
			outDBNames[i++] = lConfiguration.getLabel();
		}
		return outDBNames;
	}

	/**
	 * @return int index of the selected DB configuration in the list
	 */
	@Override
	public int getSelectedIndex() {
		final String lSelected = (dbSelection == null || dbSelection.isEmpty()) ? RelationsConstants.DFT_DBCONFIG_PLUGIN_ID
				: dbSelection;

		int i = 0;
		for (final ConfigurationDecorator lConfiguration : dbConfigurations) {
			if (lSelected.equals(lConfiguration.getName())) {
				return i;
			}
			i++;
		}
		return 0;
	}

	// --- inner classes ---

	/**
	 * Wrapper class decorating the passed DB connection configuration.
	 * 
	 * @author Luthiger
	 */
	private static class ConfigurationDecorator implements IDBConnectionConfig,
			Comparable<ConfigurationDecorator> {
		private static final String DFT_MARKER = " *"; //$NON-NLS-1$

		private final IDBConnectionConfig configuration;
		private final boolean isDefaultDB;
		private String label;

		private ConfigurationDecorator(final IDBConnectionConfig inConfiguration) {
			configuration = inConfiguration;
			final String lName = configuration.getName();
			isDefaultDB = RelationsConstants.DFT_DBCONFIG_PLUGIN_ID
					.equals(lName);
			final String lDriver = configuration.getJDBCDriverClass();
			label = lName.startsWith(lDriver) ? lName.substring(lDriver
					.length() + 1) : lName;
			if (isDefaultDB) {
				label += DFT_MARKER;
			}
		}

		boolean isDefault() {
			return isDefaultDB;
		}

		String getLabel() {
			return label;
		}

		@Override
		public String getName() {
			return configuration.getName();
		}

		@Override
		public String getJDBCDriverClass() {
			return configuration.getJDBCDriverClass();
		}

		@Override
		public String getSubprotocol() {
			return configuration.getSubprotocol();
		}

		@Override
		public boolean isEmbedded() {
			return configuration.isEmbedded();
		}

		@Override
		public IDBObjectCreator getCreator() {
			return configuration.getCreator();
		}

		@Override
		public boolean canSetIdentityField() {
			return configuration.canSetIdentityField();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((getName() == null) ? 0 : getName().hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ConfigurationDecorator other = (ConfigurationDecorator) obj;
			if (configuration.getName() == null) {
				if (other.getName() != null)
					return false;
			} else if (!getName().equals(other.getName()))
				return false;
			return true;
		}

		@Override
		public int compareTo(final ConfigurationDecorator inOther) {
			return getName().compareToIgnoreCase(inOther.getName());
		}

		@Override
		public String toString() {
			return getLabel();
		}
	}

}
