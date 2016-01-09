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
package org.elbe.relations.internal.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.actions.DBDeleteAction;
import org.elbe.relations.internal.actions.IDBChange;
import org.elbe.relations.internal.actions.IndexerAction;
import org.elbe.relations.internal.backup.XMLImport;
import org.elbe.relations.internal.backup.XMLImport.RelationReplaceHelper;
import org.elbe.relations.internal.backup.ZippedXMLImport;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.utility.AbstractRunnableWithProgress;
import org.elbe.relations.internal.utility.DBPreconditionException;
import org.elbe.relations.internal.wizards.interfaces.IImportWizard;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.impl.KeyObjectImpl;
import org.hip.kernel.bom.impl.UpdateStatement;
import org.hip.kernel.exc.VException;

/**
 * Wizard to import the database content from a (zipped) XML file.
 *
 * @author Luthiger Created on 13.10.2008
 */
@SuppressWarnings("restriction")
public class ImportFromXML extends Wizard implements IImportWizard {
	private ImportFromXMLPage page;

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private UISynchronize sync;

	@Inject
	private IDataService dataService;

	@Inject
	private DBSettings dbSettings;

	@Inject
	private RelationsStatusLineManager statusLine;

	@PostConstruct
	public void init() {
		setWindowTitle(
		        RelationsMessages.getString("ImportFromXML.window.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		page = new ImportFromXMLPage("ImportFromXMLPage", context, log); //$NON-NLS-1$
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		page.saveToHistory();
		final String lImportFile = page.getFileName();
		statusLine.showStatusLineMessage(String.format(
		        RelationsMessages.getString("ImportFromXML.msg.status"), //$NON-NLS-1$
		        lImportFile));
		final IDBChange lCreateDB = page.getResultObject();
		getShell().setVisible(false);

		try {
			lCreateDB.checkPreconditions();
			if (createAndFill(lCreateDB, lImportFile)) {
				// load the data
				dataService
				        .loadData(RelationsConstants.TOPIC_DB_CHANGED_RELOAD);

				// index data
				final IndexerAction lAction = ContextInjectionFactory
				        .make(IndexerAction.class, context);
				lAction.setSilent(true);
				lAction.run();
			} else {
				getShell().setVisible(true);
				return false;
			}
		}
		catch (final DBPreconditionException exc) {
			MessageDialog.openError(new Shell(Display.getCurrent()),
			        RelationsMessages.getString("FormDBConnection.error.title"), //$NON-NLS-1$
			        exc.getMessage());
			getShell().setVisible(true);
			return false;
		}
		return true;
	}

	private boolean createAndFill(final IDBChange inCreateDB,
	        final String inImportFile) {
		final XMLImport lImport = inImportFile.endsWith(".zip") //$NON-NLS-1$
		        ? new ZippedXMLImport(inImportFile)
		        : new XMLImport(inImportFile);
		final int lWorkItemsCount = 6;

		// run catalog creation and data import with progress monitor
		IRunnableWithProgress lJob = new AbstractRunnableWithProgress() {
			@Override
			protected final Runnable getRunnableInitialize(
		            final IProgressMonitor inMonitor) {
				return new Runnable() {
					@Override
					public void run() {
						inMonitor.beginTask(String.format(
		                        RelationsMessages.getString(
		                                "ImportFromXML.job.import.start"), //$NON-NLS-1$
		                        inImportFile), lWorkItemsCount);
					}
				};
			}

			/**
			 * Create tables in database catalog - Parse XML file and create
			 * table entries
			 */
			@Override
			protected final int process(final IProgressMonitor inMonitor)
		            throws InvocationTargetException, InterruptedException {
				int outNumberOfImported = 0;
				sync.syncExec(new Runnable() {
					@Override
					public void run() {
						inCreateDB.execute();
					}
				});
				inMonitor.worked(1);
				if (inMonitor.isCanceled()) {
					throw new InterruptedException();
				}

				try {
					outNumberOfImported = lImport.processFile(inMonitor,
		                    dbSettings.getDBConnectionConfig()
		                            .canSetIdentityField());
					monitorWorked(inMonitor);
				}
				catch (final InterruptedException exc) {
					throw exc;
				}
				catch (final Exception exc) {
					throw new InvocationTargetException(exc);
				}
				return outNumberOfImported;
			}

			@Override
			protected final Runnable getRunnableFeedback(
		            final int inNumberOfProcessed) {
				final String lFeedback = String.format(
		                RelationsMessages
		                        .getString("ImportFromXML.job.import.feedback"), //$NON-NLS-1$
		                inNumberOfProcessed);
				return new Runnable() {
					@Override
					public void run() {
						if (dbSettings.getDBConnectionConfig()
		                        .canSetIdentityField()) {
							MessageDialog.openInformation(shell,
		                            RelationsMessages.getString(
		                                    "ImportFromXML.job.import.success"), //$NON-NLS-1$
		                            lFeedback);
						} else {
							statusLine.showStatusLineMessage(lFeedback);
						}
					}
				};
			}
		};

		final ProgressMonitorDialog lImportDialog = new ProgressMonitorDialog(
		        shell);
		lImportDialog.open();
		try {
			lImportDialog.run(true, true, lJob);
		}
		catch (final InvocationTargetException exc) {
			log.error(exc, exc.getMessage());
			restorePrevious(inCreateDB);
			return false;
		}
		catch (final InterruptedException exc) {
			log.debug("Import of data interrupted by user."); //$NON-NLS-1$
			restorePrevious(inCreateDB);
			return false;
		}
		finally {
			lImportDialog.close();
		}

		if (!dbSettings.getDBConnectionConfig().canSetIdentityField()) {
			// run relation rebind with progress monitor
			final Collection<RelationReplaceHelper> lRelationsToRebind = lImport
			        .getRelationsToRebind();

			lJob = new AbstractRunnableWithProgress() {
				@Override
				protected Runnable getRunnableInitialize(
			            final IProgressMonitor inMonitor) {
					return new Runnable() {
						@Override
						public void run() {
							inMonitor.beginTask(
			                        RelationsMessages.getString(
			                                "ImportFromXML.job.rebind.start"), //$NON-NLS-1$
			                        lRelationsToRebind.size());
						}
					};
				}

				@Override
				protected int process(final IProgressMonitor inMonitor)
			            throws InvocationTargetException, InterruptedException {
					return processRebind(lRelationsToRebind, inMonitor);
				}

				@Override
				protected Runnable getRunnableFeedback(
			            final int inNumberOfProcessed) {
					final String lFeedback = String.format(
			                RelationsMessages.getString(
			                        "ImportFromXML.job.rebind.feedback"), //$NON-NLS-1$
			                inNumberOfProcessed);
					return new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(shell,
			                        RelationsMessages.getString(
			                                "ImportFromXML.job.rebind.success"), //$NON-NLS-1$
			                        lFeedback);
						}
					};
				}
			};
			final ProgressMonitorDialog lRebindDialog = new ProgressMonitorDialog(
			        shell);
			lRebindDialog.open();
			try {
				lRebindDialog.run(true, true, lJob);
			}
			catch (final InvocationTargetException exc) {
				log.error(exc, exc.getMessage());
				return false;
			}
			catch (final InterruptedException exc) {
				return false;
			}
			finally {
				lRebindDialog.close();
			}
		}
		return true;
	}

