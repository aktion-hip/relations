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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
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

import jakarta.annotation.PostConstruct;

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
     */
    @Inject
    public SearchView(final Composite inParent) {
        this.search = new Composite(inParent, SWT.NULL);

        final int lIndent = createInputControl(this.search);
        createButtonControl(this.search, lIndent);
        createListControl(lIndent);

        final GridLayout lGrid = new GridLayout(1, true);
        lGrid.marginWidth = 2;
        lGrid.verticalSpacing = 2;
        this.search.setLayout(lGrid);

        this.initialized = true;
    }

    @PostConstruct
    void afterInit(final MApplication inApplication,
            final EModelService inModelService, final MPart inPart,
            final EMenuService inService, final IEclipseContext inContext) {
        afterInit(inPart, inService);

        this.settings = new DialogSettingHelper(inPart, DIALOG_TERM);
        this.input.setItems(this.settings.getRecentValues());
    }

    private int createInputControl(final Composite inSearch) {
        this.input = new Combo(inSearch,
                SWT.BORDER | SWT.SINGLE | SWT.DROP_DOWN | SWT.SEARCH);
        final ControlDecoration lDecoration = new ControlDecoration(this.input,
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
        this.input.setLayoutData(lLayout);
        this.input.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent inEvent) {
                if (!SearchView.this.initialized) {
                    return;
                }
                final int lLength = ((Combo) inEvent.widget).getText().length();
                if (lLength == 0) {
                    SearchView.this.button.setEnabled(false);
                } else {
                    SearchView.this.button.setEnabled(true);
                }
            }
        });
        this.input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent inEvent) {
                final String lSelection = (String) SearchView.this.selectionService
                        .getSelection(RelationsConstants.PART_INSPECTOR);
                if (lSelection != null && !lSelection.isEmpty()) {
                    SearchView.this.input.setText(lSelection);
                } else {
                    SearchView.this.input.setText(""); //$NON-NLS-1$
                }
            }
        });
        this.input.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(final SelectionEvent inEvent) {
                searchFor(SearchView.this.input.getText());
            }
        });
        this.input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent event) {
                if (event.keyCode == SWT.Selection
                        && !SearchView.this.input.getText().isEmpty()) {
                    searchFor(SearchView.this.input.getText());
                }
            }
        });
        return outIndent;
    }

    private void createButtonControl(final Composite inSearch,
            final int inIndent) {
        this.button = new Button(this.search, SWT.PUSH);
        this.button.setText(RelationsMessages.getString("SearchView.lbl.search")); //$NON-NLS-1$
        this.button.setEnabled(false);

        this.button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(final SelectionEvent inEvent) {
                searchFor(SearchView.this.input.getText());
            }

            @Override
            public void widgetSelected(final SelectionEvent inEvent) {
                searchFor(SearchView.this.input.getText());
            }
        });

        final GridData lLayout = new GridData(SWT.BEGINNING, SWT.FILL, false,
                false);
        lLayout.horizontalIndent = inIndent;
        lLayout.widthHint = 60;
        this.button.setLayoutData(lLayout);
    }

    private void searchFor(final String inText) {
        addUnique(inText);

        // we need this to reset the selection marker
        this.results.setInput(this.searchController.emptyList());

        final Collection<RetrievedItemWithIcon> lSearchResult = this.searchController
                .search(inText);
        if (lSearchResult.isEmpty()) {
            return;
        }
        this.results.setInput(lSearchResult);

        final Table lTable = this.results.getTable();
        lTable.setFocus();
        lTable.select(0);
        this.results.setSelection(this.results.getSelection());
    }

    private void addUnique(final String inText) {
        final List<String> lItems = new ArrayList<String>(
                Arrays.asList(this.input.getItems()));
        while (lItems.remove(inText)) {
            // intentionally left empty
        }
        while (lItems.size() > RelationsConstants.DIALOG_HISTORY_LENGTH - 1) {
            lItems.remove(lItems.size() - 1);
        }
        lItems.add(0, inText);
        final String[] lNew = new String[lItems.size()];
        System.arraycopy(lItems.toArray(), 0, lNew, 0, lNew.length);
        this.input.setItems(lNew);
        this.input.setText(inText);
    }

    private void createListControl(final int inIndent) {
        this.results = new TableViewer(this.search,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
        this.results.setContentProvider(new ObservableListContentProvider());
        this.results.setLabelProvider(getLabelProvider());

        this.results.addDoubleClickListener(getDoubleClickListener());
        this.results.addDragSupport(DND.DROP_COPY, getDragTypes(),
                getDragSourceAdapter(this.results));
        this.results.addSelectionChangedListener(getSelectionChangedListener());

        final Control lControl = this.results.getControl();
        final GridData lLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
        lLayout.horizontalIndent = inIndent;
        lControl.setLayoutData(lLayout);
    }

    @Focus
    public void setFocus() {
        this.input.setFocus();
    }

    /**
     * Reset result list after a DB change.
     *
     * @param inEvent
     */
    @Inject
    void reset(
            @Optional @EventTopic(value = RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String inEvent) {
        final List<?> lInput = (List<?>) this.results.getInput();
        if (lInput != null) {
            lInput.clear();
            this.searchController.reset();
        }
    }

    @Override
    protected Object getControl() {
        return this.results.getControl();
    }

    @Override
    protected String getContextMenuID() {
        return RelationsConstants.POPUP_TOOLS_SEARCH;
    }

    @Override
    public boolean hasSelection() {
        return !this.results.getSelection().isEmpty();
    }

    @PersistState
    void persist() {
        this.settings.saveToHistory(this.input.getItems());
    }

}
