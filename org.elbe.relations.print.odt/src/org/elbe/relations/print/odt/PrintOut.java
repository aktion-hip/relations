/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.print.odt;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.elbe.relations.services.IPrintOut;
import org.elbe.relations.utility.AbstractJarPrintOut;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Print out in a OpenOffice.org text document.
 *
 * @author Luthiger
 */
public class PrintOut extends AbstractJarPrintOut implements IPrintOut {
	private final static String XSL_CONTENT = "print_odt.xsl"; //$NON-NLS-1$
	private final static String XSL_DOCBODY = "print_odtBody.xsl"; //$NON-NLS-1$

	private final static String MANIFEST = "manifest.xml"; //$NON-NLS-1$
	private final static String MIMETYPE = "mimetype"; //$NON-NLS-1$
	private final static String STYLES = "styles.xml"; //$NON-NLS-1$
	private final static String META = "meta.xml"; //$NON-NLS-1$
	private final static String CONTENT = "content.xml"; //$NON-NLS-1$
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private File outputFile = null;
	private JarOutputStream outputJar;
	private Document content = null;
	private boolean isNew = true;

	@Override
	protected String getXSLNameBody() {
		return XSL_DOCBODY;
	}

	@Override
	protected String getXSLNameContent() {
		return XSL_CONTENT;
	}

	@Override
	protected void insertDocBody(final String inXML) throws IOException {
		try {
			content = createDoc(inXML);
		} catch (SAXException | ParserConfigurationException exc) {
			new IOException(exc.getMessage());
		}
	}

	@Override
	protected void insertSection(final String inSection) throws IOException {
		if (content == null) {
			return;
		}

		try {
			final String lSectionText = inSection.replaceAll("&amp;lt;", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			final Node lSection = content.importNode(createDoc(lSectionText).getDocumentElement(), true);
			final NodeList lChilds = lSection.getChildNodes();
			final NodeList lTargetElements = content.getElementsByTagName("office:text"); //$NON-NLS-1$
			if (lTargetElements.getLength() > 0) {
				final Node lTargetNode = lTargetElements.item(0);
				for (int i = 0; i < lChilds.getLength(); i++) {
					lTargetNode.appendChild(lChilds.item(i).cloneNode(true));
				}
			}
		} catch (SAXException | ParserConfigurationException exc) {
			new IOException(exc.getMessage());
		}
	}

	@Override
	protected void manageAfterOpenNew(final File inPrintOut) throws IOException {
		isNew = true;
		outputFile = createChecked(inPrintOut);
		outputJar = initJar(outputFile);
		addEntry(outputJar, MIMETYPE, "No mimetype file found.", "", createMimeTypeEntry(MIMETYPE)); //$NON-NLS-1$ //$NON-NLS-2$
		addEntry(outputJar, MANIFEST, "No manifest found.", "META-INF/", null); //$NON-NLS-1$ //$NON-NLS-2$
		addEntry(outputJar, STYLES, "No styles specification found."); //$NON-NLS-1$
	}

	private JarEntry createMimeTypeEntry(final String inName) {
		final JarEntry outEntry = new JarEntry(inName);
		outEntry.setMethod(ZipEntry.STORED);
		outEntry.setSize(39);
		outEntry.setCrc(0xC32C65E);
		return outEntry;
	}

	@Override
	protected void manageBeforeClose(final File inPrintOut) throws IOException {
		if (outputJar == null) {
			return;
		}

		writeContent(outputJar, content);
		if (isNew) {
			final String dateTime = new SimpleDateFormat(DATE_FORMAT).format(new Date());
			final String metaContent = formatPart(META, getDocTitle(), getMetaDescription(), getMetaSubject(), dateTime,
					dateTime);
			addEntry(metaContent, META, outputJar);
		}
		outputJar.close();
	}

	/**
	 * Serialize content into JarEntry content.xml
	 *
	 * @param inOutputJar
	 *            JarOutputStream
	 * @param inContent
	 *            Document
	 * @throws IOException
	 */
	private void writeContent(final JarOutputStream inOutputJar, final Document inContent) throws IOException {
		if (inContent == null) {
			return;
		}

		final JarEntry lEntry = new JarEntry(CONTENT);
		inOutputJar.putNextEntry(lEntry);

		final StreamResult lResult = new StreamResult(inOutputJar);
		final Source lSource = new DOMSource(inContent);
		try {
			TransformerFactory.newInstance().newTransformer().transform(lSource, lResult);
		} catch (TransformerException | TransformerFactoryConfigurationError exc) {
			new IOException(exc.getMessage());
		}
	}

	@Override
	protected void manageAfterReopen(final File inPrintOut) throws IOException {
		isNew = false;
		final OutputObj outObj = manageAfterReopen(inPrintOut, CONTENT, outputFile);
		content = outObj.getDoc();
		outputJar = outObj.getJar();
	}

}
