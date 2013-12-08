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
package org.elbe.relations.internal.wizards;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.IDBChange;
import org.elbe.relations.internal.utility.DBPreconditionException;
import org.elbe.relations.internal.wizards.interfaces.INewWizard;

/**
 * Wizard to create a new embedded database.<br />
 * Note: this is an Eclipse 3 wizard. To make it e4, let the values for the
 * annotated field be injected (instead of using the method init()).
 * 
 * @author Luthiger
 */
public class DBNewWizard extends Wizard implements INewWizard {
	private DBNewWizardPage page;

	@Inject
	private IEclipseContext context;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Override
	public void init(final IWorkbench inWorkbench,
	        final IStructuredSelection inSelection) {
		context = (IEclipseContext) inWorkbench
		        .getAdapter(IEclipseContext.class);
		shell = inWorkbench.getDisplay().getActiveShell();

		this.init(inSelection);
	}

	@Override
	public void init(final IStructuredSelection inSelection) {
		setWindowTitle(RelationsMessages.getString("DBNewWizard.view.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		page = ContextInjectionFactory.make(DBNewWizardPage.class, context);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		final IDBChange lCreateDB = page.getResultObject();
		try {
			lCreateDB.checkPreconditions();
			BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
				@Override
				public void run() {
					lCreateDB.execute();
				}
			});
			return true;
		}
		catch (final DBPreconditionException exc) {
			MessageDialog
			        .openError(
			                new Shell(Display.getCurrent()),
			                RelationsMessages
			                        .getString("FormDBConnection.error.title"), exc.getMessage()); //$NON-NLS-1$
		}
		return false;
	}

}
