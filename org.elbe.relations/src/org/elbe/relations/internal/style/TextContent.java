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
	private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	private final ListenerList listeners = new ListenerList();

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
	public void addTextChangeListener(final TextChangeListener inListener) {
		if (inListener == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		listeners.add(inListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.custom.StyledTextContent#getCharCount()
	 */
	@Override
	public int getCharCount() {
		return (textStore.length - (gapEnd - gapStart));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.custom.StyledTextContent#getLine(int)
	 */
	@Override
	public String getLine(final int inLineIndex) {
		if ((inLineIndex >= lineCount) || (inLineIndex < 0))
			error(SWT.ERROR_INVALID_ARGUMENT);
		final int lStart = lines[inLineIndex][0];
		int lLength = lines[inLineIndex][1];
		final int lEnd = lStart + lLength - 1;
		if (!gapExists() || (lEnd < gapStart) || (lStart >= gapEnd)) {
			// line is before or after the gap
			while ((lLength - 1 >= 0)
			        && isDelimiter(textStore[lStart + lLength - 1])) {
				lLength--;
			}
			return new String(textStore, lStart, lLength);
		} else {
			// gap is in the specified range, strip out the gap
			final StringBuffer lBuffer = new StringBuffer();
			final int gapLength = gapEnd - gapStart;
			lBuffer.append(textStore, lStart, gapStart - lStart);
			lBuffer.append(textStore, gapEnd, lLength - gapLength
			        - (gapStart - lStart));
			lLength = lBuffer.length();
			while ((lLength - 1 >= 0)
			        && isDelimiter(lBuffer.charAt(lLength - 1))) {
				lLength--;
			}
			return lBuffer.toString().substring(0, lLength);
		}
	}

	private boolean isDelimiter(final char inChar) {
		if (inChar == SWT.CR)
			return true;
		if (inChar == SWT.LF)
			return true;
		return false;
	}

	private boolean gapExists() {
		return gapStart != gapEnd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.custom.StyledTextContent#getLineAtOffset(int)
	 */
	@Override
	public int getLineAtOffset(final int inOffset) {
		if ((inOffset > getCharCount()) || (inOffset < 0))
			error(SWT.ERROR_INVALID_ARGUMENT);
		int lPosition;
		if (inOffset < gapStart) {
			// position is before the gap
			lPosition = inOffset;
		} else {
			// position includes the gap
			lPosition = inOffset + (gapEnd - gapStart);
		}

		// if last line and the line is not empty you can ask for
		// a position that doesn't exist (the one to the right of the
		// last character) - for inserting
		if (lineCount > 0) {
			final int lLastLine = lineCount - 1;
			if (lPosition == lines[lLastLine][0] + lines[lLastLine][1]) {
				return lLastLine;
			}
		}

		int lHigh = lineCount;
		int lLow = -1;
		int lIndex = lineCount;
		while (lHigh - lLow > 1) {
			lIndex = (lHigh + lLow) / 2;
			final int lineStart = lines[lIndex][0];
			final int lineEnd = lineStart + lines[lIndex][1] - 1;
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
		return lineCount;
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
		if (inLine == 0)
			return 0;
		if ((inLine >= lineCount) || (inLine < 0))
			error(SWT.ERROR_INVALID_ARGUMENT);
		final int lStart = lines[inLine][0];
		if (lStart > gapEnd) {
			return lStart - (gapEnd - gapStart);
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
		if (textStore == null)
			return ""; //$NON-NLS-1$
		if (inLength == 0)
			return ""; //$NON-NLS-1$
		final int lEnd = inStart + inLength;
		if (!gapExists() || (lEnd < gapStart))
			return new String(textStore, inStart, inLength);
		if (gapStart < inStart) {
			final int gapLength = gapEnd - gapStart;
			return new String(textStore, inStart + gapLength, inLength);
		}
		final StringBuffer lBuffer = new StringBuffer();
		lBuffer.append(textStore, inStart, gapStart - inStart);
		lBuffer.append(textStore, gapEnd, lEnd - gapStart);
		return lBuffer.toString();
	}

	@Override
	public void removeTextChangeListener(final TextChangeListener inListener) {
		listeners.remove(inListener);
	}

	@Override
	public void replaceTextRange(final int inStart, final int inReplaceLength,
	        final String inText) {
		if (!isValidReplace(inStart, inReplaceLength))
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);

		// inform listeners
		final TextChangingEvent lChanging = new TextChangingEvent(this);
		lChanging.start = inStart;
		lChanging.replaceLineCount = lineCount(inStart, inReplaceLength);
		lChanging.newText = inText;
		lChanging.newLineCount = lineCount(inText);
		lChanging.replaceCharCount = inReplaceLength;
		lChanging.newCharCount = inText.length();

		final Object[] lListeners = listeners.getListeners();
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
		if (inText.length() == 0)
			return;

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
			gapStart += (lChange);
			for (int i = 0; i < inText.length(); i++) {
				textStore[inStart + i] = inText.charAt(i);
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
		for (int i = lineCount - 1; i > lStartLine; i--) {
			lines[i + lNewLinesNum] = lines[i];
		}
		// insert the new lines
		for (int i = 0; i < lNewLinesNum; i++) {
			lNewLines[i][0] += lStartLineOffset;
			lines[lStartLine + i] = lNewLines[i];
		}
		// update the last inserted line
		if (lNewLinesNum < lNewLines.length) {
			lNewLines[lNewLinesNum][0] += lStartLineOffset;
			lines[lStartLine + lNewLinesNum] = lNewLines[lNewLinesNum];
		}

		lineCount += lNewLinesNum;
		gapLine = getLineAtPhysicalOffset(gapStart);
	}

	private void expandLinesBy(final int inLines) {
		final int lSize = lines.length;
		if (lSize - lineCount >= inLines) {
			return;
		}
		final int[][] lNewLines = new int[lSize + Math.max(10, inLines)][2];
		System.arraycopy(lines, 0, lNewLines, 0, lSize);
		lines = lNewLines;
	}

	private int getLineAtPhysicalOffset(final int inPosition) {
		int outHigh = lineCount;
		int lLow = -1;
		int lIndex = lineCount;
		while (outHigh - lLow > 1) {
			lIndex = (outHigh + lLow) / 2;
			final int lineStart = lines[lIndex][0];
			final int lineEnd = lineStart + lines[lIndex][1] - 1;
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
		replaceExpandExp = 1;
		for (i = lStart; i < inLength; i++) {
			final int lLocation = i + inOffset;
			if ((lLocation >= gapStart) && (lLocation < gapEnd)) {
				// ignore the gap
			} else {
				char lChar = textStore[lLocation];
				if (lChar == SWT.CR) {
					// see if the next character is a LF
					if (lLocation + 1 < textStore.length) {
						lChar = textStore[lLocation + 1];
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
			outLines = new int[lSize + (int) (Math.pow(2, replaceExpandExp))][2];
			// outLines = new
			// int[lSize+Compatibility.pow2(replaceExpandExp)][2];
			replaceExpandExp++;
			System.arraycopy(inLinesArray, 0, outLines, 0, lSize);
		}
		final int[] lRange = new int[] { inStart, inLength };
		outLines[inCount] = lRange;
		return outLines;
	}

	private String getPhysicalLine(final int inIndex) {
		final int lStart = lines[inIndex][0];
		final int lLength = lines[inIndex][1];
		return getPhysicalText(lStart, lLength);
	}

	private String getPhysicalText(final int inStart, final int inLength) {
		return new String(textStore, inStart, inLength);
	}

	void adjustGap(final int inStart, final int inSizeHint,
	        final int inStartLine) {
		if (inStart == gapStart) {
			// text is being inserted at the gap position
			final int lSize = (gapEnd - gapStart) - inSizeHint;
			if (lowWatermark <= lSize && lSize <= highWatermark)
				return;
		} else if ((inStart + inSizeHint == gapStart) && (inSizeHint < 0)) {
			// text is being deleted at the gap position
			final int lSize = (gapEnd - gapStart) - inSizeHint;
			if (lowWatermark <= lSize && lSize <= highWatermark)
				return;
		}
		moveAndResizeGap(inStart, inSizeHint, inStartLine);
	}

	void moveAndResizeGap(final int inStart, final int inSize,
	        final int inGapLine) {
		char[] lContent = null;
		final int lOldSize = gapEnd - gapStart;
		int lNewSize;
		if (inSize > 0) {
			lNewSize = highWatermark + inSize;
		} else {
			lNewSize = lowWatermark - inSize;
		}
		// remove the old gap from the lines information
		if (gapExists()) {
			// adjust the line length
			lines[gapLine][1] = lines[gapLine][1] - lOldSize;
			// adjust the offsets of the lines after the gapLine
			for (int i = gapLine + 1; i < lineCount; i++) {
				lines[i][0] = lines[i][0] - lOldSize;
			}
		}

		if (lNewSize < 0) {
			if (lOldSize > 0) {
				// removing the gap
				lContent = new char[textStore.length - lOldSize];
				System.arraycopy(textStore, 0, lContent, 0, gapStart);
				System.arraycopy(textStore, gapEnd, lContent, gapStart,
				        lContent.length - gapStart);
				textStore = lContent;
			}
			gapStart = gapEnd = inStart;
			return;
		}
		lContent = new char[textStore.length + (lNewSize - lOldSize)];
		final int lNewGapStart = inStart;
		final int lNewGapEnd = lNewGapStart + lNewSize;
		if (lOldSize == 0) {
			System.arraycopy(textStore, 0, lContent, 0, lNewGapStart);
			System.arraycopy(textStore, lNewGapStart, lContent, lNewGapEnd,
			        lContent.length - lNewGapEnd);
		} else if (lNewGapStart < gapStart) {
			final int lDelta = gapStart - lNewGapStart;
			System.arraycopy(textStore, 0, lContent, 0, lNewGapStart);
			System.arraycopy(textStore, lNewGapStart, lContent, lNewGapEnd,
			        lDelta);
			System.arraycopy(textStore, gapEnd, lContent, lNewGapEnd + lDelta,
			        textStore.length - gapEnd);
		} else {
			final int lDelta = lNewGapStart - gapStart;
			System.arraycopy(textStore, 0, lContent, 0, gapStart);
			System.arraycopy(textStore, gapEnd, lContent, gapStart, lDelta);
			System.arraycopy(textStore, gapEnd + lDelta, lContent, lNewGapEnd,
			        lContent.length - lNewGapEnd);
		}
		textStore = lContent;
		gapStart = lNewGapStart;
		gapEnd = lNewGapEnd;

		// add the new gap to the lines information
		if (gapExists()) {
			gapLine = inGapLine;
			// adjust the line length
			final int lGapLength = gapEnd - gapStart;
			lines[gapLine][1] = lines[gapLine][1] + (lGapLength);
			// adjust the offsets of the lines after the gapLine
			for (int i = gapLine + 1; i < lineCount; i++) {
				lines[i][0] = lines[i][0] + lGapLength;
			}
		}
	}

	private void delete(final int inPosition, final int inLength,
	        final int inNumLines) {
		if (inLength == 0)
			return;

		final int lStartLine = getLineAtOffset(inPosition);
		final int lStartLineOffset = getOffsetAtLine(lStartLine);
		final int lEndLine = getLineAtOffset(inPosition + inLength);

		String lEndText = ""; //$NON-NLS-1$
		boolean lSplittingDelimiter = false;
		if (inPosition + inLength < getCharCount()) {
			lEndText = getTextRange(inPosition + inLength - 1, 2);
			if ((lEndText.charAt(0) == SWT.CR)
			        && (lEndText.charAt(1) == SWT.LF)) {
				lSplittingDelimiter = true;
			}
		}

		adjustGap(inPosition + inLength, -inLength, lStartLine);
		final int[][] lOldLines = indexLines(inPosition, inLength
		        + (gapEnd - gapStart), inNumLines);

		// enlarge the gap - the gap can be enlarged either to the
		// right or left
		if (inPosition + inLength == gapStart) {
			gapStart -= inLength;
		} else {
			gapEnd += inLength;
		}

		// figure out the length of the new concatenated line, do so by
		// finding the first line delmiter after position
		int j = inPosition;
		boolean eol = false;
		while (j < textStore.length && !eol) {
			if (j < gapStart || j >= gapEnd) {
				final char lChar = textStore[j];
				if (isDelimiter(lChar)) {
					if (j + 1 < textStore.length
					        && (lChar == SWT.CR && (textStore[j + 1] == SWT.LF))) {
						j++;
					}
					eol = true;
				}
			}
			j++;
		}
		// update the line where the deletion started
		lines[lStartLine][1] = (inPosition - lStartLineOffset)
		        + (j - inPosition);
		// figure out the number of lines that have been deleted
		int lNumOldLines = lOldLines.length - 1;
		if (lSplittingDelimiter)
			lNumOldLines -= 1;
		// shift up the lines after the last deleted line, no need to update
		// the offset or length of the lines
		for (int i = lEndLine + 1; i < lineCount; i++) {
			lines[i - lNumOldLines] = lines[i];
		}
		lineCount -= lNumOldLines;
		gapLine = getLineAtPhysicalOffset(gapStart);
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
		if (i >= gapStart) {
			i += gapEnd - gapStart;
		}
		while (lCount < inLength) {
			if ((i >= gapStart) && (i < gapEnd)) {
				// ignore the gap
			} else {
				char lChar = textStore[i];
				if (lChar == SWT.CR) {
					// see if the next character is a LF
					if (i + 1 < textStore.length) {
						lChar = textStore[i + 1];
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
			if (inStart == 0)
				return true;
			if (inStart == getCharCount())
				return true;
			final char lBefore = getTextRange(inStart - 1, 1).charAt(0);
			if (lBefore == '\r') {
				final char lAfter = getTextRange(inStart, 1).charAt(0);
				if (lAfter == '\n')
					return false;
			}
		} else {
			// deleting text, see if part of a \r\n line delimiter is being
			// deleted
			final char lStart = getTextRange(inStart, 1).charAt(0);
			if (lStart == '\n') {
				// see if char before delete position is \r
				if (inStart != 0) { // NOPMD
					final char lBefore = getTextRange(inStart - 1, 1).charAt(0);
					if (lBefore == '\r')
						return false;
				}
			}
			final char lEnd = getTextRange(inStart + inReplaceLength - 1, 1)
			        .charAt(0);
			if (lEnd == '\r') {
				// see if char after delete position is \n
				if (inStart + inReplaceLength != getCharCount()) { // NOPMD
					final char lAfter = getTextRange(inStart + inReplaceLength,
					        1).charAt(0);
					if (lAfter == '\n')
						return false;
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
		textStore = inText.toCharArray();
		gapStart = -1;
		gapEnd = -1;
		expandExp = 1;
		indexLines();

		final Object[] lListeners = listeners.getListeners();
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
		lineCount = 0;
		final int lTextLength = textStore.length;
		int i;
		for (i = lStart; i < lTextLength; i++) {
			char lChar = textStore[i];
			if (lChar == SWT.CR) {
				// see if the next character is a LF
				if (i + 1 < lTextLength) {
					lChar = textStore[i + 1];
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
		final int lSize = lines.length;
		if (lineCount == lSize) {
			// expand the lines by powers of 2
			final int[][] lNewLines = new int[lSize + pow2(expandExp)][2];
			System.arraycopy(lines, 0, lNewLines, 0, lSize);
			lines = lNewLines;
			expandExp++;
		}
		final int[] lRange = new int[] { inStart, inLength };
		lines[lineCount] = lRange;
		lineCount++;
	}

	private int pow2(final int inExp) {
		final Double outPowered = new Double(Math.pow(2, inExp));
		return outPowered.intValue();
	}

	public void dispose() {
		listeners.clear();
	}

}
