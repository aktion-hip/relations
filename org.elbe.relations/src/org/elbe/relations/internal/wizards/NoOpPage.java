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
package org.elbe.relations.internal.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.internal.utility.WizardHelper;

/**
 * No op page. Can be used to disable a wizard.
 * 
 * @author Luthiger Created on 05.05.2007
 */
public class NoOpPage extends WizardPage implements IWizardPage {
	private final String message;

	/**
	 * @param inPageName
	 *            String page name
	 * @param inMessage
	 *            String message to display
	 */
	public NoOpPage(final String inPageName, final String inMessage) {
		super(inPageName);
		message = inMessage;
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
		setControl(WizardHelper.createComposite(inParent, 1));
		setErrorMessage(message);
		setPageComplete(false);
	}

}
