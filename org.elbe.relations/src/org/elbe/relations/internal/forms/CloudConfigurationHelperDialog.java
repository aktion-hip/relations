/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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

import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.RelationsPreferences;
import org.elbe.relations.internal.preferences.CloudConfigPrefPage;
import org.elbe.relations.services.ICloudProviderConfigurationHelper;
import org.elbe.relations.utility.Feedback;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * The dialog displaying the registered OSGi implementations of the
 * <code>ICloudProviderConfigurationHelper</code> service.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public class CloudConfigurationHelperDialog extends TitleAreaDialog {

	private final List<ICloudProviderConfigurationHelper> helpers;
	private final Logger log;

	/**
	 * @param parentShell
	 *            {@link Shell}
	 * @param helpers
	 *            List&lt;ICloudProviderConfigurationHelper>
	 */
	public CloudConfigurationHelperDialog(final Shell parentShell,
			final List<ICloudProviderConfigurationHelper> helpers,
			final Logger log) {
		super(parentShell);
		this.helpers = helpers;
		this.log = log;
	}

	@Override
	public void create() {
		super.create();
		setTitle(RelationsMessages.getString("CloudConfigurationHelperDialog.config.title")); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite area = (Composite) super.createDialogArea(parent);
		final CTabFolder tabFolder = new CTabFolder(area,
				SWT.FLAT | SWT.BORDER);
		tabFolder.setLayoutData(
				GridDataFactory.fillDefaults().grab(true, true)
				.create());

		for (final ICloudProviderConfigurationHelper helper : this.helpers) {
			final CTabItem item = new CTabItem(tabFolder, SWT.NONE);
			item.setText(helper.getName());

			final Composite helperParent = createControl(tabFolder);
			item.setControl(helperParent);
			helper.createDialogArea(helperParent,
					(json, feedback) -> store(json, feedback, helper.getName()),
					this.log);
		}
		return area;
	}

	@Override
	protected Control createButtonBar(final Composite parent) {
		final Control composite = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return composite;
	}

	private void store(final JsonObject json, final Feedback feedback,
			final String name) {
		if (feedback.isSuccess()) {
			final IEclipsePreferences store = RelationsPreferences.getPreferences();
			final String key = CloudConfigPrefPage.getKey(name);
			store.put(key, new GsonBuilder().create().toJson(json).toString());
			setMessage(feedback.getMessage(), IMessageProvider.INFORMATION);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			setErrorMessage(feedback.getMessage());
		}
	}

	private Composite createControl(final CTabFolder tabFolder) {
		final Composite content = new Composite(tabFolder, SWT.NONE);
		content.setLayout(
				GridLayoutFactory.fillDefaults().margins(10, 7).create());
		return content;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 450);
	}

}
