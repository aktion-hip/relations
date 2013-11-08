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
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.biblio.meta.internal.Messages;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.utility.NewTextAction;

/**
 * Helper class to extract the COinS information.
 * 
 * @author Luthiger Created on 22.11.2009
 */
@SuppressWarnings("restriction")
public class COinSHelper {
	private static final String DELIMITER0 = "&"; //$NON-NLS-1$
	private static final String DELIMITER1 = "&amp;"; //$NON-NLS-1$
	private static final String DELIMITER2 = "="; //$NON-NLS-1$
	private static final String DELIMITER3 = "@"; //$NON-NLS-1$

	private static final String KEY_GENRE = "rft_val_fmt"; //$NON-NLS-1$

	private final Map<String, String> coinsParts;
	private final IExtractor extractor;
	private final IEclipseContext context;

	/**
	 * Constructor
	 * 
	 * @param inCoinsInfo
	 *            String the content of the title attribute in
	 *            <code>&lt;span class="Z3988" title="bibliographical information"/></code>
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @throws UnsupportedEncodingException
	 */
	public COinSHelper(final String inCoinsInfo, final IEclipseContext inContext)
			throws UnsupportedEncodingException {
		coinsParts = extract(URLDecoder.decode(inCoinsInfo, "UTF-8")); //$NON-NLS-1$
		extractor = getExtractor();
		context = inContext;
	}

	/**
	 * 
	 * @return NewTextAction the action to create the new text item filled with
	 *         the extracted information.
	 */
	public NewTextAction getAction() {
		return extractor.createAction(coinsParts, context);
	}

	private IExtractor getExtractor() {
		final String lGenreInfo = coinsParts.get(KEY_GENRE).toLowerCase();
		for (final Extractor lExtractor : Extractor.values()) {
			if (lExtractor.matches(lGenreInfo)) {
				return lExtractor;
			}
		}
		// default
		return Extractor.BOOK;
	}

	private Map<String, String> extract(final String inCoinsInfo) {
		String[] lParts = inCoinsInfo.split(DELIMITER1);
		if (lParts.length == 1) {
			lParts = inCoinsInfo.split(DELIMITER0);
		}
		final HashMap<String, String> outPartMap = new HashMap<String, String>();
		for (final String lPart : lParts) {
			final String[] lKeyValue = lPart.split(DELIMITER2);
			if (lKeyValue.length == 2) {
				final String lKey = lKeyValue[0].toLowerCase();

				// we have to check for repeatable values, e.g. "rft.au"
				final String lOldValue = outPartMap.get(lKey);
				if (lOldValue == null) {
					outPartMap.put(lKey, lKeyValue[1]);
				} else {
					outPartMap.put(lKey, lOldValue + DELIMITER3 + lKeyValue[1]);
				}
			}
		}
		return outPartMap;
	}

	// --- inner classes ---

	private static interface IExtractor {
		public NewTextAction createAction(Map<String, String> inParts,
				final IEclipseContext inContext);
	}

	private enum Extractor implements IExtractor {
		BOOK("book", new BookExtractor()), //$NON-NLS-1$
		ARTICLE("journal", new ArticleExtractor()); //$NON-NLS-1$

		private String selector;
		private IExtractor helper;

		Extractor(final String inSelector, final IExtractor inHelper) {
			selector = inSelector;
			helper = inHelper;
		}

		public boolean matches(final String inGenreInfo) {
			return inGenreInfo.endsWith(selector);
		}

		@Override
		public NewTextAction createAction(final Map<String, String> inParts,
				final IEclipseContext inContext) {
			return helper.createAction(inParts, inContext);
		}
	}

	/**
	 * Abstract extractor class.
	 */
	private static abstract class AbstractExtractor {
		private static final String AUTHORS_SEP = ", "; //$NON-NLS-1$
		private static final String KEY_TITLE = "rft.title"; //$NON-NLS-1$
		private static final String KEY_AULAST = "rft.aulast"; //$NON-NLS-1$
		private static final String KEY_AUFIRST = "rft.aufirst"; //$NON-NLS-1$
		private static final String KEY_AUINIT = "rft.auinit"; //$NON-NLS-1$
		private static final String KEY_AUINIT1 = "rft.auinit1"; //$NON-NLS-1$
		private static final String KEY_AUINITM = "rft.auinitm"; //$NON-NLS-1$
		private static final String KEY_AUSUFFIX = "rft.ausuffix"; //$NON-NLS-1$
		private static final String KEY_AU = "rft.au"; //$NON-NLS-1$
		private static final String KEY_AUCORP = "rft.aucorp"; //$NON-NLS-1$
		private static final String KEY_DATE = "rft.date"; //$NON-NLS-1$
		private static final String KEY_SPAGE = "rft.spage"; //$NON-NLS-1$
		private static final String KEY_EPAGE = "rft.epage"; //$NON-NLS-1$
		private static final String KEY_PAGES = "rft.pages"; //$NON-NLS-1$
		private static final String KEY_ISBN = "rft.isbn"; //$NON-NLS-1$
		private static final String KEY_ISSN = "rft.issn"; //$NON-NLS-1$
		private static final String KEY_GENRE = "rft.genre"; //$NON-NLS-1$

