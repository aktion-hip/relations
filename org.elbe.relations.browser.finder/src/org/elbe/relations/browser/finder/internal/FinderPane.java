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
package org.elbe.relations.browser.finder.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.EMenuService;
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
import org.elbe.relations.utility.FontUtil;
import org.hip.kernel.exc.VException;

/**
 * Pane to display the list of items in a <code>Gallery</code> list.
 *
 * @author Luthiger
 */
public class FinderPane {
    private static final long TIME_LONG = 0xFFFFFFFFL;
    private static final int ITEM_WIDTH_MIN = 170;
    private static final int ITEM_HEIGHT_MIN = 5;
    private static final Display DISPLAY = Display.getCurrent();
    private static final Color COLOR_BACK_FOCUS_ON = DISPLAY.getSystemColor(SWT.COLOR_BLUE);
    private static final Color COLOR_BACK_FOCUS_OFF = DISPLAY.getSystemColor(SWT.COLOR_GRAY);
    private static final Color COLOR_TEXT_SELECTION_ON = DISPLAY.getSystemColor(SWT.COLOR_WHITE);
    private static final Color COLOR_TEXT_SELECTION_OFF = DISPLAY.getSystemColor(SWT.COLOR_BLACK);

    private final Gallery gallery;
    private GalleryItem lastSelected = null;
    private final DragSource dndSource;
    private final DropTarget dndTarget;
    private SearchListHelper items = new SearchListHelper();
    private final IBrowserCallback callback;

    /** FinderPane constructor.
     *
     * @param parent {@link Composite}
     * @param service {@link EMenuService}
     * @param application {@link MApplication}
     * @param callback {@link IBrowserCallback} the callback to the bundle's browser part
     * @param context {@link IEclipseContext}
     * @param showScrollbar boolean <code>true</code> if the pane should display scrollbars, <code>false</code> if
     *            not */
    public FinderPane(final Composite parent, final EMenuService service, final MApplication application,
            final IBrowserCallback callback, final IEclipseContext context, final boolean showScrollbar) {
        this.callback = callback;
        this.gallery = showScrollbar ? new Gallery(parent, SWT.H_SCROLL | SWT.BORDER) : new NoScrollGallery(parent);
        this.gallery.setGroupRenderer(createGroupRenderer());
        this.gallery.setItemRenderer(createItemRenderer());
        getPreferenceFont().ifPresent(f -> setFont(f));
        service.registerContextMenu(this.gallery, Constants.BROWSER_POPUP);

        this.gallery.addFocusListener(new PaneFocusListener());
        this.gallery.addKeyListener(new PaneKeyListener());
        this.gallery.addMouseListener(new PaneMouseAdapter(application));

        this.dndSource = DragAndDropHelper.createDragSource(this.gallery, !showScrollbar);
        this.dndTarget = DragAndDropHelper.createDropTarget(this.gallery, !showScrollbar, context);
    }

    /**
     * Places the cursor on the gallery's selected (or first) item.
     */
    public void setFocus() {
        if (this.items.isEmpty()) {
            return;
        }
        if (this.gallery.getSelectionCount() == 0) {
            this.lastSelected = this.gallery.getItem(0).getItem(0);
            this.gallery.setSelection(new GalleryItem[] { this.lastSelected });
        }
        this.gallery.setFocus();
    }

    public void setFocusEnforced() {
        this.gallery.setFocus();
    }

    private Optional<Font> getPreferenceFont() {
        final IEclipsePreferences store = InstanceScope.INSTANCE.getNode(RelationsConstants.PREFERENCE_NODE);
        return FontUtil.createOrGetFont(
                store.getInt(FinderBrowserPart.class.getName(), RelationsConstants.DFT_TEXT_FONT_SIZE));
    }

    private AbstractGalleryGroupRenderer createGroupRenderer() {
        final NoGroupRenderer outRenderer = new NoGroupRenderer();
        outRenderer.setExpanded(false);
        outRenderer.setAutoMargin(true);
        return outRenderer;
    }

