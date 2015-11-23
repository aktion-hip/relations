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
package org.elbe.relations.parsing;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CompactXmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Helper class for html/xhtml parsing and XPath processing.
 * <p>
 * Typical usage is the following:
 * </p>
 * <xmp> XPathHelper helper = XPathHelper.newInstance(url); String value =
 * helper.getElement("//some/expression"); </xmp>
 * 
 * @author Luthiger Created on 16.11.2009
 */
public class XPathHelper {
	public static final String XPATH_TITLE = "//head/title"; //$NON-NLS-1$
	public static final String DEFAULT_CHARSET = "UTF-8"; //$NON-NLS-1$

	private static final String NS_XML = "xmlns:xml"; //$NON-NLS-1$

	private final TagNode docNode;

	public enum XmlSerializer {
		SIMPLE(new ISerializerFactory() {
			@Override
			public org.htmlcleaner.XmlSerializer createSerializer(
		            final CleanerProperties inProperties) {
				return new SimpleXmlSerializer(inProperties);
			}
		}), PRETTY(new ISerializerFactory() {
			@Override
			public org.htmlcleaner.XmlSerializer createSerializer(
		            final CleanerProperties inProperties) {
				return new PrettyXmlSerializer(inProperties);
			}
		}), COMPACT(new ISerializerFactory() {
			@Override
			public org.htmlcleaner.XmlSerializer createSerializer(
		            final CleanerProperties inProperties) {
				return new CompactXmlSerializer(inProperties);
			}
		});

		private ISerializerFactory factory;

		XmlSerializer(final ISerializerFactory inFactory) {
			this.factory = inFactory;
		}

		public org.htmlcleaner.XmlSerializer getSerializer(
		        final CleanerProperties inProperties) {
			return this.factory.createSerializer(inProperties);
		}
	}

	/**
	 * Private constructor.
	 * 
	 * @param inUrl
	 *            URL of the page to parse.
	 * @throws IOException
	 */
	private XPathHelper(final URL inUrl) throws IOException {
		final HtmlCleaner lCleaner = new HtmlCleaner();
		this.docNode = lCleaner.clean(inUrl, DEFAULT_CHARSET);
	}

	/**
	 * Creates a <code>XPathHelper</code> instance with the <code>URL</code> of
	 * a web page.
	 * 
	 * @param inUrl
	 *            URL of the page to parse.
	 * @return XPathHelper
	 * @throws IOException
	 */
	public static XPathHelper newInstance(final URL inUrl) throws IOException {
		return new XPathHelper(inUrl);
	}

	/**
	 * Convenience method: creates a <code>Document</code> from the specified
	 * URL.
	 * 
	 * @param inUrl
	 *            the URL: <code>scheme://host:port/file</code>
	 * @return {@link Document}
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document createDocument(final String inUrl)
	        throws ParserConfigurationException, IOException, SAXException {
		return createDocument(new URL(inUrl));
	}

	/**
	 * Convenience method (for xml/xhtml): creates a <code>Document</code> from
	 * the specified URL.
	 * 
	 * @param inUrl
	 *            URL
	 * @return {@link Document}
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document createDocument(final URL inUrl)
	        throws ParserConfigurationException, IOException, SAXException {
		final HtmlCleaner lCleaner = new HtmlCleaner();
		lCleaner.clean(inUrl);

		final DocumentBuilder lBuilder = DocumentBuilderFactory.newInstance()
		        .newDocumentBuilder();
		final URLConnection lConnection = inUrl.openConnection();
		BufferedInputStream lStream = null;
		Document outDocument = null;
		try {
			lStream = new BufferedInputStream(lConnection.getInputStream());
			outDocument = lBuilder.parse(lStream);
		}
		finally {
			lStream.close();
		}
		return outDocument;
	}

	/**
	 * Returns the text of the first node returned by the specified XPath
	 * element.
	 * 
	 * @param inXPath
	 *            String
	 * @return String the found node's value or <code>null</code>.
	 * @throws XPatherException
	 */
	public String getElement(final String inXPath) throws XPatherException {
		if (this.docNode == null)
			return null;
		final Object[] lResult = this.docNode.evaluateXPath(inXPath);
		return lResult.length == 0 ? null
		        : ((TagNode) lResult[0]).getText().toString();
	}

