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

import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.RelationsPreferences;
import org.elbe.relations.internal.services.IDBController;
import org.elbe.relations.internal.utility.CheckDirtyService;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.wizards.IUpdateListener;

/**
 * Base class for forms to input information concerning the database settings.
 *
 * @author Luthiger Created on 10.10.2008
 */
public abstract class AbstractDBSettingsForm extends AbstractPreferenceForm {
	protected ListenerList<IUpdateListener> updateListeners;
	protected CheckDirtyService dbDirtySettings;
	protected CheckDirtyService langSettings;
	private static final String BUNDLE_ID = Activator.getSymbolicName();

	final private IStatus hostEmpty = FormUtility
			.createErrorStatus(RelationsMessages
					.getString("FormDBConnection.error.hostname"), BUNDLE_ID); //$NON-NLS-1$
	final private IStatus catalogEmpty = FormUtility
			.createErrorStatus(RelationsMessages
					.getString("FormDBConnection.error.catalog"), BUNDLE_ID); //$NON-NLS-1$
	final private IStatus usernameEmpty = FormUtility
			.createErrorStatus(
					RelationsMessages.getString("FormDBConnection.error.user"), BUNDLE_ID); //$NON-NLS-1$
	final private IStatus passwrdEmpty = FormUtility
			.createErrorStatus(RelationsMessages
					.getString("FormDBConnection.error.password"), BUNDLE_ID); //$NON-NLS-1$

	private final FieldStatusManager statusManager = new FieldStatusManager();

	@Inject
	private IDBController dbController;

	protected IDBController getDBController() {
		return this.dbController;
	}

