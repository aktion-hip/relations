package org.elbe.relations.internal.style;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit test
 * 
 * @author lbenno
 */
@RunWith(MockitoJUnitRunner.class)
public class StyleParserTest {
	private final static String NL = System.getProperty("line.separator");

	private Composite parent;

	@Before
	public void setUp() {
		parent = new Shell(Display.getDefault());
	}

	@Test
	public void test() throws Exception {
		final String lTest1 = "This is a <b>Test</b>, is'n it.";
		final String lTest2 = "Part of natural science."
		        + NL
		        + "Physics is a <b>fundamental</b> science where other natural <i>sciences</i> can be built on."
		        + NL + "Das ist ein <b>Test</b>.";
		final String lTest3 = "Part of natural <i>science</i>."
		        + NL
		        + "Physics is a <b>fundamental</b> science where other natural sciences can be built on."
		        + NL + "Das ist ein <b>Test</b>.";
		final String lTest4 = "This is an <u>underline</u> test.";

		final StyledText lWidget = new StyledText(parent, SWT.NONE);
		StyleParser.getInstance().parseTagged(lTest1, lWidget);

		StyleRange[] lRanges = lWidget.getStyleRanges();
		assertEquals("size 1", 1, lRanges.length);
		StyleRange lExpected = new StyleRange(10, 4, null, null, SWT.BOLD);
		assertEquals("style 1", lExpected, lRanges[0]);

		StyleParser.getInstance().parseTagged(lTest2, lWidget);
		lRanges = lWidget.getStyleRanges();
		assertEquals("size 2", 3, lRanges.length);
		lExpected = new StyleRange(38, 11, null, null, SWT.BOLD);
		assertEquals("style 2.1", lExpected, lRanges[0]);
		lExpected = new StyleRange(78, 8, null, null, SWT.ITALIC);
		assertEquals("style 2.2", lExpected, lRanges[1]);
		lExpected = new StyleRange(116, 4, null, null, SWT.BOLD);
		assertEquals("style 2.3", lExpected, lRanges[2]);

		StyleParser.getInstance().parseTagged(lTest3, lWidget);
		lRanges = lWidget.getStyleRanges();
		assertEquals("size 3", 3, lRanges.length);
		lExpected = new StyleRange(16, 7, null, null, SWT.ITALIC);
		assertEquals("style 3.1", lExpected, lRanges[0]);
		lExpected = new StyleRange(38, 11, null, null, SWT.BOLD);
		assertEquals("style 3.2", lExpected, lRanges[1]);
		lExpected = new StyleRange(116, 4, null, null, SWT.BOLD);
		assertEquals("style 3.3", lExpected, lRanges[2]);

		StyleParser.getInstance().parseTagged(lTest4, lWidget);
		lRanges = lWidget.getStyleRanges();
		assertEquals("size 4", 1, lRanges.length);
		lExpected = new StyleRange(11, 9, null, null, SWT.NORMAL);
		lExpected.underline = true;
		assertEquals("style 4.1", lExpected, lRanges[0]);

		final String lTest5 = "line <b>one - </b><i><b>line two </b></i><b>- line</b> three";
		StyleParser.getInstance().parseTagged(lTest5, lWidget);
		final String lCompare = lWidget.getText();
		lRanges = lWidget.getStyleRanges();
		assertEquals("size 5", 3, lRanges.length);
		int lStart = lRanges[0].start;
		assertEquals("range 5.1", "one - ",
		        lCompare.substring(lStart, lStart + lRanges[0].length));
		assertEquals("font style 5.1", SWT.BOLD, lRanges[0].fontStyle);
		lStart = lRanges[1].start;
		assertEquals("range 5.2", "line two ",
		        lCompare.substring(lStart, lStart + lRanges[1].length));
		assertEquals("font style 5.2", SWT.BOLD | SWT.ITALIC,
		        lRanges[1].fontStyle);
		lStart = lRanges[2].start;
		assertEquals("range 5.3", "- line",
		        lCompare.substring(lStart, lStart + lRanges[2].length));
		assertEquals("font style 5.3", SWT.BOLD, lRanges[2].fontStyle);

		final String lTest6 = "test <u><i>underline italic</i></u> end.";
		StyleParser.getInstance().parseTagged(lTest6, lWidget);
		assertEquals("text 6", "test underline italic end.", lWidget.getText());
		lRanges = lWidget.getStyleRanges();
		assertEquals("size 6", 1, lRanges.length);
		assertEquals("font style 6.1", SWT.ITALIC, lRanges[0].fontStyle);
		assertTrue("underlined 6.2", lRanges[0].underline);

		final String lTest7 = "<b>test with path:</b> c:\\\\path\\\\to\\\\somewhere";
		StyleParser.getInstance().parseTagged(lTest7, lWidget);
		assertEquals("test with path: c:\\path\\to\\somewhere",
		        lWidget.getText());
	}

