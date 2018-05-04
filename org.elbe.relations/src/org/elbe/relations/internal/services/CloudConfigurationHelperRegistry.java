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
package org.elbe.relations.internal.services;

import java.util.ArrayList;
import java.util.List;

import org.elbe.relations.services.ICloudProviderConfigurationHelper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * @author lbenno
 *
 */
@Component(service = CloudConfigurationHelperRegistry.class)
public class CloudConfigurationHelperRegistry {
	private final List<ICloudProviderConfigurationHelper> helpers = new ArrayList<>(
			5);

	@Reference(cardinality = ReferenceCardinality.MULTIPLE)
	void bindConfigurationHelper(
			final ICloudProviderConfigurationHelper helper) {
		this.helpers.add(helper);
	}

	void unbindConfigurationHelper(
			final ICloudProviderConfigurationHelper helper) {
		this.helpers.remove(helper);
	}

	/**
	 * @return List&lt;ICloudProviderConfigurationHelper> the list of registered
	 *         helpers
	 */
	public List<ICloudProviderConfigurationHelper> getHelpers() {
		this.helpers.sort(
		        (h1, h2) -> h1.getName().compareToIgnoreCase(h2.getName()));
		return this.helpers;
	}

}
