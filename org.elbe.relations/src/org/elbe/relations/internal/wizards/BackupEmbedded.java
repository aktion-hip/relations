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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.backup.ZipBackup;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;
import org.elbe.relations.internal.utility.WizardHelper;

/**
 * Wizard to backup the embedded database.<br />
 * Note: this is an Eclipse 3 wizard. To make it e4, let the values for the
 * annotated field be injected (instead of using the method init()).
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class BackupEmbedded extends Wizard implements IExportWizard {
	private final static MessageFormat SUCCESS_MSG = new MessageFormat(
	        RelationsMessages.getString("BackupEmbedded.feedback.success")); //$NON-NLS-1$
	private final static MessageFormat PROBLEMS_MSG = new MessageFormat(
	        RelationsMessages.getString("BackupEmbedded.feedback.problems")); //$NON-NLS-1$
	private final static String NO_OP_MESSAGE = RelationsMessages
	        .getString("BackupEmbedded.message.noop"); //$NON-NLS-1$

	@Inject
	private Logger log;

	@Inject
	private DBSettings dbSettings;

	@Inject
	private RelationsStatusLineManager statusLine;

	private BackupEmbeddedPage page;

	@Override
	public void init(final IWorkbench inWorkbench,
	        final IStructuredSelection inSelection) {
		log = (Logger) inWorkbench.getAdapter(Logger.class);
		dbSettings = (DBSettings) inWorkbench.getAdapter(DBSettings.class);
		statusLine = WizardHelper.getFromWorkbench(
		        RelationsStatusLineManager.class, inWorkbench);

		setWindowTitle(RelationsMessages.getString("BackupEmbedded.page.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		// if the actual data store is an external database, we don't offer
		// backup functionality
		if (!dbSettings.getDBConnectionConfig().isEmbedded()) {
			addPage(new NoOpPage("DontBackupPage", NO_OP_MESSAGE)); //$NON-NLS-1$
			return;
		}

		page = new BackupEmbeddedPage("BackupEmbeddedPage"); //$NON-NLS-1$
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		final String lCatalog = dbSettings.getCatalog();
		try {
			final String lDataDirectory = EmbeddedCatalogHelper
			        .getDBStorePath().getCanonicalPath()
			        + File.separator
			        + lCatalog;
			final ZipBackup lBackup = new ZipBackup(lDataDirectory,
			        page.getFileName());
			lBackup.backup();
			statusLine.showStatusLineMessage(SUCCESS_MSG
			        .format(new String[] { lCatalog }));
		}
		catch (final IOException exc) {
			MessageDialog
			        .openError(
			                getShell(),
			                RelationsMessages.getString("BackupEmbedded.error"), PROBLEMS_MSG.format(new String[] { lCatalog })); //$NON-NLS-1$
			log.error(exc, exc.getMessage());
		}
		return true;
	}

	@Override
	public void dispose() {
		if (page != null)
			page.dispose();
		super.dispose();
	}

}
