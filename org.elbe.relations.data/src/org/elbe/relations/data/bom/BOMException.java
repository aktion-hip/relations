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

/**
 * Exception class for all exceptions caused by database access.
 * 
 * @author Benno Luthiger Created on Oct 16, 2004
 */
@SuppressWarnings("serial")
public class BOMException extends Exception {

	/**
	 * @param inArg0
	 */
	public BOMException(final String inArg0) {
		super(inArg0);
	}

	/**
	 * @param inArg0
	 */
	public BOMException(final Throwable inArg0) {
		super(inArg0);
	}

	/**
	 * @param inArg0
	 * @param inArg1
	 */
	public BOMException(final String inArg0, final Throwable inArg1) {
		super(inArg0, inArg1);
	}
}
