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
 * Home of the term item domain models.
 *
 * @author Benno Luthiger Created on Sep 3, 2005
 */
@SuppressWarnings("serial")
public class TermHome extends AbstractHome implements IItemFactory, ICreatableHome {
	// constants
	private final static String OBJECT_CLASS_NAME = "org.elbe.relations.data.bom.Term";
	public final static String KEY_ID = "ID";
	public final static String KEY_TITLE = "Title";
	public final static String KEY_TEXT = "Text";
	public final static String KEY_CREATED = "Created";
	public final static String KEY_MODIFIED = "Modified";

	private final static String XML_OBJECT_DEF = "<?xml version='1.0' encoding='ISO-8859-1'?>	"
			+ "<objectDef objectName='Term' parent='org.hip.kernel.bom.DomainObject' version='1.0'>	"
			+ "	<keyDefs>	" + "		<keyDef>	" + "			<keyItemDef seq='0' keyPropertyName='" + KEY_ID
			+ "'/>	" + "		</keyDef>	" + "	</keyDefs>	" + "	<propertyDefs>	"
			+ "		<propertyDef propertyName='" + KEY_ID + "' valueType='Long' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblTerm' columnName='TERMID'/>	" + "		</propertyDef>	"
			+ "		<propertyDef propertyName='" + KEY_TITLE + "' valueType='String' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblTerm' columnName='STITLE'/>	" + "		</propertyDef>	"
			+ "		<propertyDef propertyName='" + KEY_TEXT + "' valueType='String' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblTerm' columnName='STEXT'/>	" + "		</propertyDef>	"
			+ "		<propertyDef propertyName='" + KEY_CREATED + "' valueType='Timestamp' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblTerm' columnName='DTCREATION'/>	" + "		</propertyDef>	"
			+ "		<propertyDef propertyName='" + KEY_MODIFIED + "' valueType='Timestamp' propertyType='simple'>	"
			+ "			<mappingDef tableName='tblTerm' columnName='DTMUTATION'/>	" + "		</propertyDef>	"
			+ "	</propertyDefs>	" + "</objectDef>";

	/**
	 * TermHome constructor.
	 */
	public TermHome() {
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
	 * Create a new term entry in the database and returns the new item.
	 *
	 * @param inTitle
	 *            String
	 * @param inText
	 *            String
	 * @return Term
	 * @throws BOMException
	 */
	public AbstractTerm newTerm(final String inTitle, final String inText) throws BOMException {
		try {
			final Timestamp lCreated = new Timestamp(System.currentTimeMillis());
			final Term outTerm = (Term) create();
			outTerm.set(KEY_TITLE, inTitle);
			outTerm.set(KEY_TEXT, inText);
			outTerm.set(KEY_CREATED, lCreated);
			outTerm.set(KEY_MODIFIED, lCreated);

			outTerm.insert(true);

			// index term
			getIndexer().addToIndex(outTerm);

			return outTerm;
		} catch (final VException | IOException exc) {
			throw new BOMException(exc.getMessage());
		} catch (final SQLException exc) {
			if (AbstractItem.TRUNCATION_STATE.equals(exc.getSQLState())) {
				throw new BOMTruncationException(AbstractItem.TRUNCATION_MSG);
			}
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Delete the item with the specified ID.
	 *
	 * @param inItemID
	 *            long
	 * @throws BOMException
	 */
	@Override
	public void deleteItem(final long inItemID) throws BOMException {
		try {
			final KeyObject lKey = new KeyObjectImpl();
			lKey.setValue(KEY_ID, new Long(inItemID));
			delete(lKey, true);
		} catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		} catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Finds the term item with the specified ID.
	 *
	 * @param inItemID
	 *            long
	 * @return Term
	 * @throws BOMException
	 */
	public AbstractTerm getTerm(final long inItemID) throws BOMException {
		try {
			final KeyObject lKey = new KeyObjectImpl();
			lKey.setValue(KEY_ID, new Long(inItemID));
			return (AbstractTerm) findByKey(lKey);
		} catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	@Override
	public IItem getItem(final long inItemID) throws BOMException {
		return getTerm(inItemID);
	}

	/**
	 * @return String[]
	 * @see ICreatableHome#getSQLCreate()
	 */
	@Override
	public String[] getSQLCreate() {
		final String lSQL1 = "CREATE TABLE tblTerm (\n" + "  TermID	BIGINT generated always as identity,\n"
				+ "  sTitle	VARCHAR(99) not null,\n" + "  sText	CLOB,\n" + "  dtCreation	TIMESTAMP not null,\n"
				+ "  dtMutation	TIMESTAMP not null,\n" + "  PRIMARY KEY (TermID)\n" + ")";
		final String lSQL2 = "CREATE INDEX idxTerm_01 ON tblTerm(sTitle)";
		return new String[] { lSQL1, lSQL2 };
	}

	/**
	 * @see ICreatableHome#getSQLDrop()
	 */
	@Override
	public String[] getSQLDrop() {
		final String lSQL1 = "DROP TABLE tblTerm";
		return new String[] { lSQL1 };
	}

}
