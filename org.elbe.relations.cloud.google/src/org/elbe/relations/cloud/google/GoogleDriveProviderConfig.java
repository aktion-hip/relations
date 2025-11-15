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
package org.elbe.relations.cloud.google;

import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormText;
import org.elbe.relations.services.ICloudProvider;
import org.elbe.relations.services.ICloudProviderConfig;
import org.elbe.relations.utility.AbstractCloudProviderConfig;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** The Google Cloud Provider Configuration component.
 *
 * @author lbenno */
@Component
public class GoogleDriveProviderConfig extends AbstractCloudProviderConfig implements ICloudProviderConfig {

    private Text credentials;

    @Override
    public String getName() {
        return "Google Drive"; //$NON-NLS-1$
    }

    @Override
    public Control createConfigContents(final Group parent, final Consumer<Boolean> signalIsValid) {
        final FormText hint = new FormText(parent, SWT.NO_FOCUS);
        hint.setText(String.format("<form><p>%s <b>%s</b>.</p></form>", //$NON-NLS-1$
                Messages.getString("GoogleDriveProviderConfig.msg.1"), //$NON-NLS-1$
                Messages.getString("GoogleDriveProviderConfig.msg.2")), true, false); //$NON-NLS-1$
        hint.setLayoutData(GridDataFactory.swtDefaults().span(2, 0).create());

        final var label = createLabel(parent, "credentials.json");
        label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).create());

        this.credentials = createText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.credentials
        .setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 100).grab(true, false).create());
        this.credentials.addModifyListener(e -> signalIsValid.accept(!((Text) e.getSource()).getText().isEmpty()));
        return parent;
    }

    @Override
    protected int getWidthHint() {
        return 80;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.credentials.setEnabled(enabled);
    }

    @Override
    public JsonObject getConfig() {
        return JsonParser.parseString(getCheckd(this.credentials)).getAsJsonObject();
    }

    @Override
    public void initialize(final JsonObject values) {
        if (values.isEmpty()) {
            this.credentials.setText("");
        } else {
            final Gson printer = new GsonBuilder().setPrettyPrinting().create();
            this.credentials.setText(printer.toJson(values));
        }
    }

    @Override
    public ICloudProvider getProvider() {
        return new GoogleDriveProvider();
    }

    @Override
    public boolean isValid() {
        final JsonObject json = JsonParser.parseString(getCheckd(this.credentials)).getAsJsonObject();
        if (json.has("installed")) {
            final JsonObject installed = json.getAsJsonObject("installed");
            return installed.has("client_id") && installed.has("project_id") && installed.has("client_secret")
                    && installed.has("auth_uri") && installed.has("token_uri");
        }
        return false;
    }

    private String getCheckd(final Text creds) {
        final var json = creds.getText();
        return json.isEmpty() ? "{}" : json;
    }

}
