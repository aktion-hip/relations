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
package org.elbe.relations.internal.controls;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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

/**
 * View to display the bookmarks.
 * 
 * @author Luthiger
 */
public class BookmarksView extends AbstractToolPart {
	private final TableViewer bookmarksView;

	@Inject
	private BookmarksController bookmarksController;

	@Inject
	public BookmarksView(final Composite inParent) {
		bookmarksView = new TableViewer(inParent, SWT.H_SCROLL | SWT.V_SCROLL
		        | SWT.BORDER | SWT.MULTI);
		bookmarksView.setContentProvider(new ObservableListContentProvider());
		bookmarksView.setLabelProvider(getLabelProvider());
		bookmarksView.addDoubleClickListener(getDoubleClickListener());
		bookmarksView.addDragSupport(DND.DROP_COPY, getDragTypes(),
		        getDragSourceAdapter(bookmarksView));
		bookmarksView
		        .addSelectionChangedListener(getSelectionChangedListener());
	}

	@PostConstruct
	void initialize(final MPart inPart, final EMenuService inService) {
		afterInit(inPart, inService);
		bookmarksController.initialize(inPart);
	}

	@Focus
	void setFocus() {
		bookmarksView.setInput(bookmarksController.getBookmarks());
		final Table lTable = bookmarksView.getTable();
		lTable.setFocus();
		if (bookmarksView.getSelection().isEmpty()) {
			lTable.select(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.controls.AbstractToolPart#getControl()
	 */
	@Override
	protected Object getControl() {
		return bookmarksView.getControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.controls.AbstractToolPart#getContextMenuID()
	 */
	@Override
	protected String getContextMenuID() {
		return RelationsConstants.POPUP_TOOLS_BOOKMARKS;
	}

	@PersistState
	void persist() {
		bookmarksController.storeBookmarks();
	}

	/**
	 * @return boolean <code>true</code> if the view is filled and an element is
	 *         selected
	 */
	@Override
	public boolean hasSelection() {
		return !bookmarksView.getSelection().isEmpty();
	}

	/**
	 * Removes the selected item.
	 */
	public void removeSelected() {
		final Object lSelected = ((IStructuredSelection) bookmarksView
		        .getSelection()).getFirstElement();
		if (lSelected instanceof RetrievedItemWithIcon) {
			bookmarksView.remove(lSelected);
			bookmarksController.removeItem(lSelected);
		}
	}

}
