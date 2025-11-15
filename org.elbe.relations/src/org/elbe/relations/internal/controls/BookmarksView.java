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
package org.elbe.relations.internal.controls;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.controller.BookmarksController;
import org.elbe.relations.search.RetrievedItemWithIcon;

import jakarta.inject.Inject;

/**
 * View to display the bookmarks.
 *
 * @author Luthiger
 */
public class BookmarksView extends AbstractToolPart {
    private final TableViewer bookmarksViewer;

    @Inject // NOSONAR
    private BookmarksController bookmarksController;

    @Inject
    public BookmarksView(final Composite parent) {
        this.bookmarksViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
        this.bookmarksViewer.setContentProvider(new ObservableListContentProvider<RetrievedItemWithIcon>());
        this.bookmarksViewer.setLabelProvider(getLabelProvider());
        this.bookmarksViewer.addDoubleClickListener(getDoubleClickListener());
        this.bookmarksViewer.addDragSupport(DND.DROP_COPY, getDragTypes(), getDragSourceAdapter(this.bookmarksViewer));
        this.bookmarksViewer.addSelectionChangedListener(getSelectionChangedListener());
    }

    // @PostConstruct
    @Inject
    void initialize(final MPart part, final EMenuService service) {
        afterInit(part, service);
        this.bookmarksController.initialize(part);
    }

    @Focus
    void setFocus() {
        this.bookmarksViewer.setInput(this.bookmarksController.getBookmarks());
        final Table table = this.bookmarksViewer.getTable();
        table.setFocus();
        if (this.bookmarksViewer.getSelection().isEmpty()) {
            table.select(0);
        }
    }

    @Override
    protected Object getControl() {
        return this.bookmarksViewer.getControl();
    }

    @Override
    protected String getContextMenuID() {
        return RelationsConstants.POPUP_TOOLS_BOOKMARKS;
    }

    @PersistState
    void persist() {
        this.bookmarksController.storeBookmarks();
    }

    /**
     * @return boolean <code>true</code> if the view is filled and an element is
     *         selected
     */
    @Override
    public boolean hasSelection() {
        return !this.bookmarksViewer.getSelection().isEmpty();
    }

    /**
     * Removes the selected item.
     */
    public void removeSelected() {
        final Object selected = ((IStructuredSelection) this.bookmarksViewer.getSelection()).getFirstElement();
        if (selected instanceof RetrievedItemWithIcon) {
            this.bookmarksViewer.remove(selected);
            this.bookmarksController.removeItem(selected);
        }
    }

}
