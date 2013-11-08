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

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.actions.DBDeleteAction;
import org.elbe.relations.internal.data.DBSettings;

/**
 * Handler class to delete an embedded DB.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class DbDeleteHandler {

	@Inject
	private DBSettings dbSettings;

	@Inject
	private Logger log;

	@Execute
	void deleteDB(final IEclipseContext inContext) {
		final DBDeleteAction lAction = new DBDeleteAction(dbSettings,
				inContext, log);
		lAction.execute();
	}

	@CanExecute
	boolean checkEmbedded() {
		if (dbSettings.getDBConnectionConfig().isEmbedded()) {
			return !RelationsConstants.DFT_DB_EMBEDDED.equals(dbSettings
					.getCatalog());
		}
		return false;
	}

}
