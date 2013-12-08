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
package org.elbe.relations.internal.actions;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.data.IDBSettings;
import org.elbe.relations.internal.data.TempSettings;
import org.elbe.relations.internal.utility.ActionHelper;
import org.elbe.relations.internal.utility.DBPreconditionException;
import org.elbe.relations.internal.utility.DBStructureChecker;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.hip.kernel.exc.VException;

/**
 * Base class for helper classes to change the application's database (catalog).
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractChangeDB implements IDBChange {
	private IDBSettings dbSettings;
	private IDBSettings restoreSettings;

	@Inject
	private DBSettings origDbSettings;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private DBStructureChecker checker;

	@Inject
	@Named(value = RelationsConstants.DB_ACCESS_HANDLER)
	private DataSourceRegistry dbAccess;

	@Inject
	private Logger log;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.actions.IDBChange#setTemporarySettings(org
	 * .elbe.relations.internal.data.DBSettings)
	 */
	@Override
	public void setTemporarySettings(final IDBSettings inDBSettings) {
		dbSettings = inDBSettings;
	}

	protected IDBSettings getTempSettings() {
		return dbSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.actions.ICommand#execute()
	 */
	@Override
	public void execute() {
		try {
			// check structure with temporary settings
			if (!checker.hasExpectedStructure(dbSettings)) {
				MessageDialog.openError(new Shell(Display.getCurrent()),
						RelationsMessages
								.getString("FormDBConnection.error.title"), //$NON-NLS-1$
						RelationsMessages
								.getString("FormDBConnection.error.msg")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}

			// make temporary settings active
			setTempDBSettings();
			doDBChange();
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Execute the DB change: save settings to preferences and notify
	 * application about change.
	 */
	protected void doDBChange() {
		restoreSettings = new TempSettings(origDbSettings.getHost(),
				origDbSettings.getCatalog(), origDbSettings.getUser(),
				origDbSettings.getPassword(),
				origDbSettings.getDBConnectionConfig());

		// persist temporary settings
		((TempSettings) dbSettings).saveToPreferences();

		// trigger change
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_DB, "changeDB"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.actions.IDBChange#restore()
	 */
	@Override
	public void restore() {
		if (restoreSettings == null) {
			return;
		}

		((TempSettings) restoreSettings).saveToPreferences();
		dbAccess.setActiveConfiguration(ActionHelper
				.createDBConfiguration(restoreSettings));
	}

	/**
	 * Activate temporary DB settings.
	 */
	protected void setTempDBSettings() {
		dbAccess.setActiveConfiguration(ActionHelper
				.createDBConfiguration(dbSettings));
	}

	protected void setOrigDBSettings() {
		dbAccess.setActiveConfiguration(ActionHelper
				.createDBConfiguration(origDbSettings));
	}

	protected Logger getLog() {
		return log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.actions.IDBChange#checkPreconditions()
	 */
	@Override
	public void checkPreconditions() throws DBPreconditionException {
		// default implementation doing nothing
	}

}
