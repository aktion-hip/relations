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
	 * @param inSettings
	 *            {@link DBSettings}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @param inLog
	 *            {@link Logger}
	 */
	public DBDeleteAction(final DBSettings inSettings,
			final IEclipseContext inContext, final Logger inLog) {
		dbSettings = inSettings;
		context = inContext;
		log = inLog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.actions.ICommand#execute()
	 */
	@Override
	public void execute() {
		final String lCatalog = dbSettings.getCatalog();
		final String lQuestion = RelationsMessages.getString(
				"DBDeleteAction.action.msg2", new Object[] { lCatalog }); //$NON-NLS-1$
		if (MessageDialog
				.openQuestion(
						new Shell(Display.getCurrent()),
						RelationsMessages
								.getString("DBDeleteAction.action.msg1"), lQuestion)) { //$NON-NLS-1$

			// first drop the database content
			dropTables(log);

			// then change to the default embedded database
			final IDBSettings lTempSettings = new TempSettings("",
					RelationsConstants.DFT_DB_EMBEDDED, "", "",
					dbSettings.getDBConnectionConfig());
			final IDBChange lChangeDB = ContextInjectionFactory.make(
					ChangeDB.class, context);
			lChangeDB.setTemporarySettings(lTempSettings);
			lChangeDB.execute();

			// finally mark the file system traces of the embedded database
			// deleted
			final File lStore = EmbeddedCatalogHelper.getDBStorePath();
			markDeleted(new File(lStore, lCatalog), log);
			markDeleted(new File(new File(lStore.getParentFile(),
					RelationsConstants.LUCENE_STORE), lCatalog), log);
		}
	}

	private static void dropTables(final Logger inLog) {
		try {
			final ICreatableHome[] lHomes = new ICreatableHome[4];
			lHomes[0] = BOMHelper.getTermHome();
			lHomes[1] = BOMHelper.getTextHome();
			lHomes[2] = BOMHelper.getPersonHome();
			lHomes[3] = BOMHelper.getRelationHome();

			final DefaultStatement lStatement = new DefaultStatement();
			for (int i = 0; i < lHomes.length; i++) {
				final String[] lSQL = lHomes[i].getSQLDrop();
				for (int j = 0; j < lSQL.length; j++) {
					lStatement.execute(lSQL[j]);
				}
			}
		}
		catch (final SQLException exc) {
			inLog.error(exc, exc.getMessage());
			MessageDialog
					.openError(
							new Shell(Display.getCurrent()),
							RelationsMessages
									.getString("DBDeleteAction.error.title"), ERROR_MSG); //$NON-NLS-1$
		}
	}

	private static void markDeleted(final File inDirectory, final Logger inLog) {
		final File lMarker = new File(inDirectory,
				EmbeddedCatalogHelper.DELETED_MARKER);
		try {
			lMarker.createNewFile();
		}
		catch (final IOException exc) {
			inLog.error(exc, exc.getMessage());
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
	 * @param inSettings
	 *            {@link DBSettings}
	 * @param inLog
	 *            {@link Logger}
	 */
	public static void deleteEmbedded(final DBSettings inSettings,
			final Logger inLog) {
		dropTables(inLog);
		markDeleted(
				new File(EmbeddedCatalogHelper.getDBStorePath(),
						inSettings.getCatalog()), inLog);
	}

}
