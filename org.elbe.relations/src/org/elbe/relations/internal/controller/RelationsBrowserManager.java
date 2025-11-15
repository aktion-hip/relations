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
package org.elbe.relations.internal.controller;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.IRelation;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.utility.BrowserPopupStateController;
import org.elbe.relations.utility.BrowserPopupStateController.State;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/**
 * Manager for the Relations browser settings.
 * <p>
 * An instance of this object can be injected into clients (as
 * <code>IBrowserManager</code>), thus giving them the possibility to access
 * information about the actual browser state (i.e. the selected item etc.).
 * </p>
 * <p>
 * Note: Because instances of this class get a <code>IDataService</code>
 * injected, they have to be created <b>after</b> the creation of a
 * <code>IDataService</code> instance.
 * </p>
 *
 * @author Luthiger
 * @see org.elbe.relations.services.IRelationsBrowser
 */
@SuppressWarnings("restriction")
public class RelationsBrowserManager implements IBrowserManager {
    private CentralAssociationsModel model;
    private ItemAdapter selected;
    private IRelation selectedRelation;
    private final Deque<UniqueID> historyBack;
    private final Deque<UniqueID> historyNext;
    private MApplication application;

    @Inject
    private IDataService data;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private IEclipseContext context;

    @Inject
    private Logger log;

    public RelationsBrowserManager() {
        super();
        this.historyBack = new ArrayDeque<>();
        this.historyNext = new ArrayDeque<>();
    }

    private void addToHistory(final ItemAdapter model) {
        final UniqueID id = model.getUniqueID();
        if (this.historyBack.isEmpty() || !id.equals(this.historyBack.peek())) {
            this.historyBack.push(id);
        }
    }

    /**
     * For lazy initialization
     */
    private MApplication getApplication() {
        if (this.application == null) {
            this.application = this.context.get(MApplication.class);
        }
        return this.application;
    }

    /**
     * Returns the browsers' central model.
     *
     * @return {@link CentralAssociationsModel}
     */
    @Override
    public CentralAssociationsModel getCenterModel() {
        return this.model;
    }

    /**
     * Returns the model actually selected in the browsers.
     *
     * @return {@link ItemAdapter}
     */
    @Override
    public ItemAdapter getSelectedModel() {
        return this.selected;
    }

    /**
     * Sets the model to this manager.
     *
     * @param model
     *            {@link CentralAssociationsModel} may be <code>null</code>
     */
    @Override
    public void setModel(final CentralAssociationsModel model) {
        if (this.model != null) {
            addToHistory(this.model.getCenter());
        }
        this.model = model;
        handleDBChange();
    }

