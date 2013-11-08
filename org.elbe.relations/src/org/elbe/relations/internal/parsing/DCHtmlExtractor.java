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
 * Class for extracting DC metadata embedded in the html page. This extractor
 * knows to handle the following data:
 * </p>
 * 
 * <pre>
 * 	&lt;head profile="http://dublincore.org/documents/dcq-html/">
 * 	&lt;link rel="schema.DC" href="http://purl.org/dc/elements/1.1/" />
 * 	&lt;meta name="DC.title" content="Relations: Metadata" />
 * 	&lt;meta name="DC.creator" content="Benno Luthiger" />
 * 	&lt;meta name="DC.subject" content="Metadata" />
 * 	&lt;meta name="DC.description" content="This page is testing Dublin Core matadata." />
 * 	&lt;meta name="DC.publisher" content="Relations" />
 * 	&lt;meta name="DC.contributor" content="John Foo" />
 * 	&lt;meta name="DC.date" content="2010-12-15T08:49:37+02:00" scheme="DCTERMS.W3CDTF" />
 * 	&lt;meta name="DC.type" content="Text" scheme="DCTERMS.DCMIType" />
 * &lt;/head>
 * </pre>
 * 
 * @author Luthiger Created on 07.02.2010
 */
public class DCHtmlExtractor extends AbstractHtmlExtractor implements
		IHtmlExtractor {
	private static final String META_DC_REL = "//head/link[@rel=\"schema.DC\"]"; //$NON-NLS-1$
	private static final String META_DC_HREF = "http://purl.org/dc/elements/1.1/"; //$NON-NLS-1$

	private static final String META_XPATH_TITLE = "//head/meta[@name=\"DC.title\"]"; //$NON-NLS-1$
	private static final String META_XPATH_SUBJECT = "//head/meta[@name=\"DC.subject\"]"; //$NON-NLS-1$
	private static final String META_XPATH_DESCRIPTION = "//head/meta[@name=\"DC.description\"]"; //$NON-NLS-1$
	private static final String META_XPATH_CREATOR = "//head/meta[@name=\"DC.creator\"]"; //$NON-NLS-1$
	private static final String META_XPATH_PUBLISHER = "//head/meta[@name=\"DC.publisher\"]"; //$NON-NLS-1$
	private static final String META_XPATH_CONTRIBUTOR = "//head/meta[@name=\"DC.contributor\"]"; //$NON-NLS-1$
	private static final String META_XPATH_DATE = "//head/meta[@name=\"DC.date\"]"; //$NON-NLS-1$
	private static final String META_XPATH_TYPE = "//head/meta[@name=\"DC.type\"]"; //$NON-NLS-1$

	/**
	 * Checks the specified web page for DC metadata.
	 * 
	 * @param inXPathHelper
	 *            {@link XPathHelper}
	 * @return boolean <code>true</code> if the specified web page contains DC
	 *         metadata.
	 */
	public static boolean checkDCMeta(final XPathHelper inXPathHelper) {
		try {
			final String lHref = inXPathHelper
					.getAttribute(META_DC_REL, "href"); //$NON-NLS-1$
			return META_DC_HREF.equals(lHref);
		}
		catch (final XPatherException exc) {
			// intentionally left empty
			return false;
		}
	}

	@Override
	public ExtractedData extractData(final XPathHelper inXPathHelper,
			final String inTitle, final String inUrl) throws XPatherException {
		final String lTitle = inXPathHelper.getAttribute(META_XPATH_TITLE,
				"content"); //$NON-NLS-1$
		final String lSubject = inXPathHelper.getAttribute(META_XPATH_SUBJECT,
				"content"); //$NON-NLS-1$
		final String lDescription = inXPathHelper.getAttribute(
				META_XPATH_DESCRIPTION, "content"); //$NON-NLS-1$
		final String lCreator = inXPathHelper.getAttribute(META_XPATH_CREATOR,
				"content"); //$NON-NLS-1$
		final String lPublisher = inXPathHelper.getAttribute(
				META_XPATH_PUBLISHER, "content"); //$NON-NLS-1$
		final String lContributor = inXPathHelper.getAttribute(
				META_XPATH_CONTRIBUTOR, "content"); //$NON-NLS-1$
		final String lDate = inXPathHelper.getAttribute(META_XPATH_DATE,
				"content"); //$NON-NLS-1$
		final String lType = inXPathHelper.getAttribute(META_XPATH_TYPE,
				"content"); //$NON-NLS-1$

		final ExtractedData outExtracted = new ExtractedData();
		outExtracted.setTitle(hasValue(lTitle) ? lTitle : inTitle);
		outExtracted.setURL(inUrl);
		if (hasValue(lCreator)) {
			outExtracted.setAuthor(lCreator);
		}
		if (hasValue(lPublisher)) {
			outExtracted.setPublisher(lPublisher);
		}
		if (hasValue(lContributor)) {
			outExtracted.setContributor(lContributor);
		}
		if (hasValue(lType)) {
			outExtracted.setFileType(lType);
		}
		handleDate(lDate, outExtracted);
		handleComment(lSubject, lDescription, outExtracted);

		return outExtracted;
	}

}
