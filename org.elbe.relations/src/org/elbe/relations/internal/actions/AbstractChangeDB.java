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

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.transform.TransformerException;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.utility.EventStoreChecker;
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

	@Override
	public void setTemporarySettings(final IDBSettings dbSettings) {
		this.dbSettings = dbSettings;
	}

	protected IDBSettings getTempSettings() {
		return this.dbSettings;
	}

	@Override
	public void execute() {
		try {
			// check structure with temporary settings
			if (!this.checker.hasExpectedStructure(this.dbSettings)) {
				MessageDialog.openError(new Shell(Display.getCurrent()),
						RelationsMessages
						.getString("FormDBConnection.error.title"), //$NON-NLS-1$
						RelationsMessages
						.getString("FormDBConnection.error.msg")); //$NON-NLS-1$
				this.log.warn(String.format("Unable to open DB at '%s'!", //$NON-NLS-1$
						getDBInfo(this.dbSettings)));
				return;
			}

			// make temporary settings active
			setTempDBSettings();
			doDBChange();
		}
		catch (final SQLException exc) {
			this.log.error(exc, exc.getMessage());
			MessageDialog
			.openError(new Shell(Display.getCurrent()),
					RelationsMessages
					.getString("FormDBConnection.error.title"), //$NON-NLS-1$
					RelationsMessages.getString(
							"FormDBConnection.error.connection.msg", //$NON-NLS-1$
							new Object[] { this.dbSettings.getDBName(),
									this.dbSettings.getUser() }));
		}
		catch (final VException exc) {
			this.log.error(exc, exc.getMessage());
		}
	}

	private String getDBInfo(final IDBSettings settings) {
		return ActionHelper.createDBConfiguration(this.dbSettings)
				.getProperties().get("databaseName").toString(); //$NON-NLS-1$
	}

	/**
	 * Execute the DB change: save settings to preferences and notify
	 * application about change.
	 */
	protected void doDBChange() {
		this.restoreSettings = new TempSettings(this.origDbSettings.getHost(),
				this.origDbSettings.getCatalog(), this.origDbSettings.getUser(),
				this.origDbSettings.getPassword(),
				this.origDbSettings.getDBConnectionConfig());

		// persist temporary settings
		((TempSettings) this.dbSettings).saveToPreferences();

		// schema upgrade: checked creation of EventStore table
		try {
			new EventStoreChecker().createEventStoreChecked(
					this.dbSettings.getDBConnectionConfig().getCreator());
		}
		catch (IOException | TransformerException | SQLException exc) {
			this.log.error(exc, "Unable to create the EventStore table!"); //$NON-NLS-1$
		}

		// trigger change
		this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_DB, "changeDB"); //$NON-NLS-1$
	}

	@Override
	public void restore() {
		if (this.restoreSettings == null) {
			return;
		}

		((TempSettings) this.restoreSettings).saveToPreferences();
		this.dbAccess.setActiveConfiguration(ActionHelper
				.createDBConfiguration(this.restoreSettings));
	}

	/**
	 * Activate temporary DB settings.
	 */
	protected void setTempDBSettings() {
		this.dbAccess.setActiveConfiguration(ActionHelper
				.createDBConfiguration(this.dbSettings));
	}

	protected void setOrigDBSettings() {
		this.dbAccess.setActiveConfiguration(ActionHelper
				.createDBConfiguration(this.origDbSettings));
	}

	protected Logger getLog() {
		return this.log;
	}

	@Override
	public void checkPreconditions() throws DBPreconditionException {
		// default implementation doing nothing
	}

}