    private AbstractGalleryItemRenderer createItemRenderer() {
        final ListItemRenderer renderer = new ListItemRenderer();
        renderer.setShowRoundedSelectionCorners(false);
        renderer.setSelectionForegroundColor(COLOR_TEXT_SELECTION_ON);
        return renderer;
    }

    /**
     * Dispose this pane.
     */
    public void dispose() {
        this.gallery.removeAll();
        this.gallery.dispose();
        if (this.dndSource != null) {
            this.dndSource.dispose();
        }
        if (this.dndTarget != null) {
            this.dndTarget.dispose();
        }
        this.items = null;
    }

    /** Update the (single item) list with the specified item.
     *
     * @param item {@link ItemAdapter} the new item to display in the list.
     * @throws VException */
    public void update(final ItemAdapter item) throws VException {
        final GalleryItem root = prepareGallery(this.gallery);
        addItem(root, item);
        this.gallery.redraw();
    }

    /** Update the displayed content with the specified list of items.
     *
     * @param items {@link List<ItemAdapter>} the new list to display.
     * @throws VException */
    public void update(final List<ItemAdapter> items) throws VException {
        final GalleryItem root = prepareGallery(this.gallery);
        for (final ItemAdapter item : items) {
            addItem(root, item);
        }
        this.gallery.redraw();
    }

    /**
     * Clears the content and show an empty pane.
     */
    public void clear() {
        this.gallery.removeAll();
        this.gallery.redraw();
        this.items = null;
    }

    private GalleryItem prepareGallery(final Gallery gallery) {
        this.items = new SearchListHelper();
        gallery.removeAll();
        return new GalleryItem(gallery, SWT.NONE);
    }

    private void addItem(final GalleryItem rootItem, final ItemAdapter item) throws VException {
        final GalleryItemAdapter adapted = new GalleryItemAdapter(rootItem, item);
        adapted.setFont(this.gallery.getFont());
        this.items.add(item);
    }

    /**
     * Returns this gallery's selected item.
     *
     * @return {@link GalleryItemAdapter} the selected item, may be
     *         <code>null</code> if the gallery contains no items
     */
    public GalleryItemAdapter getSelected() {
        if (this.gallery.getSelectionCount() == 0) {
            this.lastSelected = null;
            return null;
        }
        this.lastSelected = this.gallery.getSelection()[0];
        return (GalleryItemAdapter) this.lastSelected;
    }

    protected boolean checkSelctionChanged() {
        if (this.gallery.getSelectionCount() == 0) {
            return false;
        }
        return this.lastSelected != this.gallery.getSelection()[0];
    }

    /** Returns the specified item's representation in the gallery.
     *
     * @param selected {@link ItemAdapter}
     * @return {@link GalleryItemAdapter} or <code>null</code> if specified item is not element of the gallery. */
    public GalleryItemAdapter getSelected(final ItemAdapter selected) {
        final int index = this.items.indexOf(selected.getUniqueID());
        if (index == -1) {
            this.lastSelected = null;
            return null;
        }

        final GalleryItem selectedItem = this.gallery.getItem(0).getItem(index);
        this.gallery.setSelection(new GalleryItem[] { selectedItem });
        this.lastSelected = selectedItem;
        return (GalleryItemAdapter) selectedItem;
    }

    /** Sets the gallery's font to the font with new size.
     *
     * @param fontSize int the font size (pt) */
    public void setFont(final Font newFont) {
        final int fontSize = newFont.getFontData()[0].getHeight();
        final FontData data = this.gallery.getFont().getFontData()[0];
        data.setHeight(fontSize);
        ((AbstractGridGroupRenderer) this.gallery.getGroupRenderer()).setItemSize(calculateWidth(fontSize),
                calculateHeight(fontSize));
        this.gallery.setFont(newFont);
        // see @ListItemRenderer.draw() for a version not using *textFont* anymore
        ((ListItemRenderer) this.gallery.getItemRenderer()).setTextFont(newFont);
        // setFontToItems(this.gallery, newFont);
    }

    private void setFontToItems(final Gallery gallery, final Font font) {
        if (gallery.getItemCount() > 0) {
            final GalleryItem[] children = this.gallery.getItem(0).getItems();
            Arrays.stream(children).forEach(i -> i.setFont(font));
        }
    }

