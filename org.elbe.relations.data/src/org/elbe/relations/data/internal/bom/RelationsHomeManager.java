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
package org.elbe.relations.data.internal.bom;

import java.util.HashMap;
import java.util.Map;

import org.hip.kernel.bom.Home;
import org.hip.kernel.bom.HomeManager;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;

/**
 * Home manager of the Relations application.
 * 
 * @author Luthiger
 */
public enum RelationsHomeManager implements HomeManager {
	INSTANCE;

	private Map<String, Home> loadedHomes = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hip.kernel.bom.HomeManager#getHome(java.lang.String)
	 */
	@Override
	public Home getHome(final String inHomeName) {

		// Pre: inHomeName not null
		if (VSys.assertNotNull(this, "getHome", inHomeName) == Assert.FAILURE) { //$NON-NLS-1$
			return null;
		}

		// No loaded found. We try to create
		try {
			synchronized (this) {
				// We try to find a loaded home
				Home outHome = null;
				outHome = loadedHomes().get(inHomeName);
				if (outHome != null) {
					return outHome;
				}

				// find class
				final Class<?> lClass = Class.forName(inHomeName);
				// Create new instance
				outHome = (Home) lClass.newInstance();
				// Add to loadedHomes
				loadedHomes().put(inHomeName, outHome);
				return outHome;
			}

			// Handling various exceptions
		}
		catch (final NoClassDefFoundError err) {
			DefaultExceptionWriter.printOut(this, err, true);
			return null;
		}
		catch (final ClassNotFoundException exc) {
			DefaultExceptionWriter.printOut(this, exc, true);
			return null;
		}
		catch (final InstantiationException exc) {
			DefaultExceptionWriter.printOut(this, exc, true);
			return null;
		}
		catch (final IllegalAccessException exc) {
			DefaultExceptionWriter.printOut(this, exc, true);
			return null;
		}
	}

	/**
	 * @return {@link Map}
	 */
	private Map<String, Home> loadedHomes() {
		if (loadedHomes == null) {
			loadedHomes = new HashMap<String, Home>(67);
		}
		return loadedHomes;
	}

}
