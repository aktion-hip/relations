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
 * The export wizard allows the user to choose which nested export wizard to
 * run. The set of available wizards comes from the export wizard extension
 * point.
 * 
 * @author Luthiger
 * @see org.eclipse.ui.internal.dialogs.ExportWizard
 */
@SuppressWarnings("restriction")
public class ExportWizard extends AbstractExtensionWizard {

	@Inject
	@Named(AbstractExtensionWizard.EXPORT_WIZARD_REGISTRY)
	private ExportWizardRegistry exportWizardRegistry;

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
		IWizardCategory lRoot = exportWizardRegistry.getRootCategory();
		final IWizardDescriptor[] lPrimary = exportWizardRegistry
				.getPrimaryWizards();

		if (categoryId != null) {
			lRoot = processCategories(lRoot);
		}

		mainPage = new ExportWizardSelectionPage(selection, lRoot, lPrimary);
		addPage(mainPage);
	}

	/**
	 * @param inSelection
	 *            IStructuredSelection
	 */
	public void init(final IStructuredSelection inSelection) {
		selection = inSelection;

		if (getWindowTitle() == null) {
			setWindowTitle(WorkbenchMessages.ExportWizard_title);
		}
		setDefaultPageImageDescriptor(WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ));
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
