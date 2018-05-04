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
package org.elbe.relations.internal.preferences;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.actions.RelationsPreferences;
import org.elbe.relations.services.ICloudProviderConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * The preference page to configure the active cloud provider.
 *
 * @author lbenno
 */
public class CloudConfigPrefPage extends AbstractPreferencePage {
	private static final String TMPL_KEY = "cloud_%s_pref";

	private final List<ConfigWrapper> mappings = new ArrayList<>(5);

	@Inject
	private IEclipseContext context;

	@Override
	protected Control createContents(final Composite parent) {
		final IEclipsePreferences store = RelationsPreferences.getPreferences();
		final ActiveWrapper activePref = createActive(store);

		final CloudConfigRegistry configRegistry = this.context
				.get(CloudConfigRegistry.class);
		final List<ICloudProviderConfig> configurations = configRegistry
				.getConfigurations();
		for (final ICloudProviderConfig configuration : configurations) {
			createGroup(configuration, parent, activePref);
		}
		return parent;
	}

	private void createGroup(final ICloudProviderConfig configuration,
			final Composite parent, final ActiveWrapper activePref) {
		final Group group = new Group(parent, SWT.NONE);
		group.setText(configuration.getName());
		group.setLayoutData(
				GridDataFactory.swtDefaults().grab(true, true)
				.align(SWT.FILL, SWT.BEGINNING).create());
		group.setLayout(
				GridLayoutFactory.swtDefaults().numColumns(2).create());
		configuration.createConfigContents(group);

		new Label(group, SWT.NONE).setText("Active:");
		final Button radioBtn = new Button(group, SWT.RADIO);
		radioBtn.setData(configuration.getName());
		radioBtn.addSelectionListener(
				SelectionListener.widgetSelectedAdapter(
						ev -> handleActivationChange(ev)));

		final ConfigWrapper config = new ConfigWrapper(configuration)
				.setRadio(radioBtn);
		this.mappings.add(config);
		configuration.initialize(activePref.getValues(getKey(config)));

		// if active group, initialize widgets with values from preferences
		if (activePref.isActive(configuration.getName())) {
			radioBtn.setSelection(true);
		} else {
			configuration.setEnabled(false);
		}
	}

	private void handleActivationChange(final SelectionEvent event) {
		final String name = event.widget.getData().toString();
		for (final ConfigWrapper config : this.mappings) {
			config.setEnabled(name.equals(config.getName()));
		}
	}

	@Override
	public boolean performOk() {
		savePreferences();
		return super.performOk();
	}

	@Override
	protected void performApply() {
		savePreferences();
		super.performApply();
	}

	private boolean savePreferences() {
		final IEclipsePreferences store = RelationsPreferences.getPreferences();
		for (final ConfigWrapper config : this.mappings) {
			store(store, config, config.isActive());
		}
		return true;
	}

	// methods for interacting with the preference store
	protected void store(final IEclipsePreferences store,
			final ConfigWrapper config, final boolean isActive) {
		store.put(getKey(config), config.jsonAsString());
		if (isActive) {
			store.put(RelationsConstants.PREFS_CLOUD_ACTIVE, config.getName());
		}
	}

	protected ActiveWrapper createActive(final IEclipsePreferences store) {
		return new ActiveWrapper(store,
				store.get(RelationsConstants.PREFS_CLOUD_ACTIVE, ""));
	}

	private String getKey(final ConfigWrapper config) {
		return getKey(config.getName());
	}

	/**
	 * Convenience method: converts the cloud provider's name to a key used in
	 * the preferences store.
	 *
	 * @param name
	 *            String the cloud provider's name
	 * @return String the generated key
	 */
	public static String getKey(final String name) {
		return String.format(TMPL_KEY, name.replace(" ", "").toLowerCase());
	}

	// ---

	/**
	 * Helper object to interact with the <code>ICloudProviderConfig</code>
	 */
	private static class ConfigWrapper {
		private final ICloudProviderConfig configuration;
		private Button isActiveRadio;

		protected ConfigWrapper(final ICloudProviderConfig configuration) {
			this.configuration = configuration;
		}

		protected ConfigWrapper setRadio(final Button isActiveRadio) {
			this.isActiveRadio = isActiveRadio;
			return this;
		}

		protected boolean isActive() {
			return this.isActiveRadio.getSelection();
		}

		protected String getName() {
			return this.configuration.getName();
		}

		protected String jsonAsString() {
			return new GsonBuilder().create()
					.toJson(this.configuration.getConfig()).toString();
		}

		protected void setEnabled(final boolean enabled) {
			this.isActiveRadio.setSelection(enabled);
			this.configuration.setEnabled(enabled);
		}

	}

	/**
	 * Helper object to interact with the preference store.
	 */
	private static class ActiveWrapper {
		private final IEclipsePreferences store;
		private final String nameOfActive;

		protected ActiveWrapper(final IEclipsePreferences store,
				final String nameOfActive) {
			this.store = store;
			this.nameOfActive = nameOfActive;
		}

		protected boolean isActive(final String name) {
			return this.nameOfActive.equals(name);
		}

		protected JsonObject getValues(final String key) {
			final String jsonOfValues = this.store.get(key, "{}");
			return new Gson().fromJson(
					jsonOfValues.isEmpty() ? "{}" : jsonOfValues,
							JsonObject.class);
		}

	}

}