	/**
	 * Convenience method (for xml/xhtml): returns the text node value from the
	 * specified XPath element.
	 * 
	 * @param inDocument
	 *            {@link Document}
	 * @param inXPath
	 *            String
	 * @return String
	 * @throws XPathExpressionException
	 */
	public static String getElement(final Document inDocument,
	        final String inXPath) throws XPathExpressionException {
		final Element lElement = createElement(inDocument, inXPath);
		return lElement == null ? "" : lElement.getFirstChild().getNodeValue(); //$NON-NLS-1$
	}

	/**
	 * Returns the specified attribute's value from the specified XPath element.
	 * 
	 * @param inXPath
	 *            String XPath expression.
	 * @param inAttribute
	 *            String the name of the attribute.
	 * @return String the found node's attribute value or <code>null</code>.
	 * @throws XPatherException
	 */
	public String getAttribute(final String inXPath, final String inAttribute)
	        throws XPatherException {
		if (this.docNode == null)
			return null;
		final Object[] lResult = this.docNode.evaluateXPath(inXPath);
		return (lResult.length == 0) ? null
		        : ((TagNode) lResult[0]).getAttributeByName(inAttribute);
	}

	/**
	 * Convenience method (for xml/xhtml): returns the specified attribute's
	 * value from the specified XPath element.
	 * 
	 * @param inDocument
	 *            {@link Document}
	 * @param inXPath
	 *            String XPath expression
	 * @param inAttribute
	 *            String attribute name
	 * @return String the attribute value
	 * @throws XPathExpressionException
	 */
	public static String getAttribute(final Document inDocument,
	        final String inXPath, final String inAttribute)
	                throws XPathExpressionException {
		final Element lElement = createElement(inDocument, inXPath);
		return lElement == null ? null : lElement.getAttribute(inAttribute);
	}

	private static Element createElement(final Document inDocument,
	        final String inXPath) throws XPathExpressionException {
		final XPathFactory lFactory = XPathFactory.newInstance();
		final XPath lXPath = lFactory.newXPath();
		final XPathExpression lXExpression = lXPath.compile(inXPath);
		final Element lElement = (Element) lXExpression
		        .evaluate(inDocument.getDocumentElement(), XPathConstants.NODE);
		return lElement;
	}

	/**
	 * Convenience method (for xml/xhtml): serializes the parsed page.
	 * 
	 * @param inSerializer
	 *            {@link XmlSerializer}
	 * @return String the cleaned and serialized html
	 * @throws IOException
	 */
	public String getSerialized(final XmlSerializer inSerializer)
	        throws IOException {
		if (this.docNode == null)
			return ""; //$NON-NLS-1$

		final CleanerProperties lProps = new HtmlCleaner().getProperties();
		lProps.setUseCdataForScriptAndStyle(true);
		lProps.setRecognizeUnicodeChars(true);
		lProps.setUseEmptyElementTags(true);
		lProps.setAdvancedXmlEscape(true);
		lProps.setTranslateSpecialEntities(true);
		lProps.setBooleanAttributeValues("empty"); //$NON-NLS-1$
		lProps.setNamespacesAware(true);
		lProps.setOmitXmlDeclaration(false);
		lProps.setOmitDoctypeDeclaration(true);
		lProps.setOmitHtmlEnvelope(false);

		this.docNode.getAttributes().remove(NS_XML);

		return inSerializer.getSerializer(lProps).getXmlAsString(this.docNode);
	}

	/**
	 * @return String the cleaned document's doctype
	 */
	public String getDocType() {
		if (this.docNode == null)
			return ""; //$NON-NLS-1$
		return this.docNode.getDocType() == null ? "" //$NON-NLS-1$
		        : this.docNode.getDocType().getContent();
	}

	private interface ISerializerFactory {
		org.htmlcleaner.XmlSerializer createSerializer(
		        CleanerProperties inProperties);
	}

	/**
	 * Removes all <code>link</code> elements that can cause problems during
	 * RDFa extraction because they have an unqualified <code>rel</code>
	 * attribute, e.g.:
	 * 
	 * <pre>
	 * &lt;link rel="shortcut icon" href="favicon.ico" />
	 * </pre>
	 */
	public void removeUnqualifiedLinks() {
		if (this.docNode == null)
			return;

		final TagNode[] lLinks = this.docNode.getElementsByName("link", true); //$NON-NLS-1$
		for (final TagNode lLink : lLinks) {
			final String lRel = lLink.getAttributeByName("rel"); //$NON-NLS-1$
			if (lRel.contains(" ")) { //$NON-NLS-1$
				lLink.removeFromTree();
			}
		}
	}

}
