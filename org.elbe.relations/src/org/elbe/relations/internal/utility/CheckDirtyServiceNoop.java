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

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

/**
 * This class overrides <code>CheckDirtyService</code> with methods doing
 * nothing. This is used when the dialog is called to create new items,
 * therefore, no dirty checks are needed.
 * 
 * @author Luthiger Created on 17.09.2006
 */
public class CheckDirtyServiceNoop extends CheckDirtyService {
	public CheckDirtyServiceNoop() {
		super(null);
	}

	@Override
	public void register(final RequiredText inText) {
	}

	@Override
	public void register(final StyledText inText) {
	}

	@Override
	public void register(final Text inText) {
	}

	@Override
	public void register(final Combo inCombo) {
	}

	@Override
	public void notifyDirtySwitch(final boolean inIsDirty) {
	}
}