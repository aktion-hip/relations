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

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.EventStoreHome;
import org.elbe.relations.data.bom.LightWeightPerson;
import org.elbe.relations.data.bom.Person;
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.hip.kernel.exc.VException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Luthiger
 */
public class PersonTest {
    private static DataHouseKeeper data;

    private final String name = "Name";
    private final String firstName = "Firstname";
    private final String text = "Text";
    private final String from = "1.1.2000";
    private final String to = "31.12.2010";

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
        final PersonHome lHome = data.getPersonHome();

        assertEquals(0, lHome.getCount());

        final AbstractPerson lPerson = lHome.newPerson(this.name, this.firstName, this.from, this.to, this.text);
        assertEquals(1, lHome.getCount());

        final AbstractPerson lPerson2 = lHome.getPerson(lPerson.getID());
        final LightWeightPerson lLightWeight = (LightWeightPerson) lPerson2.getLightWeight();

        assertEquals(lPerson.getID(), lLightWeight.getID());
        assertEquals(this.name, lLightWeight.name);
        assertEquals(this.firstName, lLightWeight.firstname);
        assertEquals(this.text, lLightWeight.text);
        assertEquals(this.from, lLightWeight.from);
        assertEquals(this.to, lLightWeight.to);
    }

    @Test
    public void testSave() throws VException, SQLException, BOMException {
        final long start = System.currentTimeMillis() - 1000;

        final String firstname2 = "Changed First Name";
        final String to2 = "8.8.2012";

        final PersonHome home = data.getPersonHome();
        final EventStoreHome storeHome = data.getEventStoreHome();

        assertEquals(0, home.getCount());
        assertEquals(0, storeHome.getCount());

        final AbstractPerson lPerson = home.newPerson(this.name, this.firstName, this.from, this.to, this.text);
        assertEquals(1, home.getCount());
        assertEquals(1, storeHome.getCount());

        final AbstractPerson person2 = home.getPerson(lPerson.getID());
        person2.save(this.name, firstname2, this.text, this.from, to2);
        assertEquals(2, storeHome.getCount());

        final AbstractPerson person3 = home.getPerson(lPerson.getID());

        assertEquals(this.name, person3.get(PersonHome.KEY_NAME));
        assertEquals(firstname2, person3.get(PersonHome.KEY_FIRSTNAME));
        assertEquals(this.text, person3.get(PersonHome.KEY_TEXT));
        assertEquals(to2, person3.get(PersonHome.KEY_TO));

        assertTrue(start < ((Timestamp) person3.get(PersonHome.KEY_MODIFIED)).getTime());
        final String created = person3.getCreated();
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
    public void testGetTitle() throws VException, SQLException, BOMException {
        final PersonHome home = data.getPersonHome();

        final AbstractPerson lPerson = home.newPerson(this.name, this.firstName, this.from, this.to, this.text);
        final AbstractPerson lPerson2 = home.getPerson(lPerson.getID());

        assertEquals(this.name + ", " + this.firstName, lPerson2.getTitle());
    }

    @Test
    public void testIndexContent() throws Exception {
        final IndexerHelper indexer = new IndexerHelper();

        final PersonHome home = data.getPersonHome();
        final AbstractPerson person = home.newPerson(this.name, this.firstName, this.from, this.to, this.text);
        ((Person) person).indexContent(indexer);

        assertEquals(1, indexer.getDocuments().size());
        final IndexerDocument lDocument = indexer.getDocuments().iterator().next();
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
        assertTrue(lFieldNames.contains("itemDateCreated"));
        assertTrue(lFieldNames.contains("itemDateModified"));
        assertTrue(lFieldNames.contains("itemFull"));
        assertTrue(lFieldFull.contains("itemType: 3"));
        assertTrue(lFieldFull.contains("itemTitle: Firstname Name"));
    }

}
