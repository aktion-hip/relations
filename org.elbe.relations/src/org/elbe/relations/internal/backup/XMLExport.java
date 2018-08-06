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
import org.elbe.relations.data.utility.RelationsSerializer;
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
public class XMLExport implements AutoCloseable {
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
	private final int numberOfItems;

	/**
	 * XMLBackup constructor
	 *
	 * @param exportFileName
	 *            String name of the backup file
	 * @param appLocale
	 *            {@link Locale} the application's locale
	 * @param numberOfItems
	 *            int
	 * @throws IOException
	 */
	public XMLExport(final String exportFileName, final Locale appLocale,
			final int numberOfItems)
					throws IOException {
		this.numberOfItems = numberOfItems;
		this.exportFile = new File(exportFileName);
		this.appLocale = appLocale;
		deleteExisting(this.exportFile);
		if (!this.exportFile.exists() && this.exportFile.getParentFile().exists()) {
			if (this.exportFile.createNewFile()) { // NOPMD
				if (!this.exportFile.canRead() || !this.exportFile.canWrite()) {
					throw new IOException(
							"Could not open file for read/write: " + this.exportFile.getName()); //$NON-NLS-1$
				}
				this.outputStream = createStream(this.exportFile);
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
	 * @param monitor
	 *            IProgressMonitor
	 * @return int number of backuped database entries
	 * @throws VException
	 * @throws SQLException
	 * @throws IOException
	 */
	public int export(final IProgressMonitor monitor) throws VException,
	SQLException, IOException {
		final SubMonitor progress = SubMonitor.convert(monitor, 100);
		int outExported = 0;

		appendText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL); //$NON-NLS-1$
		final DateFormat format = DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, DateFormat.MEDIUM, this.appLocale);
		appendText(String
				.format("<%s date=\"%s\" countAll=\"%s\">" + NL, NODE_ROOT, //$NON-NLS-1$
						format.format(Calendar.getInstance().getTime()),
						this.numberOfItems));

		outExported += processTable(
				RelationsMessages.getString("XMLExport.export.terms"), //$NON-NLS-1$
				NODE_TERMS, BOMHelper.getTermHome(), progress.newChild(25));
		if (monitor.isCanceled()) {
			return outExported;
		}

		outExported += processTable(
				RelationsMessages.getString("XMLExport.export.texts"), //$NON-NLS-1$
				NODE_TEXTS, BOMHelper.getTextHome(), progress.newChild(25));
		if (monitor.isCanceled()) {
			return outExported;
		}

		outExported += processTable(
				RelationsMessages.getString("XMLExport.export.persons"), //$NON-NLS-1$
				NODE_PERSONS, BOMHelper.getPersonHome(), progress.newChild(25));
		if (monitor.isCanceled()) {
			return outExported;
		}

		outExported += processTable(
				RelationsMessages.getString("XMLExport.export.relations"), //$NON-NLS-1$
				NODE_RELATIONS, BOMHelper.getRelationHome(),
				progress.newChild(25));
		if (monitor.isCanceled()) {
			return outExported;
		}

		appendEnd(NODE_ROOT);

		return outExported;
	}

	private int processTable(final String taskName, final String nodeName,
			final GeneralDomainObjectHome home, final IProgressMonitor monitor)
					throws IOException, VException, SQLException {
		int outExported = 0;

		monitor.subTask(taskName);
		appendStart(nodeName);
		outExported += processSelection(home, monitor);
		appendText(NL);
		appendEnd(nodeName);

		return outExported;
	}

	private void appendStart(final String text) throws IOException {
		appendText(String.format(TAG_START, text));
	}

	private void appendEnd(final String text) throws IOException {
		appendText(String.format(TAG_END, text));
	}

	/**
	 * Close the backup stream.
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		if (this.outputStream != null) {
			this.outputStream.close();
		}
	}

	private int processSelection(final GeneralDomainObjectHome home,
			final IProgressMonitor monitor)
					throws VException, SQLException, IOException {
		final SubMonitor progress = SubMonitor.convert(monitor, 100);
		int outExported = 0;
		final QueryResult result = home.select();
		final AbstractSerializer visitor = new RelationsSerializer();
		while (result.hasMoreElements()) {
			final GeneralDomainObject model = result.nextAsDomainObject();
			if (model != null) {
				model.accept(visitor);
				appendText(visitor.toString());
				model.release();
				visitor.clear();
			}

			outExported++;
			progress.worked(1);
		}
		return outExported;
	}

	private void appendText(final String text) throws IOException {
		if (this.outputStream == null) {
			return;
		}
		this.outputStream.write(text.getBytes());
	}

}
