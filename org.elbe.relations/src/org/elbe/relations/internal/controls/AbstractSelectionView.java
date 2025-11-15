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
package org.elbe.relations.internal.controls;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.dnd.ItemTransfer;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.models.ItemAdapter;
import org.hip.kernel.bom.AlternativeModel;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/** Base class for all selection lists providing general functionality to select items.
 *
 * @author Luthiger */
public abstract class AbstractSelectionView implements IPartWithSelection {
    private final TableViewer viewer;

    @Inject // NOSONAR
    private Logger log;

    // @Inject // NOSONAR
    private final EHandlerService handlerService;

    // @Inject // NOSONAR
    private final ECommandService commandService;

    @Inject
    protected AbstractSelectionView(final Composite parent, final LanguageService languageService,
            final ESelectionService selectionService, final EHandlerService handlerService,
            final ECommandService commandService) {
        this.viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        this.handlerService = handlerService;
        this.commandService = commandService;

        this.viewer.setContentProvider(new ObservableListContentProvider<Object>());
        this.viewer.setComparator(new ViewerComparator(languageService.getComparator()));
        this.viewer.addSelectionChangedListener(
                event -> selectionService.setSelection(((IStructuredSelection) event
                        .getSelection()).getFirstElement()));

        hookDoubleClickAction(this.viewer);
        hookDragnDrop(this.viewer);
    }

    @Inject
    public void init(final EMenuService service) {
        service.registerContextMenu(this.viewer.getControl(), getPopupID());
        this.viewer.setInput(getDBInput());
    }

    /** @return {@link WritableList} the data to be selected */
    protected abstract WritableList<AlternativeModel> getDBInput();

    /** @return String the id of the popup menu to display */
    protected abstract String getPopupID();

    @Focus
    public void onFocus() {
        final Table lTable = (Table) this.viewer.getControl();
        lTable.setFocus();
        if (this.viewer.getSelection().isEmpty()) {
            lTable.select(0);
        }
    }

    @Inject
    @Optional
    void updateView(
            @UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_RELOAD) final String event) {
        if (this.viewer != null) {
            final WritableList<AlternativeModel> input = getDBInput();
            if (!input.isEmpty()) {
                this.viewer.setInput(input);
            }
        }
    }

    @Inject
    @Optional
    void adjustView(@UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_DB) final String event) {
        if (this.viewer != null) {
            final WritableList<AlternativeModel> input = getDBInput();
            this.viewer.setInput(input);
        }
    }

    @Inject
    @Optional
    void initialize(
            @UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String event) {
        if (this.viewer != null) {
            final WritableList<AlternativeModel> input = getDBInput();
            if (!input.isEmpty()) {
                this.viewer.setInput(input);
            }
        }
    }

    @Inject
    @Optional
    void titleChanged(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT) final ItemAdapter item) {
        if (this.viewer == null || item == null) {
            return;
        }

        try {
            this.viewer.update(item.getLightWeight(),
                    new String[] { item.getTitle() });
        } catch (BOMException | VException exc) {
            this.log.error(exc, exc.getMessage());
        }

    }

    private void hookDragnDrop(final TableViewer viewer) {
        // make viewer a drag source
        final ItemTransfer itemTransfer = ItemTransfer.getInstance(this.log);
        final Transfer[] dragTypes = new Transfer[] { itemTransfer };
        viewer.addDragSupport(DND.DROP_COPY, dragTypes,
                new DragSourceAdapter() {
            @Override
            public void dragSetData(final DragSourceEvent event) {
                final IStructuredSelection selected = (IStructuredSelection) AbstractSelectionView.this.viewer
                        .getSelection();
                if (!selected.isEmpty()) {
                    final Object[] items = selected.toArray();
                    final UniqueID[] uniqueIDs = new UniqueID[items.length];
                    for (int i = 0; i < items.length; i++) {
                        final ILightWeightItem item = (ILightWeightItem) items[i];
                        uniqueIDs[i] = new UniqueID(item.getItemType(),
                                item.getID());
                    }
                    event.data = uniqueIDs;
                }
            }
        });

        // make viewer a drop target
        final Transfer[] dropTypes = new Transfer[] { itemTransfer };
        this.viewer.addDropSupport(DND.DROP_MOVE, dropTypes,
                new DropTargetAdapter() {
            @Override
            public void drop(final DropTargetEvent event) {
                AbstractSelectionView.this.handlerService.executeHandler(ParameterizedCommand.generateCommand(
                        AbstractSelectionView.this.commandService
                        .getCommand(ICommandIds.CMD_RELATION_REMOVE),
                        null));
            }
        });
    }

    private void hookDoubleClickAction(final TableViewer viewer) {
        viewer.addDoubleClickListener(
                event -> AbstractSelectionView.this.handlerService.executeHandler(ParameterizedCommand
                        .generateCommand(AbstractSelectionView.this.commandService
                                .getCommand(ICommandIds.CMD_ITEM_SHOW), null)));
    }

    /** @return boolean <code>true</code> if the component is filled and at least one element is selected */
    @Override
    public boolean hasSelection() {
        return !this.viewer.getSelection().isEmpty();
    }

}
