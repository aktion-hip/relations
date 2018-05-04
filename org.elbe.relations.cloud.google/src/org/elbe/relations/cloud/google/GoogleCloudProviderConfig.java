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

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.services.ICloudProvider;
import org.elbe.relations.services.ICloudProviderConfig;
import org.elbe.relations.utility.AbstractCloudProviderConfig;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonObject;

/** The Google Cloud Provider Configuration component.
 *
 * @author lbenno */
@Component
public class GoogleCloudProviderConfig extends AbstractCloudProviderConfig implements ICloudProviderConfig {
    private static final String KEY_UN = "username";
    private static final String KEY_PW = "password";

    private Text username;
    private Text password;

    @Override
    public String getName() {
        return "Google";
    }

    @Override
    public Control createConfigContents(final Group parent) {
        this.username = createLabelText(parent, "Username:");
        this.password = createLabelText(parent, "Password:");
        return parent;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.username.setEnabled(enabled);
        this.password.setEnabled(enabled);
    }

    @Override
    public JsonObject getConfig() {
        final JsonObject json = new JsonObject();
        json.addProperty(KEY_UN, this.username.getText());
        json.addProperty(KEY_PW, this.password.getText());
        return json;
    }

    @Override
    public void initialize(final JsonObject values) {
        setChecked(KEY_UN, values, this.username);
        setChecked(KEY_PW, values, this.password);
    }

    @Override
    public ICloudProvider getProvider() {
        // TODO Auto-generated method stub
        return null;
    }

}
