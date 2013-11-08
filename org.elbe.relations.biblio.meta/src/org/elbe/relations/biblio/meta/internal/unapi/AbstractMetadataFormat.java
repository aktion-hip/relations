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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.utility.NewTextAction;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Base class for metadata format parsers.
 * 
 * @author Luthiger Created on 29.12.2009
 */
@SuppressWarnings("restriction")
public abstract class AbstractMetadataFormat extends DefaultHandler implements
		IUnAPIHandler {

	private IEclipseContext context;

	/**
	 * Tests whether format handler instance can handle the specified metadata
	 * format.
	 * 
	 * @param inFormat
	 *            String the metadata format id.
	 * @return boolean <code>true</code> if the format handler instance can
	 *         handle the specified metadata format.
	 */
	@Override
	public abstract boolean canHandle(String inFormat);

	/**
	 * This method parses the specified resource and creates a
	 * <code>NewTextAction</code> with the extracted metadata.
	 * 
	 * @param inUrl
	 *            URL the source providing the metadata to handle.
	 * @return NewTextAction the action that can create a new text item with the
	 *         extracted data.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	@Override
	public NewTextAction createAction(final URL inUrl,
			final IEclipseContext inContext)
			throws ParserConfigurationException, SAXException, IOException {
		context = inContext;
		InputStream lInput = null;
		try {
			lInput = inUrl.openStream();
			final SAXParser lParser = SAXParserFactory.newInstance()
					.newSAXParser();
			lParser.parse(lInput, this);
			return getAction();
		} finally {
			if (lInput != null) {
				lInput.close();
			}
		}
	}

	protected IEclipseContext getContext() {
		return context;
	}

	/**
	 * Subclasses must provide the <code>NewTextAction</code> after parsing.
	 * 
	 * @return NewTextAction
	 */
	protected abstract NewTextAction getAction();

	// --- private classes ---

	protected static class ElementListener {
		protected Collection<String> textNodeNames = new Vector<String>();
		private StringBuilder content;
		private final String elementID;
		boolean isFlat = false;
		private final Stack<String> trail;

		ElementListener(final String inElementName, final String inTextNodeName) {
			elementID = inElementName;
			if (inTextNodeName != null && inTextNodeName.length() > 0) {
				textNodeNames.add(inTextNodeName);
			}
			trail = new Stack<String>();
		}

		boolean canListenTo(final String inName) {
			return textNodeNames.contains(inName);
		}

		String getElementID() {
			return elementID;
		}

		void activate(final String inName, final Attributes inAttributes) {
			content = new StringBuilder();
			trail.push(inName);
		}

		void deactivate(final String inName) {
			if (inName.equals(trail.peek())) {
				trail.pop();
			}
		}

		boolean isActive() {
			return !trail.isEmpty();
		}

		void addCharacters(final char[] inCharacters, final int inStart,
				final int inLength) {
			for (int i = inStart; i < inStart + inLength; i++) {
				content.append(inCharacters[i]);
			}
		}

		IContent getContent() {
			return new IContent() {
				@Override
				public String getContent() {
					return new String(content);
				}
			};
		}

		boolean isEndTag(final String inNodeName) {
			if (inNodeName != elementID)
				return false;
			return isFlat ? true : trail.isEmpty();
		}
	}

	protected static interface IElementHelper {
		ElementListener getListener(String inElementName,
				Attributes inAttributes);
	}

	protected static class GenericElementHelper implements IElementHelper {
		private final String textNodeName;

		GenericElementHelper(final String inTextNodeName) {
			textNodeName = inTextNodeName;
		}

		@Override
		public ElementListener getListener(final String inElementName,
				final Attributes inAttributes) {
			return new ElementListener(inElementName, textNodeName);
		};
	}

	protected static class GenericFlatElementHelper implements IElementHelper {
		@Override
		public ElementListener getListener(final String inElementName,
				final Attributes inAttributes) {
			final ElementListener outListener = new ElementListener(
					inElementName, ""); //$NON-NLS-1$
			outListener.isFlat = true;
			return outListener;
		}

	}

	protected static interface IContent {
		String getContent();
	}

	protected String checkNameSpace(final String inName) {
		final String[] lParts = inName.split(":"); //$NON-NLS-1$
		if (lParts.length == 1)
			return inName;
		return getNameSpace().equals(lParts[0]) ? lParts[1].intern() : null;
	}

	protected abstract String getNameSpace();

	protected abstract Collection<IContent> getElement(String inElementName);

	protected String getChecked(final String inElementName) {
		final StringBuilder outContent = new StringBuilder();
		final Collection<IContent> lContents = getElement(inElementName);
		if (lContents == null)
			return null;

		for (final IContent lContent : lContents) {
			final String lText = lContent.getContent();
			if (lText == null)
				continue;
			outContent.append(lContent.getContent()).append(" "); //$NON-NLS-1$
		}
		return new String(outContent).trim();
	}

}
