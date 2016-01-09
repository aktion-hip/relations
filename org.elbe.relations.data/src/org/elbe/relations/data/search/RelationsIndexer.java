/**
This package is part of Relations project.
Copyright (C) 2006-2016, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.elbe.relations.data.search;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.elbe.relations.data.Messages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.exc.VException;

/**
 * Class to be used to (re-) create the search index on the current database
 * items.
 *
 * @author Luthiger Created on 14.11.2006
 */
public abstract class RelationsIndexer extends AbstractSearching {
	private final static int CHUNK_SIZE = 50;
	private static final String INDEX_INDICATOR = "segments";

	/**
	 * RelationsIndexer constructor
	 *
	 * @param String
	 *            The name of the index (i.e. the database name)
	 */
	public RelationsIndexer(final String inIndexDir) {
		super(inIndexDir);
	}

	/**
	 * Refreshes the search index for the current database.
	 *
	 * @param inMonitor
	 *            IProgressMonitor
	 * @return int number of indexed items
	 * @throws IOException
	 * @throws VException
	 * @throws SQLException
	 */
	public int refreshIndex(final IProgressMonitor inMonitor) throws IOException, VException, SQLException {
		return doIndex(new IndexerHelper(), inMonitor, getIndexer());
	}

	protected int doIndex(final IndexerHelper inIndexHelper, final IProgressMonitor inMonitor, final IIndexer inIndexer)
			throws VException, SQLException, IOException {
		final SubMonitor lProgress = SubMonitor.convert(inMonitor, 100);
		int outIndexed = 0;
		inIndexer.initializeIndex(getIndexDir(), getLanguage());

		inMonitor.subTask(Messages.getString("RelationsIndexer.task.term")); //$NON-NLS-1$
		outIndexed += indexTerms(inIndexHelper, lProgress.newChild(34), inIndexer);
		if (inMonitor.isCanceled()) {
			return outIndexed;
		}

		inMonitor.subTask(Messages.getString("RelationsIndexer.task.text")); //$NON-NLS-1$
		outIndexed += indexTexts(inIndexHelper, lProgress.newChild(33), inIndexer);
		if (inMonitor.isCanceled()) {
			return outIndexed;
		}

		inMonitor.subTask(Messages.getString("RelationsIndexer.task.person")); //$NON-NLS-1$
		outIndexed += indexPersons(inIndexHelper, lProgress.newChild(33), inIndexer);
		return outIndexed;
	}

	private int indexTerms(final IndexerHelper inIndexHelper, final IProgressMonitor inMonitor,
			final IIndexer inIndexer) throws VException, SQLException, IOException {
		return processSelection(inIndexHelper, BOMHelper.getTermHome(), inMonitor, inIndexer);
	}

	private int indexTexts(final IndexerHelper inIndexHelper, final IProgressMonitor inMonitor,
			final IIndexer inIndexer) throws VException, SQLException, IOException {
		return processSelection(inIndexHelper, BOMHelper.getTextHome(), inMonitor, inIndexer);
	}

	private int indexPersons(final IndexerHelper inIndexHelper, final IProgressMonitor inMonitor,
			final IIndexer inIndexer) throws VException, SQLException, IOException {
		return processSelection(inIndexHelper, BOMHelper.getPersonHome(), inMonitor, inIndexer);
	}

	protected int processSelection(final IndexerHelper inIndexHelper, final GeneralDomainObjectHome inHome,
			final IProgressMonitor inMonitor, final IIndexer inIndexer) throws VException, SQLException, IOException {
		final SubMonitor lProgress = SubMonitor.convert(inMonitor, 100);
		int outNumberOfIndexed = 0;
		final QueryResult lResult = inHome.select();
		while (lResult.hasMoreElements()) {
			final IIndexable lIndexable = (IIndexable) lResult.nextAsDomainObject();
			lIndexable.indexContent(inIndexHelper);
			((DomainObject) lIndexable).release();

			outNumberOfIndexed++;
			if (outNumberOfIndexed % CHUNK_SIZE == 0) {
				// we let process/index the prepared documents in chunks of
				// CHUNK_SIZE
				processIndexer(inIndexer, inIndexHelper);
			}
			lProgress.worked(1);
			if (lProgress.isCanceled()) {
				return outNumberOfIndexed;
			}
		}
		processIndexer(inIndexer, inIndexHelper);
		return outNumberOfIndexed;
	}

	private void processIndexer(final IIndexer inIndexer, final IndexerHelper inIndexHelper) throws IOException {
		inIndexer.processIndexer(inIndexHelper, getIndexDir(), getLanguage());
		inIndexHelper.reset();
	}

	/**
	 * Adds an <code>Indexable</code> to this search index. This method has to
	 * be called when a new instance of an Indexable object is created, i.e.
	 * stored in the database.
	 *
	 * @param inIndexable
	 *            {@link IIndexable}
	 * @throws BOMException
	 * @throws IOException
	 * @throws BOMException
	 * @throws IOException
	 */
	public void addToIndex(final IIndexable inIndexable) throws BOMException, IOException {
		final IndexerHelper lIndexer = new IndexerHelper();
		try {
			inIndexable.indexContent(lIndexer);
			getIndexer().processIndexer(lIndexer, getIndexDir(), getLanguage());
		} catch (final VException exc) {
			throw new BOMException(exc);
		}
	}

	/**
	 * Deletes the item with the specified unique ID from this search index.
	 *
	 * @param inUniqueID
	 *            String of form <code>ItemType:ItemID</code>
	 * @throws IOException
	 */
	public void deleteItemInIndex(final String inUniqueID) throws IOException {
		getIndexer().deleteItemInIndex(inUniqueID, AbstractSearching.UNIQUE_ID, getIndexDir(), getLanguage());
	}

	/**
	 * Refreshes the item's search term in the search index.
	 *
	 * @param inItem
	 *            IItem
	 * @throws IOException
	 * @throws BOMException
	 * @throws VException
	 */
	public void refreshItemInIndex(final IItem inItem) throws IOException, BOMException, VException {
		final String lUniqueID = UniqueID.getStringOf(inItem.getItemType(), inItem.getID());
		deleteItemInIndex(lUniqueID);
		addToIndex((IIndexable) inItem);
	}

	/**
	 * Convenience method: initialize the index directory for newly created
	 * databases.
	 *
	 * @throws IOException
	 */
	public void initializeIndex() throws IOException {
		getIndexer().initializeIndex(getIndexDir(), getLanguage());
	}

	/**
	 * Convenience method: checks whether there's yet an index with the
	 * specified indexDir.
	 *
	 * @return <code>true</code> if a search index for the specified indexDir
	 *         exists yet.
	 */
	public boolean isIndexAvailable() {
		final File indexContainer = getIndexContainer();
		if (!indexContainer.exists()) {
			return false;
		}
		final String[] content = indexContainer.list();
		if (content == null || content.length == 0) {
			return false;
		}
		for (int i = 0; i < content.length; i++) {
			if (content[i].startsWith(INDEX_INDICATOR)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return String the language
	 */
	abstract protected String getLanguage();

}
