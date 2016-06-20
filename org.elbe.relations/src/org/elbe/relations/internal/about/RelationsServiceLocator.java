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
package org.elbe.relations.internal.about;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.services.IServiceLocator;

/**
 * <code>IServiceLocator</code> implementation for the about dialog.
 *
 * @author lbenno
 */
public class RelationsServiceLocator implements IServiceLocator {

	private RelationsServiceLocator() {
		// prevent public instantiation
	}

	@Override
	public <T> T getService(Class<T> api) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasService(Class<?> api) {
		// TODO Auto-generated method stub
		return false;
	}

	// ---

	/**
	 * Factory method.
	 *
	 * @return {@link IServiceLocator} a RelationsServiceLocator instance.
	 */
	public static IServiceLocator createServiceLocator(
	        IEclipseContext context) {
		return new RelationsServiceLocator();
	}

}
