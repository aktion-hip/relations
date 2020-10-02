/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2020, Benno Luthiger
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
package org.elbe.relations.internal.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.utility.RelationsSerializer;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.exc.VException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utility class to import the content of an XML export/backup.
 *
 * @author Luthiger Created on 16.10.2008
 */
public class XMLImport {
    // constants
    private final static String ROOT = "RelationsExport"; //$NON-NLS-1$
    private final static String OPERATION_CANCELED_ID = "operation_canceled"; //$NON-NLS-1$

    private final File importFile;
    private int numberOfEntries = 0;
    private final Collection<RelationReplaceHelper> relationsToRebind;

    /**
     * XMLImport constructor
     *
     * @param inXMLFileName
     *            String name of the file to import from.
     */
    public XMLImport(final String inXMLFileName) {
        this.importFile = new File(inXMLFileName);
        this.relationsToRebind = new ArrayList<>();
    }

    /**
     * Process the file and import the entries.
     *
     * @param monitor
     *            IProgressMonitor
     * @param canSetIdentityField
     *            boolean <code>true</code> if actual database can set the
     *            identity field (i.e. primary key)
     * @return int number of entries imported and created in the database
     *         catalog
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws InterruptedException
     */
    public int processFile(final IProgressMonitor monitor,
            final boolean canSetIdentityField) throws SAXException,
    ParserConfigurationException, IOException, InterruptedException {
        try (Reader reader = getReader()) {
            final InputSource source = new InputSource(reader);
            final XMLReader parser = SAXParserFactory.newInstance()
                    .newSAXParser().getXMLReader();
            parser.setContentHandler(
                    new XMLHandler(monitor, canSetIdentityField));
            parser.parse(source);
            return this.numberOfEntries;
        }
        catch (final SAXException exc) {
            if (OPERATION_CANCELED_ID.equals(exc.getMessage())) {
                throw new InterruptedException();
            } else {
                throw exc;
            }
        }
        finally {
            close();
        }
    }

    protected Reader getReader() throws IOException {
        return new BufferedReader(new FileReader(this.importFile));
    }

    /**
     * @return Collection<RelationReplaceHelper> the set of relations that have
     *         to be rebind.
     */
    public Collection<RelationReplaceHelper> getRelationsToRebind() {
        return this.relationsToRebind;
    }

    protected File getImportFile() {
        return this.importFile;
    }

    protected void close() throws IOException {
        // noting to do
    }

    // --- private classes ---

    /**
     * Class to handle the SAX parser events.
     */
    private class XMLHandler extends DefaultHandler {
        private boolean canImport = false;
        IInserterFactory inserterFactory = null;
        private IEntryInserter inserter = null;
        private IProgressMonitor monitor;
        private boolean canSetIdentityField = false;
        private final IInsertBehaviour insertBehaviour;
        private final IProgressMonitor mainMonitor;

        public XMLHandler(final IProgressMonitor inMonitor,
                final boolean inCanSetIdentityField) {
            this.mainMonitor = inMonitor;
            // monitor = SubMonitor.convert(inMonitor,
            // IProgressMonitor.UNKNOWN);
            this.canSetIdentityField = inCanSetIdentityField;
            this.insertBehaviour = this.canSetIdentityField ? new StraightInsertBehaviour()
                    : new CautiousInsertBehaviour();
        }

