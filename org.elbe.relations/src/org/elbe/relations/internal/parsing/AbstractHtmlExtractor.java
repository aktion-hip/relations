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
package org.elbe.relations.internal.parsing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.elbe.relations.parsing.ExtractedData;

/**
 * Base class for html metadata extractor classes.
 * 
 * @author Luthiger Created on 07.02.2010
 */
public abstract class AbstractHtmlExtractor {
	private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"; //$NON-NLS-1$

	protected boolean hasValue(final String inField) {
		return (inField != null && inField.length() > 0);
	}

	protected void addPart(final StringBuilder inText, final String inField) {
		if (inField != null && inField.length() > 0) {
			inText.append(inField).append(NL);
		}
	}

	/**
	 * @param inDate
	 * @param inExtracted
	 */
	protected void handleDate(final String inDate,
			final ExtractedData inExtracted) {
		if (hasValue(inDate)) {
			if (inDate.contains("T")) { //$NON-NLS-1$
				final int lLength = Math.min(DATE_PATTERN.length(),
						inDate.length() + 2);
				final DateFormat lFormat = new SimpleDateFormat(
						DATE_PATTERN.substring(0, lLength));
				try {
					inExtracted.setDateCreated(lFormat.parse(inDate));
				}
				catch (final ParseException exc) {
					inExtracted.setDateCreated(inDate);
				}
			} else {
				inExtracted.setDateCreated(inDate);
			}
		}
	}

	/**
	 * @param inField1
	 * @param inField2
	 * @param inExtracted
	 */
	protected void handleComment(final String inField1, final String inField2,
			final ExtractedData inExtracted) {
		final StringBuilder lComment = new StringBuilder();
		addPart(lComment, inField1);
		addPart(lComment, inField2);
		final String lText = new String(lComment).trim();
		if (lText.length() > 0) {
			inExtracted.setComment(lText);
		}
	}

}
