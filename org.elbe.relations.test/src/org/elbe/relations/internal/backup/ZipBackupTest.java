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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.ZipHouseKeeper;
import org.elbe.relations.internal.utility.ZipRestore;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit test
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class ZipBackupTest {

    @Mock
    private Logger log;

    @After
    public void tearDown() throws Exception {
        ZipHouseKeeper.deleteTestFiles(ZipHouseKeeper.ROOT);
    }

    @Test
    public void testCreate() throws Exception {
        final List<String> expectedList = Arrays
                .asList(ZipHouseKeeper.EXPECTED_NAMES);

        final File root = ZipHouseKeeper.createFiles();
        final File backupFile = new File(ZipHouseKeeper.ZIP_FILE);

        final ZipBackup backup = new ZipBackup(root.getCanonicalPath(),
                backupFile.getCanonicalPath());
        backup.backup();

        assertTrue("backup file exists", backupFile.exists());

        final ZipFile zip = new ZipFile(backupFile);
        assertEquals("number of entries", 4, zip.size());
        for (final Enumeration<? extends ZipEntry> entries = zip.entries(); entries
                .hasMoreElements();) {
            final ZipEntry entry = entries.nextElement();
            final String name = entry.getName();
            assertTrue("containes " + name, expectedList.contains(name));
        }
        zip.close();
        ZipHouseKeeper.ensureDelete(backupFile);
    }

    @Test
    public void testRestore() throws Exception {
        File root = ZipHouseKeeper.createFiles();
        final File backupFile = new File(ZipHouseKeeper.ZIP_FILE);

        final ZipBackup backup = new ZipBackup(root.getCanonicalPath(),
                backupFile.getCanonicalPath());
        backup.backup();

        assertTrue("backup file exists", backupFile.exists());
        ZipHouseKeeper.deleteTestFiles(ZipHouseKeeper.ROOT);

        // after creating the Zip file, we can test expanding it and restoring
        // it's content.
        final ZipRestore restore = new ZipRestore(root.getParentFile(),
                backupFile.getCanonicalPath(), this.log);
        restore.restore();

        // check the extracted files
        // content of test root
        root = new File(ZipHouseKeeper.ROOT);
        assertTrue("root exists", root.exists());
        assertTrue("root is directory", root.isDirectory());

        File[] childs = root.listFiles();
        Collection<String> childList = ZipHouseKeeper.getChildNames(childs);
        assertTrue("root contains parent",
                childList.contains(ZipHouseKeeper.PARENT));
        assertTrue("root contains child1",
                childList.contains(ZipHouseKeeper.FILE1));

        File child = ZipHouseKeeper
                .getChildFile(childs, ZipHouseKeeper.FILE1);
        ZipHouseKeeper.assertFileContent("content 1", child,
                ZipHouseKeeper.EXPECTED_CONTENT[0]);

        // content of test sub
        root = ZipHouseKeeper.getChildFile(childs, ZipHouseKeeper.PARENT);
        childs = root.listFiles();
        childList = ZipHouseKeeper.getChildNames(childs);
        assertTrue("parent contains sub",
                childList.contains(ZipHouseKeeper.CHILD));
        assertTrue("parent contains child2",
                childList.contains(ZipHouseKeeper.FILE2));
        assertTrue("parent contains child4",
                childList.contains(ZipHouseKeeper.FILE4));

        child = ZipHouseKeeper.getChildFile(childs, ZipHouseKeeper.FILE2);
        ZipHouseKeeper.assertFileContent("content 2", child,
                ZipHouseKeeper.EXPECTED_CONTENT[1]);
        child = ZipHouseKeeper.getChildFile(childs, ZipHouseKeeper.FILE4);
        ZipHouseKeeper.assertFileContent("content 4", child,
                ZipHouseKeeper.EXPECTED_CONTENT[3]);

        // content of test sub sub
        root = ZipHouseKeeper.getChildFile(childs, ZipHouseKeeper.CHILD);
        childs = root.listFiles();
        childList = ZipHouseKeeper.getChildNames(childs);
        assertTrue("sub contains child3",
                childList.contains(ZipHouseKeeper.FILE3));

        child = ZipHouseKeeper.getChildFile(childs, ZipHouseKeeper.FILE3);
        ZipHouseKeeper.assertFileContent("content 3", child,
                ZipHouseKeeper.EXPECTED_CONTENT[2]);

        ZipHouseKeeper.ensureDelete(backupFile);
    }

    @Test
    public void testCheckArchive() throws Exception {
        final File root = ZipHouseKeeper.createFiles();
        final File backupFile = new File(ZipHouseKeeper.ZIP_FILE);

        final ZipBackup backup = new ZipBackup(root.getCanonicalPath(),
                backupFile.getCanonicalPath());
        backup.backup();

        assertTrue("backup file exists", backupFile.exists());
        ZipHouseKeeper.deleteTestFiles(ZipHouseKeeper.ROOT);

        // after creating the Zip file, we can test expanding it and restoring
        // it's content.
        final ZipRestore restore = new ZipRestore(root.getParentFile(),
                backupFile.getCanonicalPath(), this.log);
        assertTrue(ZipHouseKeeper.ROOT + " is ok",
                restore.checkArchive(ZipHouseKeeper.ROOT));
        assertFalse("something is not ok", restore.checkArchive("something"));
        assertFalse("shortened root is not ok",
                restore.checkArchive(ZipHouseKeeper.ROOT.substring(0,
                        ZipHouseKeeper.ROOT.length() - 1)));

        ZipHouseKeeper.ensureDelete(backupFile);
    }

}
