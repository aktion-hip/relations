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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.backup.XMLExport;
import org.elbe.relations.internal.backup.ZippedXMLExport;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.internal.wizards.interfaces.IExportWizard;
import org.hip.kernel.exc.VException;

/**
 * Wizard to export/backup the actual database.
 *
 * @author Luthiger
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

	@PostConstruct
	public void init() {
		setWindowTitle(RelationsMessages.getString("ExportToXML.window.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		this.page = new ExportToXMLPage("ExportToXMLPage"); //$NON-NLS-1$
		addPage(this.page);
	}

	@Override
	public boolean performFinish() {
		final String lCatalog = this.dbSettings.getCatalog();
		final String lBackupFile = this.page.getFileName();
		this.statusLine.showStatusLineMessage(
				String.format(STATUS_MSG, lCatalog, lBackupFile));

		final ProgressMonitorDialog lDialog = new ProgressMonitorDialog(this.shell);
		lDialog.open();

		final ExporterJob lJob = new ExporterJob(this.page.getFileName());
		try {
			lDialog.run(true, true, lJob);
		}
		catch (final InvocationTargetException exc) {
			this.log.error(exc, exc.getMessage());
		}
		catch (final InterruptedException exc) {
			this.log.error(exc, exc.getMessage());
		}
		finally {
			lDialog.close();
		}
		return true;
	}

	@Override
	public void dispose() {
		if (this.page != null) {
			this.page.dispose();
		}
		super.dispose();
	}

	// --- private classes ---

	private class ExporterJob implements IRunnableWithProgress {
		private final String fileName;

		public ExporterJob(final String inFileName) {
			this.fileName = inFileName;
		}

		@Override
		public void run(final IProgressMonitor inMonitor) {
			inMonitor.beginTask(
					RelationsMessages.getString("ExportToXML.msg.job.start"), //$NON-NLS-1$
					ExportToXML.this.dataService.getNumberOfItems());

			try (XMLExport exporter = createExporter(this.fileName)) {
				exporter.export(inMonitor);
			}
			catch (IOException | VException | SQLException exc) {
				ExportToXML.this.log.error(exc, exc.getMessage());
			}
			finally {
				inMonitor.done();
			}
		}
	}

	private XMLExport createExporter(final String fileName) throws IOException {
		return fileName.endsWith(".zip") //$NON-NLS-1$
				? new ZippedXMLExport(fileName,
		                ExportToXML.this.languageService.getAppLocale(),
		                this.dataService.getNumberOfItems())
						: new XMLExport(fileName,
		                ExportToXML.this.languageService.getAppLocale(),
		                this.dataService.getNumberOfItems());
	}

}
