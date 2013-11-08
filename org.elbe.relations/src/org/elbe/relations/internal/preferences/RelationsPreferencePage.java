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
package org.elbe.relations.internal.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.controller.BibliographyController;
import org.elbe.relations.internal.controller.BrowserController;
import org.elbe.relations.internal.controller.BrowserController.BrowserInfo;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.wizards.IUpdateListener;
import org.elbe.relations.services.IRelationsBrowser;

/**
 * * Display and manipulation of the Relations preferences, i.e. language
 * selection, biblio schema selection and font size in the browser views.
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

	/**
	 * @param inTitle
	 */
	public RelationsPreferencePage(final String inTitle) {
		super(inTitle);
	}

	/**
	 * @param inTitle
	 * @param inImage
	 */
	public RelationsPreferencePage(final String inTitle,
			final ImageDescriptor inImage) {
		super(inTitle, inImage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite inParent) {
		final Composite outComposite = new Composite(inParent, SWT.NONE);

		final int lColumns = 2;
		setLayout(outComposite, lColumns);
		outComposite.setFont(inParent.getFont());

		biblioCombo = createLabelCombo(
				outComposite,
				RelationsMessages
						.getString("RelationsPreferencePage.lbl.biblio"), new String[] {}); //$NON-NLS-1$
		createSeparator(outComposite, lColumns);

		// font sizes in browser views
		final Label lLabel = createLabel(outComposite,
				RelationsMessages
						.getString("RelationsPreferencePage.lbl.font.size")); //$NON-NLS-1$
		((GridData) lLabel.getLayoutData()).horizontalSpan = lColumns;

		browserFontSizes = new BrowserViewsHelper(outComposite,
				browserController.getBrowserInfos(), eventBroker);

		// Max number of search hits
		createSeparator(outComposite, lColumns);
		final Label lLabel2 = createLabel(
				outComposite,
				RelationsMessages
						.getString("RelationsPreferencePage.title.fulltext.search")); //Volltext-Suche: //$NON-NLS-1$
		((GridData) lLabel2.getLayoutData()).horizontalSpan = lColumns;
		maxHits = createLabelText(
				outComposite,
				RelationsMessages
						.getString("RelationsPreferencePage.lbl.fulltext.search")); //Max. Anzahl Treffer //$NON-NLS-1$

		// Max number displayed of last changed items
		final Label lLabel3 = createLabel(
				outComposite,
				RelationsMessages
						.getString("RelationsPreferencePage.title.changed.items")); //Letzte Änderungen: //$NON-NLS-1$
		((GridData) lLabel3.getLayoutData()).horizontalSpan = lColumns;
		maxLastChanged = createLabelText(outComposite,
				RelationsMessages
						.getString("RelationsPreferencePage.lbl.changed.items")); //Max. Anzahl Einträge //$NON-NLS-1$

		initializeValues();
		return outComposite;
	}

	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		final IEclipsePreferences lStore = InstanceScope.INSTANCE
				.getNode(RelationsConstants.PREFERENCE_NODE);
		biblioCombo.setItems(biblioController.getBiblioNames());
		biblioCombo.select(biblioController.getSelectedIndex());
		maxHits.setText(String.valueOf(lStore.getInt(
				RelationsConstants.KEY_MAX_SEARCH_HITS,
				RelationsConstants.DFT_MAX_SEARCH_HITS)));
		maxLastChanged.setText(String.valueOf(lStore.getInt(
				RelationsConstants.KEY_MAX_LAST_CHANGED,
				RelationsConstants.DFT_MAX_LAST_CHANGED)));
		browserFontSizes.initializeValues(lStore);
	}

	/**
	 * The default button has been pressed.
	 */
	@Override
	protected void performDefaults() {
		biblioCombo.setItems(biblioController.getBiblioNames());
		biblioCombo.select(biblioController.getSelectedIndex());
		browserFontSizes.setDefaults();
		maxHits.setText(String.valueOf(RelationsConstants.DFT_MAX_SEARCH_HITS));
		maxLastChanged.setText(String
				.valueOf(RelationsConstants.DFT_MAX_LAST_CHANGED));
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(final IWorkbench inWorkbench) {
		// nothing to do
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
		if (biblioCombo != null) {
			final IEclipsePreferences lStore = InstanceScope.INSTANCE
					.getNode(RelationsConstants.PREFERENCE_NODE);
			lStore.put(RelationsConstants.KEY_BIBLIO_SCHEMA, biblioController
					.getBibliography(biblioCombo.getSelectionIndex()).getId());
			lStore.put(RelationsConstants.KEY_MAX_SEARCH_HITS,
					maxHits.getText());
			lStore.put(RelationsConstants.KEY_MAX_LAST_CHANGED,
					maxLastChanged.getText());
			browserFontSizes.savePreferences(lStore);
		}
		return true;
	}

	/**
	 * @see IUpdateListener#onUpdate(IStatus)
	 */
	public void onUpdate(final IStatus inStatus) {
		setErrorMessage(FormUtility.getErrorMessage(inStatus));
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
		private final Collection<Class<IRelationsBrowser>> browserIds;
		private final Map<Class<IRelationsBrowser>, Combo> browserCombos;
		private Button equalCheckBox;

		@SuppressWarnings("unchecked")
		BrowserViewsHelper(final Composite inComposite,
				final Collection<BrowserInfo> inBrowsers,
				final IEventBroker inEventBroker) {
			eventBroker = inEventBroker;
			browserIds = new ArrayList<Class<IRelationsBrowser>>(
					inBrowsers.size());
			browserCombos = new HashMap<Class<IRelationsBrowser>, Combo>();
			boolean lFirst = true;
			for (final BrowserInfo lBrowserInformation : inBrowsers) {
				final Class<IRelationsBrowser> lId = (Class<IRelationsBrowser>) lBrowserInformation
						.getBrowser();
				browserIds.add(lId);
				final Combo lCombo = createComboExtended(inComposite,
						lBrowserInformation.getName(),
						RelationsConstants.INIT_SIZES, lFirst);
				browserCombos.put(lId, lCombo);
				lFirst = false;
			}
		}

		/**
		 * Initializes the combo values from the specified preference store.
		 * 
		 * @param inStore
		 *            {@link IEclipsePreferences}
		 */
		public void initializeValues(final IEclipsePreferences inStore) {
			for (final Class<IRelationsBrowser> lID : browserCombos.keySet()) {
				final int lValue = inStore.getInt(lID.getName(),
						RelationsConstants.DFT_TEXT_FONT_SIZE);
				browserCombos.get(lID).select(getIndex(lValue));
			}
			final boolean lIsEqual = inStore.getBoolean(CHECK_BOX_ID, false);
			handleEqualCheck(lIsEqual);
			equalCheckBox.setSelection(lIsEqual);
		}

		/**
		 * Saves the values to the specified preference store.
		 * 
		 * @param inStore
		 *            {@link IEclipsePreferences}
		 */
		public void savePreferences(final IEclipsePreferences inStore) {
			final int lDefault = RelationsConstants.DFT_TEXT_FONT_SIZE;
			for (final Class<IRelationsBrowser> lID : browserCombos.keySet()) {
				final Combo lCombo = browserCombos.get(lID);
				final int lIndex = lCombo.getSelectionIndex();
				final int lValue = lIndex == -1 ? lDefault : Integer
						.valueOf(RelationsConstants.INIT_SIZES[lIndex]);
				inStore.putInt(lID.getName(), lValue);
				eventBroker.post(getTopic(lID), lValue);
			}
			inStore.putBoolean(CHECK_BOX_ID, equalCheckBox.getSelection());
		}

		private String getTopic(final Class<IRelationsBrowser> inID) {
			return inID.getName().replaceAll("\\.", "_");
		}

		/**
		 * Sets the combo values to the default value.
		 */
		public void setDefaults() {
			final int lDefaultIndex = getIndex(RelationsConstants.DFT_TEXT_FONT_SIZE);
			for (final Combo lCombo : browserCombos.values()) {
				lCombo.select(lDefaultIndex);
			}
			makeIndividual();
			equalCheckBox.setSelection(false);
		}

		private int getIndex(final int inValue) {
			return getIndex(String.valueOf(inValue));
		}

		private int getIndex(final String inValue) {
			for (int i = 0; i < RelationsConstants.INIT_SIZES.length; i++) {
				if (RelationsConstants.INIT_SIZES[i].equals(inValue)) {
					return i;
				}
			}
			return 0;
		}

		private Label createFPLabel(final Composite inParent,
				final String inText) {
			final Label outLabel = new Label(inParent, SWT.LEFT);
			outLabel.setText(inText);
			return outLabel;
		}

		private Combo createComboExtended(final Composite inParent,
				final String inText, final String[] inItems,
				final boolean inAddCheckBox) {
			createFPLabel(inParent, addShortCut(inText));

			final Composite lComposite = new Composite(inParent, SWT.NONE);
			setLayout(lComposite, 2);

			final Combo outCombo = createCombo(lComposite, inItems);
			outCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));

			if (inAddCheckBox) {
				equalCheckBox = createCheckBox(
						lComposite,
						RelationsMessages
								.getString("RelationsPreferencePage.lbl.font.size.equal")); //$NON-NLS-1$
			} else {
				createFPLabel(lComposite, ""); //$NON-NLS-1$
			}
			return outCombo;
		}

		private String addShortCut(final String inText) {
			final String[] lParts = inText.split("\\s"); //$NON-NLS-1$
			if (lParts.length == 1)
				return SHORTCUT_CHAR + inText;
			final int lLast = lParts.length - 1;
			lParts[lLast] = SHORTCUT_CHAR + lParts[lLast];
			final StringBuilder out = new StringBuilder();
			for (final String lPart : lParts) {
				out.append(lPart).append(" "); //$NON-NLS-1$
			}
			return new String(out).trim();
		}

		private Button createCheckBox(final Composite inParent,
				final String inText) {
			final Button outButton = new Button(inParent, SWT.CHECK);
			outButton.setText(inText);
			outButton.setData(CHECK_BOX_ID);
			outButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(final SelectionEvent inEvent) {
					handleEqualCheck(((Button) inEvent.widget).getSelection());
				}

				@Override
				public void widgetDefaultSelected(final SelectionEvent inEvent) {
					widgetSelected(inEvent);
				}
			});
			return outButton;
		}

		private void handleEqualCheck(final boolean inIsEqual) {
			if (inIsEqual) {
				makeEqual();
			} else {
				makeIndividual();
			}
		}

		private void makeIndividual() {
			for (final Combo lCombo : browserCombos.values()) {
				lCombo.setEnabled(true);
			}
		}

		private void makeEqual() {
			boolean lFirst = true;
			int lSelectionIndex = 0;
			for (final Class<IRelationsBrowser> lId : browserIds) {
				if (lFirst) {
					lSelectionIndex = browserCombos.get(lId.getName())
							.getSelectionIndex();
				} else {
					final Combo lCombo = browserCombos.get(lId.getName());
					lCombo.select(lSelectionIndex);
					lCombo.setEnabled(false);
				}
				lFirst = false;
			}
		}
	}

}
