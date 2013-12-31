package org.elbe.relations.utility;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * JUnit test
 * 
 * @author lbenno
 */
public class DialogSettingHandlerTest {
	private final static String[] TEST_INPUT = new String[] { "first value",
	        "second VALUE" };

	@Test
	public void testGetRecentValues() throws Exception {
		final DialogSettingHandler lHandler = new DialogSettingHandler(
		        "section", "term");
		String[] lRecent = lHandler.getRecentValues();
		assertEquals(1, lRecent.length);
		assertEquals("", lRecent[0]);

		lHandler.saveToHistory(TEST_INPUT[0]);
		lRecent = lHandler.getRecentValues();
		assertEquals("number of settings 1", 1, lRecent.length);
		assertEquals("recent value 1", TEST_INPUT[0], lRecent[0]);

		lHandler.saveToHistory(TEST_INPUT[1]);
		lRecent = lHandler.getRecentValues();
		assertEquals("number of settings 2", 2, lRecent.length);
		assertEquals("recent value 2", TEST_INPUT[1], lRecent[0]);
		assertEquals("recent value 3", TEST_INPUT[0], lRecent[1]);

		lHandler.saveToHistory(TEST_INPUT[0]);
		lRecent = lHandler.getRecentValues();
		assertEquals("number of settings 3", 2, lRecent.length);
		assertEquals("recent value 4", TEST_INPUT[0], lRecent[0]);
		assertEquals("recent value 5", TEST_INPUT[1], lRecent[1]);
	}

}
