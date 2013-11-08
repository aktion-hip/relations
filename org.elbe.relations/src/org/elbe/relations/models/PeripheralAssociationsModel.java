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

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.exc.VException;

/**
 * An associations model of an item that is not in the center of the
 * RelationsView.
 * 
 * @author Benno Luthiger
 */
@SuppressWarnings("restriction")
public class PeripheralAssociationsModel extends AbstractAssociationsModel
		implements IAssociationsModel {

	@Inject
	private Logger log;

	@Inject
	private IBrowserManager browserManager;

	/**
	 * Factory method to create an instance of
	 * <code>PeripheralAssociationsModel</code>.
	 * 
	 * @param inItem
	 *            {@link IItemModel}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link PeripheralAssociationsModel}
	 * @throws SQLException
	 * @throws VException
	 */
	public static PeripheralAssociationsModel createExternalAssociationsModel(
			final IItemModel inItem, final IEclipseContext inContext)
			throws VException, SQLException {
		final ItemAdapter lItem = new ItemAdapter(inItem, inContext);
		return createExternalAssociationsModel(lItem, inContext);
	}

	/**
	 * Factory method to create an instance of
	 * <code>ExternalAssociationsModel</code>.
	 * 
	 * @param inItem
	 *            {@link ItemAdapter}
	 * @return {@link PeripheralAssociationsModel}
	 * @throws SQLException
	 * @throws VException
	 */
	public static PeripheralAssociationsModel createExternalAssociationsModel(
			final ItemAdapter inItem, final IEclipseContext inContext)
			throws VException, SQLException {
		final PeripheralAssociationsModel outModel = ContextInjectionFactory
				.make(PeripheralAssociationsModel.class, inContext);
		outModel.setFocusedItem(inItem);
		outModel.initialize(outModel.getFocusedItem());
		return outModel;
	}

	protected void beforeInit() {
		// Nothing to do
	}

	void afterInit() {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.models.AbstractAssociationsModel#afterSave()
	 */
	@Override
	protected void afterSave() throws VException, SQLException {
		final CentralAssociationsModel lCenter = browserManager
				.getCenterModel();

		// Check if we deleted the association with the central model
		if (removed.contains(lCenter.getCenter().getUniqueID())) {
			// If yes, refresh the central model
			try {
				lCenter.refresh();
			}
			catch (final BOMException exc) {
				log.error(exc, exc.getMessage());
			}
		}
	}

}
