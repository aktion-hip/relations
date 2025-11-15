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
package org.elbe.relations.defaultbrowser.internal.controller;

import java.util.List;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.SelectionEditPolicy;
import org.eclipse.swt.SWT;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.defaultbrowser.internal.dnd.ItemDragPolicy;
import org.elbe.relations.defaultbrowser.internal.views.ItemFigure;
import org.elbe.relations.models.IBrowserItem;
import org.elbe.relations.models.IRelation;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/** Controller for the items displayed on the relations pane.
 *
 * @author Benno Luthiger */
@SuppressWarnings("restriction")
public class ItemEditPart extends AbstractGraphicalEditPart implements NodeEditPart, IBrowserItem {
    private ItemAdapter model;

    @Inject
    private Logger log;

    @Inject
    private EHandlerService handlerService;

    @Inject
    private CommandManager commandManager;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private UISynchronize sync;

    /** Factory method to create an instance of <code>ItemEditPart</code>.
     *
     * @param model {@link ItemAdapter}
     * @param context {@link IEclipseContext}
     * @return {@link ItemEditPart} */
    public static ItemEditPart createItemEditPart(final ItemAdapter model, final IEclipseContext context) {
        final ItemEditPart editPart = ContextInjectionFactory.make(ItemEditPart.class, context);
        editPart.model = model;
        return editPart;
    }

    /** @return Object of type <code>ItemAdapter</code>.
     * @see org.eclipse.gef.EditPart#getModel()
     * @see ItemAdapter */
    @Override
    public Object getModel() {
        return this.model;
    }

    /** @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure() */
    @Override
    protected IFigure createFigure() {
        String title = ""; //$NON-NLS-1$
        try {
            title = this.model.getTitle();
        } catch (final VException exc) {
            this.log.error(exc, exc.getMessage());
        }
        final IFigure figure = new ItemFigure(title, this.model.getImage());
        figure.addMouseMotionListener(new MouseMotionListener.Stub() {
            @Override
            public void mouseEntered(final MouseEvent mouseEvent) {
                final RelationsRootEditPart root = (RelationsRootEditPart) getRoot();
                if (isCenter()) {
                    return;
                }
                final ItemFigure itemFigure = (ItemFigure) mouseEvent.getSource();
                if (mouseEvent.getState() == SWT.CONTROL) {
                    itemFigure.setClickable(true);
                }
                root.setMouseOverTarget(itemFigure);
            }

            @Override
            public void mouseExited(final MouseEvent mouseEvent) {
                final RelationsRootEditPart root = (RelationsRootEditPart) getRoot();
                root.resetMouseOverTarget();
                if (isCenter()) {
                    return;
                }
                final ItemFigure itemFigure = (ItemFigure) mouseEvent.getSource();
                itemFigure.setClickable(false);
            }
        });
        figure.addMouseListener(new MouseListener.Stub() {
            // Ctrl-Click to center item
            @Override
            public void mousePressed(final MouseEvent mouseEvent) {
                final ItemFigure itemFigure = (ItemFigure) mouseEvent.getSource();
                if (itemFigure.isClickable()) {
                    // first, we have to make sure that the item is selected
                    ItemEditPart.this.eventBroker
                    .send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
                            new SelectedItemChangeEvent(ItemEditPart.this.model, null));
                    if (mouseEvent.button == 1) {
                        ItemEditPart.this.handlerService.executeHandler(new ParameterizedCommand(
                                ItemEditPart.this.commandManager.getCommand(ICommandIds.CMD_ITEM_CENTER), null));
                    }
                    mouseEvent.consume();
                }
            }

            @Override
            public void mouseDoubleClicked(final MouseEvent mouseEvent) {
                // first, we have to make sure that the item is selected
                ItemEditPart.this.eventBroker.send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
                        ItemEditPart.this.model);

                ItemEditPart.this.handlerService.executeHandler(new ParameterizedCommand(
                        ItemEditPart.this.commandManager.getCommand(ICommandIds.CMD_ITEM_EDIT), null));
            }
        });

        return figure;
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ItemDragPolicy());
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
                new SelectionEditPolicy() {
            @Override
            protected void showSelection() {
                ItemEditPart.this.sync.syncExec(() -> ((ItemFigure) getHostFigure()).changeColor(true));
            }

            @Override
            protected void hideSelection() {
                ItemEditPart.this.sync.syncExec(() -> ((ItemFigure) getHostFigure()).changeColor(false));
            }
        });
    }

    @Override
    protected List<IRelation> getModelSourceConnections() {
        return this.model.getSources();
    }

    @Override
    protected List<IRelation> getModelTargetConnections() {
        return this.model.getTargets();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(
            final ConnectionEditPart inConnection) {
        return new ChopboxAnchor(getFigure());
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(
            final ConnectionEditPart inConnection) {
        return new ChopboxAnchor(getFigure());
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(final Request inRequest) {
        return new ChopboxAnchor(getFigure());
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(final Request inRequest) {
        return new ChopboxAnchor(getFigure());
    }

    /** @return boolean <code>true</code> if this ItemEditPart is at the center. */
    public boolean isCenter() {
        return ((RelationsEditPart) getParent()).getModelID().equals(
                this.model.getUniqueID());
    }

    /** Refreshes this part's view with the specified content.
     *
     * @param inTitle String */
    public void refreshView(final String inTitle) {
        ((ItemFigure) getFigure()).setTitel(inTitle);
    }

}
