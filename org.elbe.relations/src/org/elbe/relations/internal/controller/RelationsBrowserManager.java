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
import java.util.Stack;

import javax.inject.Inject;

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
	private final Stack<UniqueID> historyBack;
	private final Stack<UniqueID> historyNext;
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
		historyBack = new Stack<UniqueID>();
		historyNext = new Stack<UniqueID>();
	}

	private void addToHistory(final ItemAdapter inModel) {
		final UniqueID lID = inModel.getUniqueID();
		if (historyBack.empty() || !lID.equals(historyBack.peek())) {
			historyBack.push(lID);
		}
	}

	/**
	 * For lazy initialization
	 */
	private MApplication getApplication() {
		if (application == null) {
			application = context.get(MApplication.class);
		}
		return application;
	}

	/**
	 * Returns the browsers' central model.
	 * 
	 * @return {@link CentralAssociationsModel}
	 */
	@Override
	public CentralAssociationsModel getCenterModel() {
		return model;
	}

	/**
	 * Returns the model actually selected in the browsers.
	 * 
	 * @return {@link ItemAdapter}
	 */
	@Override
	public ItemAdapter getSelectedModel() {
		return selected;
	}

	/**
	 * Sets the model to this manager.
	 * 
	 * @param inModel
	 *            {@link CentralAssociationsModel}
	 */
	@Override
	public void setModel(final CentralAssociationsModel inModel) {
		if (model != null) {
			addToHistory(model.getCenter());
		}
		model = inModel;
		handleDBChange();
	}

	@Inject
	@Optional
	public void dbInitialized(
	        @EventTopic(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String inMsg) {
		model = null;
		selected = null;
		handleDBChanged();
	}

	@Inject
	@Optional
	public void dbChanged(
	        @EventTopic(RelationsConstants.TOPIC_DB_CHANGED_CREATED) final UniqueID inUniqueID) {
		if (inUniqueID == null) {
			model = null;
			selected = null;
		}
		handleDBChanged();
	}

	private void handleDBChanged() {
		eventBroker.post(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_CLEAR,
		        "clear"); //$NON-NLS-1$
		handleDBChange();
		historyBack.clear();
		historyNext.clear();
	}

	private void handleDBChange() {
		if (model == null) {
			selected = null;
		} else {
			selected = model.getCenter();
		}
		eventBroker
		        .post(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL,
		                model);
	}

	@Inject
	@Optional
	public void itemChanged(
	        @EventTopic(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_MODEL) final IItemModel inItem) {
		try {
			setModel(CentralAssociationsModel.createCentralAssociationsModel(
			        inItem, context));
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Notifies the manager that the selection changed to the specified item.
	 * 
	 * @param inEvent
	 *            {@link ItemAdapter}
	 */
	@Inject
	@Optional
	public void setSelected(
	        @EventTopic(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED) final SelectedItemChangeEvent inEvent) {
		selected = inEvent.getItem();
		selectedRelation = null;
		checkSelected();
		syncBrowsersForSelected(inEvent);
	}

	/**
	 * Notifies the manager that the selection changed to the specified
	 * relation.
	 * 
	 * @param inSelectedRelation
	 *            {@link IRelation}
	 */
	@Inject
	@Optional
	public void setSelected(
	        @EventTopic(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED) final IRelation inSelectedRelation) {
		selectedRelation = inSelectedRelation;
	}

	/**
	 * Handling of the popup menu's item enablement.
	 */
	private void checkSelected() {
		if (model == null) {
			return;
		}

		if (model.getCenter().equals(selected)) {
			BrowserPopupStateController.setState(State.ITEM_CENTER,
			        getApplication());
		} else {
			BrowserPopupStateController.setState(State.ITEM_PERIPHERY,
			        getApplication());
		}
	}

	/**
	 * Synchronizes all browsers for the selected item. Note: This is needed if
	 * more then one relations browser is viewable.
	 * 
	 * @param inEvent
	 *            {@link SelectedItemChangeEvent}
	 */
	private void syncBrowsersForSelected(final SelectedItemChangeEvent inEvent) {
		eventBroker.post(
		        RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_SELECTED,
		        inEvent);
	}

	/**
	 * @return IRelation the relation actually selected or <code>null</code>
	 */
	@Override
	public IRelation getSelectedRelation() {
		return selectedRelation;
	}

	/**
	 * Checks whether the browsers have to be refreshed after an item has been
	 * deleted.
	 * 
	 * @param inItem
	 *            IItemModel the deleted item
	 * @throws VException
	 */
	@Override
	public void checkAfterDeletion(final IItemModel inItem) throws VException {
		final UniqueID lID = new UniqueID(inItem.getItemType(), inItem.getID());
		// deletion of center model?
		if (getCenterModel().getCenter().getUniqueID().equals(lID)) {
			setModel(null);
		}
		// deletion of selected model?
		else if (getSelectedModel().getUniqueID().equals(lID)) {
			setModel(reloadCenter());
		}
		// deletion of related model?
		else {
			for (final ItemAdapter lRelated : getCenterModel()
			        .getRelatedItems()) {
				if (lRelated.getUniqueID().equals(lID)) {
					setModel(reloadCenter());
				}
			}
		}
	}

	/**
	 * We have to reload the central model if one of the related items have been
	 * deleted.
	 * 
	 * @return CentralAssociationsModel the refreshed central model
	 */
	private CentralAssociationsModel reloadCenter() {
		try {
			return CentralAssociationsModel.createCentralAssociationsModel(
			        model.getCenter(), context);
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
		return null;
	}

	/**
	 * Save the browser state to the preferences.
	 * 
	 * @param inPreferences
	 *            {@link IEclipsePreferences}
	 */
	public void saveState(final IEclipsePreferences inPreferences) {
		if (model == null) {
			inPreferences.put(RelationsConstants.CENTER_ITEM_ID, ""); //$NON-NLS-1$
			return;
		}

		final UniqueID lID = model.getCenter().getUniqueID();
		if (lID != null) {
			inPreferences
			        .put(RelationsConstants.CENTER_ITEM_ID, lID.toString());
		}
	}

	/**
	 * Restore the browser state from the preferences.
	 * 
	 * @param inPreferences
	 *            {@link IEclipsePreferences}
	 */
	public void restoreState(final IEclipsePreferences inPreferences) {
		final String lID = inPreferences.get(RelationsConstants.CENTER_ITEM_ID,
		        ""); //$NON-NLS-1$
		if (!lID.isEmpty()) {
			try {
				setModel(CentralAssociationsModel
				        .createCentralAssociationsModel(
				                data.retrieveItem(new UniqueID(lID)), context));
			}
			catch (final VException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final SQLException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final BOMException exc) {
				log.error(exc, exc.getMessage());
			}
		}
	}

	@Override
	public boolean hasPrevious() {
		return !historyBack.isEmpty();
	}

	@Override
	public void moveBack() {
		historyNext.push(model.getCenter().getUniqueID());
		moveHistory(historyBack.pop());
		historyBack.pop();
	}

	@Override
	public boolean hasNext() {
		return !historyNext.isEmpty();
	}

	@Override
	public void moveForward() {
		moveHistory(historyNext.pop());
	}

	private void moveHistory(final UniqueID inUniqueID) {
		try {
			setModel(CentralAssociationsModel.createCentralAssociationsModel(
			        data.retrieveItem(inUniqueID), context));
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
	}

}
