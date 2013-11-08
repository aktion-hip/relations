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
package org.elbe.relations.internal.services;

import java.io.IOException;
import java.net.MalformedURLException;

import org.elbe.relations.parsing.ParserException;
import org.elbe.relations.parsing.WebPageParser;
import org.elbe.relations.parsing.WebPageParser.WebDropResult;

/**
 * Interface for web page parser component. This interface defines an OSGi
 * declarative service.
 * 
 * @author Luthiger
 */
public interface IWebPageParser {

	/**
	 * Parses the web page specified by the provided url.
	 * 
	 * @param inUrl
	 *            String the web page's url.
	 * @return {@link WebDropResult} parameter object containing the relevant
	 *         information from the dropped web page
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParserException
	 */
	WebPageParser.WebDropResult parse(final String inUrl)
			throws MalformedURLException, IOException, ParserException;

}
