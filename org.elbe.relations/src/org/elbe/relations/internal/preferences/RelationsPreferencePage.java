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
package org.elbe.relations.internal.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.RelationsPreferences;
import org.elbe.relations.internal.controller.BibliographyController;
import org.elbe.relations.internal.controller.BrowserController;
import org.elbe.relations.internal.controller.BrowserController.BrowserInfo;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.wizards.IUpdateListener;
import org.elbe.relations.services.IRelationsBrowser;
import org.elbe.relations.utility.FontUtil;

import jakarta.inject.Inject;

/**
 * Display and manipulation of the Relations preferences, i.e. language
 * selection, biblio schema selection and font size in the browser views.<br />
 * This is an Eclipse 3 preference page. To make it e4, let the values for the
 * annotated field be injected (instead of using the method init()).
 *
 * @author Luthiger
 */
public class RelationsPreferencePage extends AbstractPreferencePage {
    private Combo biblioCombo;
    private Text maxHits;
    private Text maxLastChanged;
    private BrowserViewsHelper browserFontSizes;

    @Inject
    private BibliographyController biblioController;
    @Inject
    private BrowserController browserController;
    @Inject
    private IEventBroker eventBroker;

    /**
     * RelationsPreferencePage constructor
     */
    public RelationsPreferencePage() {
        super();
    }

    /** @param title */
    public RelationsPreferencePage(final String title) {
        super(title);
    }

    /** @param title
     * @param image */
    public RelationsPreferencePage(final String title, final ImageDescriptor image) {
        super(title, image);
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite outComposite = new Composite(parent, SWT.NONE);

        final int columns = 2;
        setLayout(outComposite, columns);
        outComposite.setFont(parent.getFont());

        this.biblioCombo = createLabelCombo(outComposite,
                RelationsMessages.getString("RelationsPreferencePage.lbl.biblio"), //$NON-NLS-1$
                new String[] {});
        createSeparator(outComposite, columns);

        // font sizes in browser views
        final Label label = createLabel(outComposite,
                RelationsMessages.getString("RelationsPreferencePage.lbl.font.size")); //$NON-NLS-1$
        ((GridData) label.getLayoutData()).horizontalSpan = columns;

        this.browserFontSizes = new BrowserViewsHelper(outComposite, this.browserController.getBrowserInfos(),
                this.eventBroker);

        // Max number of search hits
        createSeparator(outComposite, columns);
        final Label label2 = createLabel(outComposite,
                RelationsMessages.getString("RelationsPreferencePage.title.fulltext.search")); // Volltext-Suche: //$NON-NLS-1$
        ((GridData) label2.getLayoutData()).horizontalSpan = columns;
        this.maxHits = createLabelText(outComposite,
                RelationsMessages.getString("RelationsPreferencePage.lbl.fulltext.search")); // Max. //$NON-NLS-1$

        // Max number displayed of last changed items
        final Label label3 = createLabel(outComposite,
                RelationsMessages.getString("RelationsPreferencePage.title.changed.items")); // Letzte //$NON-NLS-1$
        // Änderungen:
        ((GridData) label3.getLayoutData()).horizontalSpan = columns;
        this.maxLastChanged = createLabelText(outComposite,
                RelationsMessages.getString("RelationsPreferencePage.lbl.changed.items")); // Max. //$NON-NLS-1$

        initializeValues();
        return outComposite;
    }

    /**
     * Initializes states of the controls from the preference store.
     */
    private void initializeValues() {
        final IEclipsePreferences store = RelationsPreferences.getPreferences();
        this.biblioCombo.setItems(this.biblioController.getBiblioNames());
        this.biblioCombo.select(this.biblioController.getSelectedIndex());
        this.maxHits.setText(String.valueOf(store.getInt(RelationsConstants.KEY_MAX_SEARCH_HITS,
                RelationsConstants.DFT_MAX_SEARCH_HITS)));
        this.maxLastChanged.setText(String.valueOf(store.getInt(RelationsConstants.KEY_MAX_LAST_CHANGED,
                RelationsConstants.DFT_MAX_LAST_CHANGED)));
        this.browserFontSizes.initializeValues(store);
    }

