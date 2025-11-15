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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Utility class for testing purpose, provides helper methods for managing the
 * search index.
 *
 * @author lbenno
 */
public class IndexHouseKeeper {
    public final static String INDEX_DIR = "rel_test";
    public static final String LANGUAGE = Locale.ENGLISH.getLanguage();

    private LuceneIndexer index;

    public void setUp(final Path tempDir) throws IOException {
        this.index = new LuceneIndexer();
        this.index.initializeIndex(tempDir, LANGUAGE);
        // IndexerRegistration.getInstance().register(index);
    }

    public void tearDown(final Path tempDir) throws IOException {
        // IndexerRegistration.getInstance().unregister(index);
        this.index = null;
        // Files.delete(tempDir); not needed, as tempDir is JUnit @TempDir
    }

}
