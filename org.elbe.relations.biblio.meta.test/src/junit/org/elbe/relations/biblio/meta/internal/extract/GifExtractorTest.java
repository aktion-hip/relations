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
 * @author Luthiger Created on 21.01.2010
 */
public class GifExtractorTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String FILE_NAME = "resources/gifExtractorTest.gif";
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
    public void tearDown() {
        Locale.setDefault(this.localeOld);
    }

    @Test
    public void testAcceptsFile() throws Exception {
        final GifExtractor lExtractor = new GifExtractor();
        assertTrue(lExtractor.acceptsFile(this.file));

        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME2)));
    }

    @Test
    public void testProcess() throws Exception {
        final GifExtractor lExtractor = new GifExtractor();
        final ExtractedData lExtracted = lExtractor.process(this.file);
        lExtracted.setFilePath("");
        final String lExpected = "Test GIF fuer Relations" + NL
                + "[<i>Size: 3.30 kB;" + NL + "Type: image/gif;" + NL
                + "Last Modified: XXX</i>]";
        assertEquals(lExpected, lExtracted.getText().replaceAll(TestUtil.REGEX, TestUtil.REPLACEMENT));
    }

}
