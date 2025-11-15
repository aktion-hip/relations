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

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.search.RetrievedItem;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.search.RelationsSearcher;
import org.elbe.relations.search.RetrievedItemWithIcon;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/** Controller class for the view to search items.
 *
 * @author Luthiger */
@Creatable
@Singleton
public class SearchController {
    private WritableList<RetrievedItem> searchResults = null;
    private RelationsSearcher searcher;

    @Inject
    private IEclipseContext context;

    @Inject
    private DBSettings dbSettings;

    @Inject
    private Logger log;

    public SearchController() {
        this.searchResults = new WritableList<>(Collections.emptyList(), RetrievedItemWithIcon.class);
    }

    /** Lazy loading: we don't have a context at construction time. */
    private RelationsSearcher getSearcher() {
        if (this.searcher == null) {
            this.searcher = RelationsSearcher.createRelationsSearcher(this.context,
                    this.dbSettings);
        }
        return this.searcher;
    }

    /** Executes a search.
     *
     * @param searchQuery String the search term
     * @return Collection&lt;RetrievedItemWithIcon> */
    public Collection<RetrievedItem> search(final String searchQuery) {
        try {
            this.searchResults = new WritableList<>(getSearcher().search(searchQuery), RetrievedItemWithIcon.class);
        } catch (final Exception exc) {
            this.searchResults = new WritableList<>(Collections.emptyList(), RetrievedItemWithIcon.class);
            this.log.error(exc, exc.getMessage());
        }
        return this.searchResults;
    }

    /** @return Collection&lt;RetrievedItemWithIcon> an empty list */
    public Collection<RetrievedItem> emptyList() {
        this.searchResults.clear();
        return this.searchResults;
    }

    /** Reset searcher after DB changed. */
    public void reset() {
        this.searcher = null;
    }

}
