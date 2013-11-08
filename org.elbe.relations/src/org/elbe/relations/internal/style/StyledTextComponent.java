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

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.style.Styles.StyleParameter;
import org.xml.sax.SAXException;

/**
 * Wrapper for <code>StyledText</code> widget. This wrapper provides the actions
 * used to manipulate the text styles.
 * 
 * @author Luthiger Created on 07.09.2007
 * @see StyledText
 */
@SuppressWarnings("restriction")
public class StyledTextComponent {

	private StyledText textWidget;
	private TextContent content;
	private TextStyleProvider provider;
	private int fontSizeToUse = 0;

	private final static Color white = Display.getCurrent().getSystemColor(
			SWT.COLOR_WHITE);
	private final static Color gray = Display.getCurrent().getSystemColor(
			SWT.COLOR_WIDGET_BACKGROUND);
	private boolean disposed;

	@Inject
	private IEclipseContext context;

	/**
	 * StyledTextComponent for DI. Should not be called by clients.
	 */
	public StyledTextComponent() {
	}

	/**
	 * Factory method to create a <code>StyledTextComponent</code> with DI.
	 * 
	 * @param inContainer
	 *            {@link Composite} the widget's parent container
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link StyledTextComponent}
	 */
	public static StyledTextComponent createStyledText(
			final Composite inContainer, final IEclipseContext inContext) {
		final StyledTextComponent out = ContextInjectionFactory.make(
				StyledTextComponent.class, inContext);
		out.initialize(inContainer);
		return out;
	}

