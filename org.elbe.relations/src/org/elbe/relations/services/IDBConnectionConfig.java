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
package org.elbe.relations.services;

import org.elbe.relations.data.db.IDBObjectCreator;

/**
 * OSGi service to create the Relations database for a specified provider (e.g.
 * Derby, MySQL etc.).
 * 
 * @author Luthiger
 */
public interface IDBConnectionConfig {

	/**
	 * @return String The name of the database the provided JDBC driver connects
	 *         to.
	 */
	String getName();

	/**
	 * 
	 * @return String Fully qualified name of database driver class provided by
	 *         this plug-in's JDBC package.
	 */
	String getJDBCDriverClass();

	/**
	 * @return String The database related part in the connection URL, e.g.
	 *         jdbc:<b>mysql</b>://localhost/catalog.
	 */
	String getSubprotocol();

	/**
	 * @return boolean <code>true</code> if the database to be accessed is
	 *         embedded (i.e. runs in the same Java thread as the application
	 *         does) or not. For example: Derby is configured as embedded
	 *         database by the Relations application. MySQL can be accessed as
	 *         an external database.
	 */
	boolean isEmbedded();

	/**
	 * @return {@link IDBObjectCreator} this creator's implementation of the
	 *         <code>IDBObjectCreator</code> interface. This class can and
	 *         should use
	 *         <code>org.elbe.relations.db.AbstractDBObjectCreator</code> as
	 *         base class. The application describes the data model it uses in a
	 *         RDMBS independent way by means of an XML file. The task of the
	 *         Creator class is to provide an XSL stylesheet that transforms
	 *         this XML into a set of CREATE TABLE and CREATE INDEX statements
	 *         for the specific database.
	 */
	IDBObjectCreator getCreator();

	/**
	 * @return boolean Some databases (e.g. MySQL) allow to set the identity
	 *         field (primary key field) that is defined as
	 *         <code>auto_increment</code> to a specified value whereas other
	 *         (like Derby) don't allow this.
	 */
	boolean canSetIdentityField();

}
