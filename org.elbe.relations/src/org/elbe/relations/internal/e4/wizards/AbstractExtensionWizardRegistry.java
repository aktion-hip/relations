/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.internal.e4.wizards;

import javax.inject.Inject;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.internal.e4.wizards.util.WizardsRegistryReader;

/**
 * Abstract baseclass for wizard registries that listen to extension changes.
 *
 * @author Luthiger <br />
 *         see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry
 */
@SuppressWarnings("restriction")
public abstract class AbstractExtensionWizardRegistry
        extends AbstractWizardRegistry {

	private static final String PLUGIN_ID = "org.eclipse.ui";

	@Inject
	private IExtensionRegistry extensionRegistry; // NOPMD

	@Inject
	private IEclipseContext context; // NOPMD

	@Inject
	private Logger log; // NOPMD by Luthiger on 20.01.13 18:29

	@Override
	protected void doInitialize() {
		final WizardsRegistryReader lReader = new WizardsRegistryReader(
		        getPlugin(), getExtensionPoint(), context, extensionRegistry,
		        log);
		setWizardElements(lReader.getWizardElements());
		setPrimaryWizards(lReader.getPrimaryWizards());
		// registerWizards(getWizardElements());
	}

	/**
	 * Return the extension point id that should be used for extension registry
	 * queries.
	 *
	 * @return String the extension point id
	 */
	protected abstract String getExtensionPoint();

	/**
	 * Return the plugin id that should be used for extension registry queries.
	 *
	 * @return the plugin id
	 */
	protected String getPlugin() {
		return PLUGIN_ID;
	}

}
