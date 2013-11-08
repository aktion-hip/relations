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
package org.elbe.relations.browser.finder.internal;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.nebula.widgets.gallery.AbstractGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.AbstractGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.ListItemRenderer;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.browser.finder.Constants;
import org.elbe.relations.browser.finder.internal.FinderBrowserPart.IBrowserCallback;
import org.elbe.relations.browser.finder.internal.dnd.DragAndDropHelper;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.utility.BrowserPopupStateController;
import org.elbe.relations.utility.BrowserPopupStateController.State;
import org.hip.kernel.exc.VException;

/**
 * Pane to display the list of items in a <code>Gallery</code> list.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class FinderPane {
	private static final long TIME_LONG = 0xFFFFFFFFL;
	private static final int ITEM_WIDTH_MIN = 170;
	private static final int ITEM_HEIGHT_MIN = 7; // 5
	private static final Display DISPLAY = Display.getCurrent();
	private static final Color COLOR_BACK_FOCUS_ON = DISPLAY
			.getSystemColor(SWT.COLOR_BLUE);
	private static final Color COLOR_BACK_FOCUS_OFF = DISPLAY
			.getSystemColor(SWT.COLOR_GRAY);
	private static final Color COLOR_TEXT_SELECTION_ON = DISPLAY
			.getSystemColor(SWT.COLOR_WHITE);
	private static final Color COLOR_TEXT_SELECTION_OFF = DISPLAY
			.getSystemColor(SWT.COLOR_BLACK);

	private final Gallery gallery;
	private GalleryItem lastSelected = null;
	private final DragSource dndSource;
	private final DropTarget dndTarget;
	private SearchListHelper items = new SearchListHelper();
	private final IBrowserCallback callback;

	@Inject
	private Logger log;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private IEventBroker eventBroker;

	/**
	 * FinderPane constructor.
	 * 
	 * @param inParent
	 *            {@link Composite}
	 * @param inService
	 *            {@link EMenuService}
	 * @param inApplication
	 *            {@link MApplication}
	 * @param inCallback
	 *            {@link IBrowserCallback} the callback to the bundle's browser
	 *            part
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @param inShowScrollbar
	 *            boolean <code>true</code> if the pane should display
	 *            scrollbars, <code>false</code> if not
	 */
	public FinderPane(final Composite inParent, final EMenuService inService,
			final MApplication inApplication,
			final IBrowserCallback inCallback, final IEclipseContext inContext,
			final boolean inShowScrollbar) {
		callback = inCallback;
		gallery = inShowScrollbar ? new Gallery(inParent, SWT.H_SCROLL
				| SWT.BORDER) : new NoScrollGallery(inParent);
		gallery.setGroupRenderer(createGroupRenderer());
		gallery.setItemRenderer(createItemRenderer());
		setFontSize(getPreferenceFontSize());
		inService.registerContextMenu(gallery, Constants.BROWSER_POPUP);

		gallery.addFocusListener(new PaneFocusListener());
		gallery.addKeyListener(new PaneKeyListener());
		gallery.addMouseListener(new PaneMouseAdapter(inApplication));

		dndSource = DragAndDropHelper.createDragSource(gallery,
				!inShowScrollbar);
		dndTarget = DragAndDropHelper.createDropTarget(gallery,
				!inShowScrollbar, inContext);
	}

	/**
	 * Places the cursor on the gallery's selected (or first) item.
	 */
	public void setFocus() {
		if (gallery.getItemCount() == 0) {
			return;
		}
		if (gallery.getSelectionCount() == 0) {
			lastSelected = gallery.getItem(0).getItem(0);
			gallery.setSelection(new GalleryItem[] { lastSelected });
		}
		gallery.setFocus();
	}

	public void setFocusEnforced() {
		gallery.setFocus();
	}

	private int getPreferenceFontSize() {
		final IEclipsePreferences lStore = InstanceScope.INSTANCE
				.getNode(RelationsConstants.PREFERENCE_NODE);
		final int outSize = lStore.getInt(FinderBrowserPart.class.getName(),
				RelationsConstants.DFT_TEXT_FONT_SIZE);
		return outSize;
	}

	private AbstractGalleryGroupRenderer createGroupRenderer() {
		final NoGroupRenderer outRenderer = new NoGroupRenderer();
		outRenderer.setExpanded(false);
		outRenderer.setAutoMargin(true);
		return outRenderer;
	}

	private AbstractGalleryItemRenderer createItemRenderer() {
		final ListItemRenderer outRenderer = new ListItemRenderer();
		outRenderer.setShowRoundedSelectionCorners(false);
		outRenderer.setSelectionForegroundColor(COLOR_TEXT_SELECTION_ON);
		return outRenderer;
	}

	/**
	 * Dispose this pane.
	 */
	public void dispose() {
		gallery.removeAll();
		gallery.dispose();
		if (dndSource != null) {
			dndSource.dispose();
		}
		if (dndTarget != null) {
			dndTarget.dispose();
		}
		items = null;
	}

	/**
	 * Update the (single item) list with the specified item.
	 * 
	 * @param inItem
	 *            {@link ItemAdapter} the new item to display in the list.
	 * @throws VException
	 */
	public void update(final ItemAdapter inItem) throws VException {
		final GalleryItem lRoot = prepareGallery();
		addItem(lRoot, inItem);
		gallery.redraw();
	}

	/**
	 * Update the displayed content with the specified list of items.
	 * 
	 * @param inItems
	 *            {@link List<ItemAdapter>} the new list to display.
	 * @throws VException
	 */
	public void update(final List<ItemAdapter> inItems) throws VException {
		final GalleryItem lRoot = prepareGallery();
		for (final ItemAdapter lItem : inItems) {
			addItem(lRoot, lItem);
		}
		gallery.redraw();
	}

	/**
	 * Clears the content and show an empty pane.
	 */
	public void clear() {
		gallery.removeAll();
		gallery.redraw();
		items = null;
	}

	private GalleryItem prepareGallery() {
		items = new SearchListHelper();
		gallery.removeAll();
		return new GalleryItem(gallery, SWT.NONE);
	}

	private void addItem(final GalleryItem inRootItem, final ItemAdapter inItem)
			throws VException {
		final GalleryItemAdapter lItem = new GalleryItemAdapter(inRootItem,
				inItem);
		lItem.setFont(gallery.getFont());
		items.add(lItem.getText());
	}

	/**
	 * Returns this gallery's selected item.
	 * 
	 * @return {@link GalleryItemAdapter} the selected item, may be
	 *         <code>null</code> if the gallery contains no items
	 */
	public GalleryItemAdapter getSelected() {
		if (gallery.getSelectionCount() == 0) {
			lastSelected = null;
			return null;
		}
		lastSelected = gallery.getSelection()[0];
		return (GalleryItemAdapter) lastSelected;
	}

	protected boolean checkSelctionChanged() {
		if (gallery.getSelectionCount() == 0) {
			return false;
		}
		return lastSelected != gallery.getSelection()[0];
	}

	/**
	 * Returns the specified item's representation in the gallery.
	 * 
	 * @param inSelected
	 *            {@link ItemAdapter}
	 * @return {@link GalleryItemAdapter} or <code>null</code> if specified item
	 *         is not element of the gallery.
	 * @throws VException
	 */
	public GalleryItemAdapter getSelected(final ItemAdapter inSelected)
			throws VException {
		final int lIndex = items.indexOf(inSelected.getTitle());
		if (lIndex == -1) {
			lastSelected = null;
			return null;
		}

		final GalleryItem lSelected = gallery.getItem(0).getItem(lIndex);
		gallery.setSelection(new GalleryItem[] { lSelected });
		lastSelected = lSelected;
		return (GalleryItemAdapter) lSelected;
	}

	/**
	 * Sets the gallery's font to the specified size.
	 * 
	 * @param inFontSize
	 *            int the font size (pt)
	 */
	public void setFontSize(final int inFontSize) {
		final FontData lData = gallery.getFont().getFontData()[0];
		lData.setHeight(inFontSize);
		((AbstractGridGroupRenderer) gallery.getGroupRenderer()).setItemSize(
				calculateWidth(inFontSize), calculateHeight(inFontSize));
		final Font lNewFont = new Font(Display.getCurrent(), lData);
		gallery.setFont(lNewFont);
		((ListItemRenderer) gallery.getItemRenderer()).setTextFont(lNewFont);
	}

	private int calculateWidth(final int inFontSize) {
		return ITEM_WIDTH_MIN + 7 * inFontSize;
	}

	private int calculateHeight(final int inFontSize) {
		return Math.round(ITEM_HEIGHT_MIN + 2 * inFontSize);
	}

	// --- private classes ---

	public static class GalleryItemAdapter extends GalleryItem {
		private final ItemAdapter adapted;

		GalleryItemAdapter(final GalleryItem inParent, final ItemAdapter inItem)
				throws VException {
			super(inParent, SWT.NONE);
			adapted = inItem;
			setText(inItem.getTitle());
			setImage(inItem.getImage());
		}

		public ItemAdapter getRelationsItem() {
			return adapted;
		}
	}

	private class PaneFocusListener implements FocusListener {
		@Override
		public void focusGained(final FocusEvent inEvent) {
			setSelectionColor(COLOR_TEXT_SELECTION_ON, COLOR_BACK_FOCUS_ON);
			final GalleryItemAdapter lSelected = getSelected();
			if (lSelected != null) {
				callback.selectionChange(lSelected.getRelationsItem());
			}
		}

		@Override
		public void focusLost(final FocusEvent inEvent) {
			setSelectionColor(COLOR_TEXT_SELECTION_OFF, COLOR_BACK_FOCUS_OFF);
		}

		private void setSelectionColor(final Color inTextColor,
				final Color inBgColor) {
			final ListItemRenderer lRenderer = (ListItemRenderer) gallery
					.getItemRenderer();
			lRenderer.setSelectionForegroundColor(inTextColor);
			lRenderer.setSelectionBackgroundColor(inBgColor);

			final GalleryItem[] lSelection = gallery.getSelection();
			if (lSelection.length > 0) {
				gallery.redraw(gallery.getSelection()[0]);
			}
		}
	}

	private class PaneKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(final KeyEvent inEvent) {
			switch (inEvent.keyCode) {
			case SWT.TAB:
				callback.focusPassOver(FinderPane.this);
				break;
			case SWT.CR:
				callback.centerSelected(FinderPane.this);
				break;
			default:
				if (items == null) {
					return;
				}
				// handle selection change by arrow up/down etc.
				if (checkSelctionChanged()) {
					handleSelection(getSelected());
				}
				// handle selection change by first chars
				final int lIndex = items.search(inEvent.character,
						getSelected(), inEvent.time & TIME_LONG);
				if (lIndex >= 0) {
					handleSelection(gallery.getItem(0).getItem(lIndex));
				}
				break;
			}
		}

		private void handleSelection(final GalleryItem inSelected) {
			gallery.setSelection(new GalleryItem[] { inSelected });
			callback.selectionChange(((GalleryItemAdapter) inSelected)
					.getRelationsItem());
		}
	}

	private class PaneMouseAdapter extends MouseAdapter {
		private final MApplication application;

		PaneMouseAdapter(final MApplication inApplication) {
			application = inApplication;
		}

		@Override
		public void mouseDown(final MouseEvent inEvent) {
			final GalleryItem lItem = gallery.getItem(new Point(inEvent.x,
					inEvent.y));
			if (inEvent.button == 3) {
				if (lItem == null) {
					BrowserPopupStateController.setState(State.DISABLED,
							application);
					return;
				} else {
					callback.focusRequest(FinderPane.this);
				}
			}
			// we ensure a proper item is selected
			if (lItem != null) {
				handleSelection(lItem);
			} else {
				if (lastSelected != null) {
					handleSelection(lastSelected);
				}
			}
		}

		@Override
		public void mouseDoubleClick(final MouseEvent inEvent) {
			callback.editSelected(FinderPane.this);
		}

		private void handleSelection(final GalleryItem inSelected) {
			gallery.setSelection(new GalleryItem[] { inSelected });
			callback.selectionChange(((GalleryItemAdapter) inSelected)
					.getRelationsItem());
		}
	}

	// ---

	private class NoScrollGallery extends Gallery {
		public NoScrollGallery(final Composite inParent) {
			super(inParent, SWT.BORDER);
		}

		@Override
		protected void updateScrollBarsProperties() {
			// we don't do any scrolling
			translate = 0;
		}
	}

}
