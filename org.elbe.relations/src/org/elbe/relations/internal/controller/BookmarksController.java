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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.search.RetrievedItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.utility.BookmarksSettingHelper;
import org.elbe.relations.search.RetrievedItemWithIcon;

/**
 * Controller class for the command and view to bookmark items.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
@Creatable
@Singleton
public class BookmarksController {
	private volatile WritableList bookmarksList;
	private BookmarksSettingHelper settings;
	private final Collection<RetrievedItemWithIcon> pending = new ArrayList<RetrievedItemWithIcon>();
	private String oldDBName = "";

	@Inject
	private IDataService dataService;

	@Inject
	private DBSettings dbSettings;

	@Inject
	private Logger log;

	/**
	 * Initialize the controller.
	 * 
	 * @param inPart
	 *            {@link MPart} the bookmark view's part model
	 */
	public void initialize(final MPart inPart) {
		settings = new BookmarksSettingHelper(inPart);
		retrieveBookmarks(settings);
	}

	/**
	 * Lazy initialization.
	 */
	private WritableList getBookmarkList() {
		if (bookmarksList == null) {
			bookmarksList = new WritableList(
					new ArrayList<RetrievedItemWithIcon>(),
					RetrievedItemWithIcon.class);
		}
		return bookmarksList;
	}

	/**
	 * Returns the current set of bookmarks.
	 * 
	 * @return {@link WritableList}
	 */
	public WritableList getBookmarks() {
		return getBookmarkList();
	}

	private void retrieveBookmarks(final BookmarksSettingHelper inSettings) {
		oldDBName = getDBName();
		final WritableList lBookmarks = getBookmarkList();
		lBookmarks.clear();
		try {
			for (final String lBookmark : inSettings.getBookmarks(oldDBName)) {
				final UniqueID lID = new UniqueID(lBookmark);
				final IItem lItem = dataService.retrieveItem(lID);
				final RetrievedItemWithIcon lRetrieved = new RetrievedItemWithIcon(
						lID, lItem.getTitle());
				lBookmarks.add(lRetrieved);
				pending.remove(lRetrieved);
			}
			for (final RetrievedItemWithIcon lItem : pending) {
				lBookmarks.add(lItem);
			}
		}
		catch (final Exception exc) {
			lBookmarks.clear();
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Persists the bookmarks.
	 */
	public void storeBookmarks() {
		final StringBuilder lPersist = new StringBuilder();
		boolean isFirst = true;
		for (final Iterator<?> lItems = getBookmarkList().iterator(); lItems
				.hasNext();) {
			final RetrievedItem lItem = (RetrievedItem) lItems.next();
			if (!isFirst) {
				lPersist.append(BookmarksSettingHelper.SEP);
			}
			isFirst = false;
			lPersist.append(UniqueID.getStringOf(lItem.getItemType(),
					lItem.getID()));
		}
		if (settings != null) {
			settings.storeBookmarks(oldDBName.isEmpty() ? getDBName()
					: oldDBName, new String(lPersist));
		}
	}

	@Inject
	@Optional
	void removeItem(
			@EventTopic(RelationsConstants.TOPIC_DB_CHANGED_DELETED) final UniqueID inDeleted) {
		if (inDeleted != null) {
			getBookmarkList().remove(new RetrievedItemWithIcon(inDeleted, ""));
		}
	}

	@Inject
	@Optional
	void reinitialize(
			@EventTopic(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String inEvent) {
		storeBookmarks();
		if (settings != null) {
			retrieveBookmarks(settings);
		}
	}

	/**
	 * Adds the specified item to the current set of bookmarks.
	 * 
	 * @param inItem
	 *            {@link RetrievedItemWithIcon}
	 */
	public void addItem(final RetrievedItemWithIcon inItem) {
		if (inItem == null) {
			return;
		}
		if (getBookmarkList().contains(inItem)) {
			return;
		}
		if (settings == null) {
			pending.add(inItem);
		} else {
			getBookmarkList().add(0, inItem);
		}
	}

	/**
	 * Removes the specified item from the current set of bookmarks.
	 * 
	 * @param inItem
	 *            Object (instance of RetrievedItemWithIcon)
	 */
	public void removeItem(final Object inItem) {
		if (inItem == null) {
			return;
		}
		getBookmarkList().remove(inItem);
	}

	private String getDBName() {
		return String.format("%s/%s", dbSettings.getHost(),
				dbSettings.getCatalog());
	}

}
