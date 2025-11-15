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
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.IRelated;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/**
 * Set of all models related to the selected and centered model.
 *
 * @author Benno Luthiger
 */
@SuppressWarnings("restriction")
public class CentralAssociationsModel extends AbstractAssociationsModel implements IAssociationsModel {
    private Collection<IRelation> relations;

    @Inject
    private IEclipseContext context;

    @Inject
    Logger log;

    @Inject
    private IEventBroker eventBroker;

    /** Factory method to create an instance of <code>CentralAssociationsModel</code>.
     *
     * @param item {@link IItemModel}
     * @param context {@link IEclipseContext}
     * @return {@link CentralAssociationsModel}
     * @throws SQLException
     * @throws VException */
    public static CentralAssociationsModel createCentralAssociationsModel(final IItemModel item,
            final IEclipseContext context) throws VException, SQLException {
        final ItemAdapter adapted = new ItemAdapter(item, context);
        return createCentralAssociationsModel(adapted, context);
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
        final CentralAssociationsModel outModel = ContextInjectionFactory
                .make(CentralAssociationsModel.class, inContext);
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
    protected void initialize(final ItemAdapter inItem)
            throws VException, SQLException {
        beforeInit();
        super.initialize(getFocusedItem());
        afterInit();
    }

    private void beforeInit() {
        getCenter().refresh();
        this.relations = new ArrayList<>();
    }

    private void afterInit() {
        final ItemAdapter center = getCenter();

        // add created relations as source to the center item
        this.relations.forEach(center::addSource);

        // sort the list of related items because views like to display them in
        // sorted order
        Collections.sort(this.related);
    }

    @Override
    protected IRelation createRelation(final IItem item, final ItemAdapter source) throws VException {
        // create and configure relation
        final IRelation relation = new RelationWrapper(((IRelated) item).getRelationID());
        relation.setSourceItem(source);
        relation.setTargetItem(item);
        this.relations.add(relation);
        return relation;
    }

    /**
     * Returns a list containing both the center and the related items.
     *
     * @return List<ItemAdapter> of ItemAdapter and IRelation
     */
    public List<ItemAdapter> getAllItems() {
        final List<ItemAdapter> outList = new ArrayList<>();
        outList.add(getFocusedItem());
        outList.addAll(this.related);
        return outList;
    }

    /**
     * Returns the list containing the related items.
     *
     * @return List<ItemAdapter> of ItemAdapter and IRelation
     */
    public List<ItemAdapter> getRelatedItems() {
        return this.related;
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
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_CREATED,
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
        catch (VException | SQLException exc) {
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
        catch (VException | SQLException exc) {
            throw new BOMException(exc.getMessage());
        }

    }

    /** Returns/creates the associations model to the specified item.
     *
     * @param item ItemAdapter
     * @return IAssociationsModel
     * @throws VException
     * @throws SQLException */
    public IAssociationsModel getAssociationsModel(final ItemAdapter item)
            throws VException, SQLException {
        if (getCenter().equals(item)) {
            return this;
        }
        return PeripheralAssociationsModel.createExternalAssociationsModel(item, this.context);
    }

    @Override
    public int hashCode() {
        final int lPrime = 31;
        int outHash = super.hashCode();
        outHash = lPrime * outHash
                + (this.relations == null ? 0 : this.relations.hashCode());
        return outHash;
    }

    @Override
    public boolean equals(final Object inObj) {
        if (this == inObj) {
            return true;
        }
        if (!super.equals(inObj)) {
            return false;
        }
        if (getClass() != inObj.getClass()) {
            return false;
        }
        final CentralAssociationsModel lOther = (CentralAssociationsModel) inObj;
        if (this.relations == null) {
            if (lOther.relations != null) {
                return false;
            }
        } else if (!this.relations.equals(lOther.relations)) {
            return false;
        }
        return true;
    }

}
