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
package org.elbe.relations.internal.controls;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.dnd.ItemTransfer;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.models.ItemAdapter;
import org.hip.kernel.exc.VException;

/**
 * Base class for all selection lists providing general functionality to select
 * items.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractSelectionView implements IPartWithSelection {
	private final TableViewer viewer;

	@Inject
	private LanguageService languageService;

	@Inject
	private IDataService data;

	@Inject
	private ESelectionService selectionService;

	@Inject
	private Logger log;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private ECommandService commandService;

	@Inject
	public AbstractSelectionView(final Composite inParent) {
		viewer = new TableViewer(inParent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
	}

	@PostConstruct
	public void init(final EMenuService inService) {
		inService.registerContextMenu(viewer.getControl(), getPopupID());

		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setSorter(new ViewerSorter(languageService.getContentLanguage()));
		viewer.setInput(getDBInput());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent inEvent) {
				selectionService.setSelection(((IStructuredSelection) inEvent
						.getSelection()).getFirstElement());
			}
		});

		hookDoubleClickAction();
		hookDragnDrop();
	}

	/**
	 * @return {@link WritableList} the data to be selected
	 */
	abstract protected WritableList getDBInput();

	/**
	 * @return String the id of the popup menu to display
	 */
	abstract protected String getPopupID();

	protected IDataService getDataService() {
		return data;
	}

	@Focus
	public void onFocus() {
		final Table lTable = (Table) viewer.getControl();
		lTable.setFocus();
		if (viewer.getSelection().isEmpty()) {
			lTable.select(0);
		}
	}

	@Inject
	@Optional
	void updateView(
			@UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_RELOAD) final String inEvent) {
		viewer.setInput(getDBInput());
	}

	@Inject
	@Optional
	void initialize(
			@UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String inEvent) {
		viewer.setInput(getDBInput());
	}

	@Inject
	@Optional
	void titleChanged(
			@UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT) final ItemAdapter inItem) {
		if (viewer == null || inItem == null) {
			return;
		}

		try {
			viewer.update(inItem.getLightWeight(),
					new String[] { inItem.getTitle() });
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	private void hookDragnDrop() {
		// make viewer a drag source
		final ItemTransfer lItemTransfer = ItemTransfer.getInstance(log);
		final Transfer[] lDragTypes = new Transfer[] { lItemTransfer };
		viewer.addDragSupport(DND.DROP_COPY, lDragTypes,
				new DragSourceAdapter() {
					@Override
					public void dragSetData(final DragSourceEvent inEvent) {
						final IStructuredSelection lSelected = (IStructuredSelection) viewer
								.getSelection();
						if (!lSelected.isEmpty()) {
							final Object[] lItems = lSelected.toArray();
							final UniqueID[] lIDs = new UniqueID[lItems.length];
							for (int i = 0; i < lItems.length; i++) {
								final ILightWeightItem lItem = (ILightWeightItem) lItems[i];
								lIDs[i] = new UniqueID(lItem.getItemType(),
										lItem.getID());
							}
							inEvent.data = lIDs;
						}
					}
				});

		// make viewer a drop target
		final Transfer[] lDropTypes = new Transfer[] { lItemTransfer };
		viewer.addDropSupport(DND.DROP_MOVE, lDropTypes,
				new DropTargetAdapter() {
					@Override
					public void drop(final DropTargetEvent inEvent) {
						handlerService.executeHandler(ParameterizedCommand.generateCommand(
								commandService
										.getCommand(ICommandIds.CMD_RELATION_REMOVE),
								null));
					}
				});
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent inEvent) {
				handlerService.executeHandler(ParameterizedCommand
						.generateCommand(commandService
								.getCommand(ICommandIds.CMD_ITEM_SHOW), null));
			}
		});
	}

	/**
	 * @return boolean <code>true</code> if the component is filled and at least
	 *         one element is selected
	 */
	@Override
	public boolean hasSelection() {
		return !viewer.getSelection().isEmpty();
	}

}
