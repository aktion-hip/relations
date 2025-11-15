/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.services.ISelectedTextProvider;
import org.elbe.relations.internal.utility.BrowserUtil;

import jakarta.inject.Named;

/**
 * Handler that gets selected text from the inspector view and pastes this text
 * as url in the default browser.
 *
 * @author lbenno
 */
public class OpenURLHandler {

    @Execute
    void openURL(final EPartService partService, final MApplication application,
            @Named(IServiceConstants.ACTIVE_SHELL) final Shell shell) {
        final MPart part = partService.findPart(RelationsConstants.PART_INSPECTOR);
        if (part != null) {
            if (part.getObject() instanceof final ISelectedTextProvider provider) { // NOPMD
                String url = provider.getSelection();
                if (!url.isEmpty()) {
                    if (!BrowserUtil.textIsURL(url)) {
                        url = BrowserUtil.PREFIX_HTTP + url;
                    }
                    BrowserUtil.startBrowser(url);
                }
            }
        }
    }

}
