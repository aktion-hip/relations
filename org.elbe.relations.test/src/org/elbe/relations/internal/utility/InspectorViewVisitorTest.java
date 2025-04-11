package org.elbe.relations.internal.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.data.utility.IItemVisitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * JUnit test
 *
 * @author lbenno
 */
public class InspectorViewVisitorTest {
    private final static String EXP_TITLE = "title";
    private final static String EXP_SUBTITLE = "sub-title";
    private final static String EXP_TEXT = "text";
    private final static String NL = System.getProperty("line.separator");

    private static DataHouseKeeper data;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        data = DataHouseKeeper.INSTANCE;
    }


    @AfterEach
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testVisit() throws Exception {
        final TermHome lHome = data.getTermHome();

        final IItem lTerm = lHome.newTerm(EXP_TITLE + "1", EXP_TEXT + "1");
        assertEquals(1, lHome.getCount());

        final IItemVisitor lVisitor = new InspectorViewVisitor();
        lTerm.visit(lVisitor);
        assertEquals(EXP_TITLE + "1", lVisitor.getTitle());
        assertEquals("", lVisitor.getSubTitle());
        assertEquals(EXP_TEXT + "1", lVisitor.getText());
        assertTrue(
                ((InspectorViewVisitor) lVisitor).isTitleEditable());
        assertTrue(
                ((InspectorViewVisitor) lVisitor).isTextEditable());

        final IItem lText = data.getTextHome().newText(EXP_TITLE + "2",
                EXP_TEXT + "2", "Author", "CoAuthor", EXP_SUBTITLE, "Year",
                "Publication", "Pages", 0, 0, "Publisher", "Place", 1);
        lText.visit(lVisitor);
        assertEquals(EXP_TITLE + "2", lVisitor.getTitle());
        assertEquals("", lVisitor.getSubTitle());
        assertEquals(NL + "[text2...]", lVisitor.getText());
        assertTrue(
                ((InspectorViewVisitor) lVisitor).isTitleEditable());
        assertFalse(
                ((InspectorViewVisitor) lVisitor).isTextEditable());

        final IItem lPerson = data.getPersonHome().newPerson("Name",
                "Firstname", "From", "To", EXP_TEXT + "3");
        lPerson.visit(lVisitor);
        assertEquals("Name, Firstname", lVisitor.getTitle());
        assertEquals("", lVisitor.getSubTitle());
        assertEquals(EXP_TEXT + "3", lVisitor.getText());
        assertFalse(
                ((InspectorViewVisitor) lVisitor).isTitleEditable());
        assertTrue(
                ((InspectorViewVisitor) lVisitor).isTextEditable());
    }

}
