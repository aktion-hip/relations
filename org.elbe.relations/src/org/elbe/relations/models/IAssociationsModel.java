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

import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.utility.UniqueID;

/**
 * Interface for the model for the associations, i.e. the relations of an item.
 * 
 * @author Benno Luthiger Created on 09.05.2006
 */
public interface IAssociationsModel {

	/**
	 * Returns the associated items as array of IItem.
	 * 
	 * @return Object[] Array of <code>ItemAdapter</code>.
	 */
	Object[] getElements();

	/**
	 * Filters the specified item against the associated items.
	 * 
	 * @param inItem
	 *            ILightWeightItem
	 * @return <code>true</code> if element is included in the filtered set, and
	 *         <code>false</code> if excluded
	 */
	boolean select(ILightWeightItem inItem);

	/**
	 * Add new associations.
	 * 
	 * @param inAssociations
	 *            Object[] Array of <code>ILightWeightItem</code>s.
	 */
	void addAssociations(Object[] inAssociations);

	/**
	 * Add new associations.
	 * 
	 * @param inAssociations
	 *            UniqueID[]
	 */
	void addAssociations(UniqueID[] inAssociations);

	/**
	 * Removes the specified associations.
	 * 
	 * @param inObjects
	 *            Object[]
	 */
	void removeAssociations(Object[] inObjects);

	void removeAssociations(UniqueID[] inObjects);

	/**
	 * Removes the specified relation from this model.
	 * 
	 * @param inRelation
	 *            IRelation
	 */
	void removeRelation(IRelation inRelation);

	/**
	 * Store changes made during display of dialog.
	 * 
	 * @throws BOMException
	 */
	void saveChanges() throws BOMException;

	/**
	 * Undo changes if the user quitted the dialog without save.
	 * 
	 * @throws BOMException
	 */
	void undoChanges() throws BOMException;

	/**
	 * Checks whether the specified associatens exist already in this item's
	 * associations.
	 * 
	 * @param inAssociations
	 *            UniqueID[]
	 * @return boolean <code>true</code> if all ids are associated,
	 *         <code>false</code> if at least one item is not associated yet.
	 */
	boolean isAssociated(UniqueID[] inAssociations);

	/**
	 * Checks whether the specified id exists already in this item's
	 * associations.
	 * 
	 * @param inID
	 *            UniqueID
	 * @return boolean <code>true</code> if the specified ID is an association.
	 */
	public boolean isAssociated(UniqueID inID);

}
