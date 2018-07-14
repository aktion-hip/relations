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
package org.elbe.relations.internal.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.backup.XMLExport;
import org.elbe.relations.internal.backup.ZippedXMLExport;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.preferences.CloudConfigPrefPage;
import org.elbe.relations.internal.preferences.CloudConfigRegistry;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.services.ICloudProvider;
import org.elbe.relations.services.ICloudProviderConfig;
import org.hip.kernel.exc.VException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * This class is executing the export to the cloud according to the cloud
 * provider configuration that is stored in the preferences and marked as
 * active.
 *
 * @author lbenno
 */
@Component(service = ExportToCloudAction.class)
@SuppressWarnings("restriction")
public class ExportToCloudAction implements ICommand {

	private LanguageService languageService;
	private Logger log;
	private RelationsStatusLineManager statusLine;
	private IDataService dataService;

	private CloudConfigRegistry cloudConfigRegistry;

	@Reference
	void bindCloudProvider(final CloudConfigRegistry cloudConfigRegistry) {
		this.cloudConfigRegistry = cloudConfigRegistry;
	}


	/**
	 * Passing relevant objects to the action.
	 *
	 * @param context
	 *            {@link IEclipseContext}
	 * @return {@link ExportToCloudAction}
	 */
	public ExportToCloudAction initialize(final IEclipseContext context) {
		this.languageService = context.get(LanguageService.class);
		this.log = context.get(Logger.class);
		this.statusLine = context.get(RelationsStatusLineManager.class);
		this.dataService = context.get(IDataService.class);
		return this;
	}

	@Override
	public void execute() {
		final IEclipsePreferences store = RelationsPreferences.getPreferences();
		final ICloudProviderConfig cloudProviderConfig = getActiveCloudProviderConfig(
				store);
		if (cloudProviderConfig == null) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(),
					"Configuration Problem",
					"There is no Cloud Provider configured! (See Preferences -> Cloud Configuration)");
			return;
		}

		if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
				"Export to Cloud",
				String.format("Using the cloud provider \"%s\".",
						cloudProviderConfig.getName()))) {
			final IRunnableWithProgress operation = new ExportToCloudJob(
					cloudProviderConfig.getProvider(),
					createJson(CloudConfigPrefPage
							.getKey(cloudProviderConfig.getName()), store),
					this.languageService, this.log, this.statusLine,
			        this.dataService.getNumberOfItems()
			                + this.dataService.getNumberOfRelations());
			try {
				new ProgressMonitorDialog(Display.getDefault().getActiveShell())
				.run(true, true, operation);
			}
			catch (InvocationTargetException | InterruptedException exc) {
				this.log.error(exc, "Error during export to cloud!");
			}
		}
	}

	private JsonObject createJson(final String key,
			final IEclipsePreferences store) {
		final String jsonOfValues = store.get(key, "{}");
		return new Gson().fromJson(jsonOfValues.isEmpty() ? "{}" : jsonOfValues,
				JsonObject.class);
	}

	private ICloudProviderConfig getActiveCloudProviderConfig(
			final IEclipsePreferences store) {
		final String nameOfActive = store
				.get(RelationsConstants.PREFS_CLOUD_ACTIVE, "");
		if (nameOfActive.isEmpty()) {
			return null;
		}
		for (final ICloudProviderConfig config : this.cloudConfigRegistry
				.getConfigurations()) {
			if (nameOfActive.equals(config.getName())) {
				return config;
			}
		}
		return null;
	}

	// ---

	/**
	 * The export job with monitor.
	 */
	private static class ExportToCloudJob implements IRunnableWithProgress {

		private final ICloudProvider cloudProvider;
		private final JsonObject jsonObject;
		private final LanguageService languageService;
		private final Logger log;
		private final RelationsStatusLineManager statusLine;
		private final int numberOfItems;

		protected ExportToCloudJob(final ICloudProvider cloudProvider,
				final JsonObject jsonObject,
				final LanguageService languageService, final Logger log,
				final RelationsStatusLineManager statusLine,
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
				tempExport = File.createTempFile("relations_all",
						".zip");
				// 1) export DB content to zipped XML in temporary file
				try (XMLExport exporter = new ZippedXMLExport(
						tempExport.getAbsolutePath(),
						this.languageService.getAppLocale(),
						this.numberOfItems)) {
					exporter.export(monitor);
				}
				catch (VException | SQLException exc) {
					this.log.error(exc, exc.getMessage());
				}

				// 2) upload temporary file to cloud
				if (this.cloudProvider.upload(tempExport, this.jsonObject,
						this.log)) {
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
			}
			catch (final IOException exc) {
				this.log.error(exc, exc.getMessage());
			}
			finally {
				if (tempExport != null) {
					tempExport.delete();
				}
			}
		}
	}

}
