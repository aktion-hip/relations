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
package org.elbe.relations.data.bom;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * Interface for classes parsing styled text.
 * 
 * @author Luthiger
 */
public interface IStyleParser {

	/**
	 * Parses the specified tagged text and returns it all tags removed.
	 * 
	 * @param inTagged
	 *            String the text without style information
	 * @return String
	 * @throws IOException
	 * @throws SAXException
	 */
	String getUntaggedText(String inTagged) throws IOException, SAXException;

}
