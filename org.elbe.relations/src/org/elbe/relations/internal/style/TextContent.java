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
package org.elbe.relations.internal.style;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;

/**
 * Content model of Relations text fields.
 *
 * @author Luthiger Created on 03.09.2007
 * @see StyledTextContent
 */
public class TextContent implements StyledTextContent {
    private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$

    private final ListenerList<TextChangeListener> listeners = new ListenerList<>();

    private char[] textStore = new char[0]; // stores the actual text
    private int gapStart = -1; // the character position start of the gap
    private int gapEnd = -1; // the character position after the end of the gap
    private int gapLine = -1; // the line on which the gap exists, the gap will
    // always be associated with one line
    private final int highWatermark = 300;
    private final int lowWatermark = 50;

    private int[][] lines = new int[50][2]; // array of character positions and
    // lengths representing the lines of
    // text
    private int lineCount = 0; // the number of lines of text
    private int expandExp = 1; // the expansion exponent, used to increase the
    // lines array exponentially
    private int replaceExpandExp = 1; // the expansion exponent, used to
    // increase the lines array
    // exponentially

    public TextContent() {
        super();
        setText(""); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.swt.custom.StyledTextContent#addTextChangeListener(org.eclipse
     * .swt.custom.TextChangeListener)
     */
    @Override
    public void addTextChangeListener(final TextChangeListener listener) {
        if (listener == null) {
            error(SWT.ERROR_NULL_ARGUMENT);
        }
        this.listeners.add(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledTextContent#getCharCount()
     */
    @Override
    public int getCharCount() {
        return this.textStore.length - (this.gapEnd - this.gapStart);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledTextContent#getLine(int)
     */
    @Override
    public String getLine(final int lineIndex) {
        if (lineIndex >= this.lineCount || lineIndex < 0) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        final int lStart = this.lines[lineIndex][0];
        int lLength = this.lines[lineIndex][1];
        final int lEnd = lStart + lLength - 1;
        if (!gapExists() || lEnd < this.gapStart || lStart >= this.gapEnd) {
            // line is before or after the gap
            while (lLength - 1 >= 0
                    && isDelimiter(this.textStore[lStart + lLength - 1])) {
                lLength--;
            }
            return new String(this.textStore, lStart, lLength);
        } else {
            // gap is in the specified range, strip out the gap
            final StringBuilder buffer = new StringBuilder();
            final int gapLength = this.gapEnd - this.gapStart;
            buffer.append(this.textStore, lStart, this.gapStart - lStart);
            buffer.append(this.textStore, this.gapEnd, lLength - gapLength
                    - (this.gapStart - lStart));
            lLength = buffer.length();
            while (lLength - 1 >= 0
                    && isDelimiter(buffer.charAt(lLength - 1))) {
                lLength--;
            }
            return buffer.toString().substring(0, lLength);
        }
    }

    private boolean isDelimiter(final char inChar) {
        return inChar == SWT.CR || inChar == SWT.LF;
    }

    private boolean gapExists() {
        return this.gapStart != this.gapEnd;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledTextContent#getLineAtOffset(int)
     */
    @Override
    public int getLineAtOffset(final int inOffset) {
        if (inOffset > getCharCount() || inOffset < 0) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        int lPosition;
        if (inOffset < this.gapStart) {
            // position is before the gap
            lPosition = inOffset;
        } else {
            // position includes the gap
            lPosition = inOffset + this.gapEnd - this.gapStart;
        }

        // if last line and the line is not empty you can ask for
        // a position that doesn't exist (the one to the right of the
        // last character) - for inserting
        if (this.lineCount > 0) {
            final int lLastLine = this.lineCount - 1;
            if (lPosition == this.lines[lLastLine][0] + this.lines[lLastLine][1]) {
                return lLastLine;
            }
        }

        int lHigh = this.lineCount;
        int lLow = -1;
        int lIndex = this.lineCount;
        while (lHigh - lLow > 1) {
            lIndex = (lHigh + lLow) / 2;
            final int lineStart = this.lines[lIndex][0];
            final int lineEnd = lineStart + this.lines[lIndex][1] - 1;
            if (lPosition <= lineStart) {
                lHigh = lIndex;
            } else if (lPosition <= lineEnd) {
                lHigh = lIndex;
                break;
            } else {
                lLow = lIndex;
            }
        }
        return lHigh;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledTextContent#getLineCount()
     */
    @Override
    public int getLineCount() {
        return this.lineCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledTextContent#getLineDelimiter()
     */
    @Override
    public String getLineDelimiter() {
        return NL;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledTextContent#getOffsetAtLine(int)
     */
    @Override
    public int getOffsetAtLine(final int inLine) {
        if (inLine == 0) {
            return 0;
        }
        if (inLine >= this.lineCount || inLine < 0) {
            error(SWT.ERROR_INVALID_ARGUMENT);
        }
        final int lStart = this.lines[inLine][0];
        if (lStart > this.gapEnd) {
            return lStart - (this.gapEnd - this.gapStart);
        } else {
            return lStart;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledTextContent#getTextRange(int, int)
     */
    @Override
    public String getTextRange(final int inStart, final int inLength) {
        if (this.textStore == null) {
            return ""; //$NON-NLS-1$
        }
        if (inLength == 0) {
            return ""; //$NON-NLS-1$
        }
        final int lEnd = inStart + inLength;
        if (!gapExists() || lEnd < this.gapStart) {
            return new String(this.textStore, inStart, inLength);
        }
        if (this.gapStart < inStart) {
            final int gapLength = this.gapEnd - this.gapStart;
            return new String(this.textStore, inStart + gapLength, inLength);
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(this.textStore, inStart, this.gapStart - inStart);
        buffer.append(this.textStore, this.gapEnd, lEnd - this.gapStart);
        return buffer.toString();
    }

    @Override
    public void removeTextChangeListener(final TextChangeListener inListener) {
        this.listeners.remove(inListener);
    }

    @Override
    public void replaceTextRange(final int inStart, final int inReplaceLength,
            final String inText) {
        if (!isValidReplace(inStart, inReplaceLength)) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }

        // inform listeners
        final TextChangingEvent lChanging = new TextChangingEvent(this);
        lChanging.start = inStart;
        lChanging.replaceLineCount = lineCount(inStart, inReplaceLength);
        lChanging.newText = inText;
        lChanging.newLineCount = lineCount(inText);
        lChanging.replaceCharCount = inReplaceLength;
        lChanging.newCharCount = inText.length();

        final Object[] lListeners = this.listeners.getListeners();
        for (int i = 0; i < lListeners.length; ++i) {
            ((TextChangeListener) lListeners[i]).textChanging(lChanging);
        }

        delete(inStart, inReplaceLength, lChanging.replaceLineCount + 1);
        insert(inStart, inText);

        // inform listeners again
        for (int i = 0; i < lListeners.length; ++i) {
            ((TextChangeListener) lListeners[i])
            .textChanged(new TextChangedEvent(this));
        }
    }

    void insert(final int inStart, final String inText) {
        if (inText.isEmpty()) {
            return;
        }

        final int lStartLine = getLineAtOffset(inStart);
        final int lChange = inText.length();
        final boolean lEnd = inStart == getCharCount();
        adjustGap(inStart, lChange, lStartLine);

        // during an insert the gap will be adjusted to start at
        // position and it will be associated with startline, the
        // inserted text will be placed in the gap
        final int lStartLineOffset = getOffsetAtLine(lStartLine);
        // at this point, startLineLength will include the start line
        // and all of the newly inserted text
        final int lStartLineLength = getPhysicalLine(lStartLine).length();

        if (lChange > 0) {
            // shrink gap
            this.gapStart += lChange;
            for (int i = 0; i < inText.length(); i++) {
                this.textStore[inStart + i] = inText.charAt(i);
            }
        }

        // figure out the number of new lines that have been inserted
        final int[][] lNewLines = indexLines(lStartLineOffset,
                lStartLineLength, 10);
        // only insert an empty line if it is the last line in the text
        int lNewLinesNum = lNewLines.length - 1;
        if (lNewLines[lNewLinesNum][1] == 0) {
            // last inserted line is a new line
            if (lEnd) {
                // insert happening at end of the text, leave numNewLines as
                // is since the last new line will not be concatenated with
                // another
                // line
                lNewLinesNum += 1;
            } else {
                lNewLinesNum -= 1;
            }
        }

        // make room for the new lines
        expandLinesBy(lNewLinesNum);
        // shift down the lines after the replace line
        for (int i = this.lineCount - 1; i > lStartLine; i--) {
            this.lines[i + lNewLinesNum] = this.lines[i];
        }
        // insert the new lines
        for (int i = 0; i < lNewLinesNum; i++) {
            lNewLines[i][0] += lStartLineOffset;
            this.lines[lStartLine + i] = lNewLines[i];
        }
        // update the last inserted line
        if (lNewLinesNum < lNewLines.length) {
            lNewLines[lNewLinesNum][0] += lStartLineOffset;
            this.lines[lStartLine + lNewLinesNum] = lNewLines[lNewLinesNum];
        }

        this.lineCount += lNewLinesNum;
        this.gapLine = getLineAtPhysicalOffset(this.gapStart);
    }

    private void expandLinesBy(final int inLines) {
        final int lSize = this.lines.length;
        if (lSize - this.lineCount >= inLines) {
            return;
        }
        final int[][] lNewLines = new int[lSize + Math.max(10, inLines)][2];
        System.arraycopy(this.lines, 0, lNewLines, 0, lSize);
        this.lines = lNewLines;
    }

    private int getLineAtPhysicalOffset(final int inPosition) {
        int outHigh = this.lineCount;
        int lLow = -1;
        int lIndex = this.lineCount;
        while (outHigh - lLow > 1) {
            lIndex = (outHigh + lLow) / 2;
            final int lineStart = this.lines[lIndex][0];
            final int lineEnd = lineStart + this.lines[lIndex][1] - 1;
            if (inPosition <= lineStart) {
                outHigh = lIndex;
            } else if (inPosition <= lineEnd) {
                outHigh = lIndex;
                break;
            } else {
                lLow = lIndex;
            }
        }
        return outHigh;
    }

    private int[][] indexLines(final int inOffset, final int inLength,
            final int inNumLines) {
        int[][] lIndexedLines = new int[inNumLines][2];
        int lStart = 0;
        int lLineCount = 0;
        int i;
        this.replaceExpandExp = 1;
        for (i = lStart; i < inLength; i++) {
            final int lLocation = i + inOffset;
            if (lLocation >= this.gapStart && lLocation < this.gapEnd) {
                // ignore the gap
            } else {
                char lChar = this.textStore[lLocation];
                if (lChar == SWT.CR) {
                    // see if the next character is a LF
                    if (lLocation + 1 < this.textStore.length) {
                        lChar = this.textStore[lLocation + 1];
                        if (lChar == SWT.LF) {
                            i++;
                        }
                    }
                    lIndexedLines = addLineIndex(lStart, i - lStart + 1,
                            lIndexedLines, lLineCount);
                    lLineCount++;
                    lStart = i + 1;
                } else if (lChar == SWT.LF) {
                    lIndexedLines = addLineIndex(lStart, i - lStart + 1,
                            lIndexedLines, lLineCount);
                    lLineCount++;
                    lStart = i + 1;
                }
            }
        }
        final int[][] outLines = new int[lLineCount + 1][2];
        System.arraycopy(lIndexedLines, 0, outLines, 0, lLineCount);
        final int[] lRange = new int[] { lStart, i - lStart };
        outLines[lLineCount] = lRange;
        return outLines;
    }

    private int[][] addLineIndex(final int inStart, final int inLength,
            final int[][] inLinesArray, final int inCount) {
        final int lSize = inLinesArray.length;
        int[][] outLines = inLinesArray;
        if (inCount == lSize) {
            outLines = new int[lSize + (int) Math.pow(2, this.replaceExpandExp)][2];
            // outLines = new
            // int[lSize+Compatibility.pow2(replaceExpandExp)][2];
            this.replaceExpandExp++;
            System.arraycopy(inLinesArray, 0, outLines, 0, lSize);
        }
        final int[] lRange = new int[] { inStart, inLength };
        outLines[inCount] = lRange;
        return outLines;
    }

    private String getPhysicalLine(final int inIndex) {
        final int lStart = this.lines[inIndex][0];
        final int lLength = this.lines[inIndex][1];
        return getPhysicalText(lStart, lLength);
    }

    private String getPhysicalText(final int inStart, final int inLength) {
        return new String(this.textStore, inStart, inLength);
    }

    void adjustGap(final int inStart, final int inSizeHint,
            final int inStartLine) {
        if (inStart == this.gapStart) {
            // text is being inserted at the gap position
            final int lSize = this.gapEnd - this.gapStart - inSizeHint;
            if (this.lowWatermark <= lSize && lSize <= this.highWatermark) {
                return;
            }
        } else if (inStart + inSizeHint == this.gapStart && inSizeHint < 0) {
            // text is being deleted at the gap position
            final int lSize = this.gapEnd - this.gapStart - inSizeHint;
            if (this.lowWatermark <= lSize && lSize <= this.highWatermark) {
                return;
            }
        }
        moveAndResizeGap(inStart, inSizeHint, inStartLine);
    }

    void moveAndResizeGap(final int inStart, final int inSize,
            final int inGapLine) {
        char[] lContent = null;
        final int lOldSize = this.gapEnd - this.gapStart;
        int lNewSize;
        if (inSize > 0) {
            lNewSize = this.highWatermark + inSize;
        } else {
            lNewSize = this.lowWatermark - inSize;
        }
        // remove the old gap from the lines information
        if (gapExists()) {
            // adjust the line length
            this.lines[this.gapLine][1] = this.lines[this.gapLine][1] - lOldSize;
            // adjust the offsets of the lines after the gapLine
            for (int i = this.gapLine + 1; i < this.lineCount; i++) {
                this.lines[i][0] = this.lines[i][0] - lOldSize;
            }
        }

        if (lNewSize < 0) {
            if (lOldSize > 0) {
                // removing the gap
                lContent = new char[this.textStore.length - lOldSize];
                System.arraycopy(this.textStore, 0, lContent, 0, this.gapStart);
                System.arraycopy(this.textStore, this.gapEnd, lContent, this.gapStart,
                        lContent.length - this.gapStart);
                this.textStore = lContent;
            }
            this.gapStart = this.gapEnd = inStart;
            return;
        }
        lContent = new char[this.textStore.length + lNewSize - lOldSize];
        final int lNewGapStart = inStart;
        final int lNewGapEnd = lNewGapStart + lNewSize;
        if (lOldSize == 0) {
            System.arraycopy(this.textStore, 0, lContent, 0, lNewGapStart);
            System.arraycopy(this.textStore, lNewGapStart, lContent, lNewGapEnd,
                    lContent.length - lNewGapEnd);
        } else if (lNewGapStart < this.gapStart) {
            final int lDelta = this.gapStart - lNewGapStart;
            System.arraycopy(this.textStore, 0, lContent, 0, lNewGapStart);
            System.arraycopy(this.textStore, lNewGapStart, lContent, lNewGapEnd,
                    lDelta);
            System.arraycopy(this.textStore, this.gapEnd, lContent, lNewGapEnd + lDelta,
                    this.textStore.length - this.gapEnd);
        } else {
            final int lDelta = lNewGapStart - this.gapStart;
            System.arraycopy(this.textStore, 0, lContent, 0, this.gapStart);
            System.arraycopy(this.textStore, this.gapEnd, lContent, this.gapStart, lDelta);
            System.arraycopy(this.textStore, this.gapEnd + lDelta, lContent, lNewGapEnd,
                    lContent.length - lNewGapEnd);
        }
        this.textStore = lContent;
        this.gapStart = lNewGapStart;
        this.gapEnd = lNewGapEnd;

        // add the new gap to the lines information
        if (gapExists()) {
            this.gapLine = inGapLine;
            // adjust the line length
            final int lGapLength = this.gapEnd - this.gapStart;
            this.lines[this.gapLine][1] = this.lines[this.gapLine][1] + lGapLength;
            // adjust the offsets of the lines after the gapLine
            for (int i = this.gapLine + 1; i < this.lineCount; i++) {
                this.lines[i][0] = this.lines[i][0] + lGapLength;
            }
        }
    }

    private void delete(final int inPosition, final int inLength, final int inNumLines) {
        if (inLength == 0) {
            return;
        }

        final int lStartLine = getLineAtOffset(inPosition);
        final int lStartLineOffset = getOffsetAtLine(lStartLine);
        final int lEndLine = getLineAtOffset(inPosition + inLength);

        String lEndText = ""; //$NON-NLS-1$
        boolean lSplittingDelimiter = false;
        if (inPosition + inLength < getCharCount()) {
            lEndText = getTextRange(inPosition + inLength - 1, 2);
            if (lEndText.charAt(0) == SWT.CR
                    && lEndText.charAt(1) == SWT.LF) {
                lSplittingDelimiter = true;
            }
        }

        adjustGap(inPosition + inLength, -inLength, lStartLine);
        final int[][] lOldLines = indexLines(inPosition, inLength
                + this.gapEnd - this.gapStart, inNumLines);

        // enlarge the gap - the gap can be enlarged either to the
        // right or left
        if (inPosition + inLength == this.gapStart) {
            this.gapStart -= inLength;
        } else {
            this.gapEnd += inLength;
        }

        // figure out the length of the new concatenated line, do so by
        // finding the first line delmiter after position
        int j = inPosition;
        boolean eol = false;
        while (j < this.textStore.length && !eol) {
            if (j < this.gapStart || j >= this.gapEnd) {
                final char lChar = this.textStore[j];
                if (isDelimiter(lChar)) {
                    if (j + 1 < this.textStore.length
                            && lChar == SWT.CR && this.textStore[j + 1] == SWT.LF) {
                        j++;
                    }
                    eol = true;
                }
            }
            j++;
        }
        // update the line where the deletion started
        this.lines[lStartLine][1] = inPosition - lStartLineOffset
                + j - inPosition;
        // figure out the number of lines that have been deleted
        int lNumOldLines = lOldLines.length - 1;
        if (lSplittingDelimiter) {
            lNumOldLines -= 1;
        }
        // shift up the lines after the last deleted line, no need to update
        // the offset or length of the lines
        for (int i = lEndLine + 1; i < this.lineCount; i++) {
            this.lines[i - lNumOldLines] = this.lines[i];
        }
        this.lineCount -= lNumOldLines;
        this.gapLine = getLineAtPhysicalOffset(this.gapStart);
    }

    private int lineCount(final String lText) {
        int outLineCount = 0;
        final int lLength = lText.length();
        for (int i = 0; i < lLength; i++) {
            final char lChar = lText.charAt(i);
            if (lChar == SWT.CR) {
                if (i + 1 < lLength && lText.charAt(i + 1) == SWT.LF) {
                    i++;
                }
                outLineCount++;
            } else if (lChar == SWT.LF) {
                outLineCount++;
            }
        }
        return outLineCount;
    }

    private int lineCount(final int inOffset, final int inLength) {
        if (inLength == 0) {
            return 0;
        }
        int outLineCount = 0;
        int lCount = 0;
        int i = inOffset;
        if (i >= this.gapStart) {
            i += this.gapEnd - this.gapStart;
        }
        while (lCount < inLength) {
            if (i >= this.gapStart && i < this.gapEnd) {
                // ignore the gap
            } else {
                char lChar = this.textStore[i];
                if (lChar == SWT.CR) {
                    // see if the next character is a LF
                    if (i + 1 < this.textStore.length) {
                        lChar = this.textStore[i + 1];
                        if (lChar == SWT.LF) {
                            i++;
                            lCount++;
                        }
                    }
                    outLineCount++;
                } else if (lChar == SWT.LF) {
                    outLineCount++;
                }
                lCount++;
            }
            i++;
        }
        return outLineCount;
    }

    private boolean isValidReplace(final int inStart, final int inReplaceLength) {
        if (inReplaceLength == 0) {
            // inserting text, see if the \r\n line delimiter is being split
            if (inStart == 0) {
                return true;
            }
            if (inStart == getCharCount()) {
                return true;
            }
            final char lBefore = getTextRange(inStart - 1, 1).charAt(0);
            if (lBefore == '\r') {
                final char lAfter = getTextRange(inStart, 1).charAt(0);
                if (lAfter == '\n') {
                    return false;
                }
            }
        } else {
            // deleting text, see if part of a \r\n line delimiter is being
            // deleted
            final char lStart = getTextRange(inStart, 1).charAt(0);
            if (lStart == '\n') {
                // see if char before delete position is \r
                if (inStart != 0) { // NOPMD
                    final char lBefore = getTextRange(inStart - 1, 1).charAt(0);
                    if (lBefore == '\r') {
                        return false;
                    }
                }
            }
            final char lEnd = getTextRange(inStart + inReplaceLength - 1, 1)
                    .charAt(0);
            if (lEnd == '\r') {
                // see if char after delete position is \n
                if (inStart + inReplaceLength != getCharCount()) { // NOPMD
                    final char lAfter = getTextRange(inStart + inReplaceLength,
                            1).charAt(0);
                    if (lAfter == '\n') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.custom.StyledTextContent#setText(java.lang.String)
     */
    @Override
    public void setText(final String inText) {
        this.textStore = inText.toCharArray();
        this.gapStart = -1;
        this.gapEnd = -1;
        this.expandExp = 1;
        indexLines();

        final Object[] lListeners = this.listeners.getListeners();
        for (int i = 0; i < lListeners.length; ++i) {
            ((TextChangeListener) lListeners[i]).textSet(new TextChangedEvent(
                    this));
        }
    }

    /**
     * Reports an SWT error.
     * <p>
     *
     * @param code
     *            the error code
     */
    private void error(final int code) {
        SWT.error(code);
    }

    /**
     * Calculates the indexes of each line in the text store. Assumes no gap
     * exists. Optimized to do less checking.
     */
    private void indexLines() {
        int lStart = 0;
        this.lineCount = 0;
        final int lTextLength = this.textStore.length;
        int i;
        for (i = lStart; i < lTextLength; i++) {
            char lChar = this.textStore[i];
            if (lChar == SWT.CR) {
                // see if the next character is a LF
                if (i + 1 < lTextLength) {
                    lChar = this.textStore[i + 1];
                    if (lChar == SWT.LF) {
                        i++;
                    }
                }
                addLineIndex(lStart, i - lStart + 1);
                lStart = i + 1;
            } else if (lChar == SWT.LF) {
                addLineIndex(lStart, i - lStart + 1);
                lStart = i + 1;
            }
        }
        addLineIndex(lStart, i - lStart);
    }

    /**
     * Adds a line to the end of the line indexes array. Increases the size of
     * the array if necessary. <code>lineCount</code> is updated to reflect the
     * new entry.
     * <p>
     *
     * @param inStart
     *            the start of the line
     * @param inLength
     *            the length of the line
     */
    private void addLineIndex(final int inStart, final int inLength) {
        final int lSize = this.lines.length;
        if (this.lineCount == lSize) {
            // expand the lines by powers of 2
            final int[][] lNewLines = new int[lSize + pow2(this.expandExp)][2];
            System.arraycopy(this.lines, 0, lNewLines, 0, lSize);
            this.lines = lNewLines;
            this.expandExp++;
        }
        final int[] lRange = new int[] { inStart, inLength };
        this.lines[this.lineCount] = lRange;
        this.lineCount++;
    }

    private int pow2(final int exp) {
        final Double outPowered = Double.valueOf(Math.pow(2, exp));
        return outPowered.intValue();
    }

    public void dispose() {
        this.listeners.clear();
    }

}
