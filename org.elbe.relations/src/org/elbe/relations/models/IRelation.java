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
 * Interface for a relation between source (i.e. central) and target item.
 * 
 * @author Benno Luthiger
 */
public interface IRelation {
	/**
	 * Setter for source item.
	 * 
	 * @param inItem
	 *            {@link IItem}
	 */
	void setSourceItem(IItem inItem);

	/**
	 * Getter for source item.
	 * 
	 * @return {@link IItem}
	 */
	IItem getSourceItem();

	/**
	 * Setter for target item.
	 * 
	 * @param inItem
	 *            {@link IItem}
	 */
	void setTargetItem(IItem inItem);

	/**
	 * Getter for target item.
	 * 
	 * @return {@link IItem}
	 */
	IItem getTargetItem();

	/**
	 * Returns the relation's ID.
	 * 
	 * @return long the relation's ID.
	 */
	long getRelationID();
}
