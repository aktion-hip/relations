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

import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.bom.impl.DomainObjectImpl;
import org.hip.kernel.exc.VException;

/**
 * The model for a relation entry.
 * 
 * @author Benno Luthiger Created on Sep 3, 2004
 */
public class Relation extends DomainObjectImpl {

	public final static String HOME_CLASS_NAME = "org.elbe.relations.data.bom.RelationHome"; //$NON-NLS-1$

	/**
	 * Relation constructor.
	 */
	public Relation() {
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
	 * Returns this relation's unique ID.
	 * 
	 * @return long
	 * @throws VException
	 */
	public long getID() throws VException {
		return ((Long) get(RelationHome.KEY_ID)).longValue();
	}

	/**
	 * Returns the unique ID of the first related item.
	 * 
	 * @return long
	 * @throws VException
	 */
	public long getItemId1() throws VException {
		return new Long(get(RelationHome.KEY_ITEM1).toString()).longValue();
	}

	/**
	 * Returns the unique ID of the second related item.
	 * 
	 * @return long
	 * @throws VException
	 */
	public long getItemId2() throws VException {
		return new Long(get(RelationHome.KEY_ITEM2).toString()).longValue();
	}

	/**
	 * Returns the type of the first related item.
	 * 
	 * @return int
	 * @throws VException
	 */
	public int getItemType1() throws VException {
		return new Integer(get(RelationHome.KEY_TYPE1).toString()).intValue();
	}

	/**
	 * Returns the type of the second related item.
	 * 
	 * @return int
	 * @throws VException
	 */
	public int getItemType2() throws VException {
		return new Integer(get(RelationHome.KEY_TYPE2).toString()).intValue();
	}

	/**
	 * Returns the <code>UniqueID</code> of the first related item.
	 * 
	 * @return UniqueID
	 * @throws VException
	 */
	public UniqueID getItem1() throws VException {
		return new UniqueID(getItemType1(), getItemId1());
	}

	/**
	 * Returns the <code>UniqueID</code> of the first related item.
	 * 
	 * @return UniqueID
	 * @throws VException
	 */
	public UniqueID getItem2() throws VException {
		return new UniqueID(getItemType2(), getItemId2());
	}

}
