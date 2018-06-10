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
package org.elbe.relations.cloud.azure;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.services.ICloudProvider;
import org.elbe.relations.services.ICloudProviderConfig;
import org.elbe.relations.utility.AbstractCloudProviderConfig;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonObject;

/** The MS Azure Cloud Provider Configuration component.
 *
 * @author lbenno */
@Component
public class AzureProviderConfig extends AbstractCloudProviderConfig implements ICloudProviderConfig {
    protected static final String KEY_CONNECT = "AzureConnectionString";
    private static final String PATTERN_PART1 = "DefaultEndpointsProtocol";
    private static final String PATTERN_PART2 = "AccountName";
    private static final String PATTERN_PART3 = "AccountKey";
    private static final String PATTERN_PART4 = "EndpointSuffix=core.windows.net";
    private static final String PATTERN = PATTERN_PART1 + "=(.+?);" + PATTERN_PART2 + "=(.+?);" + PATTERN_PART3
            + "=(\\p{ASCII}+?);" + PATTERN_PART4;
    private static final String PATTERN_TMPL = PATTERN_PART1 + "=%s;" + PATTERN_PART2 + "=%s;" + PATTERN_PART3 + "=%s;"
            + PATTERN_PART4;
    private static final String PROTOCOL_HTTPS = "https";
    private static final String PROTOCOL_HTTP = "http";

    private Button protocolHttps;
    private Button protocolHttp;
    private Text accountName;
    private Text accountKey;
    private Text connectionString;
    private Focus focus = Focus.OUT;

    private enum Focus {
        OUT, IN_ACCOUNT_FIELD, IN_PASTE_FIELD;
    }

    @Override
    public String getName() {
        return "MS Azure";
    }

    @Override
    public Control createConfigContents(final Group parent, final Consumer<Boolean> signalIsValid) {
        createLabel(parent, "Protocol:");
        final Composite protocolCont = new Composite(parent, SWT.NONE);
        protocolCont.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        this.protocolHttps = createProtocolBtn(protocolCont, PROTOCOL_HTTPS, signalIsValid);
        this.protocolHttps.setSelection(true);
        this.protocolHttp = createProtocolBtn(protocolCont, PROTOCOL_HTTP, signalIsValid);

        this.accountName = createLabelText(parent, "Account Name:");
        this.accountName.addModifyListener(event -> handleTextInput(signalIsValid));
        createLabel(parent, "Account Key:");
        this.accountKey = createText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        this.accountKey.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 30).create());
        this.accountKey.addModifyListener(event -> handleTextInput(signalIsValid));

        final Pattern pattern = Pattern.compile(PATTERN);
        final Label connectionLbl = createLabel(parent, "or paste Connection String:");
        connectionLbl.setLayoutData(GridDataFactory.swtDefaults().span(2, 1).create());
        this.connectionString = createText(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
        this.connectionString.setLayoutData(
                GridDataFactory.fillDefaults().span(2, 1).grab(true, false).hint(SWT.DEFAULT, 50).create());
        this.connectionString
        .addModifyListener(event -> handlePaste(((Text) event.getSource()).getText(), pattern, signalIsValid));
        return parent;
    }

    private void handleTextInput(final Consumer<Boolean> signalIsValid) {
        if (this.focus == Focus.IN_PASTE_FIELD) {
            return;
        }

        this.focus = Focus.IN_ACCOUNT_FIELD;
        this.connectionString
        .setText(String.format(PATTERN_TMPL, this.protocolHttps.getSelection() ? PROTOCOL_HTTPS : PROTOCOL_HTTP,
                this.accountName.getText(), this.accountKey.getText()));
        this.focus = Focus.OUT;
        signalIsValid.accept(isValid());
    }

    private void handlePaste(final String connectionString, final Pattern pattern,
            final Consumer<Boolean> signalIsValid) {
        if (this.focus == Focus.IN_ACCOUNT_FIELD) {
            return;
        }

        this.focus = Focus.IN_PASTE_FIELD;
        final Matcher matcher = pattern.matcher(connectionString);
        if (matcher.find()) {
            if (PROTOCOL_HTTPS.equals(matcher.group(1))) {
                this.protocolHttps.setSelection(true);
                this.protocolHttp.setSelection(false);
            } else {
                this.protocolHttps.setSelection(false);
                this.protocolHttp.setSelection(true);
            }

            final String accName = matcher.group(2);
            this.accountName.setText(accName == null ? "" : accName);

            final String accKey = matcher.group(3);
            this.accountKey.setText(accKey == null ? "" : accKey);
        }
        this.focus = Focus.OUT;
        signalIsValid.accept(isValid());
    }

    private Button createProtocolBtn(final Composite protocolCont, final String text,
            final Consumer<Boolean> signalIsValid) {
        final Button btn = new Button(protocolCont, SWT.RADIO);
        btn.setText(text);
        btn.setLayoutData(GridDataFactory.swtDefaults().create());
        btn.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> handleTextInput(signalIsValid)));
        return btn;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.protocolHttp.setEnabled(enabled);
        this.protocolHttps.setEnabled(enabled);
        this.accountName.setEnabled(enabled);
        this.accountKey.setEnabled(enabled);
        this.connectionString.setEnabled(enabled);
    }

    @Override
    public JsonObject getConfig() {
        final JsonObject json = new JsonObject();
        json.addProperty(KEY_CONNECT, this.connectionString.getText());
        return json;
    }

    @Override
    public void initialize(final JsonObject values) {
        setChecked(KEY_CONNECT, values, this.connectionString);
    }

    @Override
    public ICloudProvider getProvider() {
        return new AzureProvider();
    }

    @Override
    public boolean isValid() {
        final String connectString = this.connectionString.getText();
        if (connectString.isEmpty()) {
            return false;
        }
        final Matcher matcher = Pattern.compile(PATTERN).matcher(connectString);
        return matcher.find();
    }

}