	private void initialize(final Composite inContainer) {
		textWidget = new StyledText(inContainer, SWT.BORDER | SWT.MULTI
				| SWT.WRAP | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);

		content = new TextContent();
		textWidget.setContent(content);
		textWidget.setLayout(new GridLayout(1, true));
		textWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		textWidget.addExtendedModifyListener(new ExtendedModifyListener() {
			@Override
			public void modifyText(final ExtendedModifyEvent inEvent) {
				if (textWidget.getCharCount() == 0)
					return;
				final StyleRange lRange = textWidget.getStyleRangeAtOffset(Math
						.max(inEvent.start - 1, 0));
				if (lRange != null) {
					final StyleRange lNew = new StyleRange(lRange.start,
							lRange.length + inEvent.length, lRange.foreground,
							lRange.background, lRange.fontStyle);
					textWidget.replaceStyleRanges(lNew.start, lNew.length,
							new StyleRange[] { lNew });
				}
				final int lEndPos = inEvent.start + inEvent.length;
				if (lEndPos
						- textWidget.getOffsetAtLine(textWidget
								.getLineAtOffset(lEndPos)) == 0) {
					final int lLineIndex = textWidget
							.getLineAtOffset(inEvent.start);
					if (lLineIndex + 2 > textWidget.getLineCount())
						return;
					// a new line has been entered, therefore, check whether we
					// have to continue a list
					final Bullet lBullet = textWidget.getLineBullet(lLineIndex);
					textWidget.setLineBullet(lLineIndex + 1, 1, lBullet);
				}
			}
		});
		textWidget.addPaintObjectListener(Styles
				.getPaintObjectListener(textWidget));
		textWidget.addVerifyKeyListener(new VerifyKeyListener() {
			@Override
			public void verifyKey(final VerifyEvent inEvent) {
				if (inEvent.keyCode == 9) { // TAB keyCode
					final StyledText lWidget = (StyledText) inEvent.getSource();
					if (consumeTabKey(lWidget)) {
						inEvent.doit = false;
						final TextStyler lStyler = new TextStyler(lWidget);
						if ((inEvent.stateMask & SWT.SHIFT) != 0) {
							lStyler.dedentLines();
							provider.notifyPositionChange(lWidget
									.getCaretOffset());
						} else {
							lStyler.indentLines();
						}
					}
				}
			}
		});
		textWidget.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(final TraverseEvent inEvent) {
				if (inEvent.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					if (consumeTabKey((StyledText) inEvent.getSource())) {
						inEvent.doit = false;
					}
				}
			}
		});
		textWidget.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent inEvent) {
				provider.notifyPositionChange(textWidget.getCaretOffset());
			}
		});
		textWidget.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent inEvent) {
				provider.notifyPositionChange(textWidget.getCaretOffset());
			}
		});
		provider = ContextInjectionFactory.make(TextStyleProvider.class,
				context);
		provider.setWidget(textWidget);

		// font handling
		if (fontSizeToUse != 0) {
			trackFontSize(fontSizeToUse);
		}

		disposed = false;
	}

	/**
	 * Checks whether a (tab key) event should be consumed.
	 * 
	 * @param inWidget
	 *            StyledText
	 * @return boolean <code>true</code> if the actual line has a bullet AND the
	 *         cursor is at the beginning of the line.
	 */
	private boolean consumeTabKey(final StyledText inWidget) {
		final int lPosition = inWidget.getCaretOffset();
		final int lLine = inWidget.getLineAtOffset(lPosition);
		if (inWidget.getLineBullet(lLine) != null
				&& lPosition == inWidget.getOffsetAtLine(lLine)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the wrapped control, i.e. the <code>StyledText</code>.
	 * 
	 * @return Control
	 */
	public Control getControl() {
		return textWidget;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control gains or loses focus, by sending it one of the messages
	 * defined in the FocusListener interface.
	 * 
	 * @param inListener
	 *            FocusListener
	 */
	public void addFocusListener(final FocusListener inListener) {
		textWidget.addFocusListener(inListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the receiver needs to be painted, by sending it one of the messages
	 * defined in the <code>PaintListener</code> interface.
	 * 
	 * @param inListener
	 *            the listener which should be notified
	 */
	public void addPaintListener(final PaintListener inListener) {
		textWidget.addPaintListener(inListener);
	}

	/**
	 * Sets the layout data associated with the receiver to the argument.
	 * 
	 * @param inLayoutData
	 *            GridData the new layout data for the receiver.
	 */
	public void setLayoutData(final GridData inLayoutData) {
		textWidget.setLayoutData(inLayoutData);
	}

	/**
	 * Returns layout data which is associated with the receiver.
	 * 
	 * @return GridData
	 */
	public GridData getLayoutData() {
		return (GridData) textWidget.getLayoutData();
	}

	/**
	 * Sets the widget content.
	 * 
	 * @param inText
	 *            String
	 * @see StyledText#setText()
	 */
	public void setText(final String inText) {
		textWidget.setText(inText);
	}

	/**
	 * Returns a copy of the widget content.
	 * 
	 * @return String
	 * @see StyledText#getText()
	 */
	public String getText() {
		return textWidget.getText();
	}

	/**
	 * Sets the tagged text (i.e. the text styles included in tags) to the
	 * widget for that it can be displayed styled.
	 * 
	 * @param inTextTagged
	 *            String the text including style information as tags.
	 * @throws IOException
	 * @throws SAXException
	 */
	public void setTaggedText(final String inTextTagged) throws IOException,
			SAXException {
		StyleParser.getInstance().parseTagged(inTextTagged, textWidget);
	}

	/**
	 * Returns the widget content including style information as tags.
	 * 
	 * @return String the text including style information as tags.
	 */
	public String getTaggedText() {
		return StyleParser.getInstance().getTagged(textWidget);
	}

	/**
	 * Returns the selected text.
	 * 
	 * @return String selected text, or an empty String if there is no
	 *         selection.
	 * @see StyledText#getSelectionText()
	 */
	public String getSelectionText() {
		return textWidget.getSelectionText();
	}

	/**
	 * Sets styles to be used for rendering the widget content. All styles in
	 * the widget will be replaced with the given set of styles.
	 * 
	 * @param inRanges
	 *            StyleRange[]
	 * @see StyledText#setStyleRanges(StyleRange[])
	 */
	public void setStyleRanges(final StyleRange[] inRanges) {
		textWidget.setStyleRanges(inRanges);
	}

	/**
	 * Returns the styles.
	 * 
	 * @return StyleRange[]
	 * @see StyledText#getStyleRanges()
	 */
	public StyleRange[] getStyleRanges() {
		return textWidget.getStyleRanges();
	}

	/**
	 * Sets the receiver's pop up menu to the argument.
	 * 
	 * @param inMenu
	 *            Menu
	 * @see Control#setMenu(Menu)
	 */
	public void setMenu(final Menu inMenu) {
		textWidget.setMenu(inMenu);
	}

	/**
	 * Sets whether the widget content can be edited.
	 * 
	 * @param inEditable
	 *            boolean
	 * @see StyledText#setEditable(boolean)
	 */
	public void setEditable(final boolean inEditable) {
		textWidget.setEnabled(true);
		textWidget.setEditable(inEditable);
		textWidget.setBackground(inEditable ? white : gray);
	}

	/**
	 * Disable the widget.
	 */
	public void setDisabled() {
		textWidget.setEnabled(false);
		textWidget.setBackground(gray);
	}

	/**
	 * Sets a new font to render text with.
	 * 
	 * @param inFont
	 *            Font
	 * @see StyledText#setFont(Font)
	 */
	public void setFont(final Font inFont) {
		textWidget.setFont(inFont);
	}

	@Inject
	void trackFontSize(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_TEXT_FONT_SIZE) final int inFontSize) {
		if (textWidget == null || textWidget.isDisposed()) {
			fontSizeToUse = inFontSize;
		} else {
			final FontData lData = textWidget.getFont().getFontData()[0];
			if (inFontSize != lData.getHeight()) {
				lData.setHeight(inFontSize);
				final Font lNewFont = new Font(Display.getCurrent(), lData);
				textWidget.setFont(lNewFont);
			}
		}
	}

	/**
	 * Disposes of the operating system resources associated with the widget.
	 */
	public void dispose() {
		textWidget.dispose();
		provider.setWidget(null);
		provider = null;
		textWidget = null;
		disposed = true;
	}

	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * Returns this widget's state of line bullets.
	 * 
	 * @return BulletsState
	 */
	public BulletsState getBulletsState() {
		return new BulletsState(textWidget);
	}

	/**
	 * @param inIsFormStyle
	 *            boolean <code>true</code> if the widget is on a form,
	 *            <code>false</code> if it is on the inspector part.
	 */
	public void setFormStyle(final boolean inIsFormStyle) {
		provider.setFormStyle(inIsFormStyle);
	}

	// ---

	/**
	 * This style widget's style provider.
	 */
	private static class TextStyleProvider {

		@Inject
		@Optional
		private IEventBroker eventBroker;

		private StyleSnapshot styleSnapshot;
		// private int currentOffset = 0;
		private StyledText textWidget;
		private boolean isFormStyle = false;

		/**
		 * Constructor needed for DI.
		 */
		@SuppressWarnings("unused")
		public TextStyleProvider() {
		}

		/**
		 * @param inIsFormStyle
		 *            boolean <code>true</code> if the widget is on a form,
		 *            <code>false</code> if it is on the inspector part.
		 */
		void setFormStyle(final boolean inIsFormStyle) {
			isFormStyle = inIsFormStyle;
		}

		void setWidget(final StyledText inWidget) {
			textWidget = inWidget;
		}

		@Inject
		@Optional
		public void setStyle(
				@EventTopic(RelationsConstants.TOPIC_STYLE_CHANGE_FORM) final Styles.StyleEvent inEvent) {
			if (textWidget == null) {
				return;
			}
			if (!(isFormStyle ^ inEvent.isFormStyle)) {
				if (inEvent.style.isToggle()) {
					final TextStyler lStyler = new TextStyler(textWidget);
					lStyler.format(inEvent.style, inEvent.isFormatNew);
					final int lOffset = textWidget.getCaretOffset();
					notifyPositionChange(Math.max(lOffset,
							lOffset - textWidget.getSelectionCount()));
				}
			}
		}

		void fireStyleChange(final StyleSnapshot inStyleSnapshot) {
			if (inStyleSnapshot != null) {
				eventBroker
						.post(isFormStyle ? RelationsConstants.TOPIC_STYLE_CHANGED_FORM
								: RelationsConstants.TOPIC_STYLE_CHANGED_INSPECTOR,
								inStyleSnapshot.createStyleParameter());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.elbe.relations.style.StyleProvider#notifyPositionChange()
		 */
		public void notifyPositionChange(int inOffset) {
			final int lLength = textWidget.getCharCount();

			if (lLength == 0)
				return;
			if (inOffset >= lLength) {
				inOffset = lLength - 1;
			}
			final StyleSnapshot lNew = new StyleSnapshot(textWidget, inOffset);
			if (styleSnapshot == null || !styleSnapshot.similarTo(lNew)) {
				// currentOffset = inOffset;
				fireStyleChange(lNew);
			}
			styleSnapshot = lNew;
		}
	}

	/**
	 * Helper class that takes a snapshot of this widget's styles at the
	 * specified cursor position.
	 */
	private static class StyleSnapshot {
		public StyleRange styleRange;
		public Bullet bullet;

		public StyleSnapshot(final StyledText inWidget, final int inOffset) {
			styleRange = inWidget.getStyleRangeAtOffset(inOffset);
			bullet = inWidget.getLineBullet(inWidget.getLineAtOffset(inOffset));
		}

		public boolean similarTo(final StyleSnapshot inSnapshot) {
			if (similarRange(inSnapshot.styleRange)
					&& similarBullet(inSnapshot.bullet))
				return true;
			return false;
		}

		private boolean similarRange(final StyleRange inStyleRange) {
			if (styleRange == null) {
				if (inStyleRange == null)
					return true;
				return false;
			}
			return styleRange.similarTo(inStyleRange);
		}

		private boolean similarBullet(final Bullet inBullet) {
			if (bullet == inBullet)
				return true;
			if (inBullet == null)
				return false;
			if (bullet == null) {
				if (inBullet != null)
					return false;
			} else if (bullet.type != inBullet.type)
				return false;
			return true;
		}

		StyleParameter createStyleParameter() {
			return Styles.createStyleParameter(styleRange, bullet);
		}

		@Override
		public String toString() {
			final StringBuilder out = new StringBuilder();
			out.append(styleRange == null ? "Style: null" : styleRange.toString()); //$NON-NLS-1$
			out.append("; Bullet: "); //$NON-NLS-1$
			out.append(bullet == null ? "null" : bullet.toString()); //$NON-NLS-1$
			return out.toString();
		}
	}

	/**
	 * Interface for classes to check the enablement state of the style
	 * provider.
	 */
	public interface EnablementChecker {
		Object checkEnablement();
	}

	/**
	 * Helper class that registeres the state of line bullets of this widget.
	 */
	public class BulletsState {
		private final Set<LineBulletState> bullets = new HashSet<LineBulletState>();

		public BulletsState(final StyledText inWidget) {
			inWidget.getLineCount();
			for (int i = 0; i < inWidget.getLineCount(); i++) {
				final Bullet lBullet = inWidget.getLineBullet(i);
				if (lBullet != null) {
					bullets.add(new LineBulletState(i, lBullet.type,
							lBullet.style.metrics.width));
				}
			}
		}

		@Override
		public boolean equals(final Object inObj) {
			if (this == inObj)
				return true;
			if (inObj == null)
				return false;
			if (getClass() != inObj.getClass())
				return false;
			final BulletsState lOther = (BulletsState) inObj;
			if (bullets == null) {
				if (lOther.bullets != null)
					return false;
			} else if (!bullets.equals(lOther.bullets))
				return false;
			return true;
		}

		public void undo(final StyledText inWidget) {
			final Hashtable<String, Bullet> lUsed = new Hashtable<String, Bullet>();
			for (final LineBulletState lBulletState : bullets) {
				Bullet lBullet = lUsed.get(lBulletState.getKey());
				if (lBullet == null) {
					lBullet = Styles.getBullet(lBulletState.type,
							lBulletState.width);
					lUsed.put(lBulletState.getKey(), lBullet);
				}
				inWidget.setLineBullet(lBulletState.index, 1, lBullet);
			}
		}
	}

	private class LineBulletState {
		private final int index;
		private final int type;
		private final int width;

		public LineBulletState(final int inIndex, final int inBulletType,
				final int inWidth) {
			index = inIndex;
			type = inBulletType;
			width = inWidth;
		}

		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + index;
			result = PRIME * result + type;
			result = PRIME * result + width;
			return result;
		}

		@Override
		public boolean equals(final Object inObj) {
			if (this == inObj)
				return true;
			if (inObj == null)
				return false;
			if (getClass() != inObj.getClass())
				return false;
			final LineBulletState lOther = (LineBulletState) inObj;
			if (index != lOther.index)
				return false;
			if (type != lOther.type)
				return false;
			if (width != lOther.width)
				return false;
			return true;
		}

		public String getKey() {
			return String.format("%s/%s", type, width); //$NON-NLS-1$
		}
	}

}
