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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.elbe.relations.internal.data.IDBSettings;
import org.hip.kernel.dbaccess.DBAccessConfiguration;

/**
 * Helper class providing generic functionality for <code>IAction</code>s and
 * <code>ICommand</code>s.
 * 
 * @author Luthiger Created on 27.11.2008
 */
public final class ActionHelper {

	private ActionHelper() {
	}

	/**
	 * Convenience method to initialize an action.
	 * 
	 * @param inAction
	 *            IAction
	 * @param inActionID
	 *            String
	 * @param inToolTip
	 *            String
	 * @param inCommandID
	 *            String
	 * @param inImage
	 *            ImageDescriptor can be <code>null</code>.
	 */
	public static void initializeAction(final IAction inAction,
			final String inActionID, final String inToolTip,
			final String inCommandID, final ImageDescriptor inImage) {
		inAction.setId(inActionID);
		inAction.setToolTipText(inToolTip);
		inAction.setActionDefinitionId(inCommandID);
		if (inImage != null) {
			inAction.setImageDescriptor(inImage);
		}
		inAction.setEnabled(false);
	}

	/**
	 * Creates a <code>DBAccessConfiguration</code> instance from the specified
	 * DB settings.
	 * 
	 * @param inDbSettings
	 *            {@link IDBSettings}
	 * @return {@link DBAccessConfiguration}
	 */
	public static DBAccessConfiguration createDBConfiguration(
			final IDBSettings inDbSettings) {
		final String lCatalog = inDbSettings.getCatalog();
		final String lPath = inDbSettings.getDBConnectionConfig().isEmbedded() ? EmbeddedCatalogHelper
				.getEmbeddedDBChecked(lCatalog) : lCatalog;
		return new DBAccessConfiguration(inDbSettings.getDBConnectionConfig()
				.getName(), inDbSettings.getHost(), lPath,
				inDbSettings.getUser(), inDbSettings.getPassword());
	}
}
