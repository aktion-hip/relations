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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.MessageFormat;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;
import org.hip.kernel.exc.VException;

/**
 * Action responsible to reindex the current database.
 *
 * @author Luthiger Created on 15.11.2006
 */
@SuppressWarnings("restriction")
public class IndexerAction extends Action {
	private boolean silent = false;

	@Inject
	private Logger log;

	@Inject
	private IDataService dataService;

	@Inject
	private IEclipseContext context;

	@Inject
	private UISynchronize jobManager;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private RelationsStatusLineManager statusLine;

	/**
	 * Setter for silent switch. If Action is set silent, no message to confirm
	 * the start of the job is displayed.
	 *
	 * @param inSilent
	 *            boolean
	 */
	public void setSilent(final boolean inSilent) {
		silent = inSilent;
	}

	/**
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		if (silent) {
			indexSilent();
			return;
		}

		final String lMessage1 = RelationsMessages
		        .getString("IndexerAction.msg.1"); //$NON-NLS-1$
		final String lMessage2 = RelationsMessages
		        .getString("IndexerAction.msg.2"); //$NON-NLS-1$
		final String lMessage3 = RelationsMessages
		        .getString("IndexerAction.msg.3"); //$NON-NLS-1$
		final String lMessage4 = RelationsMessages
		        .getString("IndexerAction.msg.4"); //$NON-NLS-1$

		final RelationsIndexer lIndexer = RelationsIndexerWithLanguage
		        .createRelationsIndexer(context);
		int lNumberOfIndexed = 0;
		try {
			lNumberOfIndexed = lIndexer.numberOfIndexed();
		}
		catch (final IOException exc) {
			// intentionally left empty
		}

		String lMessage = MessageFormat.format(lMessage1,
		        new Object[] { new Integer(lNumberOfIndexed) });
		if (lNumberOfIndexed == dataService.getNumberOfItems()) {
			lMessage += " " + lMessage2; //$NON-NLS-1$
		}
		lMessage += "\n\n" + //$NON-NLS-1$
		        MessageFormat.format(lMessage3,
		                new Object[] { dataService.getDBName() })
		        + "\n\n" + lMessage4; //$NON-NLS-1$
		if (MessageDialog.openQuestion(new Shell(Display.getCurrent()),
		        RelationsMessages.getString("IndexerAction.dialog.title"), //$NON-NLS-1$
		        lMessage)) {
			indexWithFeedback();
		}
	}

	/**
	 * Index silently and asynchronously.
	 */
	private void indexSilent() {
		jobManager.asyncExec(new Runnable() {
			@Override
			public void run() {
				final RelationsIndexer lIndexer = RelationsIndexerWithLanguage
		                .createRelationsIndexer(context);
				try {
					lIndexer.refreshIndex(new NullProgressMonitor());
				}
				catch (final IOException exc) {
					log.error(exc, exc.getMessage());
				}
				catch (final VException exc) {
					log.error(exc, exc.getMessage());
				}
				catch (final SQLException exc) {
					log.error(exc, exc.getMessage());
				}
			}
		});

	}

	private void indexWithFeedback() {
		final IEclipseContext lContext = context.createChild();
		final IndexJob lJob = new IndexJob(lContext, dataService, log);
		final ProgressMonitorDialog lDialog = new ProgressMonitorDialog(shell);
		lDialog.open();
		try {
			lDialog.run(true, true, lJob);
			statusLine.showStatusLineMessage(
			        RelationsMessages.getString("IndexerAction.job.feedback", //$NON-NLS-1$
			                new Object[] { lJob.getIndexed() }));
		}
		catch (final InvocationTargetException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final InterruptedException exc) {
			statusLine.showStatusLineMessage(RelationsMessages
			        .getString("action.indexer.status.cancelled")); //$NON-NLS-1$
			log.error(exc, exc.getMessage());
		}
		finally {
			lContext.dispose();
		}

	}

	// --- inner classes ---

	private class IndexJob implements IRunnableWithProgress {
		private final IEclipseContext context;
		private final IDataService dataService;
		private final Logger log;
		private int indexed = 0;

		public IndexJob(final IEclipseContext inContext,
		        final IDataService inDataService, final Logger inLogger) {
			context = inContext;
			dataService = inDataService;
			log = inLogger;
		}

		@Override
		public void run(final IProgressMonitor inMonitor) {
			context.set(IProgressMonitor.class.getName(), inMonitor);
			final SubMonitor lProgress = SubMonitor.convert(inMonitor,
			        dataService.getNumberOfItems());
			lProgress.beginTask(
			        RelationsMessages.getString("IndexerAction.job.start"), //$NON-NLS-1$
			        dataService.getNumberOfItems());
			final RelationsIndexer lIndexer = RelationsIndexerWithLanguage
			        .createRelationsIndexer(context);
			try {
				indexed = lIndexer.refreshIndex(lProgress);
			}
			catch (final IOException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final VException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final SQLException exc) {
				log.error(exc, exc.getMessage());
			}
			finally {
				inMonitor.done();
			}
		}

		/**
		 * @return int the number of indexed items
		 */
		int getIndexed() {
			return indexed;
		}
	}

}
