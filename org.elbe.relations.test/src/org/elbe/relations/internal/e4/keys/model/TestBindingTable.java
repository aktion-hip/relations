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
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;

/**
 * <code>MBindingTable</code> for testing purpose.
 *
 * @author lbenno
 */
public class TestBindingTable implements MBindingTable {
	private String elementId;
	private ArrayList<MKeyBinding> bindings;
	private MBindingContext bindingContext;

	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(String value) {
		elementId = value;
	}

	@Override
	public List<MKeyBinding> getBindings() {
		if (bindings == null) {
			bindings = new ArrayList<MKeyBinding>();
		}
		return bindings;
	}

	@Override
	public MBindingContext getBindingContext() {
		return bindingContext;
	}

	@Override
	public void setBindingContext(MBindingContext value) {
		bindingContext = value;
	}

	public void addBinding(MKeyBinding binding) {
		getBindings().add(binding);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.e4.ui.model.application.MApplicationElement#getPersistedState
	 * ()
	 */
	@Override
	public Map<String, String> getPersistedState() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement#getTags()
	 */
	@Override
	public List<String> getTags() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.e4.ui.model.application.MApplicationElement#getContributorURI
	 * ()
	 */
	@Override
	public String getContributorURI() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.e4.ui.model.application.MApplicationElement#setContributorURI
	 * (java.lang.String)
	 */
	@Override
	public void setContributorURI(String value) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.e4.ui.model.application.MApplicationElement#getTransientData(
	 * )
	 */
	@Override
	public Map<String, Object> getTransientData() {
		// TODO Auto-generated method stub
		return null;
	}

}