	protected Combo createDbasesCombo(final Composite inParent,
			final int inColumns, final int inIndent) {
		final Combo outCombo = createLabelCombo(
				inParent,
				inColumns,
				RelationsMessages.getString("FormDBConnection.lbl.database"), new String[] {}); //$NON-NLS-1$
		outCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent inEvent) {
				// Not needed.
			}

			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				final boolean lEmbedded = AbstractDBSettingsForm.this.dbController
						.checkEmbedded(((Combo) inEvent.widget)
								.getSelectionIndex());
				embeddedSwitch(lEmbedded);
				notifyAboutUpdate(AbstractDBSettingsForm.this.statusManager.getStati());
			}
		});
		((GridData) outCombo.getLayoutData()).horizontalIndent = inIndent;
		this.dbDirtySettings.register(outCombo);
		return outCombo;
	}

	protected Combo createEmbeddedCatalogCombo(final Composite inParent,
			final int inColumns, final int inIndent) {
		final Combo outCombo = createLabelCombo(
				inParent,
				inColumns,
				RelationsMessages
				.getString("FormDBConnection.lbl.catalog.embedded"), new String[] {}); //$NON-NLS-1$
		((GridData) outCombo.getLayoutData()).horizontalIndent = inIndent;
		this.dbDirtySettings.register(outCombo);
		return outCombo;
	}

	protected DecoratedTextField createHostField(final Composite inParent,
			final int inColumns, final int inIndent) {
		final Text lHostField = createLabeText(
				inParent,
				inColumns,
				RelationsMessages.getString("FormDBConnection.lbl.host"), SWT.BORDER | SWT.SINGLE); //$NON-NLS-1$
		((GridData) lHostField.getLayoutData()).horizontalIndent = inIndent;
		this.dbDirtySettings.register(lHostField);
		final DecoratedTextField outDecorated = createDecoratedTextField(
				lHostField,
				RelationsMessages.getString("FormDBConnection.decoration.host")); //$NON-NLS-1$
		lHostField.addFocusListener(new FormFocusListener(this.hostEmpty,
				outDecorated));
		lHostField.addModifyListener(new FormModifyListener(outDecorated));
		// add content proposal to field proposing "localhost" as possible value
		KeyStroke lKeyStroke = null;
		try {
			lKeyStroke = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
		}
		catch (final ParseException exc) {
			//
		}
		final ContentProposalAdapter lAdapter = new ContentProposalAdapter(
				lHostField,
				new TextContentAdapter(),
				new SimpleContentProposalProvider(
						new String[] { RelationsMessages
								.getString("FormDBConnection.proposal.localhost") }), //$NON-NLS-1$
				lKeyStroke, new char[] { 'l', 'L' });
		lAdapter.setPopupSize(new Point(120, 32));
		lAdapter.setPropagateKeys(true);
		lAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		this.statusManager.initialize(lHostField);
		return outDecorated;
	}

	protected DecoratedTextField createCatalogField(final Composite inParent,
			final int inColumns, final int inIndent) {
		final Text lCatalogField = createLabeText(
				inParent,
				inColumns,
				RelationsMessages.getString("FormDBConnection.lbl.catalog"), SWT.BORDER | SWT.SINGLE); //$NON-NLS-1$
		((GridData) lCatalogField.getLayoutData()).horizontalIndent = inIndent;
		this.dbDirtySettings.register(lCatalogField);
		final DecoratedTextField outDecorated = createDecoratedTextField(
				lCatalogField,
				RelationsMessages
				.getString("FormDBConnection.decoration.catalog")); //$NON-NLS-1$
		lCatalogField.addFocusListener(new FormFocusListener(this.catalogEmpty,
				outDecorated));
		this.statusManager.initialize(lCatalogField);
		return outDecorated;
	}

	protected DecoratedTextField createUsernameField(final Composite inParent,
			final int inColumns, final int inIndent) {
		final Text lUsernameField = createLabeText(
				inParent,
				inColumns,
				RelationsMessages.getString("FormDBConnection.lbl.user"), SWT.BORDER | SWT.SINGLE); //$NON-NLS-1$
		((GridData) lUsernameField.getLayoutData()).horizontalIndent = inIndent;
		this.dbDirtySettings.register(lUsernameField);
		final DecoratedTextField outDecorated = createDecoratedTextField(
				lUsernameField,
				RelationsMessages.getString("FormDBConnection.decoration.user")); //$NON-NLS-1$
		lUsernameField.addFocusListener(new FormFocusListener(this.usernameEmpty,
				outDecorated));
		lUsernameField.addModifyListener(new FormModifyListener(outDecorated));
		this.statusManager.initialize(lUsernameField);
		return outDecorated;
	}

	protected DecoratedTextField createPasswrdField(final Composite inParent,
			final int inColumns, final int inIndent) {
		final Text lPasswrdField = createLabeText(
				inParent,
				inColumns,
				RelationsMessages.getString("FormDBConnection.lbl.password"), SWT.BORDER | SWT.SINGLE | SWT.PASSWORD); //$NON-NLS-1$
		((GridData) lPasswrdField.getLayoutData()).horizontalIndent = inIndent;
		this.dbDirtySettings.register(lPasswrdField);
		final DecoratedTextField outDecorated = createDecoratedTextField(
				lPasswrdField,
				RelationsMessages
				.getString("FormDBConnection.decoration.password")); //$NON-NLS-1$
		lPasswrdField.addFocusListener(new FormFocusListener(this.passwrdEmpty,
				outDecorated));
		lPasswrdField.addModifyListener(new FormModifyListener(outDecorated));
		this.statusManager.initialize(lPasswrdField);
		return outDecorated;
	}

	protected void resetFieldStatus() {
		this.statusManager.reset();
	}

	protected void resetFieldStatusExcept(final Text inText) {
		this.statusManager.resetExcept(inText);
	}

	abstract protected void embeddedSwitch(boolean inIsEmbedded);

	abstract protected boolean isInitialized();

	abstract public void dispose();

	protected IStatus[] getStati() {
		return this.statusManager.getStati();
	}

	protected void setFieldStatus(final Text inText, final IStatus inStatus) {
		this.statusManager.set(inText, inStatus);
	}

	protected void initUpdateListeners() {
		this.updateListeners = new ListenerList<>();
	}

	/**
	 * Adds an update listener to the form to be notified about update events
	 * fired after user interaction.
	 *
	 * @param inListner
	 *            IUpdateListener
	 */
	public void addUpdateListener(final IUpdateListener inListner) {
		this.updateListeners.add(inListner);
	}

	/**
	 * Removes the specified update listener.
	 *
	 * @param inListner
	 *            IUpdateListener
	 */
	public void removeUpdateListener(final IUpdateListener inListner) {
		this.updateListeners.remove(inListner);
	}

	protected void notifyAboutUpdate(final IStatus[] inStati) {
		final MultiStatus lMulti = new MultiStatus(BUNDLE_ID, 1, inStati,
				"", null); //$NON-NLS-1$
		final Object[] lListeners = this.updateListeners.getListeners();
		for (int i = 0; i < lListeners.length; ++i) {
			((IUpdateListener) lListeners[i]).onUpdate(lMulti);
		}
	}

	private DecoratedTextField createDecoratedTextField(final Text inText,
			final String inHint) {
		FormUtility.addDecorationHint(inText, inHint);
		return new DecoratedTextField(inText,
				FormUtility.addDecorationRequired(inText),
				FormUtility.addDecorationError(inText));
	}

	// --- private classes ---

	protected IEclipsePreferences getPreferences() {
		return RelationsPreferences.getPreferences();
	}

	/**
	 * Combines/contains instance of <code>Text</code> and
	 * <code>ControlDecoration</code>s.
	 */
	protected class DecoratedTextField {
		Text text;
		ControlDecoration requiredDeco;
		ControlDecoration errorDeco;

		public DecoratedTextField(final Text inText,
				final ControlDecoration inRequired,
				final ControlDecoration inError) {
			this.text = inText;
			this.requiredDeco = inRequired;
			this.errorDeco = inError;
		}

		public Text getText() {
			return this.text;
		}

		public void dispose() {
			this.text.dispose();
			this.requiredDeco.dispose();
			this.errorDeco.dispose();
		}

		public int length() {
			return this.text.getText().length();
		}

		public void setEnabled(final boolean isEnabled) {
			this.text.setEnabled(isEnabled);
		}

		public void setRequired(final boolean inShow) {
			if (inShow) {
				this.requiredDeco.show();
			} else {
				this.requiredDeco.hide();
			}
		}

		public void setError(final boolean inShow) {
			if (inShow) {
				this.errorDeco.show();
			} else {
				this.errorDeco.hide();
			}
		}
	}

	/**
	 * Utility class to manage the dirty state of form fields.
	 *
	 * @author Luthiger Created on 11.10.2008
	 */
	protected class CheckDirtyServicePreferences extends CheckDirtyService {
		public CheckDirtyServicePreferences() {
			super(null);
		}

		@Override
		public void notifyDirtySwitch(final boolean inIsDirty) {
			if (this.isDirty ^ inIsDirty) {
				// if there is a switch in one element, check whether this was
				// the first clean or last dirty element
				final boolean lIsDirty = getDirty();
				if (this.isDirty ^ lIsDirty) {
					// the dialog's dirty status switched -> notification
					this.isDirty = lIsDirty;
				}
			}
		}
	}

	private class FormFocusListener implements FocusListener {
		private final IStatus statusError;
		private final ControlDecoration requiredDeco;
		private final ControlDecoration errorDeco;

		public FormFocusListener(final IStatus inStatusError,
				final DecoratedTextField inDecorated) {
			this.statusError = inStatusError;
			this.requiredDeco = inDecorated.requiredDeco;
			this.errorDeco = inDecorated.errorDeco;
		}

		@Override
		public void focusGained(final FocusEvent inEvent) {
			this.requiredDeco.show();
			this.errorDeco.hide();
		}

		@Override
		public void focusLost(final FocusEvent inEvent) {
			final Text lControl = (Text) inEvent.widget;
			if (lControl.getText().length() == 0) {
				AbstractDBSettingsForm.this.statusManager.set(lControl, this.statusError);
				this.requiredDeco.hide();
				this.errorDeco.show();
			}
			notifyAboutUpdate(AbstractDBSettingsForm.this.statusManager.getStati());
		}
	}

	protected class FormModifyListener implements ModifyListener {
		private final ControlDecoration requiredDeco;
		private final ControlDecoration errorDeco;

		public FormModifyListener(final DecoratedTextField inDecorated) {
			this.requiredDeco = inDecorated.requiredDeco;
			this.errorDeco = inDecorated.errorDeco;
		}

		@Override
		public void modifyText(final ModifyEvent inEvent) {
			if (!isInitialized())
				return;
			final Text lControl = (Text) inEvent.widget;
			if (lControl.getText().length() != 0) {
				AbstractDBSettingsForm.this.statusManager.set(lControl, Status.OK_STATUS);
				notifyAboutUpdate(AbstractDBSettingsForm.this.statusManager.getStati());
				this.requiredDeco.show();
				this.errorDeco.hide();
			}
		}
	}

}
