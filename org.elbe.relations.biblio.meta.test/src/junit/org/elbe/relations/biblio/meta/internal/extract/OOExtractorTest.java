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
 * @author Luthiger Created on 23.01.2010
 */
public class OOExtractorTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String FILE_NAME = "resources/openOfficeTest.odt";
    private static final String FILE_NAME2 = "resources/wordxExtractTest.docx";
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
        final OOExtractor lExtractor = new OOExtractor();
        assertTrue(lExtractor.acceptsFile(this.file));

        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME2)));
        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME3)));
    }

    @Test
    public void testProcess() throws Exception {
        final OOExtractor lExtractor = new OOExtractor();
        final ExtractedData lExtracted = lExtractor.process(this.file);
        assertEquals("Test of Relations Extractor", lExtracted.getTitle());

        lExtracted.setFilePath("");
        final String lExpected = "This comments the Test of Relations Extractor"
                + NL
                + "This is the Subject: Test"
                + NL
                + "Keywords Test Relations Extractor"
                + NL
                + "[<i>Author: Benno Luthiger;"
                + NL
                + "Size: 7.18 kB;"
                + NL
                + "Type: application/open-office-1.x;"
                + NL
                + "Created: January 24, 2010, 12:05:30 AM CET;"
                + NL
                + "Last Modified: September 29, 2020, 1:36:53 PM CEST</i>]";
        assertEquals(lExpected, lExtracted.getText());
    }

}
