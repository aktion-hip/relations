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
package org.elbe.relations.internal.e4.wizards;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.e4.wizards.util.IWizardCategory;
import org.elbe.relations.internal.e4.wizards.util.IWizardDescriptor;

/**
 * The export wizard allows the user to choose which nested export wizard to
 * run. The set of available wizards comes from the export wizard extension
 * point.
 *
 * @author Luthiger <br />
 *         see org.eclipse.ui.internal.dialogs.ExportWizard
 */
public class ExportWizard extends AbstractExtensionWizard {

	@Inject
	@Named(AbstractExtensionWizard.EXPORT_WIZARD_REGISTRY)
	private ExportWizardRegistry exportWizardRegistry;

	private AbstractExtensionWizardSelectionPage mainPage;

	private IStructuredSelection selection;

	@Override
	public void addPages() {
		IWizardCategory lRoot = exportWizardRegistry.getRootCategory();
		final IWizardDescriptor[] lPrimary = exportWizardRegistry
		        .getPrimaryWizards();

		if (categoryId != null) {
			lRoot = processCategories(lRoot);
		}

		mainPage = new ExportWizardSelectionPage(selection, getContext(), lRoot,
		        lPrimary);
		addPage(mainPage);
	}

	/**
	 * @param inSelection
	 *            IStructuredSelection
	 */
	public void init(final IStructuredSelection inSelection) {
		selection = inSelection;

		if (getWindowTitle() == null) {
			setWindowTitle(RelationsMessages.getString("Export.wizard.title")); //$NON-NLS-1$
		}
		setDefaultPageImageDescriptor(
		        RelationsImages.WIZARD_EXPORT.getDescriptor());
		setNeedsProgressMonitor(true);
	}

	@Override
	protected AbstractExtensionWizardSelectionPage getMainPage() {
		return mainPage;
	}

}
