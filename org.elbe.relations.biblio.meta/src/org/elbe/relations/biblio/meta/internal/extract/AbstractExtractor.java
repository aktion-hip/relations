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
import java.util.Collection;

import org.elbe.relations.biblio.meta.internal.utility.FileDataSource;
import org.elbe.relations.parsing.ExtractedData;

/**
 * Abstract extractor class providing generic functionality for extractor classes.
 *
 * @author Luthiger
 * Created on 14.01.2010
 */
public abstract class AbstractExtractor {
	private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	/**
	 * Closes the data source catching all exceptions.
	 * 
	 * @param inSource {@link FileDataSource}
	 */
	protected void close(FileDataSource inSource) {
		try {
			inSource.close();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Sets the generic data about the file, i.e. all except comment and created date.
	 * Sets as default value for the title the file name.
	 * 
	 * @param inFile
	 * @return ExtractedData
	 */
	protected ExtractedData extractGenericData(File inFile) {
		ExtractedData outExtracted = new ExtractedData();
		outExtracted.setTitle(inFile.getName());
		outExtracted.setFileSize(inFile.length());
		outExtracted.setFileType(getInputType());
		outExtracted.setDateModified(inFile.lastModified());
		try {
			outExtracted.setFilePath(inFile.getCanonicalPath());
		} catch (IOException exc) {
			outExtracted.setFilePath(inFile.getAbsolutePath());
		}
		return outExtracted;
	}

	
	/**
	 * @return String the dropped file's MIME type.
	 */
	protected abstract String getInputType();
	
	/**
	 * Checked addition of the field content to the text.
	 * Adds for each non empty field a text line.
	 * 
	 * @param inText StringBuilder
	 * @param inField String
	 */
	protected void addPart(StringBuilder inText, String inField) {
		if (inField != null && inField.length() > 0) {
			inText.append(inField).append(NL);
		}
	}		

	/**
	 * @param inItems Collection<String>
	 * @param inDelimiter String
	 * @return String the joined items
	 */
	protected String join(Collection<String> inItems, String inDelimiter) {
		StringBuilder out = new StringBuilder();
		boolean lFirst = false;
		for (String lItem : inItems) {
			if (!lFirst) {
				out.append(inDelimiter);
			}
			out.append(lItem);
		}
		return new String(out).trim();
	}
	
}
