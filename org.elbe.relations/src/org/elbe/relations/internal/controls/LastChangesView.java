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

import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.controller.LastChangesController;
import org.elbe.relations.internal.controller.LastChangesController.LastChangesType;
import org.elbe.relations.internal.search.RetrievedChronologicalItem;
import org.hip.kernel.exc.VException;

/**
 * View for last changed items.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class LastChangesView extends AbstractToolPart {
	private TableViewer lastChangesView;

	@Inject
	private LastChangesController lastChangesController;

	@Inject
	private Logger log;

	@Inject
	@Preference(nodePath = RelationsConstants.PREFERENCE_NODE)
	private IEclipsePreferences preferences;

	@Inject
	public LastChangesView(final Composite inParent) {
		lastChangesView = new TableViewer(inParent, SWT.H_SCROLL | SWT.V_SCROLL
		        | SWT.BORDER | SWT.MULTI);
		lastChangesView.setContentProvider(new IStructuredContentProvider() {
			@Override
			@SuppressWarnings("unchecked")
			public Object[] getElements(final Object inElement) {
				if (inElement == null)
					return null;
				return ((List<RetrievedChronologicalItem>) inElement).toArray();
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(final Viewer inViewer,
			        final Object inOldInput, final Object inNewInput) {
			}
		});
		lastChangesView.setLabelProvider(getLabelProvider());

		lastChangesView.addDoubleClickListener(getDoubleClickListener());
		lastChangesView.addDragSupport(DND.DROP_COPY, getDragTypes(),
		        getDragSourceAdapter(lastChangesView));
		lastChangesView
		        .addSelectionChangedListener(getSelectionChangedListener());
	}

	@PostConstruct
	void initialize(final MPart inPart, final EMenuService inService) {
		afterInit(inPart, inService);

		// restore settings
		final String lState = preferences.get(
		        LastChangesController.LAST_CHANGES_VIEW_TYPE,
		        LastChangesType.LAST_CREATED.getValue());
		setTitle(lastChangesController.updateType(lState));
	}

	@Inject
	@Optional
	public void update(
	        @UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String inEvent) {
		try {
			if (!lastChangesView.getControl().isDisposed()) {
				lastChangesView.setInput(lastChangesController
				        .getLastChangedItems());
			}
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	@Focus
	public void setFocus() {
		update("reload"); //$NON-NLS-1$

		final Table lTable = lastChangesView.getTable();
		lTable.setFocus();
		if (lastChangesView.getSelection().isEmpty()) {
			lTable.select(0);
		}
	}

	@Inject
	void setViewState(
	        @Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = LastChangesController.LAST_CHANGES_VIEW_TYPE) final String inLastChangeState) {
		setTitle(lastChangesController.updateType(inLastChangeState));
		update("reload"); //$NON-NLS-1$
	}

	private void setTitle(final LastChangesType inType) {
		setPartName(inType.getTitle());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.controls.AbstractToolPart#getControl()
	 */
	@Override
	protected Object getControl() {
		return lastChangesView.getControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.controls.AbstractToolPart#getContextMenuID()
	 */
	@Override
	protected String getContextMenuID() {
		return RelationsConstants.POPUP_TOOLS_CHANGES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.controls.IPartWithSelection#hasSelection()
	 */
	@Override
	public boolean hasSelection() {
		return !lastChangesView.getSelection().isEmpty();
	}

}
