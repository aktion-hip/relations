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

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.gef.editparts.FreeformGraphicalRootEditPart;
import org.elbe.relations.defaultbrowser.internal.views.ItemFigure;

/**
 * The root EditPart in the RelationsDefaultBrowser.
 * 
 * @author Benno Luthiger Created on 20.12.2005
 */
public class RelationsRootEditPart extends FreeformGraphicalRootEditPart {
	private ItemFigure mouseOverTargetFigure = null;

	/**
	 * RelationsRootEditPart constructor.
	 */
	public RelationsRootEditPart() {
		super();
	}

	@Override
	protected IFigure createFigure() {
		return super.createFigure();
	}

	@Override
	protected LayeredPane createPrintableLayers() {
		final FreeformLayeredPane layeredPane = new FreeformLayeredPane();
		layeredPane.add(new ConnectionLayer(), CONNECTION_LAYER);
		layeredPane.add(new FreeformLayer(), PRIMARY_LAYER);
		return layeredPane;
	}

	/**
	 * @param inFigure
	 *            The ItemFigure the mouse is over.
	 */
	public void setMouseOverTarget(final ItemFigure inFigure) {
		mouseOverTargetFigure = inFigure;
	}

	/**
	 * Resets the mouseOverTarget state.
	 */
	public void resetMouseOverTarget() {
		mouseOverTargetFigure = null;
	}

	/**
	 * Makes an item edit part that is child of this edit part clickable.
	 * 
	 * @param inClickable
	 *            boolean <code>true</code> if the child part has to be made
	 *            clickable.
	 */
	public void makeMousOverPartClickable(final boolean inClickable) {
		if (mouseOverTargetFigure != null) {
			mouseOverTargetFigure.setClickable(inClickable);
		}
	}

}
