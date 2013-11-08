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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.internal.actions.ICommand;
import org.elbe.relations.internal.actions.RelateCommand;
import org.elbe.relations.models.ILightWeightModel;
import org.elbe.relations.services.IBrowserManager;

/**
 * Handler to related the selected item (in the selection view) with the
 * selected item (in the browser).
 * 
 * @author Luthiger
 */
public class ItemRelateHandler extends AbstractSelectionHandler {

	@Execute
	void relateItem(final IEclipseContext inContext,
			final IBrowserManager inBrowserManager) {
		final ILightWeightModel lItem = getSelectionItem();
		final UniqueID[] lIDs = new UniqueID[] { new UniqueID(
				lItem.getItemType(), lItem.getID()) };
		final ICommand lCommand = RelateCommand.createRelateCommand(
				inBrowserManager.getCenterModel(), lIDs, inContext);
		lCommand.execute();
	}

}
