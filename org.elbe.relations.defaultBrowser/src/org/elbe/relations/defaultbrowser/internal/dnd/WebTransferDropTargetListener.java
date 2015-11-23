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

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.elbe.relations.dnd.DropDataHelper;
import org.elbe.relations.dnd.DropDataHelper.IDropHandler;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;

/**
 * TransferDropTargetListener to enable GEF drag'n drop of web pages on
 * ItemFigures.
 * 
 * @author Luthiger Created on 14.11.2009
 */
@SuppressWarnings("restriction")
public class WebTransferDropTargetListener extends AbstractTransferDropTargetListener {
	private EditPart editPart;

	@Inject
	private Logger log;

	@Inject
	private IEclipseContext context;

	@Inject
	private IBrowserManager browserManager;

	/**
	 * WebTransferDropTargetListener constructor, must not called by clients
	 * directly!
	 * 
	 * @param inViewer
	 *            EditPartViewer
	 * @param inTransfer
	 *            Transfer
	 */
	public WebTransferDropTargetListener(final EditPartViewer inViewer, final Transfer inTransfer) {
		super(inViewer, inTransfer);
	}

	/**
	 * Factory method to create instances of
	 * <code>WebTransferDropTargetListener</code>.
	 * 
	 * @param inViewer
	 *            {@link EditPartViewer}
	 * @param inTransfer
	 *            {@link Transfer}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * 
	 * @param inViewer
	 * @param inTransfer
	 * @param inContext
	 * @return {@link WebTransferDropTargetListener}
	 */
	public static WebTransferDropTargetListener create(final EditPartViewer inViewer, final Transfer inTransfer,
			final IEclipseContext inContext) {
		final WebTransferDropTargetListener out = new WebTransferDropTargetListener(inViewer, inTransfer);
		ContextInjectionFactory.inject(out, inContext);
		return out;
	}

	@Override
	protected void handleDragOver() {
		getCurrentEvent().detail = DND.DROP_COPY;
		super.handleDragOver();
	}

	@Override
	protected void handleDrop() {
		if (this.editPart != null) {
			final IDropHandler lHandler = DropDataHelper.TransferDropHandler.URL_TRANSFER.getHandler();
			try {
				lHandler.handleDrop(getCurrentEvent().data,
						DropHelper.getModel((ItemAdapter) this.editPart.getModel(), this.browserManager, this.context),
						this.context);
			} catch (final Exception exc) {
				this.log.error(exc, exc.getMessage());
			}
		}
		super.handleDrop();
	}

	@Override
	protected void updateTargetRequest() {
		// nothing to do
	}

	@Override
	protected void handleEnteredEditPart() {
		super.handleEnteredEditPart();
		this.editPart = getTargetEditPart();
	}

	@Override
	protected void handleExitingEditPart() {
		super.handleExitingEditPart();
		this.editPart = getTargetEditPart();
	}

}
