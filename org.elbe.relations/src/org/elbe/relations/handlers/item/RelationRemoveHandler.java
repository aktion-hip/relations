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

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.utility.BrowserPopupStateController;

/**
 * Handler that removes the selected (i.e. dropped) item's relation from the
 * center item.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class RelationRemoveHandler {

	@Inject
	private Logger log;

	@Execute
	void relationRemove(final IBrowserManager inBrowserManager) {
		try {
			final CentralAssociationsModel lModel = inBrowserManager
					.getCenterModel();
			final ItemAdapter[] lSelected = new ItemAdapter[] { inBrowserManager
					.getSelectedModel() };
			lModel.removeAssociations(lSelected);
			lModel.saveChanges();
			Display.getCurrent().beep();
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	@CanExecute
	boolean checkPeripheral(final IEclipseContext inContext) {
		return BrowserPopupStateController.checkStatePeriphery(inContext);
	}

}
