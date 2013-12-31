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
package org.elbe.relations.internal.addons;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.elbe.relations.RelationsConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

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

	private final IWindowCloseHandler quitHandler = new IWindowCloseHandler() {
		@Override
		public boolean close(final MWindow inWindow) {
			final List<Object> lHelpWindows = modelService.findElements(app,
			        null, null,
			        Collections.singletonList(RelationsConstants.WINDOW_HELP));
			for (final Object lHelp : lHelpWindows) {
				final MWindow lHelpWindow = (MWindow) lHelp;
				lHelpWindow.setVisible(false);
				lHelpWindow.setOnTop(false);
				lHelpWindow.setToBeRendered(false);
				EcoreUtil.delete((EObject) lHelp, true);
			}
			return true;
		}
	};

	private final EventHandler eventHandler = new EventHandler() {

		@Override
		public void handleEvent(final Event inEvent) {
			if (!UIEvents.isSET(inEvent)) {
				return;
			}
			final Object lElement = inEvent
			        .getProperty(UIEvents.EventTags.ELEMENT);
			if (!(lElement instanceof MWindow)) {
				return;
			}
			final MWindow lWindow = (MWindow) lElement;
			if (RelationsConstants.RELATIONS_CONTRIBUTOR_URI.equals(lWindow
			        .getContributorURI())) {
				if (lWindow.equals(inEvent.getProperty("ChangedElement")) //$NON-NLS-1$ // NOPMD 
				        && lWindow.getContext() != null) {
					lWindow.getContext().runAndTrack(new RunAndTrack() {
						@Override
						public boolean changed(final IEclipseContext inContext) {
							final Object lHandler = inContext
							        .get(IWindowCloseHandler.class);
							if (!quitHandler.equals(lHandler)) {
								inContext.set(IWindowCloseHandler.class,
								        quitHandler);
							}
							return true;
						}
					});
				}
			}
		}
	};

	@Inject
	IEventBroker eventBroker;

	@PostConstruct
	void hookListeners() {
		eventBroker.subscribe(UIEvents.Context.TOPIC_CONTEXT, eventHandler);
	}

	@PreDestroy
	void unhookListeners() {
		eventBroker.unsubscribe(eventHandler);
	}
}