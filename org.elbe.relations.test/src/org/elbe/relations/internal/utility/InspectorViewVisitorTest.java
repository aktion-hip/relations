package org.elbe.relations.internal.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.data.utility.IItemVisitor;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		data = DataHouseKeeper.INSTANCE;
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testVisit() throws Exception {
		final TermHome lHome = data.getTermHome();

		final IItem lTerm = lHome.newTerm(EXP_TITLE + "1", EXP_TEXT + "1");
		assertEquals("number 1", 1, lHome.getCount());

		final IItemVisitor lVisitor = new InspectorViewVisitor();
		lTerm.visit(lVisitor);
		assertEquals("term title", EXP_TITLE + "1", lVisitor.getTitle());
		assertEquals("term subtitle", "", lVisitor.getSubTitle());
		assertEquals("term expected text", EXP_TEXT + "1", lVisitor.getText());
		assertTrue("term title editable",
		        ((InspectorViewVisitor) lVisitor).isTitleEditable());
		assertTrue("term text editable",
		        ((InspectorViewVisitor) lVisitor).isTextEditable());

		final IItem lText = data.getTextHome().newText(EXP_TITLE + "2",
		        EXP_TEXT + "2", "Author", "CoAuthor", EXP_SUBTITLE, "Year",
		        "Publication", "Pages", 0, 0, "Publisher", "Place", 1);
		lText.visit(lVisitor);
		assertEquals("text title", EXP_TITLE + "2", lVisitor.getTitle());
		assertEquals("text subtitle", "", lVisitor.getSubTitle());
		assertEquals(NL + "[text2...]", lVisitor.getText());
		assertTrue("text title editable",
		        ((InspectorViewVisitor) lVisitor).isTitleEditable());
		assertFalse("text text not editable",
		        ((InspectorViewVisitor) lVisitor).isTextEditable());

		final IItem lPerson = data.getPersonHome().newPerson("Name",
		        "Firstname", "From", "To", EXP_TEXT + "3");
		lPerson.visit(lVisitor);
		assertEquals("person title", "Name, Firstname", lVisitor.getTitle());
		assertEquals("person subtitle", "", lVisitor.getSubTitle());
		assertEquals("person expected text", EXP_TEXT + "3", lVisitor.getText());
		assertFalse("person title not editable",
		        ((InspectorViewVisitor) lVisitor).isTitleEditable());
		assertTrue("person text editable",
		        ((InspectorViewVisitor) lVisitor).isTextEditable());
	}

}
