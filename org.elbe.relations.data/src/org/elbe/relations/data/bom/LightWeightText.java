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
package org.elbe.relations.data.bom;

import java.sql.Timestamp;

import org.hip.kernel.exc.VException;

/**
 * A lightweight version of the Text model.
 * 
 * @author Luthiger
 */
public class LightWeightText extends AbstractLightWeight implements
		ILightWeightItem {
	public long id;
	public String title;
	public String text;
	public Timestamp created;
	public Timestamp modified;
	public final String author;
	public final String coauthor;
	public final String subtitle;
	public final String year;
	public final String publication;
	public final String pages;
	public final int volume;
	public final int number;
	public final String publisher;
	public final String place;
	public final int type;

	public LightWeightText(final long inID, final String inTitle,
			final String inText, final String inAuthor,
			final String inCoAuthor, final String inSubtitle,
			final String inYear, final String inPublication,
			final String inPages, final int inVolume, final int inNumber,
			final String inPublisher, final String inPlace, final int inType,
			final Timestamp inCreated, final Timestamp inModified) {
		super();
		id = inID;
		title = inTitle;
		text = inText;
		author = inAuthor;
		coauthor = inCoAuthor;
		subtitle = inSubtitle;
		year = inYear;
		publication = inPublication;
		pages = inPages;
		volume = inVolume;
		number = inNumber;
		publisher = inPublisher;
		place = inPlace;
		type = inType;
		created = inCreated;
		modified = inModified;
	}

	@Override
	public String toString() {
		return title;
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public int getItemType() {
		return IItem.TEXT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.data.internal.bom.AbstractLightWeight#getCreatedModified
	 * ()
	 */
	@Override
	protected Object[] getCreatedModified() throws VException {
		return new Object[] { created, modified };
	}

}
