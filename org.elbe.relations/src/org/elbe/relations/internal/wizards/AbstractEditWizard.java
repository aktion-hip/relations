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

import java.sql.SQLException;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.internal.services.IItemEditWizard;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.exc.VException;

/**
 * Abstract wizard class to edit items.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractEditWizard extends Wizard implements
        IItemEditWizard {
	protected final static String ERROR_DIALOG = RelationsMessages
	        .getString("AbstractEditWizard.title.error"); //$NON-NLS-1$
	private RelationsEditWizardPage pageRelations;
	private ItemAdapter model;

	@Inject
	private Logger log;

	@Inject
	private IEclipseContext context;

	@Inject
	private IBrowserManager browserManager;

	/**
	 * AbstractEditWizard constructor.
	 */
	public AbstractEditWizard() {
		super();
		setWindowTitle(RelationsMessages
		        .getString("AbstractEditWizard.view.title")); //$NON-NLS-1$
	}

	@Override
	public void setModel(final ItemAdapter inModel) {
		model = inModel;
	}

	/**
	 * @return {@link ItemAdapter} the model to edit with this wizard
	 */
	protected ItemAdapter getModel() {
		return model;
	}

	protected void addPages(final ItemAdapter inModel) {
		try {
			final CentralAssociationsModel lCenter = browserManager
			        .getCenterModel();
			pageRelations = new RelationsEditWizardPage(
			        lCenter.getAssociationsModel(inModel), context);
			addPage(pageRelations);
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		try {
			pageRelations.saveChanges();
			return true;
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
			return false;
		}
	}

	/**
	 * @see Wizard#performCancel()
	 */
	@Override
	public boolean performCancel() {
		try {
			if (pageRelations != null) {
				pageRelations.undoChanges();
			}
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
		return true;
	}

}
