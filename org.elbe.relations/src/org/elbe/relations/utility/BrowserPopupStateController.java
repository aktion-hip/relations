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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;

/**
 * Helper class to control the browser's popup menu's state, i.e. the visibility
 * and enablement of the menu items.
 * 
 * @author Luthiger
 */
public class BrowserPopupStateController {
	private static final String VARIABLE = "browser.popup.visibility"; //$NON-NLS-1$

	public enum State {
		DISABLED("none"), CONNECTION("connection"), ITEM_CENTER("item_center"), ITEM_PERIPHERY( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		        "item_periphery"); //$NON-NLS-1$

		private String value;

		State(final String inValue) {
			value = inValue;
		}

		public String getValue() {
			return value;
		}
	}

	private BrowserPopupStateController() {
	}

	/**
	 * Convenience method: sets the controller's state.
	 * 
	 * @param inState
	 *            {@link State}
	 */
	public static void setState(final State inState,
	        final MApplication inApplication) {
		inApplication.getContext().set(VARIABLE, inState.getValue());
	}

	/**
	 * Convenience method to check the popup controller's state.
	 * 
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return boolean <code>true</code> if the actual variable value
	 *         corresponds to <code>State.ITEM_PERIPHERY</code>
	 */
	public static boolean checkStatePeriphery(final IEclipseContext inContext) {
		return State.ITEM_PERIPHERY.getValue().equals(inContext.get(VARIABLE));
	}

}
