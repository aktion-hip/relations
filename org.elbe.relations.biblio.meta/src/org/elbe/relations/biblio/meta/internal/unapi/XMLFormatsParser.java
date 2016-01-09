/**
This package is part of Relations application.
Copyright (C) 2009-2016, Benno Luthiger

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
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * Parser for XML documents like:
 *
 * <pre>
 * &lt;formats>
 * &lt;format name="endnote" type="text/plain"/>
 * &lt;format name="bibtex" type="text/plain"/>
 * &lt;format name="oai_dc" type="application/xml"/>
 * &lt;format name="mods" type="application/xml"/>
 * &lt;/formats>
 * </pre>
 * </p>
 *
 * @author Luthiger Created on 29.12.2009
 */
public class XMLFormatsParser extends DefaultHandler {
	private static final String TAG_FORMAT = "format".intern(); //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	private Collection<String> metadataFormats;

	// prevent instance construction
	private XMLFormatsParser() {
	}

	public static XMLFormatsParser getInstance() {
		return new XMLFormatsParser();
	}

	/**
	 * Parses the document at the specified <code>URL</code> and returns the
	 * metadata formats extracted from the document.
	 *
	 * @param inUrl
	 *            URL the document's URL.
	 * @return Collection<String> the metadata formats extracted from the
	 *         document.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Collection<String> parse(URL inUrl) throws ParserConfigurationException, SAXException, IOException {
		InputStream lInput = null;
		try {
			lInput = inUrl.openStream();
			final SAXParser lParser = SAXParserFactory.newInstance().newSAXParser();
			lParser.parse(lInput, this);
			return metadataFormats;
		} finally {
			if (lInput != null) {
				lInput.close();
			}
		}
	}

	@Override
	public void startDocument() throws SAXException {
		metadataFormats = new ArrayList<String>();
	}

	@Override
	public void startElement(String inUri, String inLocalName, String inName, Attributes inAttributes)
			throws SAXException {
		if (TAG_FORMAT.equals(inName)) {
			metadataFormats.add(inAttributes.getValue(ATTRIBUTE_NAME));
		}
	}

}
