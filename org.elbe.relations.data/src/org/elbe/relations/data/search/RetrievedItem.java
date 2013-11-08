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

import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.IItemFactory;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.exc.VException;

/**
 * Item that is element of the result set of a search query.
 * 
 * @author Luthiger Created on 09.12.2006
 * @see ILightWeightItem
 */
public abstract class RetrievedItem implements ILightWeightItem {
	private final long id;
	private final String title;
	private final int type;
	private IItemFactory factory;

	/**
	 * RetrievedItem constructor
	 * 
	 * @param inID
	 *            UniqueID
	 * @param inTitle
	 *            String
	 */
	public RetrievedItem(final UniqueID inID, final String inTitle) {
		id = inID.itemID;
		type = inID.itemType;
		title = inTitle;
		initTypeAttributes(type);
	}

	private void initTypeAttributes(final int inType) {
		switch (inType) {
		case IItem.TERM:
			factory = BOMHelper.getTermHome();
			break;
		case IItem.TEXT:
			factory = BOMHelper.getTextHome();
			break;
		case IItem.PERSON:
			factory = BOMHelper.getPersonHome();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.bom.ILightWeightItem#getID()
	 */
	@Override
	public long getID() {
		return id;
	}

	@Override
	public String toString() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.bom.ILightWeightItem#getItemType()
	 */
	@Override
	public int getItemType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.bom.ILightWeightItem#getCreated()
	 */
	@Override
	public String getCreated() throws VException {
		return null;
	}

	/**
	 * Creates a full fledged item out of this lightweight item.
	 * 
	 * @return IItem
	 * @throws BOMException
	 */
	public IItem getItem() throws BOMException {
		return factory.getItem(id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(final Object inObject) {
		if (this == inObject)
			return true;
		if (inObject == null)
			return false;
		if (getClass() != inObject.getClass())
			return false;
		final RetrievedItem lOther = (RetrievedItem) inObject;
		if (id != lOther.getID())
			return false;
		if (type != lOther.getItemType())
			return false;
		return true;
	}

}