    /**
     * The default button has been pressed.
     */
    @Override
    protected void performDefaults() {
        this.biblioCombo.setItems(this.biblioController.getBiblioNames());
        this.biblioCombo.select(this.biblioController.getSelectedIndex());
        this.browserFontSizes.setDefaults();
        this.maxHits.setText(String.valueOf(RelationsConstants.DFT_MAX_SEARCH_HITS));
        this.maxLastChanged.setText(String.valueOf(RelationsConstants.DFT_MAX_LAST_CHANGED));
        super.performDefaults();
    }

    /**
     * The user has pressed Ok. Store/apply this page's values appropriately.
     */
    @Override
    public boolean performOk() {
        return savePreferences();
    }

    @Override
    protected void performApply() {
        savePreferences();
    }

    private boolean savePreferences() {
        if (this.biblioCombo != null) {
            final IEclipsePreferences store = RelationsPreferences.getPreferences();
            store.put(RelationsConstants.KEY_BIBLIO_SCHEMA,
                    this.biblioController.getBibliography(this.biblioCombo.getSelectionIndex()).getId());
            store.put(RelationsConstants.KEY_MAX_SEARCH_HITS, this.maxHits.getText());
            store.put(RelationsConstants.KEY_MAX_LAST_CHANGED, this.maxLastChanged.getText());
            this.browserFontSizes.savePreferences(store);
        }
        return true;
    }

    /**
     * @see IUpdateListener#onUpdate(IStatus)
     */
    public void onUpdate(final IStatus status) {
        setErrorMessage(FormUtility.getErrorMessage(status));
    }

    // --- inner classes ---

    /**
     * Helper class to set the font size of the registered browser views. We
     * have to treat them in a special way because they are registered
     * dynamically.
     */
    private class BrowserViewsHelper {
        private static final String SHORTCUT_CHAR = "&"; //$NON-NLS-1$
        private static final String CHECK_BOX_ID = "makeFontEqual"; //$NON-NLS-1$

        private final IEventBroker eventBroker;
        private final List<Class<IRelationsBrowser>> browserIds;
        private final Map<Class<IRelationsBrowser>, Combo> browserCombos = new HashMap<>();
        private Button equalCheckBox;

        @SuppressWarnings("unchecked")
        BrowserViewsHelper(final Composite composite, final Collection<BrowserInfo> browsers,
                final IEventBroker eventBroker) {
            this.eventBroker = eventBroker;
            this.browserIds = new ArrayList<>(browsers.size());
            boolean isFirst = true;
            for (final BrowserInfo browserInformation : browsers) {
                final Class<IRelationsBrowser> id = (Class<IRelationsBrowser>) browserInformation.getBrowser();
                this.browserIds.add(id);
                final Combo combo = createComboExtended(composite, browserInformation.getName(),
                        RelationsConstants.INIT_SIZES, isFirst);
                this.browserCombos.put(id, combo);
                isFirst = false;
            }
        }

        /** Initializes the combo values from the specified preference store.
         *
         * @param store {@link IEclipsePreferences} */
        public void initializeValues(final IEclipsePreferences store) {
            this.browserCombos.forEach((k, v) -> {
                final int size = store.getInt(k.getName(), RelationsConstants.DFT_TEXT_FONT_SIZE);
                v.select(getIndex(size));
            });
            final boolean isEqual = store.getBoolean(CHECK_BOX_ID, false);
            handleEqualCheck(isEqual);
            this.equalCheckBox.setSelection(isEqual);
        }

        /** Saves the values to the specified preference store.
         *
         * @param store {@link IEclipsePreferences} */
        public void savePreferences(final IEclipsePreferences store) {
            final int defaultSize = RelationsConstants.DFT_TEXT_FONT_SIZE;
            final boolean isEqualSize = this.equalCheckBox.getSelection();
            store.putBoolean(CHECK_BOX_ID, isEqualSize);

            this.browserCombos.forEach((k, v) -> {
                final int index = v.getSelectionIndex();
                final int size = index == -1 ? defaultSize : Integer.valueOf(RelationsConstants.INIT_SIZES[index]);
                store.putInt(k.getName(), size);
                final Optional<Font> newFont = FontUtil.createOrGetFont(size);
                newFont.ifPresent(f -> this.eventBroker.post(getTopic(k), f));
            });
            // post font for Inspector
            final int fontSize = store.getInt(this.browserIds.get(0).getName(), defaultSize);
            FontUtil.createOrGetFont(fontSize)
                    .ifPresent(f -> this.eventBroker.post(RelationsConstants.KEY_TEXT_FONT_SIZE, f));
        }

