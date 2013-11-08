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
package org.elbe.relations.defaultbrowser.internal.dnd;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.defaultbrowser.internal.controller.ItemEditPart;
import org.elbe.relations.models.IBrowserItem;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.exc.VException;

/**
 * TransferDragSourceListener to enable GEF drag'n drop of ItemFigures.
 * 
 * @author Benno Luthiger Created on 13.08.2006
 */
@SuppressWarnings("restriction")
public class ItemTransferDragSourceListener extends
		AbstractTransferDragSourceListener {

	@Inject
	private Logger log;

	@Inject
	private IBrowserManager browserManager;

	/**
	 * ItemTransferDragSourceListener constructor, must not called by clients
	 * directly!
	 * 
	 * @param inViewer
	 *            EditPartViewer
	 * @param inTransfer
	 *            Transfer
	 */
	public ItemTransferDragSourceListener(final EditPartViewer inViewer,
			final Transfer inTransfer) {
		super(inViewer, inTransfer);
	}

	/**
	 * Factory method to create instances of
	 * <code>ItemTransferDragSourceListener</code>.
	 * 
	 * @param inViewer
	 *            {@link EditPartViewer}
	 * @param inTransfer
	 *            {@link Transfer}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link ItemTransferDragSourceListener}
	 */
	public static ItemTransferDragSourceListener create(
			final EditPartViewer inViewer, final Transfer inTransfer,
			final IEclipseContext inContext) {
		final ItemTransferDragSourceListener out = new ItemTransferDragSourceListener(
				inViewer, inTransfer);
		ContextInjectionFactory.inject(out, inContext);
		return out;
	}

	/**
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void dragSetData(final DragSourceEvent inEvent) {
		try {
			final List lSelection = getViewer().getSelectedEditParts();
			final UniqueID[] lIDs = new UniqueID[lSelection.size()];
			int lIndex = 0;
			for (final Iterator lParts = lSelection.iterator(); lParts
					.hasNext();) {
				final ItemAdapter lItem = (ItemAdapter) ((IBrowserItem) lParts
						.next()).getModel();
				lIDs[lIndex++] = new UniqueID(lItem.getItemType(),
						lItem.getID());
				;
			}
			inEvent.data = lIDs;
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
			inEvent.doit = false;
		}
	}

	/**
	 * Only start the drag if the item to drag is not in the center.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void dragStart(final DragSourceEvent inEvent) {
		super.dragStart(inEvent);
		try {
			final ItemAdapter lCenter = browserManager.getCenterModel()
					.getCenter();
			for (final Iterator lSelection = ((IStructuredSelection) getViewer()
					.getSelection()).iterator(); lSelection.hasNext();) {
				final ItemEditPart lPart = (ItemEditPart) lSelection.next();
				if (lCenter.equals(lPart.getModel())) {
					inEvent.doit = false;
					return;
				}
			}
		}
		catch (final Exception exc) {
			log.error(exc, exc.getMessage());
			inEvent.doit = false;
		}
	}

}
