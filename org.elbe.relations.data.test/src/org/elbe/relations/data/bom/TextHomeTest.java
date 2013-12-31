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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Luthiger
 */
public class TextHomeTest {
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
	public void testNewText() throws BOMException, VException, SQLException {
		final long lNow = System.currentTimeMillis() - 1000;

		final TextHome lHome = data.getTextHome();

		assertEquals("number 0", 0, lHome.getCount());

		final AbstractItem lText = lHome.newText("Book Title", "", "Author",
				"", "", "", "", "", new Integer(0), new Integer(0), "", "",
				new Integer(1));
		assertEquals("number 1", 1, lHome.getCount());

		final AbstractItem lRetrieved = lHome.getText(lText.getID());
		final long lCreated = ((Timestamp) lRetrieved.get(TextHome.KEY_CREATED))
				.getTime();
		final long lModified = ((Timestamp) lRetrieved
				.get(TextHome.KEY_MODIFIED)).getTime();
		assertEquals("Created 1", lCreated, lModified);
		assertTrue("Created 2", lCreated >= lNow);

		lHome.deleteItem(lText.getID());
		assertEquals("number 2", 0, lHome.getCount());
	}

	/*
	 * Test method for 'org.elbe.relations.bom.TextHome.getText(long)'
	 */
	@Test
	public void testGetText() throws BOMException, VException, SQLException {
		final String lTitle = "Book Title";
		final String lAuthor = "Author";
		final TextHome lHome = data.getTextHome();

		assertEquals("number 0", 0, lHome.getCount());

		AbstractItem lText = lHome.newText(lTitle, "", lAuthor, "", "", "", "",
				"", new Integer(0), new Integer(0), "", "", new Integer(1));
		final long lId = lText.getID();

		lHome.newText("another", "", "text", "", "", "", "", "",
				new Integer(0), new Integer(0), "", "", new Integer(1));
		assertEquals("number 1", 2, lHome.getCount());

		lText = (AbstractItem) lHome.getItem(lId);
		assertEquals("title", lTitle, lText.getTitle());
		assertEquals("author", lAuthor, lText.get(TextHome.KEY_AUTHOR)
				.toString());
	}

}
