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

import java.io.File;
import java.io.IOException;

import org.elbe.relations.data.Constants;

/**
 * Utility class for testing purpose, provides helper methods for managing the
 * search index.
 * 
 * @author lbenno
 */
public class IndexHouseKeeper {
	public final static File ROOT = new File(
	        System.getProperty("java.io.tmpdir"));
	public final static String INDEX_DIR = "rel_test";

	private LuceneIndexer index;

	public void setUp() throws IOException {
		index = new LuceneIndexer();
		index.initializeIndex(getDirectory());
		// IndexerRegistration.getInstance().register(index);
	}

	public void tearDown() throws IOException {
		// IndexerRegistration.getInstance().unregister(index);
		index = null;
		deleteContent(getDirectory());
	}

	// ---

	public static File getDirectory() throws IOException {
		final File lIndexContainer = checkDir(new File(ROOT,
		        Constants.LUCENE_STORE));
		return checkDir(new File(lIndexContainer, INDEX_DIR));
	}

	private static File checkDir(final File inFileToCheck) {
		if (!inFileToCheck.exists()) {
			inFileToCheck.mkdir();
		}
		return inFileToCheck;
	}

	private void deleteContent(final File inDirectory) {
		final File[] lContent = inDirectory.listFiles();
		for (int i = 0; i < lContent.length; i++) {
			if (lContent[i].isDirectory()) {
				deleteContent(lContent[i]);
			}
			lContent[i].delete();
		}
		inDirectory.delete();
	}

}
