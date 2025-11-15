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
package org.elbe.relations.browser.finder.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.elbe.relations.browser.finder.internal.FinderPane.GalleryItemAdapter;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.models.ItemAdapter;
import org.hip.kernel.exc.VException;

/** Wrapper for <code>List<String></code>s providing a special search feature to place the cursor on the element that
 * matches a key stroke.
 *
 * @author Luthiger */
public class SearchListHelper {
    private static final long KEY_PRESS_DELAY = 500;
    private static final int NO_MOVE = -1;

    private final List<String> original;
    private final List<String> lower;
    private final List<UniqueID> ids;
    private long time;
    private String search;

    SearchListHelper() {
        this.original = new ArrayList<>();
        this.lower = new ArrayList<>();
        this.ids = new ArrayList<>();
        this.time = 0;
        this.search = ""; //$NON-NLS-1$
    }

    /** Add method to fill the list.
     *
     * @param item ItemAdapter the item to add.
     * @throws VException */
    public void add(final ItemAdapter item) throws VException {
        final String text = item.getTitle();
        this.original.add(text);
        this.lower.add(text.toLowerCase());
        this.ids.add(item.getUniqueID());
    }

    /** Method to look up the specified element's position in the list.
     *
     * @param uniqueID UniqueID
     * @return int Returns the index in this list of the first occurrence of the specified element, or -1 if this list
     *         does not contain this element. */
    public int indexOf(final UniqueID uniqueID) {
        return this.ids.indexOf(uniqueID);
    }

    /** Search the list for best matches of the specified key pressed.
     *
     * @param search char the key pressed
     * @param selected {@link GalleryItemAdapter} the item in the gallery actually selected. If the selected item starts
     *            with the key pressed, the next item is checked for a match.
     * @return int index of the item matching the search key or -1, if no item matches. */
    public int search(final char search, final GalleryItemAdapter selected, final long time) {
        final String lSearch = new String(new char[] { search }).toLowerCase();

        // we combine multiple key pressed in a specified time span
        if (time - this.time < KEY_PRESS_DELAY) {
            this.search += lSearch;
        } else {
            this.search = lSearch;
        }
        this.time = time;

        // we first have to check the selected item
        final String lSelected = selected == null ? "" : selected.getText(); //$NON-NLS-1$
        int outIndex = -1;
        if (lSelected.toLowerCase().startsWith(this.search)) {
            outIndex = this.original.indexOf(lSelected) + 1;
            if (outIndex < this.original.size() && this.lower.get(outIndex).startsWith(this.search)) {
                return outIndex;
            }
        }

        // now search to collection for matching entries
        outIndex = Collections.binarySearch(this.lower, this.search);
        if (outIndex > 0) {
            return outIndex;
        }
        outIndex = -outIndex - 1;
        if (outIndex >= this.lower.size()) {
            return NO_MOVE;
        }
        final String lElement = this.lower.get(outIndex);
        if (lElement.startsWith(this.search)) {
            return outIndex;
        }
        return NO_MOVE;
    }

    /**
     * @return <code>true</code> if the helper class is empty
     */
    protected boolean isEmpty() {
        return this.original.isEmpty();
    }

}
