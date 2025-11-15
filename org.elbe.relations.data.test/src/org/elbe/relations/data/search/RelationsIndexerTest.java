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
package org.elbe.relations.data.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.elbe.relations.data.internal.search.IndexerRegistration;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.hip.kernel.exc.VException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author lbenno
 */
@ExtendWith(MockitoExtension.class)
public class RelationsIndexerTest {
    private static DataHouseKeeper data;

    @Mock
    private IProgressMonitor monitor;

    private RelationsIndexer indexer;

    @BeforeAll
    public static void init() {
        data = DataHouseKeeper.INSTANCE;
    }

    @BeforeEach
    public void setUp() {
        this.indexer = new TestIndexer();
    }

    @AfterEach
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testRefreshIndex() throws IOException, VException, Exception {
        final IIndexer lIndexer1 = mock(IIndexer.class);
        IndexerRegistration.INSTANCE.register(lIndexer1);

        int lIndexed = this.indexer.refreshIndex(this.monitor);
        assertEquals(0, lIndexed);
        verify(lIndexer1).initializeIndex(this.indexer.getIndexPath(), Locale.ENGLISH.getLanguage());
        verify(lIndexer1, times(3)).processIndexer(any(IndexerHelper.class), eq(this.indexer.getIndexPath()),
                eq(this.indexer.getLanguage()));

        // index term
        final IIndexer lIndexer2 = mock(IIndexer.class);
        IndexerRegistration.INSTANCE.register(lIndexer2);
        data.createTerm("term for indexing");
        lIndexed = this.indexer.refreshIndex(this.monitor);
        assertEquals(1, lIndexed);
        verify(lIndexer2, times(3)).processIndexer(any(IndexerHelper.class), eq(this.indexer.getIndexPath()),
                eq(this.indexer.getLanguage()));

        // index person
        final IIndexer lIndexer3 = mock(IIndexer.class);
        IndexerRegistration.INSTANCE.register(lIndexer3);
        data.createPerson("Doe", "Jane");
        lIndexed = this.indexer.refreshIndex(this.monitor);
        assertEquals(2, lIndexed);
        verify(lIndexer3, times(3)).processIndexer(any(IndexerHelper.class), eq(this.indexer.getIndexPath()),
                eq(this.indexer.getLanguage()));

        // index text
        final IIndexer lIndexer4 = mock(IIndexer.class);
        IndexerRegistration.INSTANCE.register(lIndexer4);
        data.createText("text for indexing", "Doe, Jane");
        lIndexed = this.indexer.refreshIndex(this.monitor);
        assertEquals(3, lIndexed);
        verify(lIndexer4, times(3)).processIndexer(any(IndexerHelper.class), eq(this.indexer.getIndexPath()),
                eq(this.indexer.getLanguage()));
    }

    // ---

    private static class TestIndexer extends RelationsIndexer {

        public TestIndexer() {
            super("test");
        }

        @Override
        protected String getLanguage() {
            return "en";
        }

        @Override
        protected DirectoryFactory getDirectoryFactory() {
            return new TempDirectoryFactory();
        }
    }

}
