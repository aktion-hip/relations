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

import org.osgi.service.jdbc.DataSourceFactory;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;

/**
 * @author Luthiger Created: 02.02.2012
 */
public class TestDataSourceFactoryMySQL implements DataSourceFactory {

	@Override
	public DataSource createDataSource(final Properties inProps)
			throws SQLException {
		final MysqlDataSource outSource = new MysqlDataSource();
		setup(outSource, inProps);
		return outSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.jdbc.DataSourceFactory#createConnectionPoolDataSource
	 * (java.util.Properties)
	 */
	@Override
	public ConnectionPoolDataSource createConnectionPoolDataSource(
			final Properties inProps) throws SQLException {
		final MysqlConnectionPoolDataSource outSource = new MysqlConnectionPoolDataSource();
		setup(outSource, inProps);
		return outSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.jdbc.DataSourceFactory#createXADataSource(java.util.
	 * Properties)
	 */
	@Override
	public XADataSource createXADataSource(final Properties inProps)
			throws SQLException {
		return new MysqlXADataSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.jdbc.DataSourceFactory#createDriver(java.util.Properties
	 * )
	 */
	@Override
	public Driver createDriver(final Properties inProps) throws SQLException {
		return new com.mysql.jdbc.Driver();
	}

	private void setup(final MysqlDataSource inSource,
			final Properties inProperties) {
		if (inProperties == null) {
			return;
		}
		if (inProperties.containsKey(JDBC_DATABASE_NAME)) {
			inSource.setDatabaseName(inProperties
					.getProperty(JDBC_DATABASE_NAME));
		}
		if (inProperties.containsKey(JDBC_DATASOURCE_NAME)) {
			// not supported?
		}
		if (inProperties.containsKey(JDBC_DESCRIPTION)) {
			// not supported?
		}
		if (inProperties.containsKey(JDBC_NETWORK_PROTOCOL)) {
			// not supported?
		}
		if (inProperties.containsKey(JDBC_PASSWORD)) {
			inSource.setPassword(inProperties.getProperty(JDBC_PASSWORD));
		}
		if (inProperties.containsKey(JDBC_PORT_NUMBER)) {
			inSource.setPortNumber(Integer.parseInt(inProperties
					.getProperty(JDBC_PORT_NUMBER)));
		}
		if (inProperties.containsKey(JDBC_ROLE_NAME)) {
			// not supported?
		}
		if (inProperties.containsKey(JDBC_SERVER_NAME)) {
			inSource.setServerName(inProperties.getProperty(JDBC_SERVER_NAME));
		}
		if (inProperties.containsKey(JDBC_URL)) {
			inSource.setURL(inProperties.getProperty(JDBC_URL));
		}
		if (inProperties.containsKey(JDBC_USER)) {
			inSource.setUser(inProperties.getProperty(JDBC_USER));
		}
	}

}
