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
package org.elbe.relations.browser.finder.internal;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.services.IBrowserManager;
import org.elbe.relations.services.IRelationsBrowser;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;

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
	void createControls(final Composite inParent, final IEclipseContext inContext, final EMenuService inService,
			final MApplication inApplication, final IBrowserManager inBrowserManager) {
		this.context = inContext;
		this.browserManager = inBrowserManager;

		createForm(inParent, inService, inApplication);

		this.visible = true;
		setModelToFinder(this.browserManager.getCenterModel());
		setFocus();
	}

	private void createForm(final Composite inParent, final EMenuService inService, final MApplication inApplication) {
		this.form = new SashForm(inParent, SWT.HORIZONTAL | SWT.SMOOTH);
		this.finderCenter = new FinderPane(this.form, inService, inApplication, new BrowserCallback(), this.context,
				false);
		this.finderRelated = new FinderPane(this.form, inService, inApplication, new BrowserCallback(), this.context,
				true);
		this.focusPane = this.finderCenter;
		this.form.setWeights(new int[] { 1, 3 });
	}

	@Focus
	public void setFocus() {
		if (this.model == null) {
			return;
		}
		if (!this.initialized) {
			this.initialized = true;
			setModelToFinder(this.browserManager.getCenterModel());
			try {
				this.selected = this.finderCenter.getSelected(this.model.getCenter());
			} catch (final VException exc) {
				this.log.error(exc, exc.getMessage());
			}
		}
		this.focusPane.setFocus();
	}

	@Inject
	@Optional
	@Override
	public void setModel(
			@UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL) final CentralAssociationsModel inModel) {
		setModelToFinder(inModel);
		setFocus();
	}

	private void setModelToFinder(final CentralAssociationsModel inModel) {
		if (!this.visible) {
			return;
		}
		this.model = inModel;
		if (inModel == null) {
			clearView();
		} else {
			try {
				updatePanes();
			} catch (final VException exc) {
				this.log.error(exc, exc.getMessage());
			}
		}
	}

	private void updatePanes() throws VException {
		this.finderCenter.update(this.model.getCenter());
		this.finderRelated.update(this.model.getRelatedItems());
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
			@UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_SELECTED) final SelectedItemChangeEvent inEvent) {
		if (!this.visible) {
			return;
		}

		final ItemAdapter lItem = inEvent.getItem();
		if (this.model == null || lItem == null) {
			clearView();
			return;
		}
		try {
			final FinderPane.GalleryItemAdapter lSelectedItem = this.finderCenter.getSelected(lItem);
			if (lSelectedItem == null) {
				this.selected = this.finderRelated.getSelected(lItem);
				this.focusPane = this.finderRelated;
			} else {
				this.selected = lSelectedItem;
				this.focusPane = this.finderCenter;
			}
			if (!inEvent.checkSource(this)) {
				setFocus();
			}
		} catch (final VException exc) {
			this.log.error(exc, exc.getMessage());
		}
	}

	@Inject
	@Optional
	void syncWithManager(@UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_RELOAD) final String inEvent) {
		setModelToFinder(this.browserManager.getCenterModel());
		setFocus();
	}

	@Override
	@Inject
	@Optional
	public void syncContent(
			@UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT) final ItemAdapter inItem) {
		if (!this.visible) {
			return;
		}
		if (this.selected == null || this.selected.isDisposed()) {
			return;
		}
		try {
			this.selected.setText(inItem.getTitle());
		} catch (final VException exc) {
			this.log.error(exc, exc.getMessage());
		}
	}

	@Override
	@Inject
	@Optional
	public void trackFontSize(
			@UIEventTopic("org_elbe_relations_browser_finder_internal_FinderBrowserPart") final int inFontSize) {
		if (inFontSize != 0) {
			this.finderCenter.setFontSize(inFontSize);
			this.finderRelated.setFontSize(inFontSize);
		}
	}

	// --- private classes ---

	public interface IBrowserCallback {
		/**
		 * Move the focus to the other pane.
		 * 
		 * @param inPane
		 *            {@link FinderPane} the pane having the focus and let's
		 *            them go (e.g. after a tab key)
		 */
		void focusPassOver(FinderPane inPane);

		/**
		 * Request the focus from the other pane.
		 * 
		 * @param inPane
		 *            {@link FinderPane} the pane that requests the focus (e.g.
		 *            after a click event)
		 */
		void focusRequest(FinderPane inPane);

		/**
		 * Center the selected item, i.e. move the selected item from related
		 * pane to center pane and move the focus to center pane too.
		 * 
		 * @param inPane
		 *            {@link FinderPane} the related pane
		 */
		void centerSelected(FinderPane inPane);

		/**
		 * Move the selection focus to the specified item.
		 * 
		 * @param inItem
		 *            {@link ItemAdapter} the item to display as selected
		 */
		void selectionChange(ItemAdapter inItem);

		/**
		 * Open the editor on the specified item.
		 * 
		 * @param inPane
		 *            {@link FinderPane}
		 */
		void editSelected(FinderPane inPane);
	}

	private class BrowserCallback implements IBrowserCallback {
		@Override
		public void focusPassOver(final FinderPane inPane) {
			if (inPane != FinderBrowserPart.this.focusPane) {
				return;
			}
			FinderBrowserPart.this.focusPane = inPane == FinderBrowserPart.this.finderCenter
					? FinderBrowserPart.this.finderRelated : FinderBrowserPart.this.finderCenter;
			FinderBrowserPart.this.focusPane.setFocus();
		}

		@Override
		public void focusRequest(final FinderPane inPane) {
			if (inPane == FinderBrowserPart.this.focusPane) {
				return;
			}
			FinderBrowserPart.this.focusPane = inPane;
			FinderBrowserPart.this.focusPane.setFocus();
		}

		@Override
		public void centerSelected(final FinderPane inPane) {
			if (inPane == FinderBrowserPart.this.finderCenter) {
				FinderBrowserPart.this.focusPane = FinderBrowserPart.this.finderRelated;
				FinderBrowserPart.this.focusPane.setFocus();
			} else {
				FinderBrowserPart.this.focusPane = FinderBrowserPart.this.finderCenter;
				FinderBrowserPart.this.handlerService.executeHandler(new ParameterizedCommand(
						FinderBrowserPart.this.commandManager.getCommand(ICommandIds.CMD_ITEM_CENTER), null));
			}
		}

		@Override
		public void selectionChange(final ItemAdapter inItem) {
			final FinderPane lPreviousFocus = FinderBrowserPart.this.focusPane;
			FinderBrowserPart.this.eventBroker.send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
					new SelectedItemChangeEvent(inItem, FinderBrowserPart.this));
			if (FinderBrowserPart.this.focusPane != lPreviousFocus) {
				FinderBrowserPart.this.focusPane.setFocusEnforced();
			}
		}

		@Override
		public void editSelected(final FinderPane inPane) {
			FinderBrowserPart.this.eventBroker.send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED,
					new SelectedItemChangeEvent(inPane.getSelected().getRelationsItem(), FinderBrowserPart.this));
			FinderBrowserPart.this.handlerService.executeHandler(new ParameterizedCommand(
					FinderBrowserPart.this.commandManager.getCommand(ICommandIds.CMD_ITEM_EDIT), null));
		}
	}

}