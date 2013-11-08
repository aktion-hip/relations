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
package org.elbe.relations.handlers.item;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.internal.services.IItemEditWizard;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;

/**
 * Handler to edit the selected item.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ItemEditHandler {

	@Execute
	public void execute(final IEclipseContext inContext,
			final IBrowserManager inBrowserManager,
			final IEventBroker inEventBroker, final Logger inLog) {
		final ItemAdapter lSelected = inBrowserManager.getSelectedModel();
		if (lSelected == null) {
			return;
		}

		final Class<? extends IItemEditWizard> lWizardClass = lSelected
				.getItemEditWizard();
		final IItemEditWizard lWizard = ContextInjectionFactory.make(
				lWizardClass, inContext);
		lWizard.setModel(lSelected);
		final WizardDialog lDialog = new WizardDialog(Display.getCurrent()
				.getActiveShell(), lWizard);
		lDialog.open();

	}

	/**
	 * @param inBrowserManager
	 *            {@link IBrowserManager}
	 * @return boolean <code>true</code> if there's a selected item in the
	 *         browser
	 */
	@CanExecute
	boolean checkForBrowserItem(final IBrowserManager inBrowserManager) {
		return inBrowserManager.getSelectedModel() != null;
	}

}
