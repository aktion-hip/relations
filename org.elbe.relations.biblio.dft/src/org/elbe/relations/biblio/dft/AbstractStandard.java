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
package org.elbe.relations.biblio.dft;

import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.utility.IBibliography;
import org.elbe.relations.utility.AbstractBibliography;
import org.hip.kernel.exc.VException;

/**
 * Abstract version of standard bibliography schema. Language specific fragments
 * are provided by subclasses.
 * 
 * The standard scheme is as follows:
 * <dl>
 * <dt>Book:</dt>
 * <dd>[auth] and [coauth]\n[year]. [tit]. [subtit]. [place]: [publisher].</dd>
 * <dt>Article:</dt>
 * <dd>[auth] and [coauth]\n[year]. "[tit]". [publication] [vol]:[nr], [page].</dd>
 * <dt>Contribution:</dt>
 * <dd>[auth]\n[year]. "[tit]", in [publication]. Eds. [coauth], pp. [page].
 * [place]: [publisher].</dd>
 * <dt>Webpage:</dt>
 * <dd>[auth] and [coauth]\n[year]. "[tit]. [subtit]", [publication]. (accessed
 * [place])</dd>
 * </dl>
 * 
 * @author Luthiger Created on 01.01.2007
 * @see org.elbe.relations.IBibliography
 */
public abstract class AbstractStandard extends AbstractBibliography implements
		IBibliography {
	private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	@Override
	public String render(final AbstractText inText) throws VException {
		final StringBuilder outBiblio = new StringBuilder();
		switch (inText.getType()) {
		case AbstractText.TYPE_ARTICLE:
			outBiblio.append(
					getAuthorCoAuthor(inText.getAuthor(), inText.getCoAuthor(),
							getAnd())).append(NL);
			outBiblio.append(getChecked(inText.getYear(), PERIOD));
			outBiblio
					.append("\"").append(inText.getTitle()).append("\"").append(PERIOD); //$NON-NLS-1$ //$NON-NLS-2$

			final StringBuilder lFragment = new StringBuilder(
					inText.getPublication());
			lFragment
					.append(getCheckedPre(
							getFirstOrSecondOrBoth(
									getFirstOrSecondOrBoth(inText.getVolume(),
											inText.getNumber(), ":"), inText.getPages(), ", "), " ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (lFragment.length() != 0) {
				outBiblio.append(lFragment).append("."); //$NON-NLS-1$
			}
			break;
		case AbstractText.TYPE_CONTRIBUTION:
			outBiblio.append(inText.getAuthor()).append(NL);
			outBiblio.append(getChecked(inText.getYear(), PERIOD));
			outBiblio.append("\"").append(inText.getTitle()).append("\", "); //$NON-NLS-1$ //$NON-NLS-2$
			outBiblio.append(getCheckedPre(
					getChecked(inText.getPublication(), PERIOD), "in ")); //$NON-NLS-1$
			outBiblio.append(getCheckedPre(
					getChecked(inText.getCoAuthor(), ", "), getEds())); //$NON-NLS-1$
			outBiblio.append(getCheckedPre(
					getChecked(inText.getPages(), PERIOD), "pp. ")); //$NON-NLS-1$
			outBiblio.append(getChecked(
					getFirstOrSecondOrBoth(inText.getPlace(),
							inText.getPublisher(), ": "), ".")); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		case AbstractText.TYPE_WEBPAGE:
			outBiblio.append(
					getAuthorCoAuthor(inText.getAuthor(), inText.getCoAuthor(),
							getAnd())).append(NL);
			outBiblio.append(getChecked(inText.getYear(), PERIOD));
			outBiblio.append("\"").append(inText.getTitle()); //$NON-NLS-1$
			outBiblio.append(getCheckedPre(inText.getSubtitle(), PERIOD))
					.append("\", "); //$NON-NLS-1$
			outBiblio.append(getChecked(inText.getPublication(), PERIOD));
			final String lAccessed = getCheckedPre(inText.getPlace(),
					getAccessed());
			if (lAccessed.length() != 0) {
				outBiblio.append("(").append(lAccessed).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			break;
		default: // AbstractText.TYPE_BOOK
			outBiblio.append(
					getAuthorCoAuthor(inText.getAuthor(), inText.getCoAuthor(),
							getAnd())).append(NL);
			outBiblio.append(getChecked(inText.getYear(), PERIOD));
			outBiblio.append(inText.getTitle()).append(PERIOD);
			outBiblio.append(getChecked(inText.getSubtitle(), PERIOD));
			outBiblio.append(getChecked(
					getFirstOrSecondOrBoth(inText.getPlace(),
							inText.getPublisher(), ": "), ".")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}
		return new String(outBiblio).trim();
	}

	// Hooks for subclasses
	abstract String getAnd();

	abstract String getEds();

	abstract String getAccessed();

}