	@Test
	public void testXMLEntity() throws Exception {
		String lText = "Example of command: nohup command &";
		final StyledText lWidget = new StyledText(parent, SWT.NONE);

		StyleParser.getInstance().parseTagged(lText, lWidget);
		assertEquals("parse &", lText, lWidget.getText());

		lText = "Example of entity: &amp;";
		StyleParser.getInstance().parseTagged(lText, lWidget);
		assertEquals("parse entity", lText, lWidget.getText());

		lText = "Example of leq: <tag>1 &lt; 2</tag>";
		StyleParser.getInstance().parseTagged(lText, lWidget);
		assertEquals("test '<' entity (1)", "Example of leq: 1 < 2" + NL,
		        lWidget.getText());

		lText = "Example: <i>1 &lt; 2</i>.";
		StyleParser.getInstance().parseTagged(lText, lWidget);
		assertEquals("test '<' entity (2)", "Example: 1 < 2.",
		        lWidget.getText());
		final StyleRange[] lRanges = lWidget.getStyleRanges();
		assertEquals("ranges length", 1, lRanges.length);
		assertEquals("font style", SWT.ITALIC, lRanges[0].fontStyle);
		assertEquals("range start", 9, lRanges[0].start);
		assertEquals("range length", 5, lRanges[0].length);
	}

	@Test
	public void testParseList() throws Exception {
		final String lTestList1 = "line 1" + NL
		        + "<ol_number indent=\"0\"><li>line 2" + NL
		        + "<ol_upper indent=\"1\"><li>line 3</li>" + NL
		        + "<li>line 4</li>" + NL
		        + "</ol_upper><ol_lower indent=\"2\"><li>line 5</li>" + NL
		        + "</ol_lower></li><li>line 6" + NL
		        + "<ul indent=\"1\"><li>line 7</li>" + NL + "<li>line 8</li>"
		        + NL + "</ul></li><li>line 9</li>" + NL + "</ol_number>line 10";
		final String lTestList2 = "<ol_number indent=\"0\"><li>line 1" + NL
		        + "<ol_upper indent=\"1\"><li>line 2</li>" + NL + "<li>line 3"
		        + NL + "<ol_lower indent=\"2\"><li>line 4</li>" + NL
		        + "<li>line 5</li>" + NL + "</ol_lower></li><li>line 6</li>"
		        + NL + "</ol_upper></li><li>line 7" + NL
		        + "<ul indent=\"1\"><li>line 8</li>" + NL + "<li>line 9</li>"
		        + NL + "</ul></li><li>line 10</li>" + NL + "</ol_number>";
		final String lTestList3 = "line 1" + NL
		        + "<ol_number indent=\"0\"><li>line 2</li>" + NL
		        + "<li>line 3</li>" + NL + "<li>line 4</li>" + NL
		        + "<li>line 5</li>" + NL + "<li>line 6</li>" + NL
		        + "<li>line 7</li>" + NL + "</ol_number>line 8";

		StyledText lWidget = new StyledText(parent, SWT.NONE);
		StyleParser.getInstance().parseTagged(lTestList1, lWidget);

		final Bullet lBullet1 = Styles.getBullet(ST.BULLET_CUSTOM,
		        Styles.BULLET_WIDTH);
		final Bullet lBullet2 = Styles.getBullet(ST.BULLET_LETTER_UPPER,
		        Styles.BULLET_WIDTH + Styles.INDENT);
		final Bullet lBullet3 = Styles.getBullet(ST.BULLET_LETTER_LOWER,
		        Styles.BULLET_WIDTH + 2 * Styles.INDENT);
		final Bullet lBullet4 = Styles.getBullet(ST.BULLET_DOT,
		        Styles.BULLET_WIDTH + Styles.INDENT);

		String lExpectedText = "line 1" + NL + "line 2" + NL + "line 3" + NL
		        + "line 4" + NL + "line 5" + NL + "line 6" + NL + "line 7" + NL
		        + "line 8" + NL + "line 9" + NL + "line 10";
		Bullet[] lExpectedBullets = new Bullet[] { null, lBullet1, lBullet2,
		        lBullet2, lBullet3, lBullet1, lBullet4, lBullet4, lBullet1,
		        null };
		for (int i = 0; i < lExpectedBullets.length; i++) {
			assertEqualBullets("Bullet 1." + i, lExpectedBullets[i],
			        lWidget.getLineBullet(i));
		}
		assertEquals("text 1", lExpectedText, lWidget.getText());

		lWidget = new StyledText(parent, SWT.NONE);
		StyleParser.getInstance().parseTagged(lTestList2, lWidget);
		lExpectedText = "line 1" + NL + "line 2" + NL + "line 3" + NL
		        + "line 4" + NL + "line 5" + NL + "line 6" + NL + "line 7" + NL
		        + "line 8" + NL + "line 9" + NL + "line 10" + NL;
		lExpectedBullets = new Bullet[] { lBullet1, lBullet2, lBullet2,
		        lBullet3, lBullet3, lBullet2, lBullet1, lBullet4, lBullet4,
		        lBullet1 };
		for (int i = 0; i < lExpectedBullets.length; i++) {
			assertEqualBullets("Bullet 2." + i, lExpectedBullets[i],
			        lWidget.getLineBullet(i));
		}
		assertEquals("text 2", lExpectedText, lWidget.getText());

		lWidget = new StyledText(parent, SWT.NONE);
		StyleParser.getInstance().parseTagged(lTestList3, lWidget);

		lExpectedText = "line 1" + NL + "line 2" + NL + "line 3" + NL
		        + "line 4" + NL + "line 5" + NL + "line 6" + NL + "line 7" + NL
		        + "line 8";
		lExpectedBullets = new Bullet[] { null, lBullet1, lBullet1, lBullet1,
		        lBullet1, lBullet1, lBullet1, null };
		for (int i = 0; i < lExpectedBullets.length; i++) {
			assertEqualBullets("Bullet 3." + i, lExpectedBullets[i],
			        lWidget.getLineBullet(i));
		}
		assertEquals("text 3", lExpectedText, lWidget.getText());
	}

