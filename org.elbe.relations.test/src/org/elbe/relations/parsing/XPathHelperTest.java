package org.elbe.relations.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.elbe.relations.parsing.XPathHelper.XmlSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit test
 *
 * @author lbenno
 */
class XPathHelperTest {
    private static final String XPATH_COINS = "//span[@class='Z3988']";
    private static final String TEST_FILE = "/resources/xpath_test1.html";
    private static final String TEST_FILE2 = "/resources/xpath_test2.html";

    private XPathHelper helper;

    @BeforeEach
    void setUp() throws Exception {
        this.helper = XPathHelper.newInstance(XPathHelperTest.class.getResource(TEST_FILE));
    }

    @Test
    void testGetElement() throws Exception {
        String lValue = this.helper.getElement(XPathHelper.XPATH_TITLE);
        assertEquals("Relations: Test 1", lValue);

        lValue = this.helper.getElement("/node/to/nowhere");
        assertEquals(null, lValue);
    }

    @Test
    void testGetAttribute() throws Exception {
        final String lExpected = "ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Abook&amp;rft.btitle=Hallo+Test&amp;rft.aufirst=Jane&amp;rft.aulast=Doe&amp;rft.place=New+York&amp;rft.genre=document";

        String lValue = this.helper.getAttribute(XPATH_COINS, "title");
        assertEquals(lExpected, lValue);

        lValue = this.helper.getAttribute("/node/to/nowhere", "title");
        assertEquals(null, lValue);

        lValue = this.helper.getAttribute(XPATH_COINS, "noAttribute");
        assertEquals(null, lValue);
    }

    @Test
    void testGetMetadata() throws Exception {
        this.helper = XPathHelper.newInstance(XPathHelperTest.class.getResource(TEST_FILE2));

        String lValue = this.helper.getAttribute("//head/meta[@name=\"description\"]", "content");
        assertEquals("This is a only a test", lValue);

        lValue = this.helper.getAttribute("//head/meta[@name=\"author\"]", "content");
        assertEquals("Jane Doe", lValue);

        lValue = this.helper.getAttribute("//head/meta[@name=\"keywords\"]", "content");
        assertEquals("relations, test", lValue);

        lValue = this.helper.getAttribute("//head/meta[@name=\"date\"]", "content");
        assertEquals("2009-12-15T08:49", lValue);
    }

    @Test
    void testSerialize() throws Exception {
        final String lExpected = "<span class=\"Z3988\" title=\"ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Abook&amp;rft.btitle=Hallo+Test&amp;rft.aufirst=Jane&amp;rft.aulast=Doe&amp;rft.place=New+York&amp;rft.genre=document\"></span>";
        final String lSerialize1 = this.helper.getSerialized(XmlSerializer.COMPACT);
        final String lSerialize2 = this.helper.getSerialized(XmlSerializer.PRETTY);

        assertTrue(lSerialize1.contains(lExpected));
        assertTrue(lSerialize2.contains(lExpected));
        assertTrue(lSerialize1.length() < lSerialize2.length());
    }

    @Test
    void testRemoveUnqualifiedLinks() throws Exception {
        this.helper = XPathHelper.newInstance(XPathHelperTest.class.getResource(TEST_FILE2));
        final String lValue = this.helper.getAttribute("//link", "rel");
        assertEquals("shortcut icon", lValue);

        this.helper.removeUnqualifiedLinks();
        assertNull(this.helper.getAttribute("//link", "rel"));

    }

}
