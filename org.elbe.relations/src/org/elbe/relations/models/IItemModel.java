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

import java.util.List;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.db.IAction;
import org.elbe.relations.internal.services.IItemEditWizard;

/**
 * Interface to decorate an IItem object. Models in the relations view have to
 * implement this interface.
 * 
 * @author Benno Luthiger
 */
@SuppressWarnings("restriction")
public interface IItemModel extends IItem {

	/**
	 * Add a relation this item is source of.
	 * 
	 * @param inRelation
	 *            IRelation
	 */
	void addSource(IRelation inRelation);

	/**
	 * Adds a relation this item is target of.
	 * 
	 * @param inRelation
	 */
	void addTarget(IRelation inRelation);

	/**
	 * @return List<IRelation> of IRelation this item is source.
	 */
	List<IRelation> getSources();

	/**
	 * @return List<IRelation> of IRelation this item is target.
	 */
	List<IRelation> getTargets();

	/**
	 * The item's icon.
	 * 
	 * @return Image
	 */
	Image getImage();

	/**
	 * Returns the wizard class to edit this item.
	 * 
	 * @return Class&lt;? extends IItemEditWizard>
	 */
	Class<? extends IItemEditWizard> getItemEditWizard();

	/**
	 * Factory method: Returns the item delete action appropriate for this item.
	 * 
	 * @param inLog
	 *            {@link Logger}
	 * @return IAction
	 */
	IAction getItemDeleteAction(Logger inLog);

}
