/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.data.internal.bom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import org.elbe.relations.data.bom.AbstractItem;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.EventStoreHome;
import org.elbe.relations.data.bom.LightWeightText;
import org.elbe.relations.data.bom.Text;
import org.elbe.relations.data.bom.TextHome;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Luthiger
 */
public class TextTest {
    private static DataHouseKeeper data;

    private final static String NL = System.getProperty("line.separator");

    private final String title = "Book Title";
    private final String textText = "additional text";
    private final String author = "Riese, Adam";
    private final String coAuthor = "co";
    private final String subTitle = "sub";
    private final String year = "1887";
    private final String publication = "pub";
    private final String pages = "1-2";
    private final Integer volume = Integer.valueOf(88);
    private final Integer number = Integer.valueOf(107);
    private final String publisher = "Addison Wesley";
    private final String place = "London";
    private final Integer type = Integer.valueOf(2);

    @BeforeAll
    public static void init() {
        data = DataHouseKeeper.INSTANCE;
    }

    @BeforeEach
    public void setUp() throws Exception {
        // data.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testGetID() throws Exception {
        final TextHome home = data.getTextHome();

        assertEquals(0, home.getCount());

        final AbstractItem text = home.newText(this.title, this.textText, this.author,
                this.coAuthor, this.subTitle, this.year, this.publication, this.pages, this.volume, this.number,
                this.publisher, this.place, this.type);
        assertEquals(1, home.getCount());

        final AbstractItem text2 = home.getText(text.getID());
        final LightWeightText lLightWeight = (LightWeightText) text2
                .getLightWeight();

        assertEquals(text.getID(), lLightWeight.getID());
        assertEquals(this.title, lLightWeight.title);
        assertEquals(this.textText, lLightWeight.text);
        assertEquals(this.author, lLightWeight.author);
        assertEquals(this.year, lLightWeight.year);
        assertEquals(this.author, lLightWeight.author);
        assertEquals(this.volume.intValue(), lLightWeight.volume);
        assertEquals(this.number.intValue(), lLightWeight.number);
        assertEquals(this.publisher, lLightWeight.publisher);
        assertEquals(this.place, lLightWeight.place);
        assertEquals(this.type.intValue(), lLightWeight.type);
    }

    @Test
    public void testSave() throws Exception {
        final long start = System.currentTimeMillis() - 1000;

        final String author = "Newton, Isaac";
        final String coAuthor = "Galilei, G.";
        final String publisher = "O'Reilly";
        final String publication = "New Review";
        final int number = 1;

        final TextHome home = data.getTextHome();
        final EventStoreHome storeHome = data.getEventStoreHome();

        final AbstractItem text = home.newText(this.title, this.textText, this.author,
                this.coAuthor, this.subTitle, this.year, this.publication, this.pages, this.volume, this.number,
                this.publisher, this.place, this.type);
        assertEquals(1, storeHome.getCount());

        final AbstractText text2 = home.getText(text.getID());
        text2.save(this.title, this.textText, this.type, author, coAuthor, this.subTitle,
                publisher, this.year, publication, this.pages, this.volume, Integer.valueOf(number), this.place);
        assertEquals(2, storeHome.getCount());

        final AbstractItem text3 = home.getText(text.getID());
        assertEquals(this.title, text3.getTitle());
        assertEquals(author, text3.get(TextHome.KEY_AUTHOR));
        assertEquals(coAuthor, text3.get(TextHome.KEY_COAUTHORS));
        assertEquals(publisher,
                text3.get(TextHome.KEY_PUBLISHER));
        assertEquals(publication,
                text3.get(TextHome.KEY_PUBLICATION));
        assertEquals(this.volume.intValue(),
                ((BigDecimal) text3.get(TextHome.KEY_VOLUME)).intValue());
        assertEquals(number,
                ((BigDecimal) text3.get(TextHome.KEY_NUMBER)).intValue());

        assertTrue(
                start < ((Timestamp) text3.get(TextHome.KEY_MODIFIED))
                .getTime());
        final String lCreated = text3.getCreated();
        assertNotNull(lCreated);
        // the outcome of the following assertions depends on the -nl setting
        // e.g.
        // "Erzeugt:  May 6, 2007, 10:26:59 PM; Verändert: May 6, 2007, 10:26:59 PM."
        // assertTrue("Created:", lCreated.indexOf("Created:") >= 0);
        // assertTrue("Modified:", lCreated.indexOf("Modified:") >= 0);
    }

