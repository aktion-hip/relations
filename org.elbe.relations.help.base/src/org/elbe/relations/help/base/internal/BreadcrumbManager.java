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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to manage a help plugin's breadcrumbs.
 * 
 * @author Luthiger
 */
public class BreadcrumbManager {
	private static final String DELIM = " &gt; ";
	private static final String APO_HTML = "&#39;";

	private final Map<String, String> breadcrumbMap = new HashMap<String, String>();

	/**
	 * Creates a breadcrumb for the specified model and registers it to the
	 * breadcrumb manager.
	 * 
	 * @param inTocModel
	 *            {@link ITocModel}
	 * @throws IOException
	 */
	public void registerBreadcrumb(final ITocModel inTocModel)
			throws IOException {
		final List<String> lBreadcrumbs = createBreadcrumbColl(inTocModel);
		if (!lBreadcrumbs.isEmpty()) {
			breadcrumbMap.put(inTocModel.getId(),
					constructBreadcrumb(lBreadcrumbs));
		}
	}

	private String constructBreadcrumb(final List<String> inBreadcrumbs) {
		final StringBuilder out = new StringBuilder();
		boolean isFirst = true;
		for (final String lItem : inBreadcrumbs) {
			if (!isFirst) {
				out.append(DELIM);
			}
			isFirst = false;
			out.append(lItem);
		}
		return new String(out).replaceAll("'", APO_HTML);
	}

	private List<String> createBreadcrumbColl(final ITocModel inTocModel)
			throws IOException {
		final List<String> out = new ArrayList<String>();
		ITocModel lBcItem = inTocModel.getParent();
		while (lBcItem != null) {
			final String lBreadcrumb = lBcItem.renderBreadcrumb();
			if (!lBreadcrumb.isEmpty()) {
				out.add(0, lBreadcrumb);
			}
			lBcItem = lBcItem.getParent();
		}
		return out;
	}

	/**
	 * @param inBreadcrumbId
	 *            String the pages breadcrumb id
	 * @return String the breadcrumb items (html) for the page with the
	 *         specified id
	 */
	public String getBreadcrumbs(final String inBreadcrumbId) {
		final String out = breadcrumbMap.get(inBreadcrumbId);
		return out == null ? "" : out;
	}

}
