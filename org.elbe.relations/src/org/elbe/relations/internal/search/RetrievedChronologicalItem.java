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
package org.elbe.relations.internal.search;

import java.sql.Timestamp;

import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.search.RetrievedItemWithIcon;
import org.hip.kernel.bom.AlternativeModel;

/**
 * Item that is element of the result set of a search query and knows how to
 * display a type icon.
 * 
 * @author Luthiger Created on 02.04.2009
 */
public class RetrievedChronologicalItem extends RetrievedItemWithIcon implements
		AlternativeModel {

	private final Timestamp dtCreation;
	private final Timestamp dtMutation;

	/**
	 * @param inID
	 * @param inTitle
	 * @param inCreationDate
	 * @param inMutationDate
	 */
	public RetrievedChronologicalItem(final UniqueID inID,
			final String inTitle, final Timestamp inCreationDate,
			final Timestamp inMutationDate) {
		super(inID, inTitle);
		dtCreation = inCreationDate;
		dtMutation = inMutationDate;
	}

	public Timestamp getCreationDate() {
		return dtCreation;
	}

	public Timestamp getMutationDate() {
		return dtMutation;
	}

}
