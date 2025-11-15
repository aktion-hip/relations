/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2025, Benno Luthiger
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
package org.elbe.relations.derby.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.osgi.service.jdbc.DataSourceFactory;

/** The Derby provider for the <code>org.osgi.service.jdbc.DataSourceFactory</code>.
 *
 * @author Luthiger */
public class DerbyDataSourceFactory implements DataSourceFactory {
    private static final String CREATE_DB = "create";

    @Override
    public DataSource createDataSource(final Properties props) throws SQLException {
        final EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName(props.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
        dataSource.setCreateDatabase(CREATE_DB);
        return dataSource;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(final Properties props) throws SQLException {
        final EmbeddedConnectionPoolDataSource dataSource = new EmbeddedConnectionPoolDataSource();
        dataSource.setDatabaseName(props.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
        dataSource.setCreateDatabase(CREATE_DB);
        return dataSource;
    }

    @Override
    public XADataSource createXADataSource(final Properties props) throws SQLException {
        final EmbeddedXADataSource dataSource = new EmbeddedXADataSource();
        dataSource.setDatabaseName(props.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
        dataSource.setCreateDatabase(CREATE_DB);
        return dataSource;
    }

    @Override
    public Driver createDriver(final Properties props) throws SQLException {
        return new EmbeddedDriver();
    }

}
