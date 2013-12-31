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
package org.elbe.relations.data.utility;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.elbe.relations.data.Constants;

/**
 * Collator to sort list of <code>IItem</code>s. This collator assumes that all
 * items included in the list display the item's title in the method
 * <code>IItem.toString()</code>.
 * 
 * @author Luthiger Created on 20.01.2007
 */
public class ItemCollator extends Collator {
	private final Collator collator;

	/**
	 * ItemCollator constructor.
	 */
	public ItemCollator() {
		collator = Collator.getInstance(getLocale());
	}

	private Locale getLocale() {
		try {
			return new Locale(Platform.getPreferencesService().getString(
			        Constants.MAIN_ID, Constants.KEY_LANGUAGE_CONTENT,
			        Locale.ENGLISH.getLanguage(), null));
		}
		catch (final NullPointerException exc) {
			// this may be encountered in case of testing
			return Locale.ENGLISH;
		}
	}

	/**
	 * Overridden
	 */
	@Override
	public int compare(final Object inSource, final Object inTarget) {
		return this.compare(inSource.toString(), inTarget.toString());
	}

	@Override
	public int compare(final String inSource, final String inTarget) {
		return collator.compare(inSource, inTarget);
	}

	@Override
	public CollationKey getCollationKey(final String inSource) {
		return collator.getCollationKey(inSource);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((collator == null) ? 0 : collator.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ItemCollator other = (ItemCollator) obj;
		if (collator == null) {
			if (other.collator != null)
				return false;
		} else if (!collator.equals(other.collator))
			return false;
		return true;
	}

}
