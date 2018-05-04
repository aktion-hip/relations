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
package org.elbe.relations.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.internal.forms.CloudConfigurationHelperDialog;
import org.elbe.relations.internal.services.CloudConfigurationHelperRegistry;

/**
 * Handler to manage the <code>ICloudProviderConfigurationHelper</code>. This
 * class is starting a dialog displaying the registered classes that help to
 * access the cloud.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public class CloudConfigurationHelper {

	@Inject
	private CloudConfigurationHelperRegistry helperRegistry;

	@Execute
	public void execute(
			@Named(IServiceConstants.ACTIVE_SHELL) final Shell shell,
			final Logger log) {
		final CloudConfigurationHelperDialog dialog = new CloudConfigurationHelperDialog(
				shell, this.helperRegistry.getHelpers(), log);
		if (dialog.open() == Window.OK) {
			// dialog.getResult();
		}

	}

}
