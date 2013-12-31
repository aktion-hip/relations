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
package org.elbe.relations.data.bom;

import java.util.Collection;
import java.util.StringTokenizer;

import org.elbe.relations.data.search.FullTextHelper;
import org.elbe.relations.data.search.IIndexable;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.exc.VException;

/**
 * The model for the text item.
 * 
 * @author Benno Luthiger Created on Sep 4, 2005
 */
@SuppressWarnings("serial")
public class Text extends AbstractText implements IIndexable {

	public final static String HOME_CLASS_NAME = "org.elbe.relations.data.bom.TextHome"; //$NON-NLS-1$
	public final static String COLL_HOME_CLASS_NAME = "org.elbe.relations.data.bom.CollectableTextHome"; //$NON-NLS-1$

	private final static String NL = System.getProperty("line.separator");
	private final static String ABC = "abcdefghijklmnopqrstuvwxyz";
	private final static String INDENT = "     ";
	private final static String[] REPLACE_TO = { " ", "ü", "ö", "ä", "Ü", "Ö",
	        "Ä", "ù", "è", "é", "à", "á", "\\{", "}" };
	private final static String[] REPLACE_WITH = { "_", "ue", "oe", "ae", "Ue",
	        "Oe", "Ae", "u", "e", "e", "a", "a", "", "" };
	private final static String QUOTE_START = "\"`";
	private final static String QUOTE_END = "\"'";
	private final static String QUOTE_FIND = "\"";

	/**
	 * Text constructor.
	 */
	public Text() {
		super();
	}

	/**
	 * This Method returns the class name of the home.
	 * 
	 * @return java.lang.String
	 */
	@Override
	public String getHomeClassName() {
		return HOME_CLASS_NAME;
	}

	@Override
	public void indexContent(final IndexerHelper inIndexer) throws VException {
		final IndexerDocument lDocument = new IndexerDocument();

		final FullTextHelper lFullText = new FullTextHelper();
		lDocument.addField(getFieldUniqueID(UniqueID.getStringOf(IItem.TEXT,
		        getID())));
		lDocument.addField(getFieldItemType(String.valueOf(IItem.TEXT)));
		lDocument.addField(getFieldItemID(get(TextHome.KEY_ID).toString()));
		lDocument.addField(getFieldTitle(lFullText.add(get(TextHome.KEY_TITLE)
		        .toString())));
		addCreatedModified(lDocument);

		lFullText.add(getChecked(TextHome.KEY_AUTHOR));
		lFullText.add(getChecked(TextHome.KEY_COAUTHORS));
		lFullText.add(getChecked(TextHome.KEY_NUMBER));
		lFullText.add(getChecked(TextHome.KEY_PLACE));
		lFullText.add(getChecked(TextHome.KEY_PUBLICATION));
		lFullText.add(getChecked(TextHome.KEY_PUBLISHER));
		lFullText.add(getChecked(TextHome.KEY_SUBTITLE));
		lFullText.add(getChecked(TextHome.KEY_VOLUME));
		lFullText.add(getChecked(TextHome.KEY_YEAR));
		lFullText.add(getChecked(TextHome.KEY_TEXT));
		lDocument.addField(getFieldText(lFullText.getFullText()));
		inIndexer.addDocument(lDocument);
	}

	/**
	 * Gets the content of this text item as BibTEX entry.
	 * 
	 * @param inUniqueLabels
	 *            Collection<String> of uniques labels for BibTEX entries
	 *            created before (may be empty).
	 * @return String this item's content formatted as BibTEX entry.
	 * @throws GettingException
	 */
	public String getBibtexFormatted(final Collection<String> inUniqueLabels)
	        throws GettingException {
		// default TYPE_BOOK
		String lTextType = "BOOK";
		boolean lCheckCoAuthor = true;
		boolean lCheckSubTitle = true;
		boolean lIsPublisher = true;
		StringBuilder lPart = new StringBuilder();

		switch (getTextType()) {
		case TYPE_ARTICLE:
			lTextType = "ARTICLE";
			lCheckCoAuthor = true;
			lCheckSubTitle = false;
			lIsPublisher = false;
			lPart = createArticle();
			break;
		case TYPE_CONTRIBUTION:
			lTextType = "INCOLLECTION";
			lCheckCoAuthor = false;
			lCheckSubTitle = false;
			lIsPublisher = true;
			lPart = createContribution();
			break;
		case TYPE_WEBPAGE:
			lTextType = "ARTICLE";
			lCheckCoAuthor = true;
			lCheckSubTitle = true;
			lIsPublisher = true;
			lPart = createWebpage();
			break;
		default:
			// TYPE_BOOK
			lTextType = "BOOK";
			lCheckCoAuthor = true;
			lCheckSubTitle = true;
			lIsPublisher = true;
			lPart = createBook();
			break;
		}

		final String lAuthor = get(TextHome.KEY_AUTHOR).toString();
		final String lYear = checkValueNotEmpty(TextHome.KEY_YEAR);

		final StringBuilder outText = new StringBuilder("@");
		outText.append(lTextType).append("{")
		        .append(createLabel(lAuthor, lYear, inUniqueLabels))
		        .append(",").append(NL);
		outText.append(createAuthor(lAuthor, lCheckCoAuthor)).append(
		        createGeneral(lIsPublisher, lCheckSubTitle, lYear));
		outText.append(lPart);
		return wrapUpChecks(outText) + NL + "}";
	}

