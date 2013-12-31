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

import org.elbe.relations.data.bom.IItem;

/**
 * Interface for visitor used for <code>IItem</code>.
 * 
 * @author Luthiger Created on 14.02.2007
 * @see IItem#visit(IItemVisitor)
 */
public interface IItemVisitor {

	/**
	 * @param inTitle
	 *            String the item's title
	 */
	void setTitle(String inTitle);

	/**
	 * @return String the item's title
	 */
	String getTitle();

	/**
	 * @param inTitleEditable
	 *            boolean indicates whether the item's title should be editable
	 *            in the inspector view
	 */
	void setTitleEditable(boolean inTitleEditable);

	/**
	 * @param inSubTitle
	 *            String the item's sub-title
	 */
	void setSubTitle(String inSubTitle);

	/**
	 * @return String the item's sub-title
	 */
	String getSubTitle();

	/**
	 * @param inText
	 *            String the item's text
	 */
	void setText(String inText);

	/**
	 * @return String the item's text
	 */
	String getText();

	/**
	 * @param inTextEditable
	 *            indicates whether the item's text should be editable in the
	 *            inspector view
	 */
	void setTextEditable(boolean inTextEditable);

	/**
	 * Each item has a text field, but sometimes, we want something different
	 * displayed as the item's 'text'. This setter gives the possibility to set
	 * the content of the real text field even in such a case.
	 * 
	 * @param inText
	 *            String the real text field's content as is.
	 */
	void setRealText(String inText);

	/**
	 * @return the real text field's content as is.
	 */
	String getRealText();
}
