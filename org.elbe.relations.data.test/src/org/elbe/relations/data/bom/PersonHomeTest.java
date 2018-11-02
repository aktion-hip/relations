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

import java.sql.Timestamp;

import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Luthiger
 */
public class PersonHomeTest {
    private static DataHouseKeeper data;

    @BeforeClass
    public static void init() {
        data = DataHouseKeeper.INSTANCE;
    }

    @Before
    public void setUp() throws Exception {
        // data.setUp();
    }

    @After
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testNewPerson() throws Exception {
        final long now = System.currentTimeMillis() - 1000;

        final PersonHome home = data.getPersonHome();
        final EventStoreHome storeHome = data.getEventStoreHome();

        assertEquals(0, home.getCount());
        assertEquals(0, storeHome.getCount());

        final AbstractPerson person = home.newPerson("Pan", "Peter", "", "", "");
        assertEquals(1, home.getCount());
        assertEquals(1, storeHome.getCount());

        final AbstractPerson retrieved = home.getPerson(person.getID());
        final long created = ((Timestamp) retrieved.get(PersonHome.KEY_CREATED)).getTime();
        final long modified = ((Timestamp) retrieved.get(PersonHome.KEY_MODIFIED)).getTime();
        assertEquals("Created 1", created, modified);
        assertTrue("Created 2", created >= now);

        home.deleteItem(person.getID());
        assertEquals(0, home.getCount());
        assertEquals(2, storeHome.getCount());
    }

    @Test
    public void testGetPerson() throws Exception {
        final String name = "Pan";
        final String firstName = "Peter";
        final PersonHome home = data.getPersonHome();
        final EventStoreHome storeHome = data.getEventStoreHome();

        assertEquals(0, home.getCount());
        assertEquals(0, storeHome.getCount());

        AbstractPerson person = home.newPerson(name, firstName, "", "", "");
        final long id = person.getID();

        person = home.newPerson("Name", "First", "", "", "");
        assertEquals(2, home.getCount());
        assertEquals(2, storeHome.getCount());

        person = (AbstractPerson) home.getItem(id);
        assertEquals("title", name + ", " + firstName, person.getTitle());
    }

    @Test
    public void testStructure() throws Exception {
        final PersonHome home = data.getPersonHome();
        assertTrue("table structure", home.checkStructure(null));
    }

}
