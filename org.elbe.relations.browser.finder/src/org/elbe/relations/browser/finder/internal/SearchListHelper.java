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
package org.elbe.relations.browser.finder.internal;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.elbe.relations.browser.finder.internal.FinderPane.GalleryItemAdapter;

/**
 * Wrapper for <code>List<String></code>s providing a special search feature to
 * place the cursor on the element that matches a key stroke.
 * 
 * @author Luthiger Created on 17.12.2009
 */
public class SearchListHelper {
	private static final long KEY_PRESS_DELAY = 500;
	private static final int NO_MOVE = -1;

	private final List<String> original;
	private final List<String> lower;
	private long time;
	private String search;

	SearchListHelper() {
		original = new Vector<String>();
		lower = new Vector<String>();
		time = 0;
		search = ""; //$NON-NLS-1$
	}

	/**
	 * Add method to fill the list.
	 * 
	 * @param inTitle
	 *            String the text to add.
	 */
	public void add(final String inTitle) {
		original.add(inTitle);
		lower.add(inTitle.toLowerCase());
	}

	/**
	 * Method to look up the specified element's position in the list.
	 * 
	 * @param inTitle
	 *            String
	 * @return int Returns the index in this list of the first occurrence of the
	 *         specified element, or -1 if this list does not contain this
	 *         element.
	 */
	public int indexOf(final String inTitle) {
		return original.indexOf(inTitle);
	}

	/**
	 * Search the list for best matches of the specified key pressed.
	 * 
	 * @param inSearch
	 *            char the key pressed
	 * @param inSelected
	 *            {@link GalleryItemAdapter} the item in the gallery actually
	 *            selected. If the selected item starts with the key pressed,
	 *            the next item is checked for a match.
	 * @return int index of the item matching the search key or -1, if no item
	 *         matches.
	 */
	public int search(final char inSearch, final GalleryItemAdapter inSelected,
			final long inTime) {
		final String lSearch = new String(new char[] { inSearch })
				.toLowerCase();

		// we combine multiple key pressed in a specified time span
		if (inTime - time < KEY_PRESS_DELAY) {
			search += lSearch;
		} else {
			search = lSearch;
		}
		time = inTime;

		// we first have to check the selected item
		final String lSelected = inSelected == null ? "" : inSelected.getText(); //$NON-NLS-1$
		int outIndex = -1;
		if (lSelected.toLowerCase().startsWith(search)) {
			outIndex = original.indexOf(lSelected) + 1;
			if (outIndex < original.size()
					&& lower.get(outIndex).startsWith(search)) {
				return outIndex;
			}
		}

		// now search to collection for matching entries
		outIndex = Collections.binarySearch(lower, search);
		if (outIndex > 0) {
			return outIndex;
		}
		outIndex = -outIndex - 1;
		if (outIndex >= lower.size()) {
			return NO_MOVE;
		}
		final String lElement = lower.get(outIndex);
		if (lElement.startsWith(search)) {
			return outIndex;
		}
		return NO_MOVE;
	}

}
