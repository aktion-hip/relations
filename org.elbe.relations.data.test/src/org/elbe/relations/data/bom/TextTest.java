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

package org.elbe.relations.data.bom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.data.utility.IItemVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author lbenno
 */
@ExtendWith(MockitoExtension.class)
public class TextTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String TITLE = "test title";
    private static final String TEXT = "test text";
    private static final Timestamp TIMESTAMP = new Timestamp(1387719218782l);

    @Mock
    private IItemVisitor visitor;

    private IndexerHelper indexer;

    private Text text;

    @BeforeEach
    public void setUp() throws Exception {
        this.text = new Text();
        this.text.set(TextHome.KEY_ID, 123l);
        this.text.set(TextHome.KEY_TITLE, TITLE);
        this.text.set(TextHome.KEY_TEXT, TEXT);
        this.text.set(TextHome.KEY_CREATED, TIMESTAMP);
        this.text.set(TextHome.KEY_MODIFIED, TIMESTAMP);

        this.indexer = new IndexerHelper();
    }

    @Test
    public void testVisit() throws Exception {
        this.text.visit(this.visitor);
        verify(this.visitor).setTitle(TITLE);
        verify(this.visitor).setTitleEditable(true);
        verify(this.visitor).setText(NL + "[test text...]");
        verify(this.visitor).setRealText(TEXT);
        verify(this.visitor).setTextEditable(false);
    }

    @Test
    public void testIndexContent() throws Exception {
        this.text.indexContent(this.indexer);
        final Collection<IndexerDocument> docs = this.indexer.getDocuments();
        assertEquals(1, docs.size());

        final IndexerDocument doc = docs.iterator().next();
        final Map<String, String> fields = DataHouseKeeper.createFieldMap(doc);

        assertFieldValue(fields, "itemID", "123");
        assertFieldValue(fields, "uniqueID", "2:123");
        assertFieldValue(fields, "itemType", "2");
        assertFieldValue(fields, "itemTitle", "test title");
        assertFieldValue(fields, "itemFull", "test title          test text ");
    }

    private void assertFieldValue(final Map<String, String> inFields,
            final String inID, final String inExpected) {
        assertEquals(inExpected, inFields.get(inID));
    }

    @Test
    public void testGetBibtexFormatted() throws Exception {
        final Collection<String> labels = new ArrayList<String>(3);

        this.text.set(TextHome.KEY_AUTHOR, "Doe, Jane");
        this.text.set(TextHome.KEY_YEAR, "2010");

        // book
        this.text.set(TextHome.KEY_TYPE, AbstractText.TYPE_BOOK);
        String bibtex = this.text.getBibtexFormatted(labels);
        assertEquals("@BOOK{Doe:10," + NL + "     AUTHOR = {Doe, Jane}," + NL
                + "     TITLE = {test title}," + NL + "     YEAR = 2010" + NL
                + "}", bibtex);

        // article
        this.text.set(TextHome.KEY_TYPE, AbstractText.TYPE_ARTICLE);
        bibtex = this.text.getBibtexFormatted(labels);
        assertEquals("@ARTICLE{Doe:10a," + NL + "     AUTHOR = {Doe, Jane},"
                + NL + "     TITLE = {test title}," + NL + "     YEAR = 2010"
                + NL + "}", bibtex);

        // contribution
        this.text.set(TextHome.KEY_TYPE, AbstractText.TYPE_CONTRIBUTION);
        bibtex = this.text.getBibtexFormatted(labels);
        assertEquals("@INCOLLECTION{Doe:10b," + NL
                + "     AUTHOR = {Doe, Jane}," + NL
                + "     TITLE = {test title}," + NL + "     YEAR = 2010" + NL
                + "}", bibtex);

        // web
        this.text.set(TextHome.KEY_TYPE, AbstractText.TYPE_WEBPAGE);
        bibtex = this.text.getBibtexFormatted(labels);
        assertEquals("@ARTICLE{Doe:10c," + NL + "     AUTHOR = {Doe, Jane},"
                + NL + "     TITLE = {test title}," + NL + "     YEAR = 2010"
                + NL + "}", bibtex);

    }

}
