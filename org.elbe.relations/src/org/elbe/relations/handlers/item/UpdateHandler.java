package org.elbe.relations.handlers.item;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.elbe.relations.RelationsMessages;

public class UpdateHandler {

	boolean cancelled = false;

	@Execute
	public void execute(IProvisioningAgent agent, UISynchronize sync,
	        IWorkbench workbench) {
		// update using a progress monitor
		final IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor)
		            throws InvocationTargetException, InterruptedException {
				update(agent, monitor, sync, workbench);
			}
		};

		try {
			new ProgressMonitorDialog(null).run(true, true, runnable);
		}
		catch (InvocationTargetException | InterruptedException exc) {
			exc.printStackTrace();
		}
	}

	private IStatus update(IProvisioningAgent agent, IProgressMonitor monitor,
	        UISynchronize sync, IWorkbench workbench) {
		final ProvisioningSession session = new ProvisioningSession(agent);
		// update the whole running profile, otherwise specify IUs
		final UpdateOperation operation = new UpdateOperation(session);

		final SubMonitor sub = SubMonitor.convert(monitor,
		        RelationsMessages.getString("UpdateHandler.monitor.checking"), 200); //$NON-NLS-1$

		// check if updates are available
		final IStatus status = operation.resolveModal(sub.newChild(100));
		if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
			showMessage(sync, RelationsMessages.getString("UpdateHandler.feedback.nothing")); //$NON-NLS-1$
			return Status.CANCEL_STATUS;
		} else {
			final ProvisioningJob provisioningJob = operation
			        .getProvisioningJob(sub.newChild(100));
			if (provisioningJob != null) {
				sync.syncExec(new Runnable() {

					@Override
					public void run() {
						final boolean performUpdate = MessageDialog
				                .openQuestion(null, RelationsMessages.getString("UpdateHandler.updates.title"), //$NON-NLS-1$
				                        RelationsMessages.getString("UpdateHandler.updates.msg")); //$NON-NLS-1$
						if (performUpdate) {
							provisioningJob.addJobChangeListener(
				                    new JobChangeAdapter() {
					                    @Override
					                    public void done(
				                                IJobChangeEvent event) {
						                    if (event.getResult().isOK()) {
							                    sync.syncExec(new Runnable() {

					                                @Override
					                                public void run() {
						                                final boolean restart = MessageDialog
				                                                .openQuestion(
				                                                        null,
				                                                        RelationsMessages.getString("UpdateHandler.success.title"), //$NON-NLS-1$
				                                                        RelationsMessages.getString("UpdateHandler.success.msg")); //$NON-NLS-1$
						                                if (restart) {
							                                workbench.restart();
						                                }
					                                }
				                                });
						                    } else {
							                    showError(sync,
				                                        event.getResult()
				                                                .getMessage());
							                    cancelled = true;
						                    }
					                    }
				                    });

							// since we switched to the UI thread for
				            // interacting with the user
				            // we need to schedule the provisioning thread,
				            // otherwise it would
				            // be executed also in the UI thread and not in a
				            // background thread
							provisioningJob.schedule();
						} else {
							cancelled = true;
						}
					}
				});
			} else {
				if (operation.hasResolved()) {
					showError(sync, RelationsMessages.getString("UpdateHandler.err.no.get") //$NON-NLS-1$
					        + operation.getResolutionResult());
				} else {
					showError(sync, RelationsMessages.getString("UpdateHandler.err.no.resolve")); //$NON-NLS-1$
				}
				cancelled = true;
			}
		}

		if (cancelled) {
			// reset cancelled flag
			cancelled = false;
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	private void showMessage(UISynchronize sync, final String message) {
		// as the provision needs to be executed in a background thread
		// we need to ensure that the message dialog is executed in
		// the UI thread
		sync.syncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openInformation(null, RelationsMessages.getString("UpdateHandler.feedback.title.info"), message); //$NON-NLS-1$
			}
		});
	}

	private void showError(UISynchronize sync, final String message) {
		// as the provision needs to be executed in a background thread
		// we need to ensure that the message dialog is executed in
		// the UI thread
		sync.syncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openError(null, RelationsMessages.getString("UpdateHandler.feedback.title.err"), message); //$NON-NLS-1$
			}
		});
	}
}