	private void assertEqualBullets(final String inMsg,
	        final Bullet inExpected, final Bullet inActual) {
		if (inExpected == null) {
			assertNull(inMsg, inActual);
			return;
		}
		assertTrue(
		        inMsg,
		        inExpected.type == inActual.type
		                && inExpected.style.metrics.width == inActual.style.metrics.width);
	}

	@Test
	public void testGetUntaggedText() throws Exception {
		final String lExpected = "This is a Test, is'n it.";
		final String lTest = "This is a <b>Test</b>, is'n it.";

		assertEquals("untagged 1", lExpected, StyleParser.getInstance()
		        .getUntaggedText(lTest));
		assertEquals("untagged 2", lExpected, StyleParser.getInstance()
		        .getUntaggedText(lExpected));
	}

	@Test
	public void testGetTagged() throws Exception {
		String lText = "This is a <b>Test</b>, is'n it.";

		StyledText lWidget = new StyledText(parent, SWT.NONE);
		StyleParser.getInstance().parseTagged(lText, lWidget);

		StyleRange lNew = new StyleRange(10, 4, null, null, SWT.BOLD
		        | SWT.ITALIC);
		lWidget.setStyleRanges(new StyleRange[] { lNew });

		String lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("tagged 1", "This is a <i><b>Test</b></i>, is'n it.",
		        lTagged);

		lNew = new StyleRange(10, 4, null, null, SWT.BOLD | SWT.ITALIC);
		lNew.underline = true;
		lWidget.setStyleRanges(new StyleRange[] { lNew });

		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("tagged 1",
		        "This is a <u><i><b>Test</b></i></u>, is'n it.", lTagged);

		// widget with bold = bold-italic = bold style range sequence
		lText = "line one - line two - line three";
		lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lText);
		lWidget.setStyleRanges(new StyleRange[] { new StyleRange(5, 21, null,
		        null, SWT.BOLD) });
		lWidget.setStyleRange(new StyleRange(11, 9, null, null, SWT.BOLD
		        | SWT.ITALIC));

		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("tagged 2",
		        "line <b>one - </b><i><b>line two </b></i><b>- line</b> three",
		        lTagged);

