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

import java.io.File;

import org.eclipse.e4.core.services.log.Logger;

import com.google.gson.JsonObject;

/**
 * Interface defining the OSGi service to export to a cloud provider.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public interface ICloudProvider {

	/**
	 * Executed the upload to the cloud.
	 *
	 * @param toExport
	 *            {@link File} the file containing the content to upload to the
	 *            cloud
	 * @param configuration
	 *            {@link JsonObject} the cloud provider configuration
	 * @param log
	 *            {@link Logger}
	 * @return boolean <code>true</code> if the content has been successfully
	 *         uploaded
	 */
	boolean upload(File toExport, JsonObject configuration, Logger log);

}
