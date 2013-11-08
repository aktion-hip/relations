package org.elbe.relations.internal.preferences.keys;

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

	public CategoryPatternFilter(final boolean inFilterCategories,
			final Category inCategory) {
		uncategorized = inCategory;
		filterCategories(inFilterCategories);
	}

	public void filterCategories(final boolean inFilterCategories) {
		filterCategories = inFilterCategories;
		if (filterCategories) {
			setPattern("org.eclipse.ui.keys.optimization.false"); //$NON-NLS-1$
		} else {
			setPattern("org.eclipse.ui.keys.optimization.true"); //$NON-NLS-1$
		}
	}

	public boolean isFilteringCategories() {
		return filterCategories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.PatternFilter#isLeafMatch(org.eclipse.jface.viewers
	 * .Viewer, java.lang.Object)
	 */
	@Override
	protected boolean isLeafMatch(final Viewer inViewer, final Object inElement) {
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
