/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2011-2016, Benno Luthiger
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

import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.internal.e4.wizards.AbstractExtensionWizard;
import org.elbe.relations.internal.e4.wizards.ImportWizard;
import org.elbe.relations.internal.e4.wizards.ImportWizardRegistry;

/**
 * The handler for the <code>Import...</code> wizard.
 *
 *
 * @author lbenno
 */
public class ImportHandler extends AbstractExtensionHandler {

	@Execute
	public void execute(
	        @Named(IServiceConstants.ACTIVE_SHELL) final Shell inShell,
	        final IEclipseContext inContext) {
		executeHandler(inShell, inContext);
	}

	private void executeHandler(final Shell inShell,
	        final IEclipseContext inContext) {
		// prepare context
		inContext.set(AbstractExtensionWizard.IMPORT_WIZARD_REGISTRY,
		        new ContextFunction() {
			        @Override
			        public Object compute(final IEclipseContext inContext) {
				        return ContextInjectionFactory
		                        .make(ImportWizardRegistry.class, inContext);
			        }
		        });
		final ImportWizard lWizard = ContextInjectionFactory
		        .make(ImportWizard.class, inContext);
		lWizard.setCategoryId(null);
		lWizard.init(StructuredSelection.EMPTY);

		runWizardDialog(inShell, lWizard);
	}

}