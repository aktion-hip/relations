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
package org.elbe.relations.internal.preferences;

import java.util.ArrayList;
import java.util.List;

import org.elbe.relations.services.ICloudProviderConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * This class registers implementations of the <code>ICloudProviderConfig</code>
 * service.
 *
 * @author lbenno
 */
@Component(service = CloudConfigRegistry.class)
public class CloudConfigRegistry {
	private final List<ICloudProviderConfig> configurations = new ArrayList<>();

	/**
	 * @param config
	 *            {@link ICloudProviderConfig} binds the passed service
	 *            implementation
	 */
	@Reference(cardinality = ReferenceCardinality.MULTIPLE)
	void bindCloudProviderConfig(final ICloudProviderConfig config) {
		this.configurations.add(config);
	}

	/**
	 * @param config
	 *            {@link ICloudProviderConfig} unbinds the passed service
	 *            implementation
	 */
	void unbindCloudProviderConfig(final ICloudProviderConfig config) {
		this.configurations.remove(config);
	}

	/**
	 * @return List&lt;ICloudProviderConfig> the registered cloud provider
	 *         configurations
	 */
	public List<ICloudProviderConfig> getConfigurations() {
		this.configurations.sort(
		        (c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
		return this.configurations;
	}

}
