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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.ICreatableHome;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.data.IDBSettings;
import org.elbe.relations.internal.data.TempSettings;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;
import org.hip.kernel.bom.impl.DefaultStatement;

/**
 * Action to delete an embedded database.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class DBDeleteAction implements ICommand {
	private final static String ERROR_MSG = RelationsMessages
			.getString("DBDeleteAction.error.msg"); //$NON-NLS-1$

	private final DBSettings dbSettings;
	private final Logger log;
	private final IEclipseContext context;

	/**
	 * DBDeleteAction constructor.
	 *
	 * @param settings
	 *            {@link DBSettings}
	 * @param context
	 *            {@link IEclipseContext}
	 * @param log
	 *            {@link Logger}
	 */
	public DBDeleteAction(final DBSettings settings,
			final IEclipseContext context, final Logger log) {
		this.dbSettings = settings;
		this.context = context;
		this.log = log;
	}

	@Override
	public void execute() {
		final String catalog = this.dbSettings.getCatalog();
		final String question = RelationsMessages.getString(
				"DBDeleteAction.action.msg2", new Object[] { catalog }); //$NON-NLS-1$
		if (MessageDialog
				.openQuestion(
						new Shell(Display.getCurrent()),
						RelationsMessages
						.getString("DBDeleteAction.action.msg1"), //$NON-NLS-1$
						question)) {

			// first drop the database content
			dropTables(this.log);

			// then change to the default embedded database
			final IDBSettings tempSettings = new TempSettings("", //$NON-NLS-1$
					RelationsConstants.DFT_DB_EMBEDDED, "", "", //$NON-NLS-1$ //$NON-NLS-2$
					this.dbSettings.getDBConnectionConfig());
			final IDBChange changeDB = ContextInjectionFactory.make(
					ChangeDB.class, this.context);
			changeDB.setTemporarySettings(tempSettings);
			changeDB.execute();

			// finally mark the file system traces of the embedded database
			// deleted
			final File store = EmbeddedCatalogHelper.getDBStorePath();
			markDeleted(new File(store, catalog), this.log);
			markDeleted(
					new File(new File(store.getParentFile(),
							RelationsConstants.LUCENE_STORE), catalog), this.log);
		}
	}

	private static void dropTables(final Logger log) {
		try {
			final ICreatableHome[] homes = new ICreatableHome[5];
			homes[0] = BOMHelper.getTermHome();
			homes[1] = BOMHelper.getTextHome();
			homes[2] = BOMHelper.getPersonHome();
			homes[3] = BOMHelper.getRelationHome();
			homes[4] = BOMHelper.getEventStoreHome();

			final DefaultStatement statement = new DefaultStatement();
			for (int i = 0; i < homes.length; i++) {
				final String[] sql = homes[i].getSQLDrop();
				for (int j = 0; j < sql.length; j++) {
					statement.execute(sql[j]);
				}
			}
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
			MessageDialog
			.openError(
					new Shell(Display.getCurrent()),
					RelationsMessages
					.getString("DBDeleteAction.error.title"), ERROR_MSG); //$NON-NLS-1$
		}
	}

	private static void markDeleted(final File directory, final Logger log) {
		final File marker = new File(directory,
				EmbeddedCatalogHelper.DELETED_MARKER);
		try {
			marker.createNewFile();
		}
		catch (final IOException exc) {
			log.error(exc, exc.getMessage());
			MessageDialog
			.openError(
					new Shell(Display.getCurrent()),
					RelationsMessages
					.getString("DBDeleteAction.error.title"), ERROR_MSG); //$NON-NLS-1$
		}
	}

	/**
	 * Convenience method: deletes the catalog in use in the embedded database.
	 *
	 * @param settings
	 *            {@link DBSettings}
	 * @param log
	 *            {@link Logger}
	 */
	public static void deleteEmbedded(final DBSettings settings,
			final Logger log) {
		dropTables(log);
		markDeleted(
				new File(EmbeddedCatalogHelper.getDBStorePath(),
						settings.getCatalog()),
		        log);
	}

}
