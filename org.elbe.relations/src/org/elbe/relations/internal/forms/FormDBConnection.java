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
package org.elbe.relations.internal.forms;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.naming.NamingException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.ChangeDB;
import org.elbe.relations.internal.actions.IDBChange;
import org.elbe.relations.internal.data.IDBSettings;
import org.elbe.relations.internal.data.TempSettings;
import org.elbe.relations.internal.search.IndexerController;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;
import org.elbe.relations.services.IDBConnectionConfig;

/**
 * Edit form for database connection settings.
 * 
 * @author Luthiger Created on 05.11.2006
 */
@SuppressWarnings("restriction")
public class FormDBConnection extends AbstractDBSettingsForm {
	private Combo dbDbasesCombo;
	private Combo dbCatalogEmbeddedCombo;
	private Combo dbLanguageCombo;

	private DecoratedTextField hostField;
	private DecoratedTextField catalogField;
	private DecoratedTextField usernameField;
	private DecoratedTextField passwrdField;

	private boolean initialized = false;
	private String[] languages = new String[] {};

	@Inject
	private Logger log;

	@Inject
	private IndexerController indexer;

	@Inject
	private IEclipseContext context;

	/**
	 * Factory method to create instances of <code>FormDBConnection</code> using
	 * DI.
	 * 
	 * @param inParent
	 *            {@link Composite}
	 * @param inColumns
	 *            int
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link FormDBConnection}
	 */
	public static FormDBConnection createFormDBConnection(
			final Composite inParent, final int inColumns,
			final IEclipseContext inContext) {
		final FormDBConnection out = ContextInjectionFactory.make(
				FormDBConnection.class, inContext);
		out.initialize(inParent, inColumns);
		return out;
	}

