package org.elbe.relations.internal.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
public class GenericHtmlExtractorTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String FILE_NAME = "/resources/html_extract1.html";

    private Locale localeOld;
    private URL url;

    @BeforeEach
    public void setUp() throws Exception {
        this.url = GenericHtmlExtractorTest.class.getResource(FILE_NAME);

        this.localeOld = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(this.localeOld);
    }

    @Test
    public void testExtract() throws Exception {
        final String lTitle = "The html title";
        final IHtmlExtractor lExtractor = new GenericHtmlExtractor();
        final ExtractedData lExtracted = lExtractor.extractData(
                XPathHelper.newInstance(this.url), lTitle, this.url.toExternalForm());

        assertEquals("title", lTitle, lExtracted.getTitle());

        lExtracted.setFilePath("");

        final String lExpected = "This is a only a test" + NL
                + "relations, test" + NL + "[<i>Author: Jane Doe;" + NL
                + "Created: December 15, 2009, 8:49:00 AM CET</i>]";
        assertEquals("text", lExpected, lExtracted.getText());
    }

}
