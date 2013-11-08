/*
This package is part of Relations application.
Copyright (C) 2009, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.elbe.relations.biblio.meta.internal.coins;

import java.io.UnsupportedEncodingException;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.parsing.ParserException;
import org.elbe.relations.parsing.WebPageParser.WebDropResult;
import org.elbe.relations.parsing.XPathHelper;
import org.elbe.relations.services.IBibliographyProvider;
import org.htmlcleaner.XPatherException;

/**
 * Class extracting bibliographical information provided through the COinS
 * (Context Object in Spans) API.
 * 
 * @author Luthiger Created on 20.11.2009
 */
@SuppressWarnings("restriction")
public class COinSProvider implements IBibliographyProvider {
	public static final String XPATH_COINS = "//span[@class='Z3988']"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "title"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.services.IBibliographyProvider#evaluate(org.elbe.relations
	 * .parsing.XPathHelper,
	 * org.elbe.relations.parsing.WebPageParser.WebDropResult)
	 */
	@Override
	public void evaluate(final XPathHelper inXPathHelper,
			final WebDropResult inWebDrop, final IEclipseContext inContext)
			throws ParserException {
		try {
			final String lBibliographyValue = inXPathHelper.getAttribute(
					XPATH_COINS, ATTRIBUTE_NAME);
			if (lBibliographyValue != null) {
				final COinSHelper lHelper = new COinSHelper(lBibliographyValue,
						inContext);
				inWebDrop.setNewBiblioAction(lHelper.getAction());
			}
		}
		catch (final UnsupportedEncodingException exc) {
			throw new ParserException(exc.getMessage());
		}
		catch (final XPatherException exc) {
			throw new ParserException(exc.getMessage());
		}
	}

	@Override
	public boolean isMicroFormat() {
		return true;
	}

}
