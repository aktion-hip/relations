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

import org.elbe.relations.data.DataHouseKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
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
		final long lNow = System.currentTimeMillis() - 1000;

		final PersonHome lHome = data.getPersonHome();

		assertEquals("number 0", 0, lHome.getCount());

		final AbstractPerson lPerson = lHome.newPerson("Pan", "Peter", "", "",
				"");
		assertEquals("number 1", 1, lHome.getCount());

		final AbstractPerson lRetrieved = lHome.getPerson(lPerson.getID());
		final long lCreated = ((Timestamp) lRetrieved
				.get(PersonHome.KEY_CREATED)).getTime();
		final long lModified = ((Timestamp) lRetrieved
				.get(PersonHome.KEY_MODIFIED)).getTime();
		assertEquals("Created 1", lCreated, lModified);
		assertTrue("Created 2", lCreated >= lNow);

		lHome.deleteItem(lPerson.getID());
		assertEquals("number 2", 0, lHome.getCount());
	}

	@Test
	public void testGetPerson() throws Exception {
		final String lName = "Pan";
		final String lFirstName = "Peter";
		final PersonHome lHome = data.getPersonHome();

		assertEquals("number 0", 0, lHome.getCount());

		AbstractPerson lPerson = lHome.newPerson(lName, lFirstName, "", "", "");
		final long lId = lPerson.getID();

		lPerson = lHome.newPerson("Name", "First", "", "", "");
		assertEquals("number 1", 2, lHome.getCount());

		lPerson = (AbstractPerson) lHome.getItem(lId);
		assertEquals("title", lName + ", " + lFirstName, lPerson.getTitle());
	}

	@Test
	public void testStructure() throws Exception {
		final PersonHome lHome = data.getPersonHome();
		assertTrue("table structure", lHome.checkStructure(null));
	}

}
