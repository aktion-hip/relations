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
package org.elbe.relations.internal.utility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Helper class providing utility methods for wizard pages.
 * 
 * @author Luthiger Created on 15.01.2007
 */
public class WizardHelper {

	/**
	 * Creates a composite using a <code>GridLayout</code> with the specified
	 * number of columns.
	 * 
	 * @param inParent
	 *            Composite
	 * @param inColumns
	 *            int
	 * @return Composite
	 */
	public static Composite createComposite(final Composite inParent,
			final int inColumns) {
		final GridLayout lLayout = new GridLayout();
		lLayout.numColumns = inColumns;
		lLayout.marginHeight = 0;
		lLayout.marginWidth = 0;

		final Composite outComposite = new Composite(inParent, SWT.NONE);
		outComposite.setLayout(lLayout);
		return outComposite;
	}

}
