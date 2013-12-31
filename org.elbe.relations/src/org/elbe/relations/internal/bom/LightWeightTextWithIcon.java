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
import org.elbe.relations.data.bom.LightWeightText;
import org.elbe.relations.db.IAction;
import org.elbe.relations.models.ILightWeightModel;

/**
 * A lightweight version of the <code>Text</code> model (with icon).
 * 
 * @author Luthiger
 */
public class LightWeightTextWithIcon extends LightWeightText implements
        ILightWeightModel {

	/**
	 * LightWeightTextWithIcon constructor.
	 * 
	 * @param inID
	 * @param inTitle
	 * @param inText
	 * @param inAuthor
	 * @param inCoAuthor
	 * @param inSubtitle
	 * @param inYear
	 * @param inPublication
	 * @param inPages
	 * @param inVolume
	 * @param inNumber
	 * @param inPublisher
	 * @param inPlace
	 * @param inType
	 * @param inCreated
	 * @param inModified
	 */
	public LightWeightTextWithIcon(final long inID, final String inTitle,
	        final String inText, final String inAuthor,
	        final String inCoAuthor, final String inSubtitle,
	        final String inYear, final String inPublication,
	        final String inPages, final int inVolume, final int inNumber,
	        final String inPublisher, final String inPlace, final int inType,
	        final Timestamp inCreated, final Timestamp inModified) {
		super(inID, inTitle, inText, inAuthor, inCoAuthor, inSubtitle, inYear,
		        inPublication, inPages, inVolume, inNumber, inPublisher,
		        inPlace, inType, inCreated, inModified);
	}

	/**
	 * LightWeightTextWithIcon constructor.
	 * 
	 * @param inText
	 *            {@link LightWeightText}
	 */
	public LightWeightTextWithIcon(final LightWeightText inText) {
		super(inText.id, inText.title, inText.text, inText.author,
		        inText.coauthor, inText.subtitle, inText.year,
		        inText.publication, inText.pages, inText.volume, inText.number,
		        inText.publisher, inText.place, inText.type, inText.created,
		        inText.modified);
	}

	@Override
	public Image getImage() {
		return RelationsImages.TEXT.getImage();
	}

	@Override
	public IAction getItemDeleteAction() {
		return new IAction() {
			@Override
			public void run() throws BOMException {
				BOMHelper.getTextHome().deleteItem(getID());
			}
		};
	}

}
