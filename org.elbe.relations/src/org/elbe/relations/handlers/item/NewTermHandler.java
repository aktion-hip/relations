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
package org.elbe.relations.handlers.item;

import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.internal.wizards.TermNewWizard;
import org.elbe.relations.services.IBrowserManager;

/**
 * The handler executed when the <code>new term</code> toolbar button is
 * clicked.
 *
 * @author Luthiger
 */
public class NewTermHandler extends AbastractNewItemHandler {

	@Execute
	public void execute(
	        @Named(IServiceConstants.ACTIVE_SHELL) final Shell inShell,
	        final IEclipseContext inContext,
	        final IBrowserManager inBrowserManager) {
		final TermNewWizard lWizard = ContextInjectionFactory
		        .make(TermNewWizard.class, inContext);
		// lWizard.init(createSelection(inBrowserManager));
		final WizardDialog lDialog = new WizardDialog(inShell, lWizard);
		lDialog.open();
	}
}
