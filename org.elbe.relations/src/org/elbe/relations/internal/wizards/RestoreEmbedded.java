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
package org.elbe.relations.internal.wizards;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;
import org.elbe.relations.internal.utility.ZipRestore;
import org.elbe.relations.internal.wizards.interfaces.IImportWizard;

/**
 * Wizard to restore the state of an embedded database.
 *
 * @author Luthiger Created on 10.05.2007
 */
@SuppressWarnings("restriction")
public class RestoreEmbedded extends Wizard implements IImportWizard {
	private final static MessageFormat SUCCESS_MSG = new MessageFormat(
	        RelationsMessages.getString("RestoreEmbedded.feedback.success")); //$NON-NLS-1$
	private final static MessageFormat PROBLEMS_MSG = new MessageFormat(
	        RelationsMessages.getString("RestoreEmbedded.feedback.problems")); //$NON-NLS-1$
	private final static String NO_OP_MESSAGE = RelationsMessages
	        .getString("RestoreEmbedded.msg.noop"); //$NON-NLS-1$

	@Inject
	private DBSettings dbSettings;

	@Inject
	private RelationsStatusLineManager statusLineManager;

	@Inject
	private Logger log;

	@Inject
	private org.eclipse.e4.ui.workbench.IWorkbench workbench;

	@Inject
	private IApplicationContext appContext;

	private RestoreEmbeddedPage page;

	@PostConstruct
	public void init() {
		setWindowTitle(
		        RelationsMessages.getString("RestoreEmbedded.window.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		// if the actual data store is an external database, we don't offer
		// backup functionality
		if (!dbSettings.getDBConnectionConfig().isEmbedded()) {
			addPage(new NoOpPage("DontRestorePage", NO_OP_MESSAGE)); //$NON-NLS-1$
			return;
		}

		page = new RestoreEmbeddedPage("RestoreEmbeddedPage", //$NON-NLS-1$
		        dbSettings.getCatalog(), log);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		final Shell lShell = getShell();
		final String lCatalog = dbSettings.getCatalog();
		final ZipRestore lRestore = new ZipRestore(
		        EmbeddedCatalogHelper.getDBStorePath(), page.getFileName(),
		        log);

		if (!lRestore.checkArchive(lCatalog)) {
			MessageDialog.openWarning(lShell,
			        RelationsMessages
			                .getString("RestoreEmbedded.restore.title"), //$NON-NLS-1$
			        RelationsMessages.getString("RestoreEmbedded.restore.msg")); //$NON-NLS-1$
			return false;
		}

		page.saveToHistory();

		try {
			lRestore.restore();

			if (page.getReindex()) {
				markToReindex(lCatalog);
			}

			statusLineManager.showStatusLineMessage(
			        SUCCESS_MSG.format(new String[] { lCatalog }));

			// for that the restored state becomes visible, the application has
			// to be restarted
			lShell.setVisible(false);
			if (MessageDialog.openConfirm(lShell,
			        RelationsMessages
			                .getString("RestoreEmbedded.restart.title"), //$NON-NLS-1$
			        RelationsMessages
			                .getString("RestoreEmbedded.restart.msg"))) { //$NON-NLS-1$
				lShell.getDisplay().asyncExec(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						appContext.getArguments().put(
				                RelationsConstants.EXIT_KEY,
				                IApplication.EXIT_RESTART);
						workbench.close();
					}
				});
			}
		}
		catch (final IOException exc) {
			MessageDialog.openError(lShell,
			        RelationsMessages.getString("RestoreEmbedded.error"), //$NON-NLS-1$
			        PROBLEMS_MSG.format(new String[] { lCatalog }));
			log.error(exc, exc.getMessage());
		}

		return true;
	}

	private void markToReindex(final String inCatalog) {
		final File lParent = new File(EmbeddedCatalogHelper.getDBStorePath(),
		        inCatalog);
		final File lMarker = new File(lParent,
		        EmbeddedCatalogHelper.REINDEX_MARKER);
		try {
			lMarker.createNewFile();
		}
		catch (final IOException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	@Override
	public void dispose() {
		if (page != null) {
			page.dispose();
		}
		super.dispose();
	}
}
