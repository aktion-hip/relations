/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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
package org.elbe.relations.internal.utility;

import java.sql.SQLException;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.data.IDBSettings;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.hip.kernel.exc.VException;

/**
 * This class tests whether the selected database tables have the structure
 * needed for the application.
 *
 * @author Luthiger
 */
@Creatable
public class DBStructureChecker {

	@Inject
	private IEclipseContext context;

	@Inject
	private DBSettings dbSettings;

	/**
	 * Performs the check for the database structure.
	 *
	 * @param tempSettings
	 *            {@link IDBSettings} temporary DB settings to use for the test
	 * @return boolean <code>true</code> if the database structure fulfills the
	 *         requirements.
	 * @throws SQLException
	 * @throws VException
	 */
	public boolean hasExpectedStructure(final IDBSettings tempSettings)
			throws SQLException, VException {
		final DataSourceRegistry dbAccess = (DataSourceRegistry) this.context
				.get(RelationsConstants.DB_ACCESS_HANDLER);

		dbAccess.setActiveConfiguration(
				ActionHelper.createDBConfiguration(tempSettings));
		try {
			if (BOMHelper.getPersonHome().checkStructure(null)
					&& BOMHelper.getTextHome().checkStructure(null)
					&& BOMHelper.getTermHome().checkStructure(null)
			        && BOMHelper.getRelationHome().checkStructure(null)) {
				// && BOMHelper.getEventStoreHome().checkStructure(null)
				return true;
			}
		} finally {
			dbAccess.setActiveConfiguration(
					ActionHelper
					.createDBConfiguration(this.dbSettings));
		}
		return false;
	}

}
