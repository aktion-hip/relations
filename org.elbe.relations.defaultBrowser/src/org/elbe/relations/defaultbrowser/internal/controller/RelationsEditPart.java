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

import java.util.List;

import javax.inject.Inject;

import org.eclipse.draw2d.IFigure;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.defaultbrowser.internal.views.RelationsFigure;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.IBrowserPart;
import org.elbe.relations.models.ItemAdapter;
import org.hip.kernel.exc.VException;

/**
 * Controller for the display of the set of related items.
 * 
 * @author Benno Luthiger Created on 16.12.2005
 */
@SuppressWarnings("restriction")
public class RelationsEditPart extends AbstractGraphicalEditPart implements
		IBrowserPart {
	private final CentralAssociationsModel model;

	@Inject
	private Logger log;

	public RelationsEditPart(final CentralAssociationsModel inModel) {
		super();
		model = inModel;
	}

	@Override
	public Object getModel() {
		return model;
	}

	@Override
	protected IFigure createFigure() {
		return new RelationsFigure();
	}

	/**
	 * @see org.eclipse.gef.EditPart#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		refreshVisuals();
	}

	/**
	 * @see AbstractEditPart#refreshVisuals()
	 */
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshChildren();
	}

	/**
	 * @see AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List<ItemAdapter> getModelChildren() {
		return model.getAllItems();
	}

	/**
	 * Returns the model's unique ID.
	 * 
	 * @return UniqueID
	 */
	public UniqueID getModelID() {
		final ItemAdapter lItem = model.getCenter();
		try {
			return new UniqueID(lItem.getItemType(), lItem.getID());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		return null;
	}

	@Override
	protected void createEditPolicies() {
		// Nothing to do.
	}

}
