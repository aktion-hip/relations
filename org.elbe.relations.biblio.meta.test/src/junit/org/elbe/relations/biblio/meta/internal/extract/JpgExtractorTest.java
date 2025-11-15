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
 * @author Luthiger Created on 14.01.2010
 */
public class JpgExtractorTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String FILE_NAME = "resources/jpgExtractorTest.jpg";
    private static final String FILE_NAME2 = "resources/dummy.txt";

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
        final JpgExtractor lExtractor = new JpgExtractor();
        assertTrue(lExtractor.acceptsFile(this.file));

        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME2)));
    }

    @Test
    public final void testProcess() throws IOException {
        final JpgExtractor lExtractor = new JpgExtractor();
        final ExtractedData lExtracted = lExtractor.process(this.file);
        lExtracted.setFilePath("");
        final String lExpected = "Test Image for Metadata Extractor" + NL
                + "[<i>Size: 4.18 kB;" + NL + "Type: image/jpeg;" + NL
                + "Last Modified: XXX</i>]";
        assertEquals(lExpected, lExtracted.getText().replaceAll(TestUtil.REGEX, TestUtil.REPLACEMENT));
    }

}