    private int calculateWidth(final int fontSize) {
        return ITEM_WIDTH_MIN + 7 * fontSize;
    }

    private int calculateHeight(final int fontSize) {
        return ITEM_HEIGHT_MIN + 3 * fontSize;
    }

    // --- private classes ---

    public static class GalleryItemAdapter extends GalleryItem {
        private final ItemAdapter item;

        GalleryItemAdapter(final GalleryItem parent, final ItemAdapter item) throws VException {
            super(parent, SWT.NONE);
            this.item = item;
            setText(item.getTitle());
            setImage(item.getImage());
        }

        public ItemAdapter getRelationsItem() {
            return this.item;
        }
    }

    private class PaneFocusListener implements FocusListener {
        @Override
        public void focusGained(final FocusEvent event) {
            setSelectionColor(COLOR_TEXT_SELECTION_ON, COLOR_BACK_FOCUS_ON);
            final GalleryItemAdapter selected = getSelected();
            if (selected != null) {
                FinderPane.this.callback.selectionChange(selected.getRelationsItem());
            }
        }

        @Override
        public void focusLost(final FocusEvent event) {
            setSelectionColor(COLOR_TEXT_SELECTION_OFF, COLOR_BACK_FOCUS_OFF);
        }

        private void setSelectionColor(final Color inTextColor, final Color inBgColor) {
            final ListItemRenderer lRenderer = (ListItemRenderer) FinderPane.this.gallery.getItemRenderer();
            lRenderer.setSelectionForegroundColor(inTextColor);
            lRenderer.setSelectionBackgroundColor(inBgColor);

            final GalleryItem[] selection = FinderPane.this.gallery.getSelection();
            if (selection.length > 0) {
                FinderPane.this.gallery.redraw(FinderPane.this.gallery.getSelection()[0]);
            }
        }
    }

    private class PaneKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(final KeyEvent event) {
            switch (event.keyCode) {
                case SWT.TAB:
                    FinderPane.this.callback.focusPassOver(FinderPane.this);
                    break;
                case SWT.CR:
                    FinderPane.this.callback.centerSelected(FinderPane.this);
                    break;
                default:
                    if (FinderPane.this.items == null) {
                        return;
                    }
                    // handle selection change by arrow up/down etc.
                    if (checkSelctionChanged()) {
                        handleSelection(getSelected());
                    }
                    // handle selection change by first chars
                    final int lIndex = FinderPane.this.items.search(event.character, getSelected(),
                            event.time & TIME_LONG);
                    if (lIndex >= 0) {
                        handleSelection(FinderPane.this.gallery.getItem(0).getItem(lIndex));
                    }
                    break;
            }
        }

        private void handleSelection(final GalleryItem selected) {
            FinderPane.this.gallery.setSelection(new GalleryItem[] { selected });
            FinderPane.this.callback.selectionChange(((GalleryItemAdapter) selected).getRelationsItem());
        }
    }

    private class PaneMouseAdapter extends MouseAdapter {
        private final MApplication application;

        PaneMouseAdapter(final MApplication application) {
            this.application = application;
        }

        @Override
        public void mouseDown(final MouseEvent event) {
            final GalleryItem item = FinderPane.this.gallery.getItem(new Point(event.x, event.y));
            if (event.button == 3) {
                if (item == null) {
                    BrowserPopupStateController.setState(State.DISABLED, this.application);
                    return;
                } else {
                    FinderPane.this.callback.focusRequest(FinderPane.this);
                }
            }
            // we ensure a proper item is selected
            if (item != null) {
                handleSelection(item);
            } else {
                if (FinderPane.this.lastSelected != null) {
                    handleSelection(FinderPane.this.lastSelected);
                }
            }
        }

        @Override
        public void mouseDoubleClick(final MouseEvent event) {
            FinderPane.this.callback.editSelected(FinderPane.this);
        }

        private void handleSelection(final GalleryItem selected) {
            FinderPane.this.gallery.setSelection(new GalleryItem[] { selected });
            FinderPane.this.callback.selectionChange(((GalleryItemAdapter) selected).getRelationsItem());
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
            this.translate = 0;
        }
    }

}
