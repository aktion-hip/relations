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
 * @author Luthiger Created on 22.01.2010
 */
public class PdfExtractorTest {
	private static final String NL = System.getProperty("line.separator");
	private static final String FILE_NAME = "resources/pdfExtractorTest.pdf";
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
	public void testAcceptsFile() throws Exception {
		final PdfExtractor lExtractor = new PdfExtractor();
		assertTrue("can process", lExtractor.acceptsFile(file));

		assertFalse("can't process dummy",
		        lExtractor.acceptsFile(new File(FILE_NAME2)));
	}

	@Test
	public void testProcess() throws Exception {
		final PdfExtractor lExtractor = new PdfExtractor();
		final ExtractedData lExtracted = lExtractor.process(file);
		lExtracted.setFilePath("");

		assertEquals("extracted title", "Metadata Extractor Test Document",
		        lExtracted.getTitle());

		final String lExpected = "Text Extraction" + NL
		        + "[<i>Author: Luthiger;" + NL + "Size: 16.86 kB;" + NL
		        + "Type: application/pdf;" + NL
		        + "Created: September 6, 2007, 10:21:00 AM CEST;" + NL
		        + "Last Modified: December 15, 2013, 11:02:21 PM CET</i>]";
		assertEquals("extracted text", lExpected, lExtracted.getText());
	}

}
