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
package org.elbe.relations.data.search;

/**
 * Helper class to create a full text search field gathering the content of
 * various fields.
 * 
 * @author Luthiger Created on 14.11.2006
 */
public class FullTextHelper {
	StringBuilder fullText;

	public FullTextHelper() {
		fullText = new StringBuilder();
	}

	public String add(final String inSearch) {
		fullText.append(inSearch).append(" "); //$NON-NLS-1$
		return inSearch;
	}

	public String getFullText() {
		return new String(fullText);
	}

}
