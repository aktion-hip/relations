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
package org.elbe.relations.defaultbrowser.internal;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SelectionManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.defaultbrowser.Constants;
import org.elbe.relations.defaultbrowser.internal.controller.ItemEditPart;
import org.elbe.relations.defaultbrowser.internal.controller.ItemPositionCalculator;
import org.elbe.relations.defaultbrowser.internal.controller.RelationEditPart;
import org.elbe.relations.defaultbrowser.internal.controller.RelationsEditPart;
import org.elbe.relations.defaultbrowser.internal.dnd.FileTransferDropTargetListener;
import org.elbe.relations.defaultbrowser.internal.dnd.ItemTransferDragSourceListener;
import org.elbe.relations.defaultbrowser.internal.dnd.ItemTransferDropTargetListener;
import org.elbe.relations.defaultbrowser.internal.dnd.WebTransferDropTargetListener;
import org.elbe.relations.defaultbrowser.internal.views.GraphicalViewerCreator;
import org.elbe.relations.defaultbrowser.internal.views.ItemFigure;
import org.elbe.relations.dnd.ItemTransfer;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.services.IRelationsBrowser;
import org.elbe.relations.utility.BrowserPopupStateController;
import org.elbe.relations.utility.BrowserPopupStateController.State;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;

/**
 * View to display the relations between the items for that they can be browsed.
 * The related items are placed in circles around the focus item. Every item
 * displayed can be selected and, thus, be activated.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class DefaultBrowserPart implements IRelationsBrowser {
	public static final String ID = "org.elbe.relations.defaultbrowser.part"; //$NON-NLS-1$

	private IEclipseContext context;
	private MApplication application;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private Logger log;

	private static final org.eclipse.swt.graphics.Point NO_SIZE = new org.eclipse.swt.graphics.Point(
			0, 0);

	private ItemEditPart selectedObject;

	private GraphicalViewer viewer;
	private final EditDomain editDomain = new EditDomain();

	private CentralAssociationsModel model;
	private IBrowserManager browserManager;
	private boolean visible;
	private boolean selectionChangeHandling = false;

	@PostConstruct
	void createControls(final Composite inParent,
			final IEclipseContext inContext, final EMenuService inService,
			final MApplication inApplication,
			final IBrowserManager inBrowserManager) {
		context = inContext;
		application = inApplication;
		browserManager = inBrowserManager;

		createGraphicalViewer(inParent, inContext);
		inService.registerContextMenu(viewer.getControl(),
				Constants.BROWSER_POPUP);

		visible = true;
		setModel(browserManager.getCenterModel());
	}

	private void createGraphicalViewer(final Composite inParent,
			final IEclipseContext inContext) {
		final GraphicalViewerCreator lViewerCreator = ContextInjectionFactory
				.make(GraphicalViewerCreator.class, inContext);
		viewer = lViewerCreator.createViewer(inParent);
		editDomain.addViewer(viewer);

		viewer.addSelectionChangedListener(new PartSelectionChangedListener());
		viewer.getControl().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent inEvent) {
				// we need this to allow double click on ItemEditPart
				viewer.setRouteEventsToEditDomain(false);

				if (inEvent.button == 3) {
					final EditPart lPart = viewer.findObjectAt(new Point(
							inEvent.x, inEvent.y));
					if (lPart instanceof RelationEditPart) {
						BrowserPopupStateController.setState(State.CONNECTION,
								application);
					} else if (lPart instanceof ItemEditPart) {
						// item's popup is handled in RelationsBrowserManager
					} else {
						BrowserPopupStateController.setState(State.DISABLED,
								application);
					}
				}
			}
		});
		viewer.addDropTargetListener(ItemTransferDropTargetListener.create(
				viewer, ItemTransfer.getInstance(log), context));
		viewer.addDropTargetListener(FileTransferDropTargetListener.create(
				viewer, FileTransfer.getInstance(), context));
		viewer.addDropTargetListener(WebTransferDropTargetListener.create(
				viewer, URLTransfer.getInstance(), context));
		viewer.addDragSourceListener(ItemTransferDragSourceListener.create(
				viewer, ItemTransfer.getInstance(log), context));
	}

	@Override
	@Inject
	@Optional
	public void setModel(
			@UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL) final CentralAssociationsModel inModel) {
		if (!visible) {
			return;
		}
		setInput(inModel);
		if (model == null) {
			return;
		}
		reveal(model.getCenter());
	}

	private void setInput(final CentralAssociationsModel inModel) {
		model = inModel;
		updateAfteModelChange();
	}

	public void reveal(final Object inObject) {
		final EditPart lEditPart = (EditPart) viewer.getEditPartRegistry().get(
				inObject);
		if (lEditPart != null) {
			// arrangement of children is controlled here
			setAroundCenter();
			viewer.reveal(lEditPart);
		}
	}

	private void updateAfteModelChange() {
		if (viewer == null) {
			return;
		}
		viewer.setContents(model);
		if (model == null)
			return;

		setSelectedDefault();
	}

	private void setSelectedDefault() {
		final SelectionManager lSelectionManager = viewer.getSelectionManager();
		if (model == null) {
			lSelectionManager.setSelection(StructuredSelection.EMPTY);
		} else {
			selectedObject = (ItemEditPart) viewer.getEditPartRegistry().get(
					model.getCenter());
			lSelectionManager.setSelection(new StructuredSelection(
					selectedObject));
		}
	}

	@Override
	@Inject
	@Optional
	public void syncSelected(
			@EventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_SELECTED) final SelectedItemChangeEvent inEvent) {
		// leave, if this browser is the source of the event
		if (inEvent.checkSource(this)) {
			return;
		}

		final ItemAdapter lItem = inEvent.getItem();
		if (!visible) {
			return;
		}
		if (model == null || lItem == null) {
			setSelectedDefault();
			return;
		}
		final Object lSelected = viewer.getEditPartRegistry().get(lItem);
		setFocus();
		if (selectedObject == lSelected) {
			return;
		}
		if (lSelected == null) {
			return;
		}

		selectionChangeHandling = true;
		selectedObject = (ItemEditPart) lSelected;
		viewer.getSelectionManager().setSelection(
				new StructuredSelection(selectedObject));
	}

	protected boolean isSelectionChangeHandling() {
		return selectionChangeHandling;
	}

	protected void endSelectionChangeHandling() {
		selectionChangeHandling = false;
	}

	@Focus
	public void setFocus() {
		if (viewer != null) {			
			viewer.getControl().setFocus();
		}
	}

	@Inject
	@Optional
	void syncWithManager(
			@UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_RELOAD) final String inEvent) {
		setModel(browserManager.getCenterModel());
	}

	@Override
	@Inject
	@Optional
	public void syncContent(
			@UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT) final ItemAdapter inItem) {
		if (!visible) {
			return;
		}

		try {
			selectedObject.refreshView(inItem.getTitle());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	@Inject
	@Optional
	public void close(
			@EventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_CLOSE) final IEclipseContext inContext) {
		// TODO
	}

	@Inject
	@Optional
	public void clear(
			@EventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_CLEAR) final IEclipseContext inContext) {
		setInput(null);
	}

	@Override
	@Inject
	@Optional
	public void trackFontSize(
			@EventTopic("org_elbe_relations_defaultbrowser_internal_DefaultBrowserPart") final int inFontSize) {
		if (inFontSize != 0) {
			Font lFont = null;
			final Map<IItemModel, GraphicalEditPart> lRegistry = viewer
					.getEditPartRegistry();
			for (final GraphicalEditPart lPart : lRegistry.values()) {
				final IFigure lFigure = lPart.getFigure();
				if (lFigure instanceof ItemFigure) {
					final ItemFigure lItemFigure = (ItemFigure) lFigure;
					if (lFont == null) {
						lFont = lItemFigure.getFont();
						final FontData lData = lFont.getFontData()[0];
						lData.setHeight(inFontSize);
						lFont = new Font(Display.getCurrent(), lData);
					}
					lItemFigure.setFont(lFont);
				}
			}
		}
	}

	private void postCenterItemID() {
		// TODO: UniqueID
		eventBroker.post(
				RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SEND_CENTER_ID,
				model.getCenter().getUniqueID());
	}

	private void postCenterItemModel() {
		// TODO: CentralAssociationsModel
		eventBroker.post(
				RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SEND_CENTER_MODEL,
				model);
	}

	// --- helper methods to display the child figures centered in the browser
	// window ---

	/**
	 * In this relations browser, we want the related items to be displayed in
	 * concentric circles around the center of the browser window.
	 */
	@SuppressWarnings("unchecked")
	private void setAroundCenter() {
		final Map<IItemModel, GraphicalEditPart> lRegistry = viewer
				.getEditPartRegistry();
		final org.eclipse.swt.graphics.Point lSize = getSize();
		final Point lTranslate = new Point(lSize.x / 2
				- (RelationsConstants.ITEM_WIDTH / 2), (lSize.y / 2)
				- RelationsConstants.ITEM_HEIGHT);
		moveFigure(lRegistry, model.getCenter(), new Point(0, 0), lTranslate);

		final List<ItemAdapter> lRelated = model.getRelatedItems();
		int lNumber = lRelated.size();
		int lCount = 0;
		int lOffset = 0;
		final ItemPositionCalculator lCalculator = new ItemPositionCalculator(
				RelationsConstants.ITEM_WIDTH, RelationsConstants.ITEM_HEIGHT,
				getRadius(++lCount), lNumber);
		while (lCalculator.hasMore()) {
			lOffset = setPositions(lRegistry, lCalculator.getPositions(),
					lOffset, lRelated, lTranslate);
			lNumber -= lCalculator.getCount();
			lCalculator.recalculate(getRadius(++lCount), lNumber);
		}
		setPositions(lRegistry, lCalculator.getPositions(), lOffset, lRelated,
				lTranslate);

	}

	private org.eclipse.swt.graphics.Point getSize() {
		Control lControl = viewer.getControl();
		org.eclipse.swt.graphics.Point outSize;
		while (NO_SIZE.equals(outSize = lControl.getSize())) {
			lControl = lControl.getParent();
		}
		return outSize;
	}

	private int getRadius(final int inCount) {
		return RelationsConstants.RADIUS * inCount;
	}

	private int setPositions(
			final Map<IItemModel, GraphicalEditPart> inRegistry,
			final List<Point> inPositions, final int inOffset,
			final List<ItemAdapter> inRelated, final Point inTranslate) {
		int outOffset = inOffset;
		for (final Point lPoint : inPositions) {
			moveFigure(inRegistry, inRelated.get(outOffset), lPoint,
					inTranslate);
			++outOffset;
		}
		return outOffset;
	}

	private void moveFigure(
			final Map<IItemModel, GraphicalEditPart> inRegistry,
			final ItemAdapter inModel, final Point inFrom,
			final Point inTranslate) {
		final GraphicalEditPart lEditPart = inRegistry.get(inModel);
		if (lEditPart != null) {
			lEditPart.getFigure()
					.setLocation(inFrom.getTranslated(inTranslate));
		}
	}

	// --- private classes ---

	private class PartSelectionChangedListener implements
			ISelectionChangedListener {
		@Override
		public void selectionChanged(final SelectionChangedEvent inEvent) {
			if (isSelectionChangeHandling()) {
				endSelectionChangeHandling();
				return;
			}
			if (inEvent.getSelection().isEmpty()) {
				return;
			}

			// prevent multi selection by deselecting all selected items except
			// the last
			final SelectionManager lManager = viewer.getSelectionManager();
			final Object[] lSelections = ((IStructuredSelection) inEvent
					.getSelection()).toArray();
			for (int i = 0; i < lSelections.length - 1; i++) {
				if (lSelections[i] instanceof EditPart) {
					lManager.deselect((EditPart) lSelections[i]);
				}
			}

			final Object lSelection = lSelections[lSelections.length - 1];
			if (lSelection instanceof ItemEditPart) {
				if (model != null) {
					selectedObject = (ItemEditPart) lSelection;
					eventBroker
							.post(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
									new SelectedItemChangeEvent(
											(ItemAdapter) selectedObject
													.getModel(),
											DefaultBrowserPart.this));
				}
			}
			if (lSelection instanceof RelationEditPart) {
				eventBroker
						.post(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
								((RelationEditPart) lSelection).getModel());
			}

			if (lSelection instanceof RelationsEditPart) {
				// this ensures that clicking the background doesn't deselect
				// the selected object
				lManager.setSelection(new StructuredSelection(selectedObject));
			}
		}
	}

}
