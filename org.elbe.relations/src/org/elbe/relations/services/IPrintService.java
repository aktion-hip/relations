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

/**
 * Interface defining the OSGi print service.
 * 
 * @author Luthiger
 */
public interface IPrintService {

	/**
	 * @return String the print service's name
	 */
	String getName();

	/**
	 * @return String file type of print out file, e.g. "*.txt". This file
	 *         extension will be used in the file dialog to filter the files.
	 */
	String getFileType();

	/**
	 * @return String name of the file type, used to describe the filter
	 *         extension which the dialog will use. User-friendly short
	 *         description shown for its corresponding filter.
	 */
	String getFileTypeName();

	/**
	 * @return {@link IPrintOut} the printer instance
	 */
	IPrintOut getPrinter();

}
