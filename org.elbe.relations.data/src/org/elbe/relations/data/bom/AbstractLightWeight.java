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

import java.util.Locale;

import org.elbe.relations.data.Messages;
import org.hip.kernel.exc.VException;

/**
 * Base class for light weight items.
 * 
 * @author Luthiger Created on 13.10.2006
 */
public abstract class AbstractLightWeight implements ILightWeightItem {

	/**
	 * @see IItem#getCreated()
	 */
	@Override
	public String getCreated() throws VException {
		return Messages.getString(
				"Item.created.modified", getLocale(), getCreatedModified()); //$NON-NLS-1$
	}

	abstract protected Object[] getCreatedModified() throws VException;

	/**
	 * Returns the default locale <code>ENGLISH</code>. Subclasses should
	 * override.
	 * 
	 * @return {@link Locale} the locale used for localized messages.
	 */
	protected Locale getLocale() {
		return Locale.ENGLISH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int lPrime = 31;
		int outHash = 1;
		outHash = lPrime * outHash + getItemType();
		outHash = lPrime * outHash + (int) (getID() ^ (getID() >>> 32));
		return outHash;
	}

	/**
	 * @return <code>true</code> if ID and type are equal.
	 * @see org.hip.kernel.bom.impl.DomainObjectImpl#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object inObject) {
		if (this == inObject)
			return true;
		if (inObject == null)
			return false;
		if (inObject instanceof ILightWeightItem) {
			final ILightWeightItem lItem = (ILightWeightItem) inObject;
			return getItemType() == lItem.getItemType()
					&& getID() == lItem.getID();
		}
		return false;
	}

}
