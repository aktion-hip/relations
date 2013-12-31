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
 * A lightweight version of the Person model.
 * 
 * @author Luthiger
 */
public class LightWeightPerson extends AbstractLightWeight implements
        ILightWeightItem {
	public long id;
	public String name;
	public String firstname;
	public String text;
	public String from;
	public String to;
	public Timestamp created;
	public Timestamp modified;

	/**
	 * LightWeightPerson constructor.
	 * 
	 * @param inID
	 * @param inName
	 * @param inFirstname
	 * @param inText
	 * @param inFrom
	 * @param inTo
	 * @param inCreated
	 *            {@link Timestamp}
	 * @param inModified
	 *            {@link Timestamp}
	 */
	public LightWeightPerson(final long inID, final String inName,
	        final String inFirstname, final String inText, final String inFrom,
	        final String inTo, final Timestamp inCreated,
	        final Timestamp inModified) {
		super();
		id = inID;
		name = inName;
		firstname = inFirstname;
		text = inText;
		from = inFrom;
		to = inTo;
		created = inCreated;
		modified = inModified;
	}

	@Override
	public String toString() {
		if ((firstname != null) && (firstname.length() > 0))
			return name + ", " + firstname; //$NON-NLS-1$
		return name;
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public int getItemType() {
		return IItem.PERSON;
	}

	@Override
	protected Object[] getCreatedModified() throws VException {
		return new Object[] { created, modified };
	}

}
