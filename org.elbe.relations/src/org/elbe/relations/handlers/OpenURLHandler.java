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

import java.net.MalformedURLException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.services.ISelectedTextProvider;
import org.elbe.relations.internal.utility.BrowserUtil;

/**
 * Handler that gets selected text from the inspector view and pastes this text
 * as url in the default browser.
 * 
 * @author lbenno
 */
@SuppressWarnings("restriction")
public class OpenURLHandler {

	@Inject
	private Logger log;

	@Execute
	void openURL(final EPartService inPartService,
	        final MApplication inApplication,
	        @Named(IServiceConstants.ACTIVE_SHELL) final Shell inShell) {
		final MPart lPart = inPartService
		        .findPart(RelationsConstants.PART_INSPECTOR);
		if (lPart != null) {
			if (lPart.getObject() instanceof ISelectedTextProvider) {
				String lURL = ((ISelectedTextProvider) lPart.getObject())
				        .getSelection();
				if (!lURL.isEmpty()) {
					try {
						if (!BrowserUtil.textIsURL(lURL)) {
							lURL = BrowserUtil.PREFIX_HTTP + lURL;
						}
						BrowserUtil.startBrowser(lURL);
					}
					catch (final PartInitException exc) {
						log.error(exc, exc.getMessage());
					}
					catch (final MalformedURLException exc) {
						MessageDialog
						        .openError(inShell, RelationsMessages.getString("handler.open.url.error.title"), //$NON-NLS-1$
						                RelationsMessages.getString("handler.open.url.error.msg")); //$NON-NLS-1$
						log.error(exc, exc.getMessage());
					}
				}
			}
		}
	}

}
