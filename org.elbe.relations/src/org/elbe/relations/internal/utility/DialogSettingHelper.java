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
package org.elbe.relations.internal.utility;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/** Helper class to persist the dialog input values in the part's <code>persisted state</code> area for that it can be
 * used in future sessions.
 *
 * @author Luthiger */
public class DialogSettingHelper {
    public static final String SEP = "|"; //$NON-NLS-1$

    private final MPart part;
    private final String key;

    /** DialogSettingHelper constructor.
     *
     * @param part {@link MPart} the part the dialog widget is element of
     * @param key String the key to persist the input */
    public DialogSettingHelper(final MPart part, final String key) {
        this.part = part;
        this.key = key;
    }

    /** @return String[] the recent values of the specified widget */
    public String[] getRecentValues() {
        final String persisted = this.part.getPersistedState().get(this.key);
        return persisted == null || persisted.isEmpty() ? new String[0]
                : persisted.split("\\" + SEP); //$NON-NLS-1$
    }

    /** Persist the widget's specified values.
     *
     * @param values String[] */
    public void saveToHistory(final String[] values) {
        final StringBuilder lHistory = new StringBuilder();
        boolean isFirst = true;
        for (final String lItem : values) {
            if (!isFirst) {
                lHistory.append(SEP);
            }
            isFirst = false;
            lHistory.append(lItem);
        }

        this.part.getPersistedState().put(this.key, new String(lHistory));
    }

}
