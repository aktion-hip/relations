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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.elbe.relations.internal.wizards.e4.util.WorkbenchWizardNode;

/**
 * New wizard selection tab that allows the user to select a registered 'Import'
 * wizard to be launched.
 * 
 * @author Luthiger
 */
public class ImportWizardNewPage extends AbstractWizardNewPage {

	/**
	 * ImportWizardNewPage constructor.
	 * 
	 * @param inMainPage
	 *            {@link AbstractExtensionWizardSelectionPage}
	 * @param inWizardCategories
	 *            {@link IWizardCategory}
	 * @param inPrimaryWizards
	 *            IWizardDescriptor[]
	 */
	public ImportWizardNewPage(
			final AbstractExtensionWizardSelectionPage inMainPage,
			final IWizardCategory inWizardCategories,
			final IWizardDescriptor[] inPrimaryWizards) {
		super(inMainPage, inWizardCategories, inPrimaryWizards);
	}

	@Override
	protected WorkbenchWizardNode createNode(
			final AbstractExtensionWizardSelectionPage inWizardPage,
			final IWizardDescriptor inElement) {
		return new WorkbenchWizardNode(inWizardPage, inElement) {
			@Override
			public IImportWizard createWizard() throws CoreException {
				final IWorkbenchWizard outWizard = wizardElement.createWizard();
				return (outWizard instanceof IImportWizard) ? (IImportWizard) outWizard
						: null;
			}
		};
	}

}
