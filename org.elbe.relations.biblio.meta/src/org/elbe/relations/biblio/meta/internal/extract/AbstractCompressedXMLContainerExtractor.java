/*
This package is part of Relations application.
Copyright (C) 2010, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.elbe.relations.biblio.meta.internal.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.elbe.relations.biblio.meta.internal.utility.ExtractorUtil;
import org.elbe.relations.parsing.ExtractedData;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Base class for extractor's of documents that organize their content and metadata in compressed files with xml structure.
 * Notable examples are OpenOffice.org and MS Office 2007 documents.
 *
 * @author Luthiger
 * Created on 25.01.2010
 */
public abstract class AbstractCompressedXMLContainerExtractor extends AbstractExtractor {
	private static final String ZIP_HEADER = "50 4B 03 04 14 00"; //$NON-NLS-1$
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$
	
	/**
	 * Enumeration of listeners for the parser of the document's metadata xml.
	 */
	enum ParserListener {
		TITLE(new IListenerAction() {
			public void process(ExtractedDataAdapter inExtracted, String inContent) {
				inExtracted.setTitle(inContent);
			}				
		}),
		DESCRIPTION(new IListenerAction() {
			public void process(ExtractedDataAdapter inExtracted, String inContent) {
				inExtracted.setDescription(inContent);
			}				
		}),
		SUBJECT(new IListenerAction() {
			public void process(ExtractedDataAdapter inExtracted, String inContent) {
				inExtracted.setSubject(inContent);
			}				
		}),
		CREATION(new IListenerAction() {
			public void process(ExtractedDataAdapter inExtracted, String inContent) {
				inExtracted.setCreationDate(inContent);
			}				
		}),
		AUTHOR(new IListenerAction() {
			public void process(ExtractedDataAdapter inExtracted, String inContent) {
				inExtracted.setAuthor(inContent);
			}				
		}),
		KEYWORD(new IListenerAction() {
			public void process(ExtractedDataAdapter inExtracted, String inContent) {
				inExtracted.setKeyword(inContent);
			}				
		}),
		NOOP(new IListenerAction() {
			public void process(ExtractedDataAdapter inExtracted, String inContent) {
				//do nothing
			}				
		})
		;
		
		private ParserListener parent;
		private IListenerAction action;
		private StringBuilder content = new StringBuilder();
		
		ParserListener(IListenerAction inAction) {
			action = inAction;
		}
		void setParent(ParserListener inParent) {
			parent = inParent;
			content = new StringBuilder();
		}
		ParserListener getParent() {
			return parent;
		}
		void process(ExtractedDataAdapter inExtracted) {
			action.process(inExtracted, new String(content));
		}
		public void addContent(String inContent) {
			content.append(inContent);
		}
	}

	
	/**
	 * Tests whether this extractor can process the specified file.
	 * 
	 * @param inFile File, the file to test.
	 * @return boolean <code>true</code> if the adapter is able to extract metadata from the file.
	 * @see org.elbe.relations.ds.IExtractorAdapter#acceptsFile(java.io.File)
	 */
	public boolean acceptsFile(File inFile) {
		if (!ExtractorUtil.checkFileHeader(inFile, ZIP_HEADER)) return false;
		try {
			return checkMetaEntry(inFile, getMetaEntryName());
		} catch (Exception exc) {
			//intentionally left empty;
		}
		return false;
	}

	/**
	 * @return String the name of the zip entry containing the document's metadata information.
	 */
	protected abstract String getMetaEntryName();

	private boolean checkMetaEntry(File inFile, String inMetaName) throws IOException {
		ZipInputStream lZip = null;
		try {			
			lZip = new ZipInputStream(new FileInputStream(inFile));
			boolean lEoF = false;
			while (!lEoF) {
				ZipEntry lEntry = lZip.getNextEntry();
				if (lEntry != null) {
					if (inMetaName.equals(lEntry.getName().toLowerCase().trim())) {
						return true;
					}
				}
				else {
					lEoF = true;
				}
			}
		}
		finally {
			if (lZip != null) {
				lZip.close();
			}
		}
		return false;
	}
	
