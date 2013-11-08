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
package org.elbe.relations.browser.finder.internal.dnd;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.ListItemRenderer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.browser.finder.internal.FinderPane.GalleryItemAdapter;
import org.elbe.relations.dnd.DropDataHelper;
import org.elbe.relations.dnd.DropDataHelper.IDropHandler;
import org.elbe.relations.models.IAssociationsModel;
import org.elbe.relations.models.PeripheralAssociationsModel;
import org.elbe.relations.services.IBrowserManager;

/**
 * Drop target adapter to enable item drops on finder items.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class FinderDropTargetListener extends DropTargetAdapter {
	private static final Color COLOR_BACK_DRAG_OVER = new Color(
			Display.getCurrent(), 203, 231, 229);

	private final Gallery gallery;
	private GalleryItem previousItem = null;
	private GalleryItem oldSelected = null;
	private Color bgColor;
	private final boolean isCenter;

	@Inject
	private Logger log;

	@Inject
	private IEclipseContext context;

	@Inject
	private IBrowserManager browserManager;

	/**
	 * FinderDropTargetListener constructor, must not be called by clients
	 * directly!
	 */
	public FinderDropTargetListener(final Gallery inGallery,
			final boolean inIsCenter) {
		gallery = inGallery;
		isCenter = inIsCenter;
	}

	/**
	 * Factory method to create instances of
	 * <code>FinderDropTargetListener</code>s.
	 * 
	 * @param inGallery
	 *            {@link Gallery}
	 * @param inIsCenter
	 *            boolean <code>true</code> if the item is displayed on the
	 *            center pane
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link FinderDropTargetListener}
	 */
	public static FinderDropTargetListener create(final Gallery inGallery,
			final boolean inIsCenter, final IEclipseContext inContext) {
		final FinderDropTargetListener out = new FinderDropTargetListener(
				inGallery, inIsCenter);
		ContextInjectionFactory.inject(out, inContext);
		return out;
	}

	@Override
	public void dragOver(final DropTargetEvent inEvent) {
		final GalleryItemAdapter lItem = getItemUnderCursor(inEvent.x,
				inEvent.y);
		// save the selected item when entering for that we can restore it when
		// leaving
		if (oldSelected == null) {
			if (gallery.getSelectionCount() > 0) {
				oldSelected = gallery.getSelection()[0];
			}
		}
		handleItemBackground(lItem);
		if (lItem == null) {
			inEvent.detail = DND.DROP_NONE;
		} else {
			inEvent.detail = DND.DROP_COPY;
			changeBGColor(COLOR_BACK_DRAG_OVER, lItem);
		}
	}

	private GalleryItemAdapter getItemUnderCursor(final int x, final int y) {
		Point lPoint = new Point(x, y);
		lPoint = gallery.toControl(lPoint);
		return (GalleryItemAdapter) gallery.getItem(lPoint);
	}

	@Override
	public void dragLeave(final DropTargetEvent inEvent) {
		if (previousItem != null && bgColor != null) {
			changeBGColor(bgColor, previousItem);
		}

		if (oldSelected != null) {
			gallery.setSelection(new GalleryItem[] { oldSelected });
		}
		oldSelected = null;
		previousItem = null;
	}

	@Override
	public void drop(final DropTargetEvent inEvent) {
		final IDropHandler lHandler = DropDataHelper.getDropHandler(inEvent);
		if (lHandler == null)
			return;
		try {
			lHandler.handleDrop(inEvent.data, getModel(inEvent.x, inEvent.y),
					context);
		}
		catch (final Exception exc) {
			log.error(exc, exc.getMessage());
		}
	}

	private IAssociationsModel getModel(final int x, final int y)
			throws Exception {
		if (isCenter) {
			return browserManager.getCenterModel();
		}
		final GalleryItemAdapter lItem = getItemUnderCursor(x, y);
		return PeripheralAssociationsModel.createExternalAssociationsModel(
				lItem.getRelationsItem(), context);
	}

	private void handleItemBackground(final GalleryItem inItem) {
		if (previousItem == null) {
			// old item is null
			if (inItem == null) {
				return;
			} else {
				// new item not null
				previousItem = inItem;
				bgColor = changeBGColor(COLOR_BACK_DRAG_OVER, previousItem);
			}
		} else {
			// old item not null
			if (previousItem.equals(inItem)) {
				return;
			} else {
				previousItem.setBackground(bgColor);
				changeBGColor(bgColor, previousItem);
				previousItem = inItem;
				if (inItem != null) {
					// new item not null
					bgColor = changeBGColor(COLOR_BACK_DRAG_OVER, previousItem);
				}
			}
		}
	}

	private Color changeBGColor(final Color inColor, final GalleryItem inItem) {
		final ListItemRenderer lRenderer = (ListItemRenderer) gallery
				.getItemRenderer();
		final Color outOldBG = lRenderer.getSelectionBackgroundColor();
		lRenderer.setSelectionBackgroundColor(inColor);
		gallery.setSelection(new GalleryItem[] { inItem });
		gallery.redraw(inItem);
		return outOldBG;
	}

}
