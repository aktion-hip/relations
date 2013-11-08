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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.backup.XMLExport;
import org.elbe.relations.internal.backup.ZippedXMLExport;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.preferences.LanguageService;
import org.hip.kernel.exc.VException;

/**
 * Wizard to export/backup the actual database.
 * 
 * @author Luthiger Created on 03.10.2008
 */
@SuppressWarnings("restriction")
public class ExportToXML extends Wizard implements IExportWizard {
	private final static String STATUS_MSG = RelationsMessages
			.getString("ExportToXML.msg.status"); //$NON-NLS-1$

	@Inject
	private Logger log;

	@Inject
	private DBSettings dbSettings;

	@Inject
	private IDataService dataService;

	@Inject
	private RelationsStatusLineManager statusLine;

	@Inject
	private LanguageService languageService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	private ExportToXMLPage page;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(final IWorkbench inWorkbench,
			final IStructuredSelection inSelection) {
		setWindowTitle(RelationsMessages.getString("ExportToXML.window.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		page = new ExportToXMLPage("ExportToXMLPage"); //$NON-NLS-1$
		addPage(page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		final String lCatalog = dbSettings.getCatalog();
		final String lBackupFile = page.getFileName();
		statusLine.showStatusLineMessage(String.format(STATUS_MSG, lCatalog,
				lBackupFile));

		final ProgressMonitorDialog lDialog = new ProgressMonitorJobsDialog(
				shell);
		lDialog.open();

		final ExporterJob lJob = new ExporterJob(page.getFileName()); //$NON-NLS-1$
		try {
			lDialog.run(true, true, lJob);
		}
		catch (final InvocationTargetException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final InterruptedException exc) {
			log.error(exc, exc.getMessage());
		} finally {
			lDialog.close();
		}
		return true;
	}

	@Override
	public void dispose() {
		if (page != null)
			page.dispose();
		super.dispose();
	}

	// --- private classes ---

	private class ExporterJob implements IRunnableWithProgress {
		private final String fileName;

		public ExporterJob(final String inFileName) {
			fileName = inFileName;
		}

		@Override
		public void run(final IProgressMonitor inMonitor) {
			inMonitor
					.beginTask(
							RelationsMessages
									.getString("ExportToXML.msg.job.start"), dataService.getNumberOfItems()); //$NON-NLS-1$

			XMLExport lExport = null;
			try {
				lExport = fileName.endsWith(".zip") ? new ZippedXMLExport(fileName, languageService.getAppLocale()) : new XMLExport(fileName, languageService.getAppLocale()); //$NON-NLS-1$
				lExport.export(inMonitor);
			}
			catch (final IOException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final VException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final SQLException exc) {
				log.error(exc, exc.getMessage());
			} finally {
				if (lExport != null) {
					try {
						lExport.close();
					}
					catch (final IOException exc) {
						log.error(exc, exc.getMessage());
					}
				}
				inMonitor.done();
			}
		}
	}

}
