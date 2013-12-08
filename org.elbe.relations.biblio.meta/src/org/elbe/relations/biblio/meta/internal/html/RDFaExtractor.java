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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.biblio.meta.internal.Messages;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.parsing.XPathHelper;
import org.elbe.relations.parsing.XPathHelper.XmlSerializer;
import org.elbe.relations.utility.NewTextAction;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Extractor for metadata formatted with RDFa.
 * 
 * @author Luthiger Created on 09.02.2010
 */
public class RDFaExtractor {
	private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String XSLT = "resources/RDFaParser_04.xsl"; //$NON-NLS-1$
	private static final String DOCTYPE_RDFA = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">"; //$NON-NLS-1$
	private static final String INDICATOR_ABOUT_ISBN = ":isbn:"; //$NON-NLS-1$
	private static final String INDICATOR_NAMESPACE_BIBO = "bibo:"; //$NON-NLS-1$

	private static Templates template = null;

	enum ParserListener {
		RDFA_SUBJECT("rdf:Description", "rdf:about", new IListenerProcessor() { //$NON-NLS-1$ //$NON-NLS-2$
			        @Override
			        public void process(
			                final String inPageUrl,
			                final String inNodeAbout,
			                final String inTagName,
			                final String inChildTagName,
			                final StringBuilder inContent,
			                final TypedString inTypedContent,
			                final Map<String, Map<String, Collection<TypedString>>> inExtracted,
			                final ParserListener inParent) {
				        if (inNodeAbout.startsWith(inPageUrl))
					        return;
				        Map<String, Collection<TypedString>> lContainer = inExtracted
				                .get(inNodeAbout);
				        if (lContainer == null) {
					        lContainer = new HashMap<String, Collection<TypedString>>();
					        inExtracted.put(inNodeAbout, lContainer);
				        }
				        Collection<TypedString> lValues = lContainer
				                .get(inChildTagName);
				        if (lValues == null) {
					        lValues = new Vector<TypedString>();
					        lContainer.put(inChildTagName, lValues);
				        }
				        lValues.add(inTypedContent);
			        }
		        }), PREDICATE_DC_TITLE(
		        "dc:title", "rdf:resource", new DefaultProcessor()), //$NON-NLS-1$ //$NON-NLS-2$
		PREDICATE_DC_CREATOR(
		        "dc:creator", "rdf:resource", new DefaultProcessor()), //$NON-NLS-1$ //$NON-NLS-2$
		PREDICATE_DC_CONTRIBUTOR(
		        "dc:contributor", "rdf:resource", new DefaultProcessor()), //$NON-NLS-1$ //$NON-NLS-2$
		PREDICATE_DC_PUBLISHER(
		        "dc:publisher", "rdf:resource", new DefaultProcessor()), //$NON-NLS-1$ //$NON-NLS-2$
		PREDICATE_DC_SUBJECT(
		        "dc:subject", "rdf:resource", new DefaultProcessor()), //$NON-NLS-1$ //$NON-NLS-2$
		PREDICATE_DC_DESCRIPTION(
		        "dc:description", "rdf:resource", new DefaultProcessor()), //$NON-NLS-1$ //$NON-NLS-2$
		PREDICATE_DC_DATE("dc:date", null, new DefaultProcessor()), //$NON-NLS-1$
		PREDICATE_DC_TYPE("dc:type", null, new DefaultProcessor()), //$NON-NLS-1$
		PREDICATE_DC_ISPARTOF(
		        "dc:isPartOf", "rdf:resource", new DefaultProcessor()), //$NON-NLS-1$ //$NON-NLS-2$
		PREDICATE_BIBO_DOI("bibo:doi", "rdf:resource", new DefaultProcessor()), //$NON-NLS-1$ //$NON-NLS-2$
		PREDICATE_BIBO_VOLUME("bibo:volume", null, new DefaultProcessor()), //$NON-NLS-1$
		PREDICATE_BIBO_ISSUE("bibo:issue", null, new DefaultProcessor()), //$NON-NLS-1$
		PREDICATE_BIBO_PAGE_START(
		        "bibo:pageStart", null, new DefaultProcessor()), //$NON-NLS-1$
		PREDICATE_BIBO_PAGE_END("bibo:pageEnd", null, new DefaultProcessor()), //$NON-NLS-1$
		NOOP_LISTENER("", null, new DefaultProcessor()); //$NON-NLS-1$