        @Override
        public void startElement(final String inUri, final String inLocalName,
                final String inName, final Attributes inAttributes)
                        throws SAXException {
            if (ROOT.equals(inName)) {
                this.canImport = true;
                XMLImport.this.numberOfEntries = 0;
                return;
            }
            if (this.canImport) {
                if (this.inserter != null) {
                    // e.g. field in "Term" or nodes in a field's text
                    if (this.inserter.isListening()) {
                        this.inserter.appendStartNode(inName, inAttributes);
                        return;
                    }
                    this.inserter.initializeField(inName, inAttributes,
                            this.canSetIdentityField);
                    return;
                }
                if (this.inserterFactory != null) {
                    // e.g. "Term"
                    this.inserter = this.inserterFactory.createInserter(inName,
                            this.insertBehaviour);
                    return;
                }
                if (XMLExport.NODE_TERMS.equals(inName)) {
                    // Terms
                    this.inserterFactory = new TermInserterFactory();
                    notifyMonitor("Term"); //$NON-NLS-1$
                }
                if (XMLExport.NODE_TEXTS.equals(inName)) {
                    this.inserterFactory = new TextInserterFactory();
                    notifyMonitor("Text"); //$NON-NLS-1$
                }
                if (XMLExport.NODE_PERSONS.equals(inName)) {
                    this.inserterFactory = new PersonInserterFactory();
                    notifyMonitor("Person"); //$NON-NLS-1$
                }
                if (XMLExport.NODE_RELATIONS.equals(inName)) {
                    this.inserterFactory = new RelationInserterFactory();
                    notifyMonitor("Relations"); //$NON-NLS-1$
                }
            }
        }

        private void notifyMonitor(final String inEntryType)
                throws SAXException {
            this.monitor = SubMonitor.convert(this.mainMonitor, String.format(
                    RelationsMessages.getString("XMLImport.msg.success"), //$NON-NLS-1$
                    inEntryType), 1000);
            if (this.monitor.isCanceled()) {
                throw new SAXException(OPERATION_CANCELED_ID);
            }
        }

        @Override
        public void endElement(final String inUri, final String inLocalName,
                final String inName) throws SAXException {
            if (ROOT.equals(inName)) {
                this.canImport = false;
                return;
            }
            if (this.canImport) {
                if (this.inserterFactory != null) {
                    if (this.inserter != null) {
                        if (this.inserter.testEndField(inName)) {
                            return;
                        }
                        this.inserter.appendEndNode(inName);
                    }
                    if (this.inserterFactory.getElementNode().equals(inName)) {
                        // e.g. "Term"
                        if (this.inserter != null) {
                            this.inserter.insert();
                            this.inserter = null;
                            if (this.monitor.isCanceled()) {
                                throw new SAXException(OPERATION_CANCELED_ID);
                            }
                            this.monitor.worked(1);
                        }
                        return;
                    }
                }
                if (XMLExport.NODE_TERMS.equals(inName)
                        || XMLExport.NODE_TEXTS.equals(inName)
                        || XMLExport.NODE_PERSONS.equals(inName)
                        || XMLExport.NODE_RELATIONS.equals(inName)) {
                    this.inserterFactory = null;
                }
            }
        }

        @Override
        public void characters(final char[] inChars, final int inStart,
                final int inLength) throws SAXException {
            if (this.inserter != null) {
                final char[] lTarget = new char[inLength];
                System.arraycopy(inChars, inStart, lTarget, 0, inLength);
                this.inserter.append(lTarget);
            }
        }
    }

    /**
     * Interface for factory classes. These factories create the appropriate
     * <code>IEntryInserter</code> classes.
     *
     * @author Luthiger Created on 23.10.2008
     */
    private interface IInserterFactory {
        IEntryInserter createInserter(String inEntryName,
                IInsertBehaviour inBehaviour) throws SAXException;

        String getElementNode();
    }

    private class TermInserterFactory implements IInserterFactory {
        private final static String ELEMENT_NODE = "TermEntry"; //$NON-NLS-1$
        private final DomainObjectHome home = BOMHelper.getTermHome();

        @Override
        public IEntryInserter createInserter(final String inEntryName,
                final IInsertBehaviour inBehaviour) throws SAXException {
            if (ELEMENT_NODE.equals(inEntryName)) {
                try {
                    return new TermInserter(this.home.create(), inBehaviour);
                }
                catch (final BOMException exc) {
                    throw new SAXException(exc);
                }
            }
            return null;
        }

        @Override
        public String getElementNode() {
            return ELEMENT_NODE;
        }
    }

    private class TextInserterFactory implements IInserterFactory {
        private final static String ELEMENT_NODE = "TextEntry"; //$NON-NLS-1$
        private final DomainObjectHome home = BOMHelper.getTextHome();

