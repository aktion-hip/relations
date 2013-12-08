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
package org.elbe.relations.internal.bom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.internal.controller.BibliographyController;
import org.elbe.relations.internal.style.StyleParser;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.impl.XMLSerializer;
import org.hip.kernel.exc.VException;

/**
 * Special XMLSerializer for visiting <code>DomainObject</code>s used for
 * creating XML representations of the items for print out.
 * 
 * @author Luthiger Created on 12.08.2007
 */
@SuppressWarnings({ "restriction", "serial" })
public class XMLSerializerSpecial extends XMLSerializer {
	private static final String[] TEXT_OBJECT = { "Text", "JoinRelatedText" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String KEY_TEXT = "Text"; //$NON-NLS-1$
	private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String PARA_START = "<para>"; //$NON-NLS-1$
	private static final String PARA_END = "</para>"; //$NON-NLS-1$
	private static final Pattern AMPERSAND = Pattern
			.compile("&[\\s\\p{Punct}]??"); //$NON-NLS-1$
	private static final String AMPERSAND_CODE = "&amp;"; //$NON-NLS-1$

	private static Collection<String> listTagStart;
	private static Collection<String> listTagEnd;
	private final Logger log;
	private final BibliographyController biblioController;

	static {
		listTagStart = new ArrayList<String>();
		listTagEnd = new ArrayList<String>();
		for (final String lListTag : StyleParser.getListTags()) {
			listTagStart.add(String.format("<%s", lListTag)); //$NON-NLS-1$
			listTagEnd.add(String.format("</%s>", lListTag)); //$NON-NLS-1$
		}
	}

	private boolean isText = false;
	private String biblioText = ""; //$NON-NLS-1$

	/**
	 * XMLSerializerSpecial constructor.
	 * 
	 * @param inBiblioController
	 * @param inLog
	 */
	public XMLSerializerSpecial(
			final BibliographyController inBiblioController, final Logger inLog) {
		biblioController = inBiblioController;
		log = inLog;
	}

	/**
	 * Overrides superclass implementation. The text in the field "Text" needs
	 * special treatment: we want to mark paragraphs. Additionally, for the Text
	 * model, we want to add the item information formatted as bibliographical
	 * text.
	 * 
	 * @see org.hip.kernel.bom.impl.XMLSerializer#startProperty(org.hip.kernel.bom.Property)
	 */
	@Override
	protected void startProperty(final Property inProperty) {
		final String lPropertyName = inProperty.getName();

		// handle all normal fields
		if (!KEY_TEXT.equals(lPropertyName)) {
			super.startProperty(inProperty);
			return;
		}

		// handle text field
		String lText = String.valueOf(inProperty.getValue()).trim();
		if (isText) {
			lText = "null".equals(lText) ? "" : lText; //$NON-NLS-1$ //$NON-NLS-2$
			lText = (biblioText.length() * lText.length() == 0) ? biblioText
					+ lText : biblioText + NL + NL + lText;
		}
		processValue(lPropertyName, processLineBreaks(lText));
	}

	private void processValue(final String inName, final String inValue) {
		emit_nl();
		emitStartTag(inName);
		getBuffer().append(inValue);
	}

	/**
	 * Processes a text field's line breaks, i.e. each line is enclosed with
	 * <para>...</para> tags except if the line is an item in a list.
	 * 
	 * @param inValue
	 *            String the content of the item's text field
	 * @return String
	 */
	private String processLineBreaks(final String inValue) {
		if (inValue.length() == 0) {
			return ""; //$NON-NLS-1$
		}

		final StringBuffer lText = new StringBuffer();
		final String[] lLines = inValue.split(NL);
		final LineAnalizer lAnalizer = new LineAnalizer();
		for (final String lLine : lLines) {
			lText.append(lAnalizer.render(lLine));
		}

		String outProcessed = new String(lText).trim();
		final String lEmpty = PARA_START + PARA_END;
		if (outProcessed.endsWith(lEmpty)) {
			outProcessed = outProcessed.substring(0, outProcessed.length()
					- lEmpty.length());
		}
		return outProcessed;
	}

	@Override
	protected void startDomainObject(final GeneralDomainObject inObject) {
		isText = checkTextObject(inObject.getObjectName());
		biblioText = ""; //$NON-NLS-1$

		if (isText) {
			try {
				biblioText = biblioController.getBibliography().render(
						(AbstractText) inObject);
			}
			catch (final VException exc) {
				log.error(exc, exc.getMessage());
			}
		}
		super.startDomainObject(inObject);
	}

	private boolean checkTextObject(final String inObjectName) {
		for (int i = 0; i < TEXT_OBJECT.length; i++) {
			if (TEXT_OBJECT[i].equals(inObjectName))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		final String out = super.toString();
		final Matcher lMatcher = AMPERSAND.matcher(out);
		return lMatcher.replaceAll(AMPERSAND_CODE);
	}

	// ---

	private class LineAnalizer {
		private int listLevel = 0;

		public StringBuffer render(final String inLine) {
			final StringBuffer outRendered = new StringBuffer();

			// test list end
			for (final String lTag : listTagEnd) {
				if (inLine.indexOf(lTag) >= 0) {
					listLevel--;
					if (listLevel == 0) {
						return outRendered.append(lTag).append(PARA_START)
								.append(inLine.substring(lTag.length()))
								.append(PARA_END).append(NL);
					}
				}
			}
			if (containsListStart(inLine)) {
				listLevel++;
			}
			if (listLevel == 0) {
				outRendered.append(PARA_START).append(inLine).append(PARA_END)
						.append(NL);
			} else {
				outRendered.append(inLine).append(NL);
			}
			return outRendered;
		}

		private boolean containsListStart(final String inLine) {
			for (final String lTag : listTagStart) {
				if (inLine.indexOf(lTag) >= 0) {
					return true;
				}
			}
			return false;
		}
	}

}
