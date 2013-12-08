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

import java.io.IOException;
import java.util.Locale;

import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.hip.kernel.exc.VException;

/**
 * A no operation indexer as default implementation for abstract items.
 * 
 * @author Luthiger
 */
public class NoOpIndexer extends RelationsIndexer {

	/**
	 * NoOpIndexer constuctor.
	 */
	public NoOpIndexer() {
		super(""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.data.search.RelationsIndexer#getLanguage()
	 */
	@Override
	protected String getLanguage() {
		return Locale.ENGLISH.getLanguage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.data.search.RelationsIndexer#refreshItemInIndex(org
	 * .elbe.relations.data.bom.IItem)
	 */
	@Override
	public void refreshItemInIndex(final IItem inItem) throws IOException,
			BOMException, VException {
		// intentionally do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.data.search.RelationsIndexer#addToIndex(org.elbe.relations
	 * .data.search.IIndexable)
	 */
	@Override
	public void addToIndex(final IIndexable inIndexable) throws BOMException,
			IOException {
		// intentionally do nothing
	}

}
