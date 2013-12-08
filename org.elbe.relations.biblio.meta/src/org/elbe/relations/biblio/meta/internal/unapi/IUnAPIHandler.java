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
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.utility.NewTextAction;
import org.xml.sax.SAXException;

/**
 * Interface for classes that provide a medatada format the
 * <code>UnAPIHelper</code> can process.
 * 
 * @author Luthiger Created on 03.01.2010
 */
public interface IUnAPIHandler {

	/**
	 * Tests whether format handler instance can handle the specified metadata
	 * format.
	 * 
	 * @param inFormat
	 *            String the metadata format id.
	 * @return boolean <code>true</code> if the format handler instance can
	 *         handle the specified metadata format.
	 */
	public abstract boolean canHandle(String inFormat);

	/**
	 * This method parses the specified resource and creates a
	 * <code>NewTextAction</code> with the extracted metadata.
	 * 
	 * @param inUrl
	 *            URL the source providing the metadata to handle.
	 * @param IEclipseContext
	 *            inContext
	 * @return NewTextAction the action that can create a new text item with the
	 *         extracted data.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public NewTextAction createAction(URL inUrl, final IEclipseContext inContext)
	        throws ParserConfigurationException, SAXException, IOException;

}
