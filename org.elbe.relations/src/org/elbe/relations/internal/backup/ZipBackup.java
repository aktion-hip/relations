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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class to backup an embedded database. All files of the embedded
 * database actually used are copied to a Zip file and stored at the specified
 * place.
 * 
 * @author Luthiger Created on 04.05.2007
 */
public class ZipBackup {
	private final static int LEN = 2048;

	private final File dataDirectory;
	private final File backupFileName;
	private final String parent;

	/**
	 * ZipBackup constructor
	 * 
	 * @param inDataDirectory
	 *            String The path to directory where the embedded databases are
	 *            stored.
	 * @param inBackupFileName
	 *            String The fully qualified name of the backup file (Zip file).
	 */
	public ZipBackup(final String inDataDirectory, final String inBackupFileName) {
		dataDirectory = new File(inDataDirectory);
		parent = dataDirectory.getName();
		backupFileName = new File(inBackupFileName);
	}

	/**
	 * Executes the backup of the actual embedded database.
	 * 
	 * @throws IOException
	 */
	public void backup() throws IOException {
		if (!dataDirectory.exists())
			return;

		FileOutputStream lOutput = null;
		ZipOutputStream lZipOut = null;

		try {
			lOutput = new FileOutputStream(backupFileName);
			lZipOut = new ZipOutputStream(lOutput);
			traverse(dataDirectory, parent, lZipOut);
		} finally {
			if (lZipOut != null) {
				try {
					lZipOut.close();
				}
				catch (final IOException exc) {
					// intentionally left empty
				}
			}
			if (lOutput != null) {
				try {
					lOutput.close();
				}
				catch (final IOException exc) {
					// intentionally left empty
				}
			}
		}
	}

	private void traverse(final File inDirectory, final String inPrefix,
			final ZipOutputStream inOut) throws IOException {
		final File[] lChildren = inDirectory.listFiles();
		for (int i = 0; i < lChildren.length; i++) {
			if (lChildren[i].isDirectory()) {
				traverse(lChildren[i],
						inPrefix + File.separator + lChildren[i].getName(),
						inOut);
			} else {
				process(lChildren[i], inPrefix, inOut);
			}
		}
	}

	private void process(final File inFile, final String inPrefix,
			final ZipOutputStream inOut) throws IOException {
		FileInputStream lInput = null;
		BufferedInputStream lInputBuffer = null;

		final ZipEntry lEntry = new ZipEntry(inPrefix + File.separator
				+ inFile.getName());

		try {
			inOut.putNextEntry(lEntry);

			lInput = new FileInputStream(inFile);
			lInputBuffer = new BufferedInputStream(lInput, LEN);

			final byte[] lTransfer = new byte[LEN];
			int lRead = 0;
			while ((lRead = lInputBuffer.read(lTransfer, 0, LEN)) != -1) {
				inOut.write(lTransfer, 0, lRead);
			}
			inOut.closeEntry();
		} finally {
			if (lInputBuffer != null) {
				try {
					lInputBuffer.close();
				}
				catch (final IOException exc) {
					// intentionally left empty
				}
			}
			if (lInput != null) {
				try {
					lInput.close();
				}
				catch (final IOException exc) {
					// intentionally left empty
				}
			}
		}
	}

}
