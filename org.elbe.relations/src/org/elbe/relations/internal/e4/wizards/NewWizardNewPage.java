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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.IWizard;
import org.elbe.relations.internal.e4.wizards.util.IWizardCategory;
import org.elbe.relations.internal.e4.wizards.util.IWizardDescriptor;
import org.elbe.relations.internal.e4.wizards.util.WorkbenchWizardNode;
import org.elbe.relations.internal.wizards.interfaces.INewWizard;

/**
 * New wizard selection tab that allows the user to select a registered 'New'
 * wizard to be launched.
 *
 * @author Luthiger
 */
public class NewWizardNewPage extends AbstractWizardNewPage {
	/**
	 * NewWizardNewPage constructor.
	 *
	 * @param inMainPage
	 *            {@link AbstractExtensionWizardSelectionPage}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @param inWizardCategories
	 *            {@link IWizardCategory}
	 * @param inPrimaryWizards
	 *            IWizardDescriptor[]
	 */
	public NewWizardNewPage(
	        final AbstractExtensionWizardSelectionPage inMainPage,
	        final IEclipseContext inContext,
	        final IWizardCategory inWizardCategories,
	        final IWizardDescriptor[] inPrimaryWizards) {
		super(inMainPage, inContext, inWizardCategories, inPrimaryWizards);
	}

	@Override
	protected WorkbenchWizardNode createNode(
	        final AbstractExtensionWizardSelectionPage inWizardPage,
	        final IWizardDescriptor inElement,
	        final IEclipseContext inContext) {
		return new WorkbenchWizardNode(inWizardPage, inElement, inContext) {
			@Override
			public INewWizard createWizard() throws CoreException {
				final IWizard outWizard = wizardElement.createWizard();
				return (outWizard instanceof INewWizard)
		                ? (INewWizard) outWizard : null;
			}
		};
	}

}
