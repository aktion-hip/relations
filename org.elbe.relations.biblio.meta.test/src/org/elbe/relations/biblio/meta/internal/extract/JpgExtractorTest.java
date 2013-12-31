package org.elbe.relations.biblio.meta.internal.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import org.elbe.relations.parsing.ExtractedData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Luthiger Created on 14.01.2010
 */
public class JpgExtractorTest {
	private static final String NL = System.getProperty("line.separator");
	private static final String FILE_NAME = "resources/jpgExtractorTest.jpg";
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
	public void tearDown() throws Exception {
		Locale.setDefault(localeOld);
	}

	@Test
	public final void testAcceptsFile() {
		final JpgExtractor lExtractor = new JpgExtractor();
		assertTrue("can process", lExtractor.acceptsFile(file));

		assertFalse("can't process dummy",
		        lExtractor.acceptsFile(new File(FILE_NAME2)));
	}

	@Test
	public final void testProcess() throws IOException {
		final JpgExtractor lExtractor = new JpgExtractor();
		final ExtractedData lExtracted = lExtractor.process(file);
		lExtracted.setFilePath("");
		final String lExpected = "Test Image for Metadata Extractor" + NL
		        + "[<i>Size: 4.18 kB;" + NL + "Type: image/jpeg;" + NL
		        + "Last Modified: December 15, 2013, 11:02:21 PM CET</i>]";
		assertEquals("extracted text", lExpected, lExtracted.getText());
		// System.out.println(lExtracted.getText());
	}

}
