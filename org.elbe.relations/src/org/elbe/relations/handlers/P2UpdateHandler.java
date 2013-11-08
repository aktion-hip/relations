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
package org.elbe.relations.handlers;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class P2UpdateHandler {
	private static final String REPOSITORY_LOC = "http://relations-rcp.sourceforge.net/updates/";

	@Execute
	public void execute(final IProvisioningAgent inAgent, final Shell inParent,
			final UISynchronize inSync) {

		final Job lJob = new Job("Update Job") {
			@Override
			protected IStatus run(final IProgressMonitor inMonitor) {

				/* 1. prepare update plumbing */
				final ProvisioningSession lSession = new ProvisioningSession(
						inAgent);
				final UpdateOperation lOperation = new UpdateOperation(lSession);

				// create uri
				URI lUri = null;
				try {
					lUri = new URI(REPOSITORY_LOC);
				}
				catch (final URISyntaxException e) {
					return Status.CANCEL_STATUS;
				}

				// set location of artifact and metadata repo
				lOperation.getProvisioningContext().setArtifactRepositories(
						new URI[] { lUri });
				lOperation.getProvisioningContext().setMetadataRepositories(
						new URI[] { lUri });

				/* 2. check for updates */

				// run update checks causing I/O
				final IStatus lStatus = lOperation.resolveModal(inMonitor);

				// Failed to find updates (inform user and exit)
				if (lStatus.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
					inSync.syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog
									.openWarning(inParent, "No update",
											"No updates for the current installation have been found");
						}
					});
					return Status.CANCEL_STATUS;
				}

				/* 3. install updates (causing I/0!) */
				final ProvisioningJob lUpdateJob = lOperation
						.getProvisioningJob(inMonitor);
				System.out.println(lUpdateJob);
				lUpdateJob.schedule();
				// lOperation.getProvisioningJob(inMonitor).schedule();

				// Optionally register a job change listener to track
				// installation progress and notify user upon success
				return Status.OK_STATUS;
			}
		};
		lJob.schedule();
	}

}