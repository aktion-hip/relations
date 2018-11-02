/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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
package org.elbe.relations.internal.actions;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.elbe.relations.RelationsConstants;

/**
 * Helper class to interact with the Eclipse preferences.
 *
 * @author lbenno
 */
public final class RelationsPreferences {

	private RelationsPreferences() {
		// prevent instantiation
	}

	/**
	 * Convenience method to access the relations preferences store.
	 *
	 * @return IEclipsePreferences the root of the relations preferences.
	 */
	public static IEclipsePreferences getPreferences() {
		return InstanceScope.INSTANCE
				.getNode(RelationsConstants.PREFERENCE_NODE);
	}

}
