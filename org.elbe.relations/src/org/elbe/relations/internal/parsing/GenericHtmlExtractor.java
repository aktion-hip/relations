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
 * <p>
 * Class for extracting basic html metadata elements. This extractor knows to
 * handle the following data:
 * </p>
 * 
 * <pre>
 * 	&lt;head>
 * 	&lt;title>Relations: Test&lt;/title>
 * 	&lt;meta name="description" content="This is a only a test" />
 * 	&lt;META NAME="author" CONTENT="Jane Doe" />
 * 	&lt;meta name="keywords" content="relations, test" />
 * 	&lt;meta name="date" content="2009-12-15T08:49" />
 * &lt;/head>
 * </pre>
 * 
 * @author Luthiger Created on 07.02.2010
 */
public class GenericHtmlExtractor extends AbstractHtmlExtractor implements
		IHtmlExtractor {
	private static final String META_XPATH_DESCRIPTION = "//head/meta[@name=\"description\"]"; //$NON-NLS-1$
	private static final String META_XPATH_AUTHOR = "//head/meta[@name=\"author\"]"; //$NON-NLS-1$
	private static final String META_XPATH_KEYWORDS = "//head/meta[@name=\"keywords\"]"; //$NON-NLS-1$
	private static final String META_XPATH_DATE = "//head/meta[@name=\"date\"]"; //$NON-NLS-1$

	@Override
	public ExtractedData extractData(final XPathHelper inXPathHelper,
			final String inTitle, final String inUrl) throws XPatherException {
		final String lAuthor = inXPathHelper.getAttribute(META_XPATH_AUTHOR,
				"content"); //$NON-NLS-1$
		final String lDate = inXPathHelper.getAttribute(META_XPATH_DATE,
				"content"); //$NON-NLS-1$
		final String lDescription = inXPathHelper.getAttribute(
				META_XPATH_DESCRIPTION, "content"); //$NON-NLS-1$
		final String lKeywords = inXPathHelper.getAttribute(
				META_XPATH_KEYWORDS, "content"); //$NON-NLS-1$

		final ExtractedData outExtracted = new ExtractedData();
		outExtracted.setTitle(inTitle);
		outExtracted.setURL(inUrl);
		if (hasValue(lAuthor)) {
			outExtracted.setAuthor(lAuthor);
		}
		handleDate(lDate, outExtracted);
		handleComment(lDescription, lKeywords, outExtracted);

		return outExtracted;
	}

}
