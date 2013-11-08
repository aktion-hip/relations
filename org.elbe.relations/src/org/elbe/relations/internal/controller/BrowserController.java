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
package org.elbe.relations.internal.controller;

import java.util.ArrayList;
import java.util.Collection;

import org.elbe.relations.services.IBrowserController;
import org.elbe.relations.services.IRelationsBrowser;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The controller for the registered instances of
 * <code>IBrowserController</code>.
 * 
 * @author Luthiger
 */
public class BrowserController {
	private final Collection<BrowserInfo> browsers = new ArrayList<BrowserInfo>();

	/**
	 * OSGi bind.
	 * 
	 * @param inBrowser
	 *            {@link IBrowserController}
	 */
	public void register(final IBrowserController inBrowser) {
		browsers.add(new BrowserInfo(inBrowser));
	}

	/**
	 * OSGi unbind.
	 * 
	 * @param inBrowser
	 *            {@link IBrowserController}
	 */
	public void unregister(final IBrowserController inBrowser) {
		browsers.remove(new BrowserInfo(inBrowser));
	}

	// ---

	/**
	 * @return Collection&lt;BrowserInfo> the collection of browser information
	 */
	public Collection<BrowserInfo> getBrowserInfos() {
		return browsers;
	}

	// ---

	public static class BrowserInfo {
		private final Class<? extends IRelationsBrowser> browserClass;
		private String name;

		private BrowserInfo(final IBrowserController inBrowser) {
			browserClass = inBrowser.getBrowserClass();
			final Bundle lBundle = FrameworkUtil.getBundle(browserClass);
			name = lBundle.getHeaders().get("Bundle-Name");
			name = name == null ? lBundle.getSymbolicName() : name;

		}

		/**
		 * @return String the browser's name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return Class&lt;? extends IRelationsBrowser> the relations browser
		 *         provided by the bundle
		 */
		public Class<? extends IRelationsBrowser> getBrowser() {
			return browserClass;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((browserClass == null) ? 0 : browserClass.getName()
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final BrowserInfo other = (BrowserInfo) obj;
			if (browserClass == null) {
				if (other.browserClass != null)
					return false;
			} else if (!browserClass.getName().equals(
					other.browserClass.getName()))
				return false;
			return true;
		}
	}

}
