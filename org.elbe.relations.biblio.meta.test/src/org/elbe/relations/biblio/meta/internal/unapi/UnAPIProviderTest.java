package org.elbe.relations.biblio.meta.internal.unapi;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.elbe.relations.parsing.XPathHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Luthiger Created on 29.12.2009
 */
public class UnAPIProviderTest {
	private static final String TEST_FILE = "resources/unapi_test.xhtml";
	private XPathHelper html;

	@Before
	public void setUp() throws Exception {
		final File lFile = new File(TEST_FILE);
		html = XPathHelper.newInstance(lFile.toURI().toURL());
	}

	@Test
	public void testUnAPITags() throws Exception {
		String lExpected = "http://localhost:8082/silva/static_sites/glp/unAPI_entry";
		assertEquals("server url", lExpected,
		        html.getAttribute(UnAPIProvider.UNAPI_SERVER, "href"));

		lExpected = "http://localhost:8082/silva/static_sites/glp/unAPI_entry/99";
		assertEquals("entry id", lExpected,
		        html.getAttribute(UnAPIProvider.UNAPI_ENTRY_ID, "title"));
	}

}
