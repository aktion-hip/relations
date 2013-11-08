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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.elbe.relations.internal.preferences.keys.KeyController;

/**
 * Model for a key binding context.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ContextModel extends CommonModel {
	private static final String CONTEXT_ID_ACTION_SETS = "org.eclipse.ui.contexts.actionSet"; //$NON-NLS-1$
	private static final String CONTEXT_ID_INTERNAL = ".internal."; //$NON-NLS-1$

	public static final String PROP_CONTEXTS = "contexts"; //$NON-NLS-1$
	public static final String PROP_CONTEXT_MAP = "contextIdElementMap"; //$NON-NLS-1$
	private List<ContextElement> contexts;
	private Map<String, ContextElement> contextIdToFilteredContexts;
	private Map<String, ContextElement> contextIdToElement;

	/**
	 * ContextModel constructor.
	 * 
	 * @param inKeyController
	 *            {@link KeyController}
	 */
	public ContextModel(final KeyController inKeyController) {
		super(inKeyController);
	}

	/**
	 * @param inContexts
	 */
	public void init(final List<MBindingContext> inContexts) {
		contexts = new ArrayList<ContextElement>();
		contextIdToFilteredContexts = new HashMap<String, ContextElement>();
		contextIdToElement = new HashMap<String, ContextElement>();

		for (final MBindingContext lContext : inContexts) {
			createElement(lContext, null);
		}
	}

	private void createElement(final MBindingContext inContext,
			final ContextElement inParentContext) {
		final ContextElement lElement = new ContextElement(getController());
		lElement.init(inContext);
		lElement.setParent(this);
		lElement.setParentContext(inParentContext);
		contexts.add(lElement);
		contextIdToElement.put(inContext.getElementId(), lElement);

		for (final MBindingContext lContext : inContext.getChildren()) {
			createElement(lContext, lElement);
		}
	}

	/**
	 * @return List<ContextElement>
	 */
	public List<ContextElement> getContexts() {
		return contexts;
	}

	/**
	 * @return Map&lt;String, ContextElement>
	 */
	public Map<String, ContextElement> getContextIdToElement() {
		return contextIdToElement;
	}

	/**
	 * Removes any contexts according to the parameters. The contexts are stored
	 * in a {@link List} to they can be easily restored.
	 * 
	 * @param inActionSets
	 *            <code>true</code> to filter action set contexts.
	 * @param inInternal
	 *            <code>true</code> to filter internal contexts
	 */
	public void filterContexts(final boolean inActionSets,
			final boolean inInternal) {
		// Remove undesired contexts
		for (int i = 0; i < contexts.size(); i++) {
			boolean lRemoveContext = false;
			final ContextElement lContextElement = contexts.get(i);

			if (inActionSets == true
					&& lContextElement.getId().equalsIgnoreCase(
							CONTEXT_ID_ACTION_SETS)) {
				lRemoveContext = true;
			} else {
				String lParentId = lContextElement.getParentId();
				while (lParentId != null) {
					if (lParentId.equalsIgnoreCase(CONTEXT_ID_ACTION_SETS)) {
						lRemoveContext = true;
					}
					final ContextElement lParent = contextIdToElement
							.get(lParentId);
					lParentId = lParent == null ? null : lParent.getParentId();
				}
			}

			if (inInternal == true
					&& lContextElement.getId().indexOf(CONTEXT_ID_INTERNAL) != -1) {
				lRemoveContext = true;
			}

			if (lRemoveContext) {
				contextIdToFilteredContexts.put(lContextElement.getId(),
						lContextElement);
				contextIdToElement.remove(lContextElement);
			}
		}

		contexts.removeAll(contextIdToFilteredContexts.values());

		final Iterator<String> lIterator = contextIdToFilteredContexts.keySet()
				.iterator();
		// Restore desired contexts
		while (lIterator.hasNext()) {
			boolean lRestoreContext = false;
			final ContextElement lContextElement = contextIdToFilteredContexts
					.get(lIterator.next());

			try {
				if (inActionSets == false) {
					if (lContextElement.getId().equalsIgnoreCase(
							CONTEXT_ID_ACTION_SETS)) {
						lRestoreContext = true;
					} else {
						final String lParentId = ((Context) lContextElement
								.getModelObject()).getParentId();
						if (lParentId != null
								&& lParentId
										.equalsIgnoreCase(CONTEXT_ID_ACTION_SETS)) {
							lRestoreContext = true;
						}
					}
				}
			}
			catch (final NotDefinedException e) {
				// No parentId to check
			}
			if (inInternal == false
					&& lContextElement.getId().indexOf(CONTEXT_ID_INTERNAL) != -1) {
				lRestoreContext = true;
			}

			if (lRestoreContext) {
				contexts.add(lContextElement);
				contextIdToElement
						.put(lContextElement.getId(), lContextElement);
				lIterator.remove();
			}
		}
	}

}
