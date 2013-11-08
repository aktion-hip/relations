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
package org.elbe.relations.internal.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.e4.core.services.log.Logger;

/**
 * Helper class for restoring an embedded database with the data stored to a Zip
 * file.
 * 
 * @author Luthiger Created on 10.05.2007
 */
@SuppressWarnings("restriction")
public class ZipRestore {
	private final static int LEN = 2048;

	private final File dataStore;
	private final File archive;
	private final Logger log;

	/**
	 * ZipRestore constructor.
	 * 
	 * @param inDataStore
	 *            File the directory containing the embedded databases.
	 * @param inArchiveName
	 *            String fully qualified name of the Zip file containing the
	 *            backuped database.
	 * @param inLog
	 *            {@link Logger}
	 */
	public ZipRestore(final File inDataStore, final String inArchiveName,
			final Logger inLog) {
		dataStore = inDataStore;
		archive = new File(inArchiveName);
		log = inLog;
	}

	/**
	 * Starts the data restore.
	 * 
	 * @throws IOException
	 */
	public void restore() throws IOException {
		final ZipFile lZip = new ZipFile(archive);

		try {
			final Enumeration<?> lEntries = lZip.entries();
			while (lEntries.hasMoreElements()) {
				process((ZipEntry) lEntries.nextElement(), lZip, dataStore);
			}
		} finally {
			lZip.close();
		}
	}

	private void process(final ZipEntry inEntry, final ZipFile inZip,
			final File inParent) throws IOException {
		BufferedInputStream lBufferIn = null;

		FileOutputStream lOut = null;
		BufferedOutputStream lBufferOut = null;

		final File lNew = new File(inParent, getName(inEntry.getName()));
		if (!lNew.exists()) {
			createFile(lNew);
		}

		final byte[] lTransfer = new byte[LEN];
		int lRead = 0;

		try {
			lBufferIn = new BufferedInputStream(inZip.getInputStream(inEntry));
			lOut = new FileOutputStream(lNew);
			lBufferOut = new BufferedOutputStream(lOut, LEN);
			while ((lRead = lBufferIn.read(lTransfer, 0, LEN)) != -1) {
				lBufferOut.write(lTransfer, 0, lRead);
			}
			lBufferOut.flush();
		} finally {
			closeChecked(lBufferOut);
			closeChecked(lOut);
			closeChecked(lBufferIn);
		}
	}

	protected String getName(final String inEntryName) {
		return inEntryName;
	}

	private void closeChecked(final InputStream inInput) {
		if (inInput != null) {
			try {
				inInput.close();
			}
			catch (final IOException exc) {
				// left empty intentionally
			}
		}
	}

	private void closeChecked(final OutputStream inOutput) {
		if (inOutput != null) {
			try {
				inOutput.close();
			}
			catch (final IOException exc) {
				// left empty intentionally
			}
		}
	}

	private void createFile(final File inFile) throws IOException {
		final File lParent = inFile.getParentFile();
		if (!lParent.exists()) {
			lParent.mkdirs();
		}
		inFile.createNewFile();
	}

	/**
	 * Checks the archive for the correct catalog name.
	 * 
	 * @param inCatalog
	 *            String
	 * @return boolean <code>true</code> if the archive contains the backup of
	 *         the specified catalog.
	 */
	public boolean checkArchive(final String inCatalog) {
		boolean out = false;
		try {
			final ZipFile lZip = new ZipFile(archive);
			final String lName = ((ZipEntry) lZip.entries().nextElement())
					.getName();
			if (lName.startsWith(inCatalog + File.separator)) {
				out = true;
			}
			lZip.close();
		}
		catch (final Exception exc) {
			log.error(exc, exc.getMessage());
		}
		return out;
	}

}
