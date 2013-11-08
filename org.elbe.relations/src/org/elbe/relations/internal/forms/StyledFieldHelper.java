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
package org.elbe.relations.internal.forms;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.BrowserSupport;
import org.elbe.relations.internal.utility.FormUtility;

/**
 * Helper class to handle listeners for <code>StyledText</code> widget used for
 * the journal text field.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
class StyledFieldHelper {
	private static final Display display = Display.getCurrent();
	private static final Color BLUE = display.getSystemColor(SWT.COLOR_BLUE);
	private static final Color BLACK = display.getSystemColor(SWT.COLOR_BLACK);
	private static final Color WHITE = display.getSystemColor(SWT.COLOR_WHITE);
	private static final Color GRAY = display
			.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	private static final Cursor HAND_CURSOR = new Cursor(display,
			SWT.CURSOR_HAND);

	private static final int CTRL = new Integer(SWT.CTRL);

	private final MouseListener mouseClickListener = new MouseAdapter() {
		@Override
		public void mouseDown(final MouseEvent inEvent) {
			if (!textIsURL())
				return;
			if ((inEvent.stateMask & CTRL) != 0) {
				try {
					new BrowserSupport().openURL(new URL(getTextWidget()
							.getText()));
				}
				catch (final PartInitException exc) {
					log.error(exc, exc.getMessage());
				}
				catch (final MalformedURLException exc) {
					log.error(exc, exc.getMessage());
				}
			}
		}
	};
	private final MouseTrackListener mouseTrackListener = new MouseTrackAdapter() {
		@Override
		public void mouseEnter(final MouseEvent inEvent) {
			if (!textIsURL())
				return;
			if ((inEvent.stateMask & CTRL) != 0) {
				getTextWidget().setStyleRange(getClickRange());
				getTextWidget().setCursor(HAND_CURSOR);
				decoration.show();
			}
		}

		@Override
		public void mouseExit(final MouseEvent inEvent) {
			if (!textIsURL())
				return;
			if ((inEvent.stateMask & CTRL) != 0) {
				getTextWidget().setStyleRange(getNormalRange());
				getTextWidget().setCursor(oldCursor);
				decoration.hide();
			}
		};

		@Override
		public void mouseHover(final MouseEvent inEvent) {
			if (!textIsURL())
				return;
			if ((inEvent.stateMask & CTRL) != 0) {
				getTextWidget().setStyleRange(getClickRange());
				getTextWidget().setCursor(HAND_CURSOR);
			} else {
				getTextWidget().setStyleRange(getNormalRange());
				getTextWidget().setCursor(oldCursor);
			}
		};
	};
	private final MouseMoveListener mouseMoveListener = new MouseMoveListener() {
		@Override
		public void mouseMove(final MouseEvent inEvent) {
			if (!textIsURL())
				return;
			if ((inEvent.stateMask & CTRL) != 0) {
				getTextWidget().setStyleRange(getClickRange());
				getTextWidget().setCursor(HAND_CURSOR);
			} else {
				getTextWidget().setStyleRange(getNormalRange());
				getTextWidget().setCursor(oldCursor);
			}
		}
	};

	private final FocusListener focusListener = new FocusAdapter() {
		@Override
		public void focusGained(final FocusEvent inEvent) {
			decoration.hide();
		}
	};

	private final StyledText journalText;
	private final Cursor oldCursor;
	private final ControlDecoration decoration;
	private final Logger log;

	/**
	 * Friendly constructor
	 * 
	 * @param inText
	 *            {@link StyledText} the widget this class is handling.
	 * @param inLog
	 *            {@link Logger}
	 */
	StyledFieldHelper(final StyledText inText, final Logger inLog) {
		journalText = inText;
		log = inLog;
		decoration = FormUtility.addDecorationInfo(inText,
				RelationsMessages.getString("StyledFieldHelper.info")); //$NON-NLS-1$
		oldCursor = journalText.getCursor();
	}

	private StyledText getTextWidget() {
		return journalText;
	}

	private StyleRange createRange() {
		final StyleRange outRange = new StyleRange();
		outRange.start = 0;
		outRange.length = getTextWidget().getText().length();
		return outRange;
	}

	private StyleRange getClickRange() {
		final StyleRange outRange = createRange();
		outRange.underline = true;
		outRange.foreground = BLUE;
		return outRange;
	}

	private StyleRange getNormalRange() {
		final StyleRange outRange = createRange();
		outRange.underline = false;
		outRange.foreground = BLACK;
		return outRange;
	}

	private boolean textIsURL() {
		final String lFieldContent = getTextWidget().getText();
		if (lFieldContent.startsWith("http://") || lFieldContent.startsWith("https://"))return true; //$NON-NLS-1$ //$NON-NLS-2$
		return false;
	}

	void addListeners() {
		getTextWidget().addMouseListener(mouseClickListener);
		getTextWidget().addMouseTrackListener(mouseTrackListener);
		getTextWidget().addMouseMoveListener(mouseMoveListener);
		getTextWidget().removeFocusListener(focusListener);
	}

	void removeListeners() {
		getTextWidget().removeMouseListener(mouseClickListener);
		getTextWidget().removeMouseTrackListener(mouseTrackListener);
		getTextWidget().removeMouseMoveListener(mouseMoveListener);
		getTextWidget().addFocusListener(focusListener);
	}

	void setEditable(final boolean inEditable) {
		getTextWidget().setEnabled(inEditable);
		getTextWidget().setBackground(inEditable ? WHITE : GRAY);
	}
}
