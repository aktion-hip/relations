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
package org.elbe.relations.defaultbrowser.internal.controller;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.SelectionEditPolicy;
import org.eclipse.swt.SWT;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.defaultbrowser.internal.dnd.ItemDragPolicy;
import org.elbe.relations.defaultbrowser.internal.views.ItemFigure;
import org.elbe.relations.models.IBrowserItem;
import org.elbe.relations.models.IRelation;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;

/**
 * Controller for the items displayed on the relations pane.
 * 
 * @author Benno Luthiger Created on 16.12.2005
 */
@SuppressWarnings("restriction")
public class ItemEditPart extends AbstractGraphicalEditPart implements
        NodeEditPart, IBrowserItem {
	private ItemAdapter model;

	@Inject
	private Logger log;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private CommandManager commandManager;

	@Inject
	private IEventBroker eventBroker;
	
	@Inject
	private UISynchronize sync;

	/**
	 * Factory method to create an instance of <code>ItemEditPart</code>.
	 * 
	 * @param inModel
	 *            {@link ItemAdapter}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link ItemEditPart}
	 */
	public static ItemEditPart createItemEditPart(final ItemAdapter inModel,
	        final IEclipseContext inContext) {
		final ItemEditPart out = ContextInjectionFactory.make(
		        ItemEditPart.class, inContext);
		out.model = inModel;
		return out;
	}

	/**
	 * @return Object of type <code>ItemAdapter</code>.
	 * @see org.eclipse.gef.EditPart#getModel()
	 * @see ItemAdapter
	 */
	@Override
	public Object getModel() {
		return model;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		String lTitle = ""; //$NON-NLS-1$
		try {
			lTitle = model.getTitle();
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		final IFigure outFigure = new ItemFigure(lTitle, model.getImage());
		outFigure.addMouseMotionListener(new MouseMotionListener.Stub() {
			@Override
			public void mouseEntered(final MouseEvent inMouseEvent) {
				final RelationsRootEditPart lRoot = (RelationsRootEditPart) getRoot();
				if (isCenter()) {
					return;
				}
				final ItemFigure lFigure = (ItemFigure) inMouseEvent
				        .getSource();
				if (inMouseEvent.getState() == SWT.CONTROL) {
					lFigure.setClickable(true);
				}
				lRoot.setMouseOverTarget(lFigure);
			}

			@Override
			public void mouseExited(final MouseEvent inMouseEvent) {
				final RelationsRootEditPart lRoot = (RelationsRootEditPart) getRoot();
				lRoot.resetMouseOverTarget();
				if (isCenter()) {
					return;
				}
				final ItemFigure lFigure = (ItemFigure) inMouseEvent
				        .getSource();
				lFigure.setClickable(false);
			}
		});
		outFigure.addMouseListener(new MouseListener.Stub() {
			// Ctrl-Click to center item
			@Override
			public void mousePressed(final MouseEvent inMouseEvent) {
				final ItemFigure lFigure = (ItemFigure) inMouseEvent
				        .getSource();
				if (lFigure.isClickable()) {
					// first, we have to make sure that the item is selected
					eventBroker
					        .send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
					                new SelectedItemChangeEvent(model, null));
					if (inMouseEvent.button == 1) {
						handlerService.executeHandler(new ParameterizedCommand(
						        commandManager
						                .getCommand(ICommandIds.CMD_ITEM_CENTER),
						        null));
					}
					inMouseEvent.consume();
				}
			}

			@Override
			public void mouseDoubleClicked(final MouseEvent inMouseEvent) {
				// first, we have to make sure that the item is selected
				eventBroker
				        .send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
				                model);

				handlerService.executeHandler(new ParameterizedCommand(
				        commandManager.getCommand(ICommandIds.CMD_ITEM_EDIT),
				        null));
			}
		});

		return outFigure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ItemDragPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
		        new SelectionEditPolicy() {
			        @Override
			        protected void showSelection() {
			        	sync.syncExec(new Runnable() {							
							@Override
							public void run() {
								((ItemFigure) getHostFigure()).changeColor(true);
							}
						});
			        }

			        @Override
			        protected void hideSelection() {
			        	sync.syncExec(new Runnable() {							
			        		@Override
			        		public void run() {
			        			((ItemFigure) getHostFigure()).changeColor(false);
			        		}
			        	});
			        }
		        });
	}

	@Override
	protected List<IRelation> getModelSourceConnections() {
		return model.getSources();
	}

	@Override
	protected List<IRelation> getModelTargetConnections() {
		return model.getTargets();
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
	        final ConnectionEditPart inConnection) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
	        final ConnectionEditPart inConnection) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(final Request inRequest) {
		return new ChopboxAnchor(getFigure());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(final Request inRequest) {
		return new ChopboxAnchor(getFigure());
	}

	/**
	 * @return boolean <code>true</code> if this ItemEditPart is at the center.
	 */
	public boolean isCenter() {
		return ((RelationsEditPart) getParent()).getModelID().equals(
		        model.getUniqueID());
	}

	/**
	 * Refreshes this part's view with the specified content.
	 * 
	 * @param inTitle
	 *            String
	 */
	public void refreshView(final String inTitle) {
		((ItemFigure) getFigure()).setTitel(inTitle);
	}

}
