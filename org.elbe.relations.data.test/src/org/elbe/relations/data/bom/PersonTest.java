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
 *
 * @author lbenno
 */
@ExtendWith(MockitoExtension.class)
public class PersonTest {
    private static final String NAME = "Doe";
    private static final String FIRSTNAME = "Jane";
    private static final String TEXT = "test text";
    private static final Timestamp TIMESTAMP = new Timestamp(1387719218782l);

    @Mock
    private IItemVisitor visitor;

    private IndexerHelper indexer;

    private Person person;

    @BeforeEach
    public void setUp() throws Exception {
        this.person = new Person();
        this.person.set(PersonHome.KEY_ID, 123l);
        this.person.set(PersonHome.KEY_NAME, NAME);
        this.person.set(PersonHome.KEY_FIRSTNAME, FIRSTNAME);
        this.person.set(PersonHome.KEY_TEXT, TEXT);
        this.person.set(PersonHome.KEY_CREATED, TIMESTAMP);
        this.person.set(PersonHome.KEY_MODIFIED, TIMESTAMP);

        this.indexer = new IndexerHelper();
    }

    @Test
    public void testVisit() throws Exception {
        this.person.visit(this.visitor);
        verify(this.visitor).setTitle(NAME + ", " + FIRSTNAME);
        verify(this.visitor).setTitleEditable(false);
        verify(this.visitor).setSubTitle("-");
        verify(this.visitor).setText(TEXT);
        verify(this.visitor).setTextEditable(true);
    }

    @Test
    public void testIndexContent() throws Exception {
        this.person.indexContent(this.indexer);
        final Collection<IndexerDocument> docs = this.indexer.getDocuments();
        assertEquals(1, docs.size());

        final IndexerDocument doc = docs.iterator().next();
        final Map<String, String> lFields = DataHouseKeeper.createFieldMap(doc);

        assertFieldValue(lFields, "uniqueID", "3:123");
        assertFieldValue(lFields, "itemType", "3");
        assertFieldValue(lFields, "itemID", "123");
        assertFieldValue(lFields, "itemTitle", "Jane Doe");
        assertFieldValue(lFields, "itemFull", "Jane Doe test text   ");
    }

    private void assertFieldValue(final Map<String, String> inFields,
            final String inID, final String inExpected) {
        assertEquals(inExpected, inFields.get(inID));
    }

}
