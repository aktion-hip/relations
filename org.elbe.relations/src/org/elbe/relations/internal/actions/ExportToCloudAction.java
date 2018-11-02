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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.EventStoreHome;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.backup.XMLExport;
import org.elbe.relations.internal.backup.ZippedXMLExport;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.preferences.CloudConfigPrefPage;
import org.elbe.relations.internal.preferences.CloudConfigRegistry;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.internal.utility.AbstractExportToCloudJob;
import org.elbe.relations.internal.utility.ExportToCloudDialog;
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
					RelationsMessages.getString("ExportToCloudAction.problem.title"), //$NON-NLS-1$
					RelationsMessages.getString("ExportToCloudAction.problem.msg")); //$NON-NLS-1$
			return;
		}

		final ExportToCloudDialog dialog = new ExportToCloudDialog(
				cloudProviderConfig, this.dataService.getNumberOfEvents() > 0);
		if (dialog.open() == Window.OK) {
			final IRunnableWithProgress operation = dialog.isIncremental()
					? new ExportToCloudIncremental(
							cloudProviderConfig.getProvider(),
							createJson(
									CloudConfigPrefPage.getKey(
											cloudProviderConfig.getName()),
									store),
							this.languageService, this.log, this.statusLine)
							: new ExportToCloudFull(cloudProviderConfig.getProvider(),
									createJson(
											CloudConfigPrefPage.getKey(
													cloudProviderConfig.getName()),
											store),
									this.languageService, this.log, this.statusLine,
									this.dataService.getNumberOfItems()
									+ this.dataService.getNumberOfRelations());

							try {
								new ProgressMonitorDialog(Display.getDefault().getActiveShell())
								.run(true, true, operation);
							}
							catch (InvocationTargetException | InterruptedException exc) {
								this.log.error(exc, "Error during export to cloud!"); //$NON-NLS-1$
							}
		}
	}

	private JsonObject createJson(final String key,
			final IEclipsePreferences store) {
		final String jsonOfValues = store.get(key, "{}"); //$NON-NLS-1$
		return new Gson().fromJson(jsonOfValues.isEmpty() ? "{}" : jsonOfValues, //$NON-NLS-1$
				JsonObject.class);
	}

	private ICloudProviderConfig getActiveCloudProviderConfig(
			final IEclipsePreferences store) {
		final String nameOfActive = store
				.get(RelationsConstants.PREFS_CLOUD_ACTIVE, ""); //$NON-NLS-1$
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

	private static class ExportToCloudFull extends AbstractExportToCloudJob {
		protected ExportToCloudFull(final ICloudProvider cloudProvider,
				final JsonObject jsonObject,
				final LanguageService languageService, final Logger log,
				final RelationsStatusLineManager statusLine,
				final int numberOfItems) {
			super(cloudProvider, jsonObject, languageService, log, statusLine,
					numberOfItems);
			setFullExport(true);
		}

		@Override
		protected String createTempFileName() {
			return RelationsConstants.CLOUD_SYNC_FULL;
		}

		@Override
		protected void prepareContentForExport(final File tempExport,
				final IProgressMonitor monitor) throws IOException {
			try (XMLExport exporter = new ZippedXMLExport(
					tempExport.getAbsolutePath(),
					getAppLocale(), getNumberOfItems())) {
				exporter.export(monitor);
			}
			catch (VException | SQLException exc) {
				getLog().error(exc, exc.getMessage());
			}
		}

	}

	private static class ExportToCloudIncremental
	extends AbstractExportToCloudJob {
		private static final String PATTERN = "yyyy-MM-dd-HHmmss"; //$NON-NLS-1$

		protected ExportToCloudIncremental(final ICloudProvider cloudProvider,
				final JsonObject jsonObject,
				final LanguageService languageService, final Logger log,
				final RelationsStatusLineManager statusLine) {
			super(cloudProvider, jsonObject, languageService, log, statusLine,
					0);
		}

		@Override
		protected String createTempFileName() {
			return String.format("%s%s", RelationsConstants.CLOUD_SYNC_DELTA, //$NON-NLS-1$
					new SimpleDateFormat(PATTERN).format(new Date()));
		}

		@Override
		protected void prepareContentForExport(final File tempExport,
				final IProgressMonitor monitor) throws IOException {
			try (XMLExport exporter = new EventStoreExport(
					tempExport.getAbsolutePath(), getAppLocale())) {
				exporter.export(monitor);
			}
			catch (VException | SQLException exc) {
				getLog().error(exc, exc.getMessage());
			}
		}

	}

	/**
	 * Special exporter for only the entries in the EventStore.
	 */
	private static class EventStoreExport extends ZippedXMLExport {
		private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$
		private static final String NODE_EVENT_STORE = "EventStoreEntries"; //$NON-NLS-1$

		private final Locale appLocale;

		protected EventStoreExport(final String exportFileName,
				final Locale appLocale) throws IOException {
			super(exportFileName, appLocale, 0);
			this.appLocale = appLocale;
		}

		@Override
		public int export(final IProgressMonitor monitor)
				throws VException, SQLException, IOException {
			final EventStoreHome home = BOMHelper.getEventStoreHome();
			final int numberOfItems = home.getCount();

			final SubMonitor progress = SubMonitor.convert(monitor);
			int outExported = 0;

			appendText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL); //$NON-NLS-1$
			final DateFormat format = DateFormat.getDateTimeInstance(
					DateFormat.MEDIUM, DateFormat.MEDIUM, this.appLocale);
			appendText(String.format("<%s date=\"%s\" countAll=\"%s\">" + NL, //$NON-NLS-1$
					NODE_ROOT, format.format(Calendar.getInstance().getTime()),
					numberOfItems));

			outExported += processTable(
					RelationsMessages.getString("XMLExport.export.events"), //$NON-NLS-1$
					NODE_EVENT_STORE, home, progress);
			if (monitor.isCanceled()) {
				return outExported;
			}

			appendEnd(NODE_ROOT);
			return outExported;
		}
	}

}
