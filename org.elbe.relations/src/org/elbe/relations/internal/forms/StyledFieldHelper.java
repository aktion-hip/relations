/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.BrowserUtil;
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

    private static final int CTRL = Integer.valueOf(SWT.CTRL);

    private final MouseListener mouseClickListener = new MouseAdapter() {
        @Override
        public void mouseDown(final MouseEvent inEvent) {
            if (!textIsURL()) {
                return;
            }
            if ((inEvent.stateMask & CTRL) != 0) {
                BrowserUtil.startBrowser(getTextWidget().getText());
            }
        }
    };
    private final MouseTrackListener mouseTrackListener = new MouseTrackAdapter() {
        @Override
        public void mouseEnter(final MouseEvent inEvent) {
            if (!textIsURL()) {
                return;
            }
            if ((inEvent.stateMask & CTRL) != 0) {
                getTextWidget().setStyleRange(getClickRange());
                getTextWidget().setCursor(HAND_CURSOR);
                StyledFieldHelper.this.decoration.show();
            }
        }

        @Override
        public void mouseExit(final MouseEvent inEvent) {
            if (!textIsURL()) {
                return;
            }
            if ((inEvent.stateMask & CTRL) != 0) {
                getTextWidget().setStyleRange(getNormalRange());
                getTextWidget().setCursor(StyledFieldHelper.this.oldCursor);
                StyledFieldHelper.this.decoration.hide();
            }
        };

        @Override
        public void mouseHover(final MouseEvent inEvent) {
            if (!textIsURL()) {
                return;
            }
            if ((inEvent.stateMask & CTRL) != 0) {
                getTextWidget().setStyleRange(getClickRange());
                getTextWidget().setCursor(HAND_CURSOR);
            } else {
                getTextWidget().setStyleRange(getNormalRange());
                getTextWidget().setCursor(StyledFieldHelper.this.oldCursor);
            }
        };
    };
    private final MouseMoveListener mouseMoveListener = new MouseMoveListener() {
        @Override
        public void mouseMove(final MouseEvent inEvent) {
            if (!textIsURL()) {
                return;
            }
            if ((inEvent.stateMask & CTRL) != 0) {
                getTextWidget().setStyleRange(getClickRange());
                getTextWidget().setCursor(HAND_CURSOR);
            } else {
                getTextWidget().setStyleRange(getNormalRange());
                getTextWidget().setCursor(StyledFieldHelper.this.oldCursor);
            }
        }
    };

    private final FocusListener focusListener = new FocusAdapter() {
        @Override
        public void focusGained(final FocusEvent inEvent) {
            StyledFieldHelper.this.decoration.hide();
        }
    };

    private final StyledText journalText;
    private final Cursor oldCursor;
    private final ControlDecoration decoration;

    /**
     * Friendly constructor
     *
     * @param inText
     *            {@link StyledText} the widget this class is handling.
     * @param inLog
     *            {@link Logger}
     */
    StyledFieldHelper(final StyledText inText, final Logger inLog) {
        this.journalText = inText;
        this.decoration = FormUtility.addDecorationInfo(inText,
                RelationsMessages.getString("StyledFieldHelper.info")); //$NON-NLS-1$
        this.oldCursor = this.journalText.getCursor();
    }

    private StyledText getTextWidget() {
        return this.journalText;
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
        return BrowserUtil.textIsURL(getTextWidget().getText());
    }

    void addListeners() {
        getTextWidget().addMouseListener(this.mouseClickListener);
        getTextWidget().addMouseTrackListener(this.mouseTrackListener);
        getTextWidget().addMouseMoveListener(this.mouseMoveListener);
        getTextWidget().removeFocusListener(this.focusListener);
    }

    void removeListeners() {
        getTextWidget().removeMouseListener(this.mouseClickListener);
        getTextWidget().removeMouseTrackListener(this.mouseTrackListener);
        getTextWidget().removeMouseMoveListener(this.mouseMoveListener);
        getTextWidget().addFocusListener(this.focusListener);
    }

    void setEditable(final boolean inEditable) {
        getTextWidget().setEnabled(inEditable);
        getTextWidget().setBackground(inEditable ? WHITE : GRAY);
    }
}
