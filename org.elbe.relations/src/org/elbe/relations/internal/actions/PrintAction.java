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
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.internal.controller.PrintOutManager;
import org.elbe.relations.internal.wizards.PrintOutWizard;
import org.elbe.relations.services.IBrowserManager;

/**
 * Action to start the print out of selected content.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class PrintAction implements ICommand {

	@Inject
	private IEclipseContext context;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private PrintOutManager printOutManager;

	@Inject
	private IBrowserManager browserManager;

	@Inject
	private Logger log;

	@Override
	public void execute() {
		final PrintOutWizard lWizard = ContextInjectionFactory.make(
		        PrintOutWizard.class, context);
		final WizardDialog lDialog = new WizardDialog(shell, lWizard);
		if (lDialog.open() == Window.OK) {
			if (lWizard.isInitNew()) {
				if (!printOutManager.initNew(lWizard.getPrintOutFileName(),
				        lWizard.getPrintOutPlugin())) {
					return;
				}
			} else {
				if (!printOutManager.initFurther(lWizard.getPrintOutFileName())) {
					return;
				}
			}
			printOutManager.setContentScope(lWizard.getPrintOutScope());
			printOutManager.setPrintOutReferences(lWizard
			        .getPrintOutReferences());

			final PrintJob lJob = new PrintJob(printOutManager,
			        browserManager.getSelectedModel());
			final ProgressMonitorDialog lMonitor = new ProgressMonitorJobsDialog(
			        shell);
			lMonitor.open();
			try {
				lMonitor.run(true, true, lJob);
			}
			catch (final InvocationTargetException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final InterruptedException exc) {
				log.error(exc, exc.getMessage());
			}
			finally {
				lMonitor.close();
			}
		}
	}

	// --- inner classes ---

	private class PrintJob implements IRunnableWithProgress {
		private final PrintOutManager printManager;
		private final IItem selectedItem;

		public PrintJob(final PrintOutManager inManager, final IItem inSelected) {
			printManager = inManager;
			selectedItem = inSelected;
		}

		@Override
		public void run(final IProgressMonitor inMonitor) {
			Collection<IItem> lItems;
			try {
				lItems = printManager.getItemSet(selectedItem);
				final SubMonitor lProgress = SubMonitor.convert(inMonitor,
				        lItems.size());
				lProgress
				        .beginTask(
				                RelationsMessages
				                        .getString("PrintAction.job.start"), lItems.size()); //$NON-NLS-1$
				int lNumberOfPrinted = 0;
				for (final IItem lItem : lItems) {
					lNumberOfPrinted += printManager.printItem(lItem);
					lProgress.worked(1);
				}

				// give feedback
				giveFeedback(getPrintCompleteAction(lNumberOfPrinted));
			}
			catch (final Exception exc) {
				final String lErrorMsg = exc.getMessage();
				giveFeedback(getErrorMsgAction(lErrorMsg));
				log.error(exc, exc.getMessage());
			}
			finally {
				try {
					printManager.close();
				}
				catch (final IOException exc) {
					// intentionally left empty
				}
			}
		}

		private void giveFeedback(final Action inAction) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					inAction.run();
				}
			});
		}
	}

	private Action getPrintCompleteAction(final int inNumberOfProcessed) {
		return new Action(RelationsMessages.getString("PrintAction.job.status")) { //$NON-NLS-1$
			@Override
			public void run() {
				MessageDialog
				        .openInformation(
				                shell,
				                RelationsMessages
				                        .getString("PrintAction.job.completed"), RelationsMessages.getString("PrintAction.job.completed.msg", new Object[] { new Integer(inNumberOfProcessed) })); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
	}

	private Action getErrorMsgAction(final String inErrorMsg) {
		return new Action(
		        RelationsMessages.getString("PrintAction.error.title")) { //$NON-NLS-1$
			@Override
			public void run() {
				MessageDialog
				        .openError(shell, RelationsMessages
				                .getString("PrintAction.error.msg"), inErrorMsg); //$NON-NLS-1$
			}
		};
	}

}
