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
package org.elbe.relations.internal.models;

import org.eclipse.swt.graphics.Image;
import org.elbe.relations.data.bom.IItem;

/**
 * Simple wrapper to pass an item (i.e. an <code>IItem</code> instance) with
 * it's icon.
 * 
 * @author Luthiger
 */
public class ItemWithIcon {

	private final IItem item;
	private final Image icon;

	/**
	 * ItemWithIcon constructor.
	 * 
	 * @param inItem
	 *            {@link IItem}
	 * @param inIcon
	 *            {@link Image}
	 */
	public ItemWithIcon(final IItem inItem, final Image inIcon) {
		item = inItem;
		icon = inIcon;
	}

	/**
	 * @return {@link IItem} the item
	 */
	public IItem getItem() {
		return item;
	}

	/**
	 * @return {@link Image} the icon
	 */
	public Image getIcon() {
		return icon;
	}

	@Override
	public String toString() {
		return getItem().toString();
	}

}