        private String getTopic(final Class<IRelationsBrowser> id) {
            return id.getName().replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /**
         * Sets the combo values to the default value.
         */
        public void setDefaults() {
            final int defaultIndex = getIndex(RelationsConstants.DFT_TEXT_FONT_SIZE);
            for (final Combo combo : this.browserCombos.values()) {
                combo.select(defaultIndex);
            }
            makeIndividual();
            this.equalCheckBox.setSelection(false);
        }

        private int getIndex(final int value) {
            return getIndex(String.valueOf(value));
        }

        private int getIndex(final String value) {
            for (int i = 0; i < RelationsConstants.INIT_SIZES.length; i++) {
                if (RelationsConstants.INIT_SIZES[i].equals(value)) {
                    return i;
                }
            }
            return 0;
        }

        private Label createFPLabel(final Composite parent, final String text) {
            final Label outLabel = new Label(parent, SWT.LEFT);
            outLabel.setText(text);
            return outLabel;
        }

        private Combo createComboExtended(final Composite parent, final String text, final String[] items,
                final boolean addCheckBox) {
            createFPLabel(parent, addShortCut(text));

            final Composite composite = new Composite(parent, SWT.NONE);
            setLayout(composite, 2);

            final Combo outCombo = createCombo(composite, items);
            outCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

            if (addCheckBox) {
                this.equalCheckBox = createCheckBox(composite,
                        RelationsMessages.getString("RelationsPreferencePage.lbl.font.size.equal")); //$NON-NLS-1$
            } else {
                createFPLabel(composite, ""); //$NON-NLS-1$
            }
            return outCombo;
        }

        private String addShortCut(final String text) {
            final String[] parts = text.split("\\s"); //$NON-NLS-1$
            if (parts.length == 1) {
                return SHORTCUT_CHAR + text;
            }
            final int last = parts.length - 1;
            parts[last] = SHORTCUT_CHAR + parts[last];
            final StringBuilder out = new StringBuilder();
            for (final String lPart : parts) {
                out.append(lPart).append(" "); //$NON-NLS-1$
            }
            return new String(out).trim();
        }

        private Button createCheckBox(final Composite parent, final String text) {
            final Button checkBox = new Button(parent, SWT.CHECK);
            checkBox.setText(text);
            checkBox.setData(CHECK_BOX_ID);
            checkBox.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(final SelectionEvent event) {
                    handleEqualCheck(((Button) event.widget).getSelection());
                }

                @Override
                public void widgetDefaultSelected(final SelectionEvent event) {
                    widgetSelected(event);
                }
            });
            return checkBox;
        }

        private void handleEqualCheck(final boolean inIsEqual) {
            if (inIsEqual) {
                makeEqual();
            } else {
                makeIndividual();
            }
        }

        private void makeIndividual() {
            for (final Combo combo : this.browserCombos.values()) {
                combo.setEnabled(true);
                combo.removeListener(SWT.Selection, this.setComboValue);
            }
        }

        private void makeEqual() {
            boolean isFirst = true;
            int selectionIndex = 0;
            for (final Class<IRelationsBrowser> id : this.browserIds) {
                if (isFirst) {
                    final Combo combo = this.browserCombos.get(id);
                    combo.addListener(SWT.Selection, this.setComboValue);
                    selectionIndex = combo.getSelectionIndex();
                } else {
                    final Combo combo = this.browserCombos.get(id);
                    combo.select(selectionIndex);
                    combo.setEnabled(false);
                }
                isFirst = false;
            }
        }

        Listener setComboValue = e -> {
            if (e.widget instanceof final Combo current) {
                this.browserCombos.forEach((k, v) -> {
                    if (!v.equals(current)) {
                        v.select(current.getSelectionIndex());
                    }
                });
            }
        };
    }

}
