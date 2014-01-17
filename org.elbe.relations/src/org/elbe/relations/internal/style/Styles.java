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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsConstants;

/**
 * Helper class organizing the inline and list styles used in the styled text
 * widget.
 * 
 * @author Luthiger
 */
public final class Styles {
	public final static int BULLET_WIDTH = 23;
	public final static int INDENT = 17;

	/**
	 * The styles enumeration.
	 */
	public static enum Style {
		BOLD(RelationsConstants.STYLED_TEXT_ITEM_BOLD,
		        RelationsConstants.STYLED_TEXT_POPUP_BOLD,
		        RelationsConstants.STYLED_TEXT_COMMAND_BOLD, SWT.BOLD, true,
		        false, false), ITALIC(
		        RelationsConstants.STYLED_TEXT_ITEM_ITALIC,
		        RelationsConstants.STYLED_TEXT_POPUP_ITALIC,
		        RelationsConstants.STYLED_TEXT_COMMAND_ITALIC, SWT.ITALIC,
		        true, false, false), UNDERLINE(
		        RelationsConstants.STYLED_TEXT_ITEM_UNDERLINE,
		        RelationsConstants.STYLED_TEXT_POPUP_UNDERLINE,
		        RelationsConstants.STYLED_TEXT_COMMAND_UNDERLINE, SWT.NORMAL,
		        true, true, false), UNORDERED(
		        RelationsConstants.STYLED_TEXT_ITEM_LIST_BULLET,
		        RelationsConstants.STYLED_TEXT_POPUP_LIST_BULLET,
		        RelationsConstants.STYLED_TEXT_COMMAND_LIST_BULLET,
		        ST.BULLET_DOT, true, false, true), NUMBERED(
		        RelationsConstants.STYLED_TEXT_ITEM_LIST_NUMBERED,
		        RelationsConstants.STYLED_TEXT_POPUP_LIST_NUMBERED,
		        RelationsConstants.STYLED_TEXT_COMMAND_LIST_NUMBERED,
		        ST.BULLET_CUSTOM, true, false, true), UPPERCASE(
		        RelationsConstants.STYLED_TEXT_ITEM_LIST_UPPER,
		        RelationsConstants.STYLED_TEXT_POPUP_LIST_UPPER,
		        RelationsConstants.STYLED_TEXT_COMMAND_LIST_UPPER,
		        ST.BULLET_LETTER_UPPER, true, false, true), LOWERCASE(
		        RelationsConstants.STYLED_TEXT_ITEM_LIST_LOWER,
		        RelationsConstants.STYLED_TEXT_POPUP_LIST_LOWER,
		        RelationsConstants.STYLED_TEXT_COMMAND_LIST_LOWER,
		        ST.BULLET_LETTER_LOWER, true, false, true);

		private final String itemID;
		private final String popupID;
		private final String cmdID;
		private final int styleBit;
		private final boolean toggleStyle;
		public boolean underline;
		public boolean isBlock;

		Style(final String inItemID, final String inPopupID,
		        final String inCmdID, final int inBit,
		        final boolean inToggleStyle, final boolean inUnderline,
		        final boolean inBlockStyle) {
			itemID = inItemID;
			popupID = inPopupID;
			cmdID = inCmdID;
			styleBit = inBit;
			toggleStyle = inToggleStyle;
			underline = inUnderline;
			isBlock = inBlockStyle;
		}

		public int getStyleBit() {
			return styleBit;
		}

		public boolean isToggle() {
			return toggleStyle;
		}

		/**
		 * @return String the style's item ID
		 */
		public String getItemID() {
			return itemID;
		}

		/**
		 * @return String the style's associated popup item's ID
		 */
		public String getPopupID() {
			return popupID;
		}

		/**
		 * @param inCmdID
		 *            String
		 * @return boolean <code>true</code> if this <code>Style</code> has the
		 *         specified command ID
		 */
		boolean checkCmdID(final String inCmdID) {
			return cmdID.equals(inCmdID);
		}
	}

	/**
	 * Returns the <code>Style</code> appropriate for the command with the
	 * specified ID.
	 * 
	 * @param inCmdID
	 *            String
	 * @return {@link Style}
	 */
	public static Style getStyle(final String inCmdID) {
		for (final Style lStyle : Style.values()) {
			if (lStyle.checkCmdID(inCmdID)) {
				return lStyle;
			}
		}
		return Style.BOLD;
	}

	/**
	 * Factory method: creates a style parameter object from the specified
	 * inline and list styles. This parameter object is used when the cursor is
	 * moved in the styled text widget and the style buttons should reflect the
	 * style states at the actual cursor position.
	 * 
	 * @param inStyleRange
	 *            {@link StyleRange}
	 * @param inBullet
	 *            {@link Bullet}
	 * 
	 * @return {@link StyleParameter} indicating which styles at the actual
	 *         cursor position are active
	 */
	public static StyleParameter createStyleParameter(
	        final StyleRange inStyleRange, final Bullet inBullet) {
		return new StyleParameter(inStyleRange, inBullet);
	}

	/**
	 * Factory method to create a style information parameter object. This
	 * parameter object is used when the user clicks a style button and the
	 * (un)selected style should be applied on the text portion in the styled
	 * text widget.
	 * 
	 * @param inStyle
	 *            {@link Style}
	 * @param inFormStyle
	 *            boolean <code>true</code> if this event addresses style
	 *            changes on a form, <code>false</code> for inspector
	 * @param inFormatApply
	 *            boolean if <code>true</code> the style should be applied, else
	 *            to remove it
	 * @return {@link StyleEvent}
	 */
	public static StyleEvent createStyleEvent(final Style inStyle,
	        final boolean inFormStyle, final boolean inFormatApply) {
		return new StyleEvent(inStyle, inFormStyle, inFormatApply);
	}

