/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.utility.BookmarksSettingHelper;
import org.elbe.relations.search.RetrievedItemWithIcon;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Controller class for the command and view to bookmark items.
 *
 * @author Luthiger
 */
@Creatable
@Singleton
public class BookmarksController {
    private volatile WritableList<RetrievedItemWithIcon> bookmarksList;
    private BookmarksSettingHelper settings;
    private final Collection<RetrievedItemWithIcon> pending = new ArrayList<>();
    private String oldDBName = ""; //$NON-NLS-1$

    @Inject
    private IDataService dataService;

    @Inject
    private DBSettings dbSettings;

    @Inject
    private Logger log;

    /** Initialize the controller.
     *
     * @param part {@link MPart} the bookmark view's part model */
    public void initialize(final MPart part) {
        this.settings = new BookmarksSettingHelper(part);
        retrieveBookmarks(this.settings);
    }

    /**
     * Lazy initialization.
     */
    private WritableList<RetrievedItemWithIcon> getBookmarkList() {
        if (this.bookmarksList == null) {
            this.bookmarksList = new WritableList<>(new ArrayList<>(), RetrievedItemWithIcon.class);
        }
        return this.bookmarksList;
    }

    /**
     * Returns the current set of bookmarks.
     *
     * @return {@link WritableList}
     */
    public WritableList<RetrievedItemWithIcon> getBookmarks() {
        return getBookmarkList();
    }

    private void retrieveBookmarks(final BookmarksSettingHelper settings) {
        this.oldDBName = getDBName();
        final WritableList<RetrievedItemWithIcon> bookmarks = getBookmarkList();
        bookmarks.clear();
        try {
            for (final String bookmark : settings.getBookmarks(this.oldDBName)) {
                final UniqueID id = new UniqueID(bookmark);
                final IItem item = this.dataService.retrieveItem(id);
                final RetrievedItemWithIcon retrieved = new RetrievedItemWithIcon(id, item.getTitle());
                bookmarks.add(retrieved);
                this.pending.remove(retrieved);
            }
            this.pending.forEach(i -> bookmarks.add(i));
        }
        catch (final Exception exc) {
            bookmarks.clear();
            this.log.error(exc, exc.getMessage());
        }
    }

    /**
     * Persists the bookmarks.
     */
    public void storeBookmarks() {
        if (this.settings != null) {
            final var persist = getBookmarkList().stream().map(i -> UniqueID.getStringOf(i.getItemType(), i.getID()))
                    .collect(Collectors.joining(BookmarksSettingHelper.SEP));
            this.settings.storeBookmarks(this.oldDBName.isEmpty() ? getDBName()
                    : this.oldDBName, persist);
        }
    }

    @Inject
    @Optional
    void removeItem(
            @UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_DELETED) final UniqueID deleted) {
        if (deleted != null) {
            getBookmarkList().remove(new RetrievedItemWithIcon(deleted, "")); //$NON-NLS-1$
        }
    }

    @Inject
    @Optional
    void reinitialize(
            @UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String event) {
        storeBookmarks();
        if (this.settings != null) {
            retrieveBookmarks(this.settings);
        }
    }

    /** Adds the specified item to the current set of bookmarks.
     *
     * @param item {@link RetrievedItemWithIcon} */
    public void addItem(final RetrievedItemWithIcon item) {
        if (item == null) {
            return;
        }
        if (getBookmarkList().contains(item)) {
            return;
        }
        if (this.settings == null) {
            this.pending.add(item);
        } else {
            getBookmarkList().add(0, item);
        }
    }

    /** Removes the specified item from the current set of bookmarks.
     *
     * @param item Object (instance of RetrievedItemWithIcon) */
    public void removeItem(final Object item) {
        if (item == null) {
            return;
        }
        getBookmarkList().remove(item);
    }

    private String getDBName() {
        return String.format("%s/%s", this.dbSettings.getHost(), //$NON-NLS-1$
                this.dbSettings.getCatalog());
    }

}
