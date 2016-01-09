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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Point;

/**
 * Helper class that adds style to the <code>StyledText</code> widget.
 *
 * @author Luthiger Created on 05.09.2007
 */
public class TextStyler {
	private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	private final StyledText widget;

	/**
	 * @param inWidget
	 *            StyledText the widget to apply the styles
	 */
	public TextStyler(final StyledText inWidget) {
		widget = inWidget;
	}

	/**
	 * Applies the specified style to the widget.
	 *
	 * @param inStyle
	 *            Styles.Style
	 * @param inFormatNew
	 *            boolean if <code>true</code> the style is applied, else it is
	 *            removed.
	 */
	public void format(final Styles.Style inStyle, final boolean inFormatNew) {
		if (inStyle.isBlock) {
			// block style, therefore, we have to format a list
			final Point lLineSelection = getLineSelection();
			if (inFormatNew) {
				if (doIndent(lLineSelection)) {
					indentSelectedBlock(inStyle, lLineSelection.x,
					        lLineSelection.y);
				} else {
					formatSelectedBlock(inStyle, lLineSelection.x,
					        lLineSelection.y);
				}
			} else {
				unformatSelectedBlock(lLineSelection.x, lLineSelection.y);
			}
		} else {
			// inline style
			if (inFormatNew) {
				if (hasSelection()) {
					final Collection<Point> lSelections = createSelectionRanges(
					        widget);
					for (final Point lSelection : lSelections) {
						formatSelection(inStyle, lSelection);
					}
				} else {
					formatWord(inStyle);
				}
			} else {
				if (hasSelection()) {
					unformatSelection(inStyle);
				} else {
					unformatRange(inStyle);
				}
			}
		}
		widget.redraw();
	}

	private void unformatSelection(final Styles.Style inStyle) {
		final Point lSelection = widget.getSelection();
		final StyleRange lOld = widget.getStyleRangeAtOffset(lSelection.x);
		final StyleRange lNew = new StyleRange(lSelection.x,
		        lSelection.y - lSelection.x, null, null);
		if (lOld == null) {
			lNew.fontStyle = inStyle.getStyleBit();
			lNew.underline = inStyle.underline;
		} else {
			lNew.fontStyle = lOld.fontStyle ^ inStyle.getStyleBit();
			lNew.underline = inStyle.underline ? false : lOld.underline;
		}
		widget.setStyleRange(lNew);
	}

	/**
	 * Breaks a selection spanning over multiple lines into disjunct single line
	 * selections.
	 *
	 * @param inWidget
	 *            StyledText
	 * @return Collection<Point> collection of ranges to style. The
	 *         concatenation of these selections results in the original
	 *         selection.
	 */
	private Collection<Point> createSelectionRanges(final StyledText inWidget) {
		final Collection<Point> outPoints = new ArrayList<Point>();
		final Point lSelection = inWidget.getSelectionRange();
		final String lText = inWidget.getSelectionText();
		final int lLenghtNL = NL.length();
		final String[] lSplits = lText.split(NL);
		int lStart = lSelection.x;
		for (final String lSplit : lSplits) {
			outPoints.add(new Point(lStart, lSplit.length()));
			lStart += lSplit.length() + lLenghtNL;
		}
		return outPoints;
	}

