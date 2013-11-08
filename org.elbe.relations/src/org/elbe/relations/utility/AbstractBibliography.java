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
package org.elbe.relations.utility;

import java.text.MessageFormat;

/**
 * Abstract class providing generic functionality for bibliography schemas, i.e.
 * classes implementing <code>org.elbe.relations.IBibliography</code>.
 * 
 * @author Luthiger Created on 31.12.2006
 */
public abstract class AbstractBibliography {
	protected final static String PERIOD = ". "; //$NON-NLS-1$

	private final static MessageFormat format = new MessageFormat("{0} {2} {1}"); //$NON-NLS-1$

	/**
	 * Returns the first item linked with the second one only if the second is
	 * not empty.
	 * 
	 * @param inAuthor
	 *            String
	 * @param inCoAuthor
	 *            String, may be empty
	 * @param inLink
	 *            String
	 * @return String
	 */
	protected String getAuthorCoAuthor(final String inAuthor,
			final String inCoAuthor, final String inLink) {
		if (inCoAuthor.length() == 0) {
			return inAuthor;
		}
		return format.format(new Object[] { inAuthor, inCoAuthor, inLink });
	}

	/**
	 * Returns the first item or the second or both, linked if needed.
	 * 
	 * @param inFirst
	 *            String, may be empty
	 * @param inSecond
	 *            String, may be empty
	 * @param inLink
	 *            String
	 * @return String
	 */
	protected String getFirstOrSecondOrBoth(final String inFirst,
			final String inSecond, final String inLink) {
		final boolean hasFirst = inFirst.length() != 0;
		final boolean hasSecond = inSecond.length() != 0;

		if (hasFirst && hasSecond) {
			return inFirst + inLink + inSecond;
		}
		if (hasFirst) {
			return inFirst;
		}
		return inSecond;
	}

	/**
	 * Adds the ending to the item if the item's not empty.
	 * 
	 * @param inToCheck
	 *            String, may be empty
	 * @param inEnding
	 *            String
	 * @return String
	 */
	protected String getChecked(final String inToCheck, final String inEnding) {
		if (inToCheck.length() == 0)
			return ""; //$NON-NLS-1$
		return inToCheck + inEnding;
	}

	/**
	 * Prefixes the item if the item's not empty.
	 * 
	 * @param inToCheck
	 *            String, may be empty
	 * @param inPrefix
	 *            String
	 * @return String
	 */
	protected String getCheckedPre(final String inToCheck, final String inPrefix) {
		if (inToCheck.length() == 0)
			return ""; //$NON-NLS-1$
		return inPrefix + inToCheck;
	}
}
