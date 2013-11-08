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
package org.elbe.relations.print.odt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.elbe.relations.services.IPrintOut;
import org.elbe.relations.utility.AbstractPrintOut;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Print out in a OpenOffice.org text document.
 * 
 * @author Luthiger Created on 21.01.2007
 */
public class PrintOut extends AbstractPrintOut implements IPrintOut {
	private final static String XSL_CONTENT = "print_odt.xsl"; //$NON-NLS-1$
	private final static String XSL_DOCBODY = "print_odtBody.xsl"; //$NON-NLS-1$

	private final static String MANIFEST = "manifest.xml"; //$NON-NLS-1$
	private final static String MIMETYPE = "mimetype"; //$NON-NLS-1$
	private final static String STYLES = "styles.xml"; //$NON-NLS-1$
	private final static String META = "meta.xml"; //$NON-NLS-1$
	private final static String CONTENT = "content.xml"; //$NON-NLS-1$

	private static final String MSG_META_DESCRIPTION = Messages
			.getString("PrintOut.msg.meta.desc"); //$NON-NLS-1$
	private static final String MSG_META_SUBJECT = Messages
			.getString("PrintOut.msg.meta.subject"); //$NON-NLS-1$

	private final static int BUFFER_LEN = 16384;

	private File outputFile = null;
	private JarOutputStream outputJar;
	private DocumentBuilder docBuilder = null;
	private Document content = null;
	private boolean isNew = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.print.IPrintOut#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return true;
	}

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
		}
		catch (final SAXException exc) {
			new IOException(exc.getMessage());
		}
		catch (final ParserConfigurationException exc) {
			new IOException(exc.getMessage());
		}
	}

	@Override
	protected void insertSection(final String inSection) throws IOException {
		if (content == null)
			return;

		try {
			final String lSectionText = inSection
					.replaceAll("&amp;lt;", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			final Node lSection = content.importNode(createDoc(lSectionText)
					.getDocumentElement(), true);
			final NodeList lChilds = lSection.getChildNodes();
			final NodeList lTargetElements = content
					.getElementsByTagName("office:text"); //$NON-NLS-1$
			if (lTargetElements.getLength() > 0) {
				final Node lTargetNode = lTargetElements.item(0);
				for (int i = 0; i < lChilds.getLength(); i++) {
					lTargetNode.appendChild(lChilds.item(i).cloneNode(true));
				}
			}
		}
		catch (final SAXException exc) {
			new IOException(exc.getMessage());
		}
		catch (final ParserConfigurationException exc) {
			new IOException(exc.getMessage());
		}
	}

	private Document createDoc(final String inXML) throws SAXException,
			IOException, ParserConfigurationException {
		final StringReader lReader = new StringReader(inXML);
		try {
			return getDocumentBuilder().parse(new InputSource(lReader));
		} finally {
			lReader.close();
		}
	}

	private DocumentBuilder getDocumentBuilder()
			throws ParserConfigurationException {
		if (docBuilder == null) {
			docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		}
		return docBuilder;
	}

	@Override
	protected void manageAfterOpenNew(final File inPrintOut) throws IOException {
		isNew = true;
		outputFile = createChecked(inPrintOut);
		outputJar = initJar(outputFile);
		addEntry(outputJar, MIMETYPE,
				"No mimetype file found.", "", createMimeTypeEntry(MIMETYPE)); //$NON-NLS-1$ //$NON-NLS-2$
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

	private File createChecked(final File inPrintOut) throws IOException {
		if (!inPrintOut.createNewFile()) {
			throw new IOException(
					Messages.getString("PrintOut.err.creation.fail") + inPrintOut.getName()); //$NON-NLS-1$
		}
		if (!inPrintOut.canRead() || !inPrintOut.canWrite()) {
			throw new IOException(
					"Could not open file for read/write: " + inPrintOut.getName()); //$NON-NLS-1$
		}
		return inPrintOut;
	}

	private void createOOMeta(final JarOutputStream inOutputJar)
			throws IOException {
		final URL lResource = getResource(META, "No metadata template found."); //$NON-NLS-1$
		final StringWriter lWriter = new StringWriter();

		BufferedReader lReader = null;
		Writer lOutput = null;
		try {
			// read template
			final InputStream lInput = lResource.openStream();
			lReader = new BufferedReader(new InputStreamReader(lInput));
			final char lBuffer[] = new char[BUFFER_LEN];
			int lRead;
			while ((lRead = lReader.read(lBuffer, 0, BUFFER_LEN)) != -1) {
				lWriter.write(lBuffer, 0, lRead);
			}

			// add document specific information
			String lMeta = lWriter.toString();
			final DateFormat lFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$
			final String lDateTime = lFormat.format(new Date());
			lMeta = String.format(lMeta, getDocTitle(), MSG_META_DESCRIPTION,
					MSG_META_SUBJECT, lDateTime, lDateTime);

			// write to output
			final JarEntry lEntry = new JarEntry(META);
			inOutputJar.putNextEntry(lEntry);

			final StringReader lMetaReader = new StringReader(lMeta);
			lOutput = new BufferedWriter(new OutputStreamWriter(inOutputJar));
			while ((lRead = lMetaReader.read(lBuffer, 0, BUFFER_LEN)) != -1) {
				lOutput.write(lBuffer, 0, lRead);
			}
		} finally {
			if (lReader != null)
				lReader.close();
			if (lOutput != null)
				lOutput.close();
		}
	}

	private void addEntry(final JarOutputStream inOutputJar,
			final String inResourceName, final String inExceptionMsg,
			final String inPrefix, final JarEntry inEntry) throws IOException {
		final URL lResource = getResource(inResourceName, inExceptionMsg);
		JarEntry lEntry = inEntry;
		if (lEntry == null) {
			lEntry = new JarEntry(inPrefix + getResourceName(lResource));
		}
		inOutputJar.putNextEntry(lEntry);

		InputStream lInput = null;
		try {
			lInput = lResource.openStream();
			final byte lBuffer[] = new byte[BUFFER_LEN];
			int lRead;
			while ((lRead = lInput.read(lBuffer, 0, BUFFER_LEN)) != -1) {
				inOutputJar.write(lBuffer, 0, lRead);
			}
		} finally {
			if (lInput != null)
				lInput.close();
		}
	}

	private void addEntry(final JarOutputStream inOutputJar,
			final String inResourceName, final String inExceptionMsg)
			throws IOException {
		addEntry(inOutputJar, inResourceName, inExceptionMsg, "", null); //$NON-NLS-1$
	}

	private String getResourceName(final URL inResource) {
		final String[] lParts = inResource.getPath().split("/"); //$NON-NLS-1$
		return lParts[lParts.length - 1];
	}

	private JarOutputStream initJar(final File inOutputFile) throws IOException {
		final JarOutputStream outJar = new JarOutputStream(
				new FileOutputStream(inOutputFile));
		return outJar;
	}

	private URL getResource(final String inResourceName,
			final String inExceptionMsg) throws IOException {
		final URL outUrl = getClass().getResource(inResourceName);
		if (outUrl == null) {
			throw new IOException(inExceptionMsg);
		}
		return outUrl;
	}

	@Override
	protected void manageBeforeClose(final File inPrintOut) throws IOException {
		if (outputJar == null)
			return;

		writeContent(outputJar, content);
		if (isNew) {
			createOOMeta(outputJar);
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
	private void writeContent(final JarOutputStream inOutputJar,
			final Document inContent) throws IOException {
		if (inContent == null)
			return;

		final JarEntry lEntry = new JarEntry(CONTENT);
		inOutputJar.putNextEntry(lEntry);

		final StreamResult lResult = new StreamResult(inOutputJar);
		final Source lSource = new DOMSource(inContent);
		try {
			TransformerFactory.newInstance().newTransformer()
					.transform(lSource, lResult);
		}
		catch (final TransformerException exc) {
			new IOException(exc.getMessage());
		}
		catch (final TransformerFactoryConfigurationError exc) {
			new IOException(exc.getMessage());
		}
	}

	@Override
	protected void manageAfterReopen(final File inPrintOut) throws IOException {
		isNew = false;

		File lTemporary = null;
		JarInputStream lInputJar = null;
		try {
			// rename existing print out to temporary and create new empty print
			// out
			lTemporary = File.createTempFile(
					"~$rel", null, outputFile.getParentFile()); //$NON-NLS-1$
			lTemporary.delete();
			if (!outputFile.renameTo(lTemporary)) {
				throw new IOException(
						Messages.getString("PrintOut.err.reopen.fail")); //$NON-NLS-1$
			}
			outputFile = createChecked(outputFile);

			// copy existing jar entries to new version of print out, except
			// content.xml
			lInputJar = new JarInputStream(new FileInputStream(lTemporary));
			final Manifest lManifest = lInputJar.getManifest();
			if (lManifest == null) {
				outputJar = new JarOutputStream(
						new FileOutputStream(outputFile));
			} else {
				outputJar = new JarOutputStream(
						new FileOutputStream(outputFile), lManifest);
			}
			JarEntry lEntryIn;
			while ((lEntryIn = lInputJar.getNextJarEntry()) != null) {
				if (!lEntryIn.getName().equals(CONTENT)) {
					copyEntry(lEntryIn, lInputJar, outputJar);
				}
			}
			lInputJar.close();

			// retrieve content.xml and create a document
			lInputJar = new JarInputStream(new FileInputStream(lTemporary));
			while ((lEntryIn = lInputJar.getNextJarEntry()) != null
					&& (!lEntryIn.getName().equals(CONTENT))) {
				// do nothing, just seek content.xml
			}
			content = getDocumentBuilder().parse(lInputJar);
		}
		catch (final SAXException exc) {
			new IOException(exc.getMessage());
		}
		catch (final ParserConfigurationException exc) {
			new IOException(exc.getMessage());
		} finally {
			if (lInputJar != null)
				lInputJar.close();
			if (lTemporary != null && lTemporary.exists()) {
				lTemporary.delete();
			}
		}
	}

	private void copyEntry(final JarEntry inEntry,
			final JarInputStream inInputJar, final JarOutputStream inOutputJar)
			throws IOException {
		final byte lBuffer[] = new byte[BUFFER_LEN];
		int lRead;
		inOutputJar.putNextEntry(new JarEntry(inEntry));
		while ((lRead = inInputJar.read(lBuffer, 0, BUFFER_LEN)) != -1) {
			inOutputJar.write(lBuffer, 0, lRead);
		}
	}

}
