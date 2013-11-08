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
package org.elbe.relations.internal.bom;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.ILightWeightModel;
import org.hip.kernel.exc.VException;

/**
 * Helper class for business objects.
 * 
 * @author Luthiger
 */
public class BOMHelper {

	/**
	 * Returns the <code>IItemModel</code> that corresponds to the specified
	 * <code>ILightWeightItem</code>.
	 * 
	 * @param inLightWeight
	 *            ILightWeightItem the item who's corresponding
	 *            <code>IItem</code> is to look up.
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link IItemModel}
	 * @throws BOMException
	 */
	public static IItemModel getItem(final ILightWeightModel inLightWeight,
			final IEclipseContext inContext) throws BOMException {

		final IItem lItem = org.elbe.relations.data.bom.BOMHelper
				.getItem(inLightWeight);
		try {
			switch (inLightWeight.getItemType()) {
			case IItem.TERM:
				return new TermWithIcon((AbstractTerm) lItem, inContext);
			case IItem.TEXT:
				return new TextWithIcon((AbstractText) lItem, inContext);
			case IItem.PERSON:
				return new PersonWithIcon((AbstractPerson) lItem, inContext);
			}
			return null;
		}
		catch (final VException exc) {
			throw new BOMException(exc);
		}
	}

}
