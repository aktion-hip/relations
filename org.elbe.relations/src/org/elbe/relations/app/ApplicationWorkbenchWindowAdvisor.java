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

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.elbe.relations.RelationsMessages;

/**
 * The workbench window's advisor.
 * 
 * @author lbenno
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer inConfigurer) {
        super(inConfigurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer inConfigurer) {
        return new ApplicationActionBarAdvisor(inConfigurer);
    }
    
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer lConfigurer = getWindowConfigurer();
        lConfigurer.setInitialSize(new Point(1000, 750));
        lConfigurer.setShowCoolBar(true);
        lConfigurer.setShowStatusLine(true);
        lConfigurer.setShowProgressIndicator(true);
        lConfigurer.setShowFastViewBars(true);
        lConfigurer.setShowMenuBar(true);
		lConfigurer.setTitle(RelationsMessages.getString("RelationsWorkbenchWindowAdvisor.main.title"));		 //$NON-NLS-1$
    }
}
