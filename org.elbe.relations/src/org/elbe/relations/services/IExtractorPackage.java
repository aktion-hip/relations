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
 * Service providing <code>IExtractorAdapter</code> instances.
 * 
 * @author Luthiger Created on 14.01.2010
 */
public interface IExtractorPackage {
	/**
	 * Components that provide <code>IExtractorAdapter</code>s register them by
	 * implementing this method.
	 * 
	 * @return IExtractorAdapter[] the array of <code>IExtractorAdapter</code>s
	 *         this component can provide.
	 */
	public IExtractorAdapter[] getExtractorAdapters();

}
