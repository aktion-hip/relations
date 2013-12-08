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
 * Home of the text item domain models.
 * 
 * @author Benno Luthiger Created on Sep 4, 2005
 */
@SuppressWarnings("serial")
public class TextHome extends AbstractHome implements IItemFactory,
        ICreatableHome {
	// constants
	private final static String OBJECT_CLASS_NAME = "org.elbe.relations.data.bom.Text";
	public final static String KEY_ID = "ID";
	public final static String KEY_TITLE = "Title";
	public final static String KEY_TEXT = "Text";
	public final static String KEY_AUTHOR = "Author";
	public final static String KEY_COAUTHORS = "CoAuthors";
	public final static String KEY_SUBTITLE = "Subtitle";
	public final static String KEY_YEAR = "Year";
	public final static String KEY_PUBLICATION = "Publication";
	public final static String KEY_PAGES = "Pages";
	public final static String KEY_VOLUME = "Volume";
	public final static String KEY_NUMBER = "Number";
	public final static String KEY_PUBLISHER = "Publisher";
	public final static String KEY_PLACE = "Place";
	public final static String KEY_TYPE = "Type";
	public final static String KEY_CREATED = "Created";
	public final static String KEY_MODIFIED = "Modified";

	private final static String XML_OBJECT_DEF = "<?xml version='1.0' encoding='ISO-8859-1'?>	"
	        + "<objectDef objectName='Text' parent='org.hip.kernel.bom.DomainObject' version='1.0'>	"
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
	        + "			<mappingDef tableName='tblText' columnName='TEXTID'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_TITLE
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='STITLE'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_TEXT
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='STEXT'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_AUTHOR
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='SAUTHOR'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_COAUTHORS
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='SCOAUTHORS'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_SUBTITLE
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='SSUBTITLE'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_YEAR
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='SYEAR'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_PUBLICATION
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='SPUBLICATION'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_PAGES
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='SPAGES'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_VOLUME
	        + "' valueType='Number' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='NVOLUME'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_NUMBER
	        + "' valueType='Number' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='NNUMBER'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_PUBLISHER
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='SPUBLISHER'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_PLACE
	        + "' valueType='String' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='SPLACE'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_TYPE
	        + "' valueType='Number' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='NTYPE'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_CREATED
	        + "' valueType='Timestamp' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='DTCREATION'/>	"
	        + "		</propertyDef>	"
	        + "		<propertyDef propertyName='"
	        + KEY_MODIFIED
	        + "' valueType='Timestamp' propertyType='simple'>	"
	        + "			<mappingDef tableName='tblText' columnName='DTMUTATION'/>	"
	        + "		</propertyDef>	" + "	</propertyDefs>	" + "</objectDef>";

	/**
	 * TextHome constructor.
	 */
	public TextHome() {
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
	 * Creates a new Text entry in the table.
	 * 
	 * @param inTitle
	 *            String
	 * @param inText
	 *            String
	 * @param inAuthor
	 *            String
	 * @param inCoAuthor
	 *            String
	 * @param inSubTitle
	 *            String
	 * @param inYear
	 *            String
	 * @param inPublication
	 *            String
	 * @param inPages
	 *            String
	 * @param inVolume
	 *            Integer
	 * @param inNumber
	 *            Integer
	 * @param inPublisher
	 *            String
	 * @param inPlace
	 *            String
	 * @param inType
	 *            Integer
	 * @return Text
	 * @throws BOMException
	 */
	public AbstractText newText(final String inTitle, final String inText,
	        final String inAuthor, final String inCoAuthor,
	        final String inSubTitle, final String inYear,
	        final String inPublication, final String inPages,
	        final Integer inVolume, final Integer inNumber,
	        final String inPublisher, final String inPlace, final Integer inType)
	        throws BOMException {
		try {
			final Timestamp lCreated = new Timestamp(System.currentTimeMillis());
			final Text outText = (Text) create();
			outText.set(KEY_TITLE, inTitle);
			outText.set(KEY_TEXT, inText);
			outText.set(KEY_AUTHOR, inAuthor);
			outText.set(KEY_COAUTHORS, inCoAuthor);
			outText.set(KEY_SUBTITLE, inSubTitle);
			outText.set(KEY_YEAR, inYear);
			outText.set(KEY_PUBLICATION, inPublication);
			outText.set(KEY_PAGES, inPages);
			outText.set(KEY_VOLUME, inVolume);
			outText.set(KEY_NUMBER, inNumber);
			outText.set(KEY_PUBLISHER, inPublisher);
			outText.set(KEY_PLACE, inPlace);
			outText.set(KEY_TYPE, inType);
			outText.set(KEY_CREATED, lCreated);
			outText.set(KEY_MODIFIED, lCreated);

			final Long lID = outText.insert(true);
			outText.set(KEY_ID, lID);
			// final KeyObject lKey = new KeyObjectImpl();
			// lKey.setValue(KEY_ID, new BigDecimal(lID.doubleValue()));
			// outText = (Text) findByKey(lKey);

			// index text
			getIndexer().addToIndex(outText);

			return outText;
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
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Finds the text item with the specified ID.
	 * 
	 * @param inItemID
	 *            long
	 * @return Text
	 * @throws BOMException
	 */
	public AbstractText getText(final long inItemID) throws BOMException {
		try {
			final KeyObject lKey = new KeyObjectImpl();
			lKey.setValue(KEY_ID, new Long(inItemID));
			return (AbstractText) findByKey(lKey);
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Finds the text item with the specified ID.
	 * 
	 * @param inItemID
	 *            long
	 * @return IItem
	 * @throws BOMException
	 */
	@Override
	public IItem getItem(final long inItemID) throws BOMException {
		return getText(inItemID);
	}

	/**
	 * @return String[]
	 * @see ICreatableHome#getSQLCreate()
	 */
	@Override
	public String[] getSQLCreate() {
		final String lSQL1 = "CREATE TABLE tblText (\n"
		        + "  TextID	BIGINT generated always as identity,\n"
		        + "  sTitle	VARCHAR(150) not null,\n" + "  sText	CLOB,\n"
		        + "  sAuthor	VARCHAR(100),\n" + "  sCoAuthors	VARCHAR(150),\n"
		        + "  sSubtitle	VARCHAR(200),\n" + "  sYear	VARCHAR(15),\n"
		        + "  sPublication	VARCHAR(100),\n" + "  sPages	VARCHAR(20),\n"
		        + "  nVolume	INT,\n" + "  nNumber	INT,\n"
		        + "  sPublisher	VARCHAR(99),\n" + "  sPlace	VARCHAR(99),\n"
		        + "  nType	INT,\n" + "  dtCreation	TIMESTAMP not null,\n"
		        + "  dtMutation	TIMESTAMP not null,\n"
		        + "  PRIMARY KEY (TextID)\n" + ")";
		final String lSQL2 = "CREATE INDEX idxText_01 ON tblText(sTitle)";
		final String lSQL3 = "CREATE INDEX idxText_02 ON tblText(sAuthor, sCoAuthors)";
		return new String[] { lSQL1, lSQL2, lSQL3 };
	}

	/**
	 * @see ICreatableHome#getSQLDrop()
	 */
	@Override
	public String[] getSQLDrop() {
		final String lSQL1 = "DROP TABLE tblText";
		return new String[] { lSQL1 };
	}

}
