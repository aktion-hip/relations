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
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.utility.SelectedItemChangeEvent;

/**
 * Interface for relations browser views, i.e. views that display the relations
 * between the items for that they can be browsed.
 * 
 * @author Luthiger
 */
public interface IRelationsBrowser {

	// /**
	// * Sets the browser view's visible state.
	// *
	// * @param inVisible
	// * boolean
	// */
	// public void setVisible(boolean inVisible);
	//
	// /**
	// * @return String the browser view's id.
	// */
	// public String getViewId();

	/**
	 * Sets the model to the view. This method combines the methods
	 * setInput(model), reveal(model.getCenter()) and setFocus().
	 * <p>
	 * Listens for the topic
	 * <code>RelationsConstants.TOPIC_BROWSER_MANAGER_SEND_CENTER_MODEL</code>.
	 * </p>
	 * 
	 * @param inRelated
	 *            {@link CentralAssociationsModel}
	 */
	void setModel(CentralAssociationsModel inRelated);

	/**
	 * Synchronizes the browser: sets the selected model.
	 * <p>
	 * Listens for the topic
	 * <code>RelationsConstants.TOPIC_BROWSER_MANAGER_SYNC_SELECTED</code>.
	 * </p>
	 * 
	 * @param inEvent
	 *            {@link SelectedItemChangeEvent}
	 */
	void syncSelected(SelectedItemChangeEvent inEvent);

	/**
	 * <p>
	 * Listens for the topic
	 * <code>RelationsConstants.TOPIC_BROWSER_MANAGER_SYNC_CONTENT</code>.
	 * </p>
	 * 
	 * @param inItem
	 *            {@link ItemAdapter} the item with the changed content, i.e.
	 *            title
	 */
	void syncContent(ItemAdapter inItem);

	/**
	 * Sets the displayed items font to the specified size.
	 * 
	 * @param inFontSize
	 *            int new font size (pt)
	 */
	void trackFontSize(final int inFontSize);

	// /**
	// * Set the focus to this browser view.
	// */
	// public void setFocus();

}
