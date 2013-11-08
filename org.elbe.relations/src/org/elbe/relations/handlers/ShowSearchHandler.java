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

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.addons.minmax.TrimStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.elbe.relations.RelationsConstants;

/**
 * Handler to display the search view (which is minimized by default).
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ShowSearchHandler {

	@Execute
	void showSearch(final MApplication inApplication,
			final EModelService inModelService, final EPartService inPartService) {
		// activate the search view
		final MUIElement lSearch = inModelService.find(
				RelationsConstants.PART_SEARCH, inApplication);
		inPartService.activate((MPart) lSearch);

		// show the fast view
		final MContribution lPart = (MContribution) inModelService.find(
				RelationsConstants.TRIM_STACK_TOOLS, inApplication);
		final TrimStack lTrimStack = (TrimStack) lPart.getObject();
		lTrimStack.showStack(true);
	}

}
