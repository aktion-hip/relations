package org.elbe.relations.internal.wizards.e4.util;

import org.eclipse.e4.ui.workbench.swt.internal.copy.PatternFilter;
import org.eclipse.jface.viewers.Viewer;

/**
 * A class that handles filtering wizard node items based on a supplied matching
 * string and keywords
 * 
 * @since 3.2
 * @see org.eclipse.ui.internal.dialogs.WizardPatternFilter
 */
public class WizardPatternFilter extends PatternFilter {

	/**
	 * Create a new instance of a WizardPatternFilter
	 * 
	 * @param isMatchItem
	 */
	public WizardPatternFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java
	 * .lang.Object)
	 */
	@Override
	public boolean isElementSelectable(final Object inElement) {
		return inElement instanceof WorkbenchWizardElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.dialogs.PatternFilter#isElementMatch(org.eclipse
	 * .jface.viewers.Viewer, java.lang.Object)
	 */
	@Override
	protected boolean isLeafMatch(final Viewer inViewer, final Object inElement) {
		if (inElement instanceof WizardCollectionElement) {
			return false;
		}

		if (inElement instanceof WorkbenchWizardElement) {
			final WorkbenchWizardElement lDescription = (WorkbenchWizardElement) inElement;
			final String lText = lDescription.getLabel();
			if (wordMatches(lText)) {
				return true;
			}

			final String[] lKeywordLabels = lDescription.getKeywordLabels();
			for (int i = 0; i < lKeywordLabels.length; i++) {
				if (wordMatches(lKeywordLabels[i]))
					return true;
			}
		}
		return false;
	}

}
