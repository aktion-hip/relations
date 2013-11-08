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
package org.elbe.relations.db;

import java.util.Collection;

import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.bom.LightWeightPerson;
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.data.bom.LightWeightText;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.ILightWeightModel;

/**
 * Interface for all data handling concerns.<br />
 * An implementation of this interface can be retrieved from the
 * <code>IEclipseContext</code>.
 * 
 * @author Luthiger
 */
public interface IDataService {

	/**
	 * @return List&lt;ILightWeightModel> all term items
	 */
	Collection<ILightWeightModel> getTerms();

	/**
	 * @return List&lt;ILightWeightModel> all text items
	 */
	Collection<ILightWeightModel> getTexts();

	/**
	 * @return List&lt;ILightWeightModel> all person items
	 */
	Collection<ILightWeightModel> getPersons();

	/**
	 * @return List&lt;ILightWeightModel> all items
	 */
	Collection<ILightWeightModel> getAll();

	/**
	 * Adds the newly created term item to the relevant collections and sends a
	 * notification.
	 * 
	 * @param inTerm
	 *            {@link LightWeightTerm}
	 */
	void loadNew(final LightWeightTerm inTerm);

	/**
	 * Adds the newly created text item to the relevant collections and sends a
	 * notification.
	 * 
	 * @param inText
	 *            {@link LightWeightText}
	 */
	void loadNew(final LightWeightText inText);

	/**
	 * Adds the newly created person item to the relevant collections and sends
	 * a notification.
	 * 
	 * @param inPerson
	 *            {@link LightWeightPerson}
	 */
	void loadNew(final LightWeightPerson inPerson);

	/**
	 * Loads the data from the configured data store.
	 * 
	 * @param inEventTopic
	 *            String the event topic to post after data loading has been
	 *            done
	 */
	void loadData(final String inEventTopic);

	/**
	 * Retrieves an item with an UniqueID.
	 * 
	 * @param inID
	 *            {@link UniqueID}
	 * @return {@link IItemModel}
	 * @throws BOMException
	 */
	IItemModel retrieveItem(final UniqueID inID) throws BOMException;

	/**
	 * Removes the deleted item from the relevant collections and notifies the
	 * listeners.
	 * 
	 * @param inItem
	 *            ILightWeightItem
	 */
	void removeDeleted(final ILightWeightItem inItem);

	/**
	 * Convenience method for StatusLine.
	 * 
	 * @return int The number of items in the database.
	 */
	int getNumberOfItems();

	/**
	 * Convenience method for StatusLine.
	 * 
	 * @return String e.g. <i>jdbc:mysql://localhost/relations</i>
	 */
	String getDBName();

}
