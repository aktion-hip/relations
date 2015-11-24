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
package org.elbe.relations.data.search;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.elbe.relations.data.Constants;
import org.elbe.relations.data.internal.search.IndexerRegistration;

/**
 * Provides basic functionality for full text search using lucene.
 *
 * @author Luthiger Created on 14.11.2006
 */
public abstract class AbstractSearching {
	public static final String ITEM_TYPE = "itemType"; //$NON-NLS-1$
	public static final String ITEM_ID = "itemID"; //$NON-NLS-1$
	public static final String UNIQUE_ID = "uniqueID"; //$NON-NLS-1$
	public static final String TITLE = "itemTitle"; //$NON-NLS-1$
	public static final String TEXT = "itemText"; //$NON-NLS-1$
	public static final String CONTENT_FULL = "itemFull"; //$NON-NLS-1$
	public static final String DATE_CREATED = "itemDateCreated"; //$NON-NLS-1$
	public static final String DATE_MODIFIED = "itemDateModified"; //$NON-NLS-1$

	private static DirectoryFactory cDirectoryFactory = null;

	private String indexName = ""; //$NON-NLS-1$

	/**
	 * AbstractSearching constructor.
	 *
	 * @param inIndexDir
	 *            String the name of the index, i.e. the directory where the
	 *            index is stored.
	 */
	public AbstractSearching(final String inIndexDir) {
		indexName = inIndexDir;
	}

	protected File getIndexDir() throws IOException {
		return getDirectoryFactory().getDirectory(indexName);
	}

	protected File getIndexContainer() {
		return getDirectoryFactory().getIndexContainer(indexName);
	}

	protected DirectoryFactory getDirectoryFactory() {
		if (cDirectoryFactory == null) {
			try {
				cDirectoryFactory = new FileSystemDirectoryFactory();
			} catch (final IllegalStateException exc) {
				// For testing purpose, we use an index stored in the temporary
				// directory.
				cDirectoryFactory = new TempDirectoryFactory();
			}
		}
		return cDirectoryFactory;
	}

	/**
	 * Returns the number of documents actually indexed.
	 *
	 * @return int Number of documents in the index.
	 * @throws IOException
	 */
	public int numberOfIndexed() throws IOException {
		return getIndexer().numberOfIndexed(getIndexDir());
	}

	/**
	 * @return IIndexer the actually registered <code>IIndexer</code>.
	 */
	protected IIndexer getIndexer() {
		return IndexerRegistration.INSTANCE.getIndexer();
	}

	// --- inner classes ---

	private interface DirectoryFactory {
		File getDirectory(String inIndexName) throws IOException;

		File getIndexContainer(String inIndexName);
	}

	private class FileSystemDirectoryFactory implements DirectoryFactory {
		protected File root;

		public FileSystemDirectoryFactory() {
			root = getRoot();
		}

		@Override
		public File getDirectory(final String inIndexName) throws IOException {
			final File lIndexContainer = checkDir(new File(root, Constants.LUCENE_STORE));
			return checkDir(new File(lIndexContainer, inIndexName));
		}

		private File checkDir(final File inFileToCheck) {
			if (!inFileToCheck.exists()) {
				inFileToCheck.mkdir();
			}
			return inFileToCheck;
		}

		protected File getRoot() {
			return ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		}

		@Override
		public File getIndexContainer(final String inIndexName) {
			final File lIndexContainer = checkDir(new File(root, Constants.LUCENE_STORE));
			return checkDir(new File(lIndexContainer, inIndexName));
		}
	}

	protected class TempDirectoryFactory extends FileSystemDirectoryFactory {
		@Override
		public File getRoot() {
			return new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		}
	}

}
