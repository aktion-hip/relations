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
package org.elbe.relations.defaultbrowser.internal.controller;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.elbe.relations.models.IBrowserRelation;
import org.elbe.relations.models.IRelation;

/**
 * The controller of a single relation between two items.
 * 
 * @author Benno Luthiger
 */
public class RelationEditPart extends AbstractConnectionEditPart implements
		IBrowserRelation {
	private final IRelation model;

	/**
	 * @param inRelation
	 *            IRelation
	 */
	public RelationEditPart(final IRelation inRelation) {
		super();
		model = inRelation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// Not needed.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModel()
	 */
	@Override
	public Object getModel() {
		return model;
	}

	@Override
	protected void refreshVisuals() {
		final PolylineConnection lFigure = (PolylineConnection) getFigure();
		lFigure.setForegroundColor(getSelected() == SELECTED_NONE ? ColorConstants.black
				: ColorConstants.red);
	}

	/**
	 * @see void org.eclipse.gef.editparts.AbstractEditPart.setSelected(int
	 *      value)
	 */
	@Override
	public void setSelected(final int inValue) {
		super.setSelected(inValue);
		refreshVisuals();
	}

}
