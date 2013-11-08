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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class to backup the actual database to a zipped XML file.
 * 
 * @author Luthiger Created on 26.10.2008
 */
public class ZippedXMLExport extends XMLExport {

	/**
	 * ZippedXMLExport
	 * 
	 * @param inExportFileName
	 *            String must end with <code>.zip</code>.
	 * @param inAppLocale
	 *            {@link Locale} the application's locale
	 * @throws IOException
	 */
	public ZippedXMLExport(final String inExportFileName,
			final Locale inAppLocale) throws IOException {
		super(inExportFileName, inAppLocale);
	}

	@Override
	protected OutputStream createStream(final File inExportFile)
			throws IOException {
		final FileOutputStream lStream = new FileOutputStream(inExportFile);
		final ZipOutputStream lZipped = new ZipOutputStream(lStream);
		final ZipEntry lEntry = new ZipEntry(inExportFile.getName().replaceAll(
				".zip", ".xml")); //$NON-NLS-1$ //$NON-NLS-2$
		lZipped.putNextEntry(lEntry);
		return new BufferedOutputStream(lZipped);
	}

}
