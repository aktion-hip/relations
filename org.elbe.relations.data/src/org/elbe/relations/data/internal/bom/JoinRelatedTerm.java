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
package org.elbe.relations.data.internal.bom;

import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IRelated;
import org.elbe.relations.data.bom.JoinRelatedTermHome;
import org.elbe.relations.data.bom.Term;
import org.hip.kernel.exc.VException;

/**
 * The model for the term item (created using a join from the relation table).
 * 
 * @author Benno Luthiger
 */
@SuppressWarnings("serial")
public class JoinRelatedTerm extends AbstractTerm implements IRelated {

	private final static String HOME_CLASS_NAME = "org.elbe.relations.data.bom.JoinRelatedTerm1Home"; //$NON-NLS-1$

	/**
	 * JoinRelatedTerm constructor.
	 */
	public JoinRelatedTerm() {
		super();
	}

	/**
	 * This Method returns the class name of the home.
	 * 
	 * @return java.lang.String
	 */
	@Override
	public String getHomeClassName() {
		return HOME_CLASS_NAME;
	}

	/**
	 * @see IRelated#getRelationID()
	 */
	@Override
	public long getRelationID() throws VException {
		return ((Long) get(JoinRelatedTermHome.CONNECTION_ID)).longValue();
	}

	/**
	 * Overrides super class implementation.
	 */
	@Override
	public void save(final String inTitle, final String inText)
	        throws BOMException {
		try {
			((Term) BOMHelper.getTermHome().getItem(getID())).save(inTitle,
			        inText);
			setModel(inTitle, inText);
		}
		catch (final VException exc) {
			new BOMException(exc.getMessage());
		}
	}

}
