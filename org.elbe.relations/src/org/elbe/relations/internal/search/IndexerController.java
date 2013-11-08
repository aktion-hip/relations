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
package org.elbe.relations.internal.search;

import java.util.Collection;

import org.elbe.relations.data.search.IIndexer;

/**
 * Controller class, i.e. OSGi client for the <code>IIndexer</code> service.
 * Instances of <code>IIndexer</code> are registered here.
 * 
 * @author Luthiger
 */
public class IndexerController {

	private IIndexer indexer;

	/**
	 * Registers the specified indexer instance.
	 * 
	 * @param inIndexer
	 *            {@link IIndexer}
	 */
	public void register(final IIndexer inIndexer) {
		indexer = inIndexer;
	}

	/**
	 * Unregisters the indexer instance.
	 * 
	 * @param inIndexer
	 *            {@link IIndexer}
	 */
	public void unregister(final IIndexer inIndexer) {
		indexer = null;
	}

	/**
	 * Returns an array of possible content languages for that the user can
	 * select one.
	 * 
	 * @return String[] the set of possible languages (ISO Language Codes
	 *         defined in ISO-639).
	 */
	public String[] getContentLanguages() {
		final Collection<String> outLanguages = indexer.getAnalyzerLanguages();
		return outLanguages.toArray(new String[outLanguages.size()]);
	}

}
