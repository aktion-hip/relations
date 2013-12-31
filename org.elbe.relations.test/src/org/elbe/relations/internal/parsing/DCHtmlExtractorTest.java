package org.elbe.relations.internal.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Locale;

import org.elbe.relations.parsing.ExtractedData;
import org.elbe.relations.parsing.XPathHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test
 * 
 * @author lbenno
 */
public class DCHtmlExtractorTest {
	private static final String NL = System.getProperty("line.separator");

	private static final String FILE_NAME1 = "/resources/html_extract1.html";
	private static final String FILE_NAME2 = "/resources/html_extract2.html";

	private Locale localeOld;
	private URL url;

	@Before
	public void setUp() throws Exception {
		url = DCHtmlExtractorTest.class.getResource(FILE_NAME2);

		localeOld = Locale.getDefault();
		Locale.setDefault(Locale.US);
	}

	@After
	public void tearDown() throws Exception {
		Locale.setDefault(localeOld);
	}

	@Test
	public void testCheckDCMeta() throws Exception {
		assertTrue("DC metadata found",
		        DCHtmlExtractor.checkDCMeta(XPathHelper.newInstance(url)));

		assertFalse("no DC metadata",
		        DCHtmlExtractor.checkDCMeta(XPathHelper
		                .newInstance(DCHtmlExtractorTest.class
		                        .getResource(FILE_NAME1))));
	}

	@Test
	public void testExtract() throws Exception {
		final IHtmlExtractor lExtractor = new DCHtmlExtractor();
		final ExtractedData lExtracted = lExtractor
		        .extractData(XPathHelper.newInstance(url), "something",
		                url.toExternalForm());

		assertEquals("Relations: Metadata", lExtracted.getTitle());

		lExtracted.setFilePath("");

		final String lExpected = "Metadata" + NL
		        + "This page is testing Dublin Core matadata." + NL
		        + "[<i>Author: Benno Luthiger;" + NL + "Publisher: Relations;"
		        + NL + "Contributor: John Foo;" + NL + "Type: Text;" + NL
		        + "Created: December 15, 2010, 8:49:37 AM CET</i>]";
		assertEquals(lExpected, lExtracted.getText());

	}

}
