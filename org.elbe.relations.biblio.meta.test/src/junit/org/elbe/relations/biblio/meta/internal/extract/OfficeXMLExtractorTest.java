package org.elbe.relations.biblio.meta.internal.extract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import org.elbe.relations.parsing.ExtractedData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Luthiger Created on 25.01.2010
 */
public class OfficeXMLExtractorTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String FILE_NAME = "resources/wordxExtractTest.docx";
    private static final String FILE_NAME2 = "resources/openOfficeTest.odt";
    private static final String FILE_NAME3 = "resources/dummy.txt";

    private File file;
    private Locale localeOld;

    @BeforeEach
    public void setUp() throws Exception {
        this.localeOld = Locale.getDefault();
        Locale.setDefault(Locale.US);

        this.file = new File(FILE_NAME);
        if (!this.file.exists()) {
            throw new FileNotFoundException(FILE_NAME);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(this.localeOld);
    }

    @Test
    public final void testAcceptsFile() {
        final OfficeXMLExtractor lExtractor = new OfficeXMLExtractor();
        assertTrue(lExtractor.acceptsFile(this.file));

        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME2)));
        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME3)));
    }

    @Test
    public void testProcess() throws Exception {
        final OfficeXMLExtractor lExtractor = new OfficeXMLExtractor();
        final ExtractedData lExtracted = lExtractor.process(this.file);
        assertEquals("Test Extract Word 2007",
                lExtracted.getTitle());

        lExtracted.setFilePath("");
        final String lExpected = "Use this document to test extraction of metadata."
                + NL
                + "How to extract"
                + NL
                + "test word extract"
                + NL
                + "[<i>Author: Luthiger;"
                + NL
                + "Size: 10.16 kB;"
                + NL
                + "Type: application/ms-office-1.x;"
                + NL
                + "Created: January 24, 2010, 9:56:00%sPM CET;"
                + NL
                + "Last Modified: XXX</i>]";
        assertEquals(String.format(lExpected, TestUtil.NBSP),
                lExtracted.getText().replaceAll(TestUtil.REGEX, TestUtil.REPLACEMENT));

    }

}
