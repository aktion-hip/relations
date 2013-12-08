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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.utility.NewTextAction;
import org.xml.sax.SAXException;

/**
 * Helper class to retrieve the unAPI metadata.
 * 
 * @author Luthiger Created on 29.12.2009
 */
public class UnAPIHelper {
	private static final String TMPL_FORMATS = "%s?id=%s"; //$NON-NLS-1$
	private static final String TMPL_GET_ENTRY = "%s?id=%s&format=%s"; //$NON-NLS-1$

	/**
	 * The metadata formats the unAPI helper can handle are registered here:
	 * mods, oai_dc, bibtex
	 */
	private static final IUnAPIHandler[] handledFormats = new IUnAPIHandler[] {
	        new MetadataFormatMods(), new MetadataFormatDC(),
	        new MetadataFormatBibtex() };

	private final String serverURL;
	private final String entryID;

	/**
	 * UnAPIHelper constructor
	 * 
	 * @param inServerURL
	 *            String
	 * @param inEntryID
	 *            String
	 */
	public UnAPIHelper(final String inServerURL, final String inEntryID) {
		serverURL = inServerURL;
		entryID = inEntryID;
	}

	/**
	 * Returns the action to create the new text item containing the parsed
	 * metadata.
	 * 
	 * @param inContext
	 *            IEclipseContext
	 * @return NewTextAction the action to create the new text item or
	 *         <code>null</code>.
	 * @throws MalformedURLException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public NewTextAction getAction(final IEclipseContext inContext)
	        throws MalformedURLException, ParserConfigurationException,
	        SAXException, IOException {
		// first we get the formats
		String lUrl = String.format(TMPL_FORMATS, serverURL, entryID);
		final Collection<String> lFormats = XMLFormatsParser.getInstance()
		        .parse(new URL(lUrl));
		for (final String lFormat : lFormats) {
			for (final IUnAPIHandler lFormatHandler : handledFormats) {
				if (lFormatHandler.canHandle(lFormat)) {
					lUrl = String.format(TMPL_GET_ENTRY, serverURL, entryID,
					        lFormat);
					return lFormatHandler
					        .createAction(new URL(lUrl), inContext);
				}
			}
		}
		return null;
	}

}
