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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.bom.XMLSerializerSpecial;
import org.elbe.relations.services.IPrintOut;

/**
 * Super class for all classes implementing the
 * <code>org.elbe.relations.print.IPrintOut</code> interface.
 *
 * @author Luthiger
 * @see org.elbe.relations.print.IPrintOut
 */
public abstract class AbstractPrintOut implements IPrintOut {
	private static final String NL = "\n"; //$NON-NLS-1$
	private static final String KEY_XSL_PARAMETER_1 = "RelatedWithLbl"; //$NON-NLS-1$
	private static final String KEY_XSL_PARAMETER_2 = "TocLbl"; //$NON-NLS-1$
	private static final String XML_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><docBody><docTitle>%s</docTitle><docSubTitle>%s</docSubTitle></docBody>"; //$NON-NLS-1$
	private static final String CONTENT_PATTERN = XMLSerializerSpecial.PARA_START
	        + "(.*?)" + XMLSerializerSpecial.PARA_END;
	private static final String MSG_RELATED_WITH = RelationsMessages
	        .getString("AbstractPrintOut.section.intro"); //$NON-NLS-1$
	private static final String MSG_TOC_LBL = RelationsMessages
	        .getString("AbstractPrintOut.toc"); //$NON-NLS-1$

	private String docTitle = ""; //$NON-NLS-1$
	private String docSubTitle = ""; //$NON-NLS-1$
	private File outputFile;
	private Pattern contentPattern;

	/**
	 * @param inFileName
	 *            String the name of the document created to print out the
	 *            selected content.
	 * @throws TransformerException
	 * @throws IOException
	 * @see IPrintOut#openNew(String)
	 */
	@Override
	public void openNew(final String inFileName)
	        throws IOException, TransformerException {
		final File lFile = new File(inFileName);

		deleteExisting(lFile);

		if (!lFile.exists() && lFile.getParentFile().exists()) {
			outputFile = lFile;
			manageAfterOpenNew(outputFile);
			if (docTitle.length() != 0) {
				printDocBody(creatBodyXML());
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (outputFile != null) {
			manageBeforeClose(outputFile);
		}
	}

	@Override
	public void openAppend(final String inFileName) throws IOException {
		final File lFile = new File(inFileName);
		if (lFile.exists()) {
			manageAfterReopen(lFile);
		}
	}

	private void printDocBody(final String inXML)
	        throws TransformerException, IOException {
		final TransformerProxy lTransformer = new TransformerProxy(
		        openURL(getXSLNameBody()), inXML, getStylesheetParameters());
		final StringWriter lResult = new StringWriter();
		lTransformer.renderToStream(lResult);
		insertDocBody(lResult.toString());
	}

	private String creatBodyXML() {
		return String.format(XML_TEMPLATE, docTitle, docSubTitle);
	}

	@Override
	public void setDocTitle(final String inDocTitle) throws IOException {
		docTitle = inDocTitle;
	}

	protected String getDocTitle() {
		return docTitle;
	}

	@Override
	public void setDocSubTitle(final String inDocSubtitle) throws IOException {
		docSubTitle = inDocSubtitle;
	}

	/**
	 * Ensures that the specified file is deleted.
	 *
	 * @param inFile
	 *            the File to delete if it exists.
	 * @return boolean <code>true</code> if the specified file doesn't exist
	 *         anymore.
	 */
	protected boolean deleteExisting(final File inFile) {
		if (inFile.exists()) {
			return inFile.delete();
		}
		return true;
	}

	/**
	 * Creates a <code>IOException</code> out of the provided
	 * <code>Throwable</code> passing the exception cause.
	 *
	 * @param inThrowable
	 *            Throwable
	 * @return IOException
	 */
	protected IOException createIOException(final Throwable inThrowable) {
		final IOException outException = new IOException(
		        inThrowable.getMessage());
		outException.initCause(inThrowable);
		return outException;
	}

	@Override
	public void printItem(final String inXML)
	        throws TransformerException, IOException {
		final TransformerProxy transformer = new TransformerProxy(
		        openURL(getXSLNameContent()), prepareItemXML(inXML),
		        getStylesheetParameters());
		final StringWriter result = new StringWriter();
		transformer.renderToStream(result);
		insertSection(result.toString());
	}

	/**
	 * Prepare the item's XML before it is transformed.<br/>
	 * Default behavior: In the text node, all line breaks are replaced by a
	 * <code>&lt;br/></code> tag.
	 *
	 * @param itemXML
	 *            String
	 * @return String
	 */
	protected String prepareItemXML(String itemXML) {
		final Matcher matcher = getContentPattern().matcher(itemXML);
		final StringBuffer out = new StringBuffer(itemXML.length());
		while (matcher.find()) {
			matcher.appendReplacement(out,
			        XMLSerializerSpecial.PARA_START + matcher.group(1)
			                .replace("$", "\\$").replace(NL, "<br/>")
			                + XMLSerializerSpecial.PARA_END);
		}
		matcher.appendTail(out);
		return out.toString().replace(NL, "");
	}

	private Pattern getContentPattern() {
		if (contentPattern == null) {
			contentPattern = Pattern.compile(CONTENT_PATTERN,
			        Pattern.DOTALL | Pattern.MULTILINE);
		}
		return contentPattern;
	}

	private HashMap<String, Object> getStylesheetParameters() {
		final HashMap<String, Object> outParameters = new HashMap<String, Object>();
		outParameters.put(KEY_XSL_PARAMETER_1, MSG_RELATED_WITH);
		outParameters.put(KEY_XSL_PARAMETER_2, MSG_TOC_LBL);
		return outParameters;
	}

	/**
	 * Gets the file with the specified name as URL.
	 *
	 * @param inFileName
	 *            String
	 * @return {@link URL}
	 */
	private URL openURL(final String inFileName) {
		return getClass().getResource(inFileName);
	}

	/**
	 * @return String The XSL file to transform the document body, i.e. title
	 *         and subtitle.
	 */
	abstract protected String getXSLNameBody();

	/**
	 * @return String The XSL file to transform the document content, i.e. the
	 *         items.
	 */
	abstract protected String getXSLNameContent();

	/**
	 * Hook for subclasses: At this time, the print out manager guarantees that
	 * the specified file is ready to be created, i.e. there does not exist a
	 * file with the same name in the file system. It's the duty of the subclass
	 * to create and open the print out file.
	 *
	 * @param inPrintOut
	 *            File to print out the content.
	 * @throws IOException
	 */
	abstract protected void manageAfterOpenNew(File inPrintOut)
	        throws IOException;

	abstract protected void manageAfterReopen(File inPrintOut)
	        throws IOException;

	abstract protected void manageBeforeClose(File inPrintOut)
	        throws IOException;

	/**
	 * Hook for subclasses: Insert the formatted section. It's the duty of the
	 * subclasses to insert the passed section of formated content into the open
	 * print out file.
	 *
	 * @param inSection
	 *            String
	 * @throws IOException
	 */
	abstract protected void insertSection(String inSection) throws IOException;

	/**
	 * Hook for subclasses: Insert the formatted document body. It's the duty of
	 * the subclasses to insert the passed text into the open print out file.
	 *
	 * @param inXML
	 *            String
	 * @throws IOException
	 */
	abstract protected void insertDocBody(String inXML) throws IOException;

}
