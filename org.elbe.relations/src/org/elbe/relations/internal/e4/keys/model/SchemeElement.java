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

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.bindings.Scheme;
import org.elbe.relations.internal.e4.keys.KeyController;

/**
 * Model element for a binding scheme.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public class SchemeElement extends ModelElement {

	/**
	 * @param keyController
	 */
	public SchemeElement(KeyController keyController) {
		super(keyController);
	}

	/**
	 * Initializes this scheme instance.
	 *
	 * @param scheme
	 *            {@link Scheme}
	 * @param log
	 *            {@link Logger}
	 */
	public void init(final Scheme scheme, final Logger log) {
		setId(scheme.getId());
		setModelObject(scheme);
		try {
			setName(scheme.getName());
			setDescription(scheme.getDescription());
		}
		catch (final NotDefinedException exc) {
			log.error(exc, exc.getMessage());
		}
	}
}
