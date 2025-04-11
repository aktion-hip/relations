package org.elbe.relations.internal.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.Locale;

import org.elbe.relations.parsing.ExtractedData;
import org.elbe.relations.parsing.XPathHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    public void setUp() throws Exception {
        this.url = DCHtmlExtractorTest.class.getResource(FILE_NAME2);

        this.localeOld = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(this.localeOld);
    }

    @Test
    public void testCheckDCMeta() throws Exception {
        assertTrue(DCHtmlExtractor.checkDCMeta(XPathHelper.newInstance(this.url)));

        assertFalse(DCHtmlExtractor.checkDCMeta(XPathHelper
                .newInstance(DCHtmlExtractorTest.class
                        .getResource(FILE_NAME1))));
    }

    @Test
    public void testExtract() throws Exception {
        final IHtmlExtractor lExtractor = new DCHtmlExtractor();
        final ExtractedData lExtracted = lExtractor
                .extractData(XPathHelper.newInstance(this.url), "something",
                        this.url.toExternalForm());

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
