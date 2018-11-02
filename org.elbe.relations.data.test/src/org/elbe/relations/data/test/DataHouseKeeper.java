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
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
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
import org.elbe.relations.data.bom.EventStoreHome;
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

    private static final int SLEEP_PERIOD_DERBY = 0; // milliseconds 50 200
    private static final int SLEEP_PERIOD = 50; // milliseconds 50 200
    private static final String EMBEDDED_DERBY = "org.apache.derby.jdbc.EmbeddedDriver/Derby (embedded)/10.5.1.1";
    private boolean isEmbeddedDerby = false;

    DataHouseKeeper() {
        // initMySQL();
        initDerbyEmbedded();
    }

    /**
     * @return boolean <code>true</code> if the configured connection setting is
     *         for an embedded database
     */
    public boolean isEmbedded() {
        return this.isEmbeddedDerby;
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

    public EventStoreHome getEventStoreHome() {
        return BOMHelper.getEventStoreHome();
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
    void deleteAllFrom(final String tableName) throws SQLException, VException, InterruptedException {
        final Connection connection = DataSourceRegistry.INSTANCE
                .getConnection();
        final Statement statement = connection.createStatement();
        statement.execute("DELETE FROM " + tableName);
        // lConnection.commit();
        statement.close();
        connection.close();
        Thread.currentThread().sleep(this.isEmbeddedDerby ? SLEEP_PERIOD_DERBY : SLEEP_PERIOD);
    }

    public void deleteAllFromTerm() throws SQLException, VException, InterruptedException {
        deleteAllFrom("tblTerm");
    }

    public void deleteAllFromText() throws SQLException, VException, InterruptedException {
        deleteAllFrom("tblText");
    }

    public void deleteAllFromPerson() throws SQLException, VException, InterruptedException {
        deleteAllFrom("tblPerson");
    }

    public void deleteAllFromRelation() throws SQLException, VException, InterruptedException {
        deleteAllFrom("tblRelation");
    }

    public void deleteAllFromEventStore() throws SQLException, VException, InterruptedException {
        deleteAllFrom("tblEventStore");
    }

    public void deleteAllInAll() throws SQLException, VException, InterruptedException {
        deleteAllFromPerson();
        deleteAllFromRelation();
        deleteAllFromTerm();
        deleteAllFromText();
        deleteAllFromEventStore();
    }

    // ---

    /**
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @see org.elbe.relations.handlers.DbEmbeddedCreateHandler
     */
    private void createEmbeddedTables()
            throws IOException, SAXException, ParserConfigurationException, SQLException, TransformerException {
        final IDBObjectCreator creator = new TestEmbeddedCreator();
        final DefaultStatement statement = new DefaultStatement();
        for (final String sqlCreate : creator.getCreateStatemens(Constants.XML_CREATE_OBJECTS)) {
            statement.execute(sqlCreate);
        }
    }

    private void initMySQL() {
        try {
            DataSourceRegistry.INSTANCE.setFactory(new TestDataSourceFactoryMySQL());
            DataSourceRegistry.INSTANCE.setActiveConfiguration(createDBAccessConfiguration());
            VSys.setContextPath(new File("").getAbsolutePath());
        }
        catch (final IOException exc) {
            LOG.error("Could not initialize the DataHouseKeeper for MySQL!", exc);
        }
    }

    private void initDerbyEmbedded() {
        try {
            this.isEmbeddedDerby = true;
            DataSourceRegistry.INSTANCE.setFactory(new TestDataSourceFactoryDerby());
            DataSourceRegistry.INSTANCE.setActiveConfiguration(createDBAccessConfigurationEmbedded());
            VSys.setContextPath(new File("").getAbsolutePath());
            createEmbeddedTables();
        }
        catch (final Exception exc) {
            LOG.error("Could not initialize the DataHouseKeeper for Derby!", exc);
        }
    }

    private DBAccessConfiguration createDBAccessConfiguration()
            throws IOException {
        try (InputStream stream = DataHouseKeeper.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            final Properties properties = new Properties();
            properties.load(stream);
            stream.close();
            return new DBAccessConfiguration(
                    properties.getProperty("org.hip.vif.db.driver"),
                    properties.getProperty("org.hip.vif.db.server"),
                    properties.getProperty("org.hip.vif.db.schema"),
                    properties.getProperty("org.hip.vif.db.userId"),
                    properties.getProperty("org.hip.vif.db.password"));
        }
    }

    private DBAccessConfiguration createDBAccessConfigurationEmbedded() {
        final File schema = new File(new File("."), "./data/relations_test");
        if (schema.exists()) {
            try {
                Files.walk(schema.toPath()).sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
            } catch (final IOException exc1) {
                System.out.println("Unable to delete the Derby DB!");
            }
        }
        return new DBAccessConfiguration(EMBEDDED_DERBY, "", schema.getAbsolutePath(), "", "");
    }

    /**
     * @return
     */
    public boolean isGerman() {
        return Locale.GERMAN.getLanguage().equals(
                Locale.getDefault().getLanguage());
    }

    /** Creates a field map of the specified <code>IndexerDocument</code>.
     *
     * @param document {@link IndexerDocument} the document to process
     * @return Map&lt;String, String> field name / field value */
    public static Map<String, String> createFieldMap(final IndexerDocument document) {
        final Collection<IndexerField> fields = document.getFields();
        final Map<String, String> out = new HashMap<>(
                fields.size());
        for (final IndexerField lField : fields) {
            out.put(lField.getFieldName(), lField.getValue());
        }
        return out;
    }

}
