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
import static org.junit.Assert.fail;

import org.elbe.relations.data.internal.bom.Relation;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.hip.kernel.bom.QueryResult;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Luthiger
 */
public class RelationHomeTest {
    private static DataHouseKeeper data;

    private AbstractTerm term1;
    private AbstractTerm term2;
    private AbstractPerson person;

    @BeforeClass
    public static void init() {
        data = DataHouseKeeper.INSTANCE;
    }

    @Before
    public void setUp() throws Exception {
        // data.setUp();
        this.term1 = data.createTerm("Term1");
        this.term2 = data.createTerm("Term2");
        this.person = data.createPerson("Pan", "Peter");
    }

    @After
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testNewRelation() throws Exception {
        final long lID1 = this.term1.getID();
        final long lID2 = this.person.getID();

        final RelationHome home = data.getRelationHome();
        final EventStoreHome storeHome = data.getEventStoreHome();

        assertEquals(0, home.getCount());
        assertEquals(3, storeHome.getCount());

        // create first relation with term1 and person
        Relation lRelation = home.newRelation(this.term1, this.person);
        final long lID = lRelation.getID();

        // create second relation
        lRelation = home.newRelation(this.person, this.term2);
        assertEquals(2, home.getCount());
        assertEquals(5, storeHome.getCount());

        // retrieve first relation
        lRelation = home.getRelation(lID);
        assertEquals("id 1", lID1, lRelation.getItemId1());
        assertEquals("id 2", lID2, lRelation.getItemId2());
        assertEquals("type 1", IItem.TERM, lRelation.getItemType1());
        assertEquals("type 2", IItem.PERSON, lRelation.getItemType2());

        // delete first relation
        home.deleteRelation(lID);
        assertEquals(1, home.getCount());
        assertEquals(6, storeHome.getCount());
    }

    @Test
    public void testGetRelation() throws Exception {
        final RelationHome lHome = data.getRelationHome();
        assertEquals("number 0", 0, lHome.getCount());

        lHome.newRelation(this.term1, this.person);
        lHome.newRelation(this.term2, this.term1);
        lHome.newRelation(this.person, this.term2);
        assertEquals("number 1", 3, lHome.getCount());

        final QueryResult lResult = lHome.getRelations(this.term1);
        int lCount = 0;
        while (lResult.hasMoreElements()) {
            lResult.next();
            ++lCount;
        }
        assertEquals("number of term 1", 2, lCount);
    }

    @Test
    public void testGetRelation2() throws Exception {
        final RelationHome lHome = data.getRelationHome();
        final Relation lRelation1 = lHome.newRelation(this.term1, this.person);
        final Relation lRelation2 = lHome.newRelation(this.term2, this.term1);

        final Relation lRelation3 = lHome.getRelation(IItem.TERM,
                this.term1.getID(), IItem.PERSON, this.person.getID());
        assertEquals("retrieved 1", lRelation1.getID(), lRelation3.getID());
        assertTrue("not equal", lRelation1.getID() != lRelation2.getID());

        assertEquals("number before delete", 2, lHome.getCount());
        lHome.deleteRelation(IItem.TERM, this.term1.getID(), IItem.PERSON,
                this.person.getID());
        assertEquals("number after delete", 1, lHome.getCount());
        try {
            lHome.getRelation(IItem.TERM, this.term1.getID(), IItem.PERSON,
                    this.person.getID());
            fail("Shouldn't get here");
        }
        catch (final BOMException exc) {
            // Intentionally left empty.
        }
    }

    @Test
    public void testDeleteRelations() throws Exception {
        final AbstractTerm localTerm1 = data.createTerm("local 1");
        final AbstractTerm localTerm2 = data.createTerm("local 2");

        final RelationHome home = data.getRelationHome();
        home.newRelation(localTerm1, localTerm2);
        home.newRelation(localTerm1, this.person);
        home.newRelation(localTerm1, this.term1);
        home.newRelation(this.term1, this.person);
        home.newRelation(this.term2, this.term1);
        home.newRelation(this.term2, localTerm2);

        final EventStoreHome storeHome = data.getEventStoreHome();
        assertEquals(6, home.getCount());
        assertEquals(11, storeHome.getCount());

        home.deleteRelations(this.term1);
        assertEquals(3, home.getCount());
        assertEquals(14, storeHome.getCount());

        home.deleteRelations(localTerm1);
        assertEquals(1, home.getCount());
        assertEquals(16, storeHome.getCount());
    }

}
