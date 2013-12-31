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
 * @author Luthiger Created on 25.01.2010
 */
public class OfficeXMLExtractorTest {
	private static final String NL = System.getProperty("line.separator");
	private static final String FILE_NAME = "resources/wordxExtractTest.docx";
	private static final String FILE_NAME2 = "resources/openOfficeTest.odt";
	private static final String FILE_NAME3 = "resources/dummy.txt";

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
		final OfficeXMLExtractor lExtractor = new OfficeXMLExtractor();
		assertTrue("can process", lExtractor.acceptsFile(file));

		assertFalse("can't process OO file",
		        lExtractor.acceptsFile(new File(FILE_NAME2)));
		assertFalse("can't process dummy",
		        lExtractor.acceptsFile(new File(FILE_NAME3)));
	}

	@Test
	public void testProcess() throws Exception {
		final OfficeXMLExtractor lExtractor = new OfficeXMLExtractor();
		final ExtractedData lExtracted = lExtractor.process(file);
		assertEquals("extracted title", "Test Extract Word 2007",
		        lExtracted.getTitle());

		lExtracted.setFilePath("");
		final String lExpected = "Use this document to test extraction of metadata."
		        + NL
		        + "How to extract"
		        + NL
		        + "test word extract"
		        + NL
		        + "[<i>Author: Luthiger;"
		        + NL
		        + "Size: 10.16 kB;"
		        + NL
		        + "Type: application/ms-office-1.x;"
		        + NL
		        + "Created: January 24, 2010, 9:56:00 PM CET;"
		        + NL
		        + "Last Modified: December 15, 2013, 11:02:21 PM CET</i>]";
		assertEquals("extracted text", lExpected, lExtracted.getText());

	}

}
