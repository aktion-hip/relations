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
 * @author Luthiger Created on 26.01.2010
 */
public class PowerPointExtractorTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String FILE_NAME = "resources/powerpointExtractorTest.ppt";
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
    public void testAcceptsFile() throws Exception {
        final PowerPointExtractor lExtractor = new PowerPointExtractor();
        assertTrue(lExtractor.acceptsFile(this.file));

        assertFalse(lExtractor.acceptsFile(new File(FILE_NAME2)));
    }

    @Test
    public void testProcess() throws Exception {
        final PowerPointExtractor lExtractor = new PowerPointExtractor();
        final ExtractedData lExtacted = lExtractor.process(this.file);

        assertEquals("Metadata Extract Test Slide Show", lExtacted.getTitle());

        lExtacted.setFilePath("");
        final String lExpected = "This slide show is for testing only." + NL
                + "[<i>Author: Luthiger;" + NL + "Size: 96.50 kB;" + NL
                + "Type: application/vnd.ms-powerpoint;" + NL
                + "Created: September 6, 2007, 12:25:07%sAM CEST;" + NL
                + "Last Modified: XXX</i>]";
        assertEquals(String.format(lExpected, TestUtil.NBSP),
                lExtacted.getText().replaceAll(TestUtil.REGEX, TestUtil.REPLACEMENT));
    }

}
