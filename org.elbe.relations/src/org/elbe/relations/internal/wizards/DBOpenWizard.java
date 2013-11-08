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

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.Wizard;
import org.elbe.relations.RelationsMessages;

/**
 * Wizard to change the database.
 * 
 * @author Luthiger 
 */
public class DBOpenWizard extends Wizard {
	private DBOpenWizardPage page;

	@Inject
	private IEclipseContext context;

	/**
	 * DBOpenWizard constructor.
	 */
	public DBOpenWizard() {
		setWindowTitle(RelationsMessages.getString("DBOpenWizard.view.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		page = ContextInjectionFactory.make(DBOpenWizardPage.class, context);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		return page.saveChanges();
	}

}
