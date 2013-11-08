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

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.internal.wizards.RelationsEditWizard;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.services.IRelationsBrowser;
import org.hip.kernel.exc.VException;

/**
 * Handler to display the dialog to edit the item's relations.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class RelationEditHandler {

	@Inject
	private Logger log;

	/**
	 * Display the dialog to edit the relations.
	 * 
	 * @param inShell
	 *            {@link Shell}
	 * @param inBrowserManager
	 *            {@link IBrowserManager}
	 * @param inContext
	 *            {@link IEclipseContext}
	 */
	@Execute
	void showRelationsEditor(
			@Named(IServiceConstants.ACTIVE_SHELL) final Shell inShell,
			final IBrowserManager inBrowserManager,
			final IEclipseContext inContext) {
		final ItemAdapter lSelected = inBrowserManager.getSelectedModel();
		if (lSelected == null) {
			return;
		}

		try {
			final CentralAssociationsModel lCenter = inBrowserManager
					.getCenterModel();
			final RelationsEditWizard lWizard = ContextInjectionFactory.make(
					RelationsEditWizard.class, inContext);
			lWizard.setModel(lCenter.getAssociationsModel(lSelected));
			final WizardDialog lDialog = new WizardDialog(inShell, lWizard);
			lDialog.open();
		}
		catch (final InjectionException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * @param inActivePart
	 *            {@link MPart}
	 * @return boolean <code>true</code> if an element in the view is selected
	 */
	@CanExecute
	boolean checkSelectionList(@Active final MPart inActivePart,
			final IBrowserManager inBrowserManager) {
		final Object lControl = inActivePart.getObject();
		if (lControl instanceof IRelationsBrowser) {
			return inBrowserManager.getSelectedModel() != null;
		}
		return false;
	}

}
