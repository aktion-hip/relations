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
package org.elbe.relations.internal.parsing;

import org.elbe.relations.parsing.ExtractedData;
import org.elbe.relations.parsing.XPathHelper;
import org.htmlcleaner.XPatherException;

/**
 * Interface for html extractor classes.
 * 
 * @author Luthiger Created on 07.02.2010
 */
public interface IHtmlExtractor {

	/**
	 * Extracts the metadata from the web page passed by
	 * <code>XPathHelper</code>.
	 * 
	 * @param inXPathHelper
	 *            {@link XPathHelper} the web page.
	 * @param inTitle
	 *            String the web page's html title.
	 * @param inUrl
	 *            String the web page's url.
	 * @return {@link ExtractedData} the metadata extracted from the web page.
	 * @throws XPatherException
	 */
	ExtractedData extractData(XPathHelper inXPathHelper, String inTitle,
	        String inUrl) throws XPatherException;
}
