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

package org.elbe.relations.indexer.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.elbe.relations.data.search.AbstractSearching;
import org.elbe.relations.data.search.IIndexer;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.IndexerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author lbenno
 */
public class LuceneIndexerTest {
	private final static String LANGUAGE = "de";
	private final IndexHouseKeeper housekeeper = new IndexHouseKeeper();

	@Before
	public void setUp() throws IOException {
		housekeeper.setUp();
	}

	@After
	public void tearDown() throws IOException {
		housekeeper.tearDown();
	}

	@Test
	public void testGetAnalyzerLanguages() {
		final String[] lExpected = { "en", "de", "br", "cn", "cz", "el", "fr",
		        "nl", "ru", "th" };
		final Collection<String> lExpectedLanguages = Arrays.asList(lExpected);

		final IIndexer lIndexer = new LuceneIndexer();
		final Collection<String> lLanguages = lIndexer.getAnalyzerLanguages();

		assertEquals(lExpected.length, lLanguages.size());
		for (final String lLanguage : lLanguages) {
			assertTrue(lExpectedLanguages.contains(lLanguage));
		}
	}

	@Test
	public void testProcessIndexer() throws Exception {
		final IIndexer lIndexer = new LuceneIndexer();
		lIndexer.processIndexer(getDocIndexer(),
		        IndexHouseKeeper.getDirectory(), LANGUAGE);
		final int lNumber = lIndexer.numberOfIndexed(IndexHouseKeeper
		        .getDirectory());
		assertEquals("one document indexed", 1, lNumber);
	}

	private IndexerHelper getDocIndexer() {
		final IndexerHelper outIndexer = new IndexerHelper();
		return addDocument(outIndexer, "name", "value");
	}

	private IndexerHelper addDocument(final IndexerHelper inIndexer,
	        final String inName, final String inValue) {
		final IndexerDocument lDocument = new IndexerDocument();
		lDocument.addField(new IndexerField(inName, inValue,
		        IndexerField.Store.YES, IndexerField.Index.NOT_ANALYZED));
		inIndexer.addDocument(lDocument);
		return inIndexer;
	}

	@Test
	public void testInitializeIndex() throws IOException {
		final File lDir = IndexHouseKeeper.getDirectory();
		assertTrue("index dir exists", lDir.exists());
		final String[] lContent = lDir.list();
		assertNotNull("the directory contains files", lContent);
		boolean lStartsWithSegments = false;
		for (final String lFileName : lContent) {
			lStartsWithSegments = lStartsWithSegments
			        || lFileName.startsWith("segments");
		}
		assertTrue("at least on containing file starts with 'segments'",
		        lStartsWithSegments);
	}

	@Test
	public void testDeleteItemInIndex() throws Exception {
		final String lUniqueID = "2:987";
		final String lFieldName = AbstractSearching.ITEM_ID;

		IndexerHelper lDocIndexer = getDocIndexer();
		lDocIndexer = addDocument(lDocIndexer, lFieldName, lUniqueID);

		final IIndexer lIndexer = new LuceneIndexer();
		lIndexer.processIndexer(lDocIndexer, IndexHouseKeeper.getDirectory(),
		        LANGUAGE);
		assertEquals("two documents in index", 2,
		        lIndexer.numberOfIndexed(IndexHouseKeeper.getDirectory()));

		lIndexer.deleteItemInIndex(lUniqueID, lFieldName,
		        IndexHouseKeeper.getDirectory());
		assertEquals("one document in index", 1,
		        lIndexer.numberOfIndexed(IndexHouseKeeper.getDirectory()));
	}

}
