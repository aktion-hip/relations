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

import org.elbe.relations.biblio.meta.internal.utility.ExtractorUtil;
import org.elbe.relations.services.IExtractorAdapter;

/**
 * Adapter to extract metadata from a MS PowerPoint file (ppt).
 * 
 * @author Luthiger Created on 26.01.2010
 */
public class PowerPointExtractor extends AbstractMSOfficeExtractor implements
		IExtractorAdapter {
	private static final String PPT_SUFFIX = ".ppt"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.biblio.meta.internal.extract.AbstractExtractor#
	 * getInputType()
	 */
	@Override
	protected String getInputType() {
		return "application/vnd.ms-powerpoint"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.ds.IExtractorAdapter#acceptsFile(java.io.File)
	 */
	@Override
	public boolean acceptsFile(final File inFile) {
		if (isPptFile(inFile)) {
			return ExtractorUtil.checkFileHeader(inFile, OLE_HEADER);
		}
		return false;
	}

	private boolean isPptFile(final File inFile) {
		return inFile.getName().toLowerCase().endsWith(PPT_SUFFIX);
	}

}
