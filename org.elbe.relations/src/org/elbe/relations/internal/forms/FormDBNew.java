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
		this.dbDirtySettings = new CheckDirtyServiceNoop();

		final int lIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();

		this.dbDbasesCombo = createDbasesCombo(inParent, inColumns, lIndent);

		this.catalogField = createCatalogField(inParent, inColumns, lIndent);
		this.catalogField.getText().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				final String lName = ((Text) inEvent.widget).getText();
				if (lName.length() != 0) {
					checkModifiedCatalogField(lName, getDBController()
							.checkEmbedded(FormDBNew.this.dbDbasesCombo.getSelectionIndex()));
					notifyAboutUpdate(getStati());
				}
			}
		});
		this.hostField = createHostField(inParent, inColumns, lIndent);
		this.usernameField = createUsernameField(inParent, inColumns, lIndent);
		this.passwrdField = createPasswrdField(inParent, inColumns, lIndent);

		createSeparator(inParent, inColumns);

		this.languages = this.indexer.getContentLanguages();
		this.dbLanguageCombo = createLabelCombo(
				inParent,
				inColumns,
				RelationsMessages.getString("FormDBConnection.lbl.language"), this.languages); //$NON-NLS-1$
		((GridData) this.dbLanguageCombo.getLayoutData()).horizontalIndent = lIndent;

		try {
			initializeValues();
		}
		catch (final SQLException exc) {
			this.log.error(exc, exc.getMessage());
		}
		catch (final NamingException exc) {
			this.log.error(exc, exc.getMessage());
		}
	}

	private void checkModifiedCatalogField(final String inText,
			final boolean isEmbedded) {
		if (isEmbedded) {
			setFieldStatus(this.catalogField.getText(),
					this.catalogHelper.validate(inText.toLowerCase()));
		} else {
			setFieldStatus(this.catalogField.getText(),
					this.catalogHelper.validateForAllowedChars(inText.toLowerCase()));
		}
	}

	private void initializeValues() throws SQLException, NamingException {
		final IEclipsePreferences lStore = getPreferences();
		initDBCombos();
		this.dbLanguageCombo.select(getLanguageIndex(lStore.get(
				RelationsConstants.KEY_LANGUAGE_CONTENT,
				RelationsConstants.DFT_LANGUAGE), this.languages));
		this.initialized = true;
		this.dbDbasesCombo.setFocus();
	}

	private void initDBCombos() throws SQLException, NamingException {
		this.dbDbasesCombo.setItems(getDBController().getDBNames());
		this.dbDbasesCombo.select(getDBController().getSelectedIndex());
		embeddedSwitch(getDBController().isInitialEmbedded());
	}

	@Override
	protected void embeddedSwitch(final boolean inIsEmbedded) {
		this.hostField.setEnabled(!inIsEmbedded);
		this.usernameField.setEnabled(!inIsEmbedded);
		this.passwrdField.setEnabled(!inIsEmbedded);
		this.hostField.setRequired(!inIsEmbedded);
		this.usernameField.setRequired(!inIsEmbedded);
		this.passwrdField.setRequired(!inIsEmbedded);
		checkModifiedCatalogField(this.catalogField.getText().getText(),
				inIsEmbedded);
		if (inIsEmbedded) {
			resetFieldStatusExcept(this.catalogField.getText());
		}
	}

	@Override
	protected boolean isInitialized() {
		return this.initialized;
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

		if (getDBController().checkEmbedded(this.dbDbasesCombo.getSelectionIndex())) {
			return this.catalogField.length() != 0;
		}
		return this.catalogField.length() * this.hostField.length()
				* this.usernameField.length() * this.passwrdField.length() != 0;
	}

	@Override
	public void dispose() {
		this.dbDbasesCombo.dispose();
		this.hostField.dispose();
		this.catalogField.dispose();
		this.usernameField.dispose();
		this.passwrdField.dispose();
		this.dbLanguageCombo.dispose();
	}

	/**
	 * @return {@link IDBChange} the db catalog creation handler based on the
	 *         user input.
	 */
	public IDBChange getResultObject() {
		final IDBConnectionConfig selectedDB = getDBController()
				.getConfiguration(this.dbDbasesCombo.getSelectionIndex());
		final String catalog = this.catalogField.getText().getText();

		IDBChange out;
		if (selectedDB.isEmbedded()) {
			final IDBSettings tempSettings = new TempSettings(
					selectedDB.getName(), "", catalog, "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					getDBController());
			out = ContextInjectionFactory.make(CreateEmbeddedDB.class, this.context);
			out.setTemporarySettings(tempSettings);
			((CreateEmbeddedDB) out).setHelper(this.catalogHelper);
		} else {
			final String host = this.hostField.getText().getText();
			final String user = this.usernameField.getText().getText();
			final String passwrd = this.passwrdField.getText().getText();
			final IDBSettings tempSettings = new TempSettings(
			        selectedDB.getName(), host, catalog, user, passwrd,
					getDBController());
			out = ContextInjectionFactory.make(CreateExternalDB.class, this.context);
			out.setTemporarySettings(tempSettings);
		}
		return out;
	}

}
