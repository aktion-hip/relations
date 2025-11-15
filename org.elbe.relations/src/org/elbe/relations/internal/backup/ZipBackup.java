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
    private static final int LEN = 2048;

    private final File dataDirectory;
    private final File backupFileName;
    private final String parent;

    /** ZipBackup constructor
     *
     * @param dataDirectory String The path to directory where the embedded databases are stored.
     * @param backupFileName String The fully qualified name of the backup file (Zip file). */
    public ZipBackup(final String dataDirectory, final String backupFileName) {
        this.dataDirectory = new File(dataDirectory);
        this.parent = this.dataDirectory.getName();
        this.backupFileName = new File(backupFileName);
    }

    /**
     * Executes the backup of the actual embedded database.
     *
     * @throws IOException
     */
    public void backup() throws IOException {
        if (!this.dataDirectory.exists()) {
            return;
        }

        try (FileOutputStream output = new FileOutputStream(this.backupFileName);) {
            final ZipOutputStream zipOut = new ZipOutputStream(output);
            traverse(this.dataDirectory, this.parent, zipOut);
            zipOut.close();
        }
    }

    private void traverse(final File directory, final String prefix, final ZipOutputStream out) throws IOException {
        final File[] children = directory.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].isDirectory()) {
                    traverse(children[i], prefix + File.separator + children[i].getName(), out);
                } else {
                    process(children[i], prefix, out);
                }
            }
        }
    }

    private void process(final File file, final String prefix, final ZipOutputStream out) throws IOException {
        final ZipEntry entry = new ZipEntry(prefix + File.separator + file.getName());

        try (FileInputStream input = new FileInputStream(file)) {
            out.putNextEntry(entry);

            final BufferedInputStream inputBuffer = new BufferedInputStream(input, LEN);
            final byte[] transfer = new byte[LEN];
            int read = 0;
            while ((read = inputBuffer.read(transfer, 0, LEN)) != -1) {
                out.write(transfer, 0, read);
            }
            out.closeEntry();
        }
    }

}