		// widget with Windows like path
		lText = "the path is: c:\\path\\to\\somewhere";
		lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lText);
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("the path is: c:\\\\path\\\\to\\\\somewhere", lTagged);
	}

	@Test
	public void testGetTaggedWithLT() throws Exception {
		String lText = "text with lt character: 1 < 2";
		StyledText lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lText);
		String lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("handle 'lt' entity (1)",
		        "text with lt character: 1 &lt; 2", lTagged);

		lText = "Text with bold and 1 < 2";
		lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lText);
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		lWidget.setStyleRanges(new StyleRange[] { new StyleRange(10, 4, null,
		        null, SWT.BOLD) });
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("handle 'lt' entity (2)",
		        "Text with <b>bold</b> and 1 &lt; 2", lTagged);

		lText = "Text with bold and 1 < 2.";
		lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lText);
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		lWidget.setStyleRanges(new StyleRange[] {
		        new StyleRange(10, 4, null, null, SWT.BOLD),
		        new StyleRange(19, 5, null, null, SWT.ITALIC) });
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("handle 'lt' entity (3)",
		        "Text with <b>bold</b> and <i>1 &lt; 2</i>.", lTagged);
	}

	@Test
	public void testGetTaggedList() throws Exception {
		final String lExpected1 = "line 1" + NL + "line 2" + NL + "line 3" + NL
		        + "<ul indent=\"0\"><li>line 4</li>" + NL + "<li>line 5</li>"
		        + NL + "<li>line 6</li>" + NL + "<li>line 7</li>" + NL
		        + "<li>line 8</li>" + NL + "</ul>line 9" + NL + "line 10";
		final String lExpected2 = "line 1" + NL + "line 2" + NL + "line 3" + NL
		        + "<ol_number indent=\"0\"><li>line 4</li>" + NL
		        + "<li>line 5</li>" + NL + "<li>line 6</li>" + NL
		        + "<li>line 7</li>" + NL + "<li>line 8</li>" + NL
		        + "<li>line 9</li>" + NL + "<li>line 10</li>" + NL
		        + "</ol_number>";
		final String lExpected3 = "line 1" + NL
		        + "<ol_number indent=\"0\"><li>line 2" + NL
		        + "<ol_upper indent=\"1\"><li>line 3</li>" + NL
		        + "<li>line 4</li>" + NL
		        + "</ol_upper><ol_lower indent=\"2\"><li>line 5</li>" + NL
		        + "</ol_lower></li><li>line 6" + NL
		        + "<ul indent=\"1\"><li>line 7</li>" + NL + "<li>line 8</li>"
		        + NL + "</ul></li><li>line 9</li>" + NL + "</ol_number>line 10";
		final String lExpected4 = "<ol_number indent=\"0\"><li>line 1" + NL
		        + "<ol_upper indent=\"1\"><li>line 2</li>" + NL + "<li>line 3"
		        + NL + "<ol_lower indent=\"2\"><li>line 4</li>" + NL
		        + "<li>line 5</li>" + NL + "</ol_lower></li><li>line 6</li>"
		        + NL + "</ol_upper></li><li>line 7" + NL
		        + "<ul indent=\"1\"><li>line 8</li>" + NL + "<li>line 9</li>"
		        + NL + "</ul></li><li>line 10</li>" + NL + "</ol_number>";
		final String lExpected5 = "line 1" + NL + "line 2" + NL + "line 3" + NL
		        + "<ul indent=\"0\"><li>line 4</li>" + NL + "<li>line 5</li>"
		        + NL + "<li><b>line</b> 6</li>" + NL + "<li>l<b>in</b>e 7</li>"
		        + NL + "<li>line 8</li>" + NL + "</ul>line 9" + NL + "line 10";
		final String lTest = createList(10);

		StyledText lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lTest);
		lWidget.setLineBullet(3, 5,
		        Styles.getBullet(ST.BULLET_DOT, Styles.BULLET_WIDTH));

		String lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("tagged 1", lExpected1, lTagged);

		lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lTest);
		lWidget.setLineBullet(3, 7,
		        Styles.getBullet(ST.BULLET_CUSTOM, Styles.BULLET_WIDTH));
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("tagged 2", lExpected2, lTagged);

		lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lTest);

		// create complex set of nested lists
		Bullet lBullet1 = Styles.getBullet(ST.BULLET_CUSTOM,
		        Styles.BULLET_WIDTH);
		Bullet lBullet2 = Styles.getBullet(ST.BULLET_LETTER_UPPER,
		        Styles.BULLET_WIDTH + Styles.INDENT);
		Bullet lBullet3 = Styles.getBullet(ST.BULLET_LETTER_LOWER,
		        Styles.BULLET_WIDTH + 2 * Styles.INDENT);
		Bullet lBullet4 = Styles.getBullet(ST.BULLET_DOT, Styles.BULLET_WIDTH
		        + Styles.INDENT);

		lWidget.setLineBullet(1, 1, lBullet1);
		lWidget.setLineBullet(2, 2, lBullet2);
		lWidget.setLineBullet(4, 1, lBullet3);
		lWidget.setLineBullet(5, 1, lBullet1);
		lWidget.setLineBullet(6, 2, lBullet4);
		lWidget.setLineBullet(8, 1, lBullet1);
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("tagged 3", lExpected3, lTagged);

		lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lTest);

		// create complex set of nested lists
		lBullet1 = Styles.getBullet(ST.BULLET_CUSTOM, Styles.BULLET_WIDTH);
		lBullet2 = Styles.getBullet(ST.BULLET_LETTER_UPPER, Styles.BULLET_WIDTH
		        + Styles.INDENT);
		lBullet3 = Styles.getBullet(ST.BULLET_LETTER_LOWER, Styles.BULLET_WIDTH
		        + 2 * Styles.INDENT);
		lBullet4 = Styles.getBullet(ST.BULLET_DOT, Styles.BULLET_WIDTH
		        + Styles.INDENT);

		lWidget.setLineBullet(0, 1, lBullet1);
		lWidget.setLineBullet(1, 2, lBullet2);
		lWidget.setLineBullet(3, 2, lBullet3);
		lWidget.setLineBullet(5, 1, lBullet2);
		lWidget.setLineBullet(6, 1, lBullet1);
		lWidget.setLineBullet(7, 2, lBullet4);
		lWidget.setLineBullet(9, 1, lBullet1);
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("tagged 4", lExpected4, lTagged);

		// list with bold text
		lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lTest);
		lWidget.setLineBullet(3, 5,
		        Styles.getBullet(ST.BULLET_DOT, Styles.BULLET_WIDTH));
		final StyleRange lBold1 = new StyleRange(40, 4, null, null, SWT.BOLD);
		final StyleRange lBold2 = new StyleRange(49, 2, null, null, SWT.BOLD);
		lWidget.setStyleRanges(new StyleRange[] { lBold1, lBold2 });
		lTagged = StyleParser.getInstance().getTagged(lWidget);
		assertEquals("tagged 5", lExpected5, lTagged);
	}

	private String createList(final int inLength) {
		final StringBuffer outList = new StringBuffer();
		int i = 0;
		while (++i <= inLength - 1) {
			outList.append("line " + i).append(NL);
		}
		outList.append("line " + i);
		return new String(outList);
	}

	/*
	 * TODO: StyledText has a bug: the following call should return 1 entry.
	 * Instead, two entries are returned, the second with negative length. Fixed
	 * in 3.5, removed work around in StyleParser.tagLine() [line 450]: if
	 * (lRange.length <= 0) break;
	 */
	@Test
	public void testSWTError() throws Exception {
		final String lText = "Part of natural science."
		        + NL
		        + "Physics is a fundamental science where other natural sciences can be built on."
		        + NL + "This is a test.";
		final StyleRange[] lStyles = new StyleRange[] {
		        new StyleRange(38, 11, null, null, SWT.BOLD),
		        new StyleRange(78, 8, null, null, SWT.ITALIC),
		        new StyleRange(114, 4, null, null, SWT.BOLD) };

		final StyledText lWidget = new StyledText(parent, SWT.NONE);
		lWidget.setText(lText);
		lWidget.setStyleRanges(lStyles);

		final StyleRange[] lRanges = lWidget.getStyleRanges(58, 40);
		System.out.println("Expected length: 1, actual length: "
		        + lRanges.length);
		if (lRanges.length == 2) {
			System.out.println("Length of second range: " + lRanges[1].length);
		}
	}

}
