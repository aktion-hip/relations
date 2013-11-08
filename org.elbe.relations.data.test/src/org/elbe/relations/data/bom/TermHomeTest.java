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

import org.elbe.relations.data.DataHouseKeeper;
import org.hip.kernel.exc.VException;
import org.junit.After;
import org.junit.Before;
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

	@Before
	public void setUp() throws Exception {
		// data.setUp();
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testNewTerm() throws Exception {
		final long lNow = System.currentTimeMillis() - 1000;

		final TermHome lHome = data.getTermHome();

		assertEquals("number 0", 0, lHome.getCount());

		final AbstractTerm lTerm = lHome.newTerm("Title", "Text");
		assertEquals("number 1", 1, lHome.getCount());

		final AbstractTerm lRetrieved = lHome.getTerm(lTerm.getID());
		final long lCreated = ((Timestamp) lRetrieved.get(TermHome.KEY_CREATED))
				.getTime();
		final long lModified = ((Timestamp) lRetrieved
				.get(TermHome.KEY_MODIFIED)).getTime();
		assertEquals("Created 1", lCreated, lModified);
		assertTrue("Created 2", lCreated >= lNow);

		lHome.deleteItem(lTerm.getID());
		assertEquals("number 2", 0, lHome.getCount());
	}

	@Test
	public void testGetTerm() throws VException, SQLException, BOMException {
		final String lTitle = "ThisTitle";
		final String lText = "This Text";
		final TermHome lHome = data.getTermHome();

		assertEquals("number 0", 0, lHome.getCount());

		AbstractTerm lTerm = lHome.newTerm(lTitle, lText);
		final long lId = lTerm.getID();

		lTerm = lHome.newTerm("next", "next");
		assertEquals("number 1", 2, lHome.getCount());

		lTerm = (AbstractTerm) lHome.getItem(lId);
		assertEquals("title", lTitle, lTerm.getTitle());
		assertEquals("text", lText, lTerm.get(TermHome.KEY_TEXT).toString());
	}

}
