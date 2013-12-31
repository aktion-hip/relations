package org.elbe.relations.biblio.meta.internal.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import org.elbe.relations.parsing.ExtractedData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Luthiger Created on 21.01.2010
 */
public class GifExtractorTest {
	private static final String NL = System.getProperty("line.separator");
	private static final String FILE_NAME = "resources/gifExtractorTest.gif";
	private static final String FILE_NAME2 = "resources/dummy.txt";

	private File file;
	private Locale localeOld;

	@Before
	public void setUp() throws Exception {
		localeOld = Locale.getDefault();
		Locale.setDefault(Locale.US);

		file = new File(FILE_NAME);
		if (!file.exists())
			throw new FileNotFoundException(FILE_NAME);
	}

	@After
	public void tearDown() {
		Locale.setDefault(localeOld);
	}

	@Test
	public void testAcceptsFile() throws Exception {
		final GifExtractor lExtractor = new GifExtractor();
		assertTrue(lExtractor.acceptsFile(file));

		assertFalse("can't process dummy",
		        lExtractor.acceptsFile(new File(FILE_NAME2)));
	}

	@Test
	public void testProcess() throws Exception {
		final GifExtractor lExtractor = new GifExtractor();
		final ExtractedData lExtracted = lExtractor.process(file);
		lExtracted.setFilePath("");
		final String lExpected = "Test GIF fuer Relations" + NL
		        + "[<i>Size: 3.30 kB;" + NL + "Type: image/gif;" + NL
		        + "Last Modified: December 15, 2013, 11:06:32 PM CET</i>]";
		assertEquals("extracted text", lExpected, lExtracted.getText());
	}

}
