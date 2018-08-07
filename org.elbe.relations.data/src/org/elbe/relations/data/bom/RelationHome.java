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

import org.elbe.relations.data.bom.EventStoreHome.StoreType;
import org.elbe.relations.data.internal.bom.Relation;
import org.elbe.relations.data.utility.UniqueID;
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
@SuppressWarnings("serial")
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
            + "' valueType='Number' propertyType='simple'>	"
            + "			<mappingDef tableName='tblRelation' columnName='NTYPE1'/>	"
            + "		</propertyDef>	"
            + "		<propertyDef propertyName='"
            + KEY_ITEM1
            + "' valueType='Long' propertyType='simple'>	"
            + "			<mappingDef tableName='tblRelation' columnName='NITEM1'/>	"
            + "		</propertyDef>	"
            + "		<propertyDef propertyName='"
            + KEY_TYPE2
            + "' valueType='Number' propertyType='simple'>	"
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

    /** Creates a relation between two items.
     *
     * @param item1 IItem
     * @param item2 IItem
     * @return Relation
     * @throws BOMException */
    public Relation newRelation(final IItem item1, final IItem item2) throws BOMException {
        try {
            final DomainObject outRelation = create();
            outRelation.set(KEY_TYPE1, new Integer(item1.getItemType()));
            outRelation.set(KEY_ITEM1, new Long(item1.getID()));
            outRelation.set(KEY_TYPE2, new Integer(item2.getItemType()));
            outRelation.set(KEY_ITEM2, new Long(item2.getID()));
            outRelation.insert(true);

            final long id = getMax(KEY_ID).longValue();
            outRelation.set(KEY_ID, id);
            BOMHelper.getEventStoreHome().saveEntry(new UniqueID(IItem.RELATION, id), outRelation, StoreType.CREATE);
            return (Relation) outRelation;
        }
        catch (VException | SQLException exc) {
            throw new BOMException(exc);
        }
    }

    /** Delete the relation with the specified ID.
     *
     * @param relationID
     * @throws BOMException */
    public void deleteRelation(final long relationID) throws BOMException {
        try {
            final KeyObject key = new KeyObjectImpl();
            key.setValue(KEY_ID, new Long(relationID));
            delete(key, true);
            BOMHelper.getEventStoreHome().saveEntry(new UniqueID(IItem.RELATION, relationID));
        }
        catch (VException | SQLException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    /** Delete the relation that connects the items with the specified values.
     *
     * @param type1 int Type of item 1.
     * @param id1 long ID of item 1.
     * @param type2 int Type of item 2.
     * @param id2 long ID of item 2.
     * @throws BOMException */
    public void deleteRelation(final int type1, final long id1, final int type2, final long id2) throws BOMException {
        try {
            final DomainObject relation = findByKey(createKey(type1, id1, type2, id2));
            BOMHelper.getEventStoreHome().saveEntry(new UniqueID(IItem.RELATION, (Long) relation.get(KEY_ID)));
            relation.delete(true);
        }
        catch (SQLException | VException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    /** Finds the relation item with the specified ID.
     *
     * @param relationID long
     * @return Relation
     * @throws BOMException */
    public Relation getRelation(final long relationID) throws BOMException {
        try {
            final KeyObject lKey = new KeyObjectImpl();
            lKey.setValue(KEY_ID, new Long(relationID));
            return (Relation) findByKey(lKey);
        }
        catch (final VException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    /** Returns all relations of the specified item.
     *
     * @param item
     * @return QueryResult
     * @throws BOMException */
    public QueryResult getRelations(final IItem item) throws BOMException {
        try {
            final KeyObject lKey1 = new KeyObjectImpl();
            lKey1.setValue(KEY_TYPE1, new Integer(item.getItemType()));
            lKey1.setValue(KEY_ITEM1, new Long(item.getID()));

            final KeyObject lKey2 = new KeyObjectImpl();
            lKey2.setValue(KEY_TYPE2, new Integer(item.getItemType()));
            lKey2.setValue(KEY_ITEM2, new Long(item.getID()));
            lKey1.setValue(lKey2, KeyObject.BinaryBooleanOperator.OR);

            return select(lKey1);
        }
        catch (SQLException | VException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    /** Retrieves the Relation with the specified values.
     *
     * @param type1 int
     * @param id1 long
     * @param type2 int
     * @param id2 long
     * @return Relation
     * @throws BOMException */
    public Relation getRelation(final int type1, final long id1, final int type2, final long id2) throws BOMException {
        try {
            return (Relation) findByKey(createKey(type1, id1, type2, id2));
        }
        catch (final VException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    private KeyObject createKey(final int type1, final long id1, final int type2, final long id2) throws VException {
        final KeyObject outKey = new KeyObjectImpl();
        outKey.setValue(KEY_TYPE1, new Integer(type1));
        outKey.setValue(KEY_ITEM1, new Long(id1));
        outKey.setValue(KEY_TYPE2, new Integer(type2));
        outKey.setValue(KEY_ITEM2, new Long(id2));

        final KeyObject key2 = new KeyObjectImpl();
        key2.setValue(KEY_TYPE1, new Integer(type2));
        key2.setValue(KEY_ITEM1, new Long(id2));
        key2.setValue(KEY_TYPE2, new Integer(type1));
        key2.setValue(KEY_ITEM2, new Long(id1));
        outKey.setValue(key2, KeyObject.BinaryBooleanOperator.OR);
        return outKey;
    }

    /** Deletes all relations to the specified item.
     *
     * @param item IItem */
    public void deleteRelations(final IItem item) throws BOMException {
        try {
            final KeyObject key1 = new KeyObjectImpl();
            key1.setValue(KEY_TYPE1, new Integer(item.getItemType()));
            key1.setValue(KEY_ITEM1, new Long(item.getID()));

            final KeyObject key2 = new KeyObjectImpl();
            key2.setValue(KEY_TYPE2, new Integer(item.getItemType()));
            key2.setValue(KEY_ITEM2, new Long(item.getID()));
            key1.setValue(key2, KeyObject.BinaryBooleanOperator.OR);

            // set deletion of relations to event store
            final EventStoreHome eventStoreHome = BOMHelper.getEventStoreHome();
            final QueryResult relations = select(key1);
            while (relations.hasMoreElements()) {
                final Long id = (Long) relations.nextAsDomainObject().get(RelationHome.KEY_ID);
                eventStoreHome.saveEntry(new UniqueID(IItem.RELATION, id));
            }
            // delete relations
            delete(key1, true);
        }
        catch (VException | SQLException exc) {
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
        final String sql = "DROP TABLE tblRelation";
        return new String[] { sql };
    }

}
