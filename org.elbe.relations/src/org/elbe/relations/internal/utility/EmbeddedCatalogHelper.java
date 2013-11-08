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
package org.elbe.relations.internal.utility;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.internal.data.IDBSettings;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;

/**
 * Helper class providing utility information about the embedded (Derby)
 * database's catalogs.
 * 
 * @author Luthiger Created on 07.01.2007
 */
@SuppressWarnings("restriction")
public class EmbeddedCatalogHelper {
	public final static String DELETED_MARKER = ".deleted"; //$NON-NLS-1$
	public final static String REINDEX_MARKER = ".reindex"; //$NON-NLS-1$

	private final static IStatus ERROR_EXISTS = new Status(
			Status.ERROR,
			Activator.getSymbolicName(),
			1,
			"RelationsMessages.getString(\"EmbeddedCatalogHelper.error.exists\")", null); //$NON-NLS-1$
	private final static IStatus ERROR_CHAR = new Status(
			Status.ERROR,
			Activator.getSymbolicName(),
			1,
			"RelationsMessages.getString(\"EmbeddedCatalogHelper.error.chars\")", null); //$NON-NLS-1$
	private final static Pattern ALLOWED_CHAR = Pattern.compile("\\w*"); //$NON-NLS-1$

	private final String[] catalogs;

	/**
	 * EmbeddedCatalogHelper constructor.
	 */
	public EmbeddedCatalogHelper() {
		catalogs = getCatalogs();
	}

