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
package org.elbe.relations.data.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.elbe.relations.data.Constants;
import org.elbe.relations.data.TestDataSourceFactoryDerby;
import org.elbe.relations.data.TestDataSourceFactoryMySQL;
import org.elbe.relations.data.TestEmbeddedCreator;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.data.bom.TextHome;
import org.elbe.relations.data.db.IDBObjectCreator;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.hip.kernel.bom.impl.DefaultStatement;
import org.hip.kernel.dbaccess.DBAccessConfiguration;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.hip.kernel.exc.VException;
import org.hip.kernel.sys.VSys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Utility class for testing purpose. Creating and deleting of entries in
 * viftest-DB.<br />
 * 
 * @author Luthiger
 */
public enum DataHouseKeeper {
	INSTANCE;

	private static final Logger LOG = LoggerFactory
	        .getLogger(DataHouseKeeper.class);

	// constants
	private static final String PROPERTIES_FILE = "relations_db.properties";

	private static final int SLEEP_PERIOD = 50; // milliseconds 50 200
	private static final String EMBEDDED_DERBY = "org.apache.derby.jdbc.EmbeddedDriver/Derby (embedded)/10.5.1.1";
	private boolean isEmbeddedDerby = false;

	DataHouseKeeper() {
		initMySQL();
		// initDerbyEmbedded();
	}

	/**
	 * @return boolean <code>true</code> if the configured connection setting is
	 *         for an embedded database
	 */
	public boolean isEmbedded() {
		return isEmbeddedDerby;
	}

	public TermHome getTermHome() {
		return BOMHelper.getTermHome();
	}

	public TextHome getTextHome() {
		return BOMHelper.getTextHome();
	}

	public PersonHome getPersonHome() {
		return BOMHelper.getPersonHome();
	}

	public RelationHome getRelationHome() {
		return BOMHelper.getRelationHome();
	}

	public AbstractTerm createTerm(final String inTitle, final String inText)
	        throws BOMException {
		return getTermHome().newTerm(inTitle, inText);
	}

	public AbstractTerm createTerm(final String inTitle) throws BOMException {
		return createTerm(inTitle, inTitle + " - " + inTitle + " - " + inTitle);
	}

	public AbstractPerson createPerson(final String inName,
	        final String inFirstName) throws BOMException {
		return getPersonHome().newPerson(inName, inFirstName, "", "", "");
	}

	public AbstractText createText(final String inTitle, final String inAuthor)
	        throws BOMException {
		return getTextHome().newText(inTitle, "", inAuthor, "", "", "", "", "",
		        new Integer(0), new Integer(0), "", "", new Integer(1));
	}

	public void createRelation(final IItem inItem1, final IItem inItem2)
	        throws BOMException {
		getRelationHome().newRelation(inItem1, inItem2);
	}

	// --- delete methods

	@SuppressWarnings("static-access")
	void deleteAllFrom(final String inTableName) throws SQLException,
	        VException, InterruptedException {
		final Connection lConnection = DataSourceRegistry.INSTANCE
		        .getConnection();
		final Statement lStatement = lConnection.createStatement();
		lStatement.execute("DELETE FROM " + inTableName);
		// lConnection.commit();
		lStatement.close();
		lConnection.close();
		if (!isEmbeddedDerby) {
			Thread.currentThread().sleep(SLEEP_PERIOD);
		}
	}

	public void deleteAllFromTerm() throws SQLException, VException,
	        InterruptedException {
		deleteAllFrom("tblTerm");
	}

	public void deleteAllFromText() throws SQLException, VException,
	        InterruptedException {
		deleteAllFrom("tblText");
	}

	public void deleteAllFromPerson() throws SQLException, VException,
	        InterruptedException {
		deleteAllFrom("tblPerson");
	}

	public void deleteAllFromRelation() throws SQLException, VException,
	        InterruptedException {
		deleteAllFrom("tblRelation");
	}

	public void deleteAllInAll() throws SQLException, VException,
	        InterruptedException {
		deleteAllFromPerson();
		deleteAllFromRelation();
		deleteAllFromTerm();
		deleteAllFromText();
	}

	// ---

	/**
	 * @throws TransformerException
	 * @throws TransformerFactoryConfigurationError
	 * @see org.elbe.relations.handlers.DbEmbeddedCreateHandler
	 */
	private void createEmbeddedTables() throws IOException, SAXException,
	        ParserConfigurationException, SQLException,
	        TransformerFactoryConfigurationError, TransformerException {
		final IDBObjectCreator lCreator = new TestEmbeddedCreator();
		final DefaultStatement lStatement = new DefaultStatement();
		for (final String lSQLCreate : lCreator
		        .getCreateStatemens(Constants.XML_CREATE_OBJECTS)) {
			lStatement.execute(lSQLCreate);
		}
	}

	private void initMySQL() {
		try {
			DataSourceRegistry.INSTANCE
			        .setFactory(new TestDataSourceFactoryMySQL());
			DataSourceRegistry.INSTANCE
			        .setActiveConfiguration(createDBAccessConfiguration());

			VSys.setContextPath(new File("").getAbsolutePath());
		}
		catch (final IOException exc) {
			LOG.error("Could not initialize the DataHouseKeeper for MySQL!",
			        exc);
		}
	}

	private void initDerbyEmbedded() {
		try {
			isEmbeddedDerby = true;
			DataSourceRegistry.INSTANCE
			        .setFactory(new TestDataSourceFactoryDerby());
			DataSourceRegistry.INSTANCE
			        .setActiveConfiguration(createDBAccessConfigurationEmbedded());

			VSys.setContextPath(new File("").getAbsolutePath());
			createEmbeddedTables();
		}
		catch (final Exception exc) {
			LOG.error("Could not initialize the DataHouseKeeper for Derby!",
			        exc);
		}
	}

	private DBAccessConfiguration createDBAccessConfiguration()
	        throws IOException {
		InputStream lStream = null;
		try {
			lStream = DataHouseKeeper.class.getClassLoader()
			        .getResourceAsStream(PROPERTIES_FILE);
			final Properties lProperties = new Properties();
			lProperties.load(lStream);
			lStream.close();
			return new DBAccessConfiguration(
			        lProperties.getProperty("org.hip.vif.db.driver"),
			        lProperties.getProperty("org.hip.vif.db.server"),
			        lProperties.getProperty("org.hip.vif.db.schema"),
			        lProperties.getProperty("org.hip.vif.db.userId"),
			        lProperties.getProperty("org.hip.vif.db.password"));
		}
		finally {
			if (lStream != null) {
				lStream.close();
			}
		}
	}

	private DBAccessConfiguration createDBAccessConfigurationEmbedded() {
		final File lSchema = new File(".");
		return new DBAccessConfiguration(EMBEDDED_DERBY, "", new File(lSchema,
		        "./data/relations_test").getAbsolutePath(), "", "");
	}

	/**
	 * @return
	 */
	public boolean isGerman() {
		return Locale.GERMAN.getLanguage().equals(
		        Locale.getDefault().getLanguage());
	}

	/**
	 * Creates a field map of the specified <code>IndexerDocument</code>.
	 * 
	 * @param inDoc
	 *            {@link IndexerDocument} the document to process
	 * @return Map&lt;String, String> field name / field value
	 */
	public static Map<String, String> createFieldMap(final IndexerDocument inDoc) {
		final Collection<IndexerField> lFields = inDoc.getFields();
		final Map<String, String> out = new HashMap<String, String>(
		        lFields.size());
		for (final IndexerField lField : lFields) {
			out.put(lField.getFieldName(), lField.getValue());
		}
		return out;
	}

}