	private void monitorWorked(final IProgressMonitor inMonitor) {
		if (inMonitor != null) {
			inMonitor.worked(1);
		}
	}

	private int processRebind(
	        final Collection<RelationReplaceHelper> inRelationsToRebind,
	        final IProgressMonitor inMonitor)
	                throws InvocationTargetException, InterruptedException {
		int outNumberOfEntries = 0;
		final int CHUNK_LEN = 20;
		final RelationHome lHome = BOMHelper.getRelationHome();
		UpdateStatement lStatement;

		try {
			final Collection<String> lUpdates = new ArrayList<String>(
			        CHUNK_LEN);
			for (final RelationReplaceHelper lRelationToRebind : inRelationsToRebind) {
				createUpdates(lHome, lRelationToRebind, lUpdates);

				// update in chunks to improve performance
				if (lUpdates.size() >= CHUNK_LEN) {
					lStatement = new UpdateStatement();
					lStatement.setUpdates(lUpdates);
					lStatement.executeUpdate();
					lUpdates.clear();
				}
				inMonitor.worked(1);
				outNumberOfEntries++;
				if (inMonitor.isCanceled()) {
					throw new InterruptedException();
				}
			}
			// update the last chunk
			if (lUpdates.size() > 0) {
				lStatement = new UpdateStatement();
				lStatement.setUpdates(lUpdates);
				lStatement.executeUpdate();
			}
		}
		catch (final InterruptedException exc) {
			throw exc;
		}
		catch (final Exception exc) {
			throw new InvocationTargetException(exc);
		}

		return outNumberOfEntries;
	}

	private void restorePrevious(final IDBChange inChangeDB) {
		if (dbSettings.getDBConnectionConfig().isEmbedded()) {
			DBDeleteAction.deleteEmbedded(dbSettings, log);
		}
		inChangeDB.restore();
		dataService.loadData(RelationsConstants.TOPIC_DB_CHANGED_RELOAD);
	}

	private void createUpdates(final RelationHome inHome,
	        final RelationReplaceHelper inRelationToRebind,
	        final Collection<String> inUpdates) throws VException {
		// item1
		KeyObject lChange = new KeyObjectImpl();
		lChange.setValue(RelationHome.KEY_ITEM1,
		        new Long(inRelationToRebind.newID.itemID));
		KeyObject lWhere = new KeyObjectImpl();
		lWhere.setValue(RelationHome.KEY_TYPE1,
		        new Integer(inRelationToRebind.oldID.itemType));
		lWhere.setValue(RelationHome.KEY_ITEM1,
		        new Long(inRelationToRebind.oldID.itemID));
		inUpdates.add(inHome.createUpdateString(lChange, lWhere));
		// item2
		lChange = new KeyObjectImpl();
		lChange.setValue(RelationHome.KEY_ITEM2,
		        new Long(inRelationToRebind.newID.itemID));
		lWhere = new KeyObjectImpl();
		lWhere.setValue(RelationHome.KEY_TYPE2,
		        new Integer(inRelationToRebind.oldID.itemType));
		lWhere.setValue(RelationHome.KEY_ITEM2,
		        new Long(inRelationToRebind.oldID.itemID));
		inUpdates.add(inHome.createUpdateString(lChange, lWhere));
	}

	@Override
	public void dispose() {
		if (page != null) {
			page.dispose();
		}
	}

}
