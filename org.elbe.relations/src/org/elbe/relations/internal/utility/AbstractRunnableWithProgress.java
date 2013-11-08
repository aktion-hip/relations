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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Base class for runnable classes displaying a progress monitor.
 * 
 * @author Luthiger Created on 25.10.2008
 */
public abstract class AbstractRunnableWithProgress implements
		IRunnableWithProgress {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core
	 * .runtime.IProgressMonitor)
	 */
	@Override
	public void run(final IProgressMonitor inMonitor)
			throws InvocationTargetException, InterruptedException {
		int lNumberOfProcessed = 0;
		final boolean lHasMonitor = inMonitor != null;

		// initialize progress monitor
		if (lHasMonitor) {
			final Runnable lInitialize = getRunnableInitialize(inMonitor);
			lInitialize.run();
		}

		// do the work
		lNumberOfProcessed = process(inMonitor);

		// dispose progress monitor
		if (lHasMonitor) {
			inMonitor.done();
		}

		// give feedback
		getRunnableFeedback(lNumberOfProcessed);
	}

	protected abstract Runnable getRunnableInitialize(IProgressMonitor inMonitor);

	protected abstract int process(IProgressMonitor inMonitor)
			throws InvocationTargetException, InterruptedException;

	protected abstract Runnable getRunnableFeedback(int inNumberOfProcessed);

}
