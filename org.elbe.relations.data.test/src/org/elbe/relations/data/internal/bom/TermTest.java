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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import org.elbe.relations.data.bom.AbstractItem;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.data.bom.Term;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Luthiger
 */
public class TermTest {
	private static DataHouseKeeper data;

	private final String title = "Title";
	private final String text = "Text";

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
		final TermHome lHome = data.getTermHome();

		assertEquals("number 0", 0, lHome.getCount());

		final AbstractTerm lTerm = lHome.newTerm(title, text);
		assertEquals("number 1", 1, lHome.getCount());

		final AbstractTerm lTerm2 = lHome.getTerm(lTerm.getID());
		final LightWeightTerm lLightWeight = (LightWeightTerm) lTerm2.getLightWeight();

		assertEquals("id", lTerm.getID(), lLightWeight.getID());
		assertEquals("title", title, lLightWeight.title);
		assertEquals("text", text, lLightWeight.text);
	}

	@Test
	public void testSaveTitleText() throws Exception {
		final String lTitle2 = "new title";
		final String lText2 = "new text content";

		final TermHome lHome = data.getTermHome();

		assertEquals("number 0", 0, lHome.getCount());

		final AbstractTerm lTerm = lHome.newTerm(title, text);
		assertEquals("number 1", 1, lHome.getCount());

		final AbstractTerm lTerm2 = lHome.getTerm(lTerm.getID());

		assertEquals("title 1", title, lTerm2.getTitle());
		assertEquals("text 1", text, lTerm2.get(TermHome.KEY_TEXT));

		lTerm2.saveTitleText(lTitle2, lText2);

		final AbstractTerm lTerm3 = lHome.getTerm(lTerm.getID());

		assertEquals("title 2", lTitle2, lTerm3.getTitle());
		assertEquals("text 2", lText2, lTerm3.get(TermHome.KEY_TEXT));
	}

	@Test
	public void testSave() throws Exception {
		final long lStart = System.currentTimeMillis() - 1000;

		final TermHome lHome = data.getTermHome();
		AbstractTerm lTerm = lHome.newTerm(title, text);
		assertEquals("number 1", 1, lHome.getCount());

		lTerm.save("new title", "new text");
		lTerm = lHome.getTerm(lTerm.getID());
		assertTrue("compare timestamp", lStart < ((Timestamp) lTerm.get(TermHome.KEY_MODIFIED)).getTime());

		final String lCreated = ((IItem) lTerm).getCreated();
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
	public void testIndexContent() throws Exception {
		final IndexerHelper lIndexer = new IndexerHelper();

		final TermHome lHome = data.getTermHome();
		final AbstractTerm lTerm = lHome.newTerm(title, text);
		((Term) lTerm).indexContent(lIndexer);

		assertEquals("number of index docs", 1, lIndexer.getDocuments().size());
		final IndexerDocument lDocument = lIndexer.getDocuments().iterator().next();
		final Collection<IndexerField> lFields = lDocument.getFields();

		assertEquals("number of index fields", 7, lFields.size());
		final Collection<String> lFieldNames = new ArrayList<String>();
		final Collection<String> lFieldFull = new ArrayList<String>();
		for (final IndexerField lField : lDocument.getFields()) {
			lFieldNames.add(lField.getFieldName());
			lFieldFull.add(lField.toString());
		}
		assertTrue("contains itemID", lFieldNames.contains("itemID"));
		assertTrue("contains itemType", lFieldNames.contains("itemType"));
		assertTrue("contains itemTitle", lFieldNames.contains("itemTitle"));
		assertTrue("contains itemDateCreated", lFieldNames.contains("itemDateCreated"));
		assertTrue("contains itemDateModified", lFieldNames.contains("itemDateModified"));
		assertTrue("contains itemFull", lFieldNames.contains("itemFull"));
		assertTrue("contains full 'itemType: 1'", lFieldFull.contains("itemType: 1"));
		assertTrue("contains full 'itemTitle: Title'", lFieldFull.contains("itemTitle: Title"));
	}

	/**
	 * Test of equals() and hashCode() defined on base class
	 * {@link AbstractItem}
	 *
	 * @throws Exception
	 */
	@Test
	public void testEquals() throws Exception {
		final TermHome lHome = data.getTermHome();
		final AbstractTerm lTerm1 = lHome.newTerm(title, text);
		final AbstractTerm lTerm2 = lHome.newTerm(title, text);
		final AbstractTerm lTerm3 = lHome.getTerm(lTerm1.getID());

		assertTrue("term equals self", lTerm1.equals(lTerm1));
		assertTrue("hashCode equals self", lTerm1.hashCode() == lTerm1.hashCode());
		assertFalse("model not equals null", lTerm1.equals(null));
		assertFalse("model not equals String", lTerm1.equals(lTerm1.toString()));
		assertFalse("String not equals model", "model".equals(lTerm1));
		assertFalse("model not equals term model", lTerm1.equals(lTerm2));
		assertTrue("term 1 equals term 3", lTerm1.equals(lTerm3));
		assertTrue("term 3 equals term 1", lTerm3.equals(lTerm1));
		assertEquals("hashCode 1 equals hashCode 3", lTerm1.hashCode(), lTerm3.hashCode());
	}

}
