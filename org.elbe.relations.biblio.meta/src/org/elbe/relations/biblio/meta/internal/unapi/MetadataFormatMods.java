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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObject;
import org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObject.ListenerParameter;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.utility.NewTextAction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Parser class for <em>Metadata Object Description Schema (mods)</em> metadata
 * information.
 *
 * @author Luthiger Created on 29.12.2009
 */
public class MetadataFormatMods extends AbstractMetadataFormat {
	private static final String METADATA_FORMAT_ID = "mods"; //$NON-NLS-1$
	private static final String ROOT_NODE = "mods".intern(); //$NON-NLS-1$

	private enum ModsElements {
		TITLE("titleInfo".intern(), new GenericElementHelper("title".intern()), ROOT_NODE), //$NON-NLS-1$ //$NON-NLS-2$
		NAME("name".intern(), new NameElementHelper(), ROOT_NODE), //$NON-NLS-1$
		ABSTRACT("abstract".intern(), new GenericFlatElementHelper(), ROOT_NODE), //$NON-NLS-1$
		NOTE("note".intern(), new GenericFlatElementHelper(), null), //$NON-NLS-1$
		PUBLISHER_PLACE("originInfo".intern(), new OriginElementHelper(), ROOT_NODE), //$NON-NLS-1$
		GENRE("genre".intern(), new GenericFlatElementHelper(), ROOT_NODE), //$NON-NLS-1$
		RESOURCE_TYPE("typeOfResource".intern(), new GenericFlatElementHelper(), ROOT_NODE), //$NON-NLS-1$
		ISSUE_INFO("relatedItem".intern(), new IssueElementHelper(), ROOT_NODE); //$NON-NLS-1$

		private final String elementName;
		private final IElementHelper helper;
		private final String requestedParent;

		ModsElements(final String inElementName, final IElementHelper inHelper, final String inRequestedParent) {
			elementName = inElementName;
			helper = inHelper;
			requestedParent = inRequestedParent;
		}

		String getElementName() {
			return elementName;
		}

		ElementListener getListener(final String inParentNode, final Attributes inAttributes) {
			if (requestedParent != null && requestedParent != inParentNode) {
				return null;
			}
			return helper.getListener(elementName, inAttributes);
		}
	}

	Map<String, Collection<IContent>> elements;

	private ElementListener listener;
	private NewTextAction action = null;
	private final Stack<String> ancestors = new Stack<String>();
	private String parentNode;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.elbe.relations.biblio.meta.internal.unapi.AbstractMetadataFormat#
	 * canHandle(java.lang.String)
	 */
	@Override
	public boolean canHandle(final String inFormat) {
		return METADATA_FORMAT_ID.equalsIgnoreCase(inFormat);
	}

	@Override
	protected NewTextAction getAction() {
		return action;
	}

	@Override
	public void startDocument() throws SAXException {
		elements = new HashMap<String, Collection<IContent>>();
	}

	@Override
	public void endDocument() throws SAXException {
		final String[] lAuthors = handelAuthors();
		final String lAuthor = lAuthors[0];
		final String lCoAuthors = lAuthors[1];

		// prepare the action using the action builder
		final NewTextAction.Builder lActionBuilder = new NewTextAction.Builder(
				getChecked(ModsElements.TITLE.getElementName()), lAuthor);
		if (lCoAuthors != null) {
			lActionBuilder.coAuthor(lCoAuthors);
		}
		String lAdditional = getChecked(ModsElements.ABSTRACT.getElementName());
		final String lNote = getChecked(ModsElements.NOTE.getElementName());
		if (lNote != null) {
			lAdditional = lAdditional == null ? lNote : lAdditional + "\n" + lNote; //$NON-NLS-1$
		}
		if (lAdditional != null) {
			lActionBuilder.text(lAdditional);
		}
		handleOrigin(lActionBuilder, elements.get(ModsElements.PUBLISHER_PLACE.getElementName()));
		handlerIssueInfo(lActionBuilder, elements.get(ModsElements.ISSUE_INFO.getElementName()));

		// create the action
		action = lActionBuilder.type(getType()).build(getContext());
	}

