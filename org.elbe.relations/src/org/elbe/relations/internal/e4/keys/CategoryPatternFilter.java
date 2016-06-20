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
package org.elbe.relations.internal.e4.keys;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.ui.workbench.swt.internal.copy.PatternFilter;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.internal.keys.model.BindingElement;

/**
 * Helper class.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
class CategoryPatternFilter extends PatternFilter {
	private boolean filterCategories;
	final Category uncategorized;

	/**
	 * CategoryPatternFilter constructor.
	 *
	 * @param filterCategories
	 *            boolean
	 * @param category
	 *            {@link Category}
	 */
	public CategoryPatternFilter(final boolean filterCategories,
	        final Category category) {
		uncategorized = category;
		filterCategories(filterCategories);
	}

	/**
	 * Sets the filtering flag.
	 *
	 * @param filterCategories
	 *            boolean
	 */
	public void filterCategories(final boolean filterCategories) {
		this.filterCategories = filterCategories;
		if (this.filterCategories) {
			setPattern("org.eclipse.ui.keys.optimization.false"); //$NON-NLS-1$
		} else {
			setPattern("org.eclipse.ui.keys.optimization.true"); //$NON-NLS-1$
		}
	}

	/**
	 *
	 * @return boolean
	 */
	public boolean isFilteringCategories() {
		return filterCategories;
	}

	@Override
	protected boolean isLeafMatch(final Viewer inViewer,
	        final Object inElement) {
		if (filterCategories) {
			final ParameterizedCommand inCmd = getCommand(inElement);
			try {
				if (inCmd != null
				        && inCmd.getCommand().getCategory() == uncategorized) {
					return false;
				}
			}
			catch (final NotDefinedException e) {
				return false;
			}
		}
		return super.isLeafMatch(inViewer, inElement);
	}

	private ParameterizedCommand getCommand(final Object inElement) {
		if (inElement instanceof BindingElement) {
			final Object lModelObject = ((BindingElement) inElement)
			        .getModelObject();
			if (lModelObject instanceof Binding) {
				return ((Binding) lModelObject).getParameterizedCommand();
			} else if (lModelObject instanceof ParameterizedCommand) {
				return (ParameterizedCommand) lModelObject;
			}
		}
		return null;
	}
}
