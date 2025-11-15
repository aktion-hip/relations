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
package org.elbe.relations.browser.finder.internal;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.services.IRelationsBrowser;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 * View to display the relations between the items for that they can be browsed.
 * The related items are placed in a finder style pane. Every item displayed can
 * be selected and, thus, be activated.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class FinderBrowserPart implements IRelationsBrowser {
    public static final String ID = "org.elbe.relations.finder.browser.part"; //$NON-NLS-1$

    private IEclipseContext context;
    private IBrowserManager browserManager;
    private CentralAssociationsModel model;
    private FinderPane.GalleryItemAdapter selected;

    private SashForm form;
    private FinderPane finderCenter;
    private FinderPane finderRelated;
    private FinderPane focusPane;

    private boolean visible;
    private boolean initialized = false;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private EHandlerService handlerService;

    @Inject
    private CommandManager commandManager;

    @Inject
    private Logger log;

    @PostConstruct
    void createControls(final Composite parent, final IEclipseContext context, final EMenuService service,
            final MApplication application, final IBrowserManager browserManager) {
        this.context = context;
        this.browserManager = browserManager;

        createForm(parent, service, application);

        this.visible = true;
        setModelToFinder(this.browserManager.getCenterModel());
        setFocus();
    }

    private void createForm(final Composite parent, final EMenuService service, final MApplication application) {
        this.form = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
        this.form.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        this.finderCenter = new FinderPane(this.form, service, application, new BrowserCallback(), this.context, false);
        this.finderRelated = new FinderPane(this.form, service, application, new BrowserCallback(), this.context, true);
        this.focusPane = this.finderCenter;
        this.form.setWeights(1, 3);
    }

    @Focus
    public void setFocus() {
        if (this.model == null) {
            return;
        }
        if (!this.initialized) {
            this.initialized = true;
            setModelToFinder(this.browserManager.getCenterModel());
            this.selected = this.finderCenter.getSelected(this.model.getCenter());
        }
        this.focusPane.setFocus();
    }

    @Inject
    @Optional
    @Override
    public void setModel(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL) final CentralAssociationsModel model) {
        setModelToFinder(model);
        setFocus();
    }

    private void setModelToFinder(final CentralAssociationsModel model) {
        if (!this.visible) {
            return;
        }
        this.model = model;
        if (model == null) {
            clearView();
        } else {
            try {
                this.finderCenter.update(this.model.getCenter());
                this.finderRelated.update(this.model.getRelatedItems());
            } catch (final VException exc) {
                this.log.error(exc, exc.getMessage());
            }
        }
    }

    private void clearView() {
        this.finderCenter.clear();
        this.finderRelated.clear();
        this.form.redraw();
    }

    @Override
    @Inject
    @Optional
    public void syncSelected(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_SELECTED) final SelectedItemChangeEvent event) {
        if (!this.visible) {
            return;
        }

        final ItemAdapter item = event.getItem();
        if (this.model == null || item == null) {
            clearView();
            return;
        }
        final FinderPane.GalleryItemAdapter selectedItem = this.finderCenter.getSelected(item);
        if (selectedItem == null) {
            this.selected = this.finderRelated.getSelected(item);
            this.focusPane = this.finderRelated;
        } else {
            this.selected = selectedItem;
            this.focusPane = this.finderCenter;
        }
        if (!event.checkSource(this)) {
            setFocus();
        }
    }

    @Inject
    @Optional
    void syncWithManager(@UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_RELOAD) final String event) {
        setModelToFinder(this.browserManager.getCenterModel());
        setFocus();
    }

    @Override
    @Inject
    @Optional
    public void syncContent(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT) final ItemAdapter item) {
        if (!this.visible) {
            return;
        }
        if (this.selected == null || this.selected.isDisposed()) {
            return;
        }
        try {
            this.selected.setText(item.getTitle());
        } catch (final VException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

    @Override
    @Inject
    @Optional
    public void trackFontSize(
            @UIEventTopic("org_elbe_relations_browser_finder_internal_FinderBrowserPart") final Font font) {
        this.finderCenter.setFont(font);
        this.finderRelated.setFont(font);
    }

    // --- private classes ---

    public interface IBrowserCallback {
        /** Move the focus to the other pane.
         *
         * @param pane {@link FinderPane} the pane having the focus and let's them go (e.g. after a tab key) */
        void focusPassOver(FinderPane pane);

        /** Request the focus from the other pane.
         *
         * @param pane {@link FinderPane} the pane that requests the focus (e.g. after a click event) */
        void focusRequest(FinderPane pane);

        /** Center the selected item, i.e. move the selected item from related pane to center pane and move the focus to
         * center pane too.
         *
         * @param pane {@link FinderPane} the related pane */
        void centerSelected(FinderPane pane);

        /** Move the selection focus to the specified item.
         *
         * @param item {@link ItemAdapter} the item to display as selected */
        void selectionChange(ItemAdapter item);

        /** Open the editor on the specified item.
         *
         * @param pane {@link FinderPane} */
        void editSelected(FinderPane pane);
    }

    private class BrowserCallback implements IBrowserCallback {
        @Override
        public void focusPassOver(final FinderPane pane) {
            if (pane != FinderBrowserPart.this.focusPane) {
                return;
            }
            FinderBrowserPart.this.focusPane = pane == FinderBrowserPart.this.finderCenter
                    ? FinderBrowserPart.this.finderRelated
                            : FinderBrowserPart.this.finderCenter;
            FinderBrowserPart.this.focusPane.setFocus();
        }

        @Override
        public void focusRequest(final FinderPane pane) {
            if (pane == FinderBrowserPart.this.focusPane) {
                return;
            }
            FinderBrowserPart.this.focusPane = pane;
            FinderBrowserPart.this.focusPane.setFocus();
        }

        @Override
        public void centerSelected(final FinderPane pane) {
            if (pane == FinderBrowserPart.this.finderCenter) {
                FinderBrowserPart.this.focusPane = FinderBrowserPart.this.finderRelated;
                FinderBrowserPart.this.focusPane.setFocus();
            } else {
                FinderBrowserPart.this.focusPane = FinderBrowserPart.this.finderCenter;
                FinderBrowserPart.this.handlerService.executeHandler(
                        new ParameterizedCommand(FinderBrowserPart.this.commandManager.getCommand(ICommandIds.CMD_ITEM_CENTER), null));
            }
        }

        @Override
        public void selectionChange(final ItemAdapter item) {
            final FinderPane lPreviousFocus = FinderBrowserPart.this.focusPane;
            FinderBrowserPart.this.eventBroker.send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
                    new SelectedItemChangeEvent(item, FinderBrowserPart.this));
            if (FinderBrowserPart.this.focusPane != lPreviousFocus) {
                FinderBrowserPart.this.focusPane.setFocusEnforced();
            }
        }

        @Override
        public void editSelected(final FinderPane pane) {
            FinderBrowserPart.this.eventBroker.send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
                    new SelectedItemChangeEvent(pane.getSelected().getRelationsItem(), FinderBrowserPart.this));
            FinderBrowserPart.this.handlerService.executeHandler(
                    new ParameterizedCommand(FinderBrowserPart.this.commandManager.getCommand(ICommandIds.CMD_ITEM_EDIT), null));
        }
    }

}