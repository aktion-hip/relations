/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.elbe.relations.utility.FontUtil;
import org.xml.sax.SAXException;

import jakarta.inject.Inject;

/** Wrapper for <code>StyledText</code> widget. This wrapper provides the actions used to manipulate the text styles.
 *
 * @author Luthiger
 * @see StyledText */
public class StyledTextComponent {

    private StyledText textWidget;
    private TextStyleProvider provider;
    private int fontSizeToUse = 0;

    private static final Color white = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    private static final Color gray = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    private boolean disposed;

    private final IEclipseContext context;

    /**
     * StyledTextComponent for DI. Should not be called by clients.
     */
    @Inject
    public StyledTextComponent(final IEclipseContext context) {
        this.context = context;
    }

    /** Factory method to create a <code>StyledTextComponent</code> with DI.
     *
     * @param container {@link Composite} the widget's parent container
     * @param context {@link IEclipseContext}
     * @return {@link StyledTextComponent} */
    public static StyledTextComponent createStyledText(final Composite container, final IEclipseContext context) {
        final StyledTextComponent styledText = ContextInjectionFactory.make(StyledTextComponent.class, context);
        styledText.initialize(container);
        return styledText;
    }

    private void initialize(final Composite container) {
        this.textWidget = new StyledText(container,
                SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);

        final TextContent content = new TextContent();
        this.textWidget.setContent(content);
        this.textWidget.setLayout(new GridLayout(1, true));
        this.textWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.textWidget.addExtendedModifyListener(event -> {
            if (StyledTextComponent.this.textWidget.getCharCount() == 0) {
                return;
            }
            final StyleRange range = StyledTextComponent.this.textWidget
                    .getStyleRangeAtOffset(Math.max(event.start - 1, 0));
            if (range != null) {
                final StyleRange newRange = new StyleRange(range.start, range.length + event.length, range.foreground,
                        range.background, range.fontStyle);
                StyledTextComponent.this.textWidget.replaceStyleRanges(newRange.start, newRange.length,
                        new StyleRange[] { newRange });
            }
            final int endPos = event.start + event.length;
            if (endPos - StyledTextComponent.this.textWidget.getOffsetAtLine(StyledTextComponent.this.textWidget
                    .getLineAtOffset(endPos)) == 0) {
                final int lineIndex = StyledTextComponent.this.textWidget.getLineAtOffset(event.start);
                if (lineIndex + 2 > StyledTextComponent.this.textWidget.getLineCount()) {
                    return;
                }
                // a new line has been entered, therefore, check whether we have to continue a list
                final Bullet bullet = StyledTextComponent.this.textWidget.getLineBullet(lineIndex);
                StyledTextComponent.this.textWidget.setLineBullet(lineIndex + 1, 1, bullet);
            }
        });
        this.textWidget.addPaintObjectListener(Styles.getPaintObjectListener(this.textWidget));
        this.textWidget.addVerifyKeyListener(event -> {
            if (event.keyCode == 9) { // TAB keyCode
                final StyledText widget = (StyledText) event.getSource();
                if (consumeTabKey(widget)) {
                    event.doit = false;
                    final TextStyler styler = new TextStyler(widget);
                    if ((event.stateMask & SWT.SHIFT) != 0) {
                        styler.dedentLines();
                        StyledTextComponent.this.provider.notifyPositionChange(widget.getCaretOffset());
                    } else {
                        styler.indentLines();
                    }
                }
            }
        });
        this.textWidget.addTraverseListener(event -> {
            if (event.detail == SWT.TRAVERSE_TAB_PREVIOUS && consumeTabKey((StyledText) event.getSource())) {
                event.doit = false;
            }
        });
        this.textWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(final MouseEvent event) {
                StyledTextComponent.this.provider.notifyPositionChange(StyledTextComponent.this.textWidget.getCaretOffset());
            }
        });
        this.textWidget.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent event) {
                StyledTextComponent.this.provider.notifyPositionChange(StyledTextComponent.this.textWidget.getCaretOffset());
            }
        });
        this.provider = ContextInjectionFactory.make(TextStyleProvider.class, this.context);
        this.provider.setWidget(this.textWidget);

        // font handling
        if (this.fontSizeToUse != 0) {
            trackFontSize(this.fontSizeToUse);
        }

        this.disposed = false;
    }

    /** Checks whether a (tab key) event should be consumed.
     *
     * @param widget StyledText
     * @return boolean <code>true</code> if the actual line has a bullet AND the cursor is at the beginning of the
     *         line. */
    private boolean consumeTabKey(final StyledText widget) {
        final int position = widget.getCaretOffset();
        final int line = widget.getLineAtOffset(position);
        return widget.getLineBullet(line) != null && position == widget.getOffsetAtLine(line);
    }

    /**
     * Returns the wrapped control, i.e. the <code>StyledText</code>.
     *
     * @return Control
     */
    public Control getControl() {
        return this.textWidget;
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
        this.textWidget.addFocusListener(inListener);
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
        this.textWidget.addPaintListener(inListener);
    }

    /**
     * Sets the layout data associated with the receiver to the argument.
     *
     * @param inLayoutData
     *            GridData the new layout data for the receiver.
     */
    public void setLayoutData(final GridData inLayoutData) {
        this.textWidget.setLayoutData(inLayoutData);
    }

    /**
     * Returns layout data which is associated with the receiver.
     *
     * @return GridData
     */
    public GridData getLayoutData() {
        return (GridData) this.textWidget.getLayoutData();
    }

    /**
     * Sets the widget content.
     *
     * @param inText
     *            String
     * @see StyledText#setText()
     */
    public void setText(final String inText) {
        this.textWidget.setText(inText);
    }

    /**
     * Returns a copy of the widget content.
     *
     * @return String
     * @see StyledText#getText()
     */
    public String getText() {
        return this.textWidget.getText();
    }

    /** Sets the tagged text (i.e. the text styles included in tags) to the widget for that it can be displayed styled.
     *
     * @param textTagged String the text including style information as tags.
     * @throws IOException
     * @throws SAXException */
    public void setTaggedText(final String textTagged) throws IOException, SAXException {
        StyleParser.getInstance().parseTagged(textTagged, this.textWidget);
    }

    /**
     * Returns the widget content including style information as tags.
     *
     * @return String the text including style information as tags.
     */
    public String getTaggedText() {
        return StyleParser.getInstance().getTagged(this.textWidget);
    }

    /** Returns the selected text.
     *
     * @return String selected text, or an empty String if there is no selection.
     * @see StyledText#getSelectionText() */
    public String getSelectionText() {
        return this.textWidget.getSelectionText();
    }

    /** Sets styles to be used for rendering the widget content. All styles in the widget will be replaced with the
     * given set of styles.
     *
     * @param ranges StyleRange[]
     * @see StyledText#setStyleRanges(StyleRange[]) */
    public void setStyleRanges(final StyleRange[] ranges) {
        this.textWidget.setStyleRanges(ranges);
    }

    /**
     * Returns the styles.
     *
     * @return StyleRange[]
     * @see StyledText#getStyleRanges()
     */
    public StyleRange[] getStyleRanges() {
        return this.textWidget.getStyleRanges();
    }

    /** Sets the receiver's pop up menu to the argument.
     *
     * @param menu Menu
     * @see Control#setMenu(Menu) */
    public void setMenu(final Menu menu) {
        this.textWidget.setMenu(menu);
    }

    /** Sets whether the widget content can be edited.
     *
     * @param editable boolean
     * @see StyledText#setEditable(boolean) */
    public void setEditable(final boolean editable) {
        this.textWidget.setEnabled(true);
        this.textWidget.setEditable(editable);
        this.textWidget.setBackground(editable ? white : gray);
    }

    /**
     * Disable the widget.
     */
    public void setDisabled() {
        this.textWidget.setEnabled(false);
        this.textWidget.setBackground(gray);
    }

    /** Sets a new font to render text with.
     *
     * @param font Font
     * @see StyledText#setFont(Font) */
    public void setFont(final Font font) {
        this.textWidget.setFont(font);
    }

    @Inject
    void trackFontSize(
            @Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_TEXT_FONT_SIZE) final int fontSize) {
        if (this.textWidget == null || this.textWidget.isDisposed()) {
            this.fontSizeToUse = fontSize;
        } else {
            final FontData data = this.textWidget.getFont().getFontData()[0];
            if (fontSize != data.getHeight()) {
                FontUtil.createOrGetFont(fontSize).ifPresent(f -> this.textWidget.setFont(f));
            }
        }
    }

    /**
     * Disposes of the operating system resources associated with the widget.
     */
    public void dispose() {
        this.textWidget.dispose();
        this.provider.setWidget(null);
        this.provider = null;
        this.textWidget = null;
        this.disposed = true;
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    /**
     * Returns this widget's state of line bullets.
     *
     * @return BulletsState
     */
    public BulletsState getBulletsState() {
        return new BulletsState(this.textWidget);
    }

    /** @param isFormStyle boolean <code>true</code> if the widget is on a form, <code>false</code> if it is on the
     *            inspector part. */
    public void setFormStyle(final boolean isFormStyle) {
        this.provider.setFormStyle(isFormStyle);
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

        /** @param isFormStyle boolean <code>true</code> if the widget is on a form, <code>false</code> if it is on the
         *            inspector part. */
        void setFormStyle(final boolean isFormStyle) {
            this.isFormStyle = isFormStyle;
        }

        void setWidget(final StyledText widget) {
            this.textWidget = widget;
        }

        @Inject
        @Optional
        public void setStyle(
                @UIEventTopic(RelationsConstants.TOPIC_STYLE_CHANGE_FORM) final Styles.StyleEvent event) {
            if (this.textWidget == null) {
                return;
            }
            if (!(this.isFormStyle ^ event.isFormStyle) && event.style.isToggle()) {
                final TextStyler styler = new TextStyler(this.textWidget);
                styler.format(event.style, event.isFormatNew);
                final int offset = this.textWidget.getCaretOffset();
                notifyPositionChange(Math.max(offset, offset - this.textWidget.getSelectionCount()));
            }
        }

        void fireStyleChange(final StyleSnapshot styleSnapshot) {
            if (styleSnapshot != null) {
                this.eventBroker.post(this.isFormStyle ? RelationsConstants.TOPIC_STYLE_CHANGED_FORM
                        : RelationsConstants.TOPIC_STYLE_CHANGED_INSPECTOR, styleSnapshot.createStyleParameter());
            }
        }

        public void notifyPositionChange(int offset) {
            final int length = this.textWidget.getCharCount();

            if (length == 0) {
                return;
            }
            if (offset >= length) {
                offset = length - 1;
            }
            final StyleSnapshot newStyle = new StyleSnapshot(this.textWidget, offset);
            if (this.styleSnapshot == null || !this.styleSnapshot.similarTo(newStyle)) {
                // currentOffset = inOffset;
                fireStyleChange(newStyle);
            }
            this.styleSnapshot = newStyle;
        }
    }

    /**
     * Helper class that takes a snapshot of this widget's styles at the
     * specified cursor position.
     */
    private static class StyleSnapshot {
        public final StyleRange styleRange;
        public final Bullet bullet;

        public StyleSnapshot(final StyledText widget, final int offset) {
            this.styleRange = widget.getStyleRangeAtOffset(offset);
            this.bullet = widget.getLineBullet(widget.getLineAtOffset(offset));
        }

        public boolean similarTo(final StyleSnapshot snapshot) {
            return similarRange(snapshot.styleRange) && similarBullet(snapshot.bullet);
        }

        private boolean similarRange(final StyleRange styleRange) {
            if (this.styleRange == null) {
                return styleRange == null;
            }
            return this.styleRange.similarTo(styleRange);
        }

        private boolean similarBullet(final Bullet bullet) {
            if (this.bullet == bullet) {
                return true;
            }
            if (bullet == null) {
                return false;
            }
            return this.bullet == null || this.bullet.type == bullet.type;
        }

        StyleParameter createStyleParameter() {
            return Styles.createStyleParameter(this.styleRange, this.bullet);
        }

        @Override
        public String toString() {
            final StringBuilder out = new StringBuilder();
            out.append(this.styleRange == null ? "Style: null" : this.styleRange.toString()); //$NON-NLS-1$
            out.append("; Bullet: "); //$NON-NLS-1$
            out.append(this.bullet == null ? "null" : this.bullet.toString()); //$NON-NLS-1$
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
        private final Set<LineBulletState> bullets = new HashSet<>();

        public BulletsState(final StyledText widget) {
            widget.getLineCount();
            for (int i = 0; i < widget.getLineCount(); i++) {
                final Bullet bullet = widget.getLineBullet(i);
                if (bullet != null) {
                    this.bullets.add(new LineBulletState(i, bullet.type, bullet.style.metrics.width));
                }
            }
        }

        public void undo(final StyledText widget) {
            final Map<String, Bullet> used = new HashMap<>();
            for (final LineBulletState bulletState : this.bullets) {
                Bullet bullet = used.get(bulletState.getKey());
                if (bullet == null) {
                    bullet = Styles.getBullet(bulletState.type, bulletState.width);
                    used.put(bulletState.getKey(), bullet);
                }
                widget.setLineBullet(bulletState.index, 1, bullet);
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + (this.bullets == null ? 0 : this.bullets.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BulletsState other = (BulletsState) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (this.bullets == null) {
                if (other.bullets != null) {
                    return false;
                }
            } else if (!this.bullets.equals(other.bullets)) {
                return false;
            }
            return true;
        }

        private StyledTextComponent getOuterType() {
            return StyledTextComponent.this;
        }

    }

    private record LineBulletState(int index, int type, int width) {
        public String getKey() {
            return String.format("%s/%s", this.type, this.width); //$NON-NLS-1$
        }
    }

}