    @Test
    public void testGetBibtexFormatted() throws Exception {
        final String lExpected1 = "@BOOK{Riese:87," + NL
                + "     AUTHOR = {Riese, Adam and co}," + NL
                + "     TITLE = {Book Title: sub}," + NL
                + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        final String lExpected2 = "@ARTICLE{Riese:87," + NL
                + "     AUTHOR = {Riese, Adam and co}," + NL
                + "     TITLE = {Book Title}," + NL + "     JOURNAL = {pub},"
                + NL + "     YEAR = 1887," + NL + "     VOLUME = {88}," + NL
                + "     NUMBER = {107}," + NL + "     PAGES = {1-2}," + NL
                + "     ADDRESS = {London}" + NL + "}";
        final String lExpected3 = "@INCOLLECTION{Riese:87," + NL
                + "     AUTHOR = {Riese, Adam}," + NL
                + "     TITLE = {Book Title}," + NL
                + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     EDITOR = {co}," + NL
                + "     BOOKTITLE = {pub}," + NL + "     VOLUME = {88}," + NL
                + "     ADDRESS = {London}" + NL + "}";
        final String lExpected4 = "@ARTICLE{Riese:87," + NL
                + "     AUTHOR = {Riese, Adam and co}," + NL
                + "     TITLE = {Book Title: sub}," + NL
                + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL
                + "     JOURNAL = {\\path{<pub>} (accessed London)}," + NL
                + "}";

        // test all types first
        final TextHome lHome = data.getTextHome();
        Text lText = (Text) lHome.newText(this.title, this.textText, this.author, this.coAuthor,
                this.subTitle, this.year, this.publication, this.pages, this.volume, this.number, this.publisher,
                this.place, Integer.valueOf(AbstractText.TYPE_BOOK));
        assertEquals(lExpected1, lText.getBibtexFormatted(new ArrayList<String>()));

        lText = (Text) lHome.newText(this.title, this.textText, this.author, this.coAuthor,
                this.subTitle, this.year, this.publication, this.pages, this.volume, this.number, this.publisher,
                this.place, Integer.valueOf(AbstractText.TYPE_ARTICLE));
        assertEquals(lExpected2, lText.getBibtexFormatted(new ArrayList<String>()));

        lText = (Text) lHome.newText(this.title, this.textText, this.author, this.coAuthor,
                this.subTitle, this.year, this.publication, this.pages, this.volume, this.number, this.publisher,
                this.place, Integer.valueOf(AbstractText.TYPE_CONTRIBUTION));
        assertEquals(lExpected3, lText.getBibtexFormatted(new ArrayList<String>()));

        lText = (Text) lHome.newText(this.title, this.textText, this.author, this.coAuthor,
                this.subTitle, this.year, this.publication, this.pages, this.volume, this.number, this.publisher,
                this.place, Integer.valueOf(AbstractText.TYPE_WEBPAGE));
        assertEquals(lExpected4, lText.getBibtexFormatted(new ArrayList<String>()));

        // article with co-authors
        String lExpected = "@ARTICLE{Riese:87,"
                + NL
                + "     AUTHOR = {Riese, Adam and S. Ishikawa and M. Silverstein and M. Jacobson and I. Fisksdahl-King and S. Angel},"
                + NL + "     TITLE = {Book Title}," + NL
                + "     JOURNAL = {Research policy}," + NL
                + "     YEAR = 1887," + NL + "     VOLUME = {88}," + NL
                + "     NUMBER = {107}," + NL + "     PAGES = {88-93}," + NL
                + "     ADDRESS = {London}" + NL + "}";
        lText = (Text) lHome
                .newText(
                        this.title,
                        this.textText,
                        this.author,
                        "S. Ishikawa, M. Silverstein, M. Jacobson, I. Fisksdahl-King, S. Angel",
                        "Sub-Title", this.year, "Research policy", "88-93", this.volume,
                        this.number, this.publisher, this.place, Integer.valueOf(
                                AbstractText.TYPE_ARTICLE));
        assertEquals(lExpected, lText.getBibtexFormatted(new ArrayList<String>()));

        // contribution with editors
        lExpected = "@INCOLLECTION{Ishikawa:87,"
                + NL
                + "     AUTHOR = {Ishikawa, S. and M. Silverstein},"
                + NL
                + "     TITLE = {Book Title},"
                + NL
                + "     PUBLISHER = {Addison Wesley},"
                + NL
                + "     YEAR = 1887,"
                + NL
                + "     EDITOR = {Jacobson, M. and I. Fisksdahl-King and S. Angel},"
                + NL + "     BOOKTITLE = {Research policy}," + NL
                + "     VOLUME = {88}," + NL + "     ADDRESS = {London}" + NL
                + "}";
        lText = (Text) lHome.newText(this.title, this.textText,
                "Ishikawa, S., M. Silverstein",
                "Jacobson, M., I. Fisksdahl-King, S. Angel", "Sub-Title", this.year,
                "Research policy", "88-93", this.volume, this.number, this.publisher, this.place,
                Integer.valueOf(AbstractText.TYPE_CONTRIBUTION));
        assertEquals(lExpected,
                lText.getBibtexFormatted(new ArrayList<String>()));

        // web page with URL
        lExpected = "@ARTICLE{Ishikawa:87,"
                + NL
                + "     AUTHOR = {Ishikawa, S. and M. Silverstein and Jacobson and M. and I. Fisksdahl-King and S. Angel},"
                + NL
                + "     TITLE = {Book Title: Sub-Title},"
                + NL
                + "     PUBLISHER = {Addison Wesley},"
                + NL
                + "     YEAR = 1887,"
                + NL
                + "     JOURNAL = {\\path{<http://www.oreillynet.com/pub/wlg/7996>} (accessed 20.10.1999)},"
                + NL + "}";
        lText = (Text) lHome.newText(this.title, this.textText,
                "Ishikawa, S., M. Silverstein",
                "Jacobson, M., I. Fisksdahl-King, S. Angel", "Sub-Title", this.year,
                "http://www.oreillynet.com/pub/wlg/7996", "88-93", this.volume,
                this.number, this.publisher, "20.10.1999", Integer.valueOf(
                        AbstractText.TYPE_WEBPAGE));
        assertEquals(lExpected,
                lText.getBibtexFormatted(new ArrayList<String>()));

        // book with subtitle
        lExpected = "@BOOK{Riese:87," + NL
                + "     AUTHOR = {Riese, Adam and co}," + NL
                + "     TITLE = {Book Title: Sub-Title}," + NL
                + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        lText = (Text) lHome.newText(this.title, this.textText, this.author, this.coAuthor,
                "Sub-Title", this.year, this.publication, this.pages, this.volume, this.number,
                this.publisher, this.place, Integer.valueOf(AbstractText.TYPE_BOOK));
        assertEquals(lExpected,
                lText.getBibtexFormatted(new ArrayList<String>()));

        // uniqueness of label
        final String lExpected1a = "@BOOK{Riese:87a," + NL
                + "     AUTHOR = {Riese, Adam and co}," + NL
                + "     TITLE = {Book Title (1): sub}," + NL
                + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        final String lExpected1b = "@BOOK{Riese:87b," + NL
                + "     AUTHOR = {Riese, Adam and co}," + NL
                + "     TITLE = {Book Title (2): sub}," + NL
                + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        final Collection<String> lUnique = new ArrayList<>();
        lText = (Text) lHome.newText(this.title, this.textText, this.author, this.coAuthor,
                this.subTitle, this.year, this.publication, this.pages, this.volume, this.number, this.publisher,
                this.place, Integer.valueOf(AbstractText.TYPE_BOOK));
        assertEquals(lExpected1,
                lText.getBibtexFormatted(lUnique));
        lText = (Text) lHome.newText(this.title + " (1)", this.textText, this.author,
                this.coAuthor, this.subTitle, this.year, this.publication, this.pages, this.volume, this.number,
                this.publisher, this.place, Integer.valueOf(AbstractText.TYPE_BOOK));
        assertEquals(lExpected1a,
                lText.getBibtexFormatted(lUnique));
        lText = (Text) lHome.newText(this.title + " (2)", this.textText, this.author,
                this.coAuthor, this.subTitle, this.year, this.publication, this.pages, this.volume, this.number,
                this.publisher, this.place, Integer.valueOf(AbstractText.TYPE_BOOK));
        assertEquals(lExpected1b,
                lText.getBibtexFormatted(lUnique));

        // quotations in title
        lExpected = "@BOOK{Riese:87,"
                + NL
                + "     AUTHOR = {Riese, Adam and co},"
                + NL
                + "     TITLE = {Book with quoted title: \"`Whole title quoted.\"'},"
                + NL + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        lText = (Text) lHome.newText("Book with quoted title", this.textText,
                this.author, this.coAuthor, "\"Whole title quoted.\"", this.year, this.publication,
                this.pages, this.volume, this.number, this.publisher, this.place, Integer.valueOf(
                        AbstractText.TYPE_BOOK));
        assertEquals(lExpected,
                lText.getBibtexFormatted(new ArrayList<String>()));

        lExpected = "@BOOK{Riese:87,"
                + NL
                + "     AUTHOR = {Riese, Adam and co},"
                + NL
                + "     TITLE = {Book with quoted title: a \"`feel-good\"' novel},"
                + NL + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        lText = (Text) lHome.newText("Book with quoted title", this.textText,
                this.author, this.coAuthor, "a \"feel-good\" novel", this.year, this.publication,
                this.pages, this.volume, this.number, this.publisher, this.place, Integer.valueOf(
                        AbstractText.TYPE_BOOK));
        assertEquals(lExpected,
                lText.getBibtexFormatted(new ArrayList<String>()));

        // umlaut in author's name
        lExpected = "@BOOK{Mueller:87," + NL
                + "     AUTHOR = {Müller, P. and co}," + NL
                + "     TITLE = {Umlaut in author's name: sub}," + NL
                + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        lText = (Text) lHome.newText("Umlaut in author's name", this.textText,
                "Müller, P.", this.coAuthor, this.subTitle, this.year, this.publication, this.pages,
                this.volume, this.number, this.publisher, this.place, Integer.valueOf(
                        AbstractText.TYPE_BOOK));
        assertEquals(lExpected, lText.getBibtexFormatted(new ArrayList<String>()));

        // text with ampersand somewhere
        lExpected = "@BOOK{Riese:87," + NL
                + "     AUTHOR = {Riese, Adam and co}," + NL
                + "     TITLE = {text with ampersand somewhere: sub}," + NL
                + "     PUBLISHER = {Harper \\& Row}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        lText = (Text) lHome.newText("text with ampersand somewhere", this.textText,
                this.author, this.coAuthor, this.subTitle, this.year, this.publication, this.pages, this.volume,
                this.number, "Harper & Row", this.place, Integer.valueOf(
                        AbstractText.TYPE_BOOK));
        assertEquals(lExpected,
                lText.getBibtexFormatted(new ArrayList<String>()));

