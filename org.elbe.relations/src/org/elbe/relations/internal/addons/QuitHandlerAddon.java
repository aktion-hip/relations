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
package org.elbe.relations.internal.addons;

import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.elbe.relations.RelationsConstants;
import org.osgi.service.event.EventHandler;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 * Workaround to register the main windows' close handler.
 *
 * @author Luthiger
 */
public class QuitHandlerAddon {
    @Inject
    EModelService modelService;

    @Inject
    MApplication app;

    private final IWindowCloseHandler quitHandler = window -> {
        final List<Object> helpWindows = QuitHandlerAddon.this.modelService.findElements(QuitHandlerAddon.this.app,
                null, null, Collections.singletonList(RelationsConstants.WINDOW_HELP));
        for (final Object help : helpWindows) {
            final MWindow helpWindow = (MWindow) help;
            helpWindow.setVisible(false);
            helpWindow.setOnTop(false);
            helpWindow.setToBeRendered(false);
            EcoreUtil.delete((EObject) help, true);
        }
        return true;
    };

    private final EventHandler eventHandler = event -> {
        if (!UIEvents.isSET(event)) {
            return;
        }
        final Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (!(element instanceof MWindow)) {
            return;
        }
        final MWindow window = (MWindow) element;
        if (RelationsConstants.RELATIONS_CONTRIBUTOR_URI.equals(window.getContributorURI())) {
            if (window.equals(event.getProperty("ChangedElement")) //$NON-NLS-1$ // NOPMD
                    && window.getContext() != null) {
                window.getContext().runAndTrack(new RunAndTrack() {
                    @Override
                    public boolean changed(final IEclipseContext context) {
                        final Object handler = context.get(IWindowCloseHandler.class);
                        if (!QuitHandlerAddon.this.quitHandler.equals(handler)) {
                            context.set(IWindowCloseHandler.class, QuitHandlerAddon.this.quitHandler);
                        }
                        return true;
                    }
                });
            }
        }
    };

    @Inject
    IEventBroker eventBroker;

    @PostConstruct
    void hookListeners() {
        this.eventBroker.subscribe(UIEvents.Context.TOPIC_CONTEXT, this.eventHandler);
    }

    @PreSave
    void unhookListeners() {
        this.eventBroker.unsubscribe(this.eventHandler);
    }
}