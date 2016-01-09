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
import org.eclipse.emf.ecore.xml.type.impl.SimpleAnyTypeImpl;

/**
 * <code>MBindingContext</code> for testing purpose.
 *
 * @author lbenno
 */
public class TestBindingContext extends SimpleAnyTypeImpl implements MBindingContext {

	private String elementId;
	private String name;
	private String description;
	protected List<MBindingContext> children;

	@Override
	public String getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(String value) {
		elementId = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String value) {
		name = value;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String value) {
		description = value;
	}

	@Override
	public List<MBindingContext> getChildren() {
		if (children == null) {
			children = new ArrayList<MBindingContext>();
		}
		return children;
	}

	public void addChild(MBindingContext child) {
		getChildren().add(child);
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

	// ---

	public static final String[] CONTEXT_IDS = { "1.1", "1.2", "2.1", "2.2", "1", "2" };

	/**
	 * Convenience method for testing purpose.
	 *
	 * @return List<MBindingContext>
	 */
	public static List<MBindingContext> createContexts() {
		final MBindingContext child11 = createBindingContext(CONTEXT_IDS[0]);
		final MBindingContext child12 = createBindingContext(CONTEXT_IDS[1]);
		final MBindingContext child21 = createBindingContext(CONTEXT_IDS[2]);
		final MBindingContext child22 = createBindingContext(CONTEXT_IDS[3]);
		final MBindingContext child1 = createBindingContext(CONTEXT_IDS[4], child11, child12);
		final MBindingContext child2 = createBindingContext(CONTEXT_IDS[5], child21, child22);

		final List<MBindingContext> contexts = new ArrayList<MBindingContext>();
		contexts.add(child1);
		contexts.add(child2);
		return contexts;
	}

	private static MBindingContext createBindingContext(String elementId, MBindingContext... children) {
		final TestBindingContext out = (TestBindingContext) createBindingContext(elementId);
		for (final MBindingContext child : children) {
			out.addChild(child);
		}
		return out;
	}

	/**
	 * Creates an instance of <code>MBindingContext</code> with the specified
	 * ID.
	 *
	 * @param elementId
	 *            String
	 * @return {@link MBindingContext}
	 */
	public static MBindingContext createBindingContext(String elementId) {
		final TestBindingContext out = new TestBindingContext();
		out.setElementId(elementId);
		return out;
	}

}
