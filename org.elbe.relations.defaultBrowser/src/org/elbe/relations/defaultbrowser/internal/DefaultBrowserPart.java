/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SelectionManager;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.services.IRelationsBrowser;
import org.elbe.relations.utility.BrowserPopupStateController;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 * View to display the relations between the items for that they can be browsed.
 * The related items are placed in circles around the focus item. Every item
 * displayed can be selected and, thus, be activated.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class DefaultBrowserPart implements IRelationsBrowser {
    private static final String INITIAL_SIZE = "dft.browser.initial.size"; //$NON-NLS-1$
    private static final String SIZE_SEP = "/"; //$NON-NLS-1$
    private static final org.eclipse.swt.graphics.Point NO_SIZE = new org.eclipse.swt.graphics.Point(0, 0);

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private Logger log;

    @Inject
    private UISynchronize sync;

    private IEclipseContext context;
    private MApplication application;

    private ItemEditPart selectedObject;

    private GraphicalViewer viewer;
    private final EditDomain editDomain = new EditDomain();

    private CentralAssociationsModel model;
    private IBrowserManager browserManager;
    private boolean visible;
    private boolean selectionChangeHandling = false;
    private org.eclipse.swt.graphics.Point initialSize;
    private org.eclipse.swt.graphics.Point oldSize;

    @PostConstruct
    void createControls(final Composite parent, final IEclipseContext context, final EMenuService service,
            final MApplication application,
            @Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = INITIAL_SIZE) final String initialSize,
            final IBrowserManager browserManager) {
        this.context = context;
        this.application = application;
        this.browserManager = browserManager;

        createGraphicalViewer(parent, context);
        service.registerContextMenu(this.viewer.getControl(), Constants.BROWSER_POPUP);

        this.visible = true;
        this.initialSize = getInitialSize(initialSize);
        setModel(this.browserManager.getCenterModel());
    }

    /**
     * Workaround: load persisted value from preferences (instead of
     * MPart.getPersistedState()).
     */
    private org.eclipse.swt.graphics.Point getInitialSize(final String initial) {
        if (initial == null || initial.isEmpty()) {
            return Constants.DEFAULT_SIZE;
        }
        final String[] size = initial.split(SIZE_SEP);
        return size.length == 2
                ? new org.eclipse.swt.graphics.Point(Integer.parseInt(size[0]), Integer.parseInt(size[1]))
                        : Constants.DEFAULT_SIZE;
    }

    /**
     * Workaround: we have to store the value to the preferences because we
     * can't load persisted values of an MPart in case of a fragment.
     */
    @PersistState
    void saveSize(@Preference(nodePath = RelationsConstants.PREFERENCE_NODE) final IEclipsePreferences preferences) {
        final org.eclipse.swt.graphics.Point size = this.viewer.getControl().getSize();
        preferences.put(INITIAL_SIZE, String.format("%s%s%s", size.x, SIZE_SEP, size.y)); //$NON-NLS-1$
    }

    private void createGraphicalViewer(final Composite parent, final IEclipseContext context) {
        final GraphicalViewerCreator viewerCreator = ContextInjectionFactory.make(GraphicalViewerCreator.class,
                context);
        this.viewer = viewerCreator.createViewer(parent);
        this.editDomain.addViewer(this.viewer);

        this.viewer.addSelectionChangedListener(new PartSelectionChangedListener());

        final Control control = this.viewer.getControl();
        control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(final MouseEvent event) {
                // we need this to allow double click on ItemEditPart
                DefaultBrowserPart.this.viewer.setRouteEventsToEditDomain(false);

                if (event.button == 3) {
                    final EditPart part = DefaultBrowserPart.this.viewer.findObjectAt(new Point(event.x, event.y));
                    if (part instanceof RelationEditPart) {
                        BrowserPopupStateController.setState(BrowserPopupStateController.State.CONNECTION, DefaultBrowserPart.this.application);
                    } else if (part instanceof ItemEditPart) {
                        // item's popup is handled in RelationsBrowserManager
                    } else {
                        BrowserPopupStateController.setState(BrowserPopupStateController.State.DISABLED, DefaultBrowserPart.this.application);
                    }
                }
            }
        });
        control.addListener(SWT.Resize, event -> {
            final org.eclipse.swt.graphics.Point newSize = control.getSize();
            if (DefaultBrowserPart.this.oldSize != null) {
                recenter(new PrecisionPoint((newSize.x - DefaultBrowserPart.this.oldSize.x) / 2,
                        (newSize.y - DefaultBrowserPart.this.oldSize.y) / 2));
            }
            DefaultBrowserPart.this.oldSize = newSize;
        });

        this.viewer.addDropTargetListener(
                (org.eclipse.jface.util.TransferDropTargetListener) ItemTransferDropTargetListener.create(this.viewer,
                        ItemTransfer.getInstance(this.log), this.context));
        this.viewer.addDropTargetListener(
                (org.eclipse.jface.util.TransferDropTargetListener) FileTransferDropTargetListener.create(this.viewer,
                        FileTransfer.getInstance(), this.context));
        this.viewer
        .addDropTargetListener((org.eclipse.jface.util.TransferDropTargetListener) WebTransferDropTargetListener
                .create(this.viewer, URLTransfer.getInstance(), this.context));
        this.viewer.addDragSourceListener(
                (org.eclipse.jface.util.TransferDragSourceListener) ItemTransferDragSourceListener.create(this.viewer,
                        ItemTransfer.getInstance(this.log), this.context));
    }

    @Override
    @Inject
    @Optional
    public void setModel(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL) final CentralAssociationsModel model) {
        if (!this.visible) {
            return;
        }
        if (preCondition(this.model, model)) {
            this.model = model;
            updateAfteModelChange(model);
        }
        if (this.model == null) {
            return;
        }
        reveal(this.model.getCenter());
    }

    private boolean preCondition(final CentralAssociationsModel model1, final CentralAssociationsModel model2) {
        if (model1 == null) {
            return model2 != null;
        }
        return !model1.equals(model2);
    }

    private void updateAfteModelChange(final CentralAssociationsModel model) {
        if (this.viewer == null) {
            return;
        }
        this.sync.syncExec(() -> DefaultBrowserPart.this.viewer.setContents(model));
        if (model == null) {
            return;
        }
        setSelectedDefault(model);
    }

    public void reveal(final Object object) {
        final EditPart editPart = this.viewer.getEditPartRegistry().get(object);
        if (editPart != null) {
            // arrangement of children is controlled here
            setAroundCenter();
            this.viewer.reveal(editPart);
        }
    }

    private void setSelectedDefault(final CentralAssociationsModel model) {
        final SelectionManager selectionManager = this.viewer.getSelectionManager();
        if (model == null) {
            selectionManager.setSelection(StructuredSelection.EMPTY);
        } else {
            this.selectedObject = (ItemEditPart) this.viewer.getEditPartRegistry().get(model.getCenter());
            selectionManager.setSelection(new StructuredSelection(this.selectedObject));
        }
    }

    @Override
    @Inject
    @Optional
    public void syncSelected(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_SELECTED) final SelectedItemChangeEvent event) {
        // leave, if this browser is the source of the event
        if (event.checkSource(this)) {
            return;
        }

        final ItemAdapter item = event.getItem();
        if (!this.visible) {
            return;
        }
        if (this.model == null || item == null) {
            setSelectedDefault(this.model);
            return;
        }
        final Object selected = this.viewer.getEditPartRegistry().get(item);
        setFocus();
        if (this.selectedObject == selected) {
            return;
        }
        if (selected == null) {
            return;
        }

        this.selectionChangeHandling = true;
        this.selectedObject = (ItemEditPart) selected;
        this.viewer.getSelectionManager().setSelection(new StructuredSelection(this.selectedObject));
    }

    protected boolean isSelectionChangeHandling() {
        return this.selectionChangeHandling;
    }

    protected void endSelectionChangeHandling() {
        this.selectionChangeHandling = false;
    }

    @Focus
    public void setFocus() {
        if (this.model == null) {
            return;
        }
        if (this.viewer != null) {
            this.viewer.getControl().setFocus();
        }
    }

    @Inject
    @Optional
    void syncWithManager(@UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_RELOAD) final String inEvent) {
        setModel(this.browserManager.getCenterModel());
    }

    @Override
    @Inject
    @Optional
    public void syncContent(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT) final ItemAdapter item) {
        if (!this.visible) {
            return;
        }

        try {
            this.selectedObject.refreshView(item.getTitle());
        } catch (final VException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

    @Inject
    @Optional
    public void clear(
            @EventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_CLEAR) final IEclipseContext context) {
        this.model = null;
        updateAfteModelChange(null);
    }

    @Override
    @Inject
    @Optional
    public void trackFontSize(
            @UIEventTopic("org_elbe_relations_defaultbrowser_internal_DefaultBrowserPart") final Font font) {
        final Map<Object, EditPart> registry = this.viewer.getEditPartRegistry();
        for (final EditPart part : registry.values()) {
            if (part instanceof final AbstractGraphicalEditPart editPart) {
                final IFigure figure = editPart.getFigure();
                if (figure instanceof final ItemFigure itemFigure) {
                    itemFigure.setFont(font);
                }
            }
        }
    }

    // --- helper methods to display the child figures centered in the browser window ---

    /**
     * In this relations browser, we want the related items to be displayed in
     * concentric circles around the center of the browser window.
     */
    private void setAroundCenter() {
        final Map<Object, EditPart> registry = this.viewer.getEditPartRegistry();
        final org.eclipse.swt.graphics.Point size = getSize();
        final PrecisionPoint lTranslate = new PrecisionPoint(size.x / 2 - RelationsConstants.ITEM_WIDTH / 2,
                size.y / 2 - RelationsConstants.ITEM_HEIGHT);
        moveFigure(registry, this.model.getCenter(), new PrecisionPoint(0, 0), lTranslate);

        final List<ItemAdapter> related = this.model.getRelatedItems();
        int lNumber = related.size();
        int lCount = 0;
        int lOffset = 0;
        final ItemPositionCalculator lCalculator = new ItemPositionCalculator(RelationsConstants.ITEM_WIDTH,
                RelationsConstants.ITEM_HEIGHT, getRadius(++lCount), lNumber);
        while (lCalculator.hasMore()) {
            lOffset = setPositions(registry, lCalculator.getPositions(), lOffset, related, lTranslate);
            lNumber -= lCalculator.getCount();
            lCalculator.recalculate(getRadius(++lCount), lNumber);
        }
        setPositions(registry, lCalculator.getPositions(), lOffset, related, lTranslate);
        this.oldSize = size;
    }

    private org.eclipse.swt.graphics.Point getSize() {
        Control lControl = this.viewer.getControl();
        if (!lControl.isFocusControl()) {
            return this.initialSize;
        }
        org.eclipse.swt.graphics.Point outSize;
        while (NO_SIZE.equals(outSize = lControl.getSize())) {
            lControl = lControl.getParent();
        }
        return outSize;
    }

    private int getRadius(final int count) {
        return RelationsConstants.RADIUS * count;
    }

    private int setPositions(final Map<Object, EditPart> registry, final List<PrecisionPoint> positions,
            final int inOffset, final List<ItemAdapter> related, final PrecisionPoint translate) {
        int outOffset = inOffset;
        for (final PrecisionPoint position : positions) {
            moveFigure(registry, related.get(outOffset), position, translate);
            ++outOffset;
        }
        return outOffset;
    }

    private void moveFigure(final Map<Object, EditPart> registry, final ItemAdapter model,
            final PrecisionPoint from, final PrecisionPoint translate) {
        final EditPart editPart = registry.get(model);
        if (editPart != null && editPart instanceof final AbstractGraphicalEditPart part) {
            part.getFigure().setLocation(from.getTranslated(translate));
        }
    }

    /**
     * Re-center the edit parts after a resize of the pane.
     */
    private void recenter(final PrecisionPoint translate) {
        final Map<Object, EditPart> registry = this.viewer.getEditPartRegistry();
        for (final EditPart editPart : registry.values()) {
            if (editPart instanceof final AbstractGraphicalEditPart part) {
                final Point from = part.getFigure().getBounds().getLocation();
                part.getFigure().setLocation(from.getTranslated(translate));
            }
        }

    }

    // --- private classes ---

    private class PartSelectionChangedListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(final SelectionChangedEvent event) {
            if (isSelectionChangeHandling()) {
                endSelectionChangeHandling();
                return;
            }
            if (event.getSelection().isEmpty()) {
                return;
            }

            // prevent multi selection by deselecting all selected items except
            // the last
            final SelectionManager manager = DefaultBrowserPart.this.viewer.getSelectionManager();
            final Object[] selections = ((IStructuredSelection) event.getSelection()).toArray();
            for (int i = 0; i < selections.length - 1; i++) {
                if (selections[i] instanceof final EditPart selection) {
                    manager.deselect(selection);
                }
            }

            final Object selection = selections[selections.length - 1];
            if (selection instanceof final ItemEditPart selected && DefaultBrowserPart.this.model != null) {
                DefaultBrowserPart.this.selectedObject = selected;
                DefaultBrowserPart.this.eventBroker.post(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
                        new SelectedItemChangeEvent((ItemAdapter) DefaultBrowserPart.this.selectedObject.getModel(),
                                DefaultBrowserPart.this));
            }
            if (selection instanceof final RelationEditPart editPart) {
                DefaultBrowserPart.this.eventBroker.post(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
                        editPart.getModel());
            }

            if (selection instanceof RelationsEditPart) {
                // this ensures that clicking the background doesn't deselect
                // the selected object
                manager.setSelection(new StructuredSelection(DefaultBrowserPart.this.selectedObject));
            }
        }
    }

}
