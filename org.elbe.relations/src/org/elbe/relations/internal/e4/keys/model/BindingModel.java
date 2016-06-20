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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.ui.internal.util.Util;
import org.elbe.relations.internal.e4.keys.KeyController;

/**
 * Model for key bindings.<br />
 * The binding model is the container of <code>BindingElement</code>s.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public class BindingModel extends CommonModel {
	public static final String PROP_BINDING_ADD = "bindingAdd"; //$NON-NLS-1$
	public static final String PROP_BINDING_ELEMENT_MAP = "bindingElementMap"; //$NON-NLS-1$
	public static final String PROP_BINDING_FILTER = "bindingFilter"; //$NON-NLS-1$
	public static final String PROP_BINDING_REMOVE = "bindingRemove"; //$NON-NLS-1$
	public static final String PROP_BINDINGS = "bindings"; //$NON-NLS-1$
	public static final String PROP_CONFLICT_ELEMENT_MAP = "bindingConfictMap"; //$NON-NLS-1$

	private BindingManager bindingManager;
	private HashMap<Binding, BindingElement> bindingToElement;
	private HashMap<ParameterizedCommand, BindingElement> commandToElement;
	private HashSet<BindingElement> bindingElements;
	private HashSet<ParameterizedCommand> allParameterizedCommands;

	/**
	 * @param controller
	 *            {@link KeyController}
	 */
	public BindingModel(KeyController controller) {
		super(controller);
	}

	/**
	 * Initializes the model.
	 *
	 * @param bindingManager
	 *            {@link BindingManager}
	 * @param commandManager
	 *            {@link CommandManager}
	 * @param bindingTables
	 *            List&lt;MBindingTable>
	 * @param contextModel
	 *            {@link ContextModel}
	 * @return {@link BindingModel} the instance
	 */
	@SuppressWarnings("unchecked")
	public BindingModel init(BindingManager bindingManager,
	        CommandManager commandManager, List<MBindingTable> bindingTables,
	        ContextModel contextModel) {
		this.bindingManager = bindingManager;
		bindingToElement = new HashMap<Binding, BindingElement>();
		commandToElement = new HashMap<ParameterizedCommand, BindingElement>();
		bindingElements = new HashSet<BindingElement>();

		final Set<ParameterizedCommand> cmdsForBindings = new HashSet<ParameterizedCommand>();
		final Iterator<Binding> bindings = bindingManager
		        .getActiveBindingsDisregardingContextFlat().iterator();
		while (bindings.hasNext()) {
			final Binding binding = bindings.next();
			final BindingElement element = new BindingElement(getController());
			element.init(binding, contextModel);
			element.setParent(this);
			bindingElements.add(element);
			bindingToElement.put(binding, element);
			cmdsForBindings.add(binding.getParameterizedCommand());
		}

		final Set<String> commandIds = commandManager.getDefinedCommandIds();
		allParameterizedCommands = new HashSet<ParameterizedCommand>();
		final Iterator<String> commandIdItr = commandIds.iterator();
		while (commandIdItr.hasNext()) {
			final String currentCommandId = commandIdItr.next();
			final Command currentCommand = commandManager
			        .getCommand(currentCommandId);
			try {
				allParameterizedCommands.addAll(
				        CommandHelper.generateCombinations(currentCommand));
			}
			catch (final NotDefinedException exc) {
				// It is safe to just ignore undefined commands.
			}
		}

		final Iterator<ParameterizedCommand> commands = allParameterizedCommands
		        .iterator();
		while (commands.hasNext()) {
			final ParameterizedCommand command = commands.next();
			if (!cmdsForBindings.contains(command)) {
				final BindingElement bindingElement = new BindingElement(
				        getController());
				bindingElement.init(command);
				bindingElement.setParent(this);
				bindingElements.add(bindingElement);
				commandToElement.put(command, bindingElement);
			}
		}

		return this;
	}

	/**
	 * @return Set<BindingElement> the bindings
	 */
	public Set<BindingElement> getBindings() {
		return bindingElements;
	}

	/**
	 * @return Map<Binding, BindingElement> the bindingToElement.
	 */
	public Map<Binding, BindingElement> getBindingToElement() {
		return bindingToElement;
	}

	/**
	 * Refreshes the binding model to be in sync with the {@link BindingManager}
	 *
	 * @param contextModel
	 *            {@link ContextModel}
	 * @return {@link BindingModel} the instance
	 */
	@SuppressWarnings("unchecked")
	public BindingModel refresh(ContextModel contextModel) {
		final Set<Object> cmdsForBindings = new HashSet<Object>();
		final Collection<Binding> activeManagerBindings = bindingManager
		        .getActiveBindingsDisregardingContextFlat();

		// add any bindings that we don't already have.
		final Iterator<Binding> bindings = activeManagerBindings.iterator();
		while (bindings.hasNext()) {
			final KeyBinding binding = (KeyBinding) bindings.next();
			final ParameterizedCommand parameterizedCommand = binding
			        .getParameterizedCommand();
			cmdsForBindings.add(parameterizedCommand);
			if (!bindingToElement.containsKey(binding)) {
				final BindingElement lBindingElement = new BindingElement(
				        getController());
				lBindingElement.init(binding, contextModel);
				lBindingElement.setParent(this);
				bindingElements.add(lBindingElement);
				bindingToElement.put(binding, lBindingElement);
				getController().firePropertyChange(this, PROP_BINDING_ADD, null,
				        lBindingElement);

				if (commandToElement.containsKey(parameterizedCommand)
				        && lBindingElement.getUserDelta()
				                .intValue() == Binding.SYSTEM) {
					final Object lRemove = commandToElement
					        .remove(parameterizedCommand);
					bindingElements.remove(lRemove);
					getController().firePropertyChange(this,
					        PROP_BINDING_REMOVE, null, lRemove);
				}
			}
		}

		// remove bindings that shouldn't be there
		final Iterator<BindingElement> lBindingElements = bindingElements
		        .iterator();
		while (lBindingElements.hasNext()) {
			final BindingElement bindingElement = lBindingElements.next();
			final Object obj = bindingElement.getModelObject();
			if (obj instanceof Binding) {
				final Binding binding = (Binding) obj;
				if (!activeManagerBindings.contains(binding)) {
					final ParameterizedCommand cmd = binding
					        .getParameterizedCommand();
					if (cmd != null) {
						commandToElement.remove(cmd);
					}
					bindingToElement.remove(binding);
					lBindingElements.remove();
					getController().firePropertyChange(this,
					        PROP_BINDING_REMOVE, null, bindingElement);
				}
			} else {
				cmdsForBindings.add(obj);
			}
		}

		// If we removed the last binding for a parameterized command,
		// put back the CMD
		final Iterator<ParameterizedCommand> commands = allParameterizedCommands
		        .iterator();
		while (commands.hasNext()) {
			final ParameterizedCommand cmd = commands.next();
			if (!cmdsForBindings.contains(cmd)) {
				final BindingElement bindingElement = new BindingElement(
				        getController());
				bindingElement.init(cmd);
				bindingElement.setParent(this);
				bindingElements.add(bindingElement);
				commandToElement.put(cmd, bindingElement);
				getController().firePropertyChange(this, PROP_BINDING_ADD, null,
				        bindingElement);
			}
		}

		return this;
	}

	/**
	 * Makes a copy of the selected element.
	 *
	 */
	public void copy() {
		copy((BindingElement) getSelectedElement());
	}

	/**
	 * Makes a copy of the specified element.
	 *
	 * @param element
	 *            {@link BindingElement}
	 */
	public void copy(final BindingElement element) {
		if (element == null || !(element.getModelObject() instanceof Binding)) {
			return;
		}
		final BindingElement bindingElement = new BindingElement(
		        getController());
		final ParameterizedCommand parameterizedCommand = ((Binding) element
		        .getModelObject()).getParameterizedCommand();
		bindingElement.init(parameterizedCommand);
		bindingElement.setParent(this);
		bindingElements.add(bindingElement);
		commandToElement.put(parameterizedCommand, bindingElement);
		getController().firePropertyChange(this, PROP_BINDING_ADD, null,
		        bindingElement);
		setSelectedElement(bindingElement);
	}

	/**
	 * Removes the selected element's binding.
	 */
	public void remove() {
		remove((BindingElement) getSelectedElement());
	}

	/**
	 * Removes the <code>bindingElement</code> binding.
	 *
	 * @param bindingElement
	 *            {@link BindingElement}
	 */
	public void remove(final BindingElement bindingElement) {
		if (bindingElement == null
		        || !(bindingElement.getModelObject() instanceof Binding)) {
			return;
		}

		final KeyBinding keyBinding = (KeyBinding) bindingElement
		        .getModelObject();
		if (keyBinding.getType() == Binding.USER) {
			bindingManager.removeBinding(keyBinding);
		} else {
			final KeySequence keySequence = keyBinding.getKeySequence();

			// Add the deleted binding
			bindingManager.addBinding(new KeyBinding(keySequence, null,
			        keyBinding.getSchemeId(), keyBinding.getContextId(), null,
			        null, null, Binding.USER));

			// Unbind any conflicts affected by the deleted binding
			final ConflictModel lConflictModel = getController()
			        .getConflictModel();
			lConflictModel.updateConflictsFor(bindingElement);
			final Collection<BindingElement> lConflictsList = lConflictModel
			        .getConflicts();
			if (lConflictsList != null) {
				final Object[] lConflicts = lConflictsList.toArray();
				for (int i = 0; i < lConflicts.length; i++) {
					final BindingElement lBindingElement = (BindingElement) lConflicts[i];
					if (lBindingElement == bindingElement) {
						continue;
					}
					final Object lModelObject = lBindingElement
					        .getModelObject();
					if (lModelObject instanceof Binding) {
						final Binding lBinding = (Binding) lModelObject;
						if (lBinding.getType() != Binding.SYSTEM) {
							continue;
						}
						final ParameterizedCommand lCommand = lBinding
						        .getParameterizedCommand();
						lBindingElement.fill(lCommand);
						commandToElement.put(lCommand, lBindingElement);
					}
				}
			}
		}
		final ParameterizedCommand lParameterizedCommand = keyBinding
		        .getParameterizedCommand();
		bindingElement.fill(lParameterizedCommand);
		commandToElement.put(lParameterizedCommand, bindingElement);
		getController().firePropertyChange(this, PROP_CONFLICT_ELEMENT_MAP,
		        null, bindingElement);
	}

	/**
	 * Restores the specified BindingElement. A refresh should be performed
	 * afterwards. The refresh may be done after several elements have been
	 * restored.
	 *
	 * @param bindingElement
	 *            {@link BindingElement}
	 */
	public void restoreBinding(final BindingElement bindingElement) {
		if (bindingElement == null) {
			return;
		}

		final Object lModelObject = bindingElement.getModelObject();
		ParameterizedCommand lCmd = null;
		if (lModelObject instanceof ParameterizedCommand) {
			lCmd = (ParameterizedCommand) lModelObject;
			final TriggerSequence lTrigger = bindingManager
			        .getBestActiveBindingFor(lCmd.getId());
			final Binding lBinding = bindingManager.getPerfectMatch(lTrigger);
			if (lBinding != null && lBinding.getType() == Binding.SYSTEM) {
				return;
			}
		} else if (lModelObject instanceof KeyBinding) {
			lCmd = ((KeyBinding) lModelObject).getParameterizedCommand();
		}

		// Remove any USER bindings
		final Binding[] lManagerBindings = bindingManager.getBindings();
		final ArrayList<Binding> lSystemBindings = new ArrayList<Binding>();
		final ArrayList<Binding> lRemovalBindings = new ArrayList<Binding>();
		for (final Binding lManagerBinding : lManagerBindings) {
			if (lManagerBinding.getParameterizedCommand() == null) {
				lRemovalBindings.add(lManagerBinding);
			} else if (lManagerBinding.getParameterizedCommand().equals(lCmd)) {
				if (lManagerBinding.getType() == Binding.USER) {
					bindingManager.removeBinding(lManagerBinding);
				} else if (lManagerBinding.getType() == Binding.SYSTEM) {
					lSystemBindings.add(lManagerBinding);
				}
			}
		}

		// Clear the USER bindings for parameterized commands
		final Iterator<Binding> lSystemIter = lSystemBindings.iterator();
		while (lSystemIter.hasNext()) {
			final Binding lSys = lSystemIter.next();
			final Iterator<Binding> lRemovalIter = lRemovalBindings.iterator();
			while (lRemovalIter.hasNext()) {
				final Binding lDelete = lRemovalIter.next();
				if (deletes(lDelete, lSys)
				        && lDelete.getType() == Binding.USER) {
					bindingManager.removeBinding(lDelete);
				}
			}
		}

		setSelectedElement(null);

		bindingElements.remove(bindingElement);
		bindingToElement.remove(lModelObject);
		commandToElement.remove(lModelObject);
		getController().firePropertyChange(this, PROP_BINDING_REMOVE, null,
		        bindingElement);
	}

	/**
	 * Restores the currently selected binding.
	 *
	 * @param inContextModel
	 *            {@link ContextModel}
	 */
	public void restoreBinding(final ContextModel inContextModel) {
		final BindingElement lElement = (BindingElement) getSelectedElement();
		if (lElement == null) {
			return;
		}

		restoreBinding(lElement);
		refresh(inContextModel);

		Object lObj = lElement.getModelObject();
		ParameterizedCommand lCmd = null;
		if (lObj instanceof ParameterizedCommand) {
			lCmd = (ParameterizedCommand) lObj;
		} else if (lObj instanceof KeyBinding) {
			lCmd = ((KeyBinding) lObj).getParameterizedCommand();
		}

		boolean lDone = false;
		final Iterator<BindingElement> lElements = bindingElements.iterator();
		// Reselects the command
		while (lElements.hasNext() && !lDone) {
			final BindingElement lBindingElement = lElements.next();
			lObj = lBindingElement.getModelObject();
			ParameterizedCommand lParameterized = null;
			if (lObj instanceof ParameterizedCommand) {
				lParameterized = (ParameterizedCommand) lObj;
			} else if (lObj instanceof KeyBinding) {
				lParameterized = ((KeyBinding) lObj).getParameterizedCommand();
			}
			if (lCmd.equals(lParameterized)) {
				lDone = true;
				setSelectedElement(lBindingElement);
			}
		}
	}

	// --- static method ---

	final static boolean deletes(final Binding toDelete,
	        final Binding binding) {
		boolean outDeletes = true;
		outDeletes &= Util.equals(toDelete.getContextId(),
		        binding.getContextId());
		outDeletes &= Util.equals(toDelete.getTriggerSequence(),
		        binding.getTriggerSequence());
		if (toDelete.getLocale() != null) {
			outDeletes &= Util.equals(toDelete.getLocale(),
			        binding.getLocale());
		}
		if (toDelete.getPlatform() != null) {
			outDeletes &= Util.equals(toDelete.getPlatform(),
			        binding.getPlatform());
		}
		outDeletes &= (binding.getType() == Binding.SYSTEM);
		outDeletes &= Util.equals(toDelete.getParameterizedCommand(), null);

		return outDeletes;
	}

}
