/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elbe.relations.data.bom.EventStoreHome.StoreType;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.impl.KeyObjectImpl;
import org.hip.kernel.exc.VException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author lbenno
 */
public class EventStoreHomeTest {
    // private static final String PATTERN = "TIMESTAMP\\('([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2})'\\)";
    private static final String PATTERN = "type=\"Timestamp\">([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{1,3})";
    private static DataHouseKeeper data;

    @BeforeClass
    public static void init() {
        data = DataHouseKeeper.INSTANCE;
    }

    @After
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    /** Test method for
     * {@link org.elbe.relations.data.bom.EventStoreHome#saveEntry(org.elbe.relations.data.utility.UniqueID)}.
     *
     * @throws Exception */
    @Test
    public void testSaveEntryDelete() throws Exception {
        final EventStoreHome home = data.getEventStoreHome();
        assertEquals(0, home.getCount());

        final Long id = home.saveEntry(new UniqueID(IItem.TERM, 22));
        assertEquals(1, home.getCount());

        final EventStore eventObj = (EventStore) home.findByKey(createKey(id));
        assertEquals("1:22", eventObj.get(EventStoreHome.KEY_UNIQUE_ID));
        assertEquals("3", eventObj.get(EventStoreHome.KEY_TYPE).toString());
        assertEquals("Delete(1:22)", eventObj.get(EventStoreHome.KEY_EVENT));
    }

    private KeyObject createKey(final long id) throws VException {
        final KeyObject key = new KeyObjectImpl();
        key.setValue(EventStoreHome.KEY_ID, new Long(id));
        return key;
    }

    /** Test method for
     * {@link org.elbe.relations.data.bom.EventStoreHome#saveEntry(org.elbe.relations.data.utility.UniqueID, org.hip.kernel.bom.DomainObject, org.elbe.relations.data.bom.EventStoreHome.StoreType)}.
     *
     * @throws Exception */
    @Test
    public void testSaveEntryTermCreate() throws Exception {
        final DomainObject term = data.getTermHome().create();
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        term.set(TermHome.KEY_TITLE, "Title of Term");
        term.set(TermHome.KEY_TEXT, "This is a <b>Test</b>!");
        term.set(TermHome.KEY_CREATED, created);
        term.set(TermHome.KEY_MODIFIED, created);
        term.set(TermHome.KEY_ID, new Long(42));

        final EventStoreHome home = data.getEventStoreHome();
        assertEquals(0, home.getCount());

        final Long id = home.saveEntry(new UniqueID(IItem.TERM, 42), term, StoreType.CREATE);
        assertEquals(1, home.getCount());

        final EventStore eventObj = (EventStore) home.findByKey(createKey(id));
        assertEquals("1:42", eventObj.get(EventStoreHome.KEY_UNIQUE_ID));
        assertEquals("1", eventObj.get(EventStoreHome.KEY_TYPE).toString());
        final String expected = "<TermEntry>\r\n" +
                "        <Modified field=\"DTMUTATION\" type=\"Timestamp\">---\r\n" +
                "        </Modified>\r\n" +
                "        <Title field=\"STITLE\" type=\"String\">Title of Term\r\n" +
                "        </Title>\r\n" +
                "        <Text field=\"STEXT\" type=\"String\">This is a <b>Test</b>!\r\n" +
                "        </Text>\r\n" +
                "        <ID field=\"TERMID\" type=\"Long\">42\r\n" +
                "        </ID>\r\n" +
                "        <Created field=\"DTCREATION\" type=\"Timestamp\">---\r\n" +
                "        </Created>\r\n" +
                "</TermEntry>";
        assertEquals(expected, replaceTimestamp(eventObj.get(EventStoreHome.KEY_EVENT).toString(), "---"));
    }

