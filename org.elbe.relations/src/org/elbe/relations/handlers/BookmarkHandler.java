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
package org.elbe.relations.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.controller.BookmarksController;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.search.RetrievedItemWithIcon;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.exc.VException;

/**
 * Handler to bookmark the selected item.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class BookmarkHandler {
	private final static String SUCCESS_MSG = RelationsMessages
			.getString("BookmarkHandler.msg.success"); //$NON-NLS-1$

	@Inject
	private BookmarksController bookmarksController;

	@Inject
	private IBrowserManager browserManager;

	@Inject
	private Logger log;

	@Execute
	void createBookmark(final IEclipseContext inContext,
			final RelationsStatusLineManager inStatusLine) {
		final ItemAdapter lSelected = browserManager.getSelectedModel();
		if (lSelected == null) {
			return;
		}

		try {
			final String lTitle = lSelected.getTitle();
			bookmarksController.addItem(new RetrievedItemWithIcon(lSelected
					.getUniqueID(), lTitle));
			inStatusLine.showStatusLineMessage(String.format(SUCCESS_MSG,
					lTitle));
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
	}

}
