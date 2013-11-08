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
package org.elbe.relations.data.bom;

import org.elbe.relations.data.search.NoOpIndexer;
import org.elbe.relations.data.search.RelationsIndexer;
import org.hip.kernel.bom.impl.DomainObjectHomeImpl;

/**
 * Base class for the models' home classes.
 * 
 * @author Luthiger
 */
@SuppressWarnings("serial")
public abstract class AbstractHome extends DomainObjectHomeImpl {

	private RelationsIndexer indexer = null;

	/**
	 * Returns a default implementation of the <code>RelationsIndexer</code>
	 * which does nothing. Subclasses have to override.
	 * 
	 * @return {@link RelationsIndexer}
	 */
	protected RelationsIndexer getIndexer() {
		return indexer == null ? new NoOpIndexer() : indexer;
	}

	/**
	 * @param inIndexer
	 *            {@link RelationsIndexer} the indexer to use when indexing the
	 *            item
	 */
	public void setIndexer(final RelationsIndexer inIndexer) {
		indexer = inIndexer;
	}

}
