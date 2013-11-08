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
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.elbe.relations.internal.controls.IPartWithSelection;
import org.elbe.relations.models.ILightWeightModel;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.services.IRelationsBrowser;

/**
 * Abstract class providing functionality for handlers attached to popup menu
 * items of the selection views.
 * 
 * @author Luthiger
 */
public abstract class AbstractSelectionHandler {

	private ILightWeightModel selectionItem = null;

	/**
	 * @param inActivePart
	 *            {@link MPart}
	 * @return boolean <code>true</code> if an element in the view is selected
	 */
	@CanExecute
	boolean checkSelectionList(@Active final MPart inActivePart,
			final IBrowserManager inBrowserManager) {
		final Object lControl = inActivePart.getObject();
		if (lControl instanceof IRelationsBrowser) {
			return inBrowserManager.getSelectedModel() != null;
		}
		if (lControl instanceof IPartWithSelection) {
			return ((IPartWithSelection) lControl).hasSelection();
		}
		return false;
	}

	@Inject
	void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) final ILightWeightModel inSelectionItem) {
		selectionItem = inSelectionItem;
	}

	protected ILightWeightModel getSelectionItem() {
		return selectionItem;
	}

}
