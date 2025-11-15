package org.elbe.relations.internal.utility;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Collection;

import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.ZipHouseKeeper;
import org.elbe.relations.internal.backup.ZipBackup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** JUnit test
 *
 * @author lbenno */
@SuppressWarnings("restriction")
@ExtendWith(MockitoExtension.class)
class ZipImportTest {
    private static final String DESTINATION = "test_import";

    @Mock
    private Logger log;

    @BeforeEach
    void setUp() {
        ZipHouseKeeper.deleteTestFiles(DESTINATION);
    }

    @Test
    void testImport() throws Exception {
        // preparation: we have to create a backup containing the files to import
        File root = ZipHouseKeeper.createFiles();
        final File backupFile = new File(ZipHouseKeeper.ZIP_FILE);
        final ZipBackup backup = new ZipBackup(root.getCanonicalPath(), backupFile.getCanonicalPath());
        backup.backup();

        assertTrue(backupFile.exists());
        // end preparation

        final File lWorkspace = new File("").getAbsoluteFile();
        assertNull(ZipHouseKeeper.getChildFile(lWorkspace.listFiles(), DESTINATION));

        final ZipImport lImport = new ZipImport(lWorkspace, backupFile.getCanonicalPath(), DESTINATION, this.log);
        // here we do the import, i.e. unpack the content of the ZipFile to the destination folder.
        lImport.restore();

        // cleanup
        ZipHouseKeeper.deleteTestFiles(ZipHouseKeeper.ROOT);
        ZipHouseKeeper.ensureDelete(backupFile);

        assertNotNull(ZipHouseKeeper.getChildFile(lWorkspace.listFiles(), DESTINATION));

        // *** the test of the imported directory structure starts here ***
        // check the imported files
        // content of test root
        root = new File(DESTINATION);
        assertTrue(root.exists());
        assertTrue(root.isDirectory());

        File[] lChilds = root.listFiles();
        Collection<String> lChildList = ZipHouseKeeper.getChildNames(lChilds);
        assertTrue(lChildList.contains(ZipHouseKeeper.PARENT));
        assertTrue(lChildList.contains(ZipHouseKeeper.FILE1));

        File lChild = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.FILE1);
        ZipHouseKeeper.assertFileContent("content 1", lChild, ZipHouseKeeper.EXPECTED_CONTENT[0]);

        // content of test sub
        root = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.PARENT);
        lChilds = root.listFiles();
        lChildList = ZipHouseKeeper.getChildNames(lChilds);
        assertTrue(lChildList.contains(ZipHouseKeeper.CHILD));
        assertTrue(lChildList.contains(ZipHouseKeeper.FILE2));
        assertTrue(lChildList.contains(ZipHouseKeeper.FILE4));

        lChild = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.FILE2);
        ZipHouseKeeper.assertFileContent("content 2", lChild, ZipHouseKeeper.EXPECTED_CONTENT[1]);
        lChild = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.FILE4);
        ZipHouseKeeper.assertFileContent("content 4", lChild, ZipHouseKeeper.EXPECTED_CONTENT[3]);

        // content of test sub sub
        root = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.CHILD);
        lChilds = root.listFiles();
        lChildList = ZipHouseKeeper.getChildNames(lChilds);
        assertTrue(lChildList.contains(ZipHouseKeeper.FILE3));

        lChild = ZipHouseKeeper.getChildFile(lChilds, ZipHouseKeeper.FILE3);
        ZipHouseKeeper.assertFileContent("content 3", lChild, ZipHouseKeeper.EXPECTED_CONTENT[2]);

        ZipHouseKeeper.deleteTestFiles(DESTINATION);
    }

}
