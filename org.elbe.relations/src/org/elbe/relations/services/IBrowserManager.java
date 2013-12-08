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
package org.elbe.relations.services;

import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.IRelation;
import org.elbe.relations.models.ItemAdapter;
import org.hip.kernel.exc.VException;

/**
 * Interface for an Relations browser manager component. This interface defines
 * an OSGi declarative service.
 * 
 * @author Luthiger
 */
public interface IBrowserManager {
	static final String STORE_CENTRAL_KEY = "relations.browser.center"; //$NON-NLS-1$
	
	/**
	 * Returns the browsers' central model.
	 * 
	 * @return {@link CentralAssociationsModel}
	 */
	CentralAssociationsModel getCenterModel();

	/**
	 * Returns the model actually selected in the browsers.
	 * 
	 * @return {@link ItemAdapter} the selected model in the browser view, may
	 *         be <code>null</code>
	 */
	ItemAdapter getSelectedModel();

	/**
	 * Set a new center to the browser manager.
	 * 
	 * @param inModel
	 *            {@link CentralAssociationsModel}
	 */
	void setModel(final CentralAssociationsModel inModel);

	/**
	 * Checks whether the browsers have to be refreshed after an item has been
	 * deleted.
	 * 
	 * @param inItem
	 *            {@link IItemModel} the deleted item
	 * @throws VException
	 */
	void checkAfterDeletion(final IItemModel inItem) throws VException;

	/**
	 * @return {@link IRelation} the relation actually selected or
	 *         <code>null</code>
	 */
	IRelation getSelectedRelation();

	boolean hasPrevious();

	void moveBack();

	boolean hasNext();

	void moveForward();

}