		protected String title = "-"; //$NON-NLS-1$
		private String authorLast = DELIMITER3;
		private String authorFirst = DELIMITER3;

		protected Map<String, String> parts;

		protected void setParts(final Map<String, String> inParts) {
			parts = inParts;
			title = parts.get(KEY_TITLE);
		}

		protected String getAuthor() {
			final String lAuthor = parts.get(KEY_AULAST);
			if (lAuthor == null)
				return "-"; //$NON-NLS-1$
			final StringBuilder outAuthor = new StringBuilder(lAuthor);
			authorLast = lAuthor;

			final String lFirst = parts.get(KEY_AUFIRST);
			if (lFirst != null) {
				outAuthor.append(", ").append(lFirst); //$NON-NLS-1$
				authorFirst = lFirst;
			} else {
				final String lInitials = parts.get(KEY_AUINIT);
				if (lInitials != null) {
					outAuthor.append(", ").append(lInitials); //$NON-NLS-1$
					authorFirst = lInitials;
				} else {
					final String lInitial1 = parts.get(KEY_AUINIT1);
					if (lInitial1 != null) {
						outAuthor.append(", ").append(lInitial1).append("."); //$NON-NLS-1$ //$NON-NLS-2$
						final String lInitial2 = parts.get(KEY_AUINITM);
						if (lInitial2 != null) {
							outAuthor.append(" ").append(lInitial2).append("."); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}
			final String lSuffix = parts.get(KEY_AUSUFFIX);
			if (lSuffix != null) {
				outAuthor.append(" ").append(lSuffix); //$NON-NLS-1$
			}
			return new String(outAuthor);
		}

		protected String getCoAuthor() {
			final String lAuthors = parts.get(KEY_AU);
			if (lAuthors == null)
				return ""; //$NON-NLS-1$

			final StringBuilder outAuthors = new StringBuilder();
			final String[] lAuthorArr = lAuthors.split(DELIMITER3);
			for (final String lAuthor : lAuthorArr) {
				if (!lAuthor.contains(authorLast)
						|| !lAuthor.contains(authorFirst)) {
					outAuthors.append(lAuthor).append(AUTHORS_SEP);
				}
			}
			if (outAuthors.length() > 2) {
				return outAuthors.substring(0, outAuthors.length() - 2);
			}
			return new String(outAuthors);
		}

		protected String getPages() {
			String lValue = parts.get(KEY_PAGES);
			if (lValue != null) {
				return lValue;
			}
			lValue = parts.get(KEY_SPAGE);
			if (lValue == null) {
				return ""; //$NON-NLS-1$
			}
			final String lEndPages = parts.get(KEY_EPAGE);
			if (lEndPages != null) {
				return lValue + "-" + lEndPages; //$NON-NLS-1$
			}
			return lValue;
		}

		protected String getText() {
			final StringBuilder outText = new StringBuilder();
			outText.append(appendChecked(KEY_AUCORP,
					Messages.COinSHelper_lbl_corp));
			outText.append(appendChecked(KEY_DATE,
					Messages.COinSHelper_lbl_date));
			outText.append(appendChecked(KEY_ISBN,
					Messages.COinSHelper_lbl_isbn));
			outText.append(appendChecked(KEY_ISSN,
					Messages.COinSHelper_lbl_issn));
			outText.append(appendChecked(KEY_GENRE,
					Messages.COinSHelper_lbl_genre));
			return new String(outText);
		}

		protected String appendChecked(final String inKey, final String inLabel) {
			final StringBuilder outText = new StringBuilder();
			String lValue = null;
			if ((lValue = parts.get(inKey)) != null) {
				outText.append(inLabel)
						.append(": ").append(lValue).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
				return new String(outText);
			}
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Extract book information.
	 */
	private static class BookExtractor extends AbstractExtractor implements
			IExtractor {
		private static final String KEY_BTITLE = "rft.btitle"; //$NON-NLS-1$
		private static final String KEY_ATITLE = "rft.atitle"; //$NON-NLS-1$
		private static final String KEY_PLACE = "rft.place"; //$NON-NLS-1$
		private static final String KEY_PUB = "rft.pub"; //$NON-NLS-1$
		private static final String KEY_EDITION = "rft.edition"; //$NON-NLS-1$
		private static final String KEY_TPAGES = "rft.tpages"; //$NON-NLS-1$
		private static final String KEY_SERIES = "rft.series"; //$NON-NLS-1$
		private static final String KEY_BICI = "rft.bici"; //$NON-NLS-1$

		@Override
		public NewTextAction createAction(final Map<String, String> inParts,
				final IEclipseContext inContext) {
			setParts(inParts);
			NewTextAction.Builder outAction = new NewTextAction.Builder(
					getTitle(), getAuthor());
			outAction = outAction.coAuthor(getCoAuthor());
			outAction = outAction.pages(getPages());

			String lValue = null;
			if ((lValue = inParts.get(KEY_PLACE)) != null) {
				outAction = outAction.place(lValue);
				lValue = null;
			}
			if ((lValue = inParts.get(KEY_PUB)) != null) {
				outAction = outAction.publisher(lValue);
				lValue = null;
			}
			outAction = outAction.text(getText(inParts));
			return outAction.type(AbstractText.TYPE_BOOK).build(inContext);
		}

		private String getText(final Map<String, String> inParts) {
			final StringBuilder outText = new StringBuilder();
			outText.append(appendChecked(KEY_EDITION,
					Messages.COinSHelper_lbl_ed));
			outText.append(appendChecked(KEY_SERIES,
					Messages.COinSHelper_lbl_series));
			outText.append(appendChecked(KEY_TPAGES,
					Messages.COinSHelper_lbl_pages));
			outText.append(appendChecked(KEY_BICI,
					Messages.COinSHelper_lbl_bici));
			outText.append(super.getText());

			if (outText.length() > 2) {
				return outText.substring(0, outText.length() - 2);
			}
			return new String(outText);
		}

		private String getTitle() {
			String lTitle = parts.get(KEY_BTITLE);
			if (lTitle != null)
				return lTitle;
			lTitle = parts.get(KEY_ATITLE);
			if (lTitle != null)
				return lTitle;
			return title;
		}
	}

	/**
	 * Extract journal article information.
	 */
	private static class ArticleExtractor extends AbstractExtractor implements
			IExtractor {
		private static final String KEY_ATITLE = "rft.atitle"; //$NON-NLS-1$
		private static final String KEY_JTITLE = "rft.jtitle"; //$NON-NLS-1$
		private static final String KEY_STITLE = "rft.stitle"; //$NON-NLS-1$
		private static final String KEY_VOLUME = "rft.volume"; //$NON-NLS-1$
		private static final String KEY_ISSUE = "rft.issue"; //$NON-NLS-1$
		private static final String KEY_ARTNUM = "rft.artnum"; //$NON-NLS-1$
		private static final String KEY_CHRON = "rft.chron"; //$NON-NLS-1$
		private static final String KEY_SSN = "rft.ssn"; //$NON-NLS-1$
		private static final String KEY_QUARTER = "rft.quarter"; //$NON-NLS-1$
		private static final String KEY_PART = "rft.part"; //$NON-NLS-1$
		private static final String KEY_EISSN = "rft.eissn"; //$NON-NLS-1$
		private static final String KEY_CODEN = "rft.coden"; //$NON-NLS-1$
		private static final String KEY_SICI = "rft.sici"; //$NON-NLS-1$

		@Override
		public NewTextAction createAction(final Map<String, String> inParts,
				final IEclipseContext inContext) {
			setParts(inParts);
			NewTextAction.Builder outAction = new NewTextAction.Builder(
					getTitle(), getAuthor());
			outAction = outAction.coAuthor(getCoAuthor());
			outAction = outAction.pages(getPages());
			outAction = outAction.publication(getJournal());
			outAction = outAction.volume(getVolume());
			outAction = outAction.number(getIssue());

			outAction = outAction.text(getText(inParts));
			return outAction.type(AbstractText.TYPE_ARTICLE).build(inContext);
		}

		private String getTitle() {
			final String lTitle = parts.get(KEY_ATITLE);
			if (lTitle != null)
				return lTitle;
			return title;
		}

		private String getJournal() {
			String outTitle = parts.get(KEY_JTITLE);
			if (outTitle != null)
				return outTitle;
			outTitle = parts.get(KEY_STITLE);
			if (outTitle != null)
				return outTitle;
			return ""; //$NON-NLS-1$
		}

		private int getVolume() {
			final String lVolume = parts.get(KEY_VOLUME);
			return getIntChecked(lVolume);
		}

		private int getIssue() {
			final String lIssue = parts.get(KEY_ISSUE);
			return getIntChecked(lIssue);
		}

		private int getIntChecked(final String inValue) {
			if (inValue != null) {
				try {
					return Integer.parseInt(inValue);
				}
				catch (final NumberFormatException exc) {
					return 0;
				}
			}
			return 0;
		}

		private String getText(final Map<String, String> inParts) {
			final StringBuilder outText = new StringBuilder();
			outText.append(appendChecked(KEY_ARTNUM,
					Messages.COinSHelper_lbl_number));
			outText.append(appendChecked(KEY_SICI,
					Messages.COinSHelper_lbl_sici));
			outText.append(appendChecked(KEY_CODEN,
					Messages.COinSHelper_lbl_coden));
			outText.append(appendChecked(KEY_CHRON,
					Messages.COinSHelper_lbl_chronology));
			outText.append(appendChecked(KEY_EISSN,
					Messages.COinSHelper_lbl_elissn));
			outText.append(appendChecked(KEY_SSN,
					Messages.COinSHelper_lbl_season));
			outText.append(appendChecked(KEY_QUARTER,
					Messages.COinSHelper_lbl_quarter));
			outText.append(appendChecked(KEY_PART,
					Messages.COinSHelper_lbl_part));
			outText.append(super.getText());

			if (outText.length() > 2) {
				return outText.substring(0, outText.length() - 2);
			}
			return new String(outText);
		}
	}

}
