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

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMTruncationException;
import org.elbe.relations.internal.wizards.interfaces.IItemWizardPage;

/**
 * Wizard to create a new term entry.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class TermNewWizard extends AbstractNewWizard {

	private TermNewWizardPage page;

	@Override
	public void addPages() {
		page = ContextInjectionFactory.make(TermNewWizardPage.class,
		        getEclipseContext());
		addPage(page);
		addPages(
		        RelationsMessages.getString("TermNewWizard.page.title"), RelationsMessages.getString("TermNewWizard.page.msg")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean performFinish() {
		prepareFinish(page, RelationsImages.TERM.getImage());
		try {
			((IItemWizardPage) page).save();
		}
		catch (final BOMTruncationException exc) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
			        ERROR_DIALOG, exc.getMessage());
			return false;
		}
		catch (final BOMException exc) {
			log().error(exc, exc.getMessage());
			return false;
		}
		return true;
	}

}
