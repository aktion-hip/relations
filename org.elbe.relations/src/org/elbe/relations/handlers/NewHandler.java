/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
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

import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.internal.wizards.e4.AbstractExtensionWizard;
import org.elbe.relations.internal.wizards.e4.NewWizard;

/**
 * The handler for the <code>New ...</code> wizard.
 * 
 * @author Luthiger
 */
public class NewHandler extends AbstractExtensionHandler {

	@Execute
	public void execute(
			@Named(IServiceConstants.ACTIVE_SHELL) final Shell inShell,
			final IEclipseContext inContext) throws InvocationTargetException,
			InterruptedException {

		executeHandler(inShell, inContext);
	}

	private void executeHandler(final Shell inShell,
			final IEclipseContext inContext) {
		// prepare context
		inContext.set(AbstractExtensionWizard.NEW_WIZARD_REGISTRY,
				new ContextFunction() {
					@Override
					public Object compute(final IEclipseContext inContext) {
						return ContextInjectionFactory
								.make(org.elbe.relations.internal.wizards.e4.NewWizardRegistry.class,
										inContext);
					}
				});

		final NewWizard lWizard = ContextInjectionFactory.make(NewWizard.class,
				inContext);
		lWizard.setCategoryId(null);
		lWizard.init(StructuredSelection.EMPTY);

		runWizardDialog(inShell, lWizard);
	}

}
