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

import java.sql.SQLException;

import org.elbe.relations.data.internal.bom.Relation;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.impl.DomainObjectHomeImpl;
import org.hip.kernel.bom.impl.KeyObjectImpl;
import org.hip.kernel.exc.VException;

/**
 * Home of the relations models, i.e. the entries in the table relating the
 * items.
 * 
 * @author Benno Luthiger Created on Sep 3, 2004
 */
public class RelationHome extends DomainObjectHomeImpl implements
		ICreatableHome {
	// constants
	public final static String KEY_ID = "ID";
	public final static String KEY_TYPE1 = "Type1";
	public final static String KEY_ITEM1 = "Item1";
	public final static String KEY_TYPE2 = "Type2";
	public final static String KEY_ITEM2 = "Item2";
	private final static String OBJECT_CLASS_NAME = "org.elbe.relations.data.internal.bom.Relation";

	private final static String XML_OBJECT_DEF = "<?xml version='1.0' encoding='ISO-8859-1'?>	"
			+ "<objectDef objectName='Relation' parent='org.hip.kernel.bom.DomainObject' version='1.0'>	"
			+ "	<keyDefs>	"
			+ "		<keyDef>	"
			+ "			<keyItemDef seq='0' keyPropertyName='"
			+ KEY_ID
			+ "'/>	"
			+ "		</keyDef>	"
			+ "	</keyDefs>	"
			+ "	<propertyDefs>	"
			+ "		<propertyDef propertyName='"
			+ KEY_ID
			+ "' valueType='Long' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblRelation' columnName='RELATIONID'/>	"
			+ "		</propertyDef>	"
			+ "		<propertyDef propertyName='"
			+ KEY_TYPE1
			+ "' valueType='Integer' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblRelation' columnName='NTYPE1'/>	"
			+ "		</propertyDef>	"
			+ "		<propertyDef propertyName='"
			+ KEY_ITEM1
			+ "' valueType='Long' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblRelation' columnName='NITEM1'/>	"
			+ "		</propertyDef>	"
			+ "		<propertyDef propertyName='"
			+ KEY_TYPE2
			+ "' valueType='Integer' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblRelation' columnName='NTYPE2'/>	"
			+ "		</propertyDef>	"
			+ "		<propertyDef propertyName='"
			+ KEY_ITEM2
			+ "' valueType='Long' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblRelation' columnName='NITEM2'/>	"
			+ "		</propertyDef>	" + "	</propertyDefs>	" + "</objectDef>";

	/**
	 * RelationHome constructor.
	 */
	public RelationHome() {
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

	/**
	 * Returns the object definition string of the class managed by this home.
	 * 
	 * @return java.lang.String
	 */
	@Override
	protected String getObjectDefString() {
		return XML_OBJECT_DEF;
	}

	/**
	 * Creates a relation between two items.
	 * 
	 * @param inItem1
	 *            IItem
	 * @param inItem2
	 *            IItem
	 * @return Relation
	 * @throws BOMException
	 */
	public Relation newRelation(final IItem inItem1, final IItem inItem2)
			throws BOMException {
		try {
			final DomainObject outRelation = create();
			outRelation.set(KEY_TYPE1, new Integer(inItem1.getItemType()));
			outRelation.set(KEY_ITEM1, new Long(inItem1.getID()));
			outRelation.set(KEY_TYPE2, new Integer(inItem2.getItemType()));
			outRelation.set(KEY_ITEM2, new Long(inItem2.getID()));
			outRelation.insert(true);
			outRelation.set(KEY_ID, getMax(KEY_ID).longValue());
			return (Relation) outRelation;
		}
		catch (final VException exc) {
			throw new BOMException(exc);
		}
		catch (final SQLException exc) {
			throw new BOMException(exc);
		}
	}

	/**
	 * Delete the relation with the specified ID.
	 * 
	 * @param inRelationID
	 * @throws BOMException
	 */
	public void deleteRelation(final long inRelationID) throws BOMException {
		try {
			final KeyObject lKey = new KeyObjectImpl();
			lKey.setValue(KEY_ID, new Long(inRelationID));
			delete(lKey, true);
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Delete the relation that connects the items with the specified values.
	 * 
	 * @param inType1
	 *            int Type of item 1.
	 * @param inID1
	 *            long ID of item 1.
	 * @param inType2
	 *            int Type of item 2.
	 * @param inID2
	 *            long ID of item 2.
	 * @throws BOMException
	 */
	public void deleteRelation(final int inType1, final long inID1,
			final int inType2, final long inID2) throws BOMException {
		try {
			delete(createKey(inType1, inID1, inType2, inID2), true);
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Finds the relation item with the specified ID.
	 * 
	 * @param inRelationID
	 *            long
	 * @return Relation
	 * @throws BOMException
	 */
	public Relation getRelation(final long inRelationID) throws BOMException {
		try {
			final KeyObject lKey = new KeyObjectImpl();
			lKey.setValue(KEY_ID, new Long(inRelationID));
			return (Relation) findByKey(lKey);
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Returns all relations of the specified item.
	 * 
	 * @param inItem
	 * @return QueryResult
	 * @throws BOMException
	 */
	public QueryResult getRelations(final IItem inItem) throws BOMException {
		try {
			final KeyObject lKey1 = new KeyObjectImpl();
			lKey1.setValue(KEY_TYPE1, new Integer(inItem.getItemType()));
			lKey1.setValue(KEY_ITEM1, new Long(inItem.getID()));

			final KeyObject lKey2 = new KeyObjectImpl();
			lKey2.setValue(KEY_TYPE2, new Integer(inItem.getItemType()));
			lKey2.setValue(KEY_ITEM2, new Long(inItem.getID()));
			lKey1.setValue(lKey2, KeyObject.BinaryBooleanOperator.OR);

			return select(lKey1);
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Retrieves the Relation with the specified values.
	 * 
	 * @param inType1
	 *            int
	 * @param inID1
	 *            long
	 * @param inType2
	 *            int
	 * @param inID2
	 *            long
	 * @return Relation
	 * @throws BOMException
	 */
	public Relation getRelation(final int inType1, final long inID1,
			final int inType2, final long inID2) throws BOMException {
		try {
			return (Relation) findByKey(createKey(inType1, inID1, inType2,
					inID2));
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	private KeyObject createKey(final int inType1, final long inID1,
			final int inType2, final long inID2) throws VException {
		final KeyObject outKey = new KeyObjectImpl();
		outKey.setValue(KEY_TYPE1, new Integer(inType1));
		outKey.setValue(KEY_ITEM1, new Long(inID1));
		outKey.setValue(KEY_TYPE2, new Integer(inType2));
		outKey.setValue(KEY_ITEM2, new Long(inID2));

		final KeyObject lKey = new KeyObjectImpl();
		lKey.setValue(KEY_TYPE1, new Integer(inType2));
		lKey.setValue(KEY_ITEM1, new Long(inID2));
		lKey.setValue(KEY_TYPE2, new Integer(inType1));
		lKey.setValue(KEY_ITEM2, new Long(inID1));
		outKey.setValue(lKey, KeyObject.BinaryBooleanOperator.OR);
		return outKey;
	}

	/**
	 * Deletes all relations to the specified item.
	 * 
	 * @param inItem
	 *            IItem
	 */
	public void deleteRelations(final IItem inItem) throws BOMException {
		try {
			final KeyObject lKey1 = new KeyObjectImpl();
			lKey1.setValue(KEY_TYPE1, new Integer(inItem.getItemType()));
			lKey1.setValue(KEY_ITEM1, new Long(inItem.getID()));

			final KeyObject lKey2 = new KeyObjectImpl();
			lKey2.setValue(KEY_TYPE2, new Integer(inItem.getItemType()));
			lKey2.setValue(KEY_ITEM2, new Long(inItem.getID()));
			lKey1.setValue(lKey2, KeyObject.BinaryBooleanOperator.OR);

			delete(lKey1, true);
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * @return String[]
	 * @see ICreatableHome#getSQLCreate()
	 */
	@Override
	public String[] getSQLCreate() {
		final String lSQL1 = "CREATE TABLE tblRelation (\n"
				+ "  RelationID	BIGINT generated always as identity,\n"
				+ "  nType1	SMALLINT not null,\n"
				+ "  nItem1	BIGINT not null,\n"
				+ "  nType2	SMALLINT not null,\n"
				+ "  nItem2	BIGINT not null,\n"
				+ "  PRIMARY KEY (RelationID)\n" + ")";
		final String lSQL2 = "CREATE INDEX idxRelation_01 ON tblRelation(nType1, nItem1)";
		final String lSQL3 = "CREATE INDEX idxRelation_02 ON tblRelation(nType2, nItem2)";
		return new String[] { lSQL1, lSQL2, lSQL3 };
	}

	/**
	 * @see ICreatableHome#getSQLDrop()
	 */
	@Override
	public String[] getSQLDrop() {
		final String lSQL1 = "DROP TABLE tblRelation";
		return new String[] { lSQL1 };
	}

}
