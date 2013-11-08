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

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.search.RelationsSearcher;
import org.elbe.relations.search.RetrievedItemWithIcon;

/**
 * Controller class for the view to search items.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
@Creatable
@Singleton
public class SearchController {
	private volatile WritableList searchResults = null;
	private RelationsSearcher searcher;

	@Inject
	private IEclipseContext context;

	@Inject
	private DBSettings dbSettings;

	@Inject
	private Logger log;

	public SearchController() {
		searchResults = new WritableList(Collections.emptyList(),
				RetrievedItemWithIcon.class);
	}

	/**
	 * Lazy loading: we don't have a context at construction time.
	 */
	private RelationsSearcher getSearcher() {
		if (searcher == null) {
			searcher = RelationsSearcher.createRelationsSearcher(context,
					dbSettings);
		}
		return searcher;
	}

	/**
	 * Executes a search.
	 * 
	 * @param inSearchQuery
	 *            String the search term
	 * @return Collection&lt;RetrievedItemWithIcon>
	 */
	@SuppressWarnings("unchecked")
	public Collection<RetrievedItemWithIcon> search(final String inSearchQuery) {
		try {
			searchResults = new WritableList(getSearcher()
					.search(inSearchQuery), RetrievedItemWithIcon.class);
		}
		catch (final Exception exc) {
			searchResults = new WritableList(Collections.emptyList(),
					RetrievedItemWithIcon.class);
			log.error(exc, exc.getMessage());
		}
		return searchResults;
	}

	/**
	 * Reset searcher after DB changed.
	 */
	public void reset() {
		searcher = null;
	}

}
