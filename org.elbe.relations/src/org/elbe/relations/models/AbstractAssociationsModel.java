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
package org.elbe.relations.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.utility.IItemVisitor;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.models.ItemWithIcon;
import org.elbe.relations.internal.utility.RelatedItemHelper;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.exc.VException;

/**
 * Abstract associations class providing generic functionality for associations
 * models.
 * 
 * @author Benno Luthiger Created on 09.05.2006
 * @see org.elbe.relations.models.IAssociationsModel
 */
@SuppressWarnings("restriction")
public abstract class AbstractAssociationsModel implements IAssociationsModel {
	private ItemAdapter focusItem;

	protected List<ItemAdapter> related;
	protected Collection<UniqueID> uniqueIDs;
	protected Collection<UniqueID> added;
	protected Collection<UniqueID> removed;

	@Inject
	private IDataService data;

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;

	/**
	 * Sets the item which is selected (i.e. has the focus).
	 * 
	 * @param inItem
	 *            {@link ItemAdapter}
	 */
	public void setFocusedItem(final ItemAdapter inItem) {
		focusItem = inItem;
	}

	protected ItemAdapter getFocusedItem() {
		return focusItem;
	}

	/**
	 * This method should be called in the constructor. Subclasses may extend or
	 * override.
	 * 
	 * @param inItem
	 *            ItemAdapter the focus (i.e. central) item.
	 * @throws VException
	 * @throws SQLException
	 */
	protected void initialize(final ItemAdapter inItem) throws VException,
			SQLException {
		related = new ArrayList<ItemAdapter>();
		uniqueIDs = new ArrayList<UniqueID>();

		// Add the item's ID for that the item is filtered.
		uniqueIDs.add(inItem.getUniqueID());
		added = new HashSet<UniqueID>();
		removed = new HashSet<UniqueID>();

		processResult(inItem, RelatedItemHelper.getRelatedTerms(inItem));
		processResult(inItem, RelatedItemHelper.getRelatedTexts(inItem));
		processResult(inItem, RelatedItemHelper.getRelatedPersons(inItem));
	}

	private void processResult(final ItemAdapter inSource,
			final Collection<ItemWithIcon> inItems) throws VException,
			SQLException {
		for (final ItemWithIcon lItemIcon : inItems) {
			final IItem lItem = lItemIcon.getItem();

			// create and configure relation
			final IRelation lRelation = createRelation(lItem, inSource);

			// create and configure (adapted) item
			final ItemAdapter lAdapted = new ItemAdapter(lItem,
					lItemIcon.getIcon(), context);
			lAdapted.addTarget(lRelation);

			related.add(lAdapted);
			uniqueIDs.add(new UniqueID(lItem.getItemType(), lItem.getID()));
		}
	}

	/**
	 * Creates and configures the relation. Subclasses may override.
	 * 
	 * @param inItem
	 *            {@link IItem}
	 * @param inSource
	 *            ItemAdapter
	 * @return IRelation
	 * @throws VException
	 */
	protected IRelation createRelation(final IItem inItem,
			final ItemAdapter inSource) throws VException {
		return null;
	}

	/**
	 * Hook for subclasses.
	 * 
	 */
	protected abstract void afterSave() throws VException, SQLException;

	/**
	 * Returns the associated items as array of IItem.
	 * 
	 * @return Object[] Array of <code>ItemAdapter</code>.
	 */
	@Override
	public Object[] getElements() {
		return related.toArray();
	}

	/**
	 * Filters the specified item against the associated items.
	 * 
	 * @param inItem
	 *            ILightWeightItem
	 * @return <code>true</code> if element is included in the filtered set, and
	 *         <code>false</code> if excluded
	 */
	@Override
	public boolean select(final ILightWeightItem inItem) {
		if (uniqueIDs.contains(new UniqueID(inItem.getItemType(), inItem
				.getID()))) {
			return false;
		}
		return true;
	}

	/**
	 * Add new associations.
	 * 
	 * @param inAssociations
	 *            Object[] Array of <code>ILightWeightItem</code>s.
	 */
	@Override
	public void addAssociations(final Object[] inAssociations) {
		for (int i = 0; i < inAssociations.length; i++) {
			final LightWeightAdapter lItem = new LightWeightAdapter(
					(ILightWeightModel) inAssociations[i]);
			final UniqueID lID = lItem.getUniqueID();
			related.add(new ItemAdapter(lItem, context));
			handleUniqueAdd(lID);
		}
	}