	private int getType() {
		final String lResourceType = getChecked(ModsElements.RESOURCE_TYPE.getElementName());
		final String lGenre = getChecked(ModsElements.GENRE.getElementName());
		if (lGenre != null && lGenre.length() > 0) {
			if ("journal article".equalsIgnoreCase(lGenre)) { //$NON-NLS-1$
				return AbstractText.TYPE_ARTICLE;
			}
			if ("web site".equalsIgnoreCase(lGenre)) { //$NON-NLS-1$
				return AbstractText.TYPE_WEBPAGE;
			}
		}
		if (lResourceType != null && lResourceType.length() > 0) {
			if ("text".equalsIgnoreCase(lResourceType)) { //$NON-NLS-1$
				return AbstractText.TYPE_BOOK;
			}
		}
		return AbstractText.TYPE_BOOK;
	}

	private String[] handelAuthors() {
		Collection<String> lAuthorSet = new ArrayList<String>();
		Collection<String> lCoAuthorsSet = new ArrayList<String>();
		final Collection<IContent> lNames = elements.get(ModsElements.NAME.getElementName());
		if (lNames == null) {
			return new String[] { "", null }; //$NON-NLS-1$
		}

		for (final IContent lName : lNames) {
			final String lContent = lName.getContent();
			if (lContent == null) {
				continue;
			}
			if ("creator".equals(((NameParameterObject) lName).getRoleType())) { //$NON-NLS-1$
				lAuthorSet.add(lName.getContent());
			} else {
				lCoAuthorsSet.add(lName.getContent());
			}
		}
		if (lAuthorSet.size() == 0) {
			lAuthorSet = lCoAuthorsSet;
			lCoAuthorsSet = null;
		}
		final Joiner lJoiner = new Joiner(", "); //$NON-NLS-1$
		return new String[] { lJoiner.join(lAuthorSet), lCoAuthorsSet == null ? null : lJoiner.join(lCoAuthorsSet) };
	}

	private void handlerIssueInfo(final NewTextAction.Builder inActionBuilder,
			final Collection<IContent> inCollection) {
		if (inCollection == null) {
			return;
		}
		if (inCollection.size() == 0) {
			return;
		}
		final IContent lIssues = inCollection.iterator().next();
		if (!(lIssues instanceof IssueParameterObject)) {
			return;
		}
		final IssueParameterObject lIssueInfo = (IssueParameterObject) lIssues;

		String lAdditional = lIssueInfo.getIssueTitle();
		if (lAdditional != null) {
			inActionBuilder.publication(lAdditional);
		}
		lAdditional = lIssueInfo.getIssueVolume();
		if (lAdditional != null) {
			inActionBuilder.volume(Integer.parseInt(lAdditional));
		}
		lAdditional = lIssueInfo.getIssueNumber();
		if (lAdditional != null) {
			inActionBuilder.number(Integer.parseInt(lAdditional));
		}
		lAdditional = lIssueInfo.getPages();
		if (lAdditional != null) {
			inActionBuilder.pages(lAdditional);
		}
		lAdditional = lIssueInfo.getIssueDate();
		if (lAdditional != null) {
			inActionBuilder.year(lAdditional);
		}
	}

	private void handleOrigin(final NewTextAction.Builder inActionBuilder, final Collection<IContent> inCollection) {
		if (inCollection == null) {
			return;
		}
		if (inCollection.size() == 0) {
			return;
		}
		final IContent lOrigin = inCollection.iterator().next();
		if (!(lOrigin instanceof OriginParameterObject)) {
			return;
		}
		final OriginParameterObject lPlacePublisher = (OriginParameterObject) lOrigin;

		String lAdditional = lPlacePublisher.getPlace();
		if (lAdditional != null) {
			inActionBuilder.place(lAdditional);
		}
		lAdditional = lPlacePublisher.getPublisher();
		if (lAdditional != null) {
			inActionBuilder.publisher(lAdditional);
		}
		lAdditional = lPlacePublisher.getDateIssued();
		if (lAdditional != null) {
			inActionBuilder.year(lAdditional);
		}
	}

