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

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.ChangeDB;
import org.elbe.relations.internal.actions.IDBChange;
import org.elbe.relations.internal.actions.IndexerAction;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.data.IDBSettings;
import org.elbe.relations.internal.data.TempSettings;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;
import org.elbe.relations.internal.utility.WizardHelper;
import org.elbe.relations.internal.utility.ZipImport;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Wizard to import a Relations database stored to a Zip file.<br />
 * Note: this is an Eclipse 3 wizard. To make it e4, let the values for the
 * annotated field be injected (instead of using the method init()).
 *
 * @author Luthiger Created on 15.10.2007
 */
@SuppressWarnings("restriction")
public class ImportEmbedded extends Wizard implements IImportWizard {
	private final static String SUCCESS_MSG = RelationsMessages
	        .getString("ImportEmbedded.message.success"); //$NON-NLS-1$
	private final static String PROBLEMS_MSG = RelationsMessages
	        .getString("ImportEmbedded.message.problems"); //$NON-NLS-1$

	@Inject
	private Logger log;

	@Inject
	private RelationsStatusLineManager statusLine;

	@Inject
	private org.eclipse.e4.ui.workbench.IWorkbench workbench;

	@Inject
	private IApplicationContext appContext;

	@Inject
	private IEclipseContext context;

	@Inject
	private DBSettings dbSettings;

	private ImportEmbeddedPage page;

	@Override
	public void init(final IWorkbench inWorkbench,
	        final IStructuredSelection inSelection) {
		log = inWorkbench.getAdapter(Logger.class);
		statusLine = WizardHelper.getFromWorkbench(
		        RelationsStatusLineManager.class, inWorkbench);
		workbench = inWorkbench
		        .getAdapter(org.eclipse.e4.ui.workbench.IWorkbench.class);
		appContext = inWorkbench.getAdapter(IApplicationContext.class);
		context = inWorkbench.getAdapter(IEclipseContext.class);
		dbSettings = inWorkbench.getAdapter(DBSettings.class);

		setWindowTitle(
		        RelationsMessages.getString("ImportEmbedded.window.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		page = new ImportEmbeddedPage("ImportEmbeddedPage", log); //$NON-NLS-1$
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		final String lArchiveName = page.getArchiveName();
		final String lDBName = page.getDBName();

		final ZipImport lImport = new ZipImport(
		        EmbeddedCatalogHelper.getDBStorePath(), lArchiveName, lDBName,
		        log);

		page.saveToHistory();

		try {
			lImport.restore();
			statusLine.showStatusLineMessage(
			        String.format(SUCCESS_MSG, lArchiveName, lDBName));

			if (EmbeddedCatalogHelper.deleteMarker(lDBName)) {
				// we have to restart
				restartApp(lDBName, page.getReindex());
			} else {
				// we can open the imported data straightforward
				final IDBSettings lTempSettings = new TempSettings("", lDBName, //$NON-NLS-1$
				        "", "", dbSettings.getDBConnectionConfig()); //$NON-NLS-1$ //$NON-NLS-2$
				final IDBChange lChangeDB = ContextInjectionFactory
				        .make(ChangeDB.class, context);
				lChangeDB.setTemporarySettings(lTempSettings);
				lChangeDB.execute();
				if (page.getReindex()) {
					final IndexerAction lAction = ContextInjectionFactory
					        .make(IndexerAction.class, context);
					lAction.setSilent(true);
					lAction.run();
				}
			}
			saveCatalog(lDBName);
		}
		catch (final Exception exc) {
			MessageDialog.openError(getShell(),
			        RelationsMessages.getString("RestoreEmbedded.error"), //$NON-NLS-1$
			        String.format(PROBLEMS_MSG, lArchiveName));
			log.error(exc, exc.getMessage());
		}

		return true;
	}

	private void restartApp(final String inDBName, final boolean inReindex)
	        throws IOException {
		if (inReindex) {
			final File lParent = new File(
			        EmbeddedCatalogHelper.getDBStorePath(), inDBName);
			final File lMarker = new File(lParent,
			        EmbeddedCatalogHelper.REINDEX_MARKER);
			lMarker.createNewFile();
		}

		final Shell lShell = getShell();
		lShell.setVisible(false);
		if (MessageDialog.openConfirm(lShell,
		        RelationsMessages.getString("RestoreEmbedded.restart.title"), //$NON-NLS-1$
		        RelationsMessages.getString("RestoreEmbedded.restart.msg"))) { //$NON-NLS-1$
			lShell.getDisplay().asyncExec(new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					appContext.getArguments().put(RelationsConstants.EXIT_KEY,
			                IApplication.EXIT_RESTART);
					workbench.close();
				}
			});
		}
	}

	private void saveCatalog(final String inDBName)
	        throws BackingStoreException {
		final IEclipsePreferences lNode = DefaultScope.INSTANCE
		        .getNode(RelationsConstants.PREFERENCE_NODE);
		lNode.put(RelationsConstants.KEY_DB_PLUGIN_ID,
		        RelationsConstants.DFT_DBCONFIG_PLUGIN_ID);
		lNode.put(RelationsConstants.KEY_DB_EMBEDDED_CATALOG, inDBName);
	}

	@Override
	public void dispose() {
		page.dispose();
		super.dispose();
	}

}
