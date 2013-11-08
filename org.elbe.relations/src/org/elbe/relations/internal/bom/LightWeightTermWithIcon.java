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
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.db.IAction;
import org.elbe.relations.models.ILightWeightModel;

/**
 * A lightweight version of the <code>Term</code> model (with icon).
 * 
 * @author Luthiger
 */
public class LightWeightTermWithIcon extends LightWeightTerm implements
		ILightWeightModel {

	/**
	 * LightWeightTermWithIcon constructor.
	 * 
	 * @param inID
	 * @param inTitle
	 * @param inText
	 * @param inCreated
	 * @param inModified
	 */
	public LightWeightTermWithIcon(final long inID, final String inTitle,
			final String inText, final Timestamp inCreated,
			final Timestamp inModified) {
		super(inID, inTitle, inText, inCreated, inModified);
	}

	/**
	 * LightWeightTermWithIcon constructor.
	 * 
	 * @param inTerm
	 *            {@link LightWeightTerm} the adapted term
	 */
	public LightWeightTermWithIcon(final LightWeightTerm inTerm) {
		super(inTerm.id, inTerm.title, inTerm.text, inTerm.created,
				inTerm.modified);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.models.ILightWeightModel#getImage()
	 */
	@Override
	public Image getImage() {
		return RelationsImages.TERM.getImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.models.ILightWeightModel#getItemDeleteAction()
	 */
	@Override
	public IAction getItemDeleteAction() {
		return new IAction() {
			@Override
			public void run() throws BOMException {
				BOMHelper.getTermHome().deleteItem(getID());
			}
		};
	}

}