	@Override
	protected String getNameSpace() {
		return METADATA_FORMAT_ID;
	}

	@Override
	protected Collection<IContent> getElement(final String inElementName) {
		return elements.get(inElementName);
	}

	@Override
	public void startElement(final String inUri, final String inLocalName, final String inName,
			final Attributes inAttributes) throws SAXException {
		final String lName = checkNameSpace(inName);
		if (lName == null) {
			return;
		}

		parentNode = ancestors.empty() ? "" : ancestors.peek(); //$NON-NLS-1$
		ancestors.push(lName);

		if (listener == null) {
			for (final ModsElements lElement : ModsElements.values()) {
				if (lName == lElement.getElementName()) {
					final ElementListener lListener = lElement.getListener(parentNode, inAttributes);
					if (lListener != null) {
						listener = lListener;
						if (listener.isFlat) {
							listener.activate(lName, inAttributes);
						}
						return;
					}
				}
			}
		}
		if (listener != null && listener.canListenTo(lName)) {
			listener.activate(lName, inAttributes);
			return;
		}
	}

	@Override
	public void endElement(final String inUri, final String inLocalName, final String inName) throws SAXException {
		final String lName = checkNameSpace(inName);
		if (lName == null) {
			return;
		}

		ancestors.pop();

		if (listener != null) {
			if (listener.canListenTo(lName)) {
				listener.deactivate(lName);
				return;
			}
			if (listener.isEndTag(lName)) {
				final String lElementID = listener.getElementID();
				Collection<IContent> lElements = elements.get(lElementID);
				if (lElements == null) {
					lElements = new ArrayList<IContent>();
					elements.put(lElementID, lElements);
				}
				lElements.add(listener.getContent());
				listener = null;
				return;
			}
		}
	}

	@Override
	public void characters(final char[] inCharacters, final int inStart, final int inLength) throws SAXException {
		if (listener != null && listener.isActive()) {
			listener.addCharacters(inCharacters, inStart, inLength);
		}
	}

	// --- private classes ---

	abstract private static class ExendedElementListener extends ElementListener {
		ExendedElementListener(final String inElementName, final String inTextNodeName) {
			super(inElementName, inTextNodeName);
		}

		abstract AbstractParameterObject getParameterObject();

		abstract String getValue(Attributes inAttributes);

		@Override
		void activate(final String inName, final Attributes inAttributes) {
			super.activate(inName, inAttributes);
			getParameterObject().prepare(inName, getValue(inAttributes));
		}

		@Override
		void deactivate(final String inName) {
			super.deactivate(inName);
			getParameterObject().unprepare(inName);
		}

		@Override
		void addCharacters(final char[] inCharacters, final int inStart, final int inLength) {
			getParameterObject().addCharacters(inCharacters, inStart, inLength);
		}

		@Override
		IContent getContent() {
			return getParameterObject();
		}

		@Override
		public String toString() {
			return getParameterObject().getContent();
		}
	}

	private static class NameListener extends ExendedElementListener {
		private final NameParameterObject name;

		NameListener(final String inElementName) {
			super(inElementName, null);
			textNodeNames = Arrays.asList(NameParameterObject.NAMES);
			name = new NameParameterObject();
		}

		@Override
		public AbstractParameterObject getParameterObject() {
			return name;
		}

		@Override
		String getValue(final Attributes inAttributes) {
			return inAttributes.getValue("type"); //$NON-NLS-1$
		}
	}

	private static class OriginListener extends ExendedElementListener {
		private final OriginParameterObject origin;

		OriginListener(final String inElementName) {
			super(inElementName, null);
			textNodeNames = Arrays.asList(OriginParameterObject.NAMES);
			origin = new OriginParameterObject();
		}

		@Override
		public AbstractParameterObject getParameterObject() {
			return origin;
		}

		@Override
		String getValue(final Attributes inAttributes) {
			return null;
		}
	}

	private static class IssueInfoListener extends ExendedElementListener {
		private final IssueParameterObject issueInfo;

