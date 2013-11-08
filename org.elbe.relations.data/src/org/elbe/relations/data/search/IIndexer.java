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
import java.util.Collection;
import java.util.List;

import org.elbe.relations.data.utility.RException;

/**
 * OSGi service definition describing the Relations indexer service. Bundles
 * that provide an indexer must provide a class implementing this interface.
 * 
 * @author Luthiger
 */
public interface IIndexer {

	/**
	 * Process the specified <code>Indexer</code>.
	 * 
	 * @param inIndexer
	 *            Indexer
	 * @param inIndexDir
	 *            File the directory where the search index is stored.
	 * @param inLanguage
	 *            String ISO Language Code defined in ISO-639.
	 * @throws IOException
	 */
	public void processIndexer(IndexerHelper inIndexer, File inIndexDir,
			String inLanguage) throws IOException;

	/**
	 * Process the specified <code>Indexer</code>.
	 * 
	 * @param inIndexer
	 *            Indexer
	 * @param inIndexDir
	 *            File the directory where the search index is stored.
	 * @param inLanguage
	 *            String ISO Language Code defined in ISO-639.
	 * @param inCreate
	 *            boolean <code>true</code> to create the index or overwrite the
	 *            existing one; <code>false</code> to append to the existing
	 *            index
	 * @throws IOException
	 */
	public void processIndexer(IndexerHelper inIndexer, File inIndexDir,
			String inLanguage, boolean inCreate) throws IOException;

	/**
	 * Returns the number of documents actually indexed.
	 * 
	 * @param inIndexDir
	 *            File the directory where the search index is stored.
	 * @return int Number of documents in the index.
	 * @throws IOException
	 */
	public int numberOfIndexed(File inIndexDir) throws IOException;

	/**
	 * Returns the collection of languages for which the indexer bundle can
	 * provide analyzers.
	 * 
	 * @return Collection<String> languages, i.e. ISO Language Codes defined in
	 *         ISO-639.
	 */
	public Collection<String> getAnalyzerLanguages();

	/**
	 * Deletes the item with the specified unique ID from this search index.
	 * 
	 * @param inUniqueID
	 *            String the item's unique ID
	 * @param inFieldName
	 *            String the lucene field name identifying the ID field
	 * @param inIndexDir
	 *            File the directory where the search index is stored.
	 * @throws IOException
	 */
	public void deleteItemInIndex(String inUniqueID, String inFieldName,
			File inIndexDir) throws IOException;

	/**
	 * Convenience method: initialize the specified index directory.
	 * 
	 * @param inIndexDir
	 *            File the directory where the search index is stored.
	 * @throws IOException
	 */
	public void initializeIndex(File inIndexDir) throws IOException;

	/**
	 * Searches the indexed items using the specified search query.
	 * 
	 * @param inQueryTerm
	 *            String
	 * @param inIndexDir
	 *            File the directory where the search index is stored.
	 * @param inLanguage
	 *            String ISO Language Code defined in ISO-639.
	 * @param inMaxHits
	 *            int maximal number of hits.
	 * @return List<RetrievedItem> the search result
	 * @throws IOException
	 * @throws RException
	 */
	public List<RetrievedItem> search(String inQueryTerm, File inIndexDir,
			String inLanguage, int inMaxHits) throws IOException, RException;

}
