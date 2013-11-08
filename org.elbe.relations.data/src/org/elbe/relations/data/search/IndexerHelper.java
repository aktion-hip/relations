/*
This package is part of Relations project.
Copyright (C) 2007, Benno Luthiger

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

import java.util.Collection;
import java.util.Vector;

/**
 * Helper class, wrapper for lucene IndexWriter
 *
 * @author Luthiger
 * Created on 20.11.2008
 */
public class IndexerHelper {
	private Collection<IndexerDocument> documents = new Vector<IndexerDocument>();
	
	public void addDocument(IndexerDocument inDocument) {
		documents.add(inDocument);
	}
	
	public Collection<IndexerDocument> getDocuments() {
		return documents;
	}

	/**
	 * Resets the helper, i.e. initializes the documents collection with a new <code>Collection</code>.
	 */
	public void reset() {
		documents = new Vector<IndexerDocument>();
	} 
}
