/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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
import java.sql.Timestamp;

import org.elbe.relations.data.utility.RelationsSerializer;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.bom.AbstractSerializer;
import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.exc.VException;

/** Home of the EventStore item domain models.
 *
 * @author lbenno */
public class EventStoreHome extends AbstractHome implements ICreatableHome {
    private static final long serialVersionUID = 1L;

    public enum StoreType {
        CREATE(1), UPDATE(2), DELETE(3);

        private final int id;

        StoreType(final int id) {
            this.id = id;
        }
    }

    private final static String OBJECT_CLASS_NAME = "org.elbe.relations.data.bom.EventStore";
    public final static String KEY_ID = "ID";
    public final static String KEY_TYPE = "Type";
    public final static String KEY_UNIQUE_ID = "UniqueID";
    public final static String KEY_EVENT = "Event";
    public final static String KEY_CREATED = "Created";

    private final static String XML_OBJECT_DEF = "<?xml version='1.0' encoding='ISO-8859-1'?>   "
            + "<objectDef objectName='EventStore' parent='org.hip.kernel.bom.DomainObject' version='1.0'>   "
            + " <keyDefs>   " + "       <keyDef>    " + "           <keyItemDef seq='0' keyPropertyName='" + KEY_ID
            + "'/>  " + "       </keyDef>   " + "   </keyDefs>  " + "   <propertyDefs>  "
            + "     <propertyDef propertyName='" + KEY_ID + "' valueType='Long' propertyType='simple'>  "
            + "         <mappingDef tableName='tblEventStore' columnName='EVENTSTOREID'/>   "
            + "       </propertyDef>  "
            + "     <propertyDef propertyName='" + KEY_TYPE + "' valueType='Number' propertyType='simple'>  "
            + "         <mappingDef tableName='tblEventStore' columnName='NTYPE'/>  " + "       </propertyDef>  "
            + "     <propertyDef propertyName='" + KEY_UNIQUE_ID + "' valueType='String' propertyType='simple'>  "
            + "         <mappingDef tableName='tblEventStore' columnName='SUNIQUEID'/>  " + "       </propertyDef>  "
            + "     <propertyDef propertyName='" + KEY_EVENT + "' valueType='String' propertyType='simple'>  "
            + "         <mappingDef tableName='tblEventStore' columnName='SEVENT'/>  " + "       </propertyDef>  "
            + "     <propertyDef propertyName='" + KEY_CREATED + "' valueType='Timestamp' propertyType='simple'>    "
            + "         <mappingDef tableName='tblEventStore' columnName='DTCREATION'/> " + "       </propertyDef>  "
            + " </propertyDefs> " + "</objectDef>";

    @Override
    public String getObjectClassName() {
        return OBJECT_CLASS_NAME;
    }

    @Override
    protected String getObjectDefString() {
        return XML_OBJECT_DEF;
    }

    @Override
    public String[] getSQLCreate() {
        final String sql1 = "CREATE TABLE tblEventStore (\n" + "  EventStoreID BIGINT generated always as identity,\n"
                + "  nType      SMALLINT not null,\n" + "  sUniqueID      VARCHAR(50),\n"
                + "  sEvent     CLOB,\n" + "  dtCreation TIMESTAMP not null,\n" + "  PRIMARY KEY (EventStoreID)\n"
                + ")";
        final String sql2 = "CREATE INDEX idxEventStore_01 ON tblEventStore(dtCreation)";
        final String sql3 = "CREATE INDEX idxEventStore_02 ON tblEventStore(sUniqueID, dtCreation)";
        return new String[] { sql1, sql2, sql3 };
    }

    @Override
    public String[] getSQLDrop() {
        final String sql1 = "DROP TABLE tblEventStore";
        return new String[] { sql1 };
    }

    /** Store create or update item event
     *
     * @param id {@link UniqueID} the item's id
     * @param model {@link DomainObject} the item/model
     * @param type {@link StoreType}
     * @return Long the created entry's id
     * @throws BOMException */
    public Long saveEntry(final UniqueID id, final DomainObject model, final StoreType type) throws BOMException {
        return saveEntry(id, getEvent(model), type);
    }

    /** Store delete item event.
     *
     * @param id {@link UniqueID}
     * @return Long the created entry's id
     * @throws BOMException */
    public Long saveEntry(final UniqueID id) throws BOMException {
        return saveEntry(id, String.format("Delete(%s)", id.toString()), StoreType.DELETE);
    }

    private Long saveEntry(final UniqueID id, final String event, final StoreType type) throws BOMException {
        final EventStore entry = (EventStore) create();
        try {
            entry.set(KEY_TYPE, type.id);
            entry.set(KEY_UNIQUE_ID, id.toString());
            entry.set(KEY_EVENT, event);
            entry.set(KEY_CREATED, new Timestamp(System.currentTimeMillis()));
            return entry.insert();
        } catch (final SQLException | VException exc) {
            throw new BOMException(exc.getMessage());
        }
    }

    private String getEvent(final DomainObject model) {
        final AbstractSerializer visitor = new RelationsSerializer();
        model.accept(visitor);
        return visitor.toString();
    }

}
