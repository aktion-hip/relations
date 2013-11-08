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

import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.elbe.relations.parsing.ExtractedData;

/**
 * Base class for extractor classes of MS Office (2003) documents.
 *
 * @author Luthiger
 * Created on 26.01.2010
 */
public abstract class AbstractMSOfficeExtractor extends AbstractExtractor {

	protected static final String OLE_HEADER = "D0 CF 11 E0 A1 B1 1A E1"; //$NON-NLS-1$

	public ExtractedData process(File inFile) throws IOException {
		ExtractedData outExtracted = extractGenericData(inFile);
		
		FileInputStream lStream = null;
		
		POIFSReader lReader = new POIFSReader();
		SummaryReader lListener = new SummaryReader(outExtracted);
		lReader.registerListener(lListener);
		
		try {
			lStream = new FileInputStream(inFile);
			lReader.read(lStream);
		}
		finally {
			if (lStream != null) {
				lStream.close();
			}
		}
		
		return outExtracted;
	}
	
// --- private classes ---

	class SummaryReader implements POIFSReaderListener {

		private ExtractedData extractedData;

		SummaryReader(ExtractedData inExtracted) {
			extractedData = inExtracted;
		}

		@Override
		public void processPOIFSReaderEvent(POIFSReaderEvent inEvent) {
			try {
				PropertySet lPropertySet = PropertySetFactory.create(inEvent.getStream());
				if (lPropertySet instanceof SummaryInformation) {
					SummaryInformation lSummary = (SummaryInformation) lPropertySet;
					extractedData.setTitle(lSummary.getTitle());
					extractedData.setAuthor(lSummary.getAuthor());
					extractedData.setDateCreated(lSummary.getCreateDateTime());
					
					//we process the document's subject, comments and keywords to a single comment
					String lSubject = lSummary.getSubject();
					String lComments = lSummary.getComments();
					String lKeywords = lSummary.getKeywords();
					
					StringBuilder lComment = new StringBuilder();
					addPart(lComment, lSubject);
					addPart(lComment, lComments);
					addPart(lComment, lKeywords);
					String lText = new String(lComment).trim();
					if (lText.length() > 0) {
						extractedData.setComment(lText);
					}
				}
			} 
			catch (Exception exc) {
				//intentionally left empty
			} 
		}
	}

}
