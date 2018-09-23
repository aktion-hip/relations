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

import java.io.IOException;
import java.util.function.BiConsumer;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.elbe.relations.services.ICloudProviderConfigurationHelper;
import org.elbe.relations.utility.Feedback;
import org.osgi.service.component.annotations.Component;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.json.JsonReadException;
import com.google.gson.JsonObject;

/** Implement the steps to create a Dropbox access token through registering the Relations app and receive the needed
 * access code.
 *
 * @author lbenno */
@SuppressWarnings("restriction")
@Component
public class DropboxConfigurationHelper implements ICloudProviderConfigurationHelper {
    private static final String TEMPL = "<form>" + //$NON-NLS-1$
            "<p><b>%1$s</b></p>" + //$NON-NLS-1$
            "<li style=\"text\" value=\"1.\">%2$s<br />%3$s</li>" + //$NON-NLS-1$
            "<li style=\"text\" value=\"2.\">%4$s</li>" + //$NON-NLS-1$
            "<li style=\"text\" value=\"3.\">%5$s</li>" + //$NON-NLS-1$
            "</form>"; //$NON-NLS-1$
    private static final String MSG1 = Messages.getString("DropboxConfigurationHelper.msg1"); //$NON-NLS-1$
    private static final String MSG2 = Messages.getString("DropboxConfigurationHelper.msg2"); //$NON-NLS-1$
    private static final String MSG4 = Messages.getString("DropboxConfigurationHelper.msg3"); //$NON-NLS-1$
    private static final String MSG5 = Messages.getString("DropboxConfigurationHelper.msg4"); //$NON-NLS-1$

    private Text code;

    @Override
    public String getName() {
        return "Dropbox"; //$NON-NLS-1$
    }

    @Override
    public void createDialogArea(final Composite parent, final BiConsumer<JsonObject, Feedback> store,
            final Logger log) {
        try {
            final DropboxWrapper wrapper = new DropboxWrapper();

            final FormText steps = new FormText(parent, SWT.NO_FOCUS);
            steps.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            steps.setText(String.format(TEMPL, MSG1, MSG2, wrapper.getAuthorizeUrl(), MSG4, MSG5), true,
                    true);
            steps.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(final HyperlinkEvent event) {
                    Program.launch(event.getHref().toString());
                }
            });

            this.code = new Text(parent, SWT.BORDER | SWT.SINGLE);
            this.code.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

            final Button process = new Button(parent, SWT.PUSH);
            process.setText(Messages.getString("DropboxConfigurationHelper.btn.lbl")); //$NON-NLS-1$
            process.setEnabled(false);

            this.code.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(final ModifyEvent event) {
                    process.setEnabled(!((Text) event.widget).getText().isEmpty());
                }
            });

            final Label feedback = new Label(parent, SWT.NONE);
            feedback.setText(Messages.getString("DropboxConfigurationHelper.feedback.lbl")); //$NON-NLS-1$
            feedback.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            feedback.setVisible(false);

            final Text token = new Text(parent, SWT.NONE);
            token.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
            token.setEditable(false);
            token.setVisible(false);

            // button process clicked
            process.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
                try {
                    store.accept(wrapper.finishFromCode(this.code.getText().trim(), token),
                            new Feedback(true, Messages.getString("DropboxConfigurationHelper.feedback.success"))); //$NON-NLS-1$
                    token.setVisible(true);
                    feedback.setVisible(true);
                } catch (final DbxException exc) {
                    store.accept(new JsonObject(),
                            new Feedback(false, Messages.getString("DropboxConfigurationHelper.feedback.error"))); //$NON-NLS-1$
                    log.error(exc, "Error encountered while processing the Dropbox code!"); //$NON-NLS-1$
                }
            }));

        } catch (IOException | JsonReadException exc1) {
            log.error(exc1, "Error encountered while preparing the Dropbox configuration!"); //$NON-NLS-1$
        }
    }

    // ---

    private static class DropboxWrapper {
        final DbxRequestConfig config = new DbxRequestConfig("relations-cloud/1.0"); //$NON-NLS-1$
        private final DbxWebAuth webAuth;
        private final String authorizeUrl;

        protected DropboxWrapper() throws IOException, JsonReadException {
            final DbxAppInfo appInfo = DbxAppInfo.Reader
                    .readFully(DropboxConfigurationHelper.class.getResourceAsStream("app.json")); //$NON-NLS-1$
            this.webAuth = new DbxWebAuth(this.config, appInfo);
            final DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder()
                    .withNoRedirect().build();
            this.authorizeUrl = this.webAuth.authorize(webAuthRequest);
        }

        protected String getAuthorizeUrl() {
            return this.authorizeUrl; // .replace("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        protected JsonObject finishFromCode(final String code, final Text token) throws DbxException {
            final DbxAuthFinish authFinish = this.webAuth.finishFromCode(code);
            token.setText(authFinish.getAccessToken());
            return createJson(authFinish.getAccessToken());
        }

        private JsonObject createJson(final String accessToken) {
            final JsonObject json = new JsonObject();
            json.addProperty(DropboxCloudProviderConfig.KEY_TOKEN, accessToken);
            return json;
        }

    }

}
