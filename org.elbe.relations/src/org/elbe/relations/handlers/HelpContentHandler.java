/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2016, Benno Luthiger
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

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.server.WebappManager;

/**
 * Handler to display the Relations help content.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public class HelpContentHandler {
	@Execute
	public void execute() throws Exception {
		BaseHelpSystem.ensureWebappRunning();
		final String helpURL = "http://" //$NON-NLS-1$
		        + WebappManager.getHost() + ":" //$NON-NLS-1$
		        + WebappManager.getPort() + "/help/index.jsp"; //$NON-NLS-1$
		BaseHelpSystem.getHelpBrowser(true).displayURL(helpURL);
	}

}