	/**
	 * Add new associations.
	 * 
	 * @param inAssociations
	 *            UniqueID[]
	 */
	@Override
	public void addAssociations(final UniqueID[] inAssociations) {
		try {
			for (int i = 0; i < inAssociations.length; i++) {
				final IItemModel lItem = data.retrieveItem(inAssociations[i]);
				related.add(new ItemAdapter(lItem, context));
				handleUniqueAdd(inAssociations[i]);
			}
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Add to unique IDs and checks whether the item to add has been removed
	 * before.
	 * 
	 * @param inID
	 *            UniqueID
	 */
	private void handleUniqueAdd(final UniqueID inID) {
		uniqueIDs.add(inID);
		if (removed.contains(inID)) {
			removed.remove(inID);
		} else {
			added.add(inID);
		}
	}

	/**
	 * Removes the specified associations.
	 * 
	 * @param inObjects
	 *            Object[]
	 */
	@Override
	public void removeAssociations(final Object[] inObjects) {
		for (int i = 0; i < inObjects.length; i++) {
			final ItemAdapter lItem = (ItemAdapter) inObjects[i];
			related.remove(lItem);
			handleUniqueRemove(lItem.getUniqueID());
		}
	}

	@Override
	public void removeAssociations(final UniqueID[] inAssociations) {
		try {
			for (int i = 0; i < inAssociations.length; i++) {
				final IItemModel lItem = data.retrieveItem(inAssociations[i]);
				related.remove(new ItemAdapter(lItem, context));
				handleUniqueRemove(inAssociations[i]);
			}
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Removes the specified relation from this model.
	 * 
	 * @param inRelation
	 *            IRelation
	 */
	@Override
	public void removeRelation(final IRelation inRelation) {
		final ItemAdapter lItem = new ItemAdapter(inRelation.getTargetItem(),
				null, context);
		related.remove(lItem);
		handleUniqueRemove(lItem.getUniqueID());

		// save the change in the database
		try {
			BOMHelper.getRelationHome().deleteRelation(
					inRelation.getRelationID());
			afterSave();
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Removes from unique IDs and checks whether the item to remove has been
	 * added before.
	 * 
	 * @param inID
	 *            UniqueID
	 */
	private void handleUniqueRemove(final UniqueID inID) {
		uniqueIDs.remove(inID);
		if (added.contains(inID)) {
			added.remove(inID);
		} else {
			removed.add(inID);
		}
	}

	/**
	 * Store changes made during display of dialog.
	 * 
	 * @throws BOMException
	 */
	@Override
	public void saveChanges() throws BOMException {
		final RelationHome lHome = BOMHelper.getRelationHome();
		// first add: process added
		for (final UniqueID lID : added) {
			lHome.newRelation(focusItem, new UniqueIDWrapper(lID));
		}

		// then remove: process removed
		final int lType = focusItem.getItemType();
		long lItemID;
		try {
			lItemID = focusItem.getID();
			for (final UniqueID lID : removed) {
				lHome.deleteRelation(lType, lItemID, lID.itemType, lID.itemID);
			}
			afterSave();
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * @see IAssociationsModel#undoChanges()
	 */
	@Override
	public void undoChanges() throws BOMException {
		// intentionally left empty, subclasses may override.
	}

	/**
	 * @return ItemAdapter the central item.
	 */
	public ItemAdapter getCenter() {
		return focusItem;
	}

	/**
	 * Checks whether the specified ids exist already in this item's
	 * associations.
	 * 
	 * @param inIDs
	 *            UniqueID[]
	 * @return boolean <code>true</code> if all ids are associated,
	 *         <code>false</code> if at least one item is not associated yet.
	 */
	@Override
	public boolean isAssociated(final UniqueID[] inIDs) {
		for (int i = 0; i < inIDs.length; i++) {
			if (!isAssociated(inIDs[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether the specified id exists already in this item's
	 * associations.
	 * 
	 * @param inID
	 *            UniqueID
	 * @return boolean <code>true</code> if the specified ID is an association.
	 */
	@Override
	public boolean isAssociated(final UniqueID inID) {
		return uniqueIDs.contains(inID);
	}

	// --- private classes ---

	class UniqueIDWrapper implements IItem {
		private final UniqueID id;

		public UniqueIDWrapper(final UniqueID inID) {
			id = inID;
		}

		@Override
		public long getID() throws VException {
			return id.itemID;
		}

		@Override
		public int getItemType() {
			return id.itemType;
		}

		@Override
		public String getTitle() throws VException {
			return ""; //$NON-NLS-1$
		}

		@Override
		public void visit(final IItemVisitor inVisitor) throws VException {
			// intentionally left empty
		}

		@Override
		public ILightWeightItem getLightWeight() throws BOMException {
			return null;
		}

		@Override
		public void saveTitleText(final String inTitle, final String inText)
				throws BOMException {
			throw new BOMException(
					RelationsMessages
							.getString("AbstractAssociationsModel.error.msg")); //$NON-NLS-1$
		}

		@Override
		public String getCreated() throws VException {
			return null;
		}

		@Override
		public void accept(final DomainObjectVisitor inVisitor) {
			// intentionally left empty
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int lPrime = 31;
		int outResult = 1;
		outResult = lPrime * outResult
				+ ((focusItem == null) ? 0 : focusItem.hashCode());
		outResult = lPrime * outResult
				+ ((related == null) ? 0 : related.hashCode());
		return outResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object inObj) {
		if (this == inObj) {
			return true;
		}
		if (inObj == null) {
			return false;
		}
		if (getClass() != inObj.getClass()) {
			return false;
		}
		final AbstractAssociationsModel lOther = (AbstractAssociationsModel) inObj;
		if (focusItem == null) {
			if (lOther.focusItem != null) {
				return false;
			}
		} else if (!focusItem.equals(lOther.focusItem)) {
			return false;
		}
		if (related == null) {
			if (lOther.related != null) {
				return false;
			}
		} else if (!related.equals(lOther.related)) {
			return false;
		}
		return true;
	}

}
