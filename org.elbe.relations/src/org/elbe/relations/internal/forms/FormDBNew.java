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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.CreateEmbeddedDB;
import org.elbe.relations.internal.actions.CreateExternalDB;
import org.elbe.relations.internal.actions.IDBChange;
import org.elbe.relations.internal.data.IDBSettings;
import org.elbe.relations.internal.data.TempSettings;
import org.elbe.relations.internal.search.IndexerController;
import org.elbe.relations.internal.utility.CheckDirtyServiceNoop;
import org.elbe.relations.internal.utility.EmbeddedCatalogHelper;
import org.elbe.relations.services.IDBConnectionConfig;

/**
 * Form to collect the data to create a new database catalog (either internal or
 * external).
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class FormDBNew extends AbstractDBSettingsForm {
	private Combo dbDbasesCombo;
	private DecoratedTextField hostField;
	private DecoratedTextField catalogField;
	private DecoratedTextField usernameField;
	private DecoratedTextField passwrdField;
	private Combo dbLanguageCombo;

	@Inject
	private Logger log;

	@Inject
	private IndexerController indexer;

	@Inject
	private IEclipseContext context;

	private boolean initialized = false;
	private final EmbeddedCatalogHelper catalogHelper = new EmbeddedCatalogHelper();
	private String[] languages = new String[] {};

	/**
	 * Factory method to create instances of <code>FormDBNew</code> using DI.
	 * 
	 * @param inParent
	 *            {@link Composite}
	 * @param inColumns
	 *            int
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link FormDBNew}
	 */
	public static FormDBNew createFormDBNew(final Composite inParent,
			final int inColumns, final IEclipseContext inContext) {
		final FormDBNew out = ContextInjectionFactory.make(FormDBNew.class,
				inContext);
		out.initialize(inParent, inColumns);
		return out;
	}

	private void initialize(final Composite inParent, final int inColumns) {
		initUpdateListeners();
		dbDirtySettings = new CheckDirtyServiceNoop();

		final int lIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();

		dbDbasesCombo = createDbasesCombo(inParent, inColumns, lIndent);

		catalogField = createCatalogField(inParent, inColumns, lIndent);
		catalogField.getText().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				final String lName = ((Text) inEvent.widget).getText();
				if (lName.length() != 0) {
					checkModifiedCatalogField(lName, getDBController()
							.checkEmbedded(dbDbasesCombo.getSelectionIndex()));
					notifyAboutUpdate(getStati());
				}
			}
		});
		hostField = createHostField(inParent, inColumns, lIndent);
		usernameField = createUsernameField(inParent, inColumns, lIndent);
		passwrdField = createPasswrdField(inParent, inColumns, lIndent);

		createSeparator(inParent, inColumns);

		languages = indexer.getContentLanguages();
		dbLanguageCombo = createLabelCombo(
				inParent,
				inColumns,
				RelationsMessages.getString("FormDBConnection.lbl.language"), languages); //$NON-NLS-1$
		((GridData) dbLanguageCombo.getLayoutData()).horizontalIndent = lIndent;

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

	private void checkModifiedCatalogField(final String inText,
			final boolean isEmbedded) {
		if (isEmbedded) {
			setFieldStatus(catalogField.getText(),
					catalogHelper.validate(inText.toLowerCase()));
		} else {
			setFieldStatus(catalogField.getText(),
					catalogHelper.validateForAllowedChars(inText.toLowerCase()));
		}
	}

	private void initializeValues() throws SQLException, NamingException {
		final IEclipsePreferences lStore = getPreferences();
		initDBCombos(lStore.get(RelationsConstants.KEY_DB_PLUGIN_ID,
				RelationsConstants.DFT_DBCONFIG_PLUGIN_ID), lStore.get(
				RelationsConstants.KEY_DB_CATALOG, "")); //$NON-NLS-1$
		dbLanguageCombo.select(getLanguageIndex(lStore.get(
				RelationsConstants.KEY_LANGUAGE_CONTENT,
				RelationsConstants.DFT_LANGUAGE), languages));
		initialized = true;
		dbDbasesCombo.setFocus();
	}

	private void initDBCombos(final String inDBPluginName,
			final String inDerbySchema) throws SQLException, NamingException {
		dbDbasesCombo.setItems(getDBController().getDBNames());
		dbDbasesCombo.select(getDBController().getSelectedIndex());
		embeddedSwitch(getDBController().isInitialEmbedded());
	}

	@Override
	protected void embeddedSwitch(final boolean inIsEmbedded) {
		hostField.setEnabled(!inIsEmbedded);
		usernameField.setEnabled(!inIsEmbedded);
		passwrdField.setEnabled(!inIsEmbedded);
		hostField.setRequired(!inIsEmbedded);
		usernameField.setRequired(!inIsEmbedded);
		passwrdField.setRequired(!inIsEmbedded);
		checkModifiedCatalogField(catalogField.getText().getText(),
				inIsEmbedded);
		if (inIsEmbedded) {
			resetFieldStatusExcept(catalogField.getText());
		}
	}

	@Override
	protected boolean isInitialized() {
		return initialized;
	}

	/**
	 * Returns whether this form is complete.
	 * 
	 * @return boolean <code>true</code> if this form is complete and it's okay
	 *         to move to the next page or apply the changes.
	 */
	public boolean getPageComplete() {
		final MultiStatus lMulti = new MultiStatus(Activator.getSymbolicName(),
				1, getStati(), "", null); //$NON-NLS-1$
		if (lMulti.getSeverity() == IStatus.ERROR)
			return false;

		if (getDBController().checkEmbedded(dbDbasesCombo.getSelectionIndex())) {
			return catalogField.length() != 0;
		}
		return catalogField.length() * hostField.length()
				* usernameField.length() * passwrdField.length() != 0;
	}

	@Override
	public void dispose() {
		dbDbasesCombo.dispose();
		hostField.dispose();
		catalogField.dispose();
		usernameField.dispose();
		passwrdField.dispose();
		dbLanguageCombo.dispose();
	}

	/**
	 * @return {@link IDBChange} the db catalog creation handler based on the
	 *         user input.
	 */
	public IDBChange getResultObject() {
		final IDBConnectionConfig lSelectedDB = getDBController()
				.getConfiguration(dbDbasesCombo.getSelectionIndex());
		final String lCatalog = catalogField.getText().getText();

		IDBChange out;
		if (lSelectedDB.isEmbedded()) {
			final IDBSettings lTempSettings = new TempSettings(
					lSelectedDB.getName(), "", lCatalog, "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					getDBController());
			out = ContextInjectionFactory.make(CreateEmbeddedDB.class, context);
			out.setTemporarySettings(lTempSettings);
			((CreateEmbeddedDB) out).setHelper(catalogHelper);
		} else {
			final String lHost = hostField.getText().getText();
			final String lUser = usernameField.getText().getText();
			final String lPasswrd = passwrdField.getText().getText();
			final IDBSettings lTempSettings = new TempSettings(
					lSelectedDB.getName(), lHost, lCatalog, lUser, lPasswrd,
					getDBController());
			out = ContextInjectionFactory.make(CreateExternalDB.class, context);
			out.setTemporarySettings(lTempSettings);
		}
		return out;
	}

}
