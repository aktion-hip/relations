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
package org.elbe.relations.internal.controls;

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.data.bom.ILightWeightItem;

/**
 * View to select person items.
 * 
 * @author Luthiger
 */
public class PersonView extends AbstractSelectionView {
	private static final String POPUP_ID = "org.elbe.relations.view.persons.popup"; //$NON-NLS-1$

	@Inject
	public PersonView(final Composite inParent) {
		super(inParent);
	}

	@Override
	protected WritableList getDBInput() {
		return new WritableList(getDataService().getPersons(),
				ILightWeightItem.class);
	}

	@Override
	protected String getPopupID() {
		return POPUP_ID;
	}
}
