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
package org.elbe.relations.handlers.item;

import java.sql.SQLException;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.internal.bom.BOMHelper;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.exc.VException;

/**
 * Handler to show the selected item (in the selection view) in the browser.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ItemBrowseHandler extends AbstractSelectionHandler {

	@Inject
	private IEclipseContext context;

	@Inject
	private IBrowserManager browserManager;

	@Inject
	private Logger log;

	@Execute
	void setSelectionItemToBrowser(final IEclipseContext inContext) {
		try {
			final IItemModel lItem = BOMHelper.getItem(getSelectionItem(),
					inContext);
			browserManager.setModel(CentralAssociationsModel
					.createCentralAssociationsModel(lItem, context));
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
	}

}
