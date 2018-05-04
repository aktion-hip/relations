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
package org.elbe.relations.services;

import java.util.function.BiConsumer;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.utility.Feedback;

import com.google.gson.JsonObject;

/**
 * Interface defining the OSGi service to help configuring the cloud provider.
 * I.e. the helper can create the OAuth access token.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public interface ICloudProviderConfigurationHelper {

	/**
	 * @return String the name of the cloud provider this helper is for
	 */
	String getName();

	/**
	 * Create the Control to display the helper's UI.
	 *
	 * @param parent
	 *            {@link Composite}
	 * @param store
	 *            {@link BiConsumer}
	 * @param log
	 *            {@link Logger}
	 */
	void createDialogArea(Composite parent,
			BiConsumer<JsonObject, Feedback> store,
			Logger log);

}

