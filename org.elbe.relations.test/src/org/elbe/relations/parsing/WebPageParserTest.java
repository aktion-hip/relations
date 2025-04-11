package org.elbe.relations.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.parsing.WebPageParser.WebDropResult;
import org.elbe.relations.services.IBibliographyProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * JUnit Plug-in test
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
@ExtendWith(MockitoExtension.class)
public class WebPageParserTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String FILE_NAME = "/resources/html_extract2.html";

    @Mock
    private Logger log;
    @Mock
    private IDataService data;

    private Locale localeOld;
    private IEclipseContext context;
    private WebPageParser parser;

    @BeforeEach
    public void setUp() throws Exception {
        this.localeOld = Locale.getDefault();
        Locale.setDefault(Locale.US);

        this.context = EclipseContextFactory.create("test context");
        this.context.set(Logger.class, this.log);
        this.context.set(IDataService.class, this.data);

        this.parser = ContextInjectionFactory.make(WebPageParser.class, this.context);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(this.localeOld);
        DataHouseKeeper.INSTANCE.deleteAllInAll();
    }

    @Test
    public void testParse() throws Exception {
        final String lUrl = getPath();
        final WebDropResult lResult = this.parser.parse(lUrl);

        final String lExpected = "title=Relations: Metadata&author=Benno Luthiger&coAuthor=John Foo&subTitle=&year=2010&publication=%s&pages=&volume=0&number=0&publisher=Relations&place=%s&type=3&text=%s";
        final String lText = String.format("Metadata" + NL
                + "This page is testing Dublin Core matadata." + NL
                + "[<i>Author: Benno Luthiger;" + NL + "Publisher: Relations;"
                + NL + "Contributor: John Foo;" + NL + "URL: %s;" + NL
                + "Type: Text;" + NL
                + "Created: December 15, 2010, 8:49:37 AM CET</i>]", lUrl);
        assertEquals("new text action", String.format(lExpected, lUrl,
                DateFormat.getDateInstance(DateFormat.LONG).format(new Date()),
                lText), lResult.getNewTextAction().toString());
    }

    @Test
    public void testCompare() throws Exception {
        final List<IBibliographyProvider> lProviders = new ArrayList<IBibliographyProvider>();
        lProviders.add(new TestBibliographyProvider("a", false));
        lProviders.add(new TestBibliographyProvider("b", false));
        lProviders.add(new TestBibliographyProvider("c", true));

        assertFalse(lProviders.get(0).isMicroFormat());
        Collections.sort(lProviders, new ProviderComparator());
        assertTrue(lProviders.get(0).isMicroFormat());
    }

    // --- inner classes for testing ---

    private class TestBibliographyProvider implements IBibliographyProvider {
        private final boolean isMicroFormat;
        private final String id;

        TestBibliographyProvider(final String inID, final boolean inMicroFormat) {
            this.id = inID;
            this.isMicroFormat = inMicroFormat;
        }

        @Override
        public void evaluate(final XPathHelper inXPathHelper,
                final WebDropResult inWebDrop, final IEclipseContext inContext)
                        throws ParserException {
        }

        @Override
        public boolean isMicroFormat() {
            return this.isMicroFormat;
        }

        @Override
        public String toString() {
            return this.id;
        }
    }

    private class ProviderComparator implements
    Comparator<IBibliographyProvider> {
        @Override
        public int compare(final IBibliographyProvider inProvider1,
                final IBibliographyProvider inProvider2) {
            if (inProvider1.isMicroFormat()) {
                return inProvider2.isMicroFormat() ? 0 : -1;
            }
            return inProvider2.isMicroFormat() ? 1 : 0;
        }
    }

    private String getPath() {
        final URL lUrl = WebPageParserTest.class.getResource(FILE_NAME);
        return lUrl.toExternalForm();
    }

}
