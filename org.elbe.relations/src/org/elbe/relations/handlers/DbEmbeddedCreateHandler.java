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

import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.Constants;
import org.elbe.relations.data.db.IDBObjectCreator;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.internal.data.IDBSettings;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;
import org.hip.kernel.bom.impl.DefaultStatement;

/**
 * Handler responsible for creating the tables of an embedded (i.e. Derby)
 * database.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class DbEmbeddedCreateHandler {

	@Inject
	private Logger log;

	@Execute
	public void execute(final IDBSettings inDBSettings,
			final IEclipseContext inContext) {
		createEmbedded(inDBSettings);
		prepareIndex(inDBSettings, inContext);
	}

	private void createEmbedded(final IDBSettings inDBSettings) {
		try {
			final IDBObjectCreator lCreator = inDBSettings.getDBConnectionConfig()
					.getCreator();
			final DefaultStatement lStatement = new DefaultStatement();
			for (final String lSQLCreate : lCreator
					.getCreateStatemens(Constants.XML_CREATE_OBJECTS)) {
				lStatement.execute(lSQLCreate);
			}
		}
		catch (final Exception exc) {
			log.error(exc, exc.getMessage());
		}
	}

	private void prepareIndex(final IDBSettings inDBSettings,
			final IEclipseContext inContext) {
		try {
			final RelationsIndexer lIndexer = RelationsIndexerWithLanguage
					.createRelationsIndexer(inContext);
			if (!lIndexer.isIndexAvailable()) {
				lIndexer.initializeIndex();
			}
		}
		catch (final IOException exc) {
			log.error(exc, exc.getMessage());
		}
	}

}
