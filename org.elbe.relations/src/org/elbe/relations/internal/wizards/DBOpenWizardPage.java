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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.forms.FormDBConnection;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.utility.WizardHelper;

/**
 * Page to display the form to change the database connection.
 * 
 * @author Luthiger
 */
public class DBOpenWizardPage extends WizardPage implements IUpdateListener {
	private FormDBConnection form;

	@Inject
	private IEclipseContext context;

	/**
	 * @param inPageName
	 */
	public DBOpenWizardPage() {
		super("DBOpenWizardPage"); //$NON-NLS-1$
		setTitle(RelationsMessages.getString("DBOpenWizardPage.view.title")); //$NON-NLS-1$
		setImageDescriptor(RelationsImages.WIZARD_EDIT_DB.getDescriptor());
	}

	@Override
	public void createControl(final Composite inParent) {
		final int lColumns = 2;
		final Composite lComposite = WizardHelper.createComposite(inParent,
				lColumns);
		form = FormDBConnection.createFormDBConnection(lComposite, lColumns,
				context);
		form.addUpdateListener(this);
		setControl(lComposite);
	}

	public boolean saveChanges() {
		return form.saveChanges();
	}

	@Override
	public void dispose() {
		form.removeUpdateListener(this);
		form.dispose();
		super.dispose();
	}

	@Override
	public void onUpdate(final IStatus inStatus) {
		setErrorMessage(FormUtility.getErrorMessage(inStatus));
		setPageComplete(form.getPageComplete());
	}

}
