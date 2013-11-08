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
package org.elbe.relations.defaultbrowser.internal.views;

import javax.inject.Inject;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.defaultbrowser.internal.controller.ItemEditPart;
import org.elbe.relations.defaultbrowser.internal.controller.RelationEditPart;
import org.elbe.relations.defaultbrowser.internal.controller.RelationsEditPart;
import org.elbe.relations.defaultbrowser.internal.controller.RelationsRootEditPart;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.IRelation;
import org.elbe.relations.models.ItemAdapter;

/**
 * Functionality for configuring the GraphicalViewer
 * 
 * @author Benno Luthiger Created on 17.12.2005
 */
@SuppressWarnings("restriction")
public class GraphicalViewerCreator {
	private RelationsRootEditPart rootPart = null;

	@Inject
	private IEclipseContext context;

	/**
	 * Creates the viewer for the relations pane.
	 * 
	 * @param inParent
	 *            Composite
	 * @return GraphicalViewer
	 */
	public GraphicalViewer createViewer(final Composite inParent) {
		final GraphicalViewer outViewer = new ScrollingGraphicalViewer();
		outViewer.createControl(inParent);

		// configure the viewer
		outViewer.getControl().setBackground(ColorConstants.lightGray);
		rootPart = new RelationsRootEditPart();
		outViewer.setRootEditPart(rootPart);
		final KeyHandler lKeyHandler = new GraphicalViewerKeyHandler(outViewer);
		lKeyHandler.put(KeyStroke.getPressed(SWT.CONTROL, SWT.NONE),
				new Action() {
					@Override
					public void run() {
						rootPart.makeMousOverPartClickable(true);
					}
				});
		lKeyHandler.put(KeyStroke.getReleased(SWT.CONTROL, SWT.CONTROL),
				new Action() {
					@Override
					public void run() {
						rootPart.makeMousOverPartClickable(false);
					}
				});
		outViewer.setKeyHandler(lKeyHandler);

		outViewer.setEditPartFactory(getEditPartFactory());

		return outViewer;
	}

	private EditPartFactory getEditPartFactory() {
		return new EditPartFactory() {
			@Override
			public EditPart createEditPart(final EditPart inContext,
					final Object inModel) {
				if (inModel instanceof CentralAssociationsModel) {
					return new RelationsEditPart(
							(CentralAssociationsModel) inModel);
				} else if (inModel instanceof ItemAdapter) {
					return ItemEditPart.createItemEditPart(
							(ItemAdapter) inModel, context);
				} else if (inModel instanceof IRelation) {
					return new RelationEditPart((IRelation) inModel);
				}
				return null;
			}
		};
	}

}
