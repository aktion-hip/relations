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
package org.elbe.relations.data.utility;

import org.elbe.relations.data.bom.IItem;
import org.hip.kernel.exc.VException;

/**
 * Utility class for an item's unique ID that consists of item type and item ID.
 * 
 * @author Benno Luthiger Created on 09.05.2006
 */
public class UniqueID {
	private static final String TEMPLATE = "%s:%s"; //$NON-NLS-1$

	public int itemType;
	public long itemID;

	/**
	 * UniqueID constructor from itemType/itemID.
	 * 
	 * @param inItemType
	 *            int
	 * @param inID
	 *            long
	 */
	public UniqueID(final int inItemType, final long inID) {
		super();
		itemType = inItemType;
		itemID = inID;
	}

	/**
	 * UniqueID constructor from a String.
	 * 
	 * @param inUniqueID
	 *            String of form <code>itemType:itemID</code>
	 */
	public UniqueID(final String inUniqueID) {
		super();
		final int lIndex = inUniqueID.indexOf(":"); //$NON-NLS-1$
		itemType = Integer.parseInt(inUniqueID.substring(0, lIndex));
		itemID = Integer.parseInt(inUniqueID.substring(lIndex + 1));
	}

	@Override
	public String toString() {
		return getStringOf(itemType, itemID);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * UniqueIDs are equal if they're of equal type and have the same item ID.
	 */
	@Override
	public boolean equals(final Object inObject) {
		if (inObject == null)
			return false;
		if (inObject instanceof UniqueID) {
			final UniqueID lTest = (UniqueID) inObject;
			return itemType == lTest.itemType && itemID == lTest.itemID;
		}
		return false;
	}

	/**
	 * Convenience method that returns the string version of a UniqueID.
	 * 
	 * @param inItemType
	 *            int
	 * @param inID
	 *            long
	 * @return String
	 */
	public static String getStringOf(final int inItemType, final long inID) {
		return String.format(TEMPLATE, inItemType, inID);
	}
	
	/**
	 * Factory method: creates a <code>UniqueID</code> from the specified <code>IItem</code>.
	 * 
	 * @param inItem {@link IItem}
	 * @return {@link UniqueID} or an empty String in case of an error
	 */
	public static UniqueID createUniqueID(IItem inItem) {
		try {
			return new UniqueID(inItem.getItemType(), inItem.getID());
		} catch (VException exc) {
			// intentionally left empty
		}
		return new NullUniqueID();
	}
	
//	--- private classes ---
	
	private static class NullUniqueID extends UniqueID {

		public NullUniqueID() {
			super(0, 0);
		}
		
		@Override
		public String toString() {
			return ""; //$NON-NLS-1$
		}
	}

}
