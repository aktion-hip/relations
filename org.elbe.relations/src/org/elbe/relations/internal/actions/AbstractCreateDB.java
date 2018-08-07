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

import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;

/**
 * Base class for helper classes that create a new database (catalog).
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractCreateDB extends AbstractChangeDB {

	@Inject
	private IEclipseContext context;

	protected void createIndex() {
		final RelationsIndexer indexer = RelationsIndexerWithLanguage
				.createRelationsIndexer(this.context);
		if (!indexer.isIndexAvailable()) {
			try {
				indexer.initializeIndex();

				final IndexerAction action = ContextInjectionFactory.make(
						IndexerAction.class, this.context);
				action.setSilent(true);
				action.run();
			}
			catch (final IOException exc) {
				getLog().error(exc, exc.getMessage());
			}
		}
	}

	protected IEclipseContext getContext() {
		return this.context;
	}

}
