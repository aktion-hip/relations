package org.elbe.relations.internal.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit test
 *
 * @author lbenno
 */
public class TextStylerTest {
    private final static String NL = System.getProperty("line.separator");
    private final static String TEXT = "This is the StyledText, kind-of get_all_and_run widget. Hey, check 66666."; // "This is the StyledText, kind-of get_all_and_run widget.";
    private final static StyleRange EXPECTED1 = new StyleRange(10, 20, null,
            null, SWT.ITALIC);

    private Composite parent;

    @BeforeEach
    public void setUp() {
        this.parent = new Shell(Display.getDefault());
    }

    @Test
    public void testWordSelection() {
        final char[] lLine = TEXT.toCharArray();
        final TextStylerSub lStyler = new TextStylerSub(null);

        Point lPoint = lStyler.selectWord(10, 0, lLine);
        assertEquals("the",
                TEXT.substring(lPoint.x, lPoint.x + lPoint.y));

        lPoint = lStyler.selectWord(14, 0, lLine);
        assertEquals("StyledText",
                TEXT.substring(lPoint.x, lPoint.x + lPoint.y));

        lPoint = lStyler.selectWord(0, 0, lLine);
        assertEquals("This",
                TEXT.substring(lPoint.x, lPoint.x + lPoint.y));

        lPoint = lStyler.selectWord(TEXT.length(), 0, lLine);
        assertEquals(".",
                TEXT.substring(lPoint.x, lPoint.x + lPoint.y));

        lPoint = lStyler.selectWord(25, 0, lLine);
        assertEquals("kind",
                TEXT.substring(lPoint.x, lPoint.x + lPoint.y));

        lPoint = lStyler.selectWord(35, 0, lLine);
        assertEquals("get_all_and_run",
                TEXT.substring(lPoint.x, lPoint.x + lPoint.y));

        lPoint = lStyler.selectWord(70, 0, lLine);
        assertEquals("66666",
                TEXT.substring(lPoint.x, lPoint.x + lPoint.y));
    }

    @Test
    public void testWordSelection2() throws Exception {
        final String lText = "line one" + NL + "line two with_bold" + NL
                + "line three";
        StyledText lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);
        lWidget.setCaretOffset(23);

        StyleRange[] lRanges = lWidget.getStyleRanges();
        assertEquals(0, lRanges.length);

        TextStyler lStyler = new TextStyler(lWidget);
        lStyler.format(Styles.Style.BOLD, true);
        lRanges = lWidget.getStyleRanges();
        assertEquals(1, lRanges.length);
        StyleRange lRange = lRanges[0];
        assertEquals("with_bold",
                lWidget.getTextRange(lRange.start, lRange.length));

        lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);
        lWidget.setCaretOffset(lText.length());
        lStyler = new TextStyler(lWidget);
        lStyler.format(Styles.Style.BOLD, true);

        lRanges = lWidget.getStyleRanges();
        assertEquals(1, lRanges.length);
        lRange = lRanges[0];
        assertEquals("three",
                lWidget.getTextRange(lRange.start, lRange.length));
    }

    @Test
    public void testModifyRanges() throws Exception {
        final TextStylerSub lStyler = new TextStylerSub(null);

        StyleRange[] lRanges = lStyler.modifyRanges(Styles.Style.BOLD, 10, 20,
                getRangesSingle());
        assertEquals(1, lRanges.length);
        assertEquals(getDefaultExpected(), lRanges[0]);

        lRanges = lStyler.modifyRanges(Styles.Style.BOLD, 5, 25,
                getRangesSingle());
        assertEquals(2, lRanges.length);
        StyleRange lExpected = new StyleRange(5, 5, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[0]);
        assertEquals(getDefaultExpected(), lRanges[1]);

        lRanges = lStyler.modifyRanges(Styles.Style.BOLD, 5, 35,
                getRangesSingle());
        assertEquals(3, lRanges.length);
        lExpected = new StyleRange(5, 5, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[0]);
        assertEquals(getDefaultExpected(), lRanges[1]);
        lExpected = new StyleRange(30, 10, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[2]);

        lRanges = lStyler.modifyRanges(Styles.Style.BOLD, 5, 62,
                getRangesDoubleItalic());
        assertEquals(4, lRanges.length);
        lExpected = new StyleRange(5, 5, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[0]);
        assertEquals(getDefaultExpected(), lRanges[1]);
        lExpected = new StyleRange(30, 20, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[2]);
        lExpected = new StyleRange(50, 17, null, null, SWT.BOLD | SWT.ITALIC);
        assertEquals(lExpected, lRanges[3]);

        lRanges = lStyler.modifyRanges(Styles.Style.BOLD, 5, 62,
                getRangesDoubleMixed());
        assertEquals(3, lRanges.length);
        lExpected = new StyleRange(5, 5, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[0]);
        assertEquals(getDefaultExpected(), lRanges[1]);
        lExpected = new StyleRange(30, 37, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[2]);

        lRanges = lStyler.modifyRanges(Styles.Style.BOLD, 5, 70,
                getRangesDoubleMixed());
        assertEquals(3, lRanges.length);
        lExpected = new StyleRange(5, 5, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[0]);
        assertEquals(getDefaultExpected(), lRanges[1]);
        lExpected = new StyleRange(30, 45, null, null, SWT.BOLD);
        assertEquals(lExpected, lRanges[2]);
    }

    @Test
    public void testModifyRanges2() throws Exception {
        final String lText = "line one" + NL + "line two" + NL + "line three";
        StyledText lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);
        lWidget.setSelection(new Point(5, 14));

        TextStyler lStyler = new TextStyler(lWidget);
        lStyler.format(Styles.Style.BOLD, true);

        StyleRange[] lRanges = lWidget.getStyleRanges();
        assertEquals(2, lRanges.length);
        int lStart = lRanges[0].start;
        assertEquals("one",
                lText.substring(lStart, lStart + lRanges[0].length));
        lStart = lRanges[1].start;
        assertEquals("line",
                lText.substring(lStart, lStart + lRanges[1].length));

        lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);
        lWidget.setSelection(new Point(5, 30));
        lStyler = new TextStyler(lWidget);
        lStyler.format(Styles.Style.BOLD, true);

        lRanges = lWidget.getStyleRanges();
        assertEquals(3, lRanges.length);
        lStart = lRanges[0].start;
        assertEquals("one",
                lText.substring(lStart, lStart + lRanges[0].length));
        lStart = lRanges[1].start;
        assertEquals("line two",
                lText.substring(lStart, lStart + lRanges[1].length));
        lStart = lRanges[2].start;
        assertEquals("line three",
                lText.substring(lStart, lStart + lRanges[2].length));
    }

    @Test
    public void testSetStyleRanges() throws Exception {
        final String lText = "line one - line two - line three";
        StyledText lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);

        // a normal style range intersecting a bold style range results in two
        // separated bold style ranges
        StyleRange lBold = new StyleRange(5, 21, null, null, SWT.BOLD);
        lWidget.setStyleRanges(new StyleRange[] { lBold });
        StyleRange[] lRanges = lWidget.getStyleRanges();
        assertEquals(1, lRanges.length);
        int lStart = lRanges[0].start;
        assertEquals("one - line two - line",
                lText.substring(lStart, lStart + lRanges[0].length));

        lWidget.setStyleRange(new StyleRange(11, 9, null, null));
        lRanges = lWidget.getStyleRanges();
        assertEquals(2, lRanges.length);
        lStart = lRanges[0].start;
        assertEquals("one - ",
                lText.substring(lStart, lStart + lRanges[0].length));
        lStart = lRanges[1].start;
        assertEquals("- line",
                lText.substring(lStart, lStart + lRanges[1].length));

        // a bold-italic style range intersecting a bold style range results in
        // three subsequent style ranges
        lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);

        lBold = new StyleRange(5, 21, null, null, SWT.BOLD);
        lWidget.setStyleRanges(new StyleRange[] { lBold });
        lWidget.setStyleRange(new StyleRange(11, 9, null, null, SWT.BOLD
                | SWT.ITALIC));

        lRanges = lWidget.getStyleRanges();
        assertEquals(3, lRanges.length);
        lStart = lRanges[0].start;
        assertEquals("one - ",
                lText.substring(lStart, lStart + lRanges[0].length));
        assertEquals(SWT.BOLD, lRanges[0].fontStyle);
        lStart = lRanges[1].start;
        assertEquals("line two ",
                lText.substring(lStart, lStart + lRanges[1].length));
        assertEquals(SWT.BOLD | SWT.ITALIC,
                lRanges[1].fontStyle);
        lStart = lRanges[2].start;
        assertEquals("- line",
                lText.substring(lStart, lStart + lRanges[2].length));
        assertEquals(SWT.BOLD, lRanges[2].fontStyle);

        // same test with TextStyler
        lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);
        lWidget.setSelection(new Point(5, 26));

        TextStyler lStyler = new TextStyler(lWidget);
        lStyler.format(Styles.Style.BOLD, true);

        lRanges = lWidget.getStyleRanges();
        assertEquals(1, lRanges.length);
        lStart = lRanges[0].start;
        assertEquals("one - line two - line",
                lText.substring(lStart, lStart + lRanges[0].length));

        lWidget.setSelection(new Point(11, 20));
        lStyler.format(Styles.Style.BOLD, false);
        lRanges = lWidget.getStyleRanges();
        assertEquals(2, lRanges.length);
        lStart = lRanges[0].start;
        assertEquals("one - ",
                lText.substring(lStart, lStart + lRanges[0].length));
        lStart = lRanges[1].start;
        assertEquals("- line",
                lText.substring(lStart, lStart + lRanges[1].length));

        // same test bold and italic
        lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);
        lWidget.setSelection(new Point(5, 26));

        // range 5, 21, bold-italic
        lStyler = new TextStyler(lWidget);
        lStyler.format(Styles.Style.BOLD, true);
        lStyler.format(Styles.Style.ITALIC, true);

        lRanges = lWidget.getStyleRanges();
        assertEquals(1, lRanges.length);
        lStart = lRanges[0].start;
        assertEquals("one - line two - line",
                lText.substring(lStart, lStart + lRanges[0].length));
        assertEquals(SWT.BOLD | SWT.ITALIC,
                lRanges[0].fontStyle);

        // remove bold -> range 11, 9, italic
        lWidget.setSelection(new Point(11, 20));
        lStyler.format(Styles.Style.BOLD, false);
        lRanges = lWidget.getStyleRanges();
        assertEquals(3, lRanges.length);
        lStart = lRanges[0].start;
        assertEquals("one - ",
                lText.substring(lStart, lStart + lRanges[0].length));
        assertEquals(SWT.BOLD | SWT.ITALIC,
                lRanges[0].fontStyle);
        lStart = lRanges[1].start;
        assertEquals("line two ",
                lText.substring(lStart, lStart + lRanges[1].length));
        assertEquals(SWT.ITALIC, lRanges[1].fontStyle);
        lStart = lRanges[2].start;
        assertEquals("- line",
                lText.substring(lStart, lStart + lRanges[2].length));
        assertEquals(SWT.BOLD | SWT.ITALIC,
                lRanges[2].fontStyle);

        // start with bold = bold-italic = bold style range sequence, then
        // select an overspreading area to style it bold
        lWidget = new StyledText(this.parent, SWT.NONE);
        lWidget.setText(lText);
        lWidget.setSelection(new Point(5, 26));
        lStyler = new TextStyler(lWidget);
        lStyler.format(Styles.Style.BOLD, true);
        lWidget.setSelection(new Point(11, 20));
        lStyler.format(Styles.Style.ITALIC, true);
        lRanges = lWidget.getStyleRanges();
        assertEquals(3, lRanges.length);

        lWidget.setSelection(new Point(2, 31));
        lStyler.format(Styles.Style.BOLD, true);
        lRanges = lWidget.getStyleRanges();
        assertEquals(3, lRanges.length);
        lStart = lRanges[0].start;
        assertEquals("ne one - ",
                lText.substring(lStart, lStart + lRanges[0].length));
        assertEquals(SWT.BOLD, lRanges[0].fontStyle);
        lStart = lRanges[1].start;
        assertEquals("line two ",
                lText.substring(lStart, lStart + lRanges[1].length));
        assertEquals(SWT.BOLD | SWT.ITALIC,
                lRanges[1].fontStyle);
        lStart = lRanges[2].start;
        assertEquals("- line thre",
                lText.substring(lStart, lStart + lRanges[2].length));
        assertEquals(SWT.BOLD, lRanges[2].fontStyle);
    }

    private StyleRange getDefaultExpected() {
        final StyleRange outExpected = new StyleRange(EXPECTED1.start,
                EXPECTED1.length, null, null, EXPECTED1.fontStyle);
        outExpected.fontStyle |= SWT.BOLD;
        return outExpected;
    }

    private StyleRange[] getRangesSingle() {
        final StyleRange[] outRanges = new StyleRange[] { EXPECTED1 };
        return outRanges;
    }

    private StyleRange[] getRangesDoubleItalic() {
        final StyleRange lRange = new StyleRange(50, 17, null, null, SWT.ITALIC);
        final StyleRange[] outRanges = new StyleRange[] { EXPECTED1, lRange };
        return outRanges;
    }

    private StyleRange[] getRangesDoubleMixed() {
        final StyleRange lRange = new StyleRange(50, 17, null, null, SWT.BOLD);
        final StyleRange[] outRanges = new StyleRange[] { EXPECTED1, lRange };
        return outRanges;
    }

    // ---

    private class TextStylerSub extends TextStyler {
        public TextStylerSub(final StyledText inWidget) {
            super(inWidget);
        }

        @Override
        public Point selectWord(final int inPosition, final int inLineStart,
                final char[] inLine) {
            return super.selectWord(inPosition, inLineStart, inLine);
        }

        @Override
        public StyleRange[] modifyRanges(final Styles.Style inStyle,
                final int inStart, final int inLength,
                final StyleRange[] inRanges) {
            return super.modifyRanges(inStyle, inStart, inLength, inRanges);
        }
    }

}
