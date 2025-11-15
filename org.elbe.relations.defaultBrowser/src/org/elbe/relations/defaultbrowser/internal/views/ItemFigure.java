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
package org.elbe.relations.defaultbrowser.internal.views;

import java.util.Optional;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.defaultbrowser.internal.DefaultBrowserPart;
import org.elbe.relations.utility.FontUtil;

/** Figure to display an item.
 *
 * @author Benno Luthiger */
public class ItemFigure extends Figure {
    private static final int ICON_WIDTH = 16;
    private static final int LABEL_WIDTH = RelationsConstants.ITEM_WIDTH - ICON_WIDTH;
    private static final Color ACTIVE_COLOR = JFaceResources.getColorRegistry()
            .get(JFacePreferences.ACTIVE_HYPERLINK_COLOR);

    private final String title;
    private final Image image;
    private final Label label;
    private final RectangleFigure underline;
    private Cursor defaultCursor = null;
    private boolean isClickable = false;

    /** ItemFigure constructor.
     *
     * @param title String
     * @param image {@link Image} */
    public ItemFigure(final String title, final Image image) {
        super();
        this.title = title;
        this.image = image;
        this.label = createFigure(this.title);
        this.underline = createUnderline();
        this.label.add(this.underline);
        getPreferenceFont().ifPresent(this::setFont);
    }

    private Label createFigure(final String title) {
        setOpaque(true);
        setLayoutManager(new XYLayout());
        setBorder(new LineBorder(ColorConstants.black));
        setBackgroundColor(GraphicalViewerCreator.BG_COLOR);
        setSize(RelationsConstants.ITEM_WIDTH, RelationsConstants.ITEM_HEIGHT);

        final Label figLabel = new Label(title);
        figLabel.setOpaque(true);
        figLabel.setBackgroundColor(GraphicalViewerCreator.BG_COLOR);
        figLabel.setLabelAlignment(PositionConstants.LEFT);
        add(figLabel);
        setConstraint(figLabel, new Rectangle(ICON_WIDTH, 0, LABEL_WIDTH, RelationsConstants.ITEM_HEIGHT));

        final ImageFigure figure = new ImageFigure(this.image);
        add(figure);
        setConstraint(figure, new Rectangle(-1, -1, ICON_WIDTH, ICON_WIDTH));
        return figLabel;
    }

    /** @return {@link Font} may be <code>null</code> */
    private Optional<Font> getPreferenceFont() {
        final IEclipsePreferences store = InstanceScope.INSTANCE.getNode(RelationsConstants.PREFERENCE_NODE);
        return FontUtil.createOrGetFont(
                store.getInt(DefaultBrowserPart.class.getName(), RelationsConstants.DFT_TEXT_FONT_SIZE));
    }

    private RectangleFigure createUnderline() {
        final RectangleFigure outUnderline = new RectangleFigure();
        outUnderline.setForegroundColor(ACTIVE_COLOR);
        outUnderline.setVisible(false);
        return outUnderline;
    }

    /** Adjust the underline figure to the proper size and position of the text. This is needed for that the figure
     * looks like a hyperlink to signal that the figure is clickable.
     *
     * @param underline RectangleFigure the figure to adjust.
     * @param label Label the label containing the text to underline, used for calculating the underline's width.
     * @param font Font the font to adjust the underline for, used for calculating the underline's y position. */
    private void adjustUnderline(final RectangleFigure underline, final Label label, final Font font) {
        int width = label.getPreferredSize(LABEL_WIDTH, RelationsConstants.ITEM_HEIGHT).width;
        if (width >= LABEL_WIDTH) {
            width -= 5;
        }
        // y position is calculated from the figures y position
        final int yPosFigure = getBounds().y;
        final int yPos = (int) Math
                .round((double) (font.getFontData()[0].getHeight() + RelationsConstants.ITEM_HEIGHT) / 2)
                + (yPosFigure == 0 ? 1 : yPosFigure + 2);
        // x position is taken from the underline's old x position, width is
        // adjusted to the label width
        final Rectangle underlineBounds = new Rectangle(underline.getBounds().x, yPos, width, 1);
        underline.setBounds(underlineBounds);
    }

    /** Whether the figure is selected or not should be signaled by the figure's color.
     *
     * @param selected boolean */
    public void changeColor(final boolean selected) {
        setBackgroundColor(selected ? ColorConstants.white : GraphicalViewerCreator.BG_COLOR);
        this.label.setBackgroundColor(selected ? ColorConstants.white : GraphicalViewerCreator.BG_COLOR);
    }

    /** Sets this figure's clickable state. If the figure is clickable, it can be moved to the center.
     *
     * @param clickable boolean */
    public void setClickable(final boolean clickable) {
        if (clickable) {
            this.isClickable = true;
            this.label.setForegroundColor(ACTIVE_COLOR);
            this.underline.setVisible(true);
            this.defaultCursor = getCursor();
            setCursor(Cursors.HAND);
        } else {
            this.isClickable = false;
            this.label.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
            this.underline.setVisible(false);
            setCursor(this.defaultCursor);
        }
    }

    /**
     * Returns the figure's clickable state.
     *
     * @return boolean whether the figure is clickable (i.e. ready to move to
     *         the center by mouse click) or not.
     */
    public boolean isClickable() {
        return this.isClickable;
    }

    /** Sets the figures lable to the specified text.
     *
     * @param title String */
    public void setTitel(final String title) {
        this.label.setText(title);
    }

    @Override
    public void setFont(final Font font) {
        this.label.setFont(font);
        adjustUnderline(this.underline, this.label, font);
        super.setFont(font);
    }

}