	private void initialize(final Composite inParent, final int inColumns) {
		initUpdateListeners();

		dbDirtySettings = new CheckDirtyServicePreferences();
		langSettings = new CheckDirtyServicePreferences();
		final int lIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();

		dbDbasesCombo = createDbasesCombo(inParent, inColumns, lIndent);
		dbCatalogEmbeddedCombo = createEmbeddedCatalogCombo(inParent,
				inColumns, lIndent);
		hostField = createHostField(inParent, inColumns, lIndent);

		catalogField = createCatalogField(inParent, inColumns, lIndent);
		catalogField.getText().addModifyListener(
				new FormModifyListener(catalogField));

		usernameField = createUsernameField(inParent, inColumns, lIndent);
		passwrdField = createPasswrdField(inParent, inColumns, lIndent);

		createSeparator(inParent, inColumns);

		languages = indexer.getContentLanguages();
		dbLanguageCombo = createLabelCombo(
				inParent,
				inColumns,
				RelationsMessages.getString("FormDBConnection.lbl.language"), languages); //$NON-NLS-1$
		((GridData) dbLanguageCombo.getLayoutData()).horizontalIndent = lIndent;
		langSettings.register(dbLanguageCombo);

		try {
			initializeValues();
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final NamingException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Initializes the controls' states with values from the preference store.
	 */
	private void initializeValues() throws SQLException, NamingException {
		initPreferences();
		dbDirtySettings.freeze();
		langSettings.freeze();
		initialized = true;
	}

	private IEclipsePreferences initPreferences() throws SQLException,
			NamingException {
		final IEclipsePreferences outStore = getPreferences();
		hostField.getText().setText(
				outStore.get(RelationsConstants.KEY_DB_HOST, "")); //$NON-NLS-1$
		catalogField.getText().setText(
				outStore.get(RelationsConstants.KEY_DB_CATALOG, "")); //$NON-NLS-1$
		usernameField.getText().setText(
				outStore.get(RelationsConstants.KEY_DB_USER_NAME, "")); //$NON-NLS-1$
		passwrdField.getText().setText(
				outStore.get(RelationsConstants.KEY_DB_PASSWORD, "")); //$NON-NLS-1$
		dbLanguageCombo.select(getLanguageIndex(outStore.get(
				RelationsConstants.KEY_LANGUAGE_CONTENT,
				RelationsConstants.DFT_LANGUAGE), languages));
		initDBCombos(outStore.get(RelationsConstants.KEY_DB_PLUGIN_ID,
				RelationsConstants.DFT_DBCONFIG_PLUGIN_ID), outStore.get(
				RelationsConstants.KEY_DB_CATALOG,
				RelationsConstants.DFT_DB_EMBEDDED));

		return outStore;
	}

	private void initDBCombos(final String inDBPluginName,
			final String inDerbySchema) throws SQLException, NamingException {
		// check existence of Derby catalogs
		final String[] lDerbyCatalogs = EmbeddedCatalogHelper.getCatalogs();
		dbCatalogEmbeddedCombo.setItems(lDerbyCatalogs);
		dbCatalogEmbeddedCombo.select(getIndex(inDerbySchema, lDerbyCatalogs));

		dbDbasesCombo.setItems(getDBController().getDBNames());
		dbDbasesCombo.select(getDBController().getSelectedIndex());
		embeddedSwitch(getDBController().isInitialEmbedded());
	}

	private int getIndex(final String inSelectedLabel,
			final String[] inSelection) {
		for (int i = 0; i < inSelection.length; i++) {
			if (inSelection[i].equals(inSelectedLabel))
				return i;
		}
		return 0;
	}

	@Override
	protected void embeddedSwitch(final boolean isEmbedded) {
		dbCatalogEmbeddedCombo.setEnabled(isEmbedded);
		hostField.setEnabled(!isEmbedded);
		catalogField.setEnabled(!isEmbedded);
		usernameField.setEnabled(!isEmbedded);
		passwrdField.setEnabled(!isEmbedded);
		hostField.setRequired(!isEmbedded);
		catalogField.setRequired(!isEmbedded);
		usernameField.setRequired(!isEmbedded);
		passwrdField.setRequired(!isEmbedded);
		if (isEmbedded) {
			resetFieldStatus();
		}
	}

	/**
	 * Saves the input.
	 * 
	 * @return boolean <code>true</code> if saving succeeded.
	 */
	public boolean saveChanges() {
		if (dbDirtySettings.isDirty() || langSettings.isDirty()) {
			if (dbDirtySettings.isDirty()) {
				final IDBConnectionConfig lSelectedDB = getDBController()
						.getConfiguration(dbDbasesCombo.getSelectionIndex());
				final IDBChange lChangeDB = ContextInjectionFactory.make(
						ChangeDB.class, context);

				if (lSelectedDB.isEmbedded()) {
					final String lCatalogName = dbCatalogEmbeddedCombo
							.getItem(dbCatalogEmbeddedCombo.getSelectionIndex());
					final IDBSettings lTempSettings = new TempSettings(
							lSelectedDB.getName(), "", lCatalogName, "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							getDBController());
					lChangeDB.setTemporarySettings(lTempSettings);
				} else {
					final String lHost = hostField.getText().getText();
					final String lCatalog = catalogField.getText().getText();
					final String lUser = usernameField.getText().getText();
					final String lPasswrd = passwrdField.getText().getText();
					final IDBSettings lTempSettings = new TempSettings(
							lSelectedDB.getName(), lHost, lCatalog, lUser,
							lPasswrd, getDBController());
					lChangeDB.setTemporarySettings(lTempSettings);
				}
				lChangeDB.execute();
			}
			if (langSettings.isDirty()) {
				final IEclipsePreferences lStore = getPreferences();
				lStore.put(RelationsConstants.KEY_LANGUAGE_CONTENT,
						languages[dbLanguageCombo.getSelectionIndex()]);
				return true;
			}
		}
		return true;
	}

	public void setDefaults() {
		try {
			initPreferences();
		}
		catch (final SQLException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final NamingException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Disposes the SWT resources allocated by this form object.
	 */
	@Override
	public void dispose() {
		hostField.dispose();
		catalogField.dispose();
		usernameField.dispose();
		passwrdField.dispose();
		dbLanguageCombo.dispose();
		dbCatalogEmbeddedCombo.dispose();
		dbDbasesCombo.dispose();
	}

	/**
	 * Returns whether this form is complete.
	 * 
	 * @return boolean <code>true</code> if this form is complete and it's okay
	 *         to move to the next page or apply the changes.
	 */
	public boolean getPageComplete() {
		if (getDBController().checkEmbedded(dbDbasesCombo.getSelectionIndex())) {
			return true;
		}
		return catalogField.length() * hostField.length()
				* usernameField.length() * passwrdField.length() != 0;
	}

	@Override
	protected boolean isInitialized() {
		return initialized;
	}

}
