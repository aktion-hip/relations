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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import org.elbe.relations.data.bom.AbstractItem;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.EventStoreHome;
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.data.bom.Term;
import org.elbe.relations.data.bom.TermHome;
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
public class TermTest {
    private static DataHouseKeeper data;

    private final String title = "Title";
    private final String text = "Text";

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
    public void testGetLightWeight() throws Exception {
        final TermHome home = data.getTermHome();

        assertEquals(0, home.getCount());

        final AbstractTerm term = home.newTerm(this.title, this.text);
        assertEquals(1, home.getCount());

        final AbstractTerm term2 = home.getTerm(term.getID());
        final LightWeightTerm lightWeight = (LightWeightTerm) term2.getLightWeight();

        assertEquals(term.getID(), lightWeight.getID());
        assertEquals(this.title, lightWeight.title);
        assertEquals(this.text, lightWeight.text);
    }

    @Test
    public void testSaveTitleText() throws Exception {
        final String title2 = "new title";
        final String text2 = "new text content";

        final TermHome home = data.getTermHome();
        final EventStoreHome storeHome = data.getEventStoreHome();

        assertEquals(0, home.getCount());
        assertEquals(0, storeHome.getCount());

        final AbstractTerm term = home.newTerm(this.title, this.text);
        assertEquals(1, home.getCount());
        assertEquals(1, storeHome.getCount());

        final AbstractTerm term2 = home.getTerm(term.getID());

        assertEquals(this.title, term2.getTitle());
        assertEquals(this.text, term2.get(TermHome.KEY_TEXT));

        term2.saveTitleText(title2, text2);
        assertEquals(2, storeHome.getCount());

        final AbstractTerm term3 = home.getTerm(term.getID());

        assertEquals(title2, term3.getTitle());
        assertEquals(text2, term3.get(TermHome.KEY_TEXT));
    }

    @Test
    public void testSave() throws Exception {
        final long start = System.currentTimeMillis() - 1000;

        final TermHome home = data.getTermHome();
        final EventStoreHome storeHome = data.getEventStoreHome();

        AbstractTerm term = home.newTerm(this.title, this.text);
        assertEquals(1, home.getCount());
        assertEquals(1, storeHome.getCount());

        term.save("new title", "new text");
        assertEquals(2, storeHome.getCount());

        term = home.getTerm(term.getID());
        assertTrue(start < ((Timestamp) term.get(TermHome.KEY_MODIFIED)).getTime());

        final String created = term.getCreated();
        assertNotNull(created);

        String createdLbl = "Created:";
        String modifiedLbl = "Modified:";
        if (data.isGerman()) {
            createdLbl = "Erzeugt:";
            modifiedLbl = "Verändert:";
        }
        assertTrue(created.indexOf(createdLbl) >= 0);
        assertTrue(created.indexOf(modifiedLbl) >= 0);
    }

    @Test
    public void testIndexContent() throws Exception {
        final IndexerHelper indexer = new IndexerHelper();

        final TermHome home = data.getTermHome();
        final AbstractTerm term = home.newTerm(this.title, this.text);
        ((Term) term).indexContent(indexer);

        assertEquals(1, indexer.getDocuments().size());
        final IndexerDocument document = indexer.getDocuments().iterator().next();
        final Collection<IndexerField> fields = document.getFields();

        assertEquals(7, fields.size());
        final Collection<String> fieldNames = new ArrayList<>();
        final Collection<String> fieldFull = new ArrayList<>();
        for (final IndexerField field : document.getFields()) {
            fieldNames.add(field.getFieldName());
            fieldFull.add(field.toString());
        }
        assertTrue(fieldNames.contains("itemID"));
        assertTrue(fieldNames.contains("itemType"));
        assertTrue(fieldNames.contains("itemTitle"));
        assertTrue(fieldNames.contains("itemDateCreated"));
        assertTrue(fieldNames.contains("itemDateModified"));
        assertTrue(fieldNames.contains("itemFull"));
        assertTrue(fieldFull.contains("itemType: 1"));
        assertTrue(fieldFull.contains("itemTitle: Title"));
    }

    /**
     * Test of equals() and hashCode() defined on base class
     * {@link AbstractItem}
     *
     * @throws Exception
     */
    @Test
    public void testEquals() throws Exception {
        final TermHome lHome = data.getTermHome();
        final AbstractTerm lTerm1 = lHome.newTerm(this.title, this.text);
        final AbstractTerm lTerm2 = lHome.newTerm(this.title, this.text);
        final AbstractTerm lTerm3 = lHome.getTerm(lTerm1.getID());

        assertTrue(lTerm1.equals(lTerm1));
        assertTrue(lTerm1.hashCode() == lTerm1.hashCode());
        assertFalse(lTerm1.equals(null));
        assertFalse(lTerm1.equals(lTerm1.toString()));
        assertFalse("model".equals(lTerm1));
        assertFalse(lTerm1.equals(lTerm2));
        assertTrue(lTerm1.equals(lTerm3));
        assertTrue(lTerm3.equals(lTerm1));
        assertEquals(lTerm1.hashCode(), lTerm3.hashCode());
    }

}