        @Override
        public IEntryInserter createInserter(final String inEntryName,
                final IInsertBehaviour inBehaviour) throws SAXException {
            if (ELEMENT_NODE.equals(inEntryName)) {
                try {
                    return new TextInserter(this.home.create(), inBehaviour);
                }
                catch (final BOMException exc) {
                    throw new SAXException(exc);
                }
            }
            return null;
        }

        @Override
        public String getElementNode() {
            return ELEMENT_NODE;
        }
    }

    private class PersonInserterFactory implements IInserterFactory {
        private final static String ELEMENT_NODE = "PersonEntry"; //$NON-NLS-1$
        private final DomainObjectHome home = BOMHelper.getPersonHome();

        @Override
        public IEntryInserter createInserter(final String inEntryName,
                final IInsertBehaviour inBehaviour) throws SAXException {
            if (ELEMENT_NODE.equals(inEntryName)) {
                try {
                    return new PersonInserter(this.home.create(), inBehaviour);
                }
                catch (final BOMException exc) {
                    throw new SAXException(exc);
                }
            }
            return null;
        }

        @Override
        public String getElementNode() {
            return ELEMENT_NODE;
        }
    }

    private class RelationInserterFactory implements IInserterFactory {
        private final static String ELEMENT_NODE = "RelationEntry"; //$NON-NLS-1$
        private final DomainObjectHome home = BOMHelper.getRelationHome();

        @Override
        public IEntryInserter createInserter(final String inEntryName,
                final IInsertBehaviour inBehaviour) throws SAXException {
            if (ELEMENT_NODE.equals(inEntryName)) {
                try {
                    return new RelationInserter(this.home.create(), inBehaviour);
                }
                catch (final BOMException exc) {
                    throw new SAXException(exc);
                }
            }
            return null;
        }

        @Override
        public String getElementNode() {
            return ELEMENT_NODE;
        }
    }

    /**
     * Interface for classes encapsulating the entry node in the parsed XML. If
     * the parser announces the end of the entry, the
     * <code>IEntryInserter</code> is able to insert the data into the table.
     *
     * @author Luthiger Created on 23.10.2008
     */
    private interface IEntryInserter {
        void insert() throws SAXException;

        void initializeField(String inName, Attributes inAttributes,
                boolean inCanSetIdentityField);

        boolean testEndField(String inName) throws SAXException;

        void append(char[] inChars);

        void appendStartNode(String inName, Attributes inAttributes);

        void appendEndNode(String inName);

        boolean isListening();
    }

    abstract class Inserter {
        private final DomainObject model;
        private IInsertField field = null;
        private StringBuilder value = new StringBuilder();
        private final IInsertBehaviour insertBehaviour;
        private final int itemType;
        private Long expectedID;

        public Inserter(final DomainObject inModel,
                final IInsertBehaviour inBehaviour, final int inItemType) {
            this.model = inModel;
            this.insertBehaviour = inBehaviour;
            this.itemType = inItemType;
            this.expectedID = new Long(0);
        }

        public void insert() throws SAXException {
            this.insertBehaviour.insert(this.model, this.expectedID, this.itemType);
            XMLImport.this.numberOfEntries++;
        }

        public void initializeField(final String inName,
                final Attributes inAttributes,
                final boolean inCanSetIdentityField) {
            final String lType = inAttributes.getValue("type"); //$NON-NLS-1$
            if ("String".equals(lType)) { //$NON-NLS-1$
                this.field = new StringField(inName);
            }
            if ("Number".equals(lType) || "Long".equals(lType)) { //$NON-NLS-1$ //$NON-NLS-2$
                if (inCanSetIdentityField) {
                    this.field = new NumberField(inName);
                } else {
                    this.field = new CautiousNumberField(inName);
                }
            }
            if ("Integer".equals(lType)) { //$NON-NLS-1$
                if (inCanSetIdentityField) {
                    this.field = new IntegerField(inName);
                } else {
                    this.field = new CautiousIntegerField(inName);
                }
            }
            if ("Timestamp".equals(lType)) { //$NON-NLS-1$
                this.field = new TimestampField(inName);
            }
        }

