package org.elbe.relations.biblio.meta.internal.unapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Luthiger Created on 29.12.2009
 */
public class XMLFormatsParserTest {
    private static final String FILENAME = "resources/unapi_formats.xml";
    private File xml;

    @BeforeEach
    public void setUp() throws Exception {
        this.xml = new File(FILENAME);
    }

    @Test
    public final void testParse() throws Exception {
        final String[] lExpected = new String[] { "endnote", "bibtex",
                "oai_dc", "mods" };
        final Collection<String> lFormats = XMLFormatsParser.getInstance()
                .parse(this.xml.toURI().toURL());
        assertEquals(lExpected.length, lFormats.size());
        for (final String lFormat : lExpected) {
            assertTrue(lFormats.contains(lFormat));
        }
    }

}
