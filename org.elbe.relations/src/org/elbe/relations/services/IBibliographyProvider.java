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
package org.elbe.relations.services;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.parsing.ParserException;
import org.elbe.relations.parsing.WebPageParser;
import org.elbe.relations.parsing.WebPageParser.WebDropResult;
import org.elbe.relations.parsing.XPathHelper;

/**
 * Interface for classes that provide methods to extract bibliographical
 * information from a web page.
 * <p>
 * Classes implementing this interface have to be registered to the registry in
 * <code>WebPageParser</code>, i.e. the have to be listed in
 * <code>WebPageParser.registerProviders()</code>.
 * </p>
 * 
 * @author Luthiger
 * @see WebPageParser
 */
public interface IBibliographyProvider {

	/**
	 * Evaluates the parsed web page.
	 * 
	 * @param inXPathHelper
	 *            {@link XPathHelper} instance of the parser helper with the
	 *            parsed web page.
	 * @param inWebDrop
	 *            {@link WebDropResult} the parameter object where the
	 *            evaluation results can be stored into.
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @throws ParserException
	 */
	void evaluate(XPathHelper inXPathHelper, WebDropResult inWebDrop,
	        IEclipseContext inContext) throws ParserException;

	/**
	 * Is the provided bibliographical information a micro data format?
	 * 
	 * @return boolean <code>true</code> if the bibliographical metadata is
	 *         embedded in the web page.
	 */
	boolean isMicroFormat();

}
