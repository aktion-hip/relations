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
package org.elbe.relations.internal.e4.wizards.util;

import org.eclipse.e4.ui.workbench.swt.internal.copy.PatternFilter;
import org.eclipse.jface.viewers.Viewer;

/**
 * A class that handles filtering wizard node items based on a supplied matching
 * string and keywords
 *
 * <br />
 * see org.eclipse.ui.internal.dialogs.WizardPatternFilter
 */
@SuppressWarnings("restriction")
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
	protected boolean isLeafMatch(final Viewer inViewer,
	        final Object inElement) {
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
				if (wordMatches(lKeywordLabels[i])) {
					return true;
				}
			}
		}
		return false;
	}

}
