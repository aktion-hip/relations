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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.internal.forms.AbstractEditForm;
import org.elbe.relations.internal.forms.FormAssociate;

/**
 * Page to add initial relations to the newly created item.
 * 
 * @author Luthiger
 */
public class RelationsNewWizardPage extends WizardPage {
	private FormAssociate form;
	private boolean loaded = false;

	@Inject
	private IEclipseContext context;

	/**
	 * RelationsNewWizardPage
	 */
	public RelationsNewWizardPage() {
		super("RelationsWizardPage"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(final Composite inParent) {
		form = FormAssociate.createFormAssociate(inParent, context);
		form.initialize();
		setControl(form.getControl());
	}

	/**
	 * @see AbstractRelationsWizardPage#getForm()
	 */
	protected AbstractEditForm getForm() {
		return form;
	}

	@Override
	public void setVisible(final boolean inVisible) {
		if (!loaded && inVisible) {
			form.loadModel(((AbstractNewWizard) getWizard()).getNewModel());
		}
		loaded = true;
		super.setVisible(inVisible);
	}

	@Override
	public void dispose() {
		if (form != null) {
			form.dispose();
		}
		super.dispose();
	}

}
