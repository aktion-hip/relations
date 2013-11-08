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
package org.elbe.relations.data.internal.search;

import java.util.Collection;

import org.elbe.relations.data.search.IIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registration for classes implementing the <code>IIndexer</code> interface.
 * This singleton tells the application whether an indexer bundle is available
 * (or not) and gives access to it.
 * 
 * @author Luthiger
 */
public enum IndexerRegistration {
	INSTANCE;

	private static final Logger LOG = LoggerFactory
			.getLogger(IndexerRegistration.class);

	private IIndexer indexer;

	public void register(final IIndexer inIndexer) {
		indexer = inIndexer;
	}

	public void unregister(final IIndexer inIndexer) {
		indexer = null;
	}

	/**
	 * @return {@link IIndexer} the registered indexer
	 */
	public IIndexer getIndexer() {
		if (indexer == null) {
			final NullPointerException outExc = new NullPointerException();
			LOG.error("indexer is null", outExc); //$NON-NLS-1$
			throw outExc;
		}
		return indexer;
	}

	/**
	 * @return String[] the set of possible languages (ISO Language Codes
	 *         defined in ISO-639)
	 */
	public String[] getContentLanguages() {
		final Collection<String> outLanguages = getIndexer()
				.getAnalyzerLanguages();
		return outLanguages.toArray(new String[outLanguages.size()]);
	}

}