	/**
	 * This method returns all catalogs of the embedded database.
	 * 
	 * @return String[] containing the name of all catalogs of the embedded
	 *         (Derby) database.
	 */
	public static String[] getCatalogs() {
		final File lDBStore = getDBStorePath();

		// we retrieve all directories in the db store except those marked
		// deleted, i.e. containing the delete marker file.
		final File[] lDBDirs = lDBStore.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File inFile) {
				if (!inFile.isDirectory())
					return false;
				final String[] lContent = inFile.list(new FilenameFilter() {
					@Override
					public boolean accept(final File inArg0,
							final String inFileName) {
						return DELETED_MARKER.equals(inFileName);
					}
				});
				return lContent.length == 0;
			}
		});
		final String[] outEmbeddedDBs = new String[lDBDirs.length];
		for (int i = 0; i < lDBDirs.length; i++) {
			outEmbeddedDBs[i] = lDBDirs[i].getName();
		}
		return outEmbeddedDBs;
	}

	/**
	 * Validates the specified input.
	 * 
	 * @param inInput
	 *            String
	 * @return IStatus <code>ERROR_EXISTS</code> if the inputed value equals an
	 *         existing embedded database, <code>ERROR_CHAR</code> if the input
	 *         contains characters that are not allowed, <code>OK_STATUS</code>
	 *         else.
	 */
	public IStatus validate(final String inInput) {
		if (checkCatalogExists(inInput)) {
			return ERROR_EXISTS;
		}
		if (!ALLOWED_CHAR.matcher(inInput).matches()) {
			return ERROR_CHAR;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Validates the specified input, checks for allowed characters only.
	 * 
	 * @param inInput
	 *            String
	 * @return IStatus <code>OK_STATUS</code> if the inputed data contains only
	 *         allowed characters, else returns <code>ERROR_CHAR</code>.
	 */
	public IStatus validateForAllowedChars(final String inInput) {
		if (!ALLOWED_CHAR.matcher(inInput).matches()) {
			return ERROR_CHAR;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Checks for the existence of a catalog (of an embedded database) with the
	 * specified name.
	 * 
	 * @param inCatalogName
	 *            String the name of the catalog to check
	 * @return boolean <code>true</code> if a catalog with the name exists, else
	 *         <code>false</code>.
	 */
	public boolean checkCatalogExists(final String inCatalogName) {
		for (final String lCatalogName : catalogs) {
			if (inCatalogName.equals(lCatalogName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convenience method: checks whether the default embedded database exists.
	 * If not, we may have to do initial DB setup.
	 * 
	 * @return boolean <code>true</code> if there is a directory having the name
	 *         of the default database.
	 */
	public static boolean hasDefaultEmbedded() {
		final File lDBStore = getDBStorePath();
		final File[] lDBDirs = lDBStore.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File inFile) {
				return inFile.isDirectory();
			}
		});
		for (int i = 0; i < lDBDirs.length; i++) {
			if (RelationsConstants.DFT_DB_EMBEDDED.equals(lDBDirs[i].getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Workaround: we're not able to delete the traces in the filesystem of an
	 * embedded database that has been deleted during a session, therefore, we
	 * do this in the next session during system start.
	 */
	public static void cleanUp() {
		final File lDBStore = getDBStorePath();
		cleanUp(lDBStore);
		cleanUp(new File(lDBStore.getParentFile(),
				RelationsConstants.LUCENE_STORE));
	}

	private static void cleanUp(final File inParent) {
		if (!inParent.exists()) {
			return;
		}

		// we retrieve all directories marked deleted, i.e. containing the
		// delete marker file.
		final File[] lDBDirs = inParent.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File inFile) {
				if (!inFile.isDirectory()) {
					return false;
				}
				final String[] lContent = inFile.list(new FilenameFilter() {
					@Override
					public boolean accept(final File inArg,
							final String inFileName) {
						return DELETED_MARKER.equals(inFileName);
					}
				});
				return lContent.length == 1;
			}
		});

		for (int i = 0; i < lDBDirs.length; i++) {
			deleteContent(lDBDirs[i]);
		}
	}

	private static void deleteContent(final File inDirectory) {
		final File[] lContent = inDirectory.listFiles();
		for (int i = 0; i < lContent.length; i++) {
			if (lContent[i].isDirectory()) {
				deleteContent(lContent[i]);
			}
			if (!lContent[i].delete()) {
				lContent[i].deleteOnExit();
			}
		}
		if (!inDirectory.delete()) {
			inDirectory.deleteOnExit();
		}
	}

	/**
	 * Delete the marker file in case we want to recreated the embedded database
	 * in the same session.
	 * 
	 * @param inCatalog
	 *            String Name of the new database
	 * @return boolean <code>true</code> if the file is successfully deleted;
	 *         <code>false</code> otherwise.
	 */
	public static boolean deleteMarker(final String inCatalog) {
		return deleteMarker(inCatalog, DELETED_MARKER);
	}

	private static boolean deleteMarker(final String inCatalog,
			final String inMarkerFile) {
		final File lMarker = new File(new File(getDBStorePath(), inCatalog),
				inMarkerFile);
		if (lMarker.exists()) {
			return lMarker.delete();
		}
		return false;
	}

	/**
	 * Check for reindex marker in the embedded database's catalog and reindex
	 * the catalog if marker exists. This is a workaround because a restore is
	 * not effective until a restart of the application.
	 * 
	 * @param inDBSettings
	 *            {@link IDBSettings}
	 * @param inContext
	 *            {@link IEclipseContext}
	 */
	public static void reindexChecked(final IDBSettings lDBSettings,
			final IEclipseContext inContext) {

		// return, if not embedded database
		if (!lDBSettings.getDBConnectionConfig().isEmbedded()) {
			return;
		}

		final String lCatalog = lDBSettings.getCatalog();
		// return, if not marked to reindex
		if (!isMarkedToReindex(lCatalog)) {
			return;
		}

		// delete marker file and start reindex
		deleteMarker(lCatalog, REINDEX_MARKER);

		final RelationsIndexer lIndexer = RelationsIndexerWithLanguage
				.createRelationsIndexer(inContext);
		if (!lIndexer.isIndexAvailable()) {
			try {
				lIndexer.initializeIndex();
			}
			catch (final IOException exc) {
				final Logger lLog = inContext.get(Logger.class);
				lLog.error(exc, exc.getMessage());
			}
		}
	}

	private static boolean isMarkedToReindex(final String inCatalog) {
		final File lDirectory = new File(getDBStorePath(), inCatalog);
		final String[] lContent = lDirectory.list(new FilenameFilter() {
			@Override
			public boolean accept(final File inArg0, final String inFileName) {
				return REINDEX_MARKER.equals(inFileName);
			}
		});
		return lContent != null ? lContent.length == 1 : false;
	}

	/**
	 * Convenience method to retrieve the path to the embedded DB's data in the
	 * file system.
	 * 
	 * @return File the path to the location in the file system where the
	 *         embedded DB's data is stored.
	 */
	public static File getDBStorePath() {
		final File lRoot = ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toFile();
		final File outStore = new File(lRoot, RelationsConstants.DERBY_STORE);
		if (!outStore.exists()) {
			outStore.mkdir();
		}
		return outStore;
	}

	/**
	 * @return String the absolute path of the embedded's default DB.
	 */
	public static String getEmbeddedDftDBChecked() {
		return getEmbeddedDBChecked(RelationsConstants.DFT_DB_EMBEDDED);
	}

	/**
	 * Returns the absolute path of the embedded DB for the specified catalog.
	 * 
	 * @param String
	 *            inCatalog the DB catalog
	 * @return String the absolute path of the embedded DB for the specified
	 *         catalog.
	 */
	public static String getEmbeddedDBChecked(final String inCatalog) {
		final File lDirectory = new File(getDBStorePath(), inCatalog);
		return lDirectory.getAbsolutePath();
	}

}
