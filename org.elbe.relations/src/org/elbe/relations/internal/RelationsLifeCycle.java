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

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

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
	void initializeApp(final IEclipseContext inContext,
	        final IEventBroker inEventBroker) {

		// set db settings and controller to workspace context
		if (dbSettings != null && inContext.get(DBSettings.class) == null) {
			inContext.set(DBSettings.class, dbSettings);
		}
		ContextInjectionFactory.inject(dbController, inContext);

		// set DataSourceRegistry to eclipse context to make instance available
		// in application
		final DataSourceRegistry lDbAccess = DataSourceRegistry.INSTANCE;
		inContext.set(RelationsConstants.DB_ACCESS_HANDLER, lDbAccess);

		// do some cleanup of former sessions
		EmbeddedCatalogHelper.cleanUp();

		// set language service to the context
		inContext.set(LanguageService.class,
		        ContextInjectionFactory.make(LanguageService.class, inContext));

		// set a suitable implementation of the IDataService to the context
		final DataService lDataService = ContextInjectionFactory
		        .make(DataService.class, inContext);
		inContext.set(IDataService.class, lDataService);

		// set a suitable implementation of the IBrowserManager to the context
		browserManager = ContextInjectionFactory
		        .make(RelationsBrowserManager.class, inContext);
		inContext.set(IBrowserManager.class, browserManager);

		// register a special event handler
		inEventBroker.subscribe(ShowTextItemForm.TOPIC, new ShowTextItemForm());

		boolean lDBConfigured = false;
		if (dbSettings != null && dbSettings.getDBConnectionConfig() != null
		        && dbSettings.getDBConnectionConfig().isEmbedded()
		        && RelationsConstants.DFT_DBCONFIG_PLUGIN_ID
		                .equals(dbSettings.getDBConnectionConfig().getName())) {
			// check existence of default database and create one, if needed
			if (!EmbeddedCatalogHelper.hasDefaultEmbedded()) { // NOPMD
				if (dbController.checkEmbedded()) {
					lDbAccess.setActiveConfiguration(
					        createDftDBAccessConfiguration());
					lDBConfigured = true;
					final DbEmbeddedCreateHandler lDBCreate = ContextInjectionFactory
					        .make(DbEmbeddedCreateHandler.class, inContext);
					lDBCreate.execute(dbSettings, inContext);

				} else {
					MessageDialog.openError(new Shell(Display.getDefault()),
					        RelationsMessages.getString(
					                "relations.life.cycle.db.open.error.title"), //$NON-NLS-1$
					        RelationsMessages.getString(
					                "relations.life.cycle.db.open.error.msg")); //$NON-NLS-1$
				}
			}
		}
		if (!lDBConfigured) {
			lDbAccess.setActiveConfiguration(
			        ActionHelper.createDBConfiguration(dbSettings));
		}
		lDataService.loadData(RelationsConstants.TOPIC_DB_CHANGED_RELOAD);

		if (dbSettings != null) {
			EmbeddedCatalogHelper.reindexChecked(dbSettings, inContext);
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
	void doRestore(final MApplication inApplication) {
		browserManager.restoreState(preferences);
		checkBindings(inApplication);
	}

	private void checkBindings(MApplication inApplication) {
		for (final MBindingTable bindingTable : inApplication
		        .getBindingTables()) {
			checkBindings(bindingTable);
		}
	}

	private void checkBindings(MBindingTable bindingTable) {
		final DuplicateFixer fixer = new DuplicateFixer();
		for (final MKeyBinding binding : bindingTable.getBindings()) {
			fixer.add(binding.getKeySequence());
		}
		if (fixer.hasDuplicates()) {
			fixer.fixDuplicates(bindingTable);
		}
	}

	@ProcessRemovals
	void preRendering(final IProvisioningAgent inAgent) {
		addRepository(inAgent);
	}

	private void addRepository(final IProvisioningAgent inAgent) {
		final IMetadataRepositoryManager lMetadataManager = (IMetadataRepositoryManager) inAgent
		        .getService(IMetadataRepositoryManager.SERVICE_NAME);
		final IArtifactRepositoryManager lArtifactManager = (IArtifactRepositoryManager) inAgent
		        .getService(IArtifactRepositoryManager.SERVICE_NAME);
		if (lMetadataManager == null || lArtifactManager == null) {
			log.warn("P2 metadata/artifact manager is null!"); //$NON-NLS-1$
			return;
		}

		try {
			final URI lURI = new URI(RelationsConstants.UPDATE_SITE);
			lMetadataManager.addRepository(lURI);
			lArtifactManager.addRepository(lURI);
		}
		catch (final URISyntaxException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Save db settings and browser state to preferences.
	 *
	 * @param inApplication
	 *            {@link MApplication}
	 */
	@SuppressWarnings("unchecked")
	@PreDestroy
	void saveApp(final MApplication inApplication,
	        final EModelService inModelService) {
		// save browser id
		final MElementContainer<MUIElement> lBrowserStack = (MElementContainer<MUIElement>) inModelService
		        .find(RelationsConstants.PART_STACK_BROWSERS, inApplication);
		final MUIElement lBrowser = lBrowserStack.getSelectedElement();
		preferences.put(RelationsConstants.ACTIVE_BROWSER_ID,
		        lBrowser.getElementId());

		// save browser model
		browserManager.saveState(preferences);
		// flush preferences
		try {
			preferences.flush();
		}
		catch (final BackingStoreException exc) {
			log.error(exc, exc.getMessage());
		}
	}

}
