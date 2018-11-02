/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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
 * @author Luthiger
 */
public class ZippedXMLExport extends XMLExport {

	/**
	 * ZippedXMLExport
	 *
	 * @param exportFileName
	 *            String must end with <code>.zip</code>.
	 * @param appLocale
	 *            {@link Locale} the application's locale
	 * @param numberOfItems
	 *            int
	 * @throws IOException
	 */
	public ZippedXMLExport(final String exportFileName, final Locale appLocale,
			final int numberOfItems)
					throws IOException {
		super(exportFileName, appLocale, numberOfItems);
	}

	@Override
	protected OutputStream createStream(final File exportFile)
			throws IOException {
		final FileOutputStream stream = new FileOutputStream(exportFile);
		final ZipOutputStream zipped = new ZipOutputStream(stream);
		final ZipEntry entry = new ZipEntry(exportFile.getName().replaceAll(
				".zip", ".xml")); //$NON-NLS-1$ //$NON-NLS-2$
		zipped.putNextEntry(entry);
		return new BufferedOutputStream(zipped);
	}

}
