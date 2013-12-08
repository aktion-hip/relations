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

import java.util.Iterator;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.elbe.relations.RelationsConstants;

/**
 * Handler to activate the relation browser views.
 * 
 * @author lbenno
 */
public class ActivateViewBrowsers {

	@Execute
	void activate(final EPartService inPartService,
	        final EModelService inModelService, final MApplication inApplication) {
		// get browser stack
		final MPartStack lStack = (MPartStack) inModelService.find(
		        RelationsConstants.PART_STACK_BROWSERS, inApplication);
		final Iterator<MStackElement> lParts = lStack.getChildren().iterator();
		// iterate over children
		while (lParts.hasNext()) {
			final MStackElement lElement = lParts.next();
			if (lElement instanceof MPart) {
				final MPart lPart = (MPart) lElement;
				// activate visible
				if (inPartService.isPartVisible(lPart)) {
					inPartService.activate(lPart, true);
					break;
				}
			}
		}
	}

}
