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

/**
 * ItemVisitor used in <code>InspectorView</code>.
 * 
 * @author Luthiger Created on 14.02.2007
 * 
 */
public class InspectorViewVisitor extends AbstractItemVisitor {
	private boolean isTitleEditable;
	private boolean isTextEditable;
	private String realText;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.utility.IItemVisitor#setSubTitle(java.lang.String)
	 */
	@Override
	public void setSubTitle(final String inSubTitle) {
		// not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.utility.IItemVisitor#getSubTitle()
	 */
	@Override
	public String getSubTitle() {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.utility.IItemVisitor#setTitleEditable(boolean)
	 */
	@Override
	public void setTitleEditable(final boolean inTitleEditable) {
		isTitleEditable = inTitleEditable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.utility.IItemVisitor#getTitleEditable()
	 */
	public boolean isTitleEditable() {
		return isTitleEditable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.utility.IItemVisitor#setTextEditable(boolean)
	 */
	@Override
	public void setTextEditable(final boolean inTextEditable) {
		isTextEditable = inTextEditable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.utility.IItemVisitor#getTextEditable()
	 */
	public boolean isTextEditable() {
		return isTextEditable;
	}

	/**
	 * @return String the real text field's content as is.
	 */
	@Override
	public String getRealText() {
		return realText == null ? getText() : realText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.utility.IItemVisitor#setRealText(java.lang.String)
	 */
	@Override
	public void setRealText(final String inText) {
		realText = inText;
	}

}
