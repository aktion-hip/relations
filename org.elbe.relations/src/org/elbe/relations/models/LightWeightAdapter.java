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

import java.util.List;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.utility.IItemVisitor;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IAction;
import org.elbe.relations.internal.services.IItemEditWizard;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.exc.VException;

/**
 * Adapter for ILightWeightItem objects, adapting <code>ILightWeightItem</code>
 * to <code>IItemModel</code> interface.
 * 
 * @author Luthiger Created on 01.10.2006
 */
@SuppressWarnings("restriction")
public class LightWeightAdapter implements IItemModel {
	private final ILightWeightItem item;

	private Image image = null;

	/**
	 * LightWeightAdapter constructor adapting a <code>ILightWeightItem</code>
	 * instance.
	 * 
	 * @param inItem
	 *            {@link ILightWeightItem} the adaptee
	 */
	public LightWeightAdapter(final ILightWeightItem inItem) {
		super();
		item = inItem;
	}

	/**
	 * LightWeightAdapter constructor adapting a <code>ILightWeightModel</code>
	 * instance.
	 * 
	 * @param inItem
	 *            {@link ILightWeightModel} the adaptee
	 */
	public LightWeightAdapter(final ILightWeightModel inItem) {
		super();
		item = inItem;
		image = inItem.getImage();
	}

	/**
	 * @see org.elbe.relations.bom.IItem#getCmdID()
	 */
	@Override
	public long getID() throws VException {
		return item.getID();
	}

	/**
	 * @see org.elbe.relations.bom.IItem#getItemType()
	 */
	@Override
	public int getItemType() {
		return item.getItemType();
	}

	/**
	 * @see org.elbe.relations.bom.IItem#getTitle()
	 */
	@Override
	public String getTitle() throws VException {
		return item.toString();
	}

	/**
	 * @see org.elbe.relations.bom.IItem#getImage()
	 */
	@Override
	public Image getImage() {
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.bom.IItem#visit(org.elbe.relations.utility.IItemVisitor
	 * )
	 */
	@Override
	public void visit(final IItemVisitor inVisitor) throws VException {
		// intentionally left empty
	}

	/**
	 * Intentionally returning null
	 * 
	 * @see org.elbe.relations.bom.IItem#getItemEditWizard()
	 */
	@Override
	public Class<? extends IItemEditWizard> getItemEditWizard() {
		return null;
	}

	public UniqueID getUniqueID() {
		return new UniqueID(getItemType(), item.getID());
	}

	/**
	 * @see org.elbe.relations.bom.IItem#getLightWeight()
	 */
	@Override
	public ILightWeightItem getLightWeight() throws BOMException {
		return item;
	}

	/**
	 * @return <code>true</code> if ID and type are equal.
	 */
	@Override
	public boolean equals(final Object inObject) {
		if (inObject == null)
			return false;
		if (inObject instanceof IItem) {
			final IItem lItem = (IItem) inObject;
			try {
				return getItemType() == lItem.getItemType()
						&& getID() == lItem.getID();
			}
			catch (final VException exc) {
				return false;
			}
		}
		return false;
	}

	/**
	 * @see org.elbe.relations.bom.IItem#saveTitleText(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void saveTitleText(final String inTitle, final String inText)
			throws BOMException {
		throw new BOMException(
				RelationsMessages.getString("LightWeightAdapter.exc.msg")); //$NON-NLS-1$
	}

	/**
	 * @see org.elbe.relations.bom.IItem#getItemDeleteAction()
	 */
	@Override
	public IAction getItemDeleteAction(final Logger inLog) {
		if (item instanceof IItemModel) {
			return ((IItemModel) item).getItemDeleteAction(inLog);

		}
		return null;
	}

	/**
	 * @see IItem#getCreated()
	 */
	@Override
	public String getCreated() throws VException {
		return item.getCreated();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.bom.IItem#accept(org.hip.kernel.bom.DomainObjectVisitor
	 * )
	 */
	@Override
	public void accept(final DomainObjectVisitor inVisitor) {
		// This class adapts LightWeightItem, therefore, nothing to use for a
		// XML visitor.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.models.IItemModel#addSource(org.elbe.relations.models
	 * .IRelation)
	 */
	@Override
	public void addSource(final IRelation inRelation) {
		// intentionally left empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.models.IItemModel#addTarget(org.elbe.relations.models
	 * .IRelation)
	 */
	@Override
	public void addTarget(final IRelation inRelation) {
		// intentionally left empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.models.IItemModel#getSources()
	 */
	@Override
	public List<IRelation> getSources() {
		// intentionally left empty
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.models.IItemModel#getTargets()
	 */
	@Override
	public List<IRelation> getTargets() {
		// intentionally left empty
		return null;
	}

}
