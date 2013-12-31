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
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.data.utility.IItemVisitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author lbenno
 */
@RunWith(MockitoJUnitRunner.class)
public class TextTest {
	private static final String NL = System.getProperty("line.separator");
	private static final String TITLE = "test title";
	private static final String TEXT = "test text";
	private static final Timestamp TIMESTAMP = new Timestamp(1387719218782l);

	@Mock
	private IItemVisitor visitor;

	private IndexerHelper indexer;

	private Text text;

	@Before
	public void setUp() throws Exception {
		text = new Text();
		text.set(TextHome.KEY_ID, 123l);
		text.set(TextHome.KEY_TITLE, TITLE);
		text.set(TextHome.KEY_TEXT, TEXT);
		text.set(TextHome.KEY_CREATED, TIMESTAMP);
		text.set(TextHome.KEY_MODIFIED, TIMESTAMP);

		indexer = new IndexerHelper();
	}

	@Test
	public void testVisit() throws Exception {
		text.visit(visitor);
		verify(visitor).setTitle(TITLE);
		verify(visitor).setTitleEditable(true);
		verify(visitor).setText(NL + "[test text...]");
		verify(visitor).setRealText(TEXT);
		verify(visitor).setTextEditable(false);
	}

	@Test
	public void testIndexContent() throws Exception {
		text.indexContent(indexer);
		final Collection<IndexerDocument> docs = indexer.getDocuments();
		assertEquals(1, docs.size());

		final IndexerDocument doc = docs.iterator().next();
		final Map<String, String> fields = DataHouseKeeper.createFieldMap(doc);

		assertFieldValue(fields, "itemID", "123");
		assertFieldValue(fields, "uniqueID", "2:123");
		assertFieldValue(fields, "itemType", "2");
		assertFieldValue(fields, "itemTitle", "test title");
		assertFieldValue(fields, "itemFull", "test title          test text ");
	}

	private void assertFieldValue(final Map<String, String> inFields,
	        final String inID, final String inExpected) {
		assertEquals(inExpected, inFields.get(inID));
	}

	@Test
	public void testGetBibtexFormatted() throws Exception {
		final Collection<String> labels = new ArrayList<String>(3);

		text.set(TextHome.KEY_AUTHOR, "Doe, Jane");
		text.set(TextHome.KEY_YEAR, "2010");

		// book
		text.set(TextHome.KEY_TYPE, AbstractText.TYPE_BOOK);
		String bibtex = text.getBibtexFormatted(labels);
		assertEquals("@BOOK{Doe:10," + NL + "     AUTHOR = {Doe, Jane}," + NL
		        + "     TITLE = {test title}," + NL + "     YEAR = 2010" + NL
		        + "}", bibtex);

		// article
		text.set(TextHome.KEY_TYPE, AbstractText.TYPE_ARTICLE);
		bibtex = text.getBibtexFormatted(labels);
		assertEquals("@ARTICLE{Doe:10a," + NL + "     AUTHOR = {Doe, Jane},"
		        + NL + "     TITLE = {test title}," + NL + "     YEAR = 2010"
		        + NL + "}", bibtex);

		// contribution
		text.set(TextHome.KEY_TYPE, AbstractText.TYPE_CONTRIBUTION);
		bibtex = text.getBibtexFormatted(labels);
		assertEquals("@INCOLLECTION{Doe:10b," + NL
		        + "     AUTHOR = {Doe, Jane}," + NL
		        + "     TITLE = {test title}," + NL + "     YEAR = 2010" + NL
		        + "}", bibtex);

		// web
		text.set(TextHome.KEY_TYPE, AbstractText.TYPE_WEBPAGE);
		bibtex = text.getBibtexFormatted(labels);
		assertEquals("@ARTICLE{Doe:10c," + NL + "     AUTHOR = {Doe, Jane},"
		        + NL + "     TITLE = {test title}," + NL + "     YEAR = 2010"
		        + NL + "}", bibtex);

	}

}