        public boolean testEndField(final String inName) throws SAXException {
            if (this.field == null) {
                return false;
            }
            if (!this.field.getFieldName().equals(inName)) {
                return false;
            }

            // e.g. "</ID>"
            try {
                final String lValue = this.value.toString().trim();
                if (this.field.setValueTo(this.model, lValue)) {
                    this.expectedID = new Long(lValue);
                }
                this.value = new StringBuilder();
                this.field = null;
                return true;
            }
            catch (final VException exc) {
                throw new SAXException(exc);
            }
        }

        public void append(final char[] inChars) {
            this.value.append(inChars);
        }

        public void appendStartNode(final String inName,
                final Attributes inAttributes) {
            this.value.append(String.format(
                    "<%s%s>", inName, processAttributes(inAttributes))); //$NON-NLS-1$
        }

        private StringBuilder processAttributes(final Attributes inAttributes) {
            final StringBuilder outAttributes = new StringBuilder();
            for (int i = 0; i < inAttributes.getLength(); i++) {
                outAttributes
                .append(String
                        .format(" %s=\"%s\"", inAttributes.getQName(i), inAttributes.getValue(i))); //$NON-NLS-1$
            }
            return outAttributes;
        }

        public void appendEndNode(final String inName) {
            this.value.append(String.format("</%s>", inName)); //$NON-NLS-1$
        }

        public boolean isListening() {
            return this.field != null;
        }

        protected void setField(final IInsertField inField) {
            this.field = inField;
        }
    }

    private class TermInserter extends Inserter implements IEntryInserter {
        public TermInserter(final DomainObject inModel,
                final IInsertBehaviour inBehaviour) {
            super(inModel, inBehaviour, IItem.TERM);
        }
    }

    private class TextInserter extends Inserter implements IEntryInserter {
        public TextInserter(final DomainObject inModel,
                final IInsertBehaviour inBehaviour) {
            super(inModel, inBehaviour, IItem.TEXT);
        }
    }

    private class PersonInserter extends Inserter implements IEntryInserter {
        public PersonInserter(final DomainObject inModel,
                final IInsertBehaviour inBehaviour) {
            super(inModel, inBehaviour, IItem.PERSON);
        }
    }

    private class RelationInserter extends Inserter implements IEntryInserter {
        private static final String ATT_NAME = "field"; //$NON-NLS-1$
        private static final String FIELD_NAME1 = "NTYPE1"; //$NON-NLS-1$
        private static final String FIELD_NAME2 = "NTYPE2"; //$NON-NLS-1$

        public RelationInserter(final DomainObject inModel,
                final IInsertBehaviour inBehaviour) {
            super(inModel, inBehaviour, 0);
        }

        @Override
        public void initializeField(final String inName,
                final Attributes inAttributes,
                final boolean inCanSetIdentityField) {
            // this is a workaround needed by the type change:
            // old <Type1 field="NTYPE1" type="Number"> ->
            // new <Type1 field="NTYPE1" type="Integer">
            final String lFieldName = inAttributes.getValue(ATT_NAME);
            if (FIELD_NAME1.equals(lFieldName)
                    || FIELD_NAME2.equals(lFieldName)) {
                setField(new IntegerField(inName));
            } else {
                super.initializeField(inName, inAttributes,
                        inCanSetIdentityField);
            }
        }
    }

    /**
     * Interface for classes that encapsulate the field node in the parsed XML.
     *
     * @author Luthiger Created on 23.10.2008
     */
    private interface IInsertField {
        String getFieldName();

        boolean setValueTo(DomainObject inModel, String inValue)
                throws VException;
    }

    abstract class AbstractField implements IInsertField {
        protected String fieldName;

        public AbstractField(final String inFieldName) {
            this.fieldName = inFieldName;
        }

        @Override
        public String getFieldName() {
            return this.fieldName;
        }
    }

    private class StringField extends AbstractField {
        public StringField(final String inFieldName) {
            super(inFieldName);
        }

        @Override
        public boolean setValueTo(final DomainObject inModel,
                final String inValue) throws VException {
            inModel.set(this.fieldName,
                    RelationsSerializer.prepareForImport(inValue));
            return false;
        }
    }

