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
package org.elbe.relations.data.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.elbe.relations.data.Constants;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Base class for classes providing the SQL statements to create the database
 * objects (tables and indexes) needed by the application.
 * <p>
 * This class assumes a generic description of the database tables in an XML in
 * the <code>resources</code> directory. Subclasses have to provide the XSL file
 * that is able to transform the generic XML description into concrete SQL
 * create statements suitable for the specific database. To do this, subclasses
 * have to return the XSL's URL by the method <code>getXSL()</code>.
 * </p>
 *
 * @author Luthiger
 */
public abstract class AbstractDBObjectCreator implements IDBObjectCreator {
	private static Bundle bundle = FrameworkUtil.getBundle(Constants.class);
	// private static Bundle bundle = Platform
	// .getBundle(RelationsConstants.MAIN_ID);
	private static String RESOURCES_DIR = "resources/"; //$NON-NLS-1$

	/**
	 * Returns the SQL statements based on the specified database model.
	 *
	 * @param inXMLName
	 *            String name of the XML file specifying the database model.
	 * @return Collection<String> of SQL CREATE TABLE/INDEX statements
	 * @throws IOException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	@Override
	public Collection<String> getCreateStatemens(final String inXMLName)
			throws IOException, TransformerFactoryConfigurationError, TransformerException {
		final XMLHandler lHandler = new XMLHandler();
		InputStream xml = null;
		InputStream xsl = null;

		if (bundle == null) {
			return Collections.emptyList();
		}

		try {
			final URL entry = bundle.getEntry(RESOURCES_DIR + inXMLName);
			if (entry == null) {
				return Collections.emptyList();
			}

			// get xml and xsl
			xml = entry.openStream();
			final Source lXMLSource = new StreamSource(xml);
			xsl = getXSL().openStream();
			final Source lXSLSource = new StreamSource(xsl);

			// transform xml
			final Templates lTemplates = TransformerFactory.newInstance().newTemplates(lXSLSource);
			final Transformer lTransformer = lTemplates.newTransformer();
			final Result lResult = new SAXResult(lHandler);
			lTransformer.transform(lXMLSource, lResult);
		} finally {
			if (xsl != null) {
				xsl.close();
			}
			if (xml != null) {
				xml.close();
			}
		}
		return lHandler.getStatements();
	}

	protected abstract URL getXSL();

	// --- private classes ---

	private class XMLHandler implements ContentHandler {
		private final Collection<String> statements = new ArrayList<String>();
		private StringBuilder entry = null;
		private boolean isInEntry = false;

		@Override
		public void startElement(final String inUri, final String inLocalName, final String inName,
				final Attributes inAtts) throws SAXException {
			if (Constants.NODE_NAME_CREATED_OBJECT.equals(inName)) {
				entry = new StringBuilder();
				isInEntry = true;
			}
		}

		@Override
		public void endElement(final String inUri, final String inLocalName, final String inName) throws SAXException {
			if (Constants.NODE_NAME_CREATED_OBJECT.equals(inName)) {
				final String lEntry = entry.toString().trim();
				if (lEntry.length() > 0) {
					statements.add(lEntry);
				}
				isInEntry = false;
			}
		}

		@Override
		public void characters(final char[] inChars, final int inStart, final int inLength) throws SAXException {
			if (isInEntry) {
				final char[] lTarget = new char[inLength];
				System.arraycopy(inChars, inStart, lTarget, 0, inLength);
				entry.append(lTarget);
			}
		}

		public Collection<String> getStatements() {
			return statements;
		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void ignorableWhitespace(final char[] inCh, final int inStart, final int inLength) throws SAXException {
		}

		@Override
		public void processingInstruction(final String inTarget, final String inData) throws SAXException {
		}

		@Override
		public void setDocumentLocator(final Locator inLocator) {
		}

		@Override
		public void skippedEntity(final String inName) throws SAXException {
		}

		@Override
		public void startPrefixMapping(final String inPrefix, final String inUri) throws SAXException {
		}

		@Override
		public void endPrefixMapping(final String inPrefix) throws SAXException {
		}
	}

}
