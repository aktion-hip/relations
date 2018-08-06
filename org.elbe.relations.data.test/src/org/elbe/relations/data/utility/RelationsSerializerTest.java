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

package org.elbe.relations.data.utility;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IProgressMonitor;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.hip.kernel.bom.AbstractSerializer;
import org.hip.kernel.bom.DomainObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

/** JUnit test
 *
 * @author lbenno */
public class RelationsSerializerTest {
    private final static String NL = System.getProperty("line.separator");
    private final static String EXPECTED = "" + NL + "<TermEntry>" + NL
            + "        <Modified field=\"DTMUTATION\" type=\"Timestamp\">" + NL + "        </Modified>" + NL
            + "        <Title field=\"STITLE\" type=\"String\">RelationsSerializerTest" + NL + "        </Title>" + NL
            + "        <Text field=\"STEXT\" type=\"String\">The investments in R&amp;D are <bold>very</<bold> important. They must not be < 1'000 per month."
            + NL + "        </Text>" + NL + "        <ID field=\"TERMID\" type=\"Long\">" + NL + "        </ID>" + NL
            + "        <Created field=\"DTCREATION\" type=\"Timestamp\">" + NL + "        </Created>" + NL
            + "</TermEntry>";

    private static DataHouseKeeper data;

    @Mock
    private IProgressMonitor monitor;

    @BeforeClass
    public static void before() {
        data = DataHouseKeeper.INSTANCE;
    }

    @After
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testSerialize() throws Exception {
        final String lText = "The investments in R&D are <bold>very</<bold> important. They must not be < 1'000 per month.";

        final DomainObject lModel = data.getTermHome().create();
        lModel.set(TermHome.KEY_TEXT, lText);
        lModel.set(TermHome.KEY_TITLE, "RelationsSerializerTest");

        final AbstractSerializer lVisitor = new RelationsSerializer();
        lModel.accept(lVisitor);
        assertEquals(EXPECTED, lVisitor.toString());
    }

    @Test
    public void testPrepare() {
        final String lOriginal = "The investments in R&D are <bold>very</<bold> important. They must not be < 1'000 per month.";
        final String lExpectedPrepared = "The investments in R&amp;D are <bold>very</<bold> important. They must not be < 1'000 per month.";

        assertEquals(lExpectedPrepared, RelationsSerializer.prepareForExport(lOriginal));
        assertEquals(lOriginal, RelationsSerializer.prepareForImport(lExpectedPrepared));
    }

}
