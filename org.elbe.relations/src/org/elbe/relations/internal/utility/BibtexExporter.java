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
package org.elbe.relations.internal.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.Text;
import org.elbe.relations.data.bom.TextHome;
import org.elbe.relations.db.IDataService;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.impl.OrderObjectImpl;

/**
 * This class proceeds the job of exporting the text items to a BibTEX file.
 *
 * @author Luthiger Created on 06.05.2007
 */
@SuppressWarnings("restriction")
public class BibtexExporter {
	private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private Logger log;

	@Inject
	private IDataService dataService;

	private String fileName;

	/**
	 * Sets the name of the BibTEX file.
	 *
	 * @param inBibtexName
	 *            String name of the BibTEX file.
	 */
	public void setFileName(final String inBibtexName) {
		fileName = inBibtexName;
	}

	/**
	 * Starts the export job.
	 */
	public void export() {
		final ProgressMonitorDialog lDialog = new ProgressMonitorDialog(shell);
		lDialog.open();
		final ExportJob lJob = new ExportJob();
		try {
			lDialog.run(true, true, lJob);
		}
		catch (final InvocationTargetException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final InterruptedException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	// --- inner classes ---

	private class ExportJob implements IRunnableWithProgress {

		@Override
		public void run(final IProgressMonitor inMonitor) {
			final File lBibtex = new File(fileName);
			final int lNumberOf = dataService.getTexts().size();
			final Collection<String> lUnique = new ArrayList<String>(lNumberOf);

			FileWriter lWriter = null;
			final SubMonitor lProgress = SubMonitor.convert(inMonitor,
			        lNumberOf);
			lProgress.beginTask(RelationsMessages
			        .getString("BibtexExporter.action.message"), lNumberOf); //$NON-NLS-1$
			try {
				lWriter = new FileWriter(lBibtex);

				final OrderObject lOrder = new OrderObjectImpl();
				lOrder.setValue(TextHome.KEY_AUTHOR, 1);
				final QueryResult lTexts = BOMHelper.getTextHome()
				        .select(lOrder);

				boolean lFirst = true;
				while (lTexts.hasMoreElements()) {
					if (!lFirst) {
						lWriter.write(NL + NL);
					}
					lFirst = false;

					final Text lText = (Text) lTexts.nextAsDomainObject();
					lWriter.write(lText.getBibtexFormatted(lUnique));
					lText.release();

					lProgress.worked(1);
				}
			}
			catch (final Exception exc) {
				log.error(exc, exc.getMessage());
			}
			finally {
				if (lWriter != null) {
					try {
						lWriter.close();
					}
					catch (final IOException exc) {
						// intentionally left empty
					}
				}
				lProgress.done();
			}
		}
	}

}