        // author in curley brackets
        lExpected = "@BOOK{Open_Source_Initiative:87," + NL
                + "     AUTHOR = {{Open Source Initiative} and co}," + NL
                + "     TITLE = {author in curley brackets: sub}," + NL
                + "     PUBLISHER = {Addison Wesley}," + NL
                + "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
                + "}";
        lText = (Text) lHome.newText("author in curley brackets", this.textText,
                "{Open Source Initiative}", this.coAuthor, this.subTitle, this.year,
                this.publication, this.pages, this.volume, this.number, this.publisher, this.place,
                Integer.valueOf(AbstractText.TYPE_BOOK));
        assertEquals(lExpected,
                lText.getBibtexFormatted(new ArrayList<String>()));
    }

    @Test
    public void testIndexContent() throws Exception {
        final IndexerHelper lIndexer = new IndexerHelper();

        final TextHome lHome = data.getTextHome();
        final AbstractText lText = lHome.newText(this.title, this.textText, this.author,
                this.coAuthor, this.subTitle, this.year, this.publication, this.pages, this.volume, this.number,
                this.publisher, this.place, this.type);
        ((Text) lText).indexContent(lIndexer);

        assertEquals(1, lIndexer.getDocuments().size());
        final IndexerDocument lDocument = lIndexer.getDocuments().iterator()
                .next();
        final Collection<IndexerField> lFields = lDocument.getFields();

        assertEquals(7, lFields.size());
        final Collection<String> lFieldNames = new ArrayList<>();
        final Collection<String> lFieldFull = new ArrayList<>();
        for (final IndexerField lField : lDocument.getFields()) {
            lFieldNames.add(lField.getFieldName());
            lFieldFull.add(lField.toString());
        }
        assertTrue(lFieldNames.contains("itemID"));
        assertTrue(lFieldNames.contains("itemType"));
        assertTrue(lFieldNames.contains("itemTitle"));
        assertTrue(
                lFieldNames.contains("itemDateCreated"));
        assertTrue(
                lFieldNames.contains("itemDateModified"));
        assertTrue(lFieldNames.contains("itemFull"));
        assertTrue(lFieldFull.contains("itemType: 2"));
        assertTrue(lFieldFull.contains("itemTitle: Book Title"));
    }

}