	private int getTextType() throws GettingException {
		return Integer.parseInt(get(TextHome.KEY_TYPE).toString());
	}

	private StringBuilder createBook() throws GettingException {
		return addPartChecked(checkValueNotEmpty(TextHome.KEY_PLACE), "ADDRESS");
	}

	private StringBuilder createArticle() {
		final StringBuilder outArticle = new StringBuilder();
		outArticle.append(addPartChecked(
		        checkValueNotNull(TextHome.KEY_VOLUME), "VOLUME"));
		outArticle.append(addPartChecked(
		        checkValueNotNull(TextHome.KEY_NUMBER), "NUMBER"));
		outArticle.append(addPartChecked(
		        checkValueNotEmpty(TextHome.KEY_PAGES), "PAGES"));
		outArticle.append(addPartChecked(
		        checkValueNotEmpty(TextHome.KEY_PLACE), "ADDRESS"));
		return outArticle;
	}

	private StringBuilder createContribution() {
		final StringBuilder outContribution = new StringBuilder();
		outContribution.append(createEditors());
		outContribution.append(addPartChecked(
		        checkValueNotEmpty(TextHome.KEY_PUBLICATION), "BOOKTITLE"));
		outContribution.append(addPartChecked(
		        checkValueNotNull(TextHome.KEY_VOLUME), "VOLUME"));
		outContribution.append(addPartChecked(
		        checkValueNotEmpty(TextHome.KEY_PLACE), "ADDRESS"));
		return outContribution;
	}

	private StringBuilder createEditors() {
		final StringBuilder outPart = new StringBuilder();
		final String lValue = checkValueNotEmpty(TextHome.KEY_COAUTHORS);
		if (lValue.length() == 0) {
			return outPart;
		}

		final int lPosition = lValue.indexOf(",");
		String lFirst = lValue.substring(0, lPosition + 1);
		final String lRest = lValue.substring(lPosition + 1);
		lFirst += lRest.replaceAll(",\\s*", " and ");
		outPart.append(INDENT).append("EDITOR = {")
		        .append(lFirst.replaceAll(" und ", " and ")).append("},")
		        .append(NL);
		return outPart;
	}

	private StringBuilder createWebpage() throws GettingException {
		final StringBuilder outWebpage = new StringBuilder();
		final String lURL = checkValueNotEmpty(TextHome.KEY_PUBLICATION);
		final String lAccessed = checkValueNotEmpty(TextHome.KEY_PLACE);

		if ((lURL + lAccessed).length() > 0) {
			outWebpage.append(INDENT).append("JOURNAL = {");
			if (lURL.length() > 0) {
				outWebpage.append("\\path{<").append(lURL).append(">} ");
			}
			if (lAccessed.length() > 0) {
				outWebpage.append("(").append("accessed").append(" ")
				        .append(lAccessed).append(")");
			}
			outWebpage.append("},");
		}
		return outWebpage;
	}

	private StringBuilder addPartChecked(final String inValue,
	        final String inBibtex) {
		final StringBuilder outPart = new StringBuilder();
		if (inValue.length() == 0) {
			return outPart;
		}
		outPart.append(INDENT).append(inBibtex).append(" = {").append(inValue)
		        .append("},").append(NL);
		return outPart;
	}

	/**
	 * Checks whether the number value of the specified field is not 0.
	 * 
	 * @param inKey
	 *            String key of property (i.e. field)
	 * @return String a stringified number or an empty string.
	 */
	private String checkValueNotNull(final String inKey) {
		try {
			final String lValue = get(inKey).toString().trim();
			if (Integer.parseInt(lValue) != 0) {
				return lValue;
			}
		}
		catch (final Exception exc) {
			// intentionally left empty
		}
		return "";
	}

