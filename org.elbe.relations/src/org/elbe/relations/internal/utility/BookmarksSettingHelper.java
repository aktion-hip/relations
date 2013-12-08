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
package org.elbe.relations.internal.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/**
 * Helper class for the bookmark view (part) to persist the bookmark values per
 * database in the part's <code>persisted state</code> area.
 * 
 * @author Luthiger
 */
public class BookmarksSettingHelper {
	public static final String SEP = ","; //$NON-NLS-1$

	private final MPart bookmarkPart;

	public BookmarksSettingHelper(final MPart inBookmarkPart) {
		bookmarkPart = inBookmarkPart;
	}

	/**
	 * Retrieves the persisted bookmarks.
	 * 
	 * @param inDBName
	 *            the key to retrieve the persisted bookmarks
	 * @return Collection&lt;String>
	 */
	public Collection<String> getBookmarks(final String inDBName) {
		final String lBookmarks = bookmarkPart.getPersistedState()
				.get(inDBName);
		if (lBookmarks == null || lBookmarks.isEmpty()) {
			return new ArrayList<String>();
		}
		final List<String> out = Arrays.asList(lBookmarks.split(SEP));
		Collections.sort(out, new Comparator<String>() {
			@Override
			public int compare(final String inO1, final String inO2) {
				return inO1.compareToIgnoreCase(inO2);
			}
		});
		return out;
	}

	/**
	 * Persists the bookmarks.
	 * 
	 * @param inDbName
	 *            String the key to persist the bookmarks
	 * @param inValues
	 *            String the comma separated list (unique ids) of bookmarks
	 */
	public void storeBookmarks(final String inDbName, final String inValues) {
		bookmarkPart.getPersistedState().put(inDbName, inValues);
	}

}
