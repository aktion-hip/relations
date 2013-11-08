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
package org.elbe.relations.models;

import org.elbe.relations.data.bom.IItem;

/**
 * Wrapper for a relation between two items.
 * 
 * @author Benno Luthiger
 */
public class RelationWrapper implements IRelation {
	private final long relationID;
	private IItem source;
	private IItem target;

	/**
	 * @param inRelationID
	 *            long
	 */
	public RelationWrapper(final long inRelationID) {
		super();
		relationID = inRelationID;
	}

	@Override
	public void setSourceItem(final IItem inItem) {
		source = inItem;
	}

	@Override
	public IItem getSourceItem() {
		return source;
	}

	@Override
	public void setTargetItem(final IItem inItem) {
		target = inItem;
	}

	@Override
	public IItem getTargetItem() {
		return target;
	}

	/**
	 * @see IRelation#getRelationID()
	 */
	@Override
	public long getRelationID() {
		return relationID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int lPrime = 31;
		int outHash = 1;
		outHash = lPrime * outHash + (int) (relationID ^ (relationID >>> 32));
		return outHash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object inObj) {
		if (this == inObj)
			return true;
		if (inObj == null)
			return false;
		if (getClass() != inObj.getClass())
			return false;
		final RelationWrapper lOther = (RelationWrapper) inObj;
		if (relationID != lOther.relationID)
			return false;
		return true;
	}

}
