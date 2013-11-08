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
package org.elbe.relations.internal.wizards.e4;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * The import wizard allows the user to choose which nested import wizard to
 * run. The set of available wizards comes from the import wizard extension
 * point.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ImportWizard extends AbstractExtensionWizard {

	@Inject
	@Named(AbstractExtensionWizard.IMPORT_WIZARD_REGISTRY)
	private ImportWizardRegistry importWizardRegistry;

	@Inject
	private IExtensionRegistry extensionRegistry;

	private AbstractExtensionWizardSelectionPage mainPage;

	private IStructuredSelection selection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		IWizardCategory lRoot = importWizardRegistry.getRootCategory();
		final IWizardDescriptor[] lPrimary = importWizardRegistry
				.getPrimaryWizards();

		if (categoryId != null) {
			lRoot = processCategories(lRoot);
		}

		mainPage = new ImportWizardSelectionPage(selection, lRoot, lPrimary);
		addPage(mainPage);
	}

	/**
	 * @param inSelection
	 *            IStructuredSelection
	 */
	public void init(final IStructuredSelection inSelection) {
		selection = inSelection;

		if (getWindowTitle() == null) {
			setWindowTitle(WorkbenchMessages.ImportWizard_title);
		}
		setDefaultPageImageDescriptor(WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ));
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.e4.AbstractExtensionWizard#getMainPage
	 * ()
	 */
	@Override
	protected AbstractExtensionWizardSelectionPage getMainPage() {
		return mainPage;
	}

}
