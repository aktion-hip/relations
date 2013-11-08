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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Vector;

import org.elbe.relations.data.DataHouseKeeper;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.LightWeightPerson;
import org.elbe.relations.data.bom.Person;
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.IndexerHelper;
import org.hip.kernel.exc.VException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Luthiger
 */
public class PersonTest {
	private static DataHouseKeeper data;

	private final String name = "Name";
	private final String firstName = "Firstname";
	private final String text = "Text";
	private final String from = "1.1.2000";
	private final String to = "31.12.2010";

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
	public void testGetLightWeight() throws Exception {
		final PersonHome lHome = data.getPersonHome();

		assertEquals("number 0", 0, lHome.getCount());

		final AbstractPerson lPerson = lHome.newPerson(name, firstName, from,
				to, text);
		assertEquals("number 1", 1, lHome.getCount());

		final AbstractPerson lPerson2 = lHome.getPerson(lPerson.getID());
		final LightWeightPerson lLightWeight = (LightWeightPerson) lPerson2
				.getLightWeight();

		assertEquals("id", lPerson.getID(), lLightWeight.getID());
		assertEquals("name", name, lLightWeight.name);
		assertEquals("firstName", firstName, lLightWeight.firstname);
		assertEquals("text", text, lLightWeight.text);
		assertEquals("from", from, lLightWeight.from);
		assertEquals("to", to, lLightWeight.to);
	}

	@Test
	public void testSave() throws VException, SQLException, BOMException {
		final long lStart = System.currentTimeMillis() - 1000;

		final String lFirstname2 = "Changed First Name";
		final String lTo2 = "8.8.2012";

		final PersonHome lHome = data.getPersonHome();

		assertEquals("number 0", 0, lHome.getCount());

		final AbstractPerson lPerson = lHome.newPerson(name, firstName, from,
				to, text);
		assertEquals("number 1", 1, lHome.getCount());

		final AbstractPerson lPerson2 = lHome.getPerson(lPerson.getID());
		lPerson2.save(name, lFirstname2, text, from, lTo2);

		final AbstractPerson lPerson3 = lHome.getPerson(lPerson.getID());

		assertEquals("name", name, lPerson3.get(PersonHome.KEY_NAME));
		assertEquals("firstName", lFirstname2,
				lPerson3.get(PersonHome.KEY_FIRSTNAME));
		assertEquals("text", text, lPerson3.get(PersonHome.KEY_TEXT));
		assertEquals("to", lTo2, lPerson3.get(PersonHome.KEY_TO));

		assertTrue("compare timestamp",
				lStart < ((Timestamp) lPerson3.get(PersonHome.KEY_MODIFIED))
						.getTime());
		final String lCreated = ((IItem) lPerson3).getCreated();
		assertNotNull("created string exists", lCreated);

		String lCreatedLbl = "Created:";
		String lModifiedLbl = "Modified:";
		if (data.isGerman()) {
			lCreatedLbl = "Erzeugt:";
			lModifiedLbl = "Verändert:";
		}
		assertTrue("Created:", lCreated.indexOf(lCreatedLbl) >= 0);
		assertTrue("Modified:", lCreated.indexOf(lModifiedLbl) >= 0);
	}

	@Test
	public void testGetTitle() throws VException, SQLException, BOMException {
		final PersonHome lHome = data.getPersonHome();

		final AbstractPerson lPerson = lHome.newPerson(name, firstName, from,
				to, text);
		final AbstractPerson lPerson2 = lHome.getPerson(lPerson.getID());

		assertEquals("title is name, firstName", name + ", " + firstName,
				lPerson2.getTitle());
	}

	@Test
	public void testIndexContent() throws Exception {
		final IndexerHelper lIndexer = new IndexerHelper();

		final PersonHome lHome = data.getPersonHome();
		final AbstractPerson lPerson = lHome.newPerson(name, firstName, from,
				to, text);
		((Person) lPerson).indexContent(lIndexer);

		assertEquals("number of index docs", 1, lIndexer.getDocuments().size());
		final IndexerDocument lDocument = lIndexer.getDocuments().iterator()
				.next();
		final Collection<IndexerField> lFields = lDocument.getFields();
		assertEquals("number of index fields", 7, lFields.size());
		final Collection<String> lFieldNames = new Vector<String>();
		final Collection<String> lFieldFull = new Vector<String>();
		for (final IndexerField lField : lDocument.getFields()) {
			lFieldNames.add(lField.getFieldName());
			lFieldFull.add(lField.toString());
		}
		assertTrue("contains itemID", lFieldNames.contains("itemID"));
		assertTrue("contains itemType", lFieldNames.contains("itemType"));
		assertTrue("contains itemTitle", lFieldNames.contains("itemTitle"));
		assertTrue("contains itemDateCreated",
				lFieldNames.contains("itemDateCreated"));
		assertTrue("contains itemDateModified",
				lFieldNames.contains("itemDateModified"));
		assertTrue("contains itemFull", lFieldNames.contains("itemFull"));
		assertTrue("contains full 'itemType: 3'",
				lFieldFull.contains("itemType: 3"));
		assertTrue("contains full 'itemTitle: Firstname Name'",
				lFieldFull.contains("itemTitle: Firstname Name"));
	}

}
