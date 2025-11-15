package org.elbe.relations.biblio.meta.internal.extract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    void setUp() throws FileNotFoundException {
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
    final void testAcceptsFile() {
        final OOExtractor lExtractor = new OOExtractor();
        assertTrue(lExtractor.acceptsFile(this.file));

        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME2)));
        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME3)));
    }

    @Test
    void testProcess() throws IOException {
        final OOExtractor lExtractor = new OOExtractor();
        final ExtractedData extracted = lExtractor.process(this.file);
        assertEquals("Test of Relations Extractor", extracted.getTitle());

        extracted.setFilePath("");
        final String expected = "This comments the Test of Relations Extractor"
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
                + "Created: January 24, 2010, 12:05:30%sAM CET;"
                + NL
                + "Last Modified: XXX</i>]";
        assertEquals(String.format(expected, TestUtil.NBSP),
                extracted.getText().replaceAll(TestUtil.REGEX, TestUtil.REPLACEMENT));
    }

}
