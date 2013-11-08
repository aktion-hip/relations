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

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMTruncationException;
import org.elbe.relations.internal.wizards.interfaces.IItemWizardPage;

/**
 * Wizard to edit a text item, i.e. the item content and the item's relations.
 * 
 * @author Luthiger Created on 10.10.2006
 */
@SuppressWarnings("restriction")
public class TextEditWizard extends AbstractEditWizard {
	private TextEditWizardPage page;

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;

	@Override
	public void addPages() {
		page = ContextInjectionFactory.make(TextEditWizardPage.class, context);
		page.setModel(getModel());
		addPage(page);
		addPages(getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		try {
			((IItemWizardPage) page).save();
			super.performFinish();
			return true;
		}
		catch (final BOMTruncationException exc) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					ERROR_DIALOG, exc.getMessage());
			return false;
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
			return false;
		}
	}

}
