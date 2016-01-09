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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.elbe.relations.internal.e4.keys.KeyController;

/**
 * Model for a key binding context. <br />
 * The context model is the container of <code>ContextElement</code>s.
 *
 * @author lbenno
 */
public class ContextModel extends CommonModel {
	private static final String CONTEXT_ID_ACTION_SETS = "org.eclipse.ui.contexts.actionSet"; //$NON-NLS-1$
	private static final String CONTEXT_ID_INTERNAL = ".internal."; //$NON-NLS-1$

	public static final String PROP_CONTEXTS = "contexts"; //$NON-NLS-1$
	public static final String PROP_CONTEXT_MAP = "contextIdElementMap"; //$NON-NLS-1$

	private List<ContextElement> contexts;
	private Map<String, ContextElement> contextIdToFilteredContexts;
	private Map<String, ContextElement> contextIdToElement;

	/**
	 * @param controller
	 *            {@link KeyController}
	 */
	public ContextModel(KeyController controller) {
		super(controller);
	}

	/**
	 * Initializes the model.
	 *
	 * @param contexts
	 *            List&lt;MBindingContext>
	 * @return {@link ContextModel} the instance
	 */
	public ContextModel init(List<MBindingContext> contexts) {
		this.contexts = new ArrayList<ContextElement>();
		contextIdToFilteredContexts = new HashMap<String, ContextElement>();
		contextIdToElement = new HashMap<String, ContextElement>();

		for (final MBindingContext lContext : contexts) {
			createElement(lContext, null);
		}

		return this;
	}

	private void createElement(MBindingContext inContext,
	        ContextElement inParentContext) {
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
	 * in a {@link List} so they can be easily restored.
	 *
	 * @param inActionSets
	 *            <code>true</code> to filter action set contexts
	 * @param inInternal
	 *            <code>true</code> to filter internal contexts
	 * @return {@link ContextModel} the instance
	 */
	public ContextModel filterContexts(boolean inActionSets,
	        boolean inInternal) {
		// Remove undesired contexts
		for (int i = 0; i < contexts.size(); i++) {
			boolean removeContext = false;
			final ContextElement contextElement = contexts.get(i);

			if (inActionSets == true && contextElement.getId()
			        .equalsIgnoreCase(CONTEXT_ID_ACTION_SETS)) {
				removeContext = true;
			} else {
				String parentId = contextElement.getParentId();
				while (parentId != null) {
					if (parentId.equalsIgnoreCase(CONTEXT_ID_ACTION_SETS)) {
						removeContext = true;
					}
					final ContextElement parent = contextIdToElement
					        .get(parentId);
					parentId = parent == null ? null : parent.getParentId();
				}
			}

			if (inInternal == true && contextElement.getId()
			        .indexOf(CONTEXT_ID_INTERNAL) != -1) {
				removeContext = true;
			}

			if (removeContext) {
				contextIdToFilteredContexts.put(contextElement.getId(),
				        contextElement);
				contextIdToElement.remove(contextElement.getId());
			}
		}

		contexts.removeAll(contextIdToFilteredContexts.values());

		final Iterator<String> iterator = contextIdToFilteredContexts.keySet()
		        .iterator();
		// Restore desired contexts
		while (iterator.hasNext()) {
			boolean restoreContext = false;
			final ContextElement contextElement = contextIdToFilteredContexts
			        .get(iterator.next());

			if (inActionSets == false) {
				if (contextElement.getId()
				        .equalsIgnoreCase(CONTEXT_ID_ACTION_SETS)) {
					restoreContext = true;
				} else {
					final String parentId = contextElement.getParentId();
					if (parentId != null && parentId
					        .equalsIgnoreCase(CONTEXT_ID_ACTION_SETS)) {
						restoreContext = true;
					}
				}
			}
			if (inInternal == false && contextElement.getId()
			        .indexOf(CONTEXT_ID_INTERNAL) != -1) {
				restoreContext = true;
			}

			if (restoreContext) {
				contexts.add(contextElement);
				contextIdToElement.put(contextElement.getId(), contextElement);
				iterator.remove();
			}
		}

		return this;
	}

}
