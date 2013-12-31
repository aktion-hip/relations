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

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.internal.models.UnsavedAssociationsModel;
import org.elbe.relations.internal.wizards.interfaces.INewWizard;
import org.elbe.relations.internal.wizards.interfaces.IRelationsSaveHelper;
import org.elbe.relations.models.IAssociationsModel;
import org.elbe.relations.models.IBrowserItem;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.models.LightWeightAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.exc.VException;

/**
 * Abstract wizard class to create new items.<br />
 * Note: this is an Eclipse 3 wizard. To make it e4, let the values for the
 * annotated field be injected (instead of using the method init()).
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractNewWizard extends Wizard implements INewWizard {
	protected final static String ERROR_DIALOG = RelationsMessages
	        .getString("AbstractNewWizard.title.error"); //$NON-NLS-1$

	private RelationsNewWizardPage pageRelations;
	private UnsavedAssociationsModel model = null;

	@Inject
	private Logger log;

	@Inject
	private IEclipseContext context;

	@Inject
	private IBrowserManager browserManager;

	/**
	 * AbstractNewWizard
	 */
	public AbstractNewWizard() {
		super();
	}

	/**
	 * We use the workbench selection to suggest an initial association.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(final IWorkbench inWorkbench,
	        final IStructuredSelection inSelection) {
		log = (Logger) inWorkbench.getAdapter(Logger.class);
		context = (IEclipseContext) inWorkbench
		        .getAdapter(IEclipseContext.class);
		browserManager = (IBrowserManager) inWorkbench
		        .getAdapter(IBrowserManager.class);

		init(inSelection);
	}

	protected Logger log() {
		return log;
	}

	protected IEclipseContext getEclipseContext() {
		return context;
	}

	/**
	 * Adds the <code>RelationsNewWizardPage</code> to this wizard.
	 * 
	 * @param inTitle
	 *            String to display
	 * @param inDescription
	 *            String to display
	 */
	protected void addPages(final String inTitle, final String inDescription) {
		pageRelations = ContextInjectionFactory.make(
		        RelationsNewWizardPage.class, context);
		pageRelations.setTitle(inTitle);
		pageRelations.setDescription(inDescription);
		addPage(pageRelations);
	}

	protected void prepareFinish(final AbstractRelationsWizardPage inPage,
	        final Image inImage) {
		inPage.setRelationsSaveHelper(new IRelationsSaveHelper() {
			@Override
			public void saveWith(final IItem inItem) throws BOMException {
				model.replaceCenter(new ItemAdapter(inItem, inImage, context));
				model.saveChanges();
			}
		});
	}

	/**
	 * We use the workbench selection to suggest an initial association.
	 * 
	 * @see org.elbe.relations.internal.wizards.interfaces.INewWizard#init(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(final IStructuredSelection inSelection) {
		try {
			ItemAdapter lSelected = null;
			if ((inSelection != null) && !inSelection.isEmpty()
			        && (inSelection.getFirstElement() instanceof IBrowserItem)) {
				lSelected = (ItemAdapter) ((IBrowserItem) inSelection
				        .getFirstElement()).getModel();
			}
			if (lSelected == null) {
				lSelected = browserManager.getSelectedModel();
			}
			if (lSelected == null) {
				model = UnsavedAssociationsModel.createModel(createDummy(),
				        context, RelationsImages.TEXT.getImage());
			} else {
				model = UnsavedAssociationsModel.createModel(createDummy(),
				        context, lSelected);
			}
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
		setWindowTitle(RelationsMessages
		        .getString("AbstractNewWizard.view.title")); //$NON-NLS-1$
	}

	private IItem createDummy() {
		final ILightWeightItem lDummy = new ILightWeightItem() {

			@Override
			public long getID() {
				return 0;
			}

			@Override
			public int getItemType() {
				return 0;
			}

			@Override
			public String getCreated() throws VException {
				return null;
			}
		};
		return new LightWeightAdapter(lDummy);
	}

	/**
	 * Returns this wizard's model for the newly created item's associations.
	 * 
	 * @return IAssociationsModel
	 */
	public IAssociationsModel getNewModel() {
		return model;
	}

}