		private ParserListener parent;
		private StringBuilder content = new StringBuilder();
		private TypedString typedContent;
		private String tagName = ""; //$NON-NLS-1$
		private String childTagName = ""; //$NON-NLS-1$
		private final String attributeName;
		private String attributeValue;
		private final IListenerProcessor processor;

		ParserListener(final String inTagName, final String inAttributeName,
		        final IListenerProcessor inProcessor) {
			tagName = inTagName;
			attributeName = inAttributeName;
			processor = inProcessor;
		}

		String getTagName() {
			return tagName;
		}

		void setParent(final ParserListener inParent) {
			parent = inParent;
			content = new StringBuilder();
		}

		void setChildTagName(final String inTagName) {
			childTagName = inTagName;
		}

		ParserListener getParent() {
			return parent;
		}

		public void addContent(final String inContent) {
			content.append(inContent);
		}

		public void setContent(final TypedString inContent) {
			typedContent = inContent;
		}

		public boolean canListen(final String inTag) {
			return tagName.equals(inTag);
		}

		public void evalAttributes(final Attributes inAttributes) {
			if (attributeName != null) {
				attributeValue = inAttributes.getValue(attributeName);
			}
		}

		public String getAttribute() {
			return attributeValue;
		}

		public void process(
		        final String inPageUrl,
		        final Map<String, Map<String, Collection<TypedString>>> inExtracted) {
			processor.process(inPageUrl, attributeValue, tagName, childTagName,
			        content, typedContent, inExtracted, parent);
		}

	}

