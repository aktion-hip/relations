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
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.ui.internal.util.Util;
import org.elbe.relations.internal.preferences.keys.KeyController;

/**
 * Model for key bindings.
 * 
 * @author Luthiger
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

	/**
	 * Holds all the {@link BindingElement} objects.
	 */
	private Set<BindingElement> bindingElements;

	/**
	 * A map of {@link Binding} objects to {@link BindingElement} objects.
	 */
	private Map<Binding, BindingElement> bindingToElement;

	/**
	 * A map of {@link ParameterizedCommand} objects to {@link BindingElement}
	 * objects.
	 */
	private Map<ParameterizedCommand, BindingElement> commandToElement;

	private Collection<ParameterizedCommand> allParameterizedCommands;

	/**
	 * BindingModel constructor.
	 * 
	 * @param inKeyController
	 *            {@link KeyController}
	 */
	public BindingModel(final KeyController inKeyController) {
		super(inKeyController);
	}

	/**
	 * @param inBindingManager
	 * @param inBindingService
	 *            {@link EBindingService}
	 * @param inCommandManager
	 *            {@link CommandManager}
	 * @param inBindingTables
	 *            List<MBindingTable>
	 * @param inContextModel
	 *            {@link ContextModel}
	 */
	@SuppressWarnings({ "unchecked" })
	public void init(final BindingManager inBindingService,
			final CommandManager inCommandManager,
			final List<MBindingTable> inBindingTables,
			final ContextModel inContextModel) {
		final Set<ParameterizedCommand> lCmdsForBindings = new HashSet<ParameterizedCommand>();
		bindingManager = inBindingService;
		bindingToElement = new HashMap<Binding, BindingElement>();
		commandToElement = new HashMap<ParameterizedCommand, BindingElement>();

		bindingElements = new HashSet<BindingElement>();
		final Iterator<Binding> lBindings = bindingManager
				.getActiveBindingsDisregardingContextFlat().iterator();
		while (lBindings.hasNext()) {
			final Binding lBinding = lBindings.next();
			final BindingElement lElement = new BindingElement(getController());
			lElement.init(lBinding, inContextModel);
			lElement.setParent(this);
			bindingElements.add(lElement);
			bindingToElement.put(lBinding, lElement);
			lCmdsForBindings.add(lBinding.getParameterizedCommand());
		}

		final Set<String> lCommandIds = inCommandManager.getDefinedCommandIds();
		allParameterizedCommands = new HashSet<ParameterizedCommand>();
		final Iterator<String> lCommandIdItr = lCommandIds.iterator();
		while (lCommandIdItr.hasNext()) {
			final String lCurrentCommandId = lCommandIdItr.next();
			final Command lCurrentCommand = inCommandManager
					.getCommand(lCurrentCommandId);
			try {
				allParameterizedCommands.addAll(CommandHelper
						.generateCombinations(lCurrentCommand));
				// allParameterizedCommands.addAll(ParameterizedCommand
				// .generateCombinations(lCurrentCommand));
			}
			catch (final NotDefinedException exc) {
				// It is safe to just ignore undefined commands.
			}
		}

		final Iterator<ParameterizedCommand> lCommands = allParameterizedCommands
				.iterator();
		while (lCommands.hasNext()) {
			final ParameterizedCommand lCmd = lCommands.next();
			if (!lCmdsForBindings.contains(lCmd)) {
				final BindingElement lBindingElement = new BindingElement(
						getController());
				lBindingElement.init(lCmd);
				lBindingElement.setParent(this);
				bindingElements.add(lBindingElement);
				commandToElement.put(lCmd, lBindingElement);
			}
		}

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
	 * @param inContextModel
	 */
	@SuppressWarnings("unchecked")
	public void refresh(final ContextModel inContextModel) {
		final Set<Object> lCmdsForBindings = new HashSet<Object>();
		final Collection<Binding> lActiveManagerBindings = bindingManager
				.getActiveBindingsDisregardingContextFlat();

		// add any bindings that we don't already have.
		final Iterator<Binding> lBindings = lActiveManagerBindings.iterator();
		while (lBindings.hasNext()) {
			final KeyBinding lBinding = (KeyBinding) lBindings.next();
			final ParameterizedCommand lParameterizedCommand = lBinding
					.getParameterizedCommand();
			lCmdsForBindings.add(lParameterizedCommand);
			if (!bindingToElement.containsKey(lBinding)) {
				final BindingElement lBindingElement = new BindingElement(
						getController());
				lBindingElement.init(lBinding, inContextModel);
				lBindingElement.setParent(this);
				bindingElements.add(lBindingElement);
				bindingToElement.put(lBinding, lBindingElement);
				getController().firePropertyChange(this, PROP_BINDING_ADD,
						null, lBindingElement);

				if (commandToElement.containsKey(lParameterizedCommand)
						&& lBindingElement.getUserDelta().intValue() == Binding.SYSTEM) {
					final Object lRemove = commandToElement
							.remove(lParameterizedCommand);
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
			final BindingElement lBindingElement = lBindingElements.next();
			final Object lObj = lBindingElement.getModelObject();
			if (lObj instanceof Binding) {
				final Binding lBinding = (Binding) lObj;
				if (!lActiveManagerBindings.contains(lBinding)) {
					final ParameterizedCommand lCmd = lBinding
							.getParameterizedCommand();
					if (lCmd != null) {
						commandToElement.remove(lCmd);
					}
					bindingToElement.remove(lBinding);
					lBindingElements.remove();
					getController().firePropertyChange(this,
							PROP_BINDING_REMOVE, null, lBindingElement);
				}
			} else {
				lCmdsForBindings.add(lObj);
			}
		}

		// If we removed the last binding for a parameterized command,
		// put back the CMD
		final Iterator<ParameterizedCommand> lCommands = allParameterizedCommands
				.iterator();
		while (lCommands.hasNext()) {
			final ParameterizedCommand lCmd = lCommands.next();
			if (!lCmdsForBindings.contains(lCmd)) {
				final BindingElement lBindingElement = new BindingElement(
						getController());
				lBindingElement.init(lCmd);
				lBindingElement.setParent(this);
				bindingElements.add(lBindingElement);
				commandToElement.put(lCmd, lBindingElement);
				getController().firePropertyChange(this, PROP_BINDING_ADD,
						null, lBindingElement);
			}
		}
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
	 * @param inBindingElement
	 *            {@link BindingElement}
	 */
	public void remove(final BindingElement inBindingElement) {
		if (inBindingElement == null
				|| !(inBindingElement.getModelObject() instanceof Binding)) {
			return;
		}

		final KeyBinding lKeyBinding = (KeyBinding) inBindingElement
				.getModelObject();
		if (lKeyBinding.getType() == Binding.USER) {
			bindingManager.removeBinding(lKeyBinding);
		} else {
			final KeySequence lKeySequence = lKeyBinding.getKeySequence();

			// Add the deleted binding
			bindingManager.addBinding(new KeyBinding(lKeySequence, null,
					lKeyBinding.getSchemeId(), lKeyBinding.getContextId(),
					null, null, null, Binding.USER));

			// Unbind any conflicts affected by the deleted binding
			final ConflictModel lConflictModel = getController()
					.getConflictModel();
			lConflictModel.updateConflictsFor(inBindingElement);
			final Collection<BindingElement> lConflictsList = lConflictModel
					.getConflicts();
			if (lConflictsList != null) {
				final Object[] lConflicts = lConflictsList.toArray();
				for (int i = 0; i < lConflicts.length; i++) {
					final BindingElement lBindingElement = (BindingElement) lConflicts[i];
					if (lBindingElement == inBindingElement) {
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
		final ParameterizedCommand lParameterizedCommand = lKeyBinding
				.getParameterizedCommand();
		inBindingElement.fill(lParameterizedCommand);
		commandToElement.put(lParameterizedCommand, inBindingElement);
		getController().firePropertyChange(this, PROP_CONFLICT_ELEMENT_MAP,
				null, inBindingElement);
	}

	/**
	 * Restores the specified BindingElement. A refresh should be performed
	 * afterwards. The refresh may be done after several elements have been
	 * restored.
	 * 
	 * @param inElement
	 */
	public void restoreBinding(final BindingElement inElement) {
		if (inElement == null) {
			return;
		}

		final Object lModelObject = inElement.getModelObject();
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
				if (deletes(lDelete, lSys) && lDelete.getType() == Binding.USER) {
					bindingManager.removeBinding(lDelete);
				}
			}
		}

		setSelectedElement(null);

		bindingElements.remove(inElement);
		bindingToElement.remove(lModelObject);
		commandToElement.remove(lModelObject);
		getController().firePropertyChange(this, PROP_BINDING_REMOVE, null,
				inElement);
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

	/**
	 * @param inBindings
	 *            HashSet&lt;BindingElement> The bindings to set.
	 */
	public void setBindings(final HashSet<BindingElement> inBindings) {
		final Set<BindingElement> lOld = bindingElements;
		bindingElements = inBindings;
		getController().firePropertyChange(this, PROP_BINDINGS, lOld,
				inBindings);
	}

	/**
	 * @param inBindingToElement
	 *            Map&lt;Binding, BindingElement> The bindingToElement to set.
	 */
	public void setBindingToElement(
			final Map<Binding, BindingElement> inBindingToElement) {
		final Map<Binding, BindingElement> lOld = bindingToElement;
		bindingToElement = inBindingToElement;
		getController().firePropertyChange(this, PROP_BINDING_ELEMENT_MAP,
				lOld, inBindingToElement);
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
	 * @param inElement
	 *            {@link BindingElement}
	 */
	public void copy(final BindingElement inElement) {
		if (inElement == null
				|| !(inElement.getModelObject() instanceof Binding)) {
			return;
		}
		final BindingElement lBindingElement = new BindingElement(
				getController());
		final ParameterizedCommand lParameterizedCommand = ((Binding) inElement
				.getModelObject()).getParameterizedCommand();
		lBindingElement.init(lParameterizedCommand);
		lBindingElement.setParent(this);
		bindingElements.add(lBindingElement);
		commandToElement.put(lParameterizedCommand, lBindingElement);
		getController().firePropertyChange(this, PROP_BINDING_ADD, null,
				lBindingElement);
		setSelectedElement(lBindingElement);
	}

	// --- static method ---

	final static boolean deletes(final Binding inDelete, final Binding inBinding) {
		boolean outDeletes = true;
		outDeletes &= Util.equals(inDelete.getContextId(),
				inBinding.getContextId());
		outDeletes &= Util.equals(inDelete.getTriggerSequence(),
				inBinding.getTriggerSequence());
		if (inDelete.getLocale() != null) {
			outDeletes &= Util.equals(inDelete.getLocale(),
					inBinding.getLocale());
		}
		if (inDelete.getPlatform() != null) {
			outDeletes &= Util.equals(inDelete.getPlatform(),
					inBinding.getPlatform());
		}
		outDeletes &= (inBinding.getType() == Binding.SYSTEM);
		outDeletes &= Util.equals(inDelete.getParameterizedCommand(), null);

		return outDeletes;
	}

}
