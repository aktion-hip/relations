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
package org.elbe.relations.data.utility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.transform.TransformerException;

import org.elbe.relations.data.Constants;
import org.elbe.relations.data.db.IDBObjectCreator;
import org.hip.kernel.bom.impl.AbstractQueryStatement;
import org.hip.kernel.bom.impl.DefaultStatement;
import org.hip.kernel.exc.VException;

/** Helper class to create the <code>EventStore</code> table and to manage it's entries..
 *
 * @author lbenno */
public class EventStoreChecker {
    private static final String TBL_EVENT_STORE = "tblEventStore";

    /** Checked creation of the <code>EventStore</code> table, i.e. the table is created only if it does not exist yet.
     *
     * @param creator {@link IDBObjectCreator}
     * @return boolean <code>true</code> if the table has been successfully created, <code>false</code> if the table
     *         already existed
     * @throws IOException
     * @throws TransformerException
     * @throws SQLException */
    public boolean createEventStoreChecked(final IDBObjectCreator creator)
            throws IOException, TransformerException, SQLException {
        if (!exists()) {
            return createEventStore(creator);
        }
        return false;
    }

    private boolean exists() throws SQLException {
        return new MetaDataQuery().exists(TBL_EVENT_STORE);
    }

    private boolean createEventStore(final IDBObjectCreator creator)
            throws IOException, TransformerException, SQLException {
        final DefaultStatement statement = new DefaultStatement();
        for (final String sqlCreate : creator.getCreateEventStoreStatements(Constants.XML_CREATE_OBJECTS)) {
            statement.execute(sqlCreate);
        }
        return true;
    }

    /** Removes all entries from the <code>EventStore</code> table.
     *
     * @throws SQLException */
    public void clear() throws SQLException {
        new DeleteQuery().deleteAll(TBL_EVENT_STORE);
    }

    // ---

    /** Helper class to query the schema's metadata, e.g. for the existing tables. */
    private static class MetaDataQuery extends AbstractQueryStatement {
        private static final long serialVersionUID = 1L;

        protected boolean exists(final String tableName) throws SQLException {
            try (Connection connection = getConnection()) {
                final DatabaseMetaData metaData = connection.getMetaData();
                final ResultSet result = metaData.getTables(null, null, tableName, null);
                if (result.next()) {
                    return true;
                }
                return metaData.getTables(null, null, tableName.toUpperCase(), null).next();
            } catch (final VException exc) {
                throw new SQLException(exc);
            }
        }
    }

    /** Helper class to remove all entries from the specified table. */
    private static class DeleteQuery extends AbstractQueryStatement {
        private static final long serialVersionUID = 1L;

        protected void deleteAll(final String tableName) throws SQLException {
            try (Connection connection = getConnection()) {
                final Statement statement = connection.createStatement();
                statement.execute(String.format("DELETE FROM %s", tableName));
            } catch (final VException exc) {
                throw new SQLException(exc);
            }
        }
    }

}