	private boolean doIndent(final Point inLineSelection) {
		final int lBlockStart = inLineSelection.x;
		final Bullet lBullet = widget.getLineBullet(lBlockStart);
		if (lBullet == null) {
			return false;
		}
		if (lBlockStart > 0) {
			final Bullet lBulletBefore = widget.getLineBullet(lBlockStart - 1);
			if (lBulletBefore == null) {
				return false;
			}
			if (lBullet.hashCode() == lBulletBefore.hashCode()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indents the selected or (if no selection) actual line.
	 */
	public void indentLines() {
		final Point lLineSelection = getLineSelection();
		indentSelectedBlock(null, lLineSelection.x, lLineSelection.y);
	}

	/**
	 * Remove indent of the selected or (if no selection) actual line.
	 */
	public void dedentLines() {
		final Point lLineSelection = getLineSelection();
		Bullet lBullet = widget.getLineBullet(lLineSelection.x);
		// do we have to merge the selection with the preceding line style?
		if (lLineSelection.x > 0) {
			lBullet = searchFistDifferentBullet(lLineSelection.x, lBullet);
		} else {
			final int lIndent = Math.max(Styles.BULLET_WIDTH,
			        lBullet.style.metrics.width - Styles.INDENT);
			lBullet = Styles.getBullet(lBullet.type, lIndent);
		}
		widget.setLineBullet(lLineSelection.x, lLineSelection.y, null);
		widget.setLineBullet(lLineSelection.x, lLineSelection.y, lBullet);
		widget.redraw();
	}

	private Bullet searchFistDifferentBullet(final int inLineStart,
	        final Bullet inBullet) {
		int i = inLineStart;
		while (i > 0) {
			Bullet outBullet = null;
			if ((outBullet = widget.getLineBullet(--i)) != inBullet) {
				return outBullet;
			}
		}
		return null;
	}

	/**
	 * Indents the selected block.
	 *
	 * @param inStyle
	 *            StyleProperty. If <code>null</code>, the actual bullet style
	 *            is used.
	 * @param inLineStart
	 *            int start of the lines to be indented
	 * @param inLineCount
	 *            int number of lines to be indented
	 */
	private void indentSelectedBlock(final Styles.Style inStyle,
	        final int inLineStart, final int inLineCount) {
		final Bullet lBullet = widget.getLineBullet(inLineStart);
		final int lStyle = inStyle == null ? lBullet.type
		        : inStyle.getStyleBit();
		widget.setLineBullet(inLineStart, inLineCount, null);
		widget.setLineBullet(inLineStart, inLineCount, Styles.getBullet(lStyle,
		        lBullet.style.metrics.width + Styles.INDENT));
		widget.redraw();
	}

	/**
	 * @return Point(x=LineStart, y=LineCount)
	 */
	private Point getLineSelection() {
		int lLineStart = 0;
		int lLineCount = 1;
		if (hasSelection()) {
			final Point lSelection = widget.getSelection();
			lLineStart = widget.getLineAtOffset(lSelection.x);
			lLineCount = widget.getLineAtOffset(lSelection.y) - lLineStart + 1;
		} else {
			lLineStart = widget.getLineAtOffset(widget.getCaretOffset());
		}
		return new Point(lLineStart, lLineCount);
	}

	private void formatSelectedBlock(final Styles.Style inStyle,
	        final int inLineStart, final int inLineCount) {
		widget.setLineBullet(inLineStart, inLineCount,
		        Styles.getBullet(inStyle.getStyleBit(), Styles.BULLET_WIDTH));
	}

	private void unformatSelectedBlock(final int inLineStart,
	        final int inLineCount) {
		final Bullet lOldBullet = widget.getLineBullet(inLineStart);
		widget.setLineBullet(inLineStart, inLineCount, null);
		// check for nested lists
		if (inLineStart == 0) {
			return;
		}
		final Bullet lBullet = widget.getLineBullet(inLineStart - 1);
		if (lBullet == null) {
			return;
		}
		// the bullet of the preceding line is different, therefore, apply it to
		// the actual lines
		if (lBullet != lOldBullet) {
			widget.setLineBullet(inLineStart, inLineCount, lBullet);
		}
	}

	private void formatWord(final Styles.Style inStyle) {
		final Point lSelection = selectWord(widget.getCaretOffset());
		formatSelection(inStyle, lSelection);
	}

	private Point selectWord(final int inPosition) {
		final StyledTextContent lContent = widget.getContent();
		final int lLineIndex = lContent.getLineAtOffset(inPosition);
		return selectWord(inPosition, lContent.getOffsetAtLine(lLineIndex),
		        lContent.getLine(lLineIndex).toCharArray());
	}

	/**
	 * Returns the coordinates (start/length) of the word the cursor actually is
	 * positioned.
	 *
	 * @param inPosition
	 *            int the cursor position in the text.
	 * @param inLineStart
	 *            int the position of the actual line start in the text.
	 * @param inLine
	 *            char[] the chars of the actual text line.
	 * @return Point the start/length of the selected word
	 */
	protected Point selectWord(final int inPosition, final int inLineStart,
	        final char[] inLine) {
		int lStart = 0;
		int lPosition = Math.min(inPosition - inLineStart, inLine.length - 1);
		while (lPosition > 0) {
			if (!Character.isJavaIdentifierPart(inLine[lPosition])) {
				lStart = lPosition + 1;
				break;
			}
			lPosition--;
		}
		lStart = Math.min(lStart, inLine.length - 1);

		int lEnd = inLine.length - 1;
		lPosition = inPosition - inLineStart;
		while (lPosition < inLine.length) {
			if (!Character.isJavaIdentifierPart(inLine[lPosition])) {
				lEnd = lPosition - 1;
				break;
			}
			lPosition++;
		}
		if (lStart > lEnd) {
			return new Point(inPosition, inLineStart);
		}
		return new Point(inLineStart + lStart, lEnd - lStart + 1);
	}

	private void unformatRange(final Styles.Style inStyle) {
		final StyleHelper lHelper = modifyRanges(widget.getStyleRanges(),
		        widget.getCaretOffset());
		lHelper.setModifiedStyle(widget, inStyle);
	}

	private void formatSelection(final Styles.Style inStyle,
	        final Point inSelection) {
		final int lStart = inSelection.x;
		final int lLength = inSelection.y;

		final StyleRange[] lRanges = widget.getStyleRanges(lStart, lLength);
		if (lRanges.length == 0) {
			final StyleRange lRange = createNewRange(inStyle, lStart, lLength);
			widget.setStyleRange(lRange);
		} else {
			final StyleRange[] lModified = modifyRanges(inStyle, lStart,
			        lLength, lRanges);
			widget.replaceStyleRanges(lStart, lLength, lModified);
		}
	}

	/**
	 * Modifies the font styles in the specified range by the specified font
	 * style.
	 *
	 * @param inStyle
	 *            StyleProperty the style to add
	 * @param inStart
	 *            int
	 * @param inLength
	 *            int
	 * @param inRanges
	 *            StyleRange[] the ranges to modify
	 * @return StyleRange[] a complete set of non overlapping ranges with the
	 *         specified style.
	 */
	protected StyleRange[] modifyRanges(final Styles.Style inStyle,
	        final int inStart, final int inLength,
	        final StyleRange[] inRanges) {
		final List<StyleRange> lRanges = new ArrayList<StyleRange>();
		int lPosition = inStart;
		for (int i = 0; i < inRanges.length; i++) {
			final StyleRange lRange = inRanges[i];
			final int lRangeStart = lRange.start;
			if (lPosition < lRangeStart) {
				lRanges.add(createNewRange(inStyle, lPosition,
				        lRangeStart - lPosition));
				lPosition = lRangeStart;
			}
			lRange.fontStyle |= inStyle.getStyleBit();
			lRange.underline |= inStyle.underline;
			lRanges.add(lRange);
			lPosition += lRange.length;
			// TODO: work around for bug in StyledText.getStyleRanges(start,
			// length) that returns incorrect style ranges with negative length
			// int lRangeLength = lRange.length;
			// if (lRangeLength > 0) {
			// ...
			// }
		}
		final int lEnd = inStart + inLength;
		if (lPosition < lEnd) {
			lRanges.add(createNewRange(inStyle, lPosition, lEnd - lPosition));
		}

		// nothing to merge
		if (lRanges.size() == 1) {
			return lRanges.toArray(new StyleRange[] {});
		}

		// merge similar ranges
		final Collection<StyleRange> outRanges = new ArrayList<StyleRange>();
		int i = 0;
		while (i < lRanges.size()) {
			final StyleRange lMergeIn = lRanges.get(i);
			int j = i;
			while (++j < lRanges.size()) {
				final StyleRange lTest = lRanges.get(j);
				if (!lTest.similarTo(lMergeIn)) {
					break;
				}
				lMergeIn.length += lTest.length;
			}
			outRanges.add(lMergeIn);
			i = j;
		}
		return outRanges.toArray(new StyleRange[] {});
	}

	private StyleRange createNewRange(final Styles.Style inStyle,
	        final int inStart, final int inLength) {
		final StyleRange outRange = new StyleRange();
		outRange.start = inStart;
		outRange.length = inLength;
		outRange.fontStyle = inStyle.getStyleBit();
		outRange.underline = inStyle.underline;
		return outRange;
	}

	private boolean hasSelection() {
		return widget.getSelectionCount() > 0;
	}

	private StyleHelper modifyRanges(final StyleRange[] inRanges,
	        final int inPosition) {
		final StyleHelper outHelper = new StyleHelper(inPosition);
		for (int i = 0; i < inRanges.length; i++) {
			outHelper.addRange(inRanges[i]);
		}
		return outHelper;
	}

	// ---

	private class StyleHelper {
		private final int position;
		private final Collection<StyleRange> ranges = new ArrayList<StyleRange>();
		private int start;
		private int end;

		public StyleHelper(final int inPosition) {
			position = inPosition;
			start = Integer.MAX_VALUE;
			end = 0;
		}

		public void addRange(final StyleRange inRange) {
			final int lStart = inRange.start;
			final int lEnd = lStart + inRange.length;
			if (lStart <= position && position <= lEnd) {
				start = Math.min(start, lStart);
				end = Math.max(end, lEnd);
				ranges.add(inRange);
			}
		}

		public int getStart() {
			return start;
		}

		public int getLength() {
			return end - start;
		}

		// public StyleRange[] getRanges() {
		// ArrayList<StyleRange> outRanges = new ArrayList<StyleRange>(ranges);
		// return outRanges.toArray(new StyleRange[] {});
		// }
		public void setModifiedStyle(final StyledText inWidget,
		        final Styles.Style inStyle) {
			boolean lClear = false;
			final Collection<StyleRange> lStyleRanges = new ArrayList<StyleRange>();
			for (final StyleRange lRange : ranges) {
				lRange.fontStyle ^= inStyle.getStyleBit();
				if (inStyle.underline) {
					lRange.underline = false;
				}
				// if Range of Style.normal, we don't add it to the list
				if (lRange.fontStyle == 0 && !lRange.underline) {
					lClear = true;
				} else {
					lStyleRanges.add(lRange);
				}
			}
			if (lClear) {
				inWidget.setStyleRanges(getStart(), getLength(), null, null);
			}
			if (lStyleRanges.size() == 0) {
				return;
			}

			final StyleRange[] lStyles = lStyleRanges
			        .toArray(new StyleRange[] {});
			final int[] lRanges = new int[lStyleRanges.size() * 2];
			for (int i = 0; i < lStyles.length; i++) {
				lRanges[i * 2] = lStyles[i].start;
				lRanges[i * 2 + 1] = lStyles[i].length;
			}
			inWidget.setStyleRanges(getStart(), getLength(), lRanges, lStyles);
		}
	}

}
