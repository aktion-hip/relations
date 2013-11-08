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
package org.elbe.relations.internal.utility;

import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.internal.services.IItemEditWizard;
import org.elbe.relations.internal.wizards.PersonEditWizard;
import org.elbe.relations.internal.wizards.TermEditWizard;
import org.elbe.relations.internal.wizards.TextEditWizard;

/**
 * Utility class to provide item functionality appropriate for the specified
 * type.
 * 
 * @author Luthiger
 */
public class ItemModelHelper {
	public enum Item {
		TERM(IItem.TERM, TermEditWizard.class), PERSON(IItem.PERSON,
				PersonEditWizard.class), TEXT(IItem.TEXT, TextEditWizard.class);

		private final int type;
		private final Class<? extends IItemEditWizard> editWizard;

		Item(final int inType,
				final Class<? extends IItemEditWizard> inEditWizard) {
			type = inType;
			editWizard = inEditWizard;
		}

		public boolean checkType(final int inType) {
			return type == inType;
		}

		public Class<? extends IItemEditWizard> getItemEditWizard() {
			return editWizard;
		}
	}

	// ---

	/**
	 * Returns the Item with the specified type.
	 * 
	 * @param inType
	 *            int the item type
	 * @return {@link Item}
	 * @see IItem.TERM etc
	 */
	public static Item getItem(final int inType) {
		for (final Item lItem : Item.values()) {
			if (lItem.checkType(inType)) {
				return lItem;
			}
		}
		return Item.TERM;
	}

}
