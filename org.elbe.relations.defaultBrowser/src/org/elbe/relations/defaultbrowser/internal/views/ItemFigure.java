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
package org.elbe.relations.defaultbrowser.internal.views;

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
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.defaultbrowser.internal.DefaultBrowserPart;

/**
 * Figure to display an item.
 *
 * @author Benno Luthiger Created on 17.12.2005
 */
public class ItemFigure extends Figure {
	private final static int ICON_WIDTH = 16;
	private final static int LABEL_WIDTH = RelationsConstants.ITEM_WIDTH - ICON_WIDTH;
	private final static Color ACTIVE_COLOR = JFaceResources.getColorRegistry()
			.get(JFacePreferences.ACTIVE_HYPERLINK_COLOR);

	private final String title;
	private final Image image;
	private final Label label;
	private final RectangleFigure underline;
	private Cursor defaultCursor = null;
	private boolean isClickable = false;

	/**
	 * @param inTitle
	 *            String
	 * @param inIconType
	 *            int
	 */
	public ItemFigure(final String inTitle, final Image inImage) {
		super();
		title = inTitle;
		image = inImage;
		label = createFigure(title);
		underline = createUnderline();
		label.add(underline);
		setFont(getPreferenceFont());
	}

	private Label createFigure(final String inTitle) {
		setOpaque(true);
		setLayoutManager(new XYLayout());
		setBorder(new LineBorder(ColorConstants.black));
		setBackgroundColor(GraphicalViewerCreator.BG_COLOR);
		setSize(RelationsConstants.ITEM_WIDTH, RelationsConstants.ITEM_HEIGHT);

		final Label outLabel = new Label(inTitle);
		outLabel.setOpaque(true);
		outLabel.setBackgroundColor(GraphicalViewerCreator.BG_COLOR);
		outLabel.setLabelAlignment(PositionConstants.LEFT);
		add(outLabel);
		setConstraint(outLabel, new Rectangle(ICON_WIDTH, 0, LABEL_WIDTH, RelationsConstants.ITEM_HEIGHT));

		final ImageFigure lImage = new ImageFigure(image);
		add(lImage);
		setConstraint(lImage, new Rectangle(-1, -1, ICON_WIDTH, ICON_WIDTH));
		return outLabel;
	}

	private Font getPreferenceFont() {
		final Font lFont = JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
		final FontData lData = lFont.getFontData()[0];

		final IEclipsePreferences lStore = InstanceScope.INSTANCE.getNode(RelationsConstants.PREFERENCE_NODE);
		final int lSize = lStore.getInt(DefaultBrowserPart.class.getName(), RelationsConstants.DFT_TEXT_FONT_SIZE);
		lData.setHeight(lSize);

		final Font outFont = new Font(Display.getCurrent(), lData);
		return outFont;
	}

	private RectangleFigure createUnderline() {
		final RectangleFigure outUnderline = new RectangleFigure();
		outUnderline.setForegroundColor(ACTIVE_COLOR);
		outUnderline.setVisible(false);
		return outUnderline;
	}

	/**
	 * Adjust the underline figure to the proper size and position of the text.
	 * This is needed for that the figure looks like a hyperlink to signal that
	 * the figure is clickable.
	 *
	 * @param inUnderline
	 *            RectangleFigure the figure to adjust.
	 * @param inLabel
	 *            Label the label containing the text to underline, used for
	 *            calculating the underline's width.
	 * @param inFont
	 *            Font the font to adjust the underline for, used for
	 *            calculating the underline's y position.
	 */
	private void adjustUnderline(final RectangleFigure inUnderline, final Label inLabel, final Font inFont) {
		int lWidth = inLabel.getPreferredSize(LABEL_WIDTH, RelationsConstants.ITEM_HEIGHT).width;
		if (lWidth >= LABEL_WIDTH) {
			lWidth -= 5;
		}
		// y position is calculated from the figures y position
		final int yPosFigure = getBounds().y;
		final int yPos = (int) (Math
				.round((double) (inFont.getFontData()[0].getHeight() + RelationsConstants.ITEM_HEIGHT) / 2))
				+ (yPosFigure == 0 ? 1 : yPosFigure + 2);
		// x position is taken from the underline's old x position, width is
		// adjusted to the label width
		final Rectangle lUnderlineBounds = new Rectangle(inUnderline.getBounds().x, yPos, lWidth, 1);
		inUnderline.setBounds(lUnderlineBounds);
	}

	/**
	 * Whether the figure is selected or not should be signaled by the figure's
	 * color.
	 *
	 * @param inSelected
	 *            boolean
	 */
	public void changeColor(final boolean inSelected) {
		setBackgroundColor((inSelected) ? ColorConstants.white : GraphicalViewerCreator.BG_COLOR);
		label.setBackgroundColor((inSelected) ? ColorConstants.white : GraphicalViewerCreator.BG_COLOR);
	}

	/**
	 * Sets this figure's clickable state. If the figure is clickable, it can be
	 * moved to the center.
	 *
	 * @param inClickable
	 *            boolean
	 */
	public void setClickable(final boolean inClickable) {
		if (inClickable) {
			isClickable = true;
			label.setForegroundColor(ACTIVE_COLOR);
			underline.setVisible(true);
			defaultCursor = getCursor();
			setCursor(Cursors.HAND);
		} else {
			isClickable = false;
			label.setForegroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			underline.setVisible(false);
			setCursor(defaultCursor);
		}
	}

	/**
	 * Returns the figure's clickable state.
	 *
	 * @return boolean whether the figure is clickable (i.e. ready to move to
	 *         the center by mouse click) or not.
	 */
	public boolean isClickable() {
		return isClickable;
	}

	/**
	 * Sets the figures lable to the specified text.
	 *
	 * @param inTitle
	 *            String
	 */
	public void setTitel(final String inTitle) {
		label.setText(inTitle);
	}

	@Override
	public void setFont(final Font inFont) {
		label.setFont(inFont);
		adjustUnderline(underline, label, inFont);
		super.setFont(inFont);
	}

}
