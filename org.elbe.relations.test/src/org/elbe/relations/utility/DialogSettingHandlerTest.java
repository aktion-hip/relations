package org.elbe.relations.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * JUnit test
 *
 * @author lbenno
 */
@Disabled("Must be run as JUnit Plug-in Test")
class DialogSettingHandlerTest {
    private static final String[] TEST_INPUT = new String[] { "first value", "second VALUE" };

    @Test
    void testGetRecentValues() throws Exception {
        final DialogSettingHandler handler = new DialogSettingHandler("section", "term");
        String[] lRecent = handler.getRecentValues();
        assertEquals(1, lRecent.length);
        assertEquals("", lRecent[0]);

        handler.saveToHistory(TEST_INPUT[0]);
        lRecent = handler.getRecentValues();
        assertEquals(1, lRecent.length);
        assertEquals("recent value 1", TEST_INPUT[0], lRecent[0]);

        handler.saveToHistory(TEST_INPUT[1]);
        lRecent = handler.getRecentValues();
        assertEquals(2, lRecent.length);
        assertEquals("recent value 2", TEST_INPUT[1], lRecent[0]);
        assertEquals("recent value 3", TEST_INPUT[0], lRecent[1]);

        handler.saveToHistory(TEST_INPUT[0]);
        lRecent = handler.getRecentValues();
        assertEquals(2, lRecent.length);
        assertEquals("recent value 4", TEST_INPUT[0], lRecent[0]);
        assertEquals("recent value 5", TEST_INPUT[1], lRecent[1]);
    }

}
