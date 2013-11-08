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

import org.elbe.relations.data.utility.IItemVisitor;

/**
 * Base class for <code>IItemVisitor</code>.
 * 
 * @author Luthiger Created on 14.02.2007
 * @see IItemVisitor
 */
public abstract class AbstractItemVisitor implements IItemVisitor {
	String title;
	String text;

	/*
	 * @see IItemVisitor#getText()
	 */
	@Override
	public String getText() {
		return text;
	}

	/*
	 * @see IItemVisitor#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/*
	 * @see IItemVisitor#setText()
	 */
	@Override
	public void setText(final String inText) {
		text = inText;
	}

	/*
	 * @see IItemVisitor#setTitle()
	 */
	@Override
	public void setTitle(final String inTitle) {
		title = inTitle;
	}

}
