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

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.impl.KeyObjectImpl;
import org.hip.kernel.exc.VException;

/**
 * Home of the person item domain models.
 * 
 * @author Benno Luthiger Created on Sep 3, 2005
 */
@SuppressWarnings("serial")
public class PersonHome extends AbstractHome implements IItemFactory,
        ICreatableHome {

	private final static String OBJECT_CLASS_NAME = "org.elbe.relations.data.bom.Person";
	public final static String KEY_ID = "ID";
	public final static String KEY_NAME = "Name";
	public final static String KEY_FIRSTNAME = "Firstname";
	public final static String KEY_TEXT = "Text";
	public final static String KEY_FROM = "From";
	public final static String KEY_TO = "To";
	public final static String KEY_CREATED = "Created";
	public final static String KEY_MODIFIED = "Modified";

	private final static String XML_OBJECT_DEF = "<?xml version='1.0' encoding='ISO-8859-1'?>	"
	        + "<objectDef objectName='Person' parent='org.hip.kernel.bom.DomainObject' version='1.0'>	"
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
	        + "			<mappingDef tableName='tblPerson' columnName='PERSONID'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_NAME
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblPerson' columnName='SNAME'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_FIRSTNAME
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblPerson' columnName='SFIRSTNAME'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_TEXT
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblPerson' columnName='STEXT'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_FROM
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblPerson' columnName='SFROM'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_TO
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblPerson' columnName='STO'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_CREATED
	        + "' valueType='Timestamp' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblPerson' columnName='DTCREATION'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_MODIFIED
	        + "' valueType='Timestamp' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblPerson' columnName='DTMUTATION'/>	"
	        + "		</propertyDef>	" + "	</propertyDefs>	" + "</objectDef>";

	/**
	 * PersonHome constructor.
	 */
	public PersonHome() {
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
	 * Create a new person entry in the database and returns the new item.
	 * 
	 * @param inName
	 *            String
	 * @param inFirstname
	 *            String
	 * @param inFrom
	 *            String
	 * @param inTo
	 *            String
	 * @param inText
	 *            String
	 * @return Person
	 * @throws BOMException
	 */
	public AbstractPerson newPerson(final String inName,
	        final String inFirstname, final String inFrom, final String inTo,
	        final String inText) throws BOMException {
		try {
			final Timestamp lCreated = new Timestamp(System.currentTimeMillis());
			final Person outPerson = (Person) create();
			outPerson.set(PersonHome.KEY_NAME, inName);
			outPerson.set(PersonHome.KEY_FIRSTNAME, inFirstname);
			outPerson.set(PersonHome.KEY_FROM, inFrom);
			outPerson.set(PersonHome.KEY_TO, inTo);
			outPerson.set(PersonHome.KEY_TEXT, inText);
			outPerson.set(KEY_CREATED, lCreated);
			outPerson.set(KEY_MODIFIED, lCreated);

			final Long lID = outPerson.insert(true);
			outPerson.set(KEY_ID, lID);
			// final KeyObject lKey = new KeyObjectImpl();
			// lKey.setValue(KEY_ID, new BigDecimal(lID.doubleValue()));
			// outPerson = (Person) findByKey(lKey);

			// index person
			getIndexer().addToIndex(outPerson);

			return outPerson;
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final IOException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			if (AbstractItem.TRUNCATION_STATE.equals(exc.getSQLState())) {
				throw new BOMTruncationException(AbstractItem.TRUNCATION_MSG);
			}
			throw new BOMException(exc.getMessage());
		}
	}

	@Override
	public void deleteItem(final long inItemID) throws BOMException {
		try {
			final KeyObject lKey = new KeyObjectImpl();
			lKey.setValue(KEY_ID, new Long(inItemID));
			delete(lKey, true);
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	public AbstractPerson getPerson(final long inItemID) throws BOMException {
		try {
			final KeyObject lKey = new KeyObjectImpl();
			lKey.setValue(KEY_ID, new Long(inItemID));
			return (AbstractPerson) findByKey(lKey);
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	@Override
	public IItem getItem(final long inItemID) throws BOMException {
		return getPerson(inItemID);
	}

	/**
	 * @return String[]
	 * @see ICreatableHome#getSQLCreate()
	 */
	@Override
	public String[] getSQLCreate() {
		final String lSQL1 = "CREATE TABLE tblPerson (\n"
		        + "  PersonID	BIGINT generated always as identity,\n"
		        + "  sName		VARCHAR(99) not null,\n"
		        + "  sFirstname	VARCHAR(50),\n" + "  sText		CLOB,\n"
		        + "  sFrom		VARCHAR(30),\n" + "  sTo		VARCHAR(30),\n"
		        + "  dtCreation	TIMESTAMP not null,\n"
		        + "  dtMutation	TIMESTAMP not null,\n"
		        + "  PRIMARY KEY (PersonID)\n" + ")";
		final String lSQL2 = "CREATE INDEX idxPerson_01 ON tblPerson(sName, sFirstname)";
		final String lSQL3 = "CREATE INDEX idxPerson_02 ON tblPerson(sFrom, sTo)";
		return new String[] { lSQL1, lSQL2, lSQL3 };
	}

	/**
	 * @see ICreatableHome#getSQLDrop()
	 */
	@Override
	public String[] getSQLDrop() {
		final String lSQL1 = "DROP TABLE tblPerson";
		return new String[] { lSQL1 };
	}

}
