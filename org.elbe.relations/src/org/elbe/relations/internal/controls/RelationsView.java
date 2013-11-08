/*
This package is part of Relations application.
Copyright (C) 2011, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.elbe.relations.internal.controls;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Luthiger
 *
 */
public class RelationsView {

	@Inject
	public RelationsView(Composite inParent) {
		final GridLayout lLayout = new GridLayout(2, false);
		inParent.setLayout(lLayout);
		
		createLabel(inParent, "Relations test ...");
	}

	private void createLabel(Composite inParent, String inText) {
		Label lLabel = new Label(inParent, SWT.NONE);
		lLabel.setText(inText);
		GridData lGrid = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		lGrid.horizontalIndent = 20;
		lLabel.setLayoutData(lGrid);
	}
	
}
