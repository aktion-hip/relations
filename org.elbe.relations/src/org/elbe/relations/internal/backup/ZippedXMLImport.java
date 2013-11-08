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
package org.elbe.relations.internal.backup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class to import the content of an zipped XML export/backup.
 * 
 * @author Luthiger Created on 27.10.2008
 */
public class ZippedXMLImport extends XMLImport {
	private ZipFile zipFile = null;

	/**
	 * ZippedXMLImport
	 * 
	 * @param inZipFileName
	 *            String must end with <code>.zip</code>.
	 */
	public ZippedXMLImport(final String inZipFileName) {
		super(inZipFileName);
	}

	@Override
	protected Reader getReader() throws IOException {
		zipFile = new ZipFile(getImportFile());
		final ZipEntry lEntry = zipFile.entries().nextElement();

		final InputStreamReader lReader = new InputStreamReader(
				zipFile.getInputStream(lEntry));
		return new BufferedReader(lReader);
	}

	@Override
	protected void close(final Reader inReader) throws IOException {
		super.close(inReader);
		if (zipFile != null) {
			zipFile.close();
		}
	}

}
