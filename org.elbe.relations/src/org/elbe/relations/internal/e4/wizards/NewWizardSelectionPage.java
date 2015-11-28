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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.elbe.relations.internal.e4.wizards.util.IWizardCategory;
import org.elbe.relations.internal.e4.wizards.util.IWizardDescriptor;

/**
 * New wizard selection tab that allows the user to either select a registered
 * 'New' wizard to be launched, or to select a solution or projects to be
 * retrieved from an available server. This page contains two visual tabs that
 * allow the user to perform these tasks.
 *
 * Temporarily has two inner pages. The new format page is used if the system is
 * currently aware of activity categories.
 *
 * @author Luthiger <br />
 *         see org.eclipse.ui.internal.dialogs.NewWizardSelectionPage
 */
public class NewWizardSelectionPage
        extends AbstractExtensionWizardSelectionPage {

	private final IWizardCategory wizardCategories;
	private final IWizardDescriptor[] primaryWizards;
	private NewWizardNewPage newResourcePage;

	/**
	 * NewWizardSelectionPage constructor
	 *
	 * @param inSelection
	 *            {@link IStructuredSelection}
	 * @param inContext
	 * @param inCategories
	 *            {@link IWizardCategory}
	 * @param inPrimary
	 *            IWizardDescriptor[]
	 */
	protected NewWizardSelectionPage(final IStructuredSelection inSelection,
	        IEclipseContext inContext, final IWizardCategory inCategories,
	        final IWizardDescriptor[] inPrimary) {
		super("newWizardSelectionPage");

		setPageComplete(false);
		setTitle("Select a wizard");
		setCurrtenResourceSelection(inSelection);
		setContext(inContext);
		wizardCategories = inCategories;
		primaryWizards = inPrimary;
	}

	@Override
	public void createControl(final Composite inParent) {
		newResourcePage = new NewWizardNewPage(this, getContext(),
		        wizardCategories, primaryWizards);
		newResourcePage.setDialogSettings(getDialogSettings());

		final Control lControl = newResourcePage.createControl(inParent);
		setControl(lControl);
	}
}
