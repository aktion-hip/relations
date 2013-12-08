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

/**
 * @author Luthiger
 * @see org.elbe.relations.data.bom.bom.JoinRelatedPersonHome
 */
@SuppressWarnings("serial")
public class JoinRelatedTerm1Home extends JoinRelatedTermHome {
	private final static String colName = RelationHome.KEY_ITEM1;

	public JoinRelatedTerm1Home() {
		super();
	}

	@Override
	protected String getObjectDefString() {
		return XML_OBJECT_DEF1 + colName + XML_OBJECT_DEF2;
	}
}
