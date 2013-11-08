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

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.elbe.relations.internal.controls.InspectorView;

/**
 * Handler to undo the changes applied in the inspector part.
 * 
 * @author Luthiger
 */
public class InspectorUndoHandler {

	@CanExecute
	boolean canExecute(
			@Named(IServiceConstants.ACTIVE_PART) final MDirtyable inDirtyable) {
		return inDirtyable == null ? false : inDirtyable.isDirty();
	}

	@Execute
	void undoChanges(@Named(IServiceConstants.ACTIVE_PART) final MPart inPart) {
		if (inPart.getObject() instanceof InspectorView) {
			((InspectorView) inPart.getObject()).undoChanges();
		}
	}

}
