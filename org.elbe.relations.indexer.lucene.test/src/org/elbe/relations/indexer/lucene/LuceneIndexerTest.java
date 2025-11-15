/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.elbe.relations.data.search.AbstractSearching;
import org.elbe.relations.data.search.IIndexer;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.IndexerHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author lbenno
 */
public class LuceneIndexerTest {
    private final IndexHouseKeeper housekeeper = new IndexHouseKeeper();

    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        this.housekeeper.setUp(this.tempDir);
    }

    @AfterEach
    public void tearDown() throws IOException {
        this.housekeeper.tearDown(this.tempDir);
    }

    @Test
    void testGetAnalyzerLanguages() {
        final String[] expected = { "ar", "bg", "br", "ca", "cn", "cz", "da", "de", "el", "en", "es", "eu", "fa", "fi",
                "fr", "gl", "hi", "hu", "hy", "id", "it", "lv", "nl", "no", "pt", "ro", "ru", "sv", "th", "tr" };
        final Collection<String> expectedLanguages = Arrays.asList(expected);

        final IIndexer indexer = new LuceneIndexer();
        final Collection<String> languages = indexer.getAnalyzerLanguages();

        assertEquals(expected.length, languages.size());
        for (final String language : languages) {
            assertTrue(expectedLanguages.contains(language));
        }
    }

    @Test
    void testProcessIndexer() throws Exception {
        final IIndexer lIndexer = new LuceneIndexer();

        lIndexer.processIndexer(getDocIndexer(), this.tempDir, IndexHouseKeeper.LANGUAGE);
        assertEquals(1, lIndexer.numberOfIndexed(this.tempDir));

        // initialize
        lIndexer.initializeIndex(this.tempDir, IndexHouseKeeper.LANGUAGE);
        assertEquals(0, lIndexer.numberOfIndexed(this.tempDir));
    }

    @Test
    void testDeleteItemInIndex() throws Exception {
        final String lUniqueID = "2:987";
        final String lFieldName = AbstractSearching.ITEM_ID;

        final IndexerHelper docIndexer = addDocument(getDocIndexer(), lFieldName, lUniqueID, IndexerField.Type.ID);

        final IIndexer lIndexer = new LuceneIndexer();
        lIndexer.processIndexer(docIndexer, this.tempDir, IndexHouseKeeper.LANGUAGE);
        assertEquals(2, lIndexer.numberOfIndexed(this.tempDir));

        lIndexer.deleteItemInIndex(lUniqueID, lFieldName, this.tempDir, IndexHouseKeeper.LANGUAGE);
        assertEquals(1, lIndexer.numberOfIndexed(this.tempDir));
    }

    private IndexerHelper getDocIndexer() {
        final IndexerHelper outIndexer = new IndexerHelper();
        return addDocument(outIndexer, "name", "value", IndexerField.Type.FULL_TEXT);
    }

    private IndexerHelper addDocument(final IndexerHelper inIndexer, final String inName, final String inValue,
            final IndexerField.Type inType) {
        final IndexerDocument lDocument = new IndexerDocument();
        lDocument.addField(new IndexerField(inName, inValue, IndexerField.Store.YES, inType, 1.0f));
        inIndexer.addDocument(lDocument);
        return inIndexer;
    }

}
