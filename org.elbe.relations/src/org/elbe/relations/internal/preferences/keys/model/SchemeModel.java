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
package org.elbe.relations.internal.preferences.keys.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.elbe.relations.internal.preferences.keys.KeyController;

/**
 * Model for a binding scheme.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class SchemeModel extends CommonModel {
	public static final String PROP_SCHEMES = "schemes"; //$NON-NLS-1$

	private List<SchemeElement> schemes;

	/**
	 * @param inKeyController
	 */
	public SchemeModel(final KeyController inKeyController) {
		super(inKeyController);
	}

	/**
	 * @param inBindingManager
	 *            {@link BindingManager}
	 * @param inLog
	 */
	public void init(final BindingManager inBindingManager, final Logger inLog) {
		schemes = new ArrayList<SchemeElement>();
		final Scheme[] lDefinedSchemes = inBindingManager.getDefinedSchemes();
		for (int i = 0; i < lDefinedSchemes.length; i++) {
			final SchemeElement lElement = new SchemeElement(getController());
			lElement.init(lDefinedSchemes[i], inLog);
			lElement.setParent(this);
			schemes.add(lElement);
			if (lDefinedSchemes[i] == inBindingManager.getActiveScheme()) {
				setSelectedElement(lElement);
			}
		}
	}

	/**
	 * @return List<SchemeElement> the schemes
	 */
	public List<SchemeElement> getSchemes() {
		return schemes;
	}

	/**
	 * @param inSchemes
	 *            List<SchemeElement> the schemes to set
	 */
	public void setSchemes(final List<SchemeElement> inSchemes) {
		final List<SchemeElement> lOld = schemes;
		schemes = inSchemes;
		getController().firePropertyChange(this, PROP_SCHEMES, lOld, inSchemes);
	}

}
