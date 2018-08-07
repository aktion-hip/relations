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
package org.elbe.relations.internal.wizards;

import javax.annotation.PostConstruct;
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
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.IDBChange;
import org.elbe.relations.internal.utility.DBPreconditionException;
import org.elbe.relations.internal.wizards.interfaces.INewWizard;

/**
 * Wizard to create a new embedded database.
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

	@PostConstruct
	public void init(
			@Named(IServiceConstants.ACTIVE_SELECTION) final IStructuredSelection selection) {
		setWindowTitle(RelationsMessages.getString("DBNewWizard.view.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		this.page = ContextInjectionFactory.make(DBNewWizardPage.class, this.context);
		addPage(this.page);
	}

	@Override
	public boolean performFinish() {
		final IDBChange createDB = this.page.getResultObject();
		try {
			createDB.checkPreconditions();
			BusyIndicator.showWhile(this.shell.getDisplay(), new Runnable() {
				@Override
				public void run() {
					createDB.execute();
				}
			});
			return true;
		}
		catch (final DBPreconditionException exc) {
			MessageDialog.openError(new Shell(Display.getCurrent()),
					RelationsMessages.getString("FormDBConnection.error.title"), //$NON-NLS-1$
					exc.getMessage());
		}
		return false;
	}

}
