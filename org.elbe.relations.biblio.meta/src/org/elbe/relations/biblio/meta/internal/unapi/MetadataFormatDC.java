/**
This package is part of Relations application.
Copyright (C) 2010-2016, Benno Luthiger

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.utility.NewTextAction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Parser class for <em>Dublin Core</em> metadata information.
 *
 * @author Luthiger Created on 02.01.2010
 */
public class MetadataFormatDC extends AbstractMetadataFormat {
	private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String METADATA_FORMAT_ID = "oai_dc"; //$NON-NLS-1$
	private static final String NAME_SPACE = "dc".intern(); //$NON-NLS-1$

	private enum DCElements {
		TITLE("title".intern(), new GenericFlatElementHelper()), //$NON-NLS-1$
		CREATOR("creator".intern(), new GenericFlatElementHelper()), //$NON-NLS-1$
		CONTRIBUTOR("contributor".intern(), new GenericFlatElementHelper()), //$NON-NLS-1$
		PUBLISHER("publisher".intern(), new GenericFlatElementHelper()), //$NON-NLS-1$
		DESCRIPTION("description".intern(), new GenericFlatElementHelper()), //$NON-NLS-1$
		SUBJECT("subject".intern(), new GenericFlatElementHelper()), //$NON-NLS-1$
		DATE("date".intern(), new GenericFlatElementHelper()), //$NON-NLS-1$
		TYPE("type".intern(), new GenericFlatElementHelper()); //$NON-NLS-1$

		private final String elementName;
		private final IElementHelper helper;

		DCElements(final String inElementName, final IElementHelper inHelper) {
			elementName = inElementName;
			helper = inHelper;
		}

		String getElementName() {
			return elementName;
		}

		ElementListener getListener(final Attributes inAttributes) {
			return helper.getListener(elementName, inAttributes);
		}
	}

	private Map<String, Collection<IContent>> elements;

	private ElementListener listener;
	private NewTextAction action = null;

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
		final String lAuthor = getCollection(elements.get(DCElements.CREATOR.getElementName()), ", "); //$NON-NLS-1$
		final NewTextAction.Builder lActionBuilder = new NewTextAction.Builder(
				getChecked(DCElements.TITLE.getElementName()), lAuthor == null ? "-" : lAuthor); //$NON-NLS-1$

		String lAdditional = getChecked(DCElements.PUBLISHER.getElementName());
		if (lAdditional != null) {
			lActionBuilder.publisher(lAdditional);
		}
		lAdditional = getCollection(elements.get(DCElements.CONTRIBUTOR.getElementName()), ", "); //$NON-NLS-1$
		if (lAdditional != null) {
			lActionBuilder.coAuthor(lAdditional);
		}
		lAdditional = getChecked(DCElements.DATE.getElementName());
		if (lAdditional != null) {
			lActionBuilder.year(lAdditional);
		}

		final String lSubject = getChecked(DCElements.SUBJECT.getElementName());
		final String lDescription = getChecked(DCElements.DESCRIPTION.getElementName());
		if (lSubject != null && lDescription != null) {
			lActionBuilder.text(lSubject + NL + lDescription);
		} else {
			lAdditional = lDescription == null ? lSubject : lDescription;
		}
		if (lAdditional != null) {
			lActionBuilder.text(lAdditional);
		}

		action = lActionBuilder.type(AbstractText.TYPE_BOOK).build(getContext());
	}

	private String getCollection(final Collection<IContent> inContents, final String inDelimiter) {
		if (inContents == null) {
			return null;
		}

		final StringBuilder outContent = new StringBuilder();
		boolean lFirst = true;
		for (final IContent lContent : inContents) {
			if (!lFirst) {
				outContent.append(inDelimiter);
			}
			lFirst = false;
			outContent.append(lContent.getContent());
		}
		return new String(outContent);
	}

	@Override
	public void startElement(final String inUri, final String inLocalName, final String inName,
			final Attributes inAttributes) throws SAXException {
		final String lName = checkNameSpace(inName);
		if (lName == null) {
			return;
		}

		if (listener == null) {
			for (final DCElements lElement : DCElements.values()) {
				if (lName.equals(lElement.getElementName())) {
					final ElementListener lListener = lElement.getListener(inAttributes);
					if (lListener != null) {
						listener = lListener;
						listener.activate(lName, inAttributes);
						return;
					}
				}
			}
		}
	}

	@Override
	public void endElement(final String inUri, final String inLocalName, final String inName) throws SAXException {
		final String lName = checkNameSpace(inName);
		if (lName == null) {
			return;
		}

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

	@Override
	protected String getNameSpace() {
		return NAME_SPACE;
	}

	@Override
	protected Collection<IContent> getElement(final String inElementName) {
		return elements.get(inElementName);
	}

}
