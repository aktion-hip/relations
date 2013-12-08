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
package org.elbe.relations.app;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution.  
 * This class is used to run the application in compatibility mode. 
 * 
 * <p>
 * To restart the application, implementors have to inject the
 * <code>IApplicationContext</code> and then put the
 * <code>IApplication.EXIT_RESTART</code> to the
 * <code>IApplicationContext.getArguments()</code> map before they close the
 * <code>IWorkbench</code>. I.e.:
 * </p>
 * 
 * <pre>
 * appContext.getArguments().put(RelationsApplication.EXIT_KEY,
 * 		IApplication.EXIT_RESTART);
 * workbench.close();
 * </pre> 
 * */
public class RelationsApplication implements IApplication {
	public static final String EXIT_KEY = "relation.exit.key"; //$NON-NLS-1$

	@Override
	public Object start(IApplicationContext inContext) throws Exception {
		Display lDisplay = PlatformUI.createDisplay();
		try {
			PlatformUI.createAndRunWorkbench(lDisplay, new ApplicationWorkbenchAdvisor());
			final Object outReturn = inContext.getArguments().get(EXIT_KEY);
			return outReturn == null ? IApplication.EXIT_OK : outReturn;
		} finally {
			lDisplay.dispose();
		}
		
	}

	@Override
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench lWorkbench = PlatformUI.getWorkbench();
		final Display lDisplay = lWorkbench.getDisplay();
		lDisplay.syncExec(new Runnable() {
			public void run() {
				if (!lDisplay.isDisposed())
					lWorkbench.close();
			}
		});
	}
}
