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
		final List<String> lExpectedList = Arrays
		        .asList(ZipHouseKeeper.EXPECTED_NAMES);

		final File lRoot = ZipHouseKeeper.createFiles();
		final File lBackupFile = new File(ZipHouseKeeper.ZIP_FILE);

		final ZipBackup lBackup = new ZipBackup(lRoot.getCanonicalPath(),
		        lBackupFile.getCanonicalPath());
		lBackup.backup();

		assertTrue("backup file exists", lBackupFile.exists());

		final ZipFile lZip = new ZipFile(lBackupFile);
		assertEquals("number of entries", 4, lZip.size());
		for (final Enumeration<? extends ZipEntry> lEntries = lZip.entries(); lEntries
		        .hasMoreElements();) {
			final ZipEntry lEntry = lEntries.nextElement();
			final String lName = lEntry.getName();
			assertTrue("containes " + lName, lExpectedList.contains(lName));
		}
		lZip.close();
		ZipHouseKeeper.ensureDelete(lBackupFile);
	}

	@Test
	public void testRestore() throws Exception {
		File lRoot = ZipHouseKeeper.createFiles();
		final File lBackupFile = new File(ZipHouseKeeper.ZIP_FILE);

		final ZipBackup lBackup = new ZipBackup(lRoot.getCanonicalPath(),
		        lBackupFile.getCanonicalPath());
		lBackup.backup();

		assertTrue("backup file exists", lBackupFile.exists());
		ZipHouseKeeper.deleteTestFiles(ZipHouseKeeper.ROOT);

		// after creating the Zip file, we can test expanding it and restoring
		// it's content.
		final ZipRestore lRestore = new ZipRestore(lRoot.getParentFile(),
		        lBackupFile.getCanonicalPath(), log);
		lRestore.restore();

		// check the extracted files
		// content of test root
		lRoot = new File(ZipHouseKeeper.ROOT);
		assertTrue("root exists", lRoot.exists());
		assertTrue("root is directory", lRoot.isDirectory());

		File[] lChilds = lRoot.listFiles();
		Collection<String> lChildList = ZipHouseKeeper.getChildNames(lChilds);
		assertTrue("root contains parent",
		        lChildList.contains(ZipHouseKeeper.PARENT));
		assertTrue("root contains child1",
		        lChildList.contains(ZipHouseKeeper.FILE1));

		File lChild = ZipHouseKeeper
		        .getChildFile(lChilds, ZipHouseKeeper.FILE1);
		ZipHouseKeeper.assertFileContent("content 1", lChild,
		        ZipHouseKeeper.EXPECTED_CONTENT[0]);

		// content of test sub
		lRoot = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.PARENT);
		lChilds = lRoot.listFiles();
		lChildList = ZipHouseKeeper.getChildNames(lChilds);
		assertTrue("parent contains sub",
		        lChildList.contains(ZipHouseKeeper.CHILD));
		assertTrue("parent contains child2",
		        lChildList.contains(ZipHouseKeeper.FILE2));
		assertTrue("parent contains child4",
		        lChildList.contains(ZipHouseKeeper.FILE4));

		lChild = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.FILE2);
		ZipHouseKeeper.assertFileContent("content 2", lChild,
		        ZipHouseKeeper.EXPECTED_CONTENT[1]);
		lChild = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.FILE4);
		ZipHouseKeeper.assertFileContent("content 4", lChild,
		        ZipHouseKeeper.EXPECTED_CONTENT[3]);

		// content of test sub sub
		lRoot = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.CHILD);
		lChilds = lRoot.listFiles();
		lChildList = ZipHouseKeeper.getChildNames(lChilds);
		assertTrue("sub contains child3",
		        lChildList.contains(ZipHouseKeeper.FILE3));

		lChild = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.FILE3);
		ZipHouseKeeper.assertFileContent("content 3", lChild,
		        ZipHouseKeeper.EXPECTED_CONTENT[2]);

		ZipHouseKeeper.ensureDelete(lBackupFile);
	}

	@Test
	public void testCheckArchive() throws Exception {
		final File lRoot = ZipHouseKeeper.createFiles();
		final File lBackupFile = new File(ZipHouseKeeper.ZIP_FILE);

		final ZipBackup lBackup = new ZipBackup(lRoot.getCanonicalPath(),
		        lBackupFile.getCanonicalPath());
		lBackup.backup();

		assertTrue("backup file exists", lBackupFile.exists());
		ZipHouseKeeper.deleteTestFiles(ZipHouseKeeper.ROOT);

		// after creating the Zip file, we can test expanding it and restoring
		// it's content.
		final ZipRestore lRestore = new ZipRestore(lRoot.getParentFile(),
		        lBackupFile.getCanonicalPath(), log);
		assertTrue(ZipHouseKeeper.ROOT + " is ok",
		        lRestore.checkArchive(ZipHouseKeeper.ROOT));
		assertFalse("something is not ok", lRestore.checkArchive("something"));
		assertFalse("shortened root is not ok",
		        lRestore.checkArchive(ZipHouseKeeper.ROOT.substring(0,
		                ZipHouseKeeper.ROOT.length() - 1)));

		ZipHouseKeeper.ensureDelete(lBackupFile);
	}

}
