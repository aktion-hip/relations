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
package org.elbe.relations.internal.services;

import org.elbe.relations.services.IDBConnectionConfig;

/**
 * Interface for database configuration component.
 *
 * @author Luthiger
 */
public interface IDBController {

	/**
	 * Checks for an embedded database.
	 *
	 * @return boolean <code>true</code> if there's at least on configuration
	 *         for an embedded database.
	 */
	boolean checkEmbedded();

	/**
	 * Checks whether the database configuration with the specified index
	 * configures is an embedded database.
	 *
	 * @param index
	 *            int
	 * @return boolean <code>true</code> if the selected configuration is an
	 *         embedded database.
	 */
	boolean checkEmbedded(final int index);

	/**
	 * Checks whether the DB configuration actually used by the application is
	 * an embedded DB.
	 *
	 * @return boolean <code>true</code> if the DB used configuration (and
	 *         therefore the selected configuration when the wizard comes up) is
	 *         embedded
	 */
	boolean isInitialEmbedded();

	/**
	 * Returns the configuration matching the specified name. If no one found
	 * with this name, return the default configuration, then the configuration
	 * of the first embedded, then the first configuration registered.
	 *
	 * @param configurationName
	 *            {@link IDBConnectionConfig}
	 * @return {@link IDBConnectionConfig}
	 */
	IDBConnectionConfig getConfiguration(final String configurationName);

	/**
	 * Returns the configuration with the specified index in the list.
	 *
	 * @param index
	 *            int
	 * @return {@link IDBConnectionConfig}
	 */
	IDBConnectionConfig getConfiguration(final int index);

	/**
	 * @return String[] array of DB names (labels) of the registered DB
	 *         configurations
	 */
	String[] getDBNames();

	/**
	 * @return int index of the selected DB configuration in the list
	 */
	int getSelectedIndex();

}
