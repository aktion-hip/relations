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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.parsing.DCHtmlExtractor;
import org.elbe.relations.internal.parsing.GenericHtmlExtractor;
import org.elbe.relations.internal.parsing.IHtmlExtractor;
import org.elbe.relations.internal.services.IWebPageParser;
import org.elbe.relations.services.IBibliographyPackage;
import org.elbe.relations.services.IBibliographyProvider;
import org.elbe.relations.utility.NewTextAction;
import org.elbe.relations.utility.NewTextAction.Builder;
import org.htmlcleaner.XPatherException;

/**
 * Component to parse web pages dropped on items on the relations browser.
 * <p>
 * This component contains a registration method
 * <code>{@link WebPageParser#setBibliographyProviders()}</code> where all
 * <code>IBibliographyProvider</code> classes have to be registered.
 * </p>
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class WebPageParser implements IWebPageParser {

	private final List<IBibliographyProvider> bibliographyProviders = new ArrayList<IBibliographyProvider>();

	@Inject
	private Logger log;

	@Inject
	private IEclipseContext context;

	/**
	 * OSGi DS: bind the <code>IBibliographyPackage</code> service.
	 *
	 * @param inBibliographyPackage
	 *            {@link IBibliographyPackage}
	 */
	public void setBibliographyProviders(
	        final IBibliographyPackage inBibliographyPackage) {
		for (final IBibliographyProvider lProvider : inBibliographyPackage
		        .getBibliographyProviders()) {
			bibliographyProviders.add(lProvider);
		}
	}

	/**
	 * OSGi DS: unbind the <code>IBibliographyPackage</code> service.
	 *
	 * @param inBibliographyProvider
	 *            {@link IBibliographyPackage}
	 */
	public void unsetBibliographyProviders(
	        final IBibliographyPackage inBibliographyProvider) {
		for (final IBibliographyProvider lProvider : inBibliographyProvider
		        .getBibliographyProviders()) {
			bibliographyProviders.remove(lProvider);
		}
	}

	/**
	 * Parses the web page specified by the provided url.
	 *
	 * @param inUrl
	 *            String the web page's url.
	 * @return WebPageParser.WebDropResult parameter object containing the
	 *         relevant information from the dropped web page.
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParserException
	 */
	@Override
	public WebPageParser.WebDropResult parse(final String inUrl)
	        throws MalformedURLException, IOException, ParserException {
		final XPathHelper lHelper = XPathHelper.newInstance(new URL(inUrl));
		try {
			final String lTitle = lHelper.getElement(XPathHelper.XPATH_TITLE);
			final WebPageParser.WebDropResult outResult = new WebPageParser.WebDropResult(
			        lTitle == null ? "" : lTitle, inUrl); //$NON-NLS-1$

			// first we extract all metadata information that seems to be
			// appropriate about the web page dropped
			processPageMetadata(lHelper, outResult);

			// then we look for bibliographical data that may be provided in the
			// web page
			processPageBibliography(lHelper, outResult);
			return outResult;
		}
		catch (final XPatherException exc) {
			throw new ParserException(exc.getMessage());
		}
	}

	private void processPageMetadata(final XPathHelper inHelper,
	        final WebDropResult inWebResult) throws XPatherException {
		final IHtmlExtractor lExtractor = DCHtmlExtractor.checkDCMeta(inHelper)
		        ? new DCHtmlExtractor() : new GenericHtmlExtractor();
		final ExtractedData lExtracted = lExtractor.extractData(inHelper,
		        inWebResult.getTitle(), inWebResult.getUrl());
		inWebResult.setText(lExtracted.getText());
		inWebResult.setNewTextAction(createAction(lExtracted));
	}

	private void processPageBibliography(final XPathHelper inHelper,
	        final WebDropResult inWebResult) {
		try {
			for (final IBibliographyProvider lProvider : getProviders()) {
				lProvider.evaluate(inHelper, inWebResult, context);
				if (inWebResult.hasBibliography()) {
					break;
				}
			}
		}
		catch (final ParserException exc) {
			log.error(exc, exc.getMessage());
			Display.getCurrent().beep();
			final RelationsStatusLineManager lStatusLine = context
			        .get(RelationsStatusLineManager.class);
			lStatusLine.showStatusLineMessage(RelationsMessages
			        .getString("WebPageParser.msg.parsing.error")); //$NON-NLS-1$
		}
	}

	private NewTextAction createAction(final ExtractedData inExtracted) {
		final String lAuthor = inExtracted.getAuthor();
		final Builder lBuilder = new NewTextAction.Builder(
		        inExtracted.getTitle(), lAuthor.length() == 0 ? "-" : lAuthor); //$NON-NLS-1$
		lBuilder.coAuthor(inExtracted.getContributor());
		lBuilder.publisher(inExtracted.getPublisher());
		lBuilder.publication(inExtracted.getPath());
		lBuilder.year(inExtracted.getYear());
		lBuilder.place(
		        DateFormat.getDateInstance(DateFormat.LONG).format(new Date()));
		lBuilder.text(inExtracted.getText());
		return lBuilder.build(context);
	}

	private Collection<IBibliographyProvider> getProviders() {
		Collections.sort(bibliographyProviders, new ProviderComparator());
		return bibliographyProviders;
	}

	// --- inner class ---

	public static class WebDropResult {
		private NewTextAction newTextAction = null;
		private NewTextAction newBiblioAction = null;
		private final String title;
		private final String url;
		private String text;

		/**
		 * Constructor
		 *
		 * @param inTitle
		 *            String the web page's title
		 * @param inUrl
		 *            String the web page's url
		 */
		public WebDropResult(final String inTitle, final String inUrl) {
			title = inTitle;
			url = inUrl;
		}

		/**
		 * @return String the web page's title
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * @return the url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * Sets the text for the item's text field.
		 *
		 * @param inText
		 *            String
		 */
		public void setText(final String inText) {
			text = inText;
		}

		/**
		 * @return String the text
		 */
		public String getText() {
			return text;
		}

		/**
		 * Signals that the dragged web page contains bibliographical
		 * information that can be evaluated.
		 *
		 * @return <code>true</code> if web page contains bibliographical
		 *         information.
		 */
		public boolean hasBibliography() {
			return newBiblioAction != null;
		}

		/**
		 * Sets the <code>NewTextAction</code> to create a new text item.
		 *
		 * @param inAction
		 *            NewTextAction
		 */
		public void setNewTextAction(final NewTextAction inAction) {
			newTextAction = inAction;
		}

		/**
		 * Sets the <code>NewTextAction</code> to create a new text item with
		 * the extracted bibliographical information.
		 *
		 * @param inAction
		 *            {@link NewTextAction}
		 */
		public void setNewBiblioAction(final NewTextAction inAction) {
			newBiblioAction = inAction;
		}

		/**
		 * @return NewTextAction the action to create a new text item with the
		 *         extracted metadata from the web page.
		 */
		public NewTextAction getNewTextAction() {
			return newTextAction;
		}

		/**
		 * @return {@link NewTextAction} the action to create a new text item
		 *         with the extracted bibliographical information.
		 */
		public NewTextAction getNewBiblioAction() {
			return newBiblioAction;
		}
	}

	/**
	 * We want the micro format providers being used first. This
	 * <code>Comparator</code> ensures this.
	 *
	 * @author Luthiger Created on 29.12.2009
	 */
	private class ProviderComparator
	        implements Comparator<IBibliographyProvider> {
		@Override
		public int compare(final IBibliographyProvider inProvider1,
		        final IBibliographyProvider inProvider2) {
			if (inProvider1.isMicroFormat()) {
				return inProvider2.isMicroFormat() ? 0 : -1;
			}
			return inProvider2.isMicroFormat() ? 1 : 0;
		}
	}

}
