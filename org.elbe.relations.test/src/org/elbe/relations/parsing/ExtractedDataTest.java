package org.elbe.relations.parsing;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test
 * 
 * @author lbenno
 */
public class ExtractedDataTest {
	private static final String NL = System.getProperty("line.separator");

	private Locale localeOld;

	@Before
	public void setUp() throws Exception {
		localeOld = Locale.getDefault();
		Locale.setDefault(Locale.US);
	}

	@After
	public void tearDown() throws Exception {
		Locale.setDefault(localeOld);
	}

	@Test
	public void testGetText() {
		ExtractedData lExtracted = new ExtractedData();

		lExtracted.setFilePath("c:\\data\\test\\testImage.jpg");
		String lExpcected = "[<i>File: c:\\data\\test\\testImage.jpg</i>]";
		assertEquals("file path only", lExpcected, lExtracted.getText());

		lExtracted.setFileSize(3000456L);
		lExpcected = "[<i>File: c:\\data\\test\\testImage.jpg;" + NL
		        + "Size: 2,930.13 kB</i>]";
		assertEquals("path - size", lExpcected, lExtracted.getText());

		lExtracted.setFileType("image/jpeg");
		lExpcected = "[<i>File: c:\\data\\test\\testImage.jpg;" + NL
		        + "Size: 2,930.13 kB;" + NL + "Type: image/jpeg</i>]";
		assertEquals("path - size - type", lExpcected, lExtracted.getText());

		final long lMillis = 1300000000000L;
		lExtracted.setDateCreated(lMillis);
		lExpcected = "[<i>File: c:\\data\\test\\testImage.jpg;" + NL
		        + "Size: 2,930.13 kB;" + NL + "Type: image/jpeg;" + NL
		        + "Created: March 13, 2011, 8:06:40 AM CET</i>]";
		assertEquals("path - size - type - created", lExpcected,
		        lExtracted.getText());

		//
		lExtracted = new ExtractedData();
		lExtracted.setFileType("image/jpeg");
		lExpcected = "[<i>Type: image/jpeg</i>]";
		assertEquals("type only", lExpcected, lExtracted.getText());

		lExtracted.setFileSize(3000456L);
		lExpcected = "[<i>Size: 2,930.13 kB;" + NL + "Type: image/jpeg</i>]";
		assertEquals("size - type", lExpcected, lExtracted.getText());
	}

}
