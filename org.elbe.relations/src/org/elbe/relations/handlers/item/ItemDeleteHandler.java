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

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.bom.BOMHelper;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.services.IRelationsBrowser;
import org.hip.kernel.exc.VException;

/**
 * Handler to delete the selected item (in the selection view).
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ItemDeleteHandler extends AbstractSelectionHandler {

	@Inject
	private Logger log;

	@Inject
	private IDataService dataService;

	@Inject
	private IBrowserManager browserManager;

	@Execute
	void deleteItem(@Active final MPart inActivePart,
			final IEclipseContext inContext) {
		try {
			final Object lControl = inActivePart.getObject();
			final boolean lDelInBrowser = (lControl instanceof IRelationsBrowser);
			final IItemModel lItem = lDelInBrowser ? browserManager
					.getSelectedModel() : BOMHelper.getItem(getSelectionItem(),
					inContext);
			if (isVerified(
					RelationsMessages
							.getString("AbstractDeleteAction.delete.title"), lItem.getTitle())) { //$NON-NLS-1$
				// Delegate the deletion to the selected item.
				lItem.getItemDeleteAction(log).run();
				// Delete the item's relations.
				org.elbe.relations.data.bom.BOMHelper.getRelationHome()
						.deleteRelations(lItem);
				// Notify browsers - we need this to check whether we have to
				// refresh the center model.
				browserManager.checkAfterDeletion(lItem);
				// Notify model and views listening to model changes - we need
				// this to refresh the collection containing all items.
				dataService.removeDeleted(lDelInBrowser ? lItem
						.getLightWeight() : getSelectionItem());
			}
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Ask the user to verify the deletion of the selected item.
	 * 
	 * @param inWindowTitle
	 *            String
	 * @param inItemTitle
	 *            String
	 * @return boolean true, if the user clicked 'Ok'.
	 */
	private boolean isVerified(final String inWindowTitle,
			final String inItemTitle) {
		return MessageDialog
				.openQuestion(
						Display.getCurrent().getActiveShell(),
						inWindowTitle,
						RelationsMessages
								.getString(
										"AbstractDeleteAction.delete.msg", new String[] { inItemTitle })); //$NON-NLS-1$
	}

}