    @Test
    public void testSaveEntryTermUpdate() throws Exception {
        final DomainObject term = data.getTermHome().create();
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        term.set(TermHome.KEY_TITLE, "Title of Term");
        term.set(TermHome.KEY_TEXT, "This is a <b>Test</b>!");
        term.set(TermHome.KEY_CREATED, created);
        term.set(TermHome.KEY_MODIFIED, created);
        term.set(TermHome.KEY_ID, new Long(43));

        final EventStoreHome home = data.getEventStoreHome();
        assertEquals(0, home.getCount());

        final Long id = home.saveEntry(new UniqueID(IItem.TERM, 43), term, StoreType.UPDATE);
        assertEquals(1, home.getCount());

        final EventStore eventObj = (EventStore) home.findByKey(createKey(id));
        assertEquals("1:43", eventObj.get(EventStoreHome.KEY_UNIQUE_ID));
        assertEquals("2", eventObj.get(EventStoreHome.KEY_TYPE).toString());
        final String expected = "<TermEntry>\r\n" +
                "        <Modified field=\"DTMUTATION\" type=\"Timestamp\">---\r\n" +
                "        </Modified>\r\n" +
                "        <Title field=\"STITLE\" type=\"String\">Title of Term\r\n" +
                "        </Title>\r\n" +
                "        <Text field=\"STEXT\" type=\"String\">This is a <b>Test</b>!\r\n" +
                "        </Text>\r\n" +
                "        <ID field=\"TERMID\" type=\"Long\">43\r\n" +
                "        </ID>\r\n" +
                "        <Created field=\"DTCREATION\" type=\"Timestamp\">---\r\n" +
                "        </Created>\r\n" +
                "</TermEntry>";
        assertEquals(expected, replaceTimestamp(eventObj.get(EventStoreHome.KEY_EVENT).toString(), "---"));
    }

    @Test
    public void testSaveEntryPerson() throws Exception {
        final DomainObject person = data.getPersonHome().create();
        final Timestamp created = new Timestamp(System.currentTimeMillis());
        person.set(PersonHome.KEY_NAME, "Doe");
        person.set(PersonHome.KEY_FIRSTNAME, "Jane");
        person.set(PersonHome.KEY_TEXT, "Person for <i>testing</i> purposes!");
        person.set(PersonHome.KEY_CREATED, created);
        person.set(PersonHome.KEY_MODIFIED, created);
        person.set(PersonHome.KEY_ID, new Long(1001));

        final EventStoreHome home = data.getEventStoreHome();
        assertEquals(0, home.getCount());

        final Long id = home.saveEntry(new UniqueID(IItem.PERSON, 1001), person, StoreType.CREATE);
        assertEquals(1, home.getCount());

        final EventStore eventObj = (EventStore) home.findByKey(createKey(id));
        assertEquals("3:1001", eventObj.get(EventStoreHome.KEY_UNIQUE_ID));
        assertEquals("1", eventObj.get(EventStoreHome.KEY_TYPE).toString());
        final String expected = "<PersonEntry>\r\n" +
                "        <Firstname field=\"SFIRSTNAME\" type=\"String\">Jane\r\n" +
                "        </Firstname>\r\n" +
                "        <Modified field=\"DTMUTATION\" type=\"Timestamp\">---\r\n" +
                "        </Modified>\r\n" +
                "        <Text field=\"STEXT\" type=\"String\">Person for <i>testing</i> purposes!\r\n" +
                "        </Text>\r\n" +
                "        <ID field=\"PERSONID\" type=\"Long\">1001\r\n" +
                "        </ID>\r\n" +
                "        <From field=\"SFROM\" type=\"String\">\r\n" +
                "        </From>\r\n" +
                "        <To field=\"STO\" type=\"String\">\r\n" +
                "        </To>\r\n" +
                "        <Name field=\"SNAME\" type=\"String\">Doe\r\n" +
                "        </Name>\r\n" +
                "        <Created field=\"DTCREATION\" type=\"Timestamp\">---\r\n" +
                "        </Created>\r\n" +
                "</PersonEntry>";
        assertEquals(expected, replaceTimestamp(eventObj.get(EventStoreHome.KEY_EVENT).toString(), "---"));
    }

    private static String replaceTimestamp(final String toReplace, final String replacement) {
        return replace(toReplace, replacement, PATTERN);
    }

    private static String replace(String toReplace, final String replacement, final String patternStr) {
        final Pattern pattern = Pattern.compile(patternStr);
        final Matcher matcher = pattern.matcher(toReplace);
        if (matcher.find()) {
            toReplace = toReplace.replace(matcher.group(1), replacement);
        }
        return toReplace;
    }

}
