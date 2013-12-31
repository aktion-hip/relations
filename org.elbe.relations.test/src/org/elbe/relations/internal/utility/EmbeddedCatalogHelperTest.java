package org.elbe.relations.internal.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit Plug-in test
 * 
 * @author lbenno
 */
public class EmbeddedCatalogHelperTest {
	private final static String[] CATALOGS = new String[] { "catalog1",
	        "catalog2", "catalog3" };
	private static File STORE_DIR;

	@BeforeClass
	public static void before() {
		STORE_DIR = new File(ResourcesPlugin.getWorkspace().getRoot()
		        .getLocation().toFile(), RelationsConstants.DERBY_STORE);
	}

	@Before
	public void setUp() throws Exception {
		createCatalogs(STORE_DIR);
	}

	@After
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
		assertEquals("number of catalogs 1", 3, lCatalogs.length);
		for (int i = 0; i < lCatalogs.length; i++) {
			assertEquals("catalog " + i, CATALOGS[i], lCatalogs[i]);
		}

		// create delete marker in catalog
		final File lCatalog = new File(STORE_DIR, CATALOGS[1]);
		final File lMarker = new File(lCatalog,
		        EmbeddedCatalogHelper.DELETED_MARKER);
		lMarker.createNewFile();

		lCatalogs = EmbeddedCatalogHelper.getCatalogs();
		assertEquals("number of catalogs 2", 2, lCatalogs.length);
		assertEquals("catalog a", CATALOGS[0], lCatalogs[0]);
		assertEquals("catalog b", CATALOGS[2], lCatalogs[1]);

		EmbeddedCatalogHelper.deleteMarker(CATALOGS[1]);
		assertEquals("number of catalogs 3", 3,
		        EmbeddedCatalogHelper.getCatalogs().length);
	}

	@Test
	public void testHasDefaultEmbedded() throws Exception {
		assertFalse("no default catalog",
		        EmbeddedCatalogHelper.hasDefaultEmbedded());

		createCatalog(STORE_DIR, RelationsConstants.DFT_DB_EMBEDDED);
		assertTrue("default catalog found",
		        EmbeddedCatalogHelper.hasDefaultEmbedded());
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