    @Inject
    @Optional
    public void dbInitialized(
            @EventTopic(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String inMsg) {
        this.model = null;
        this.selected = null;
        handleDBChanged();
    }

    @Inject
    @Optional
    public void dbChanged(
            @EventTopic(RelationsConstants.TOPIC_DB_CHANGED_CREATED) final UniqueID inUniqueID) {
        if (inUniqueID == null) {
            this.model = null;
            this.selected = null;
        }
        handleDBChanged();
    }

    private void handleDBChanged() {
        this.eventBroker.post(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_CLEAR,
                "clear"); //$NON-NLS-1$
        handleDBChange();
        this.historyBack.clear();
        this.historyNext.clear();
    }

    private void handleDBChange() {
        if (this.model == null) {
            this.selected = null;
        } else {
            this.selected = this.model.getCenter();
        }
        this.eventBroker
        .post(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL,
                this.model);
    }

    @Inject
    @Optional
    public void itemChanged(@EventTopic(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_MODEL) final IItemModel item) {
        try {
            setModel(CentralAssociationsModel.createCentralAssociationsModel(item, this.context));
        }
        catch (VException | SQLException exc) {
            this.log.error(exc, exc.getMessage());
        }

    }

    /** Notifies the manager that the selection changed to the specified item.
     *
     * @param event {@link ItemAdapter} */
    @Inject
    @Optional
    public void setSelected(
            @EventTopic(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED) final SelectedItemChangeEvent event) {
        this.selected = event.getItem();
        this.selectedRelation = null;
        checkSelected();
        syncBrowsersForSelected(event);
    }

    /** Notifies the manager that the selection changed to the specified relation.
     *
     * @param selectedRelation {@link IRelation} */
    @Inject
    @Optional
    public void setSelected(
            @EventTopic(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED) final IRelation selectedRelation) {
        this.selectedRelation = selectedRelation;
    }

    /**
     * Handling of the popup menu's item enablement.
     */
    private void checkSelected() {
        if (this.model == null) {
            return;
        }

        if (this.model.getCenter().equals(this.selected)) {
            BrowserPopupStateController.setState(State.ITEM_CENTER, getApplication());
        } else {
            BrowserPopupStateController.setState(State.ITEM_PERIPHERY, getApplication());
        }
    }

    /** Synchronizes all browsers for the selected item. Note: This is needed if more then one relations browser is
     * viewable.
     *
     * @param event {@link SelectedItemChangeEvent} */
    private void syncBrowsersForSelected(final SelectedItemChangeEvent event) {
        this.eventBroker.post(
                RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_SELECTED, event);
    }

    /**
     * @return IRelation the relation actually selected or <code>null</code>
     */
    @Override
    public IRelation getSelectedRelation() {
        return this.selectedRelation;
    }

    /** Checks whether the browsers have to be refreshed after an item has been deleted.
     *
     * @param item IItemModel the deleted item
     * @throws VException */
    @Override
    public void checkAfterDeletion(final IItemModel item) throws VException {
        final UniqueID id = new UniqueID(item.getItemType(), item.getID());
        // deletion of center model?
        final CentralAssociationsModel centerModel = getCenterModel();
        final ItemAdapter selectedModel = getSelectedModel();
        if (centerModel != null && selectedModel != null) {
            if (centerModel.getCenter().getUniqueID().equals(id)) {
                setModel(null);
            }
            // deletion of selected model?
            else if (selectedModel.getUniqueID().equals(id)) {
                setModel(reloadCenter());
            }
            // deletion of related model?
            else {
                for (final ItemAdapter related : centerModel
                        .getRelatedItems()) {
                    if (related.getUniqueID().equals(id)) {
                        setModel(reloadCenter());
                    }
                }
            }
        }
    }

    /**
     * We have to reload the central model if one of the related items have been
     * deleted.
     *
     * @return CentralAssociationsModel the refreshed central model, may be
     *         <code>null</code>
     */
    private CentralAssociationsModel reloadCenter() {
        try {
            return CentralAssociationsModel.createCentralAssociationsModel(
                    this.model.getCenter(), this.context);
        }
        catch (VException | SQLException exc) {
            this.log.error(exc, exc.getMessage());
        }
        return null;
    }

    /** Save the browser state to the preferences.
     *
     * @param preferences {@link IEclipsePreferences} */
    public void saveState(final IEclipsePreferences preferences) {
        if (this.model == null) {
            preferences.put(RelationsConstants.CENTER_ITEM_ID, ""); //$NON-NLS-1$
            return;
        }

        final UniqueID uniqueId = this.model.getCenter().getUniqueID();
        if (uniqueId != null) {
            preferences.put(RelationsConstants.CENTER_ITEM_ID, uniqueId.toString());
        }
    }

    /** Restore the browser state from the preferences.
     *
     * @param preferences {@link IEclipsePreferences} */
    public void restoreState(final IEclipsePreferences preferences) {
        final String id = preferences.get(RelationsConstants.CENTER_ITEM_ID, ""); //$NON-NLS-1$
        if (!id.isEmpty()) {
            try {
                setModel(CentralAssociationsModel
                        .createCentralAssociationsModel(this.data.retrieveItem(new UniqueID(id)), this.context));
            }
            catch (VException | SQLException | BOMException exc) {
                this.log.error(exc, exc.getMessage());
            }
        }
    }

    @Override
    public boolean hasPrevious() {
        return !this.historyBack.isEmpty();
    }

    @Override
    public void moveBack() {
        this.historyNext.push(this.model.getCenter().getUniqueID());
        moveHistory(this.historyBack.pop());
        this.historyBack.pop();
    }

    @Override
    public boolean hasNext() {
        return !this.historyNext.isEmpty();
    }

    @Override
    public void moveForward() {
        moveHistory(this.historyNext.pop());
    }

    private void moveHistory(final UniqueID uniqueID) {
        try {
            setModel(CentralAssociationsModel.createCentralAssociationsModel(
                    this.data.retrieveItem(uniqueID), this.context));
        }
        catch (VException | SQLException | BOMException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

}
