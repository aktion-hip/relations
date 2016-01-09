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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.elbe.relations.internal.e4.keys.KeyController;

/**
 * Model for key binding conflicts.
 *
 * @author lbenno
 */
public class ConflictModel extends CommonModel {
	public static final String PROP_CONFLICTS = "conflicts"; //$NON-NLS-1$
	public static final String PROP_CONFLICTS_ADD = "conflictsAdd"; //$NON-NLS-1$
	public static final String PROP_CONFLICTS_REMOVE = "conflictsRemove"; //$NON-NLS-1$

	/**
	 * The set of conflicts for the currently selected element.
	 */
	private Collection<BindingElement> conflicts;

	/**
	 * A mapping of binding element to known conflicts.
	 */
	private Map<BindingElement, Collection<BindingElement>> conflictsMap;

	private BindingModel bindingModel;
	private BindingManager bindingManager;

	/**
	 * ConflictModel constructor.
	 *
	 * @param keyController
	 *            {@link KeyController}
	 */
	public ConflictModel(KeyController keyController) {
		super(keyController);
	}

	/**
	 * Method to be called for initializing the conflict model instance.
	 *
	 * @param bindingModel
	 *            {@link BindingModel}
	 * @param bindingManager
	 *            {@link BindingManager}
	 */
	public void init(BindingModel bindingModel, BindingManager bindingManager) {
		this.bindingModel = bindingModel;
		this.bindingManager = bindingManager;

		conflictsMap = new HashMap<BindingElement, Collection<BindingElement>>();
		final Iterator<BindingElement> lBindings = bindingModel.getBindings()
		        .iterator();
		while (lBindings.hasNext()) {
			final BindingElement lBinding = lBindings.next();
			if (lBinding.getModelObject() instanceof Binding) {
				updateConflictsFor(lBinding);
			}
		}
		getController()
		        .addPropertyChangeListener(new IPropertyChangeListener() {
			        @Override
			        public void propertyChange(
		                    final PropertyChangeEvent inEvent) {
				        if (inEvent.getSource() == ConflictModel.this
		                        && CommonModel.PROP_SELECTED_ELEMENT
		                                .equals(inEvent.getProperty())) {
					        if (inEvent.getNewValue() != null) {
						        updateConflictsFor(
		                                (BindingElement) inEvent.getOldValue(),
		                                (BindingElement) inEvent.getNewValue());
						        setConflicts(conflictsMap
		                                .get(inEvent.getNewValue()));
					        } else {
						        setConflicts(null);
					        }
				        } else if (BindingModel.PROP_BINDING_REMOVE
		                        .equals(inEvent.getProperty())) {
					        updateConflictsFor(
		                            (BindingElement) inEvent.getOldValue(),
		                            (BindingElement) inEvent.getNewValue(),
		                            true);
				        }
			        }
		        });
	}

	/**
	 * @return Returns the conflicts.
	 */
	public Collection<BindingElement> getConflicts() {
		return conflicts;
	}

	/**
	 * Sets the conflicts to the given collection. Any conflicts in the
	 * collection that do not exist in the <code>bindingModel</code> are
	 * removed.
	 *
	 * @param inConflicts
	 *            The conflicts to set.
	 */
	public void setConflicts(final Collection<BindingElement> inConflicts) {
		final Object lOld = conflicts;
		conflicts = inConflicts;

		if (conflicts != null) {
			final Iterator<BindingElement> lConflictIter = conflicts.iterator();
			final Map<Binding, BindingElement> lBindingToElement = bindingModel
			        .getBindingToElement();
			while (lConflictIter.hasNext()) {
				final Object lNext = lConflictIter.next();
				if (!lBindingToElement.containsValue(lNext)
				        && !lNext.equals(getSelectedElement())) {
					lConflictIter.remove();
				}
			}
		}
		getController().firePropertyChange(this, PROP_CONFLICTS, lOld,
		        inConflicts);
	}

