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

import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.handlers.DbEmbeddedCreateHandler;
import org.elbe.relations.internal.utility.DBPreconditionException;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;

/**
 * Helper class to create a catalog in an embedded database.
 *
 * @author Luthiger
 */
public class CreateEmbeddedDB extends AbstractCreateDB {

	private EmbeddedCatalogHelper catalogHelper;

	@Override
	public void checkPreconditions() throws DBPreconditionException {
		if (this.catalogHelper.validate(getTempSettings().getCatalog()) != Status.OK_STATUS) {
			throw new DBPreconditionException(
					RelationsMessages
					.getString("CreateEmbeddedDB.exception.exists")); //$NON-NLS-1$
		}
	}

	/**
	 * @param inCatalogHelper
	 */
	public void setHelper(final EmbeddedCatalogHelper inCatalogHelper) {
		this.catalogHelper = inCatalogHelper;
	}

	@Override
	public void execute() {
		setTempDBSettings();

		final DbEmbeddedCreateHandler dbCreate = ContextInjectionFactory.make(
				DbEmbeddedCreateHandler.class, getContext());
		dbCreate.execute(getTempSettings(), getContext());

		doDBChange();
		createIndex();
	}

}
