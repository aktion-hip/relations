/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.internal.e4.keys.model;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.elbe.relations.internal.e4.keys.KeyController;

/**
 * Model for a key binding context element.
 *
 * @author lbenno
 */
public class ContextElement extends ModelElement {

	private ContextElement parent;

	/**
	 * @param controller
	 *            {@link KeyController}
	 */
	public ContextElement(KeyController controller) {
		super(controller);
	}

	/**
	 * @param context
	 *            {@link MBindingContext}
	 */
	public void init(MBindingContext context) {
		setId(context.getElementId());
		setModelObject(context);
		setName(context.getName());
		setDescription(context.getDescription());
	}

	/**
	 * @param parentContext
	 *            {@link ContextElement}
	 */
	public void setParentContext(ContextElement parentContext) {
		parent = parentContext;
	}

	public String getParentId() {
		return parent == null ? null : parent.getId();
	}

}
