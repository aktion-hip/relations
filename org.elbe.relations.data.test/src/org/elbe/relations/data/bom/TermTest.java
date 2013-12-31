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
public class TermTest {
	private static final String TITLE = "test title";
	private static final String TEXT = "test text";
	private static final Timestamp TIMESTAMP = new Timestamp(1387719218782l);

	@Mock
	private IItemVisitor visitor;

	private IndexerHelper indexer;

	private Term term;

	@Before
	public void setUp() throws Exception {
		term = new Term();
		term.set(TermHome.KEY_ID, 123l);
		term.set(TermHome.KEY_TITLE, TITLE);
		term.set(TermHome.KEY_TEXT, TEXT);
		term.set(TermHome.KEY_CREATED, TIMESTAMP);
		term.set(TermHome.KEY_MODIFIED, TIMESTAMP);

		indexer = new IndexerHelper();
	}

	@Test
	public void testVisit() throws Exception {
		term.visit(visitor);
		verify(visitor).setTitle(TITLE);
		verify(visitor).setTitleEditable(true);
		verify(visitor).setText(TEXT);
		verify(visitor).setTextEditable(true);
	}

	@Test
	public void testIndexContent() throws Exception {
		term.indexContent(indexer);
		final Collection<IndexerDocument> docs = indexer.getDocuments();
		assertEquals(1, docs.size());

		final IndexerDocument doc = docs.iterator().next();
		final Map<String, String> fields = DataHouseKeeper.createFieldMap(doc);

		assertFieldValue(fields, "itemID", "123");
		assertFieldValue(fields, "uniqueID", "1:123");
		assertFieldValue(fields, "itemType", "1");
		assertFieldValue(fields, "itemTitle", "test title");
		assertFieldValue(fields, "itemFull", "test title test text ");
	}

	private void assertFieldValue(final Map<String, String> inFields,
	        final String inID, final String inExpected) {
		assertEquals(inExpected, inFields.get(inID));
	}

}
