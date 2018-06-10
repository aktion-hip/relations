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

import java.util.function.Consumer;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import com.google.gson.JsonObject;

/**
 * The interface defining the configuration of a cloud provider.
 *
 * @author lbenno
 */
public interface ICloudProviderConfig {

	/**
	 *
	 * @return String the name of the cloud provider.
	 */
	String getName();

	/**
	 * Creates and returns the SWT control for the customized group body.
	 *
	 * @param parent
	 *            {@link Group} the parent composite (SWT group)
	 * @param signalIsValid
	 *            {@link Consumer} the lambda function to signal that the
	 *            configuration is valid
	 * @return {@link Control}
	 */
	Control createConfigContents(Group parent, Consumer<Boolean> signalIsValid);

	/**
	 * @return boolean <code>true</code> if this configuration is valid
	 */
	boolean isValid();

	/**
	 * Call to enable or disable the widgets created in
	 * {@link #createConfigContents(Group)}.
	 *
	 * @param enabled
	 *            boolean
	 */
	void setEnabled(boolean enabled);

	/**
	 * Returns the configuration as JSON object.
	 *
	 * @return {@link JsonObject} the configuration
	 */
	JsonObject getConfig();

	/**
	 * Initialize the values of the SWT controls by evaluating the passed json
	 * object.
	 *
	 * @param values
	 *            {@link JsonObject} the json containing the values
	 */
	void initialize(JsonObject values);

	/**
	 * @return {@link ICloudProvider} the functionality to interact with the
	 *         configured cloud
	 */
	ICloudProvider getProvider();

}