	/**
	 * Extracts metadata formatted using RDFa to be used as text item.
	 * 
	 * @param inXPathHelper
	 *            {@link XPathHelper} the parsed and cleaned web page.
	 * @param inUrl
	 *            String the dropped page's url.
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link NewTextAction} the action to create a new text item with
	 *         the extracted bibliographical information, may be
	 *         <code>null</code> (if no such information found).
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static NewTextAction process(final XPathHelper inXPathHelper,
	        final String inUrl, final IEclipseContext inContext)
	        throws IOException, TransformerException,
	        ParserConfigurationException, SAXException {
		// first, we make a plausibility test for the existence of RDFa
		if (!DOCTYPE_RDFA.equalsIgnoreCase(inXPathHelper.getDocType()))
			return null;

		// extract RDF triples
		inXPathHelper.removeUnqualifiedLinks();
		final StreamSource lSource = new StreamSource(new StringReader(
		        inXPathHelper.getSerialized(XmlSerializer.COMPACT)));
		final Transformer lTransformer = getTemplate().newTransformer();
		lTransformer.setParameter("this", inUrl); //$NON-NLS-1$

		final StringWriter lWriter = new StringWriter();
		lTransformer.transform(lSource, new StreamResult(lWriter));

		// evaluate extracted RDF triples
		final RDFaParser lParser = new RDFaParser(inUrl, inContext);
		lParser.parse(new String(lWriter.getBuffer()));
		return lParser.getNewTextAction();
	}

	private static Templates getTemplate() throws TransformerException,
	        IOException {
		if (template == null) {
			final StreamSource lSource = new StreamSource(RDFaExtractor.class
			        .getClassLoader().getResource(XSLT).openStream());
			template = TransformerFactory.newInstance().newTemplates(lSource);
		}
		return template;
	}

	// --- private classes ---

	private static class RDFaParser extends DefaultHandler {
		private NewTextAction textAction = null;
		private final Map<String, Map<String, Collection<TypedString>>> extracted = new HashMap<String, Map<String, Collection<TypedString>>>();

		private ParserListener currentListener = ParserListener.NOOP_LISTENER;
		private final String webPageUrl;
		private final IEclipseContext context;

		public RDFaParser(final String inUrl, final IEclipseContext inContext) {
			webPageUrl = inUrl;
			context = inContext;
		}

		void parse(final String inRDFa) throws ParserConfigurationException,
		        SAXException, IOException {
			final SAXParser lParser = SAXParserFactory.newInstance()
			        .newSAXParser();
			lParser.parse(new InputSource(new StringReader(inRDFa)), this);
		}

		NewTextAction getNewTextAction() {
			return textAction;
		}

		@Override
		public void startElement(final String inUri, final String inTag,
		        final String inFullTag, final Attributes inAttributes)
		        throws SAXException {
			for (final ParserListener lListener : ParserListener.values()) {
				if (lListener.canListen(inFullTag)) {
					lListener.setParent(currentListener);
					currentListener = lListener;
					currentListener.evalAttributes(inAttributes);
					return;
				}
			}
		}

		@Override
		public void endElement(final String inUri, final String inTag,
		        final String inFullTag) throws SAXException {
			if (currentListener == null)
				return;

			currentListener.process(webPageUrl, extracted);
			currentListener = currentListener.getParent();
		}

		@Override
		public void characters(final char[] inCharacters, final int inStart,
		        final int inLength) throws SAXException {
			if (currentListener != null) {
				currentListener.addContent(new String(inCharacters, inStart,
				        inLength));
			}
		}

		@Override
		public void endDocument() throws SAXException {
			if (extracted.isEmpty())
				return;

			if (extracted.size() == 1) {
				evaluate(extracted.values().iterator().next());
			} else {
				// We have to choose one out of several available RDFa entries.
				// We take the first containing bibliographical information:
				// either the about URI indicates bibliographical information
				for (final Entry<String, Map<String, Collection<TypedString>>> lEntry : extracted
				        .entrySet()) {
					if (lEntry.getKey().toLowerCase()
					        .contains(INDICATOR_ABOUT_ISBN)) {
						evaluate(lEntry.getValue());
						return;
					}
				}
				// or at least one of the extracted RDFa predicates is from the
				// bibo namespace
				for (final Entry<String, Map<String, Collection<TypedString>>> lEntry : extracted
				        .entrySet()) {
					final Map<String, Collection<TypedString>> lRDFaEntry = lEntry
					        .getValue();
					for (final String lPredicate : lRDFaEntry.keySet()) {
						if (lPredicate.toLowerCase().startsWith(
						        INDICATOR_NAMESPACE_BIBO)) {
							evaluate(lRDFaEntry);
						}
					}
				}
			}
		}

		private void evaluate(
		        final Map<String, Collection<TypedString>> inExtracted) {
			boolean isArticle = false;
			final String lAuthor = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_CREATOR
			                .getTagName()), ", "); //$NON-NLS-1$
			final String lTitle = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_TITLE
			                .getTagName()), ", "); //$NON-NLS-1$
			final NewTextAction.Builder lActionBuilder = new NewTextAction.Builder(
			        lTitle.length() == 0 ? "-" : lTitle, lAuthor.length() == 0 ? "-" : lAuthor); //$NON-NLS-1$ //$NON-NLS-2$

			String lAdditional = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_PUBLISHER
			                .getTagName()), ", "); //$NON-NLS-1$
			if (lAdditional.length() != 0) {
				lActionBuilder.publisher(lAdditional);
			}
			lAdditional = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_CONTRIBUTOR
			                .getTagName()), ", "); //$NON-NLS-1$
			if (lAdditional.length() != 0) {
				lActionBuilder.coAuthor(lAdditional);
			}
			lAdditional = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_DATE
			                .getTagName()), " "); //$NON-NLS-1$
			if (lAdditional.length() != 0) {
				lActionBuilder.year(lAdditional);
			}
			lAdditional = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_ISPARTOF
			                .getTagName()), " "); //$NON-NLS-1$
			if (lAdditional.length() != 0) {
				lActionBuilder.publication(lAdditional);
			}

			lAdditional = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_BIBO_VOLUME
			                .getTagName()), " "); //$NON-NLS-1$
			if (lAdditional.length() != 0) {
				isArticle = true;
				lActionBuilder.volume(toInt(lAdditional));
			}
			lAdditional = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_BIBO_ISSUE
			                .getTagName()), " "); //$NON-NLS-1$
			if (lAdditional.length() != 0) {
				isArticle = true;
				lActionBuilder.number(toInt(lAdditional));
			}
			final String lPageStart = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_BIBO_PAGE_START
			                .getTagName()), " "); //$NON-NLS-1$
			final String lPageEnd = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_BIBO_PAGE_END
			                .getTagName()), " "); //$NON-NLS-1$
			if (lPageStart.length() + lPageEnd.length() != 0) {
				isArticle = true;
				lActionBuilder.pages(String.format(
				        "%s - %s", lPageStart, lPageEnd)); //$NON-NLS-1$
			}

			// information for text field: subject, description
			String lTextValue = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_SUBJECT
			                .getTagName()), ", "); //$NON-NLS-1$
			final StringBuilder lText = new StringBuilder();
			if (lTextValue.length() != 0) {
				lText.append(lTextValue).append(NL);
			}
			lTextValue = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_DESCRIPTION
			                .getTagName()), ", "); //$NON-NLS-1$
			if (lTextValue.length() != 0) {
				lText.append(lTextValue).append(NL);
			}
			// additional text (more technical)
			final StringBuilder lAdditionalText = new StringBuilder();
			lTextValue = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_DC_TYPE
			                .getTagName()), " "); //$NON-NLS-1$
			if (lTextValue.length() != 0) {
				lAdditionalText.append(Messages.RDFaExtractor_lbl_type)
				        .append(": ").append(lTextValue).append(NL); //$NON-NLS-1$
			}
			lTextValue = getValueChecked(
			        inExtracted.get(ParserListener.PREDICATE_BIBO_DOI
			                .getTagName()), " "); //$NON-NLS-1$
			if (lTextValue.length() != 0) {
				lAdditionalText.append("DOI: ").append(lTextValue).append(NL); //$NON-NLS-1$
			}
			if (lAdditionalText.length() != 0) {
				lText.append("[<i>").append(new String(lAdditionalText).trim()).append("</i>]"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (lText.length() != 0) {
				lActionBuilder.text(new String(lText).trim());
			}

			lActionBuilder.type(isArticle ? AbstractText.TYPE_ARTICLE
			        : AbstractText.TYPE_BOOK);
			textAction = lActionBuilder.build(context);
		}

		private int toInt(final String inAdditional) {
			try {
				return Integer.parseInt(inAdditional);
			}
			catch (final NumberFormatException exc) {
				return 0;
			}
		}

		private String getValueChecked(final Collection<TypedString> inValues,
		        final String inJoin) {
			if (inValues == null)
				return ""; //$NON-NLS-1$

			final StringBuilder lRel = new StringBuilder();
			boolean lFirstRel = true;
			final StringBuilder lText = new StringBuilder();
			boolean lFirstText = true;
			for (final TypedString lValue : inValues) {
				if (lValue.isTypeText) {
					if (!lFirstText) {
						lText.append(inJoin);
					}
					lFirstText = false;
					lText.append(lValue.getContent());
				} else {
					if (!lFirstRel) {
						lRel.append(inJoin);
					}
					lFirstRel = false;
					lRel.append(lValue.getContent());
				}
			}
			return lText.length() == 0 ? new String(lRel).trim() : new String(
			        lText).trim();
		}
	}

	private static interface IListenerProcessor {
		void process(String inPageUrl, String inNodeAbout, String inTagName,
		        String inChildTagName, StringBuilder inContent,
		        TypedString inTypedContent,
		        Map<String, Map<String, Collection<TypedString>>> inExtracted,
		        ParserListener inParent);
	}

	private static class DefaultProcessor implements IListenerProcessor {
		@Override
		public void process(
		        final String inPageUrl,
		        final String inAttributeValue,
		        final String inTagName,
		        final String inChildTagName,
		        final StringBuilder inContent,
		        final TypedString inTypedContent,
		        final Map<String, Map<String, Collection<TypedString>>> inExtracted,
		        final ParserListener inParent) {
			if (inParent != null) {
				final String lContentText = new String(inContent).trim();
				final TypedString lContent = (inAttributeValue != null)
				        && inAttributeValue.trim().length() != 0 ? new TypedString(
				        inAttributeValue.trim(), false) : new TypedString(
				        lContentText);
				inParent.setContent(lContent);
				inParent.setChildTagName(inTagName);
			}
		}
	}

	private static class TypedString {
		private boolean isTypeText = true;
		private final String content;

		TypedString(final String inContent) {
			content = inContent;
		}

		TypedString(final String inContent, final boolean inTypeText) {
			content = inContent;
			isTypeText = inTypeText;
		}

		String getContent() {
			return content;
		}
	}

}
