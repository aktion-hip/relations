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
package org.elbe.relations.internal.preferences.keys.model;

import org.elbe.relations.internal.preferences.keys.KeyController;

/**
 * Model base class providing common functionality.
 * 
 * @author Luthiger
 */
public class CommonModel extends ModelElement {
	public static final String PROP_SELECTED_ELEMENT = "selectedElement"; //$NON-NLS-1$
	private ModelElement selectedElement;

	/**
	 * CommonModel constructor.
	 * 
	 * @param inKeyController
	 *            {@link KeyController}
	 */
	public CommonModel(final KeyController inKeyController) {
		super(inKeyController);
	}

	/**
	 * @return Returns the selectedContext.
	 */
	public ModelElement getSelectedElement() {
		return selectedElement;
	}

	/**
	 * @param inSelectedContext
	 *            The selectedContext to set.
	 */
	public void setSelectedElement(final ModelElement inSelectedContext) {
		final ModelElement lOld = this.selectedElement;
		this.selectedElement = inSelectedContext;
		getController().firePropertyChange(this, PROP_SELECTED_ELEMENT, lOld,
				inSelectedContext);
	}

}
