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
package org.elbe.relations.data.db;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.elbe.relations.data.Constants;
import org.elbe.relations.data.TestEmbeddedCreator;
import org.junit.Test;

/**
 * @author lbenno
 */
public class AbstractDBObjectCreatorTest {
    private static final String[] EXPECTED = { "CREATE TABLE tblTerm (\n" +
            "        TermID BIGINT not null generated always as identity,\n" +
            "    sTitle VARCHAR(99) not null,\n" +
            "    sText CLOB,\n" +
            "    dtCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
            "    dtMutation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
            "    \n" +
            "        PRIMARY KEY (TermID)\n" +
            "    )",
            "CREATE TABLE tblText (\n" +
                    "        TextID BIGINT not null generated always as identity,\n" +
                    "    sTitle VARCHAR(150),\n" +
                    "    sText CLOB,\n" +
                    "    sAuthor VARCHAR(100),\n" +
                    "    sCoAuthors VARCHAR(150),\n" +
                    "    sSubtitle VARCHAR(300),\n" +
                    "    sYear VARCHAR(15),\n" +
                    "    sPublication VARCHAR(200),\n" +
                    "    sPages VARCHAR(20),\n" +
                    "    nVolume INTEGER,\n" +
                    "    nNumber INTEGER,\n" +
                    "    sPublisher VARCHAR(99),\n" +
                    "    sPlace VARCHAR(99),\n" +
                    "    nType INTEGER,\n" +
                    "    dtCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    dtMutation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    \n" +
                    "        PRIMARY KEY (TextID)\n" +
                    "    )",
                    "CREATE TABLE tblPerson (\n" +
                            "        PersonID BIGINT not null generated always as identity,\n" +
                            "    sName VARCHAR(99) not null,\n" +
                            "    sFirstname VARCHAR(50),\n" +
                            "    sText CLOB,\n" +
                            "    sFrom VARCHAR(30),\n" +
                            "    sTo VARCHAR(30),\n" +
                            "    dtCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                            "    dtMutation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                            "    \n" +
                            "        PRIMARY KEY (PersonID)\n" +
                            "    )",
                            "CREATE TABLE tblRelation (\n" +
                                    "        RelationID BIGINT not null generated always as identity,\n" +
                                    "    nType1 SMALLINT not null,\n" +
                                    "    nItem1 BIGINT not null,\n" +
                                    "    nType2 SMALLINT not null,\n" +
                                    "    nItem2 BIGINT not null,\n" +
                                    "    \n" +
                                    "        PRIMARY KEY (RelationID)\n" +
                                    "    )",
                                    "CREATE TABLE tblEventStore (\n" +
                                            "        EventStoreID BIGINT not null generated always as identity,\n" +
                                            "    dtCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                                            "    nType SMALLINT not null,\n" +
                                            "    sUniqueID VARCHAR(50) not null,\n" +
                                            "    sEvent CLOB not null,\n" +
                                            "    \n" +
                                            "        PRIMARY KEY (EventStoreID)\n" +
                                            "    )",
                                            "CREATE INDEX idxTerm_01 ON tblTerm(sTitle)", "CREATE INDEX idxTerm_02 ON tblTerm(dtCreation)",
                                            "CREATE INDEX idxTerm_03 ON tblTerm(dtMutation)", "CREATE INDEX idxText_01 ON tblText(sTitle)",
                                            "CREATE INDEX idxText_02 ON tblText(sAuthor, sCoAuthors)", "CREATE INDEX idxText_03 ON tblText(dtCreation)",
                                            "CREATE INDEX idxText_04 ON tblText(dtMutation)",
                                            "CREATE INDEX idxPerson_01 ON tblPerson(sName, sFirstname)",
                                            "CREATE INDEX idxPerson_02 ON tblPerson(sFrom, sTo)", "CREATE INDEX idxPerson_03 ON tblPerson(dtCreation)",
                                            "CREATE INDEX idxPerson_04 ON tblPerson(dtMutation)",
                                            "CREATE INDEX idxRelation_01 ON tblRelation(nType1, nItem1)",
                                            "CREATE INDEX idxRelation_02 ON tblRelation(nType2, nItem2)",
                                            "CREATE INDEX idxEventStore_01 ON tblEventStore(dtCreation)",
    "CREATE INDEX idxEventStore_02 ON tblEventStore(sUniqueID, dtCreation)" };

    @Test
    public void testGetCreateStatemens() throws Exception {
        final IDBObjectCreator creator = new TestEmbeddedCreator();
        final Collection<String> sqlCreate = creator.getCreateStatemens(Constants.XML_CREATE_OBJECTS);
        assertEquals(EXPECTED.length, sqlCreate.size());

        int i = 0;
        for (final String sql : sqlCreate) {
            assertEquals(EXPECTED[i++], sql);
        }
    }

    @Test
    public void testGetCreateStatemensEventStore() throws Exception {
        final List<String> expected = Arrays.asList(EXPECTED[4], EXPECTED[18], EXPECTED[19]);

        final IDBObjectCreator creator = new TestEmbeddedCreator();
        final Collection<String> sqlCreate = creator.getCreateEventStoreStatements(Constants.XML_CREATE_OBJECTS);
        assertEquals(expected.size(), sqlCreate.size());

        int i = 0;
        for (final String sql : sqlCreate) {
            assertEquals(expected.get(i++), sql);
        }
    }

}
