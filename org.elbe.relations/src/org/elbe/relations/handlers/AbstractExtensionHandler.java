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
package org.elbe.relations.handlers;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.elbe.relations.internal.e4.wizards.AbstractExtensionWizard;

/**
 * Base class for handlers for the <code>org.eclipse.ui.newWizards</code>,
 * <code>org.eclipse.ui.exportWizards</code> and
 * <code>org.eclipse.ui.importWizards</code> commands.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractExtensionHandler {

	protected void runWizardDialog(final Shell inShell,
	        final AbstractExtensionWizard inWizard) {
		final IDialogSettings lWorkbenchSettings = WorkbenchPlugin.getDefault()
		        .getDialogSettings();
		IDialogSettings lWizardSettings = lWorkbenchSettings
		        .getSection("NewWizardAction");
		if (lWizardSettings == null) {
			lWizardSettings = lWorkbenchSettings
			        .addNewSection("NewWizardAction");
		}
		inWizard.setDialogSettings(lWizardSettings);
		inWizard.setForcePreviousAndNextButtons(true);

		final WizardDialog dialog = new WizardDialog(inShell, inWizard);
		dialog.create();
		dialog.open();
	}

}
