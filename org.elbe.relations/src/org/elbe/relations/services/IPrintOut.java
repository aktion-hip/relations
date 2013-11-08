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
package org.elbe.relations.services;

import java.io.IOException;

import javax.xml.transform.TransformerException;

/**
 * Interface for all classes extending the
 * <code>org.elbe.relations.printOut</code> extension point.
 * 
 * @author Luthiger Created on 14.01.2007
 */
public interface IPrintOut {

	/**
	 * Open new document to write text.
	 * 
	 * @param inFileName
	 *            String the name of the document created to print out the
	 *            selected content.
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void openNew(String inFileName) throws IOException,
			TransformerException;

	/**
	 * Open document to append text.
	 * 
	 * @param inFileName
	 *            String the name of the document that shall be opened for that
	 *            the selected content can be printed out.
	 * @throws IOException
	 */
	public void openAppend(String inFileName) throws IOException;

	/**
	 * Close the document.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;

	/**
	 * Sets the title of the output document.
	 * 
	 * @param inDocTitle
	 *            String
	 * @throws IOException
	 */
	public void setDocTitle(String inDocTitle) throws IOException;

	/**
	 * Sets the subtitle (e.g. database store, date) of the output document.
	 * 
	 * @param inDocSubtitle
	 *            String
	 * @throws IOException
	 */
	public void setDocSubTitle(String inDocSubtitle) throws IOException;

	/**
	 * Passes the serialized item to print out.
	 * 
	 * @param inXML
	 *            The item serialized as XML.
	 * @throws TransformerException
	 * @throws IOException
	 */
	public void printItem(String inXML) throws TransformerException,
			IOException;

	/**
	 * The plug-in has to notify the system whether it can work or not.
	 * 
	 * @return boolean <code>true</code> if the ressources the print out plug-in
	 *         needs to work are available on the system.
	 */
	public boolean isAvailable();

}