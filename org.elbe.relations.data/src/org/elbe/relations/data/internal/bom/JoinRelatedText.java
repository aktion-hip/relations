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
package org.elbe.relations.data.internal.bom;

import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.IRelated;
import org.elbe.relations.data.bom.JoinRelatedTextHome;
import org.elbe.relations.data.bom.Text;
import org.hip.kernel.exc.VException;

/**
 * The model for the text item (created using a join from the relation table).
 * 
 * @author Benno Luthiger
 */
public class JoinRelatedText extends AbstractText implements IRelated {

	private final static String HOME_CLASS_NAME = "org.elbe.relations.data.bom.JoinRelatedText1Home"; //$NON-NLS-1$

	/**
	 * JoinRelatedText constructor.
	 */
	public JoinRelatedText() {
		super();
	}

	/**
	 * This Method returns the class name of the home.
	 * 
	 * @return java.lang.String
	 */
	@Override
	public String getHomeClassName() {
		return HOME_CLASS_NAME;
	}

	/**
	 * @see IRelated#getRelationID()
	 */
	@Override
	public long getRelationID() throws VException {
		return ((Long) get(JoinRelatedTextHome.CONNECTION_ID)).longValue();
	}

	/**
	 * Overrides super class implementation.
	 * 
	 * @see IItem#saveTitleText(String, String)
	 */
	@Override
	public void saveTitleText(final String inTitle, final String inText)
			throws BOMException {
		try {
			BOMHelper.getTextHome().getItem(getID())
					.saveTitleText(inTitle, inText);
			setModel(inTitle, inText);
		}
		catch (final VException exc) {
			new BOMException(exc.getMessage());
		}
	}

	/**
	 * Overrides super class implementation.
	 */
	@Override
	public void save(final String inTitle, final String inText,
			final Integer inType, final String inAuthor,
			final String inCoAuthor, final String inSubTitle,
			final String inPublisher, final String inYear,
			final String inJournal, final String inPages,
			final Integer inArticleVolume, final Integer inArticleNumber,
			final String inLocation) throws BOMException {
		try {
			((Text) BOMHelper.getTextHome().getItem(getID())).save(inTitle,
					inText, inType, inAuthor, inCoAuthor, inSubTitle,
					inPublisher, inYear, inJournal, inPages, inArticleVolume,
					inArticleNumber, inLocation);
			setModel(inTitle, inText, inType, inAuthor, inCoAuthor, inSubTitle,
					inPublisher, inYear, inJournal, inPages, inArticleVolume,
					inArticleNumber, inLocation);
		}
		catch (final VException exc) {
			new BOMException(exc.getMessage());
		}
	}

}
