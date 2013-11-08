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
package org.elbe.relations.defaultbrowser.internal.dnd;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.IAssociationsModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.models.PeripheralAssociationsModel;
import org.elbe.relations.services.IBrowserManager;

/**
 * Helper class for this browser plugin's drop.
 * 
 * @author Luthiger Created on 20.12.2009
 */
@SuppressWarnings("restriction")
public class DropHelper {

	// prevent class instantiation
	private DropHelper() {
	}

	/**
	 * Checks whether the drop target is the central or an external edit part
	 * and returns the appropriate <code>IAssociationsModel</code>.
	 * 
	 * @param inSelected
	 *            {@link ItemAdapter} the drop target's model.
	 * @param inBrowserManager
	 *            {@link IBrowserManager}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return IAssociationsModel the browser model.
	 * @throws Exception
	 */
	public static IAssociationsModel getModel(final ItemAdapter inSelected,
			final IBrowserManager inBrowserManager,
			final IEclipseContext inContext) throws Exception {
		final CentralAssociationsModel lCenter = inBrowserManager
				.getCenterModel();
		if (lCenter.getCenter().equals(inSelected)) {
			return lCenter;
		}
		return PeripheralAssociationsModel.createExternalAssociationsModel(
				inSelected, inContext);
	}

}
