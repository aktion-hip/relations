package org.elbe.relations.internal.utility;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.ZipHouseKeeper;
import org.elbe.relations.internal.backup.ZipBackup;
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
public class ZipImportTest {

	@Mock
	private Logger log;

	@Test
	public void testImport() throws Exception {
		// preparation: we have to create a backup containing the files to
		// import
		File lRoot = ZipHouseKeeper.createFiles();
		final File lBackupFile = new File(ZipHouseKeeper.ZIP_FILE);

		final ZipBackup lBackup = new ZipBackup(lRoot.getCanonicalPath(),
		        lBackupFile.getCanonicalPath());
		lBackup.backup();

		assertTrue("backup file exists", lBackupFile.exists());
		// end preparation

		final File lWorkspace = (new File("")).getAbsoluteFile();
		final String lDestination = "test_import";
		assertNull("destination doesn't exist yet",
		        ZipHouseKeeper.getChildFile(lWorkspace.listFiles(),
		                lDestination));

		final ZipImport lImport = new ZipImport(lWorkspace,
		        lBackupFile.getCanonicalPath(), lDestination, log);
		// here we do the import, i.e. unpack the content of the ZipFile to the
		// destination folder.
		lImport.restore();

		// cleanup
		ZipHouseKeeper.deleteTestFiles(ZipHouseKeeper.ROOT);
		ZipHouseKeeper.ensureDelete(lBackupFile);

		assertNotNull("destination does exist now",
		        ZipHouseKeeper.getChildFile(lWorkspace.listFiles(),
		                lDestination));

		// *** the test of the imported directory structure starts here ***
		// check the imported files
		// content of test root
		lRoot = new File(lDestination);
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

		ZipHouseKeeper.deleteTestFiles(lDestination);
	}

}
