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
package org.elbe.relations;

import java.net.URL;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The Relations bundle activator.
 * 
 * @author Luthiger
 */
public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(final BundleContext inBundleContext) throws Exception {
		Activator.context = inBundleContext;
	}

	@Override
	public void stop(final BundleContext inBundleContext) throws Exception {
		Activator.context = null;
	}

	/**
	 * @return String this bundle's name
	 */
	public static String getSymbolicName() {
		return context.getBundle().getSymbolicName();
	}

	/**
	 * Retrieves the entry with the specified path within this bundle.
	 * 
	 * @param inPath
	 *            String
	 * @return URL the entry
	 */
	public static URL getEntry(final String inPath) {
		return context.getBundle().getEntry(inPath);
	}

}
