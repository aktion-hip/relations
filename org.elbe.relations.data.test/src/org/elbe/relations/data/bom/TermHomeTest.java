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
package org.elbe.relations.data.bom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.elbe.relations.data.test.DataHouseKeeper;
import org.hip.kernel.exc.VException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Luthiger
 */
public class TermHomeTest {
    private static DataHouseKeeper data;

    @BeforeClass
    public static void init() {
        data = DataHouseKeeper.INSTANCE;
    }

    @After
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testNewTerm() throws Exception {
        final long now = System.currentTimeMillis() - 1000;

        final TermHome home = data.getTermHome();
        final EventStoreHome storeHome = data.getEventStoreHome();

        assertEquals(0, home.getCount());
        assertEquals(0, storeHome.getCount());

        final AbstractTerm term = home.newTerm("Title", "Text");
        assertEquals(1, home.getCount());
        assertEquals(1, storeHome.getCount());

        final AbstractTerm retrieved = home.getTerm(term.getID());
        final long created = ((Timestamp) retrieved.get(TermHome.KEY_CREATED)).getTime();
        final long modified = ((Timestamp) retrieved.get(TermHome.KEY_MODIFIED)).getTime();
        assertEquals(created, modified);
        assertTrue(created >= now);

        home.deleteItem(term.getID());
        assertEquals(0, home.getCount());
        assertEquals(2, storeHome.getCount());
    }

    @Test
    public void testGetTerm() throws VException, SQLException, BOMException {
        final String title = "ThisTitle";
        final String text = "This Text";
        final TermHome home = data.getTermHome();

        assertEquals("number 0", 0, home.getCount());

        AbstractTerm term = home.newTerm(title, text);
        final long id = term.getID();

        term = home.newTerm("next", "next");
        assertEquals("number 1", 2, home.getCount());

        term = (AbstractTerm) home.getItem(id);
        assertEquals("title", title, term.getTitle());
        assertEquals("text", text, term.get(TermHome.KEY_TEXT).toString());
    }

}
