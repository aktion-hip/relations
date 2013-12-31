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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.IRelated;
import org.hip.kernel.exc.VException;

/**
 * Set of all models related to the selected and centered model.
 * 
 * @author Benno Luthiger
 */
@SuppressWarnings("restriction")
public class CentralAssociationsModel extends AbstractAssociationsModel
        implements IAssociationsModel {
	private Collection<IRelation> relations;

	@Inject
	private IEclipseContext context;

	@Inject
	Logger log;

	@Inject
	private IEventBroker eventBroker;

	/**
	 * Factory method to create an instance of
	 * <code>CentralAssociationsModel</code>.
	 * 
	 * @param inItem
	 *            {@link IItemModel}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link CentralAssociationsModel}
	 * @throws SQLException
	 * @throws VException
	 */
	public static CentralAssociationsModel createCentralAssociationsModel(
	        final IItemModel inItem, final IEclipseContext inContext)
	        throws VException, SQLException {
		final ItemAdapter lItem = new ItemAdapter(inItem, inContext);
		return createCentralAssociationsModel(lItem, inContext);
	}

	/**
	 * Factory method to create an instance of
	 * <code>CentralAssociationsModel</code>.
	 * 
	 * @param inItem
	 *            {@link ItemAdapter}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link CentralAssociationsModel}
	 * @throws SQLException
	 * @throws VException
	 */
	public static CentralAssociationsModel createCentralAssociationsModel(
	        final ItemAdapter inItem, final IEclipseContext inContext)
	        throws VException, SQLException {
		final CentralAssociationsModel outModel = ContextInjectionFactory.make(
		        CentralAssociationsModel.class, inContext);
		outModel.setFocusedItem(inItem);
		outModel.initialize(outModel.getFocusedItem());
		return outModel;
	}

	/**
	 * This method extends the super class implementation.
	 * 
	 * @param inItem
	 *            ItemAdapter the focus (i.e. central) item.
	 * @throws VException
	 * @throws SQLException
	 */
	@Override
	protected void initialize(final ItemAdapter inItem) throws VException,
	        SQLException {
		beforeInit();
		super.initialize(getFocusedItem());
		afterInit();
	}

	private void beforeInit() {
		getCenter().refresh();
		relations = new Vector<IRelation>();
	}

	@SuppressWarnings("rawtypes")
	private void afterInit() {
		final ItemAdapter lCenter = getCenter();

		// add created relations as source to the center item
		for (final Iterator lRelations = relations.iterator(); lRelations
		        .hasNext();) {
			lCenter.addSource((IRelation) lRelations.next());
		}

		// sort the list of related items because views like to display them in
		// sorted order
		Collections.sort(related);
	}

	@Override
	protected IRelation createRelation(final IItem inItem,
	        final ItemAdapter inSource) throws VException {
		// create and configure relation
		final IRelation outRelation = new RelationWrapper(
		        ((IRelated) inItem).getRelationID());
		outRelation.setSourceItem(inSource);
		outRelation.setTargetItem(inItem);
		relations.add(outRelation);
		return outRelation;
	}

	/**
	 * Returns a list containing both the center and the related items.
	 * 
	 * @return List<ItemAdapter> of ItemAdapter and IRelation
	 */
	public List<ItemAdapter> getAllItems() {
		final List<ItemAdapter> outList = new Vector<ItemAdapter>();
		outList.add(getFocusedItem());
		outList.addAll(related);
		return outList;
	}

	/**
	 * Returns the list containing the related items.
	 * 
	 * @return List<ItemAdapter> of ItemAdapter and IRelation
	 */
	public List<ItemAdapter> getRelatedItems() {
		return related;
	}

	/**
	 * Notify listeners about changes
	 * 
	 * @throws SQLException
	 * @throws VException
	 */
	@Override
	protected void afterSave() throws VException, SQLException {
		initialize(getFocusedItem());
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_CREATED,
		        getFocusedItem().getUniqueID());
	}

	/**
	 * @see IAssociationsModel#undoChanges()
	 */
	@Override
	public void undoChanges() throws BOMException {
		try {
			initialize(getFocusedItem());
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	/**
	 * Updates the model with the actual state in the DB table.
	 * 
	 * @throws BOMException
	 */
	public void refresh() throws BOMException {
		try {
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
	 * Returns/creates the associations model to the specified item.
	 * 
	 * @param inItem
	 *            ItemAdapter
	 * @return IAssociationsModel
	 * @throws VException
	 * @throws SQLException
	 */
	public IAssociationsModel getAssociationsModel(final ItemAdapter inItem)
	        throws VException, SQLException {
		if (getCenter().equals(inItem)) {
			return this;
		}
		return PeripheralAssociationsModel.createExternalAssociationsModel(
		        inItem, context);
	}

	@Override
	public int hashCode() {
		final int lPrime = 31;
		int outHash = super.hashCode();
		outHash = lPrime * outHash
		        + ((relations == null) ? 0 : relations.hashCode());
		return outHash;
	}

	@Override
	public boolean equals(final Object inObj) {
		if (this == inObj)
			return true;
		if (!super.equals(inObj))
			return false;
		if (getClass() != inObj.getClass())
			return false;
		final CentralAssociationsModel lOther = (CentralAssociationsModel) inObj;
		if (relations == null) {
			if (lOther.relations != null)
				return false;
		} else if (!relations.equals(lOther.relations))
			return false;
		return true;
	}

}
