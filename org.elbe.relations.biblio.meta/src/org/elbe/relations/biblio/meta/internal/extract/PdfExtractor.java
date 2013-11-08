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
import java.io.IOException;

import org.elbe.relations.biblio.meta.internal.pdf.PDDocument;
import org.elbe.relations.biblio.meta.internal.pdf.PDDocumentInformation;
import org.elbe.relations.biblio.meta.internal.utility.ExtractorUtil;
import org.elbe.relations.parsing.ExtractedData;
import org.elbe.relations.services.IExtractorAdapter;

/**
 * Adapter to extract metadata from a PDF.
 * 
 * @author Luthiger Created on 21.01.2010
 */
public class PdfExtractor extends AbstractExtractor implements
		IExtractorAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.biblio.meta.internal.extract.AbstractExtractor#
	 * getInputType()
	 */
	@Override
	protected String getInputType() {
		return "application/pdf"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.ds.IExtractorAdapter#acceptsFile(java.io.File)
	 */
	@Override
	public boolean acceptsFile(final File inFile) {
		return ExtractorUtil.checkFileHeader(inFile,
				ExtractorUtil.toHexFilter("%PDF")) || //$NON-NLS-1$
				ExtractorUtil.checkFileHeader(inFile,
						ExtractorUtil.toHexFilter("%pdf")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.ds.IExtractorAdapter#process(java.io.File)
	 */
	@Override
	public ExtractedData process(final File inFile) throws IOException {
		final ExtractedData outExtracted = extractGenericData(inFile);

		PDDocument lPdf = null;
		try {
			lPdf = PDDocument.load(inFile);
			if (lPdf.isEncrypted()) {
				throw new IOException("Can't parse encrypted PDF!"); //$NON-NLS-1$
			}

			final PDDocumentInformation lInfo = lPdf.getDocumentInformation();

			final String lTitle = lInfo.getTitle();
			outExtracted.setTitle(lTitle == null ? inFile.getName() : lTitle);
			outExtracted.setAuthor(lInfo.getAuthor());
			outExtracted.setComment(lInfo.getSubject());
			if (lInfo.getCreationDate() != null) {
				outExtracted.setDateCreated(lInfo.getCreationDate().getTime());
			}
		} finally {
			if (lPdf != null) {
				lPdf.close();
			}
		}

		return outExtracted;
	}

}
