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
package org.elbe.relations.services;

import java.io.File;
import java.io.IOException;

import org.elbe.relations.parsing.ExtractedData;

/**
 * Interface for metadata adapter classes, i.e. classes that can extract
 * metadata information from dropped files.
 * 
 * @author Luthiger Created on 14.01.2010
 */
public interface IExtractorAdapter {

	/**
	 * Tests whether this extractor can process the specified file.
	 * 
	 * @param inFile
	 *            File, the file to test.
	 * @return boolean <code>true</code> if the adapter is able to extract
	 *         metadata from the file.
	 */
	public boolean acceptsFile(File inFile);

	/**
	 * Processes the file and returns the extracted metadata.
	 * 
	 * @param inFile
	 *            , the file to process-
	 * @return {@link ExtractedData}
	 * @throws IOException
	 */
	public ExtractedData process(File inFile) throws IOException;

}
