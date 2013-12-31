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
package org.elbe.relations.internal.actions;

import java.sql.SQLException;
import java.util.Collection;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.handlers.DbEmbeddedCreateHandler;
import org.elbe.relations.internal.utility.DBPreconditionException;
import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.bom.impl.DefaultStatement;
import org.hip.kernel.bom.impl.DomainObjectHomeImpl;

/**
 * Helper class to create a catalog in an external database.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class CreateExternalDB extends AbstractCreateDB {
	private boolean withTableCreation = true;

	@Override
	public void checkPreconditions() throws DBPreconditionException {
		setTempDBSettings();

		try {
			// can we connect?
			final QueryStatement lStatement = new DefaultStatement();
			final Collection<String> lTables = lStatement.showTables();
			// we can connect

			// is catalog empty?
			if (lTables.size() == 0) {
				return;
			}
			// catalog is empty and we return positive

			// do the tables we need exist and are they empty?
			final int lTableCheck = tryHome(BOMHelper.getTermHome())
			        + tryHome(BOMHelper.getTextHome())
			        + tryHome(BOMHelper.getPersonHome())
			        + tryHome(BOMHelper.getRelationHome());

			if (lTableCheck > 0) {
				throw new DBPreconditionException(
				        RelationsMessages
				                .getString("CreateExternalDB.precondition.non.empty")); //$NON-NLS-1$
			}
			if (lTableCheck < 0 && lTableCheck > -4) {
				throw new DBPreconditionException(
				        RelationsMessages
				                .getString("CreateExternalDB.precondition.some.tables")); //$NON-NLS-1$
			}
			if (lTableCheck == 0) {
				// the tables we need are there but they are all empty
				withTableCreation = false;
				return;
			}
		}
		catch (final SQLException exc) {
			getLog().error(exc, exc.getMessage());
		}
		catch (final BOMException exc) {
			getLog().error(exc, exc.getMessage());
		}
		finally {
			setOrigDBSettings();
		}
	}

	private int tryHome(final DomainObjectHomeImpl inHome) throws BOMException {
		try {
			return inHome.getCount();
		}
		catch (final SQLException exc) {
			// We got a SQL exception because the speciefied table does not
			// exist. We signal this by returning -1.
			return -1;
		}
	}

	@Override
	public void execute() {
		setTempDBSettings();
		if (withTableCreation) {
			final DbEmbeddedCreateHandler lDBCreate = ContextInjectionFactory
			        .make(DbEmbeddedCreateHandler.class, getContext());
			lDBCreate.execute(getTempSettings(), getContext());
		}
		createIndex();
		doDBChange();
	}

}
