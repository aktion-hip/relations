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
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.elbe.relations.internal.e4.wizards.util.IWizardCategory;
import org.elbe.relations.internal.e4.wizards.util.IWizardDescriptor;

/**
 * The new wizard is responsible for allowing the user to choose which new
 * (nested) wizard to run. The set of available new wizards comes from the new
 * extension point.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class NewWizard extends AbstractExtensionWizard {

	@Inject
	@Named(AbstractExtensionWizard.NEW_WIZARD_REGISTRY)
	private NewWizardRegistry newWizardRegistry;

	private AbstractExtensionWizardSelectionPage mainPage;

	private IStructuredSelection selection;

	@Override
	public void addPages() {
		IWizardCategory lRoot = newWizardRegistry.getRootCategory();
		final IWizardDescriptor[] lPrimary = newWizardRegistry
		        .getPrimaryWizards();

		if (categoryId != null) {
			lRoot = processCategories(lRoot);
		}
		mainPage = new NewWizardSelectionPage(selection, getContext(), lRoot,
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
			setWindowTitle(WorkbenchMessages.NewWizard_title);
		}
		setDefaultPageImageDescriptor(WorkbenchImages.getImageDescriptor(
		        IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ));
		setNeedsProgressMonitor(true);
	}

	@Override
	protected AbstractExtensionWizardSelectionPage getMainPage() {
		return mainPage;
	}

}
