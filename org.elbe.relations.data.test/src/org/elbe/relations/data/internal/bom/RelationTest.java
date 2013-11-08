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
package org.elbe.relations.data.internal.bom;

import static org.junit.Assert.fail;

import org.elbe.relations.data.DataHouseKeeper;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.exc.VException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Luthiger
 */
public class RelationTest {
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
	public void testGetItem() throws Exception {
		final AbstractTerm term1 = data.createTerm("term 1");
		final AbstractTerm term2 = data.createTerm("term 2");
		final AbstractPerson person = data.createPerson("person1", "1");

		final RelationHome lHome = data.getRelationHome();
		final Relation lRelation1 = lHome.newRelation(term1, person);
		final Relation lRelation2 = lHome.newRelation(term2, term1);

		assertRelations("Relation 1", lRelation1.getItem1(),
				lRelation1.getItem2(), term1, person);
		assertRelations("Relation 2", lRelation2.getItem1(),
				lRelation2.getItem2(), term2, term1);
	}

	private void assertRelations(final String inComment,
			final UniqueID inRelated1, final UniqueID inRelated2,
			final IItem inExpected1, final IItem inExpected2) throws VException {
		final UniqueID lExpected1 = new UniqueID(inExpected1.getItemType(),
				inExpected1.getID());
		final UniqueID lExpected2 = new UniqueID(inExpected2.getItemType(),
				inExpected2.getID());

		if (inRelated1.equals(lExpected1)) {
			if (inRelated2.equals(lExpected2)) {
				return;
			}
		} else {
			if (inRelated2.equals(lExpected1)) {
				return;
			}
		}
		fail(inComment);
	}

}
