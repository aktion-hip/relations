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
package org.elbe.relations.cloud.dropbox;

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

import com.google.gson.JsonObject;

/** The Dropbox Cloud Provider Configuration component.
 *
 * @see org.eclipse.ui.forms.widgets.FormText
 *
 * @author lbenno */
@Component
public class DropboxCloudProviderConfig extends AbstractCloudProviderConfig implements ICloudProviderConfig {
    protected static final String KEY_TOKEN = "access_token"; //$NON-NLS-1$

    private Text token;

    @Override
    public String getName() {
        return "Dropbox"; //$NON-NLS-1$
    }

    @Override
    public Control createConfigContents(final Group parent, final Consumer<Boolean> signalIsValid) {
        this.token = createLabelText(parent, Messages.getString("DropboxCloudProviderConfig.access.lbl")); //$NON-NLS-1$
        this.token.addModifyListener(event -> signalIsValid.accept(!((Text) event.getSource()).getText().isEmpty()));
        final FormText hint = new FormText(parent, SWT.NO_FOCUS);
        hint.setText(String.format("<form><p>%s <b>%s</b>.</p></form>", Messages.getString("DropboxCloudProviderConfig.msg.1"), //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("DropboxCloudProviderConfig.msg.2")), true, false); //$NON-NLS-1$
        hint.setLayoutData(GridDataFactory.swtDefaults().span(2, 0).create());

        return parent;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.token.setEnabled(enabled);
    }

    @Override
    public JsonObject getConfig() {
        final JsonObject json = new JsonObject();
        json.addProperty(KEY_TOKEN, this.token.getText());
        return json;
    }

    @Override
    public void initialize(final JsonObject values) {
        setChecked(KEY_TOKEN, values, this.token);
    }

    @Override
    public ICloudProvider getProvider() {
        return new DropboxCloudProvider();
    }

    @Override
    public boolean isValid() {
        return !this.token.getText().isEmpty();
    }

}
