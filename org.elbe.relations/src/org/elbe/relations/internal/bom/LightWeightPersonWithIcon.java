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
package org.elbe.relations.internal.bom;

import java.sql.Timestamp;

import org.eclipse.swt.graphics.Image;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.LightWeightPerson;
import org.elbe.relations.db.IAction;
import org.elbe.relations.models.ILightWeightModel;

/**
 * A lightweight version of the <code>Person</code> model (with icon).
 * 
 * @author Luthiger
 */
public class LightWeightPersonWithIcon extends LightWeightPerson implements
        ILightWeightModel {

	/**
	 * LightWeightPersonWithIcon constructor.
	 * 
	 * @param inID
	 * @param inName
	 * @param inFirstname
	 * @param inText
	 * @param inFrom
	 * @param inTo
	 * @param inCreated
	 * @param inModified
	 */
	public LightWeightPersonWithIcon(final long inID, final String inName,
	        final String inFirstname, final String inText, final String inFrom,
	        final String inTo, final Timestamp inCreated,
	        final Timestamp inModified) {
		super(inID, inName, inFirstname, inText, inFrom, inTo, inCreated,
		        inModified);
	}

	/**
	 * LightWeightPersonWithIcon constructor.
	 * 
	 * @param inPerson
	 *            {@link LightWeightPerson}
	 */
	public LightWeightPersonWithIcon(final LightWeightPerson inPerson) {
		super(inPerson.id, inPerson.name, inPerson.firstname, inPerson.text,
		        inPerson.from, inPerson.to, inPerson.created, inPerson.modified);
	}

	@Override
	public Image getImage() {
		return RelationsImages.PERSON.getImage();
	}

	@Override
	public IAction getItemDeleteAction() {
		return new IAction() {
			@Override
			public void run() throws BOMException {
				BOMHelper.getPersonHome().deleteItem(getID());
			}
		};
	}

}
