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
 * @author Luthiger Created on 23.01.2010
 */
public class OOExtractorTest {
	private static final String NL = System.getProperty("line.separator");
	private static final String FILE_NAME = "resources/openOfficeTest.odt";
	private static final String FILE_NAME2 = "resources/wordxExtractTest.docx";
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
		final OOExtractor lExtractor = new OOExtractor();
		assertTrue("can process", lExtractor.acceptsFile(file));

		assertFalse("can't process OfficeXML",
		        lExtractor.acceptsFile(new File(FILE_NAME2)));
		assertFalse("can't process dummy",
		        lExtractor.acceptsFile(new File(FILE_NAME3)));
	}

	@Test
	public void testProcess() throws Exception {
		final OOExtractor lExtractor = new OOExtractor();
		final ExtractedData lExtracted = lExtractor.process(file);
		assertEquals("extracted title", "Test of Relations Extractor",
		        lExtracted.getTitle());

		lExtracted.setFilePath("");
		final String lExpected = "This comments the Test of Relations Extractor"
		        + NL
		        + "This is the Subject: Test"
		        + NL
		        + "Keywords Test Relations Extractor"
		        + NL
		        + "[<i>Author: Benno Luthiger;"
		        + NL
		        + "Size: 7.18 kB;"
		        + NL
		        + "Type: application/open-office-1.x;"
		        + NL
		        + "Created: January 24, 2010, 12:05:30 AM CET;"
		        + NL
		        + "Last Modified: December 15, 2013, 11:02:21 PM CET</i>]";
		assertEquals("extracted text", lExpected, lExtracted.getText());
	}

}
