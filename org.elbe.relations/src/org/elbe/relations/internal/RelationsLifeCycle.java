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
package org.elbe.relations.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.utility.EventStoreChecker;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.handlers.DbEmbeddedCreateHandler;
import org.elbe.relations.handlers.ShowTextItemForm;
import org.elbe.relations.internal.controller.RelationsBrowserManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.data.DataService;
import org.elbe.relations.internal.e4.keys.model.DuplicateFixer;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.internal.services.IDBController;
import org.elbe.relations.internal.utility.ActionHelper;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.dbaccess.DBAccessConfiguration;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This application's life cycle handler.
 *
 * @PostContextCreate: Called after the application context is created
 * @ProcessAdditions: Called before the model is passed to the renderer
 * @ProcessRemovals: Called before the model is passed to the renderer
 * @PreSave: Called before the model is persisted (does not work with
 *           compatibility layer)
 * @PreDestroy: Called before the model is destroyed
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class RelationsLifeCycle {

	private RelationsBrowserManager browserManager;

	@Inject
	private Logger log;

	@Inject
	private IDBController dbController;

	@Inject
	private DBSettings dbSettings;

	@Inject
	@Preference(nodePath = RelationsConstants.PREFERENCE_NODE)
	private IEclipsePreferences preferences;

	@PostContextCreate
	void initializeApp(final IEclipseContext context,
			final IEventBroker eventBroker) {

		// set db settings and controller to workspace context
		if (this.dbSettings != null && context.get(DBSettings.class) == null) {
			context.set(DBSettings.class, this.dbSettings);
		}
		ContextInjectionFactory.inject(this.dbController, context);

		// set DataSourceRegistry to eclipse context to make instance available
		// in application
		final DataSourceRegistry dbAccess = DataSourceRegistry.INSTANCE;
		context.set(RelationsConstants.DB_ACCESS_HANDLER, dbAccess);

		// do some cleanup of former sessions
		EmbeddedCatalogHelper.cleanUp();

		// set language service to the context
		context.set(LanguageService.class,
				ContextInjectionFactory.make(LanguageService.class, context));

		// set a suitable implementation of the IDataService to the context
		final DataService dataService = ContextInjectionFactory
				.make(DataService.class, context);
		context.set(IDataService.class, dataService);

		// set a suitable implementation of the IBrowserManager to the context
		this.browserManager = ContextInjectionFactory
				.make(RelationsBrowserManager.class, context);
		context.set(IBrowserManager.class, this.browserManager);

		// register a special event handler
		eventBroker.subscribe(ShowTextItemForm.TOPIC, new ShowTextItemForm());

		boolean isDBConfigured = false;
		if (this.dbSettings != null && this.dbSettings.getDBConnectionConfig() != null
				&& this.dbSettings.getDBConnectionConfig().isEmbedded()
				&& RelationsConstants.DFT_DBCONFIG_PLUGIN_ID
				.equals(this.dbSettings.getDBConnectionConfig().getName())) {
			// check existence of default database and create one, if needed
			if (!EmbeddedCatalogHelper.hasDefaultEmbedded()) { // NOPMD
				if (this.dbController.checkEmbedded()) {
					dbAccess.setActiveConfiguration(
							createDftDBAccessConfiguration());
					isDBConfigured = true;
					final DbEmbeddedCreateHandler lDBCreate = ContextInjectionFactory
							.make(DbEmbeddedCreateHandler.class, context);
					lDBCreate.execute(this.dbSettings, context);

				} else {
					MessageDialog.openError(new Shell(Display.getDefault()),
							RelationsMessages.getString(
									"relations.life.cycle.db.open.error.title"), //$NON-NLS-1$
							RelationsMessages.getString(
									"relations.life.cycle.db.open.error.msg")); //$NON-NLS-1$
				}
			}
		}
		if (!isDBConfigured) {
			dbAccess.setActiveConfiguration(
					ActionHelper.createDBConfiguration(this.dbSettings));
		}
		// schema upgrade: checked creation of EventStore table
		try {
			new EventStoreChecker().createEventStoreChecked(
					this.dbSettings.getDBConnectionConfig().getCreator());
		}
		catch (IOException | TransformerException | SQLException exc) {
			this.log.error(exc, "Unable to create the EventStore table!");
		}

		dataService.loadData(RelationsConstants.TOPIC_DB_CHANGED_RELOAD);

		if (this.dbSettings != null) {
			EmbeddedCatalogHelper.reindexChecked(this.dbSettings, context);
		}
	}

	private DBAccessConfiguration createDftDBAccessConfiguration() {
		return new DBAccessConfiguration(
				RelationsConstants.DFT_DBCONFIG_PLUGIN_ID,
				"./" //$NON-NLS-1$
				+ RelationsConstants.DERBY_STORE + "/" //$NON-NLS-1$
				+ RelationsConstants.DFT_DB_EMBEDDED,
				EmbeddedCatalogHelper.getEmbeddedDftDBChecked(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@ProcessAdditions
	void doRestore(final MApplication application) {
		this.browserManager.restoreState(this.preferences);
		checkBindings(application);
	}

	private void checkBindings(final MApplication application) {
		for (final MBindingTable bindingTable : application
				.getBindingTables()) {
			checkBindings(bindingTable);
		}
	}

	private void checkBindings(final MBindingTable bindingTable) {
		final DuplicateFixer fixer = new DuplicateFixer();
		for (final MKeyBinding binding : bindingTable.getBindings()) {
			fixer.add(binding.getKeySequence());
		}
		if (fixer.hasDuplicates()) {
			fixer.fixDuplicates(bindingTable);
		}
	}

	@ProcessRemovals
	void preRendering(final IProvisioningAgent agent) {
		addRepository(agent);
	}

	private void addRepository(final IProvisioningAgent agent) {
		final IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent
				.getService(IMetadataRepositoryManager.SERVICE_NAME);
		final IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
				.getService(IArtifactRepositoryManager.SERVICE_NAME);
		if (metadataManager == null || artifactManager == null) {
			this.log.warn("P2 metadata/artifact manager is null!"); //$NON-NLS-1$
			return;
		}

		try {
			final URI uri = new URI(RelationsConstants.UPDATE_SITE);
			metadataManager.addRepository(uri);
			artifactManager.addRepository(uri);
		}
		catch (final URISyntaxException exc) {
			this.log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Save db settings and browser state to preferences.
	 *
	 * @param application
	 *            {@link MApplication}
	 */
	@SuppressWarnings("unchecked")
	@PreDestroy
	void saveApp(final MApplication application,
			final EModelService modelService) {
		// save browser id
		final MElementContainer<MUIElement> browserStack = (MElementContainer<MUIElement>) modelService
				.find(RelationsConstants.PART_STACK_BROWSERS, application);
		final MUIElement browser = browserStack.getSelectedElement();
		this.preferences.put(RelationsConstants.ACTIVE_BROWSER_ID,
				browser.getElementId());

		// save browser model
		this.browserManager.saveState(this.preferences);
		// flush preferences
		try {
			this.preferences.flush();
		}
		catch (final BackingStoreException exc) {
			this.log.error(exc, exc.getMessage());
		}
	}

}
