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

import org.elbe.relations.internal.e4.wizards.util.IWizardCategory;
import org.elbe.relations.internal.e4.wizards.util.IWizardDescriptor;
import org.elbe.relations.internal.e4.wizards.util.IWizardRegistry;
import org.elbe.relations.internal.e4.wizards.util.WizardCollectionElement;
import org.elbe.relations.internal.e4.wizards.util.WorkbenchWizardElement;

/**
 * Abstract base class for various workbench wizards.
 *
 * @author Luthiger<br />
 *         see org.eclipse.ui.internal.wizards.AbstractWizardRegistry
 */
public abstract class AbstractWizardRegistry implements IWizardRegistry {

	private WorkbenchWizardElement[] primaryWizards;
	private WizardCollectionElement wizardElements;

	private boolean initialized = false;

	public void dispose() {
		primaryWizards = null;
		wizardElements = null;
		initialized = false;
	}

	/**
	 * Perform initialization of this registry. Should never be called by
	 * implementations.
	 */
	protected abstract void doInitialize();

	/**
	 * Return the wizard elements.
	 *
	 * @return the wizard elements
	 */
	protected WizardCollectionElement getWizardElements() {
		initialize();
		return wizardElements;
	}

	/**
	 * Read the contents of the registry if necessary.
	 */
	protected final synchronized void initialize() {
		if (isInitialized()) {
			return;
		}

		initialized = true;
		doInitialize();
	}

	/**
	 * Return whether the registry has been read.
	 *
	 * @return whether the registry has been read
	 */
	private boolean isInitialized() {
		return initialized;
	}

	/**
	 * Set the primary wizards.
	 *
	 * @param inPrimaryWizards
	 *            the primary wizards
	 */
	protected void setPrimaryWizards(
	        final WorkbenchWizardElement[] inPrimaryWizards) {
		primaryWizards = inPrimaryWizards;
	}

	/**
	 * Set the wizard elements.
	 *
	 * @param inWizardElements
	 *            the wizard elements
	 */
	protected void setWizardElements(
	        final WizardCollectionElement inWizardElements) {
		wizardElements = inWizardElements;
	}

	@Override
	public IWizardCategory findCategory(final String inId) {
		initialize();
		return wizardElements.findCategory(inId);
	}

	@Override
	public IWizardDescriptor findWizard(final String inId) {
		initialize();
		return wizardElements.findWizard(inId, true);
	}

	@Override
	public IWizardDescriptor[] getPrimaryWizards() {
		initialize();
		return primaryWizards;
	}

	@Override
	public IWizardCategory getRootCategory() {
		initialize();
		return wizardElements;
	}

}
