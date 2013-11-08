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
package org.elbe.relations.help.base.internal;

import org.elbe.relations.help.base.Constants;

/**
 * 
 * @author Luthiger
 */
public final class HelpUtil {

	private HelpUtil() {
	}

	/**
	 * Retrieves the help page id from the hepl page's url.
	 * 
	 * @param inNamespace
	 *            String the help bundle's namespace (i.e. the bundle's symbolic
	 *            name)
	 * @param inUrl
	 *            String the help page url
	 * @return String the help page's id (that can be used to identify the
	 *         page's breadcrumb)
	 */
	public static String getBreadcrumbId(final String inNamespace,
			final String inUrl) {
		final int lPos = inUrl.lastIndexOf(Constants.PATH_DELIM + inNamespace
				+ Constants.PATH_DELIM);
		return inUrl.substring(lPos + inNamespace.length() + 2);
	}

}
