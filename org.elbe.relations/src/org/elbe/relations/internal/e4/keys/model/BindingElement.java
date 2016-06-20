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

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.internal.e4.keys.KeyController;
import org.elbe.relations.internal.e4.keys.NewKeysPreferenceMessages;
import org.elbe.relations.internal.e4.keys.RelationsKeysPreferencePage;

/**
 * Model for the key binding elements.<br />
 * This model is used for the table on the key preferences page.
 *
 * @author lbenno
 */
public class BindingElement extends ModelElement {
	public static final String PROP_TRIGGER = "trigger"; //$NON-NLS-1$
	public static final String PROP_CONTEXT = "bindingContext"; //$NON-NLS-1$
	public static final String PROP_CATEGORY = "category"; //$NON-NLS-1$
	public static final String PROP_USER_DELTA = "userDelta"; //$NON-NLS-1$
	private static final String PROP_IMAGE = "image"; //$NON-NLS-1$
	public static final String PROP_CONFLICT = "bindingConflict"; //$NON-NLS-1$

	private ContextElement context;
	private Image image;
	private TriggerSequence trigger;
	private Boolean conflict;
	private String category;
	private Integer userDelta;

	/**
	 * @param controller
	 */
	public BindingElement(KeyController controller) {
		super(controller);
	}

	/**
	 * @param command
	 *            {@link ParameterizedCommand}
	 */
	public void init(ParameterizedCommand command) {
		setCommandInfo(command);
		setTrigger(null);
		setContext(null);
		setUserDelta(Binding.SYSTEM);
		setModelObject(command);
	}

	/**
	 * @param inCommand
	 *            {@link ParameterizedCommand}
	 */
	public void fill(final ParameterizedCommand inCommand) {
		setCommandInfo(inCommand);
		setTrigger(null);
		setContext(null);
		setUserDelta(Binding.SYSTEM);
		setModelObject(inCommand);
	}

	/**
	 * @param binding
	 *            {@link Binding}
	 * @param contextModel
	 *            {@link ContextModel}
	 */
	public void init(Binding binding, ContextModel contextModel) {
		setTrigger(binding.getTriggerSequence());
		setContext(contextModel.getContextIdToElement()
		        .get(binding.getContextId()));
		setCommandInfo(binding.getParameterizedCommand());
		setUserDelta(binding.getType());
		setModelObject(binding);
	}

	/**
	 * @param binding
	 *            {@link KeyBinding}
	 * @param contextModel
	 *            {@link ContextModel}
	 */
	public void fill(final KeyBinding binding,
	        final ContextModel contextModel) {
		setTrigger(binding.getTriggerSequence());
		setContext(contextModel.getContextIdToElement()
		        .get(binding.getContextId()));
		setCommandInfo(binding.getParameterizedCommand());
		setUserDelta(binding.getType());
		setModelObject(binding);
	}

	/**
	 * @param userDelta
	 *            {@link Integer}
	 */
	public void setUserDelta(Integer userDelta) {
		final Object lOld = this.userDelta;
		this.userDelta = userDelta;
		getController().firePropertyChange(this, PROP_USER_DELTA, lOld,
		        userDelta);
	}

	/**
	 *
	 * @return Integer the binding's user delta
	 */
	public Integer getUserDelta() {
		return userDelta;
	}

	private void setCommandInfo(ParameterizedCommand bindingCommand) {
		setId(bindingCommand.getId());
		try {
			setName(bindingCommand.getName());
		}
		catch (final NotDefinedException | IllegalStateException exc) {
			setName(NewKeysPreferenceMessages.Undefined_Command);
		}
		try {
			setDescription(bindingCommand.getCommand().getDescription());
		}
		catch (final NotDefinedException exc) {
			setDescription(RelationsKeysPreferencePage.ZERO_LENGTH_STRING);
		}
		try {
			setCategory(bindingCommand.getCommand().getCategory().getName());
		}
		catch (final NotDefinedException exc) {
			setCategory(NewKeysPreferenceMessages.Unavailable_Category);
		}
		setConflict(Boolean.FALSE);
	}

	/**
	 * @param category
	 *            String the binding's category
	 */
	public void setCategory(String category) {
		final Object lOld = this.category;
		this.category = category;
		getController().firePropertyChange(this, PROP_CATEGORY, lOld, category);
	}

	/**
	 *
	 * @return String the binding's category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param conflict
	 *            {@link Boolean}
	 */
	public void setConflict(Boolean conflict) {
		final Object lOld = this.conflict;
		this.conflict = conflict;
		getController().firePropertyChange(this, PROP_CONFLICT, lOld, conflict);
	}

	/**
	 *
	 * @return {@link Boolean} the conflict flag
	 */
	public Boolean getConflict() {
		return conflict;
	}

	/**
	 * @param context
	 *            {@link ContextElement} the context to set
	 */
	public void setContext(final ContextElement context) {
		final ContextElement lOld = this.context;
		this.context = context;
		getController().firePropertyChange(this, PROP_CONTEXT, lOld, context);
	}

	/**
	 * @return {@link ContextElement}
	 */
	public ContextElement getContext() {
		return context;
	}

	/**
	 * @param inImage
	 *            The image to set.
	 */
	public void setImage(final Image inImage) {
		final Object lOld = image;
		image = inImage;
		getController().firePropertyChange(this, PROP_IMAGE, lOld, inImage);
	}

	/**
	 * @return {@link Image} the image.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * @param trigger
	 *            {@link TriggerSequence}
	 */
	public void setTrigger(final TriggerSequence trigger) {
		final Object lOld = this.trigger;
		this.trigger = trigger;
		getController().firePropertyChange(this, PROP_TRIGGER, lOld, trigger);
	}

	/**
	 * @return {@link TriggerSequence}
	 */
	public TriggerSequence getTrigger() {
		return trigger;
	}

}
