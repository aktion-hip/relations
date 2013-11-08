/*
This package is part of Relations application.
Copyright (C) 2010, Benno Luthiger

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
package org.elbe.relations.biblio.meta.internal.html;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.parsing.ParserException;
import org.elbe.relations.parsing.WebPageParser.WebDropResult;
import org.elbe.relations.parsing.XPathHelper;
import org.elbe.relations.services.IBibliographyProvider;
import org.elbe.relations.utility.NewTextAction;

/**
 * Class extracting metadata from a web page and bibliographical data formatted
 * as RDFa microformat.
 * 
 * @author Luthiger Created on 06.02.2010
 * @see IBibliographyProvider
 */
@SuppressWarnings("restriction")
public class HmtlRDFaProvider implements IBibliographyProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.ds.IBibliographyProvider#evaluate(org.elbe.relations
	 * .parsing.XPathHelper,
	 * org.elbe.relations.parsing.WebPageParser.WebDropResult)
	 */
	@Override
	public void evaluate(final XPathHelper inXPathHelper,
			final WebDropResult inWebDrop, final IEclipseContext inContext)
			throws ParserException {
		try {
			final NewTextAction lAction = RDFaExtractor.process(inXPathHelper,
					inWebDrop.getUrl(), inContext);
			if (lAction != null) {
				inWebDrop.setNewBiblioAction(lAction);
			}
		}
		catch (final Exception exc) {
			throw new ParserException(exc.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.ds.IBibliographyProvider#isMicroFormat()
	 */
	@Override
	public boolean isMicroFormat() {
		return true;
	}

}
