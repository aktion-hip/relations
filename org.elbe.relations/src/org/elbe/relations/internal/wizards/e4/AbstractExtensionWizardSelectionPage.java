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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Base class for wizard selection pages, i.e. the page to select registered
 * new, export or import wizards.
 * 
 * @author Luthiger
 */
public abstract class AbstractExtensionWizardSelectionPage extends WizardPage {

	private boolean canFinishEarly = false;
	private boolean hasPages;
	private IWizardNode selectedNode;
	private final List<IWizardNode> selectedWizardNodes = new ArrayList<IWizardNode>();
	protected IStructuredSelection currentResourceSelection;

	/**
	 * AbstractExtensionWizardSelectionPage constructor.
	 * 
	 * @param inPageName
	 */
	protected AbstractExtensionWizardSelectionPage(final String inPageName) {
		super(inPageName);
	}

	@Override
	public IWizardPage getNextPage() {
		if (selectedNode == null) {
			return null;
		}

		final boolean isCreated = selectedNode.isContentCreated();

		final IWizard wizard = selectedNode.getWizard();

		if (wizard == null) {
			setSelectedNode(null);
			return null;
		}

		if (!isCreated) {
			// Allow the wizard to create its pages
			wizard.addPages();
		}

		return wizard.getStartingPage();
	}

	/**
	 * 
	 */
	public void advanceToNextPageOrFinish() {
		if (canFlipToNextPage()) {
			getContainer().showPage(getNextPage());
		} else if (canFinishEarly()) {
			if (getWizard().performFinish()) {
				((WizardDialog) getContainer()).close();
			}
		}
	}

	/**
	 * @param inCanFinishEarly
	 */
	public void setCanFinishEarly(final boolean inNewValue) {
		canFinishEarly = inNewValue;
	}

	public boolean canFinishEarly() {
		return canFinishEarly;
	}

	public void setHasPages(final boolean inNewValue) {
		hasPages = inNewValue;
	}

	@Override
	public boolean canFlipToNextPage() {
		if (hasPages) {
			return selectedNode != null;
		}
		return false;
	}

	/**
	 * @param inSelectedNode
	 */
	public void selectWizardNode(final IWizardNode inSelectedNode) {
		setSelectedNode(inSelectedNode);
	}

	/**
	 * Sets or clears the currently selected wizard node within this page.
	 * 
	 * @param inNode
	 *            the wizard node, or <code>null</code> to clear
	 */
	protected void setSelectedNode(final IWizardNode inNode) {
		addSelectedNode(inNode);
		selectedNode = inNode;
		if (isCurrentPage()) {
			getContainer().updateButtons();
		}
	}

	/**
	 * Adds the given wizard node to the list of selected nodes if it is not
	 * already in the list.
	 * 
	 * @param inNode
	 *            the wizard node, or <code>null</code>
	 */
	private void addSelectedNode(final IWizardNode inNode) {
		if (inNode == null) {
			return;
		}

		if (selectedWizardNodes.contains(inNode)) {
			return;
		}

		selectedWizardNodes.add(inNode);
	}

	/**
	 * Returns the currently selected wizard node within this page.
	 * 
	 * @return the wizard node, or <code>null</code> if no node is selected
	 */
	public IWizardNode getSelectedNode() {
		return selectedNode;
	}

	public IStructuredSelection getCurrentResourceSelection() {
		return currentResourceSelection;
	}

	@Override
	public void dispose() {
		super.dispose();
		// notify nested wizards
		for (int i = 0; i < selectedWizardNodes.size(); i++) {
			selectedWizardNodes.get(i).dispose();
		}
	}

}
