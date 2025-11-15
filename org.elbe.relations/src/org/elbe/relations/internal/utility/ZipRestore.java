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
    private static final int LEN = 2048;

    private final File dataStore;
    private final File archive;
    private final Logger log;

    /** ZipRestore constructor.
     *
     * @param dataStore File the directory containing the embedded databases.
     * @param archiveName String fully qualified name of the Zip file containing the backuped database.
     * @param log {@link Logger} */
    public ZipRestore(final File dataStore, final String archiveName, final Logger log) {
        this.dataStore = dataStore;
        this.archive = new File(archiveName);
        this.log = log;
    }

    /**
     * Starts the data restore.
     *
     * @throws IOException
     */
    public void restore() throws IOException {
        try (ZipFile zip = new ZipFile(this.archive)) {
            final Enumeration<?> entries = zip.entries();
            while (entries.hasMoreElements()) {
                process((ZipEntry) entries.nextElement(), zip, this.dataStore);
            }
        }
    }

    private void process(final ZipEntry inEntry, final ZipFile inZip,
            final File inParent) throws IOException {

        final File newFile = new File(inParent, getName(inEntry.getName()));
        if (!newFile.exists()) {
            createFile(newFile);
        }

        final byte[] transfer = new byte[LEN];
        int read = 0;

        try (BufferedInputStream bufferIn = new BufferedInputStream(inZip.getInputStream(inEntry))) {
            final FileOutputStream out = new FileOutputStream(newFile);
            final BufferedOutputStream bufferOut = new BufferedOutputStream(out, LEN);
            while ((read = bufferIn.read(transfer, 0, LEN)) != -1) {
                bufferOut.write(transfer, 0, read);
            }
            bufferOut.flush();
            bufferOut.close();
        }
    }

    protected String getName(final String inEntryName) {
        return inEntryName;
    }

    private boolean createFile(final File file) throws IOException {
        final File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        return file.createNewFile();
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
            final ZipFile lZip = new ZipFile(this.archive);
            final String lName = lZip.entries().nextElement()
                    .getName();
            if (lName.startsWith(inCatalog + File.separator)) {
                out = true;
            }
            lZip.close();
        }
        catch (final Exception exc) {
            this.log.error(exc, exc.getMessage());
        }
        return out;
    }

}
