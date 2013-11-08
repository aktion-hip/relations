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

import org.elbe.relations.data.DataHouseKeeper;
import org.elbe.relations.data.internal.bom.Relation;
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
		term1 = data.createTerm("Term1");
		term2 = data.createTerm("Term2");
		person = data.createPerson("Pan", "Peter");
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testNewRelation() throws Exception {
		final long lID1 = term1.getID();
		final long lID2 = person.getID();

		final RelationHome lHome = data.getRelationHome();

		assertEquals("number 0", 0, lHome.getCount());

		// create first relation with term1 and person
		Relation lRelation = lHome.newRelation(term1, person);
		final long lID = lRelation.getID();

		// create second relation
		lRelation = lHome.newRelation(person, term2);
		assertEquals("number 1", 2, lHome.getCount());

		// retrieve first relation
		lRelation = lHome.getRelation(lID);
		assertEquals("id 1", lID1, lRelation.getItemId1());
		assertEquals("id 2", lID2, lRelation.getItemId2());
		assertEquals("type 1", IItem.TERM, lRelation.getItemType1());
		assertEquals("type 2", IItem.PERSON, lRelation.getItemType2());

		// delete first relation
		lHome.deleteRelation(lID);
		assertEquals("number 2", 1, lHome.getCount());
	}

	@Test
	public void testGetRelation() throws Exception {
		final RelationHome lHome = data.getRelationHome();
		assertEquals("number 0", 0, lHome.getCount());

		lHome.newRelation(term1, person);
		lHome.newRelation(term2, term1);
		lHome.newRelation(person, term2);
		assertEquals("number 1", 3, lHome.getCount());

		final QueryResult lResult = lHome.getRelations(term1);
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
		final Relation lRelation1 = lHome.newRelation(term1, person);
		final Relation lRelation2 = lHome.newRelation(term2, term1);

		final Relation lRelation3 = lHome.getRelation(IItem.TERM,
				term1.getID(), IItem.PERSON, person.getID());
		assertEquals("retrieved 1", lRelation1.getID(), lRelation3.getID());
		assertTrue("not equal", lRelation1.getID() != lRelation2.getID());

		assertEquals("number before delete", 2, lHome.getCount());
		lHome.deleteRelation(IItem.TERM, term1.getID(), IItem.PERSON,
				person.getID());
		assertEquals("number after delete", 1, lHome.getCount());
		try {
			lHome.getRelation(IItem.TERM, term1.getID(), IItem.PERSON,
					person.getID());
			fail("Shouldn't get here");
		}
		catch (final BOMException exc) {
			// Intentionally left empty.
		}
	}

	@Test
	public void testDeleteRelations() throws Exception {
		final AbstractTerm lTerm1 = data.createTerm("local 1");
		final AbstractTerm lTerm2 = data.createTerm("local 2");

		final RelationHome lHome = data.getRelationHome();
		lHome.newRelation(lTerm1, lTerm2);
		lHome.newRelation(lTerm1, person);
		lHome.newRelation(lTerm1, term1);
		lHome.newRelation(term1, person);
		lHome.newRelation(term2, term1);
		lHome.newRelation(term2, lTerm2);

		assertEquals("number of relations 1", 6, lHome.getCount());

		lHome.deleteRelations(term1);
		assertEquals("number of relations 2", 3, lHome.getCount());

		lHome.deleteRelations(lTerm1);
		assertEquals("number of relations 3", 1, lHome.getCount());
	}

}
