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

import java.text.MessageFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.utility.BibtexExporter;
import org.elbe.relations.internal.wizards.interfaces.IExportWizard;

/**
 * Wizard to export the content of all text items to a file in BibTEX format.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class ExportBibtex extends Wizard implements IExportWizard {
	private final static MessageFormat SUCCESS_MSG = new MessageFormat(
	        RelationsMessages.getString("ExportBibtex.feedback.success")); //$NON-NLS-1$
	private final static MessageFormat PROBLEMS_MSG = new MessageFormat(
	        RelationsMessages.getString("ExportBibtex.feedback.problems")); //$NON-NLS-1$

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;

	@Inject
	private DBSettings dbSettings;

	@Inject
	private RelationsStatusLineManager statusLine;

	private ExportBibtexPage page;

	@PostConstruct
	public void init() {
		setWindowTitle(
		        RelationsMessages.getString("ExportBibtex.window.title")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new ExportBibtexPage("ExportBibtexPage"); //$NON-NLS-1$
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		try {
			page.saveToHistory();

			final BibtexExporter lExporter = ContextInjectionFactory
			        .make(BibtexExporter.class, context);
			lExporter.setFileName(page.getFileName());
			lExporter.export();
			statusLine.showStatusLineMessage(SUCCESS_MSG
			        .format(new String[] { dbSettings.getCatalog() }));
		}
		catch (final Exception exc) {
			MessageDialog.openError(getShell(),
			        RelationsMessages.getString("ExportBibtex.error"), //$NON-NLS-1$
			        PROBLEMS_MSG
			                .format(new String[] { dbSettings.getCatalog() }));
			log.error(exc, exc.getMessage());
		}
		return true;
	}

	@Override
	public void dispose() {
		page.dispose();
		super.dispose();
	}

}
