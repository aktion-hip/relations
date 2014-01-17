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

package org.elbe.relations.handlers.style;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.elbe.relations.RelationsConstants;

/**
 * Helper class for retrieving the <code>MHandledMenuItem</code> of the popup
 * menu <code>org.elbe.relations.inspector.popup</code>.
 * 
 * @author lbenno
 */
public final class PopupHelper {
	private MMenu popupStyles;
	private MHandledMenuItem menuItem;

	/**
	 * Finds the popup menu's item with the specified ID.
	 * 
	 * @param inItemID
	 *            String
	 * @param inApplication
	 *            {@link MApplication}
	 * @param inModel
	 *            {@link EModelService}
	 * @return {@link MHandledMenuItem}
	 */
	public MHandledMenuItem findMenuItem(final String inItemID,
	        final MApplication inApplication, final EModelService inModel) {
		return findMenuItem(inItemID, getPopupMenu(inApplication, inModel));
	}

	/**
	 * Sets the item with the specified ID' toggle state.
	 * 
	 * @param inItemID
	 *            String
	 * @param inState
	 *            boolean
	 * @param inApplication
	 *            {@link MApplication}
	 * @param inModel
	 *            {@link EModelService}
	 */
	public void setSelected(final String inItemID, final boolean inState,
	        final MApplication inApplication, final EModelService inModel) {
		final MHandledMenuItem lItem = getMenuItem(inItemID, inApplication,
		        inModel);
		if (lItem != null) {
			lItem.setSelected(inState);
		}
	}

	private MHandledMenuItem getMenuItem(final String inItemID,
	        final MApplication inApplication, final EModelService inModel) {
		if (menuItem == null) {
			menuItem = findMenuItem(inItemID, inApplication, inModel);
		}
		return menuItem;
	}

	private MMenu getPopupMenu(final MApplication inApplication,
	        final EModelService inModel) {
		if (popupStyles == null) {
			final MPart lPart = (MPart) inModel.find(
			        RelationsConstants.PART_INSPECTOR, inApplication);
			for (final MMenu lMenu : lPart.getMenus()) {
				if (RelationsConstants.POPUP_INSPECTOR.equals(lMenu
				        .getElementId())) {
					popupStyles = lMenu;
					break;
				}
			}
		}
		return popupStyles;
	}

	private MHandledMenuItem findMenuItem(final String inItemID,
	        final MMenu inMenu) {
		inMenu.getChildren();
		for (final MMenuElement lItem : inMenu.getChildren()) {
			if (inItemID.equals(lItem.getElementId())) {
				return (MHandledMenuItem) lItem;
			}
		}
		return null;
	}

}