	/**
	 * @param inKey
	 *            String key of property (i.e. field)
	 * @return String a value or an empty string.
	 */
	private String checkValueNotEmpty(final String inKey) {
		try {
			final String lValue = get(inKey).toString().trim();
			return lValue;
		}
		catch (final Exception exc) {
			// intentionally left empty
		}
		return "";
	}

	private StringBuilder createGeneral(final boolean inIsPublisher,
	        final boolean inCheckSubTitle, final String inYear)
	        throws GettingException {
		final StringBuilder outText = new StringBuilder(INDENT);
		outText.append("TITLE = {").append(
		        replaceQuotes(get(TextHome.KEY_TITLE).toString()));
		if (inCheckSubTitle) {
			final String lSubTitle = checkValueNotEmpty(TextHome.KEY_SUBTITLE);
			if (lSubTitle.length() > 0) {
				outText.append(": ").append(replaceQuotes(lSubTitle));
			}
		}
		outText.append("},").append(NL);
		if (inIsPublisher) {
			outText.append(addPartChecked(
			        checkValueNotEmpty(TextHome.KEY_PUBLISHER), "PUBLISHER"));
		} else {
			outText.append(addPartChecked(
			        checkValueNotEmpty(TextHome.KEY_PUBLICATION), "JOURNAL"));
		}
		outText.append(INDENT).append("YEAR = ").append(inYear).append(",")
		        .append(NL);

		return outText;
	}

	private StringBuilder replaceQuotes(final String inText) {
		final StringTokenizer lTokenizer = new StringTokenizer(inText,
		        QUOTE_FIND);

		final StringBuilder outQuoted = new StringBuilder();
		boolean lStart = true;
		if (inText.startsWith(QUOTE_FIND)) {
			outQuoted.append(QUOTE_START);
			lStart = false;
		}

		boolean lFirst = true;
		while (lTokenizer.hasMoreTokens()) {
			if (!lFirst) {
				outQuoted.append(lStart ? QUOTE_START : QUOTE_END);
				lStart = !lStart;
			}
			lFirst = false;
			outQuoted.append(lTokenizer.nextToken());
		}
		outQuoted.append(lStart ? "" : QUOTE_END);
		return outQuoted;
	}

	private String createAuthor(final String inAuthor,
	        final boolean inCheckCoAuthor) throws GettingException {
		final int lPosition = inAuthor.indexOf(",");

		String outAuthor = inAuthor.substring(0, lPosition + 1);
		final String lRest = inAuthor.substring(lPosition + 1);
		outAuthor += lRest.replaceAll(",\\s*", " and ");

		if (inCheckCoAuthor) {
			final String lCoAuthor = checkValueNotEmpty(TextHome.KEY_COAUTHORS);
			if (lCoAuthor.length() > 0) {
				outAuthor += " and " + lCoAuthor.replaceAll(",\\s*", " and ");
			}
		}

		return INDENT + "AUTHOR = {" + outAuthor.replaceAll(" und ", " and ")
		        + "}," + NL;
	}

	private String createLabel(final String inAuthor, final String inYear,
	        final Collection<String> inUniqueLabels) {
		final StringTokenizer lTokenizer = new StringTokenizer(inAuthor, ",");
		String outLabel = lTokenizer.nextToken();
		outLabel = replace(outLabel);
		outLabel += inYear.length() == 0 ? "" : ":" + inYear.substring(2);

		outLabel = checkUnique(inUniqueLabels, outLabel, "", 0);
		return outLabel;
	}

	private String checkUnique(final Collection<String> inUniqueLabels,
	        final String inLabel, final String inSuffix, final int inPosition) {
		if (inUniqueLabels.contains(inLabel + inSuffix)) {
			final String lSuffix = ABC.substring(inPosition, inPosition + 1);
			return checkUnique(inUniqueLabels, inLabel, lSuffix, inPosition + 1);
		} else {
			final String outLabel = inLabel + inSuffix;
			inUniqueLabels.add(outLabel);
			return outLabel;
		}
	}

	private String replace(final String inToProcess) {
		String outProcessed = inToProcess;
		for (int i = 0; i < REPLACE_TO.length; i++) {
			outProcessed = outProcessed.replaceAll(REPLACE_TO[i],
			        REPLACE_WITH[i]);
		}
		return outProcessed;
	}

	private String wrapUpChecks(final StringBuilder inToCheck) {
		final String lEnding = "," + NL;
		String outChecked = new String(inToCheck);
		if (outChecked.endsWith(lEnding)) {
			outChecked = outChecked.substring(0,
			        outChecked.length() - lEnding.length());
		}
		return outChecked.replaceAll("&", "\\\\&");
	}

}
