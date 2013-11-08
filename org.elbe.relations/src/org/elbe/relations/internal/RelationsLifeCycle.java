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
package org.elbe.relations.internal;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.handlers.DbEmbeddedCreateHandler;
import org.elbe.relations.internal.controller.RelationsBrowserManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.data.DataService;
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
 * @PreSave: Called before the model is persisted
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class RelationsLifeCycle {
	private static final String ACTIVE_BROWSER_ID = "active.relations.browser";

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
		final DataService lDataService = ContextInjectionFactory.make(
				DataService.class, inContext);
		inContext.set(IDataService.class, lDataService);

		// set a suitable implementation of the IBrowserManager to the context
		browserManager = ContextInjectionFactory.make(
				RelationsBrowserManager.class, inContext);
		inContext.set(IBrowserManager.class, browserManager);

		boolean lDBConfigured = false;
		if (dbSettings.getDBConnectionConfig().isEmbedded()
				&& RelationsConstants.DFT_DBCONFIG_PLUGIN_ID.equals(dbSettings
						.getDBConnectionConfig().getName())) {
			// check existence of default database and create one, if needed
			if (!EmbeddedCatalogHelper.hasDefaultEmbedded()) {
				if (dbController.checkEmbedded()) {
					lDbAccess
							.setActiveConfiguration(createDftDBAccessConfiguration());
					lDBConfigured = true;
					final DbEmbeddedCreateHandler lDBCreate = ContextInjectionFactory
							.make(DbEmbeddedCreateHandler.class, inContext);
					lDBCreate.execute(dbSettings, inContext);

				} else {
					MessageDialog
							.openError(new Shell(Display.getDefault()),
									"Database error",
									"There is no configuration for the embedded database provided.");
				}
			}
		}
		if (!lDBConfigured) {
			lDbAccess.setActiveConfiguration(ActionHelper
					.createDBConfiguration(dbSettings));
		}
		lDataService.loadData(RelationsConstants.TOPIC_DB_CHANGED_RELOAD);

		EmbeddedCatalogHelper.reindexChecked(dbSettings, inContext);
	}

	private DBAccessConfiguration createDftDBAccessConfiguration() {
		return new DBAccessConfiguration(
				RelationsConstants.DFT_DBCONFIG_PLUGIN_ID, "./"
						+ RelationsConstants.DERBY_STORE + "/"
						+ RelationsConstants.DFT_DB_EMBEDDED,
				EmbeddedCatalogHelper.getEmbeddedDftDBChecked(), "", "");
	}

	@ProcessAdditions
	void preRendering(final MApplication inApplication,
			final EModelService inModelService, final IEclipseContext inContext) {
		browserManager.restoreState(preferences);

		// additions
		// final MUIElement lToolbar = inModelService.find(
		// "relations.toolbar:text.styling", inApplication);
		// System.out.println(lToolbar);
	}

	/**
	 * Save db settings and browser state to preferences.
	 * 
	 * @param inApplication
	 *            {@link MApplication}
	 */
	@SuppressWarnings("unchecked")
	@PreSave
	void saveApp(final MApplication inApplication,
			final EModelService inModelService) {
		// save browser id
		final MElementContainer<MUIElement> lBrowserStack = (MElementContainer<MUIElement>) inModelService
				.find(RelationsConstants.PART_STACK_BROWSERS, inApplication);
		preferences.put(ACTIVE_BROWSER_ID, lBrowserStack.getSelectedElement()
				.getElementId());

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
