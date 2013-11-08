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
package org.elbe.relations.internal.backup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMHelper;
import org.hip.kernel.bom.AbstractSerializer;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.exc.VException;

/**
 * Utility class to backup the actual database to an XML file.
 * 
 * @author Luthiger Created on 04.10.2008
 */
public class XMLExport {
	private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$
	private final static String TAG_START = "<%s>" + NL; //$NON-NLS-1$
	private final static String TAG_END = "</%s>" + NL; //$NON-NLS-1$

	private final static String NODE_ROOT = "RelationsExport"; //$NON-NLS-1$
	public final static String NODE_TERMS = "TermEntries"; //$NON-NLS-1$
	public final static String NODE_TEXTS = "TextEntries"; //$NON-NLS-1$
	public final static String NODE_PERSONS = "PersonEntries"; //$NON-NLS-1$
	public final static String NODE_RELATIONS = "RelationEntries"; //$NON-NLS-1$

	private final File exportFile;
	private OutputStream outputStream = null;
	private final Locale appLocale;

	/**
	 * XMLBackup constructor
	 * 
	 * @param inExportFileName
	 *            String name of the backup file
	 * @param inAppLocale
	 *            {@link Locale} the application's locale
	 * @throws IOException
	 */
	public XMLExport(final String inExportFileName, final Locale inAppLocale)
			throws IOException {
		exportFile = new File(inExportFileName);
		appLocale = inAppLocale;
		deleteExisting(exportFile);
		if (!exportFile.exists() && exportFile.getParentFile().exists()) {
			if (exportFile.createNewFile()) {
				if (!exportFile.canRead() || !exportFile.canWrite()) {
					throw new IOException(
							"Could not open file for read/write: " + exportFile.getName()); //$NON-NLS-1$
				}
				outputStream = createStream(exportFile);
			}
		}
	}

	protected OutputStream createStream(final File inExportFile)
			throws IOException {
		final FileOutputStream lStream = new FileOutputStream(inExportFile);
		return new BufferedOutputStream(lStream);
	}

	private boolean deleteExisting(final File inFile) {
		if (inFile.exists()) {
			return inFile.delete();
		}
		return true;
	}

	/**
	 * Perform the export to an XML file.
	 * 
	 * @param inMonitor
	 *            IProgressMonitor
	 * @return int number of backuped database entries
	 * @throws VException
	 * @throws SQLException
	 * @throws IOException
	 */
	public int export(final IProgressMonitor inMonitor) throws VException,
			SQLException, IOException {
		final SubMonitor lProgress = SubMonitor.convert(inMonitor, 100);
		int outExported = 0;

		appendText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL); //$NON-NLS-1$
		final DateFormat lFormat = DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, DateFormat.MEDIUM, appLocale);
		appendText(String
				.format("<%s date=\"%s\">" + NL, NODE_ROOT, lFormat.format(Calendar.getInstance().getTime()))); //$NON-NLS-1$

		outExported += processTable(
				RelationsMessages.getString("XMLExport.export.terms"), NODE_TERMS, BOMHelper.getTermHome(), lProgress.newChild(25)); //$NON-NLS-1$
		if (inMonitor.isCanceled())
			return outExported;

		outExported += processTable(
				RelationsMessages.getString("XMLExport.export.texts"), NODE_TEXTS, BOMHelper.getTextHome(), lProgress.newChild(25)); //$NON-NLS-1$
		if (inMonitor.isCanceled())
			return outExported;

		outExported += processTable(
				RelationsMessages.getString("XMLExport.export.persons"), NODE_PERSONS, BOMHelper.getPersonHome(), lProgress.newChild(25)); //$NON-NLS-1$
		if (inMonitor.isCanceled())
			return outExported;

		outExported += processTable(
				RelationsMessages.getString("XMLExport.export.relations"), NODE_RELATIONS, BOMHelper.getRelationHome(), lProgress.newChild(25)); //$NON-NLS-1$
		if (inMonitor.isCanceled())
			return outExported;

		appendEnd(NODE_ROOT);

		return outExported;
	}

	private int processTable(final String inTaskName, final String inNodeName,
			final GeneralDomainObjectHome inHome,
			final IProgressMonitor inMonitor) throws IOException, VException,
			SQLException {
		int outExported = 0;

		inMonitor.subTask(inTaskName);
		appendStart(inNodeName);
		outExported += processSelection(inHome, inMonitor);
		appendText(NL);
		appendEnd(inNodeName);

		return outExported;
	}

	private void appendStart(final String inText) throws IOException {
		appendText(String.format(TAG_START, inText));
	}

	private void appendEnd(final String inText) throws IOException {
		appendText(String.format(TAG_END, inText));
	}

	/**
	 * Close the backup stream.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (outputStream != null) {
			outputStream.close();
		}
	}

	private int processSelection(final GeneralDomainObjectHome inHome,
			final IProgressMonitor inMonitor) throws VException, SQLException,
			IOException {
		final SubMonitor lProgress = SubMonitor.convert(inMonitor, 100);
		int outExported = 0;
		final QueryResult lResult = inHome.select();
		final AbstractSerializer lVisitor = new RelationsSerializer();
		while (lResult.hasMoreElements()) {
			final GeneralDomainObject lModel = lResult.nextAsDomainObject();
			if (lModel != null) {
				lModel.accept(lVisitor);
				appendText(lVisitor.toString());
				lModel.release();
				lVisitor.clear();
			}

			outExported++;
			lProgress.worked(1);
		}
		return outExported;
	}

	private void appendText(final String inText) throws IOException {
		if (outputStream == null)
			return;
		outputStream.write(inText.getBytes());
	}

}
