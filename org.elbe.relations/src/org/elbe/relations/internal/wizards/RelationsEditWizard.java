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
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.models.IAssociationsModel;

/**
 * Wizard to edit an item's relations, calling
 * <code>RelationsEditWizardPage</code>.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class RelationsEditWizard extends Wizard {
	private RelationsEditWizardPage page;
	private IAssociationsModel model;

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;

	/**
	 * RelationsEditWizard constructor.
	 * 
	 */
	public RelationsEditWizard() {
		super();
		setWindowTitle(RelationsMessages
				.getString("RelationsEditWizard.view.title")); //$NON-NLS-1$
	}

	/**
	 * @param inModel
	 *            {@link IAssociationsModel} the model to edit
	 */
	public void setModel(final IAssociationsModel inModel) {
		model = inModel;
	}

	@Override
	public void addPages() {
		page = new RelationsEditWizardPage(model, context);
		addPage(page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		try {
			page.saveChanges();
			return true;
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
			return false;
		}
	}

	@Override
	public boolean performCancel() {
		try {
			page.undoChanges();
			return true;
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
			return false;
		}
	}

}
