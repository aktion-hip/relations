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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;

/**
 * Helper to change to a different DB catalog (schema).
 * 
 * @author Luthiger
 */
public class ChangeDB extends AbstractChangeDB {

	@Inject
	private IEclipseContext context;

	@Override
	public void execute() {
		super.execute();
		initIndex();
	}

	@SuppressWarnings("restriction")
	private void initIndex() {
		final RelationsIndexer lIndexer = RelationsIndexerWithLanguage
		        .createRelationsIndexer(context);
		if (!lIndexer.isIndexAvailable()) {
			try {
				lIndexer.initializeIndex();
			}
			catch (final IOException exc) {
				getLog().error(exc, exc.getMessage());
			}
		}
	}

}
