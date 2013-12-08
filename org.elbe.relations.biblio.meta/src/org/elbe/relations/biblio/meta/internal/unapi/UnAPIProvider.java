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
package org.elbe.relations.biblio.meta.internal.unapi;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.parsing.ParserException;
import org.elbe.relations.parsing.WebPageParser;
import org.elbe.relations.parsing.XPathHelper;
import org.elbe.relations.services.IBibliographyProvider;

/**
 * Class extracting bibliographical information provided through unAPI.
 * 
 * @author Luthiger Created on 29.12.2009
 */
public class UnAPIProvider implements IBibliographyProvider {
	public static final String UNAPI_SERVER = "//link[@rel='unapi-server'][@title='unAPI']"; //$NON-NLS-1$
	public static final String UNAPI_ENTRY_ID = "//abbr[@class='unapi-id']"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_SERVER = "href"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_ENTRY_ID = "title"; //$NON-NLS-1$

	@Override
	public void evaluate(final XPathHelper inXPathHelper,
	        final WebPageParser.WebDropResult inWebDrop,
	        final IEclipseContext inContext) throws ParserException {
		try {
			final String lServerName = inXPathHelper.getAttribute(UNAPI_SERVER,
			        ATTRIBUTE_NAME_SERVER);
			final String lEntryID = inXPathHelper.getAttribute(UNAPI_ENTRY_ID,
			        ATTRIBUTE_NAME_ENTRY_ID);
			if (lServerName != null && lEntryID != null) {
				final UnAPIHelper lHelper = new UnAPIHelper(lServerName,
				        lEntryID);
				inWebDrop.setNewBiblioAction(lHelper.getAction(inContext));
			}
		}
		catch (final Exception exc) {
			throw new ParserException(exc.getMessage());
		}
	}

	@Override
	public boolean isMicroFormat() {
		return false;
	}

}
