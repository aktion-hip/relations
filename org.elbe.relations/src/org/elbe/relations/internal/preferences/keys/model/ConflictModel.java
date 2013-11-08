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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.elbe.relations.internal.preferences.keys.KeyController;

/**
 * Model for key binding conflicts.
 * 
 * @author Luthiger
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
	 * @param inKeyController
	 *            {@link KeyController}
	 */
	public ConflictModel(final KeyController inKeyController) {
		super(inKeyController);
	}

	/**
	 * @param inModel
	 *            {@link BindingModel}
	 * @param inBindingManager
	 *            {@link BindingManager}
	 */
	public void init(final BindingModel inModel,
			final BindingManager inBindingManager) {
		bindingModel = inModel;
		bindingManager = inBindingManager;

		conflictsMap = new HashMap<BindingElement, Collection<BindingElement>>();
		final Iterator<BindingElement> lBindings = bindingModel.getBindings()
				.iterator();
		while (lBindings.hasNext()) {
			final BindingElement lBinding = lBindings.next();
			if (lBinding.getModelObject() instanceof Binding) {
				updateConflictsFor(lBinding);
			}
		}
		getController().addPropertyChangeListener(
				new IPropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent inEvent) {
						if (inEvent.getSource() == ConflictModel.this
								&& CommonModel.PROP_SELECTED_ELEMENT
										.equals(inEvent.getProperty())) {
							if (inEvent.getNewValue() != null) {
								updateConflictsFor(
										(BindingElement) inEvent.getOldValue(),
										(BindingElement) inEvent.getNewValue());
								setConflicts(conflictsMap.get(inEvent
										.getNewValue()));
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

	public void updateConflictsFor(final BindingElement inSource) {
		updateConflictsFor(inSource, false);
	}

	public void updateConflictsFor(final BindingElement inOldValue,
			final BindingElement inNewValue) {
		updateConflictsFor(inOldValue, inNewValue, false);
	}

	public void updateConflictsFor(final BindingElement inSource,
			final boolean inRemoval) {
		updateConflictsFor(null, inSource, inRemoval);
	}

	private void updateConflictsFor(final BindingElement inOldValue,
			final BindingElement inNewValue, final boolean inRemoval) {
		updateConflictsFor(inNewValue,
				inOldValue == null ? null : inOldValue.getTrigger(),
				inNewValue == null ? null : inNewValue.getTrigger(), inRemoval);
	}

	/**
	 * Executes updates of conflicts.
	 * 
	 * @param inNewValue
	 *            {@link BindingElement}
	 * @param inOldTrigger
	 *            {@link TriggerSequence}
	 * @param inNewTrigger
	 *            {@link TriggerSequence}
	 * @param inRemoval
	 *            boolean
	 */
	@SuppressWarnings("unchecked")
	public void updateConflictsFor(final BindingElement inNewValue,
			final TriggerSequence inOldTrigger,
			final TriggerSequence inNewTrigger, final boolean inRemoval) {
		// process conflict removal
		final Collection<BindingElement> lMatches = conflictsMap
				.get(inNewValue);
		if (lMatches != null) {
			if (inNewTrigger == null || inRemoval) {
				conflictRemove(inNewValue, lMatches);
				return;
			} else if (inOldTrigger != null
					&& !inNewTrigger.equals(inOldTrigger)) {
				conflictRemove(inNewValue, lMatches);
			} else {
				return;
			}
		}

		if (inNewValue.getTrigger() == null
				|| !(inNewValue.getModelObject() instanceof Binding)) {
			return;
		}

		// add new conflict
		final Binding lNewBinding = (Binding) inNewValue.getModelObject();
		final TriggerSequence lTrigger = lNewBinding.getTriggerSequence();
		final Collection<Binding> lMgrMatches = (Collection<Binding>) bindingManager
				.getActiveBindingsDisregardingContext().get(lTrigger);
		final ArrayList<BindingElement> lLocalConflicts = new ArrayList<BindingElement>();
		if (lMgrMatches != null) {
			lLocalConflicts.add(inNewValue);
			final Iterator<Binding> lMatchIter = lMgrMatches.iterator();
			while (lMatchIter.hasNext()) {
				final Binding lBinding = lMatchIter.next();
				if (lNewBinding != lBinding
						&& lBinding.getContextId().equals(
								lNewBinding.getContextId())
						&& lBinding.getSchemeId().equals(
								lNewBinding.getSchemeId())) {
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
				lKnownConflicts.add(inNewValue);
				conflictsMap.put(inNewValue, lKnownConflicts);
				inNewValue.setConflict(Boolean.TRUE);
				if (lKnownConflicts == conflicts) {
					getController().firePropertyChange(this,
							PROP_CONFLICTS_ADD, null, inNewValue);
				} else if (inNewValue == getSelectedElement()) {
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

	/**
	 * @param inNewValue
	 * @param inMatches
	 */
	private void conflictRemove(final BindingElement inNewValue,
			final Collection<BindingElement> inMatches) {
		// we need to clear this match
		inMatches.remove(inNewValue);
		conflictsMap.remove(inNewValue);
		if (inMatches == conflicts) {
			getController().firePropertyChange(this, PROP_CONFLICTS_REMOVE,
					null, inNewValue);
		}
		if (inMatches.size() == 1) {
			final BindingElement lBinding = inMatches.iterator().next();
			conflictsMap.remove(lBinding);
			lBinding.setConflict(Boolean.FALSE);
			if (inMatches == conflicts) {
				setConflicts(null);
			}
		}
	}

}
