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
package org.elbe.relations.print.doc;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.elbe.relations.utility.AbstractJarPrintOut;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Print out in a MS Word document.
 *
 * @author lbenno
 */
public class PrintOut extends AbstractJarPrintOut {
	private final static String XSL_DOCBODY = "print_docxBody.xsl"; //$NON-NLS-1$
	private final static String XSL_CONTENT = "print_docx.xsl"; //$NON-NLS-1$

	private static final String CONTENT_TYPES = "[Content_Types].xml";
	private static final String _RELS = ".rels";
	private final static String CONTENT = "word/document.xml"; //$NON-NLS-1$
	private static final String FOOTER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><footer><w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1417\" w:right=\"1417\" w:bottom=\"1134\" w:left=\"1417\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/><w:cols w:space=\"708\"/><w:docGrid w:linePitch=\"360\"/></w:sectPr></footer>";
	private static final Format SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");;

	private File outputFile;
	private JarOutputStream outputJar;
	private Document content = null;

	@Override
	protected String getXSLNameBody() {
		return XSL_DOCBODY;
	}

	@Override
	protected String getXSLNameContent() {
		return XSL_CONTENT;
	}

	@Override
	protected void manageAfterOpenNew(File inPrintOut) throws IOException {
		outputFile = createChecked(inPrintOut);
		outputJar = initJar(outputFile);
		addEntry(outputJar, CONTENT_TYPES, "No content types found."); //$NON-NLS-1$
		addEntry(outputJar, _RELS, "No relations (.rels) found.", "_rels/", null); //$NON-NLS-1$ //$NON-NLS-2$
		addEntry(outputJar, "app.xml", "No app.xml.", "docProps/", null); //$NON-NLS-1$ //$NON-NLS-2$
		addEntry(formatPart("core.xml", getDocTitle(), getMetaSubject(), getMetaDescription(), SDF.format(new Date())),
				"docProps/core.xml", outputJar);
		addEntry(outputJar, "styles.xml", "No styles.xml.", "word/", null);
		addEntry(outputJar, "fontTable.xml", "No fontTable.xml.", "word/", null);
		addEntry(outputJar, "numbering.xml", "No numbering.xml.", "word/", null);
		addEntry(outputJar, "document.xml.rels", "No document.xml.rels.", "word/_rels/", null);
	}

	@Override
	protected void manageAfterReopen(File inPrintOut) throws IOException {
		final OutputObj outObj = manageAfterReopen(inPrintOut, CONTENT, outputFile);
		content = outObj.getDoc();
		outputJar = outObj.getJar();
	}

	@Override
	protected void manageBeforeClose(File inPrintOut) throws IOException {
		if (outputJar == null) {
			return;
		}
		writeContent(outputJar, content);
		outputJar.close();
	}

	/**
	 * Serialize content into JarEntry <code>word/document.xml</code>.
	 *
	 * @param inOutputJar
	 *            {@link JarOutputStream} the docx zip stream
	 * @param inContent
	 *            {@link Document} the content of <code>word/document.xml</code>
	 * @throws IOException
	 */
	private void writeContent(JarOutputStream inOutputJar, Document inContent) throws IOException {
		if (inContent == null) {
			return;
		}

		try {
			insertSection(FOOTER, inContent); // add footer
			final JarEntry entry = new JarEntry(CONTENT);
			inOutputJar.putNextEntry(entry);

			final StreamResult result = new StreamResult(inOutputJar);
			final Source source = new DOMSource(inContent);
			TransformerFactory.newInstance().newTransformer().transform(source, result);
		} catch (TransformerException | TransformerFactoryConfigurationError exc) {
			new IOException(exc.getMessage());
		}
	}

	private void insertSection(String inSection, Document inContent) throws IOException {
		if (inContent == null) {
			return;
		}

		try {
			final String sectionText = inSection.replaceAll("&amp;lt;", "&lt;"); // $NON-NLS-1$ //$NON-NLS-2$
			final Node section = inContent.importNode(createDoc(sectionText).getDocumentElement(), true);
			final NodeList children = section.getChildNodes();
			final NodeList targetElements = inContent.getElementsByTagName("w:body"); //$NON-NLS-1$
			if (targetElements.getLength() > 0) {
				final Node targetNode = targetElements.item(0);
				for (int i = 0; i < children.getLength(); i++) {
					targetNode.appendChild(children.item(i).cloneNode(true));
				}
			}
		} catch (SAXException | ParserConfigurationException exc) {
			new IOException(exc.getMessage());
		}
	}

	@Override
	protected void insertSection(String inSection) throws IOException {
		insertSection(inSection, content);
	}

	@Override
	protected void insertDocBody(String inXML) throws IOException {
		try {
			content = createDoc(inXML);
		} catch (SAXException | ParserConfigurationException exc) {
			new IOException(exc.getMessage());
		}
	}

}
