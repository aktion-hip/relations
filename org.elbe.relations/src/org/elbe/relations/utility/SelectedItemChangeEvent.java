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

import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IRelationsBrowser;

/**
 * A parameter object that is sent when the user changed the selected item in
 * the browser.
 * 
 * @author Luthiger
 */
public class SelectedItemChangeEvent {

	private final ItemAdapter item;
	private final IRelationsBrowser source;

	/**
	 * SelectedItemChangeEvent constructor.
	 * 
	 * @param inItem
	 *            {@link ItemAdapter} the newly selected item
	 * @param inSource
	 *            {@link IRelationsBrowser} the source of the change event
	 */
	public SelectedItemChangeEvent(final ItemAdapter inItem,
	        final IRelationsBrowser inSource) {
		item = inItem;
		source = inSource;
	}

	/**
	 * @return {@link ItemAdapter} the newly selected item
	 */
	public ItemAdapter getItem() {
		return item;
	}

	/**
	 * Checks the source of the selection change event.
	 * 
	 * @param inBrowser
	 *            {@link IRelationsBrowser}
	 * @return boolean <code>true</code> if the specified browser is the source
	 *         of the event
	 */
	public boolean checkSource(final IRelationsBrowser inBrowser) {
		return source == null ? false : source.equals(inBrowser);
	}
}