	/**
	 * Executes updates of conflicts.
	 *
	 * @param newValue
	 *            {@link BindingElement}
	 * @param oldTrigger
	 *            {@link TriggerSequence}
	 * @param newTrigger
	 *            {@link TriggerSequence}
	 * @param removal
	 *            boolean
	 */
	@SuppressWarnings("unchecked")
	public void updateConflictsFor(BindingElement newValue,
	        TriggerSequence oldTrigger, TriggerSequence newTrigger,
	        boolean removal) {
		// process conflict removal
		final Collection<BindingElement> lMatches = conflictsMap.get(newValue);
		if (lMatches != null) {
			if (newTrigger == null || removal) {
				conflictRemove(newValue, lMatches);
				return;
			} else if (oldTrigger != null && !newTrigger.equals(oldTrigger)) {
				conflictRemove(newValue, lMatches);
			} else {
				return;
			}
		}

		if (newValue.getTrigger() == null
		        || !(newValue.getModelObject() instanceof Binding)) {
			return;
		}

		// add new conflict
		final Binding lNewBinding = (Binding) newValue.getModelObject();
		final TriggerSequence lTrigger = lNewBinding.getTriggerSequence();
		final Collection<Binding> lMgrMatches = (Collection<Binding>) bindingManager
		        .getActiveBindingsDisregardingContext().get(lTrigger);
		final List<BindingElement> lLocalConflicts = new ArrayList<BindingElement>();
		if (lMgrMatches != null) {
			lLocalConflicts.add(newValue);
			final Iterator<Binding> lMatchIter = lMgrMatches.iterator();
			while (lMatchIter.hasNext()) {
				final Binding lBinding = lMatchIter.next();
				if (lNewBinding != lBinding
				        && lBinding.getContextId()
				                .equals(lNewBinding.getContextId())
				        && lBinding.getSchemeId()
				                .equals(lNewBinding.getSchemeId())) {
					final BindingElement lElement = bindingModel
					        .getBindingToElement().get(lBinding);
					if (lElement != null) {
						lLocalConflicts.add(lElement);
					}
				}
			}
		}

		if (lLocalConflicts.size() > 1) {
			// first find if it is already a conflict collection
			Collection<BindingElement> lKnownConflicts = null;
			Iterator<BindingElement> lConflictsIter = lLocalConflicts
			        .iterator();
			while (lConflictsIter.hasNext() && lKnownConflicts == null) {
				final BindingElement lBinding = lConflictsIter.next();
				lKnownConflicts = conflictsMap.get(lBinding);
			}
			if (lKnownConflicts != null) {
				lKnownConflicts.add(newValue);
				conflictsMap.put(newValue, lKnownConflicts);
				newValue.setConflict(Boolean.TRUE);
				if (lKnownConflicts == conflicts) {
					getController().firePropertyChange(this, PROP_CONFLICTS_ADD,
					        null, newValue);
				} else if (newValue == getSelectedElement()) {
					setConflicts(lKnownConflicts);
				}
				return;
			}
			boolean isSelected = false;
			lConflictsIter = lLocalConflicts.iterator();
			while (lConflictsIter.hasNext()) {
				final BindingElement lBinding = lConflictsIter.next();
				if (lBinding != null) {
					conflictsMap.put(lBinding, lLocalConflicts);
					lBinding.setConflict(Boolean.TRUE);
				}
				if (lBinding == getSelectedElement()) {
					isSelected = true;
				}
			}
			if (isSelected) {
				setConflicts(lLocalConflicts);
			}
		}
	}

	private void conflictRemove(final BindingElement newValue,
	        final Collection<BindingElement> matches) {
		// we need to clear this match
		matches.remove(newValue);
		conflictsMap.remove(newValue);
		if (matches == conflicts) {
			getController().firePropertyChange(this, PROP_CONFLICTS_REMOVE,
			        null, newValue);
		}
		if (matches.size() == 1) {
			final BindingElement lBinding = matches.iterator().next();
			conflictsMap.remove(lBinding);
			lBinding.setConflict(Boolean.FALSE);
			if (matches == conflicts) {
				setConflicts(null);
			}
		}
	}

	/**
	 * Executes updates of conflicts.
	 *
	 * @param source
	 *            {@link BindingElement}
	 */
	public void updateConflictsFor(final BindingElement source) {
		updateConflictsFor(source, false);
	}

	/**
	 * Executes updates of conflicts.
	 *
	 * @param oldValue
	 *            {@link BindingElement}
	 * @param newValue
	 *            {@link BindingElement}
	 */
	public void updateConflictsFor(final BindingElement oldValue,
	        final BindingElement newValue) {
		updateConflictsFor(oldValue, newValue, false);
	}

	/**
	 * Executes updates of conflicts.
	 *
	 * @param source
	 *            {@link BindingElement}
	 * @param removal
	 *            boolean
	 */
	public void updateConflictsFor(final BindingElement source,
	        final boolean removal) {
		updateConflictsFor(null, source, removal);
	}

	private void updateConflictsFor(final BindingElement oldValue,
	        final BindingElement newValue, final boolean removal) {
		updateConflictsFor(newValue,
		        oldValue == null ? null : oldValue.getTrigger(),
		        newValue == null ? null : newValue.getTrigger(), removal);
	}

}
