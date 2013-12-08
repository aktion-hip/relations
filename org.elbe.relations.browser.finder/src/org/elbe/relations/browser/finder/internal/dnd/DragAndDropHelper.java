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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryDragSourceEffect;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.elbe.relations.browser.finder.internal.FinderPane.GalleryItemAdapter;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.dnd.DropDataHelper;

/**
 * Helper class to handle this browser view's drag and drop.
 * 
 * @author Luthiger Created on 17.12.2009
 */
public final class DragAndDropHelper {
	private static final int OPERATIONS = DND.DROP_MOVE | DND.DROP_COPY;

	private DragAndDropHelper() {
		// prevent class instantiation
	}

	/**
	 * Create drag source to remove related items from the center item or to
	 * relate one related item with another (by dropping the first on the
	 * later).
	 * 
	 * @param inGallery
	 *            Gallery the gallery widget.
	 * @param inIsCenter
	 *            boolean <code>true</code> if this gallery displays the center
	 *            item, <code>false</code> if it displays the related items.
	 * @return {@link DragSource}
	 */
	public static DragSource createDragSource(final Gallery inGallery,
	        final boolean inIsCenter) {
		final DragSource outDragSource = new DragSource(inGallery, OPERATIONS);
		outDragSource.setTransfer(DropDataHelper.DRAG_TYPES);

		outDragSource.addDragListener(new FinderDragSourceAdapter(inGallery,
		        inIsCenter));
		outDragSource
		        .setDragSourceEffect(new GalleryDragSourceEffect(inGallery));

		return outDragSource;
	}

	/**
	 * Create drop target to add relations.
	 * 
	 * @param inGallery
	 *            Gallery the gallery widget.
	 * @param inIsCenter
	 *            boolean <code>true</code> if this gallery displays the center
	 *            item, <code>false</code> if it displays the related items.
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link DropTarget}
	 */
	public static DropTarget createDropTarget(final Gallery inGallery,
	        final boolean inIsCenter, final IEclipseContext inContext) {
		final DropTarget outDropTarget = new DropTarget(inGallery, OPERATIONS);
		outDropTarget.setTransfer(DropDataHelper.DROP_TYPES);
		outDropTarget.addDropListener(FinderDropTargetListener.create(
		        inGallery, inIsCenter, inContext));
		return outDropTarget;
	}

	// --- inner classes ---

	private static class FinderDragSourceAdapter extends DragSourceAdapter {
		private final Gallery gallery;
		private final boolean isCenter;

		FinderDragSourceAdapter(final Gallery inGallery,
		        final boolean inIsCenter) {
			gallery = inGallery;
			isCenter = inIsCenter;
		}

		@Override
		public void dragStart(final DragSourceEvent inEvent) {
			if (isCenter) {
				inEvent.doit = false;
				return;
			}
			if (gallery.getSelectionCount() == 0) {
				inEvent.doit = false;
			}
		}

		@Override
		public void dragSetData(final DragSourceEvent inEvent) {
			final GalleryItemAdapter lSelection = (GalleryItemAdapter) gallery
			        .getSelection()[0];
			inEvent.data = new UniqueID[] { lSelection.getRelationsItem()
			        .getUniqueID() };
		}
	}

}
