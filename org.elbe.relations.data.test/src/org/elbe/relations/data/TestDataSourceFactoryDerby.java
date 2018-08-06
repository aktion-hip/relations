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
package org.elbe.relations.data;

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

/** @author Luthiger */
public class TestDataSourceFactoryDerby implements DataSourceFactory {

    @Override
    public DataSource createDataSource(final Properties properties)
            throws SQLException {
        final EmbeddedDataSource outSource = new EmbeddedDataSource();
        setup(outSource, properties);
        return outSource;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(final Properties properties) throws SQLException {
        final EmbeddedConnectionPoolDataSource outSource = new EmbeddedConnectionPoolDataSource();
        setup(outSource, properties);
        return outSource;
    }

    @Override
    public XADataSource createXADataSource(final Properties properties) throws SQLException {
        final EmbeddedXADataSource outSource = new EmbeddedXADataSource();
        setup(outSource, properties);
        return outSource;
    }

    @Override
    public Driver createDriver(final Properties properties) throws SQLException {
        return new EmbeddedDriver();
    }

    protected void setup(final EmbeddedDataSource source, final Properties properties) {
        if (properties == null) {
            return;
        }
        if (properties.containsKey(DataSourceFactory.JDBC_DATABASE_NAME)) {
            source.setDatabaseName(properties.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
            source.setCreateDatabase("create");
        }
        if (properties.containsKey(DataSourceFactory.JDBC_DATASOURCE_NAME)) {
            // not supported?
        }
        if (properties.containsKey(DataSourceFactory.JDBC_DESCRIPTION)) {
            // not supported?
        }
        if (properties.containsKey(DataSourceFactory.JDBC_NETWORK_PROTOCOL)) {
            // not supported?
        }
        if (properties.containsKey(DataSourceFactory.JDBC_PASSWORD)) {
            source.setPassword(properties
                    .getProperty(DataSourceFactory.JDBC_PASSWORD));
        }
        if (properties.containsKey(DataSourceFactory.JDBC_USER)) {
            source.setUser(properties.getProperty(DataSourceFactory.JDBC_USER));
        }
    }

}
