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
package org.elbe.relations.search;

import org.eclipse.swt.graphics.Image;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.search.RetrievedItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IAction;
import org.elbe.relations.models.ILightWeightModel;

/**
 * Item that is element of the result set of a search query and knows how to
 * display a type icon.
 * 
 * @author Luthiger
 */
public class RetrievedItemWithIcon extends RetrievedItem implements
		ILightWeightModel {
	private Image typeIcon;

	/**
	 * RetrievedItemWithIcon constructor.
	 * 
	 * @param inID
	 *            {@link UniqueID}
	 * @param inTitle
	 *            String
	 */
	public RetrievedItemWithIcon(final UniqueID inID, final String inTitle) {
		super(inID, inTitle);
		initTypeAttributes(inID.itemType);
	}

	private void initTypeAttributes(final int inType) {
		switch (inType) {
		case IItem.TERM:
			typeIcon = RelationsImages.TERM.getImage();
			break;
		case IItem.TEXT:
			typeIcon = RelationsImages.TEXT.getImage();
			break;
		case IItem.PERSON:
			typeIcon = RelationsImages.PERSON.getImage();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.data.search.RetrievedItem#getImage()
	 */
	@Override
	public Image getImage() {
		return typeIcon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.models.ILightWeightModel#getItemDeleteAction()
	 */
	@Override
	public IAction getItemDeleteAction() {
		// intentionally returning null
		return null;
	}

}
