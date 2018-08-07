/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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
package org.elbe.relations.internal.utility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.data.utility.EventStoreChecker;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.services.ICloudProvider;

import com.google.gson.JsonObject;

/**
 * Base class for the jobs to export to the cloud.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public abstract class AbstractExportToCloudJob
implements IRunnableWithProgress {
	private final ICloudProvider cloudProvider;
	private final JsonObject jsonObject;
	private final LanguageService languageService;
	private final Logger log;
	private final RelationsStatusLineManager statusLine;
	private final int numberOfItems;

	/**
	 * AbstractExportToCloudJob constructor.
	 *
	 * @param cloudProvider
	 *            {@link ICloudProvider}
	 * @param jsonObject
	 *            {@link JsonObject}
	 * @param languageService
	 *            {@link LanguageService}
	 * @param log
	 *            {@link Logger}
	 * @param statusLine
	 *            {@link RelationsStatusLineManager}
	 * @param numberOfItems
	 *            int
	 */
	public AbstractExportToCloudJob(final ICloudProvider cloudProvider,
			final JsonObject jsonObject, final LanguageService languageService,
			final Logger log, final RelationsStatusLineManager statusLine,
			final int numberOfItems) {
		this.cloudProvider = cloudProvider;
		this.jsonObject = jsonObject;
		this.languageService = languageService;
		this.log = log;
		this.statusLine = statusLine;
		this.numberOfItems = numberOfItems;
	}

	@Override
	public void run(final IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		File tempExport = null;
		try {
			final String fileName = createTempFileName();
			tempExport = File.createTempFile(fileName, ".zip");

			// 1) export DB content to zipped XML in temporary file
			prepareContentForExport(tempExport, monitor);

			// 2) upload temporary file to cloud
			if (this.cloudProvider.upload(tempExport,
					String.format("%s.zip", fileName),
					this.jsonObject, this.log)) {
				Display.getDefault().asyncExec(() -> {
					this.statusLine.showStatusLineMessage(
							"Successfully exported Relations data to cloud.");
				});
			} else {
				Display.getDefault().asyncExec(() -> {
					MessageDialog.openError(
							Display.getDefault().getActiveShell(),
							"Export Error",
							"Could not export the data to the cloud! See log file for more information.");
				});
			}

			// 3) clear entries in EventStore
			new EventStoreChecker().clear();
		}
		catch (final IOException | SQLException exc) {
			this.log.error(exc, exc.getMessage());
		}
		finally {
			if (tempExport != null) {
				tempExport.delete();
			}
		}
	}

	protected abstract String createTempFileName();

	protected abstract void prepareContentForExport(File tempExport,
			final IProgressMonitor monitor) throws IOException;

	protected Locale getAppLocale() {
		return this.languageService.getAppLocale();
	}

	protected Logger getLog() {
		return this.log;
	}

	protected int getNumberOfItems() {
		return this.numberOfItems;
	}

}
