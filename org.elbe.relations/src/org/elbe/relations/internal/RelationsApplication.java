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
package org.elbe.relations.internal;

import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * This Relations application class is a wrapper of the
 * <code>E4Application</code>. We need this to make restart of the application
 * possible (see <code>RelationsApplication.start()</code>.
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
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class RelationsApplication implements IApplication {
	public static final String EXIT_KEY = "relation.exit.key";

	private E4Application e4App;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	@Override
	public Object start(final IApplicationContext inContext) throws Exception {
		e4App = new E4Application();
		e4App.start(inContext);
		final Object outReturn = inContext.getArguments().get(EXIT_KEY);
		return outReturn == null ? IApplication.EXIT_OK : outReturn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	@Override
	public void stop() {
		e4App.stop();
	}

}
