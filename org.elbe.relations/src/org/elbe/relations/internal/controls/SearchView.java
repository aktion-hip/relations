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

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.search.RetrievedItem;
import org.elbe.relations.internal.controller.SearchController;
import org.elbe.relations.internal.utility.DialogSettingHelper;

import jakarta.inject.Inject;

/**
 * View to search items. By default, this view is configured as fast view (i.e.
 * is displayed minimized).
 *
 * @author Luthiger
 */
public class SearchView extends AbstractToolPart {
    private static final String QUERY_HINT = RelationsMessages.getString("SearchView.tip.search"); //$NON-NLS-1$
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
    private final DialogSettingHelper settings;

    /** SearchView constructor, called through DI.
     *
     * @param parent {@link Composite}
     * @param part {@link MPart}
     * @param service {@link EMenuService} */
    @Inject
    public SearchView(final Composite parent, final MPart part, final EMenuService service) {
        this.search = new Composite(parent, SWT.NULL);
        this.search.setLayout(GridLayoutFactory.swtDefaults().margins(2, SWT.DEFAULT).spacing(SWT.DEFAULT, 2).create());

        final int indent = createInputControl(this.search);
        createButtonControl(this.search, indent);
        createListControl(indent);

        afterInit(part, service);

        this.settings = new DialogSettingHelper(part, DIALOG_TERM);
        this.input.setItems(this.settings.getRecentValues());

        this.initialized = true;
    }

    private int createInputControl(final Composite search) {
        this.input = new Combo(search, SWT.BORDER | SWT.SINGLE | SWT.DROP_DOWN | SWT.SEARCH);
        final ControlDecoration decoration = new ControlDecoration(this.input, SWT.LEFT | SWT.TOP);
        final FieldDecoration proposeDeco = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
        proposeDeco.setDescription(QUERY_HINT);
        decoration.setImage(proposeDeco.getImage());
        decoration.setDescriptionText(proposeDeco.getDescription());

        final GridData lLayout = new GridData(GridData.FILL_HORIZONTAL);
        final int outIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
        lLayout.horizontalIndent = outIndent;
        this.input.setLayoutData(lLayout);
        this.input.addModifyListener(event -> {
            if (!SearchView.this.initialized) {
                return;
            }
            final int length = ((Combo) event.widget).getText().length();
            SearchView.this.button.setEnabled(length != 0);
        });
        this.input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent event) {
                final String selection = (String) SearchView.this.selectionService
                        .getSelection(RelationsConstants.PART_INSPECTOR);
                if (selection != null && !selection.isEmpty()) {
                    SearchView.this.input.setText(selection);
                } else {
                    SearchView.this.input.setText(""); //$NON-NLS-1$
                }
            }
        });
        this.input.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(final SelectionEvent event) {
                searchFor(SearchView.this.input.getText());
            }
        });
        this.input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent event) {
                if (event.keyCode == SWT.Selection && !SearchView.this.input.getText().isEmpty()) {
                    searchFor(SearchView.this.input.getText());
                }
            }
        });
        return outIndent;
    }

    private void createButtonControl(final Composite search, final int indent) {
        this.button = new Button(search, SWT.PUSH);
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

        final GridData layout = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
        layout.horizontalIndent = indent;
        layout.widthHint = 60;
        this.button.setLayoutData(layout);
    }

    private void searchFor(final String inText) {
        addUnique(inText);

        // we need this to reset the selection marker
        this.results.setInput(this.searchController.emptyList());

        final Collection<RetrievedItem> searchResult = this.searchController.search(inText);
        if (searchResult.isEmpty()) {
            return;
        }
        this.results.setInput(searchResult);

        final Table table = this.results.getTable();
        table.setFocus();
        table.select(0);
        this.results.setSelection(this.results.getSelection());
    }

    private void addUnique(final String text) {
        final List<String> items = new ArrayList<>(Arrays.asList(this.input.getItems()));
        while (items.remove(text)) {
            // intentionally left empty
        }
        while (items.size() > RelationsConstants.DIALOG_HISTORY_LENGTH - 1) {
            items.remove(items.size() - 1);
        }
        items.add(0, text);
        final String[] newItems = new String[items.size()];
        System.arraycopy(items.toArray(), 0, newItems, 0, newItems.length);
        this.input.setItems(newItems);
        this.input.setText(text);
    }

    private void createListControl(final int indent) {
        this.results = new TableViewer(this.search, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
        this.results.setContentProvider(new ObservableListContentProvider<RetrievedItem>());
        this.results.setLabelProvider(getLabelProvider());

        this.results.addDoubleClickListener(getDoubleClickListener());
        this.results.addDragSupport(DND.DROP_COPY, getDragTypes(), getDragSourceAdapter(this.results));
        this.results.addSelectionChangedListener(getSelectionChangedListener());

        final Control control = this.results.getControl();
        final GridData layout = new GridData(SWT.FILL, SWT.FILL, true, true);
        layout.horizontalIndent = indent;
        control.setLayoutData(layout);
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
