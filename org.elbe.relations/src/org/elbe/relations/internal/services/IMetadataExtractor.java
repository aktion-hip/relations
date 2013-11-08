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
package org.elbe.relations.internal.services;

import java.io.File;
import java.io.IOException;

import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.exc.VException;

/**
 * Interface for an metadata extractor component. This interface defines an OSGi
 * declarative service.
 * 
 * @author Luthiger
 */
public interface IMetadataExtractor {

	/**
	 * Extracts the metadata from the dropped file, create a term item and
	 * returns the new item's <code>UniqueID</code>.
	 * 
	 * @param inDrop
	 *            {@link File}
	 * @return {@link UniqueID} the newly created item's ID.
	 * @throws VException
	 * @throws IOException
	 */
	UniqueID extract(final File inDrop) throws VException, IOException;

}
