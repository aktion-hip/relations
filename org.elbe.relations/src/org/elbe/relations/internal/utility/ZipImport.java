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
package org.elbe.relations.internal.utility;

import java.io.File;

import org.eclipse.e4.core.services.log.Logger;

/**
 * Helper class for importing an embedded database with the data stored to a Zip
 * file.
 * 
 * @author Luthiger Created on 20.10.2007
 */
@SuppressWarnings("restriction")
public class ZipImport extends ZipRestore {
	private final String dbName;

	/**
	 * @param inDataStore
	 *            File Directory where the embedded databases are stored in the
	 *            application's workspace.
	 * @param inArchiveName
	 *            String name of the Zip file containing the exported database.
	 * @param inDBName
	 *            String name of the embedded database that is filled with the
	 *            imported data.
	 */
	public ZipImport(final File inDataStore, final String inArchiveName,
			final String inDBName, final Logger inLog) {
		super(inDataStore, inArchiveName, inLog);
		dbName = inDBName;
	}

	@Override
	protected String getName(final String inEntryName) {
		return dbName
				+ inEntryName.substring(inEntryName.indexOf(File.separator));
	}

}
