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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.internal.forms.FormAssociate;
import org.elbe.relations.models.IAssociationsModel;

/**
 * Dialog to edit the item's associations.
 * 
 * @author Benno Luthiger 
 */
public class RelationsEditWizardPage extends WizardPage {
	private FormAssociate form;
	private final IAssociationsModel model;
	private final IEclipseContext context;

	/**
	 * RelationsEditWizardPage
	 * 
	 * @param inModel
	 *            IAssociationsModel
	 * @param inContext
	 */
	public RelationsEditWizardPage(final IAssociationsModel inModel,
			final IEclipseContext inContext) {
		super("RelationsEditWizardPage"); //$NON-NLS-1$
		setTitle(RelationsMessages
				.getString("RelationsEditWizardPage.view.title")); //$NON-NLS-1$
		model = inModel;
		context = inContext;
	}

	@Override
	public void createControl(final Composite inParent) {
		form = FormAssociate.createFormAssociate(inParent, model, context);
		form.initialize();
		setControl(form.getControl());
	}

	public void saveChanges() throws BOMException {
		form.saveChanges();
	}

	public void undoChanges() throws BOMException {
		form.undoChanges();
	}

	@Override
	public void dispose() {
		form.dispose();
		super.dispose();
	}

}
