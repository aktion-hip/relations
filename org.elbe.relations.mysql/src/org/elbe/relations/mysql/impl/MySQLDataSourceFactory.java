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
package org.elbe.relations.mysql.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.mysql.cj.jdbc.MysqlXADataSource;

/** The MySQL provider for the <code>org.osgi.service.jdbc.DataSourceFactory</code>.
 *
 * @author lbenno */
public class MySQLDataSourceFactory implements DataSourceFactory {

    private <T extends MysqlDataSource> T initialize(final T mysqlSource, final Properties props) {
        getChecked(props, DataSourceFactory.JDBC_DATABASE_NAME).ifPresent(mysqlSource::setDatabaseName);
        getChecked(props, DataSourceFactory.JDBC_USER).ifPresent(mysqlSource::setUser);
        getChecked(props, DataSourceFactory.JDBC_PASSWORD).ifPresent(mysqlSource::setPassword);
        getChecked(props, DataSourceFactory.JDBC_SERVER_NAME).ifPresent(mysqlSource::setServerName);
        getChecked(props, DataSourceFactory.JDBC_URL).ifPresent(mysqlSource::setUrl);
        getChecked(props, DataSourceFactory.JDBC_PORT_NUMBER)
        .ifPresent(p -> mysqlSource.setPortNumber(Integer.parseInt(p)));
        return mysqlSource;
    }

    private Optional<String> getChecked(final Properties props, final String name) {
        return Optional.ofNullable(props.getProperty(name));
    }

    @Override
    public DataSource createDataSource(final Properties props) throws SQLException {
        return initialize(new MysqlDataSource(), props);
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(final Properties props) throws SQLException {
        return initialize(new MysqlConnectionPoolDataSource(), props);
    }

    @Override
    public XADataSource createXADataSource(final Properties props) throws SQLException {
        return initialize(new MysqlXADataSource(), props);
    }

    @Override
    public Driver createDriver(final Properties props) throws SQLException {
        return new com.mysql.cj.jdbc.Driver();
    }

}
