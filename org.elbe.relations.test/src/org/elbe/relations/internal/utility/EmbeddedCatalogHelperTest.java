package org.elbe.relations.internal.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * JUnit Plug-in test
 *
 * @author lbenno
 */
@Disabled
public class EmbeddedCatalogHelperTest {
    private final static String[] CATALOGS = new String[] { "catalog1",
            "catalog2", "catalog3" };
    private static File STORE_DIR;

    @BeforeAll
    public static void before() {
        STORE_DIR = new File(ResourcesPlugin.getWorkspace().getRoot()
                .getLocation().toFile(), RelationsConstants.DERBY_STORE);
    }

    @BeforeEach
    public void setUp() throws Exception {
        createCatalogs(STORE_DIR);
    }

    @AfterEach
    public void tearDown() {
        deleteCatalogs();
    }

    @Test
    public void testValidate() {
        final EmbeddedCatalogHelper lHelper = new EmbeddedCatalogHelper();
        IStatus lStatus = lHelper.validate("test");
        assertEquals("valid input", "OK", lStatus.getMessage());

        lStatus = lHelper.validate(CATALOGS[2]);
        assertEquals("invalid input 1",
                RelationsMessages
                .getString("EmbeddedCatalogHelper.error.exists"),
                lStatus.getMessage());

        lStatus = lHelper.validate("test 96");
        assertEquals("invalid input 2",
                RelationsMessages
                .getString("EmbeddedCatalogHelper.error.chars"),
                lStatus.getMessage());
    }

    @Test
    public void testGetCatalogs() throws Exception {
        String[] lCatalogs = EmbeddedCatalogHelper.getCatalogs();
        assertEquals(3, lCatalogs.length);
        for (int i = 0; i < lCatalogs.length; i++) {
            assertEquals("catalog " + i, CATALOGS[i], lCatalogs[i]);
        }

        // create delete marker in catalog
        final File lCatalog = new File(STORE_DIR, CATALOGS[1]);
        final File lMarker = new File(lCatalog,
                EmbeddedCatalogHelper.DELETED_MARKER);
        lMarker.createNewFile();

        lCatalogs = EmbeddedCatalogHelper.getCatalogs();
        assertEquals(2, lCatalogs.length);
        assertEquals("catalog a", CATALOGS[0], lCatalogs[0]);
        assertEquals("catalog b", CATALOGS[2], lCatalogs[1]);

        EmbeddedCatalogHelper.deleteMarker(CATALOGS[1]);
        assertEquals(3, EmbeddedCatalogHelper.getCatalogs().length);
    }

    @Test
    public void testHasDefaultEmbedded() throws Exception {
        assertFalse(EmbeddedCatalogHelper.hasDefaultEmbedded());

        createCatalog(STORE_DIR, RelationsConstants.DFT_DB_EMBEDDED);
        assertTrue(EmbeddedCatalogHelper.hasDefaultEmbedded());
    }

    private void createCatalogs(final File inStore) {
        createCatalog(inStore, CATALOGS[0]);
        createCatalog(inStore, CATALOGS[1]);
        createCatalog(inStore, CATALOGS[2]);
    }

    private void createCatalog(final File inStore, final String inCatalogName) {
        final File lCatalog = new File(inStore, inCatalogName);
        lCatalog.mkdirs();
    }

    private void deleteCatalogs() {
        final File lDBStore = STORE_DIR;
        if (lDBStore.exists()) {
            traverse(lDBStore);
            ensureDelete(lDBStore);
        }
    }

    private void traverse(final File inDirectory) {
        final File[] lChildren = inDirectory.listFiles();
        for (int i = 0; i < lChildren.length; i++) {
            if (lChildren[i].isDirectory()) {
                traverse(lChildren[i]);
                ensureDelete(lChildren[i]);
            } else {
                ensureDelete(lChildren[i]);

            }
        }
    }

    private void ensureDelete(final File inFile) {
        if (!inFile.delete()) {
            inFile.deleteOnExit();
        }
    }

}
