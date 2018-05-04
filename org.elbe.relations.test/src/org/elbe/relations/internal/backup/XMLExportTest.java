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

package org.elbe.relations.internal.backup;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.io.IOUnitils;
import org.w3c.dom.Document;

/**
 * JUnit test
 *
 * @author lbenno
 */
@RunWith(MockitoJUnitRunner.class)
public class XMLExportTest {
    private static final String FILE_NAME = "export.tmp";

    private static DataHouseKeeper data;

    private File exportFile;

    @Mock
    private IProgressMonitor monitor;

    private XMLExport exporter;

    @BeforeClass
    public static void before() {
        data = DataHouseKeeper.INSTANCE;
    }

    @Before
    public void setUp() throws Exception {
        final IItem lTerm = data.createTerm("test term",
                "the test term's description");
        final IItem lPerson = data.createPerson("Doe", "Jane");
        data.createText("test text", "Test, Text");
        data.createRelation(lTerm, lPerson);

        this.exportFile = IOUnitils.createTempFile(FILE_NAME);
    }

    @After
    public void tearDown() throws Exception {
        data.deleteAllInAll();
        IOUnitils.deleteTempFileOrDir(new File(FILE_NAME));
    }

    @Test
    public void testExport() throws Exception {
        this.exporter = new XMLExport(this.exportFile.getAbsolutePath(), Locale.ENGLISH, 0);
        this.exporter.export(this.monitor);
        this.exporter.close();

        final DocumentBuilder lDocBuilder = DocumentBuilderFactory
                .newInstance().newDocumentBuilder();
        final Document lDoc = lDocBuilder.parse(this.exportFile);

        // term entry
        final XPath lXPath = XPathFactory.newInstance().newXPath();
        XPathExpression lExpr = lXPath
                .compile("/RelationsExport/TermEntries/TermEntry/Title/text()");
        assertEquals("test term", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        lExpr = lXPath
                .compile("/RelationsExport/TermEntries/TermEntry/Text/text()");
        assertEquals("the test term's description",
                lExpr.evaluate(lDoc, XPathConstants.STRING).toString().trim());

        lExpr = lXPath
                .compile("/RelationsExport/TermEntries/TermEntry/ID/@field");
        assertEquals("TERMID", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        lExpr = lXPath
                .compile("/RelationsExport/TermEntries/TermEntry/ID/text()");
        final String lTermId = lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim();

        // text entry
        lExpr = lXPath
                .compile("/RelationsExport/TextEntries/TextEntry/Author/text()");
        assertEquals("Test, Text", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        lExpr = lXPath
                .compile("/RelationsExport/TextEntries/TextEntry/Title/text()");
        assertEquals("test text", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        lExpr = lXPath
                .compile("/RelationsExport/TextEntries/TextEntry/ID/@field");
        assertEquals("TEXTID", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        // person entry
        lExpr = lXPath
                .compile("/RelationsExport/PersonEntries/PersonEntry/Name/text()");
        assertEquals("Doe", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        lExpr = lXPath
                .compile("/RelationsExport/PersonEntries/PersonEntry/Firstname/text()");
        assertEquals("Jane", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        lExpr = lXPath
                .compile("/RelationsExport/PersonEntries/PersonEntry/ID/@field");
        assertEquals("PERSONID", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        lExpr = lXPath
                .compile("/RelationsExport/PersonEntries/PersonEntry/ID/text()");
        final String lPersonId = lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim();

        // relations
        lExpr = lXPath
                .compile("/RelationsExport/RelationEntries/RelationEntry/Type1/text()");
        assertEquals("1", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());
        lExpr = lXPath
                .compile("/RelationsExport/RelationEntries/RelationEntry/Type2/text()");
        assertEquals("3", lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());
        lExpr = lXPath
                .compile("/RelationsExport/RelationEntries/RelationEntry/Item1/text()");
        assertEquals(lTermId, lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());
        lExpr = lXPath
                .compile("/RelationsExport/RelationEntries/RelationEntry/Item2/text()");
        assertEquals(lPersonId, lExpr.evaluate(lDoc, XPathConstants.STRING)
                .toString().trim());

        // lExpr = lXPath
        // .compile("not(/RelationsExport/RelationEntries/*[count(*)>0 or text()])");

    }

}
