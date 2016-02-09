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
package org.elbe.relations.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.elbe.relations.RelationsMessages;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class providing functionality for print out into zipped structures.
 *
 * @author lbenno
 */
public abstract class AbstractJarPrintOut extends AbstractPrintOut {
	public final static int BUFFER_LEN = 16384;

	private static final String CHARSET = "UTF-8";

	private DocumentBuilder docBuilder = null;

	@Override
	public boolean isAvailable() {
		return true;
	}

	/**
	 * Checks the sanity of the specified file, i.e. if it can be created and
	 * put into read/write mode.
	 *
	 * @param fileToPrint
	 *            {@link File}
	 * @return {@link File}
	 * @throws IOException
	 */
	protected File createChecked(File fileToPrint) throws IOException {
		if (!fileToPrint.createNewFile()) {
			throw new IOException(
			        RelationsMessages.getString("PrintOut.err.creation.fail") //$NON-NLS-1$
			                + fileToPrint.getName());
		}
		if (!fileToPrint.canRead() || !fileToPrint.canWrite()) {
			throw new IOException("Could not open file for read/write: " //$NON-NLS-1$
			        + fileToPrint.getName());
		}
		return fileToPrint;
	}

	/**
	 * Creates a jar output stream for the specified file.
	 *
	 * @param fileToPrint
	 *            {@link File}
	 * @return {@link JarOutputStream}
	 * @throws IOException
	 */
	protected JarOutputStream initJar(final File fileToPrint)
	        throws IOException {
		return new JarOutputStream(new FileOutputStream(fileToPrint));
	}

	protected void addEntry(final JarOutputStream inOutputJar,
	        final String inResourceName, final String inExceptionMsg,
	        final String inPrefix, final JarEntry inEntry) throws IOException {
		final URL resource = getResource(inResourceName, inExceptionMsg);
		final JarEntry entry = inEntry == null
		        ? new JarEntry(inPrefix + getResourceName(resource)) : inEntry;
		inOutputJar.putNextEntry(entry);

		try (InputStream input = resource.openStream()) {
			final byte buffer[] = new byte[BUFFER_LEN];
			int read;
			while ((read = input.read(buffer, 0, BUFFER_LEN)) != -1) {
				inOutputJar.write(buffer, 0, read);
			}
		}
	}

	protected void addEntry(final JarOutputStream inOutputJar,
	        final String inResourceName, final String inExceptionMsg)
	                throws IOException {
		addEntry(inOutputJar, inResourceName, inExceptionMsg, "", null); //$NON-NLS-1$
	}

	protected URL getResource(final String inResourceName,
	        final String inExceptionMsg) throws IOException {
		final URL outUrl = getClass().getResource(inResourceName);
		if (outUrl == null) {
			throw new IOException(inExceptionMsg);
		}
		return outUrl;
	}

	protected String getResourceName(final URL inResource) {
		final String[] parts = inResource.getPath().split("/"); //$NON-NLS-1$
		return parts[parts.length - 1];
	}

	protected Document createDoc(final String inXML)
	        throws SAXException, IOException, ParserConfigurationException {
		try (final StringReader lReader = new StringReader(inXML)) {
			return getDocumentBuilder().parse(new InputSource(lReader));
		}
	}

	protected DocumentBuilder getDocumentBuilder()
	        throws ParserConfigurationException {
		if (docBuilder == null) {
			docBuilder = DocumentBuilderFactory.newInstance()
			        .newDocumentBuilder();
		}
		return docBuilder;
	}

	/**
	 * Reads the file (i.e. template) with the specified name.
	 *
	 * @param templateName
	 *            String
	 * @param content
	 *            Object[] the content to fill into the template
	 * @return String the final content, i.e. the template filled with the
	 *         content snippets
	 */
	protected String formatPart(String templateName, Object... content) {
		final InputStream input = getClass().getResourceAsStream(templateName);
		try (Scanner scanner = new Scanner(input, CHARSET)) {
			final String template = scanner.useDelimiter("\\A").next();
			return String.format(template, content);
		}
	}

	protected OutputObj manageAfterReopen(final File inPrintOut,
	        String inContentName, File inOutputFile) throws IOException {
		final OutputObj out = new OutputObj();
		final File temporary = File.createTempFile("~$rel", null, //$NON-NLS-1$
		        inOutputFile.getParentFile());
		try {
			// rename existing print out to temporary and create new empty print
			// out
			temporary.delete();
			if (!inOutputFile.renameTo(temporary)) {
				throw new IOException(RelationsMessages
				        .getString("PrintOut.err.reopen.fail")); //$NON-NLS-1$
			}
			inOutputFile = createChecked(inOutputFile);

			// copy existing jar entries to new version of print out, except
			// content.xml
			final JarInputStream inputJar = new JarInputStream(
			        new FileInputStream(temporary));
			final Manifest manifest = inputJar.getManifest();
			if (manifest == null) {
				out.jar = new JarOutputStream(
				        new FileOutputStream(inOutputFile));
			} else {
				out.jar = new JarOutputStream(
				        new FileOutputStream(inOutputFile), manifest);
			}
			JarEntry entryIn;
			while ((entryIn = inputJar.getNextJarEntry()) != null) {
				if (!entryIn.getName().equals(inContentName)) {
					copyEntry(entryIn, inputJar, out.getJar());
				}
			}

			// retrieve content.xml and create a document
			try (JarInputStream inputJar2 = new JarInputStream(
			        new FileInputStream(temporary))) {
				while ((entryIn = inputJar2.getNextJarEntry()) != null
				        && (!entryIn.getName().equals(inContentName))) {
					// do nothing, just seek content.xml
				}
				out.doc = getDocumentBuilder().parse(inputJar2);
			}
			catch (SAXException | ParserConfigurationException exc) {
				new IOException(exc.getMessage());
			}
		}
		finally {
			// in case of an IOException, we make sure that temporary resources
			// are deleted.
			if (temporary != null && temporary.exists()) {
				temporary.deleteOnExit();
			}
		}
		return out;
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

	/**
	 * Adds a new part to the output.
	 *
	 * @param inEntryContent
	 *            String the entry's content
	 * @param inEntryName
	 *            String the entry's path and name
	 * @param inOutputJar
	 *            {@link JarOutputStream}
	 * @throws IOException
	 */
	protected void addEntry(String inEntryContent, String inEntryName,
	        JarOutputStream inOutputJar) throws IOException {
		final JarEntry entry = new JarEntry(inEntryName);
		inOutputJar.putNextEntry(entry);

		try (InputStream input = new ByteArrayInputStream(
		        inEntryContent.getBytes(CHARSET))) {
			final byte buffer[] = new byte[BUFFER_LEN];
			int read;
			while ((read = input.read(buffer, 0, BUFFER_LEN)) != -1) {
				inOutputJar.write(buffer, 0, read);
			}
		}
	}

	protected String getMetaDescription() {
		return RelationsMessages.getString("PrintOut.msg.meta.desc");
	}

	protected String getMetaSubject() {
		return RelationsMessages.getString("PrintOut.msg.meta.subject");
	}

	// --- private classes ---
	/**
	 * Parameter object.
	 *
	 */
	protected static class OutputObj {
		private JarOutputStream jar;
		private Document doc;

		public Document getDoc() {
			return doc;
		}

		public JarOutputStream getJar() {
			return jar;
		}
	}

}