	/**
	 * Convenience method, returns the appropriate <code>Bullet</code> instance
	 * for the specified bullet style.
	 * 
	 * @param inBulletStyle
	 *            bullet style
	 * @param inWidth
	 *            int
	 * @return Bullet
	 */
	public static Bullet getBullet(final int inBulletStyle, final int inWidth) {
		int lOr = 0;
		String lBulletText = ""; //$NON-NLS-1$
		if ((inBulletStyle & (ST.BULLET_LETTER_UPPER | ST.BULLET_LETTER_LOWER)) != 0) {
			lOr = ST.BULLET_TEXT;
			lBulletText = ")"; //$NON-NLS-1$
		}
		final Bullet outBullet = new Bullet(inBulletStyle | lOr,
		        getDefaultRange(inWidth));
		outBullet.text = lBulletText;
		return outBullet;
	}

	/**
	 * Convenience method, returns a default <code>StyleRange</code> with the
	 * specified width that can be used to format line bullets.
	 * 
	 * @param inWidth
	 *            int
	 * @return StyleRange
	 */
	public static StyleRange getDefaultRange(final int inWidth) {
		final StyleRange outRange = new StyleRange();
		outRange.start = 0;
		outRange.length = 0;
		outRange.metrics = new GlyphMetrics(0, 0, inWidth);
		return outRange;
	}

	/**
	 * Convenience method
	 * 
	 * @param inWidget
	 *            {@link StyledText}
	 * @return {@link PaintObjectListener}
	 */
	public static PaintObjectListener getPaintObjectListener(
	        final StyledText inWidget) {
		return new PaintObjectListener() {
			@Override
			public void paintObject(final PaintObjectEvent inEvent) {
				final Display lDisplay = inEvent.display;
				final StyleRange lStyle = inEvent.style;
				final int lPosition = inEvent.x + lStyle.metrics.width
				        - BULLET_WIDTH + 2;
				Font lFont = lStyle.font;
				if (lFont == null)
					lFont = inWidget.getFont();
				final TextLayout lLayout = new TextLayout(lDisplay);
				lLayout.setAscent(inEvent.ascent);
				lLayout.setDescent(inEvent.descent);
				lLayout.setFont(lFont);
				lLayout.setText(String.format("%s.", inEvent.bulletIndex + 1)); //$NON-NLS-1$
				lLayout.draw(inEvent.gc, lPosition, inEvent.y);
				lLayout.dispose();
			}
		};
	}

	// ---

	/**
	 * Utility class for the styled text widget for that the toolbar
	 * contribution items can reflect their toggle state according to the style
	 * at the actual cursor position in the text widget.
	 * 
	 * @author Luthiger
	 */
	public static class StyleParameter {
		private final Map<Style, Boolean> toggleStates;

		/**
		 * @param inStyleRange
		 * @param inBullet
		 */
		private StyleParameter(final StyleRange inStyleRange,
		        final Bullet inBullet) {
			toggleStates = new HashMap<Styles.Style, Boolean>(7);

			// font styles
			if (inStyleRange == null) {
				toggleStates.put(Style.BOLD, Boolean.FALSE);
				toggleStates.put(Style.ITALIC, Boolean.FALSE);
				toggleStates.put(Style.UNDERLINE, Boolean.FALSE);
			} else {
				toggleStates.put(Style.BOLD, Boolean
				        .valueOf((inStyleRange.fontStyle & SWT.BOLD) != 0));
				toggleStates.put(Style.ITALIC, Boolean
				        .valueOf((inStyleRange.fontStyle & SWT.ITALIC) != 0));
				toggleStates.put(Style.UNDERLINE,
				        Boolean.valueOf(inStyleRange.underline));
			}

			// list styles
			if (inBullet == null) {
				toggleStates.put(Style.UNORDERED, Boolean.FALSE);
				toggleStates.put(Style.NUMBERED, Boolean.FALSE);
				toggleStates.put(Style.UPPERCASE, Boolean.FALSE);
				toggleStates.put(Style.LOWERCASE, Boolean.FALSE);
			} else {
				toggleStates.put(Style.UNORDERED,
				        Boolean.valueOf((inBullet.type & ST.BULLET_DOT) != 0));
				toggleStates.put(Style.NUMBERED, Boolean
				        .valueOf((inBullet.type & ST.BULLET_CUSTOM) != 0));
				toggleStates
				        .put(Style.UPPERCASE,
				                Boolean.valueOf((inBullet.type & ST.BULLET_LETTER_UPPER) != 0));
				toggleStates
				        .put(Style.LOWERCASE,
				                Boolean.valueOf((inBullet.type & ST.BULLET_LETTER_LOWER) != 0));
			}
		}

		/**
		 * Returns the style's toggle state at the actual cursor position.
		 * 
		 * @param inStyle
		 *            {@link Style}
		 * @return Boolean <code>true</code> if the specified style is toggeled,
		 *         i.e. active at the actual cursor position
		 */
		public Boolean getIsToggeled(final Style inStyle) {
			final Boolean outState = toggleStates.get(inStyle);
			return outState == null ? Boolean.FALSE : outState;
		}
	}

	/**
	 * Parameter object to transport style change information.
	 */
	public static class StyleEvent {
		public final Style style;
		public final boolean isFormStyle;
		public final boolean isFormatNew;

		private StyleEvent(final Style inStyle, final boolean inFormStyle,
		        final boolean inFormatNew) {
			style = inStyle;
			isFormStyle = inFormStyle;
			isFormatNew = inFormatNew;
		}
	}

}
