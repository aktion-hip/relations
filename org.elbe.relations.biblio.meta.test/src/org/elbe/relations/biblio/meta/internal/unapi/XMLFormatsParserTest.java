package org.elbe.relations.biblio.meta.internal.unapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Luthiger Created on 29.12.2009
 */
public class XMLFormatsParserTest {
	private static final String FILENAME = "resources/unapi_formats.xml";
	private File xml;

	@Before
	public void setUp() throws Exception {
		xml = new File(FILENAME);
	}

	@Test
	public final void testParse() throws Exception {
		final String[] lExpected = new String[] { "endnote", "bibtex",
		        "oai_dc", "mods" };
		final Collection<String> lFormats = XMLFormatsParser.getInstance()
		        .parse(xml.toURI().toURL());
		assertEquals("size", lExpected.length, lFormats.size());
		for (final String lFormat : lExpected) {
			assertTrue("lookup format", lFormats.contains(lFormat));
		}
	}

}
