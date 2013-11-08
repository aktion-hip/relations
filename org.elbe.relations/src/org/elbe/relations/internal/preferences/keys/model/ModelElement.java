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
 * Key binding model.
 * 
 * @author Luthiger
 */
public class ModelElement {

	public static final String PROP_PARENT = "parent"; //$NON-NLS-1$
	public static final String PROP_ID = "id"; //$NON-NLS-1$
	public static final String PROP_NAME = "name"; //$NON-NLS-1$
	public static final String PROP_DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String PROP_MODEL_OBJECT = "modelObject"; //$NON-NLS-1$
	private final KeyController controller;
	private ModelElement parent;
	private String id;
	private String name;
	private String description;
	private Object modelObject;

	/**
	 * ModelElement constructor.
	 * 
	 * @param inKeyController
	 *            {@link KeyController}
	 */
	public ModelElement(final KeyController inKeyController) {
		controller = inKeyController;
	}

	/**
	 * @return Returns the parent.
	 */
	public ModelElement getParent() {
		return parent;
	}

	/**
	 * @param inParent
	 *            The parent to set.
	 */
	public void setParent(final ModelElement inParent) {
		final ModelElement lOld = this.parent;
		this.parent = inParent;
		controller.firePropertyChange(this, PROP_PARENT, lOld, inParent);
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param inId
	 *            The id to set.
	 */
	public void setId(final String inId) {
		final String lOld = this.id;
		this.id = inId;
		controller.firePropertyChange(this, PROP_ID, lOld, inId);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param inName
	 *            The name to set.
	 */
	public void setName(final String inName) {
		final String lOld = this.name;
		this.name = inName;
		controller.firePropertyChange(this, PROP_NAME, lOld, inName);
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param inDescription
	 *            The description to set.
	 */
	public void setDescription(final String inDescription) {
		final String lOld = this.description;
		this.description = inDescription;
		controller.firePropertyChange(this, PROP_DESCRIPTION, lOld,
				inDescription);
	}

	/**
	 * @return Returns the context.
	 */
	public Object getModelObject() {
		return modelObject;
	}

	/**
	 * @param inContext
	 *            Object The context to set.
	 */
	public void setModelObject(final Object inContext) {
		final Object lOld = modelObject;
		modelObject = inContext;
		controller.firePropertyChange(this, PROP_MODEL_OBJECT, lOld, inContext);
	}

	protected KeyController getController() {
		return controller;
	}

}