	/**
	 * Processes the file and returns the extracted metadata.
	 * 
	 * @param inFile {@link File}
	 * @return ExtractedData
	 * @throws IOException
	 * @see org.elbe.relations.ds.IExtractorAdapter#process(java.io.File)
	 */
	public ExtractedData process(File inFile) throws IOException {
		ExtractedData outExtracted = extractGenericData(inFile);
		
		String lMetaName = getMetaEntryName();
		ZipInputStream lZip = null;
		
		try {
			lZip = new ZipInputStream(new FileInputStream(inFile));
			boolean lEoF = false;
			while (!lEoF) {
				ZipEntry lEntry = lZip.getNextEntry();
				if (lEntry != null) {
					if (lMetaName.equals(lEntry.getName().toLowerCase().trim())) {
						processMetadata(lZip, outExtracted);
						//we're only interested in the metadata
						break;
					}
				}
				else {
					lEoF = true;
				}
			}
		} catch (Exception exc) {
			throw new IOException(exc.getMessage());
		}
		
		return outExtracted;
	}

	private void processMetadata(ZipInputStream inStream, ExtractedData inExtracted) throws Exception {
		SAXParserFactory lFactory = SAXParserFactory.newInstance();
		lFactory.setValidating(false);
		lFactory.setNamespaceAware(true);
		SAXParser lParser = lFactory.newSAXParser();
		lParser.parse(inStream, new MetadataHandler(inExtracted));
	}	

	/**
	 * Factory method: returns the correct <code>ParserListener</code> to listen for the node with the specified tag name. 
	 * 
	 * @param inTagName String the node's tag name.
	 * @return {@link ParserListener}
	 */
	abstract protected ParserListener getParserListener(String inTagName);

	
// --- private classes ---
	
	protected static interface IParserListenerFactory {
		ParserListener getParserListern(String inTagName);
	}

	private class MetadataHandler extends DefaultHandler {

		private ExtractedDataAdapter extracted;
		private ParserListener currentListener;

		public MetadataHandler(ExtractedData inExtracted) {
			extracted = new ExtractedDataAdapter(inExtracted);
			currentListener = ParserListener.NOOP;
		}
		
		@Override
		public void startElement(String inUri, String inTag, String inFullTag, Attributes inAttributes) throws SAXException {
			ParserListener lListener = getParserListener(inFullTag);
			lListener.setParent(currentListener);
			currentListener = lListener;
		}
		
		@Override
		public void endElement(String inUri, String inTag, String inFullTag) throws SAXException {
			currentListener.process(extracted);
			currentListener = currentListener.getParent();
		}
		
		@Override
		public void characters(char[] inCharacters, int inStart, int inLength) throws SAXException {
			currentListener.addContent(new String(inCharacters, inStart, inLength));
		}
		@Override
		public void endDocument() throws SAXException {
			extracted.consolidate();
		}
	}

	static interface IListenerAction {
		void process(ExtractedDataAdapter inExtracted, String inContent);
	}
	
	class ExtractedDataAdapter {
		private ExtractedData extracted;
		
		private String description = ""; //$NON-NLS-1$
		private String subject = ""; //$NON-NLS-1$
		private Collection<String> keywords = new Vector<String>();

		ExtractedDataAdapter(ExtractedData inExtracted) {
			extracted = inExtracted;
		}
		public void setCreationDate(String inContent) {			
			try {
				extracted.setDateCreated(DATE_FORMAT.parse(inContent));
			} catch (ParseException exc) {
				exc.printStackTrace();
			}
		}
		public void setAuthor(String inAuthor) {
			extracted.setAuthor(inAuthor);
		}
		public void setTitle(String inTitle) {
			extracted.setTitle(inTitle);
		}
		public void setSubject(String inContent) {
			subject = inContent;
		}
		public void setDescription(String inContent) {
			description = inContent;
		}
		public void setKeyword(String inContent) {
			keywords.add(inContent);
		}
		public void consolidate() {
			StringBuilder lComment = new StringBuilder();
			addPart(lComment, description);
			addPart(lComment, subject);
			addPart(lComment, join(keywords, " ")); //$NON-NLS-1$
			String lText = new String(lComment).trim();
			if (lText.length() > 0) {
				extracted.setComment(lText);
			}
		}
	}

}
