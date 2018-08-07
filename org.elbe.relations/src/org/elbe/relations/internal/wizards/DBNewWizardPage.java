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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.IDBChange;
import org.elbe.relations.internal.forms.FormDBNew;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.utility.WizardHelper;

/**
 * Wizard page to display the input field for that the user can enter the name
 * of the embedded database to be created.
 *
 * @author Luthiger Created on 05.11.2006
 */
public class DBNewWizardPage extends WizardPage implements IWizardPage,
IUpdateListener {
	private FormDBNew form;

	@Inject
	private IEclipseContext context;

	public DBNewWizardPage() {
		super("DBNewWizardPage"); //$NON-NLS-1$
		setTitle(RelationsMessages.getString("DBNewWizardPage.view.title")); //$NON-NLS-1$
		setMessage(RelationsMessages
				.getString("DBNewWizardPage.wizard.message")); //$NON-NLS-1$
		setImageDescriptor(RelationsImages.WIZARD_NEW_DB.getDescriptor());
	}

	@Override
	public void createControl(final Composite parent) {
		final int columns = 2;
		final Composite composite = WizardHelper.createComposite(parent,
				columns);
		this.form = FormDBNew.createFormDBNew(composite, columns, this.context);
		this.form.addUpdateListener(this);
		setControl(composite);
		setPageComplete(false);
	}

	@Override
	public void onUpdate(final IStatus status) {
		setErrorMessage(FormUtility.getErrorMessage(status));
		setPageComplete(this.form.getPageComplete());
	}

	@Override
	public void dispose() {
		this.form.removeUpdateListener(this);
		this.form.dispose();
		super.dispose();
	}

	/**
	 * @return {@link IDBChange} the db catalog creation handler based on the
	 *         user input.
	 */
	public IDBChange getResultObject() {
		return this.form.getResultObject();
	}

}
