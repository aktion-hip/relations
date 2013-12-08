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
package org.elbe.relations.biblio.meta.internal.unapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.utility.NewTextAction;
import org.xml.sax.SAXException;

/**
 * 
 * @author Luthiger Created on 03.01.2010
 */
public class MetadataFormatBibtex implements IUnAPIHandler {
	private static final String METADATA_FORMAT_ID = "bibtex"; //$NON-NLS-1$

	private static final String BIBTEX_START = "@"; //$NON-NLS-1$
	private static final String BIBTEX_DELIMITER = ","; //$NON-NLS-1$
	private static final String BIBTEX_SIGN = "="; //$NON-NLS-1$
	private static final String[] ENTRY_STARTS = { "{", "(" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] ENTRY_ENDS = { "}", ")" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] FIELD_BRACKET_STARTS = { "\"", "{" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] FIELD_BRACKET_ENDS = { "\"", "}" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] FIELD_WHITE_SPACES = { "\r", "\n" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] BOOK_TYPES = {
	        "book", "booklet", "manual", "mastersthesis", "phdthesis", "proceedings", "techreport", "unpublished" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	private static final String[] ARTICLE_TYPES = { "article" }; //$NON-NLS-1$
	private static final String CONTRIBUTION_TYPE_INBOOK = "inbook"; //$NON-NLS-1$
	private static final String[] CONTRIBUTION_TYPES = {
	        CONTRIBUTION_TYPE_INBOOK,
	        "incollection", "conference", "inproceedings" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String[] WEB_TYPES = { "misc" }; //$NON-NLS-1$

	private static final String AUTHOR_NA = "-"; //$NON-NLS-1$
	private static final String AUTHOR_SEPARATOR = "and"; //$NON-NLS-1$

	private StringBuilder content;
	private String entryEnd = ENTRY_ENDS[0];
	private String referenceType;
	private Map<String, BibtexField> fields;
	private BibtexField actualField;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.biblio.meta.internal.unapi.IUnAPIHandler#canHandle
	 * (java.lang.String)
	 */
	@Override
	public boolean canHandle(final String inFormat) {
		return METADATA_FORMAT_ID.equalsIgnoreCase(inFormat);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.biblio.meta.internal.unapi.IUnAPIHandler#createAction
	 * (java.net.URL)
	 */
	@Override
	public NewTextAction createAction(final URL inUrl,
	        final IEclipseContext inContext)
	        throws ParserConfigurationException, SAXException, IOException {
		InputStream lStream = null;
		try {
			// first parse the stream
			lStream = inUrl.openStream();
			int lInput;
			final byte[] lByte = new byte[1];
			String lCharacter;
			IListener actualListener = new StartListener();
			while ((lInput = lStream.read()) > -1) {
				lByte[0] = (byte) lInput;
				lCharacter = new String(lByte);
				if (actualListener.processCharacter(lCharacter)) {
					actualListener = actualListener.newListener();
				}
			}
			// then process the information
			final AbstractCreator lCreator = actionFactory(referenceType,
			        inContext);
			return lCreator.create();
		}
		finally {
			if (lStream != null) {
				lStream.close();
			}
		}
	}

	private AbstractCreator actionFactory(final String inReferenceType,
	        final IEclipseContext inContext) {
		final String[] lAuthorAndCo = getAuthorAndCo(fields.get("author")); //$NON-NLS-1$

		for (final String lType : BOOK_TYPES) {
			if (lType.equals(inReferenceType)) {
				return new BookCreator(inContext, lAuthorAndCo[0],
				        lAuthorAndCo[1]);
			}
		}
		for (final String lType : ARTICLE_TYPES) {
			if (lType.equals(inReferenceType)) {
				return new ArticleCreator(inContext, lAuthorAndCo[0],
				        lAuthorAndCo[1]);
			}
		}
		for (final String lType : CONTRIBUTION_TYPES) {
			if (lType.equals(inReferenceType)) {
				return createContributionType(inContext, inReferenceType);
			}
		}
		for (final String lType : WEB_TYPES) {
			if (lType.equals(inReferenceType)) {
				return new WebPageCreator(inContext, lAuthorAndCo[0],
				        lAuthorAndCo[1]);
			}
		}
		return new BookCreator(inContext, lAuthorAndCo[0], lAuthorAndCo[1]);
	}

	private AbstractCreator createContributionType(
	        final IEclipseContext inContext, final String inReferenceType) {
		if (CONTRIBUTION_TYPE_INBOOK.equals(inReferenceType)) {
			return new ContributionCreator(
			        inContext,
			        getFieldChecked("chapter"), getFieldChecked("author"), getFieldChecked("title"), getFieldChecked("editor")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		// "incollection", "conference", "inproceedings"
		return new ContributionCreator(
		        inContext,
		        getFieldChecked("title"), getFieldChecked("author"), getFieldChecked("booktitle"), getFieldChecked("editor")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	private String getFieldChecked(final String inFieldName) {
		final BibtexField lField = fields.get(inFieldName);
		return lField == null ? "-" : lField.fieldContent; //$NON-NLS-1$
	}

	private String[] getAuthorAndCo(final BibtexField inField) {
		if (inField == null)
			return new String[] { AUTHOR_NA, null };
		final String[] lParts = inField.fieldContent.split(AUTHOR_SEPARATOR);
		if (lParts.length == 1)
			return new String[] { lParts[0], null };

		final StringBuilder lCoAuthors = new StringBuilder();
		boolean lFirst = true;
		for (int i = 1; i < lParts.length; i++) {
			if (!lFirst) {
				lCoAuthors.append(AUTHOR_SEPARATOR);
			}
			lFirst = false;
			lCoAuthors.append(lParts[i]);
		}
		return new String[] { lParts[0], new String(lCoAuthors).trim() };
	}

	// --- private classes ---

	private interface IListener {
		boolean processCharacter(String inCharacter);

		IListener newListener();
	}

	private class StartListener implements IListener {
		@Override
		public boolean processCharacter(final String inCharacter) {
			return BIBTEX_START.equals(inCharacter);
		}

		@Override
		public IListener newListener() {
			content = new StringBuilder();
			return new ReferenceItemListener();
		}
	}

	private class ReferenceItemListener implements IListener {
		@Override
		public boolean processCharacter(final String inCharacter) {
			for (int i = 0; i < ENTRY_STARTS.length; i++) {
				if (ENTRY_STARTS[i].equals(inCharacter)) {
					entryEnd = ENTRY_ENDS[i];
					return true;
				}
			}
			content.append(inCharacter);
			return false;
		}

		@Override
		public IListener newListener() {
			referenceType = new String(content).toLowerCase();
			content = new StringBuilder();
			return new KeyWordListener();
		}
	}

	private class KeyWordListener implements IListener {
		@Override
		public boolean processCharacter(final String inCharacter) {
			return BIBTEX_DELIMITER.equals(inCharacter);
		}

		@Override
		public IListener newListener() {
			content = new StringBuilder();
			fields = new HashMap<String, BibtexField>();
			return new FieldNameListener();
		}
	}

	private class FieldNameListener implements IListener {
		@Override
		public boolean processCharacter(final String inCharacter) {
			if (BIBTEX_SIGN.equals(inCharacter)) {
				return true;
			}
			content.append(inCharacter);
			return false;
		}

		@Override
		public IListener newListener() {
			actualField = new BibtexField();
			actualField.fieldName = new String(content).trim().toLowerCase();
			content = new StringBuilder();
			return new FieldContentListener();
		}
	}

	private class FieldContentListener implements IListener {
		@Override
		public boolean processCharacter(final String inCharacter) {
			for (final String lWhiteSpace : FIELD_WHITE_SPACES) {
				if (lWhiteSpace.equals(inCharacter)) {
					content = new StringBuilder(new String(content).trim())
					        .append(" "); //$NON-NLS-1$
					return false;
				}
			}
			if (BIBTEX_DELIMITER.equals(inCharacter)) {
				if (!actualField.inBrackets) {
					return true;
				}
			}
			if (actualField.endBracketChar.equals(inCharacter)
			        && actualField.inBrackets) {
				actualField.inBrackets = false;
				return false;
			}
			if (!actualField.inBrackets) {
				for (int i = 0; i < FIELD_BRACKET_STARTS.length; i++) {
					if (FIELD_BRACKET_STARTS[i].equals(inCharacter)) {
						actualField.endBracketChar = FIELD_BRACKET_ENDS[i];
						actualField.inBrackets = true;
						return false;
					}
				}
			}
			if (entryEnd.equals(inCharacter) && !actualField.inBrackets) {
				// this is the end of the BibTex entry
				return true;
			}
			content.append(inCharacter);
			return false;
		}

		@Override
		public IListener newListener() {
			actualField.fieldContent = new String(content).trim();
			fields.put(actualField.fieldName, actualField);
			actualField = new BibtexField();
			content = new StringBuilder();
			return new FieldNameListener();
		}
	}

	private class BibtexField {
		String fieldName = ""; //$NON-NLS-1$
		String fieldContent = ""; //$NON-NLS-1$
		String endBracketChar = ""; //$NON-NLS-1$
		boolean inBrackets = false;
	}

	// ---

	private abstract class AbstractCreator {
		private final IEclipseContext context;

		public AbstractCreator(final IEclipseContext inContext) {
			context = inContext;
		}

		NewTextAction create() {
			final NewTextAction.Builder lActionBuilder = getBuilder();

			BibtexField lAdditional = fields.get("year"); //$NON-NLS-1$
			if (lAdditional != null) {
				lActionBuilder.year(lAdditional.fieldContent);
			}
			lAdditional = fields.get("volume"); //$NON-NLS-1$
			if (lAdditional != null) {
				try {
					lActionBuilder.volume(Integer
					        .parseInt(lAdditional.fieldContent));
				}
				catch (final NumberFormatException exc) {
				}
			}
			lAdditional = fields.get("number"); //$NON-NLS-1$
			if (lAdditional != null) {
				try {
					lActionBuilder.number(Integer
					        .parseInt(lAdditional.fieldContent));
				}
				catch (final NumberFormatException exc) {
				}
			}
			lAdditional = fields.get("pages"); //$NON-NLS-1$
			if (lAdditional != null) {
				lActionBuilder.pages(lAdditional.fieldContent);
			}
			lAdditional = fields.get("publisher"); //$NON-NLS-1$
			if (lAdditional != null) {
				lActionBuilder.publisher(lAdditional.fieldContent);
			}
			lAdditional = fields.get("address"); //$NON-NLS-1$
			if (lAdditional != null) {
				lActionBuilder.place(lAdditional.fieldContent);
			}
			lAdditional = fields.get("journal"); //$NON-NLS-1$
			if (lAdditional != null) {
				lActionBuilder.publication(lAdditional.fieldContent);
			}
			setText();
			return lActionBuilder.type(getTextType()).build(context);
		}

		protected void setCoAuthors(final NewTextAction.Builder inBuilder,
		        final String inCoAuthors) {
			if (inCoAuthors != null) {
				inBuilder.coAuthor(inCoAuthors);
			}
		}

		private void setText() {
			final StringBuilder lText = new StringBuilder();
			final String lSeparator = ", "; //$NON-NLS-1$

			BibtexField lAdditional = fields.get("note"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("booktitle"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("school"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("institution"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("organization"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("series"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("edition"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("type"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("howpublished"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			lAdditional = fields.get("isbn"); //$NON-NLS-1$
			if (lAdditional != null) {
				lText.append(lAdditional.fieldContent).append(lSeparator);
			}
			if (lText.length() > lSeparator.length()) {
				lText.substring(0, lText.length() - lSeparator.length());
			}
			getBuilder().text(new String(lText));
		}

		abstract protected NewTextAction.Builder getBuilder();

		abstract protected int getTextType();
	}

	private class BookCreator extends AbstractCreator {
		private final NewTextAction.Builder actionBuilder;

		public BookCreator(final IEclipseContext inContext,
		        final String inAuthor, final String inCoAuthors) {
			super(inContext);
			actionBuilder = new NewTextAction.Builder(
			        fields.get("title").fieldContent, checkAuthor(inAuthor)); //$NON-NLS-1$
			setCoAuthors(actionBuilder, inCoAuthors);
		}

		private String checkAuthor(final String inAuthor) {
			if (!AUTHOR_NA.equals(inAuthor))
				return inAuthor;
			final BibtexField lAuthor = fields.get("editor"); //$NON-NLS-1$
			if (lAuthor == null)
				return AUTHOR_NA;
			return lAuthor.fieldContent;
		}

		@Override
		protected NewTextAction.Builder getBuilder() {
			return actionBuilder;
		}

		@Override
		protected int getTextType() {
			return AbstractText.TYPE_BOOK;
		}
	}

	private class ArticleCreator extends AbstractCreator {
		private final NewTextAction.Builder actionBuilder;

		public ArticleCreator(final IEclipseContext inContext,
		        final String inAuthor, final String inCoAuthors) {
			super(inContext);
			actionBuilder = new NewTextAction.Builder(
			        fields.get("title").fieldContent, inAuthor); //$NON-NLS-1$
			setCoAuthors(actionBuilder, inCoAuthors);
		}

		@Override
		protected NewTextAction.Builder getBuilder() {
			return actionBuilder;
		}

		@Override
		protected int getTextType() {
			return AbstractText.TYPE_ARTICLE;
		}
	}

	private class ContributionCreator extends AbstractCreator {
		private final NewTextAction.Builder actionBuilder;

		public ContributionCreator(final IEclipseContext inContext,
		        final String inTitle, final String inAuthors,
		        final String inPublication, final String inEditor) {
			super(inContext);
			actionBuilder = new NewTextAction.Builder(inTitle, inAuthors);

			if (inPublication != null) {
				actionBuilder.publication(inPublication);
			}
			if (inEditor != null) {
				actionBuilder.coAuthor(inEditor);
			}
		}

		@Override
		protected NewTextAction.Builder getBuilder() {
			return actionBuilder;
		}

		@Override
		protected int getTextType() {
			return AbstractText.TYPE_CONTRIBUTION;
		}
	}

	private class WebPageCreator extends AbstractCreator {
		private final NewTextAction.Builder actionBuilder;

		public WebPageCreator(final IEclipseContext inContext,
		        final String inAuthor, final String inCoAuthors) {
			super(inContext);
			actionBuilder = new NewTextAction.Builder(
			        fields.get("title").fieldContent, inAuthor); //$NON-NLS-1$
			setCoAuthors(actionBuilder, inCoAuthors);
		}

		@Override
		protected NewTextAction.Builder getBuilder() {
			return actionBuilder;
		}

		@Override
		protected int getTextType() {
			return AbstractText.TYPE_WEBPAGE;
		}
	}

}
