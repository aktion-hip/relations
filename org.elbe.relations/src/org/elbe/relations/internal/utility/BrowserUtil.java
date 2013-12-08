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

package org.elbe.relations.internal.utility;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.PartInitException;

/**
 * Helper class to start browser.
 * 
 * @author lbenno
 */
public final class BrowserUtil {
	public static final String PREFIX_HTTP = "http://"; //$NON-NLS-1$ 
	private static final String PREFIX_HTTPS = "https://"; //$NON-NLS-1$

	/**
	 * Fires up the default browser and calls the specified URL.
	 * 
	 * @param inURL
	 *            String
	 * @throws PartInitException
	 * @throws MalformedURLException
	 */
	public static void startBrowser(final String inURL)
	        throws PartInitException, MalformedURLException {
		new BrowserSupport().openURL(new URL(inURL));
	}

	/**
	 * Checks whether the specified text starts with <code>http</code> or
	 * <code>https</code>.
	 * 
	 * @param inToCheck
	 * @return boolean <code>true</code> if the specified text starts with
	 *         <code>http</code> or <code>https</code>
	 */
	public static boolean textIsURL(final String inToCheck) {
		return inToCheck.startsWith(PREFIX_HTTP)
		        || inToCheck.startsWith(PREFIX_HTTPS);
	}
}
