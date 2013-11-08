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

import org.hip.kernel.bom.impl.JoinedDomainObjectHomeImpl;

/**
 * Home for joins of related items with terms. This home can be used to retrieve
 * all term items related to an item.
 * 
 * @author Luthiger
 */
abstract public class JoinRelatedTermHome extends JoinedDomainObjectHomeImpl {
	public final static String CONNECTION_ID = "ConnectionID";
	private final static String OBJECT_CLASS_NAME = "org.elbe.relations.data.internal.bom.JoinRelatedTerm";
	protected final static String XML_OBJECT_DEF1 = "<?xml version='1.0' encoding='ISO-8859-1'?>	"
			+ "<joinedObjectDef objectName='JoinRelatedTerm' parent='org.hip.kernel.bom.ReadOnlyDomainObject' version='1.0'>	"
			+ "	<columnDefs>	" + "		<columnDef columnName='"
			+ TermHome.KEY_ID
			+ "' domainObject='org.elbe.relations.data.bom.Term'/>	"
			+ "		<columnDef columnName='"
			+ TermHome.KEY_TITLE
			+ "' domainObject='org.elbe.relations.data.bom.Term'/>	"
			+ "		<columnDef columnName='"
			+ TermHome.KEY_TEXT
			+ "' domainObject='org.elbe.relations.data.bom.Term'/>	"
			+ "		<columnDef columnName='"
			+ TermHome.KEY_CREATED
			+ "' domainObject='org.elbe.relations.data.bom.Term'/>	"
			+ "		<columnDef columnName='"
			+ TermHome.KEY_MODIFIED
			+ "' domainObject='org.elbe.relations.data.bom.Term'/>	"
			+ "		<columnDef columnName='"
			+ RelationHome.KEY_ID
			+ "' alias='"
			+ CONNECTION_ID
			+ "' domainObject='org.elbe.relations.data.internal.bom.Relation'/>	"
			+ "		<columnDef columnName='"
			+ RelationHome.KEY_ITEM1
			+ "' domainObject='org.elbe.relations.data.internal.bom.Relation'/>	"
			+ "		<columnDef columnName='"
			+ RelationHome.KEY_TYPE1
			+ "' domainObject='org.elbe.relations.data.internal.bom.Relation'/>	"
			+ "		<columnDef columnName='"
			+ RelationHome.KEY_ITEM2
			+ "' domainObject='org.elbe.relations.data.internal.bom.Relation'/>	"
			+ "		<columnDef columnName='"
			+ RelationHome.KEY_TYPE2
			+ "' domainObject='org.elbe.relations.data.internal.bom.Relation'/>	"
			+ "	</columnDefs>	"
			+ "	<joinDef joinType='EQUI_JOIN'>	"
			+ "		<objectDesc objectClassName='org.elbe.relations.data.internal.bom.Relation'/>	"
			+ "		<objectDesc objectClassName='org.elbe.relations.data.bom.Term'/>	"
			+ "		<joinCondition>	" + "			<columnDef columnName='";
	protected final static String XML_OBJECT_DEF2 = "' domainObject='org.elbe.relations.data.internal.bom.Relation'/>	"
			+ "			<columnDef columnName='"
			+ TermHome.KEY_ID
			+ "' domainObject='org.elbe.relations.data.bom.Term'/>	"
			+ "		</joinCondition>	" + "	</joinDef>	" + "</joinedObjectDef>";

	/**
	 * JoinRelatedTermHome constructor.
	 */
	public JoinRelatedTermHome() {
		super();
	}

	/**
	 * Returns the name of the objects which this home can create.
	 * 
	 * @return java.lang.String
	 */
	@Override
	public String getObjectClassName() {
		return OBJECT_CLASS_NAME;
	}

}
