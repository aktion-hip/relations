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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsConstants;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class PreferenceDialog extends TitleAreaDialog {

	@Inject
	@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = "username")
	private String username;

	@Inject
	@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = "password")
	private String password;

	@Inject
	@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_DB_HOST)
	private String host;

	private Text usernameField;
	private Text passwordField;
	private Text hostField;

	@Inject
	public PreferenceDialog(
			@Named(IServiceConstants.ACTIVE_SHELL) final Shell inParentShell) {
		super(inParentShell);
	}

	@Override
	protected Control createDialogArea(final Composite inParent) {
		final Composite outArea = (Composite) super.createDialogArea(inParent);

		getShell().setText("Connection informations");
		setTitle("Connection informations");
		setMessage("Configure the connection informations");

		final Composite lContainer = new Composite(outArea, SWT.NONE);
		lContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		lContainer.setLayout(new GridLayout(2, false));

		Label lLabel = new Label(lContainer, SWT.NONE);
		lLabel.setText("Username");

		usernameField = new Text(lContainer, SWT.BORDER);
		usernameField.setText(username == null ? "" : username);
		usernameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lLabel = new Label(lContainer, SWT.NONE);
		lLabel.setText("Password");

		passwordField = new Text(lContainer, SWT.BORDER);
		passwordField.setText(password == null ? "" : password);
		passwordField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lLabel = new Label(lContainer, SWT.NONE);
		lLabel.setText("Host");

		hostField = new Text(lContainer, SWT.BORDER);
		hostField.setText(host == null ? "" : host);
		hostField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return outArea;
	}

	@Override
	protected void okPressed() {
		final IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode(RelationsConstants.PREFERENCE_NODE);
		prefs.put("username", usernameField.getText());
		prefs.put("password", passwordField.getText());
		prefs.put("host", hostField.getText());
		try {
			prefs.flush();
			super.okPressed();
		}
		catch (final BackingStoreException exc) {
			ErrorDialog.openError(
					getShell(),
					"Error",
					"Error while storing preferences",
					new Status(IStatus.ERROR, Activator.getSymbolicName(), exc
							.getMessage(), exc));
		}
	}
}