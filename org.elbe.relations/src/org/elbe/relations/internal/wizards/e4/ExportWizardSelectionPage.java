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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * New wizard selection tab that allows the user to either select a registered
 * 'Export' wizard to be launched, or to select a solution or projects to be
 * retrieved from an available server. This page contains two visual tabs that
 * allow the user to perform these tasks.
 * 
 * @author Luthiger
 */
public class ExportWizardSelectionPage extends
		AbstractExtensionWizardSelectionPage {

	private final IWizardCategory wizardCategories;
	private final IWizardDescriptor[] primaryWizards;
	private ExportWizardNewPage exportResourcePage;

	/**
	 * ExportWizardSelectionPage constructor.
	 * 
	 * @param inSelection
	 *            {@link IStructuredSelection}
	 * @param inCategories
	 *            {@link IWizardCategory}
	 * @param inPrimary
	 *            IWizardDescriptor[]
	 */
	protected ExportWizardSelectionPage(final IStructuredSelection inSelection,
			final IWizardCategory inCategories,
			final IWizardDescriptor[] inPrimary) {
		super("exportWizardSelectionPage");

		setPageComplete(false);
		setTitle("Select a wizard");
		currentResourceSelection = inSelection;
		wizardCategories = inCategories;
		primaryWizards = inPrimary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createControl(final Composite inParent) {
		exportResourcePage = new ExportWizardNewPage(this, wizardCategories,
				primaryWizards);
		exportResourcePage.setDialogSettings(getDialogSettings());

		final Control lControl = exportResourcePage.createControl(inParent);
		setControl(lControl);
	}

}