		IssueInfoListener(final String inElementName) {
			super(inElementName, null);
			textNodeNames = Arrays.asList(IssueParameterObject.NAMES);
			issueInfo = new IssueParameterObject();
		}

		@Override
		public AbstractParameterObject getParameterObject() {
			return issueInfo;
		}

		@Override
		String getValue(final Attributes inAttributes) {
			return inAttributes.getValue("type"); //$NON-NLS-1$
		}
	}

	// ---

	private static class NameElementHelper implements IElementHelper {
		@Override
		public ElementListener getListener(final String inElementName, final Attributes inAttributes) {
			if ("personal".equalsIgnoreCase(inAttributes.getValue("type"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return new NameListener(inElementName);
			}
			return null;
		};
	}

	private static class OriginElementHelper implements IElementHelper {
		@Override
		public ElementListener getListener(final String inElementName, final Attributes inAttributes) {
			return new OriginListener(inElementName);
		};
	}

	private static class IssueElementHelper implements IElementHelper {
		@Override
		public ElementListener getListener(final String inElementName, final Attributes inAttributes) {
			if ("host".equalsIgnoreCase(inAttributes.getValue("type"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return new IssueInfoListener(inElementName);
			}
			return null;
		}

	}

	// ---

	private abstract static class AbstractParameterObject implements IContent {
		abstract protected ListenerParameterObject getParameterObject();

		void prepare(final String inName, final String inValue) {
			getParameterObject().prepare(inName, inValue);
		}

		void unprepare(final String inName) {
			getParameterObject().unprepare(inName);
		}

		void addCharacters(final char[] inCharacters, final int inStart, final int inLength) {
			getParameterObject().addCharacters(inCharacters, inStart, inLength);
		}
	}

	private static class NameParameterObject extends AbstractParameterObject {
		private static final String ELEMENT_NAME_PART = "namePart"; //$NON-NLS-1$
		private static final String ELEMENT_ROLE = "role"; //$NON-NLS-1$
		private static final String ELEMENT_ROLE_TERM = "roleTerm"; //$NON-NLS-1$
		static final String[] NAMES = { ELEMENT_NAME_PART, ELEMENT_ROLE, ELEMENT_ROLE_TERM };

		private static final String NAME_DEFAULT = "defaultName"; //$NON-NLS-1$
		private static final String NAME_FAMILY = "familyName"; //$NON-NLS-1$
		private static final String NAME_FIRST = "firstName"; //$NON-NLS-1$
		private static final String ROLE_TERM = "roleTerm"; //$NON-NLS-1$

		private final ListenerParameterObject name;

		NameParameterObject() {
			name = new ListenerParameterObject();
			name.addParameter(NAME_DEFAULT, ELEMENT_NAME_PART, null);
			name.addParameter(NAME_FAMILY, ELEMENT_NAME_PART, "family"); //$NON-NLS-1$
			name.addParameter(NAME_FIRST, ELEMENT_NAME_PART, "given"); //$NON-NLS-1$

			name.addParameter(ELEMENT_ROLE, ELEMENT_ROLE, null);
			name.addParameter(ROLE_TERM, ELEMENT_ROLE_TERM, "text"); //$NON-NLS-1$
		}

		@Override
		protected ListenerParameterObject getParameterObject() {
			return name;
		}

		String getRoleType() {
			return name.getContent(ROLE_TERM);
		}

		@Override
		public String getContent() {
			final String lFamilyName = name.getContent(NAME_FAMILY);
			if (lFamilyName == null) {
				return name.getContent(NAME_DEFAULT);
			}
			final String lFirstName = name.getContent(NAME_FIRST);
			return lFirstName == null ? lFamilyName : String.format("%s, %s", lFamilyName, lFirstName); //$NON-NLS-1$
		}
	}

	private static class OriginParameterObject extends AbstractParameterObject implements IContent {
		private static final String ELEMENT_PLACE = "place"; //$NON-NLS-1$
		private static final String ELEMENT_PLACE_TEXT = "text"; //$NON-NLS-1$
		private static final String ELEMENT_PLACE_TERM = "placeTerm"; //$NON-NLS-1$
		private static final String ELEMENT_PUBLISHER = "publisher"; //$NON-NLS-1$
		private static final String ELEMENT_DATE_ISSUED = "dateIssued"; //$NON-NLS-1$
		static final String[] NAMES = { ELEMENT_PLACE, ELEMENT_PLACE_TEXT, ELEMENT_PLACE_TERM, ELEMENT_PUBLISHER,
				ELEMENT_DATE_ISSUED };

		private static final String NAME_PLACE = "place"; //$NON-NLS-1$
		private static final String NAME_PLACE_ALTERNATE = "placeText"; //$NON-NLS-1$
		private static final String NAME_PUBLISHER = "publisher"; //$NON-NLS-1$
		private static final String NAME_DATE = "issueDate"; //$NON-NLS-1$

		private final ListenerParameterObject origin;

		OriginParameterObject() {
			origin = new ListenerParameterObject();
			origin.addParameter(NAME_PLACE, ELEMENT_PLACE_TEXT, null);
			origin.addParameter(NAME_PLACE_ALTERNATE, ELEMENT_PLACE_TERM, null);
			origin.addParameter(NAME_PUBLISHER, ELEMENT_PUBLISHER, null);
			origin.addParameter(NAME_DATE, ELEMENT_DATE_ISSUED, null);
		}

		@Override
		protected ListenerParameterObject getParameterObject() {
			return origin;
		}

		String getPlace() {
			final String outPlace = origin.getContent(NAME_PLACE);
			return outPlace == null ? origin.getContent(NAME_PLACE_ALTERNATE) : outPlace;
		}

		String getPublisher() {
			return origin.getContent(NAME_PUBLISHER);
		}

		String getDateIssued() {
			return origin.getContent(NAME_DATE);
		}

		@Override
		public String getContent() {
			return getPlace();
		}
	}

	private static class IssueParameterObject extends AbstractParameterObject implements IContent {
		private static final String ELEMENT_TITLE_INFO = "titleInfo"; //$NON-NLS-1$
		private static final String ELEMENT_TITLE = "title"; //$NON-NLS-1$
		private static final String ELEMENT_PART = "part"; //$NON-NLS-1$
		private static final String ELEMENT_DETAIL = "detail"; //$NON-NLS-1$
		private static final String ELEMENT_NUMBER = "number"; //$NON-NLS-1$
		private static final String ELEMENT_EXTENT = "extent"; //$NON-NLS-1$
		private static final String ELEMENT_START = "start"; //$NON-NLS-1$
		private static final String ELEMENT_END = "end"; //$NON-NLS-1$
		private static final String ELEMENT_DATE = "date"; //$NON-NLS-1$
		static final String[] NAMES = { ELEMENT_TITLE_INFO, ELEMENT_TITLE, ELEMENT_PART, ELEMENT_DETAIL, ELEMENT_NUMBER,
				ELEMENT_EXTENT, ELEMENT_START, ELEMENT_END, ELEMENT_DATE };

		private static final String NAME_ISSUE_TITLE = "issueTitle"; //$NON-NLS-1$
		private static final String NAME_ISSUE_VOLUME = "volume"; //$NON-NLS-1$
		private static final String NAME_ISSUE_NUMBER1 = "number1"; //$NON-NLS-1$
		private static final String NAME_ISSUE_NUMBER2 = "number2"; //$NON-NLS-1$
		private static final String NAME_ISSUE_PAGES = "pages"; //$NON-NLS-1$
		private static final String NAME_ISSUE_PAGE_START = "pageStart"; //$NON-NLS-1$
		private static final String NAME_ISSUE_PAGE_END = "pageEnd"; //$NON-NLS-1$
		private static final String NAME_ISSUE_DATE = "issueDate"; //$NON-NLS-1$
		private static final String NAME_PART = "part"; //$NON-NLS-1$
		private static final String NAME_VALUE = "value"; //$NON-NLS-1$

		private final ListenerParameterObject issueInfo;

		IssueParameterObject() {
			issueInfo = new ListenerParameterObject();
			final ListenerParameter lTitle = issueInfo.addParameter(NAME_ISSUE_TITLE, ELEMENT_TITLE_INFO, null);
			lTitle.addChild(NAME_VALUE, ELEMENT_TITLE, null);

			final ListenerParameter lPart = issueInfo.addParameter(NAME_PART, ELEMENT_PART, null);
			final ListenerParameter lVolume = lPart.addChild(NAME_ISSUE_VOLUME, ELEMENT_DETAIL, "volume"); //$NON-NLS-1$
			lVolume.addChild(NAME_VALUE, ELEMENT_NUMBER, null);

			final ListenerParameter lNumberIssue = lPart.addChild(NAME_ISSUE_NUMBER1, ELEMENT_DETAIL, "issue"); //$NON-NLS-1$
			lNumberIssue.addChild(NAME_VALUE, ELEMENT_NUMBER, null);

			final ListenerParameter lNumberNumber = lPart.addChild(NAME_ISSUE_NUMBER2, ELEMENT_DETAIL, "number"); //$NON-NLS-1$
			lNumberNumber.addChild(NAME_VALUE, ELEMENT_NUMBER, null);

			final ListenerParameter lPages = lPart.addChild(NAME_ISSUE_PAGES, ELEMENT_EXTENT, null);
			lPages.addChild(NAME_ISSUE_PAGE_START, ELEMENT_START, null);
			lPages.addChild(NAME_ISSUE_PAGE_END, ELEMENT_END, null);

			lPart.addChild(NAME_ISSUE_DATE, ELEMENT_DATE, null);
		}

		@Override
		protected ListenerParameterObject getParameterObject() {
			return issueInfo;
		}

		String getIssueTitle() {
			return issueInfo.getContent(NAME_ISSUE_TITLE + ListenerParameterObject.PATH_DELIMITER + NAME_VALUE);
		}

		String getIssueVolume() {
			return issueInfo.getContent(NAME_PART + ListenerParameterObject.PATH_DELIMITER + NAME_ISSUE_VOLUME
					+ ListenerParameterObject.PATH_DELIMITER + NAME_VALUE);
		}

		String getIssueNumber() {
			final String outNumber = issueInfo.getContent(NAME_PART + ListenerParameterObject.PATH_DELIMITER
					+ NAME_ISSUE_NUMBER1 + ListenerParameterObject.PATH_DELIMITER + NAME_VALUE);
			return outNumber == null ? issueInfo.getContent(NAME_PART + ListenerParameterObject.PATH_DELIMITER
					+ NAME_ISSUE_NUMBER2 + ListenerParameterObject.PATH_DELIMITER + NAME_VALUE) : outNumber;
		}

		String getPages() {
			final String outStart = issueInfo.getContent(NAME_PART + ListenerParameterObject.PATH_DELIMITER
					+ NAME_ISSUE_PAGES + ListenerParameterObject.PATH_DELIMITER + NAME_ISSUE_PAGE_START);
			if (outStart == null) {
				return null;
			}
			final String outEnd = issueInfo.getContent(NAME_PART + ListenerParameterObject.PATH_DELIMITER
					+ NAME_ISSUE_PAGES + ListenerParameterObject.PATH_DELIMITER + NAME_ISSUE_PAGE_END);
			return outEnd == null ? outStart : outStart + "-" + outEnd; //$NON-NLS-1$
		}

		String getIssueDate() {
			return issueInfo.getContent(NAME_PART + ListenerParameterObject.PATH_DELIMITER + NAME_ISSUE_DATE);
		}

		@Override
		public String getContent() {
			return null;
		}
	}

	// ---

	private static class Joiner {
		private final String delimiter;

		Joiner(final String inDelimiter) {
			delimiter = inDelimiter;
		}

		String join(final Collection<String> inSet) {
			final StringBuilder out = new StringBuilder();
			boolean lFirst = true;
			for (final String lElement : inSet) {
				if (!lFirst) {
					out.append(delimiter);
				}
				lFirst = false;
				out.append(lElement);
			}
			return new String(out);
		}
	}

}