    private class NumberField extends AbstractField {
        public NumberField(final String inFieldName) {
            super(inFieldName);
        }

        @Override
        public boolean setValueTo(final DomainObject inModel,
                final String inValue) throws VException {
            inModel.set(this.fieldName, new Long(inValue));
            return false;
        }
    }

    private class CautiousNumberField extends AbstractField {
        public CautiousNumberField(final String inFieldName) {
            super(inFieldName);
        }

        @Override
        public boolean setValueTo(final DomainObject inModel,
                final String inValue) throws VException {
            if (this.fieldName.equals(inModel.getObjectDef().getPrimaryKeyDef()
                    .getKeyName(0))) {
                return true;
            }
            inModel.set(this.fieldName, new Long(inValue));
            return false;
        }
    }

    private class IntegerField extends AbstractField {
        public IntegerField(final String inFieldName) {
            super(inFieldName);
        }

        @Override
        public boolean setValueTo(final DomainObject inModel,
                final String inValue) throws VException {
            inModel.set(this.fieldName, new Integer(inValue));
            return false;
        }
    }

    private class CautiousIntegerField extends AbstractField {
        public CautiousIntegerField(final String inFieldName) {
            super(inFieldName);
        }

        @Override
        public boolean setValueTo(final DomainObject inModel,
                final String inValue) throws VException {
            if (this.fieldName.equals(inModel.getObjectDef().getPrimaryKeyDef()
                    .getKeyName(0))) {
                return true;
            }
            inModel.set(this.fieldName, new Integer(inValue));
            return false;
        }
    }

    private class TimestampField extends AbstractField {
        public TimestampField(final String inFieldName) {
            super(inFieldName);
        }

        @Override
        public boolean setValueTo(final DomainObject inModel,
                final String inValue) throws VException {
            inModel.set(this.fieldName, Timestamp.valueOf(inValue));
            return false;
        }
    }

    /**
     * Interface to encapsulate inserter behaviour added to
     * <code>Inserter</code> objects.
     *
     * @author Luthiger Created on 23.10.2008
     */
    private interface IInsertBehaviour {
        /**
         * Calls the insert method on the specified model.
         *
         * @param inModel
         *            DomainObject
         * @param inExpectedID
         *            Long
         * @param inItemType
         *            int
         * @throws SAXException
         */
        void insert(DomainObject inModel, Long inExpectedID, int inItemType)
                throws SAXException;
    }

    private class StraightInsertBehaviour implements IInsertBehaviour {
        @Override
        public void insert(final DomainObject inModel, final Long inExpectedID,
                final int inItemType) throws SAXException {
            try {
                inModel.insert(true);
            }
            catch (VException | SQLException exc) {
                throw new SAXException(exc);
            }
        }
    }

    private class CautiousInsertBehaviour implements IInsertBehaviour {
        @Override
        public void insert(final DomainObject inModel, final Long inExpectedID,
                final int inItemType) throws SAXException {
            try {
                final Long lID = inModel.insert(true);
                if (inItemType == 0) {
                    return;
                }
                if (!lID.equals(inExpectedID)) {
                    final RelationReplaceHelper lAdjusted = new RelationReplaceHelper(
                            new UniqueID(inItemType, inExpectedID.longValue()),
                            new UniqueID(inItemType, lID.longValue()));
                    XMLImport.this.relationsToRebind.add(lAdjusted);
                }
            }
            catch (VException | SQLException exc) {
                throw new SAXException(exc);
            }
        }
    }

    /**
     * Parameter object helping to replace an old id with a new one. Used for
     * the rebinding of relations.
     *
     * @author Luthiger Created on 23.10.2008
     */
    public class RelationReplaceHelper {
        public UniqueID oldID;
        public UniqueID newID;

        public RelationReplaceHelper(final UniqueID inOld, final UniqueID inNew) {
            this.oldID = inOld;
            this.newID = inNew;
        }

        @Override
        public String toString() {
            return String
                    .format("%s -> %s", this.oldID.toString(), this.newID.toString()); //$NON-NLS-1$
        }
    }

}
