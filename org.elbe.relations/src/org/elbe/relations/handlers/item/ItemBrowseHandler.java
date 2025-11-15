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
package org.elbe.relations.handlers.item;

import java.sql.SQLException;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.internal.bom.BOMHelper;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/** Handler to show the selected item (in the selection view) in the browser.
 *
 * @author Luthiger */
public class ItemBrowseHandler extends AbstractSelectionHandler {

    @Inject
    private IBrowserManager browserManager;

    @SuppressWarnings("restriction")
    @Inject
    private Logger log;

    @SuppressWarnings("unchecked")
    @Execute
    void setSelectionItemToBrowser(final IEclipseContext context, final EModelService modelService,
            final EPartService partService, final MApplication application) {
        if (getSelectionItem() == null) {
            return;
        }
        try {
            final IItemModel item = BOMHelper.getItem(getSelectionItem(), context);
            this.browserManager.setModel(CentralAssociationsModel.createCentralAssociationsModel(item, context));

            // move focus to browser
            final MElementContainer<MUIElement> lBrowserStack = (MElementContainer<MUIElement>) modelService
                    .find(RelationsConstants.PART_STACK_BROWSERS, application);
            final MUIElement browser = lBrowserStack.getSelectedElement();
            if (browser != null) {
                partService.activate((MPart) browser, true);
            }
        } catch (BOMException | VException | SQLException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

}
