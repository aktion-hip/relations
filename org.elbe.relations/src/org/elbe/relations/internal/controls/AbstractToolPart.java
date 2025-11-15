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
package org.elbe.relations.internal.controls;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.dnd.ItemTransfer;
import org.elbe.relations.models.ILightWeightModel;

import jakarta.inject.Inject;

/**
 * Abstract base class for Relations <code>ViewPart</code>s, providing generic
 * functionality for views.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractToolPart implements IPartWithSelection {

    @Inject
    private EHandlerService handlerService;

    @Inject
    private ECommandService commandService;

    @Inject
    private ESelectionService selectionService;

    @Inject
    private Logger log;

    private MPart part;

    protected IBaseLabelProvider getLabelProvider() {
        return new LabelProvider() {
            @Override
            public Image getImage(final Object element) {
                return ((ILightWeightModel) element).getImage();
            }

            @Override
            public String getText(final Object element) {
                return element.toString();
            }
        };
    }

    protected void afterInit(final MPart part, final EMenuService service) {
        this.part = part;
        service.registerContextMenu(getControl(), getContextMenuID());
    }

    protected abstract Object getControl();

    protected abstract String getContextMenuID();

    /** Set the part's title.
     *
     * @param title String */
    protected void setPartName(final String title) {
        if (this.part != null) {
            this.part.setLabel(title);
        }
    }

    protected IDoubleClickListener getDoubleClickListener() {
        return event -> AbstractToolPart.this.handlerService.executeHandler(
                ParameterizedCommand.generateCommand(
                        AbstractToolPart.this.commandService.getCommand(ICommandIds.CMD_ITEM_SHOW), null));
    }

    protected Transfer[] getDragTypes() {
        return new Transfer[] { ItemTransfer.getInstance(this.log) };
    }

    protected DragSourceListener getDragSourceAdapter(final TableViewer viewer) {
        return new DragSourceAdapter() {
            @Override
            public void dragSetData(final DragSourceEvent event) {
                final IStructuredSelection selected = (IStructuredSelection) viewer.getSelection();
                if (!selected.isEmpty()) {
                    final Object[] lItems = selected.toArray();
                    final UniqueID[] lIDs = new UniqueID[lItems.length];
                    for (int i = 0; i < lItems.length; i++) {
                        final ILightWeightItem lItem = (ILightWeightItem) lItems[i];
                        lIDs[i] = new UniqueID(lItem.getItemType(), lItem.getID());
                    }
                    event.data = lIDs;
                }
            }
        };
    }

    protected MenuManager getMenuManager(final Control control) {
        final MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        control.setMenu(menuManager.createContextMenu(control));
        return menuManager;
    }

    protected ISelectionChangedListener getSelectionChangedListener() {
        return event -> AbstractToolPart.this.selectionService
                .setSelection(((IStructuredSelection) event.getSelection()).getFirstElement());
    }

    protected MPart getPart() {
        return this.part;
    }

}
