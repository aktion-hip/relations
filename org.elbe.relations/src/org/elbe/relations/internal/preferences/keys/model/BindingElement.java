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

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.internal.preferences.keys.KeyController;
import org.elbe.relations.internal.preferences.keys.NewKeysPreferenceMessages;

/**
 * Model for a key binding element.
 * 
 * @author Luthiger
 */
public class BindingElement extends ModelElement {
	public final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	public static final String PROP_TRIGGER = "trigger"; //$NON-NLS-1$
	public static final String PROP_CONTEXT = "bindingContext"; //$NON-NLS-1$
	public static final String PROP_CATEGORY = "category"; //$NON-NLS-1$
	public static final String PROP_USER_DELTA = "userDelta"; //$NON-NLS-1$
	private static final String PROP_IMAGE = "image"; //$NON-NLS-1$
	public static final String PROP_CONFLICT = "bindingConflict"; //$NON-NLS-1$

	private TriggerSequence trigger;
	private ContextElement context;
	private String category;
	private Boolean conflict;
	private Integer userDelta;
	private Image image;

	/**
	 * BindingElement constructor.
	 * 
	 * @param inKeyController
	 */
	public BindingElement(final KeyController inKeyController) {
		super(inKeyController);
	}

	/**
	 * @param inBinding
	 *            {@link Binding}
	 * @param inContextModel
	 *            {@link ContextModel}
	 */
	public void init(final Binding inBinding, final ContextModel inContextModel) {
		setTrigger(inBinding.getTriggerSequence());
		setContext(inContextModel.getContextIdToElement().get(
				inBinding.getContextId()));
		setCommandInfo(inBinding.getParameterizedCommand());
		setUserDelta(new Integer(inBinding.getType()));
		setModelObject(inBinding);
	}

	/**
	 * @param inCommand
	 *            {@link ParameterizedCommand}
	 */
	public void init(final ParameterizedCommand inCommand) {
		setCommandInfo(inCommand);
		setTrigger(null);
		setContext(null);
		setUserDelta(new Integer(Binding.SYSTEM));
		setModelObject(inCommand);
	}

	private void setCommandInfo(final ParameterizedCommand bindingCommand) {
		setId(bindingCommand.getId());
		try {
			setName(bindingCommand.getName());
		}
		catch (final NotDefinedException e) {
			setName(NewKeysPreferenceMessages.Undefined_Command);
		}
		try {
			setDescription(bindingCommand.getCommand().getDescription());
		}
		catch (final NotDefinedException e) {
			setDescription(ZERO_LENGTH_STRING);
		}
		try {
			setCategory(bindingCommand.getCommand().getCategory().getName());
		}
		catch (final NotDefinedException e) {
			setCategory(NewKeysPreferenceMessages.Unavailable_Category);
		}
		setConflict(Boolean.FALSE);
	}

	public void setUserDelta(final Integer inUserDelta) {
		final Object lOld = this.userDelta;
		userDelta = inUserDelta;
		getController().firePropertyChange(this, PROP_USER_DELTA, lOld,
				inUserDelta);
	}

	/**
	 * @return Integer the userDelta.
	 */
	public Integer getUserDelta() {
		return userDelta;
	}

	public void setConflict(final Boolean inConflict) {
		final Object lOld = this.conflict;
		conflict = inConflict;
		getController().firePropertyChange(this, PROP_CONFLICT, lOld,
				inConflict);
	}

	/**
	 * @return Boolean the conflict.
	 */
	public Boolean getConflict() {
		return conflict;
	}

	public void setCategory(final String inCategory) {
		final Object lOld = this.category;
		category = inCategory;
		getController().firePropertyChange(this, PROP_CATEGORY, lOld,
				inCategory);
	}

	public String getCategory() {
		return category;
	}

	/**
	 * @param inContext
	 *            {@link ContextElement} the context to set
	 */
	public void setContext(final ContextElement inContext) {
		final ContextElement lOld = context;
		context = inContext;
		getController().firePropertyChange(this, PROP_CONTEXT, lOld, context);
	}

	/**
	 * @return {@link ContextElement}
	 */
	public ContextElement getContext() {
		return context;
	}

	/**
	 * @param inTrigger
	 */
	public void setTrigger(final TriggerSequence inTrigger) {
		final Object lOld = trigger;
		trigger = inTrigger;
		getController().firePropertyChange(this, PROP_TRIGGER, lOld, inTrigger);
	}

	/**
	 * @return {@link TriggerSequence}
	 */
	public TriggerSequence getTrigger() {
		return trigger;
	}

	/**
	 * @param inImage
	 *            The image to set.
	 */
	public void setImage(final Image inImage) {
		final Object lOld = this.image;
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
	 * @param inBinding
	 *            KeyBinding
	 * @param inContextModel
	 *            ContextModel
	 */
	public void fill(final KeyBinding inBinding,
			final ContextModel inContextModel) {
		setCommandInfo(inBinding.getParameterizedCommand());
		setTrigger(inBinding.getTriggerSequence());
		setContext(inContextModel.getContextIdToElement().get(
				inBinding.getContextId()));
		setUserDelta(new Integer(inBinding.getType()));
		setModelObject(inBinding);
	}

	/**
	 * @param inParameterizedCommand
	 *            ParameterizedCommand
	 */
	public void fill(final ParameterizedCommand inParameterizedCommand) {
		setCommandInfo(inParameterizedCommand);
		setTrigger(null);
		setContext(null);
		setUserDelta(new Integer(Binding.SYSTEM));
		setModelObject(inParameterizedCommand);
	}

}
