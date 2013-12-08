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
package org.elbe.relations.handlers.activate;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.addons.minmax.TrimStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

/**
 * Helper class for part activation.
 * 
 * @author lbenno
 */
@SuppressWarnings("restriction")
public final class ActivationHelper {

	private ActivationHelper() {
		// prevent instantiation
	}

	/**
	 * Activate the view with the specified ID.
	 * 
	 * @param inViewID
	 *            String the view's ID
	 * @param inPartService
	 *            {@link EPartService}
	 * @param inModelService
	 *            {@link EModelService}
	 * @param inApplication
	 *            {@link MApplication}
	 */
	protected static void activate(final String inViewID,
	        final EPartService inPartService,
	        final EModelService inModelService, final MApplication inApplication) {
		final MPart lPart = (MPart) inModelService
		        .find(inViewID, inApplication);
		inPartService.activate(lPart, true);
	}

	/**
	 * Show the fast view with the specified ID.
	 * 
	 * @param inPartID
	 *            String the fast view's ID
	 * @param inModelService
	 *            {@link EModelService}
	 * @param inApplication
	 *            {@link MApplication}
	 */
	protected static void showFast(final String inPartID,
	        final EModelService inModelService, final MApplication inApplication) {
		final MContribution lPart = (MContribution) inModelService.find(
		        inPartID, inApplication);
		final TrimStack lTrimStack = (TrimStack) lPart.getObject();
		lTrimStack.showStack(true);
	}

}
