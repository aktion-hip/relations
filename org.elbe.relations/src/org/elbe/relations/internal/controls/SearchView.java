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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.controller.SearchController;
import org.elbe.relations.internal.utility.DialogSettingHelper;
import org.elbe.relations.search.RetrievedItemWithIcon;

/**
 * View to search items. By default, this view is configured as fast view (i.e.
 * is displayed minimized).
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class SearchView extends AbstractToolPart {
	private static final String QUERY_HINT = RelationsMessages
			.getString("SearchView.tip.search"); //$NON-NLS-1$
	private static final String DIALOG_TERM = "relations.search.memory"; //$NON-NLS-1$

	@Inject
	private ESelectionService selectionService;

	@Inject
	private SearchController searchController;

	private final Composite search;
	private Combo input;
	private Button button;
	private TableViewer results;

	private boolean initialized = false;
	private DialogSettingHelper settings;

	/**
	 * SearchView constructor, called through DI.
	 * 
	 * @param inParent
	 *            {@link Composite}
	 * @param inContext
	 *            {@link IEclipseContext}
	 */
	@Inject
	public SearchView(final Composite inParent, final IEclipseContext inContext) {
		search = new Composite(inParent, SWT.NULL);

		final int lIndent = createInputControl(search);
		createButtonControl(search, lIndent);
		createListControl(search, lIndent);

		final GridLayout lGrid = new GridLayout(1, true);
		lGrid.marginWidth = 2;
		lGrid.verticalSpacing = 2;
		search.setLayout(lGrid);

		initialized = true;
	}

	@PostConstruct
	void afterInit(final MApplication inApplication,
			final EModelService inModelService, final MPart inPart,
			final EMenuService inService, final IEclipseContext inContext) {
		afterInit(inPart, inService);

		settings = new DialogSettingHelper(inPart, DIALOG_TERM);
		input.setItems(settings.getRecentValues());
	}

	private int createInputControl(final Composite inSearch) {
		input = new Combo(inSearch, SWT.BORDER | SWT.SINGLE | SWT.DROP_DOWN
				| SWT.SEARCH);
		final ControlDecoration lDecoration = new ControlDecoration(input,
				SWT.LEFT | SWT.TOP);
		final FieldDecoration lProposeDeco = FieldDecorationRegistry
				.getDefault().getFieldDecoration(
						FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		lProposeDeco.setDescription(QUERY_HINT);
		lDecoration.setImage(lProposeDeco.getImage());
		lDecoration.setDescriptionText(lProposeDeco.getDescription());

		final GridData lLayout = new GridData(GridData.FILL_HORIZONTAL);
		final int outIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		lLayout.horizontalIndent = outIndent;
		input.setLayoutData(lLayout);
		input.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				if (!initialized) {
					return;
				}
				final int lLength = ((Combo) inEvent.widget).getText().length();
				if (lLength == 0) {
					button.setEnabled(false);
				} else {
					button.setEnabled(true);
				}
			}
		});
		input.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				final String lSelection = (String) selectionService
						.getSelection(RelationsConstants.PART_INSPECTOR);
				if (lSelection != null && !lSelection.isEmpty()) {
					input.setText(lSelection);
				} else {
					input.setText(""); //$NON-NLS-1$
				}
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
			}
		});
		input.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				searchFor(input.getText());
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent inEvent) {
				searchFor(input.getText());
			}
		});
		return outIndent;
	}

	private void createButtonControl(final Composite inSearch,
			final int inIndent) {
		button = new Button(search, SWT.PUSH);
		button.setText(RelationsMessages.getString("SearchView.lbl.search")); //$NON-NLS-1$
		button.setEnabled(false);

		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent inEvent) {
				searchFor(input.getText());
			}

			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				searchFor(input.getText());
			}
		});

		final GridData lLayout = new GridData(SWT.BEGINNING, SWT.FILL, false,
				false);
		lLayout.horizontalIndent = inIndent;
		lLayout.widthHint = 60;
		button.setLayoutData(lLayout);
		inSearch.getShell().setDefaultButton(button);
	}

	private void searchFor(final String inText) {
		addUnique(inText);
		final Collection<RetrievedItemWithIcon> lSearchResult = searchController
				.search(inText);
		if (lSearchResult.isEmpty()) {
			return;
		}

		results.setInput(lSearchResult);
		final Table lTable = results.getTable();
		lTable.setFocus();
		lTable.select(0);
		results.setSelection(results.getSelection());
	}

	private void addUnique(final String inText) {
		final Vector<String> lItems = new Vector<String>(Arrays.asList(input
				.getItems()));
		while (lItems.remove(inText)) {
			// intentionally left empty
		}
		if (lItems.size() > RelationsConstants.DIALOG_HISTORY_LENGTH - 1) {
			lItems.setSize(RelationsConstants.DIALOG_HISTORY_LENGTH - 1);
		}
		lItems.insertElementAt(inText, 0);
		final String[] lNew = new String[lItems.size()];
		System.arraycopy(lItems.toArray(), 0, lNew, 0, lNew.length);
		input.setItems(lNew);
		input.setText(inText);
	}

	private void createListControl(final Composite inSearch, final int inIndent) {
		results = new TableViewer(search, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER | SWT.MULTI);
		results.setContentProvider(new ObservableListContentProvider());
		results.setLabelProvider(getLabelProvider());

		results.addDoubleClickListener(getDoubleClickListener());
		results.addDragSupport(DND.DROP_COPY, getDragTypes(),
				getDragSourceAdapter(results));
		results.addSelectionChangedListener(getSelectionChangedListener());

		final Control lControl = results.getControl();
		final GridData lLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
		lLayout.horizontalIndent = inIndent;
		lControl.setLayoutData(lLayout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Focus
	public void setFocus() {
		input.setFocus();
	}

	/**
	 * Reset result list after a DB change.
	 * 
	 * @param inEvent
	 */
	@Inject
	void reset(
			@Optional @EventTopic(value = RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String inEvent) {
		final List<?> lInput = (List<?>) results.getInput();
		if (lInput != null) {
			lInput.clear();
			searchController.reset();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.controls.AbstractToolPart#getControl()
	 */
	@Override
	protected Object getControl() {
		return results.getControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.controls.AbstractToolPart#getContextMenuID()
	 */
	@Override
	protected String getContextMenuID() {
		return RelationsConstants.POPUP_TOOLS_SEARCH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.controls.IPartWithSelection#hasSelection()
	 */
	@Override
	public boolean hasSelection() {
		return !results.getSelection().isEmpty();
	}

	@PersistState
	void persist() {
		settings.saveToHistory(input.getItems());
	}

}
