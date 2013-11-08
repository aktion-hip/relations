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

import java.util.StringTokenizer;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.wizards.IWizardCategory;

/**
 * Base class for wizards provided by Eclipse extensions, i.e. implementing the
 * <code>org.eclipse.ui.newWizards</code>,
 * <code>org.eclipse.ui.exportWizards</code> or
 * <code>org.eclipse.ui.importWizards</code> extension point.
 * 
 * @author Luthiger
 */
public abstract class AbstractExtensionWizard extends Wizard {

	public static final String NEW_WIZARD_REGISTRY = "relations.new.wizard.registry";
	public static final String EXPORT_WIZARD_REGISTRY = "relations.export.wizard.registry";
	public static final String IMPORT_WIZARD_REGISTRY = "relations.import.wizard.registry";

	static final String CATEGORY_SEPARATOR = "/"; //$NON-NLS-1$

	protected String categoryId;

	/**
	 * @param inRoot
	 * @return
	 */
	protected IWizardCategory processCategories(IWizardCategory inRoot) {
		IWizardCategory lCategories = inRoot;
		final StringTokenizer lFamilyTokenizer = new StringTokenizer(
				categoryId, CATEGORY_SEPARATOR);
		while (lFamilyTokenizer.hasMoreElements()) {
			lCategories = getChildWithID(lCategories,
					lFamilyTokenizer.nextToken());
			if (lCategories == null) {
				break;
			}
		}
		if (lCategories != null) {
			inRoot = lCategories;
		}
		return inRoot;
	}

	/**
	 * Returns the child collection element for the given id
	 */
	IWizardCategory getChildWithID(final IWizardCategory inParent,
			final String inID) {
		final IWizardCategory[] lChildren = inParent.getCategories();
		for (int i = 0; i < lChildren.length; ++i) {
			final IWizardCategory lCurrentChild = lChildren[i];
			if (lCurrentChild.getId().equals(inID)) {
				return lCurrentChild;
			}
		}
		return null;
	}

	/**
	 * @param inString
	 */
	public void setCategoryId(final String inID) {
		categoryId = inID;
	}

	/**
	 * @return String the category ID
	 */
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public boolean performFinish() {
		// TODO
		// save our selection state
		// mainPage.saveWidgetValues();
		// if we're finishing from the main page then perform finish on the
		// selected wizard.
		if (getContainer().getCurrentPage() == getMainPage()) {
			if (getMainPage().canFinishEarly()) {
				final IWizard lWizard = getMainPage().getSelectedNode()
						.getWizard();
				lWizard.setContainer(getContainer());
				return lWizard.performFinish();
			}
		}
		return true;
	}

	protected abstract AbstractExtensionWizardSelectionPage getMainPage();

}
