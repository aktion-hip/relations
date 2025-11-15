/*
 * Copyright (c) 2025 Benno Luthiger
 */
package org.elbe.relations.cloud.google;

import java.util.function.BiConsumer;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.elbe.relations.services.ICloudProviderConfigurationHelper;
import org.elbe.relations.utility.Feedback;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/** Implement the steps to create a Google Drive <code>credentials.json</code> through creating a OAuth client ID.
 *
 * @author lbenno */
@Component
public class GoogleConfigurationHelper implements ICloudProviderConfigurationHelper {
    private static final String TEMPL = """
            <form>
            <p><b>%1$s</b></p>
            <li style="text" value="1.">%2$s<br />%3$s</li>
            <li style="text" value="2.">%4$s</li>
            <li style="text" value="3.">%5$s</li>
            <li style="text" value="4.">%6$s</li>
            <li style="text" value="5.">%7$s</li>
            <li style="text" value="6.">%8$s</li>
            </form>
            """;
    private static final String MSG1 = Messages.getString("GoogleConfigurationHelper.msg1"); //$NON-NLS-1$
    private static final String MSG2 = Messages.getString("GoogleConfigurationHelper.msg2"); //$NON-NLS-1$
    private static final String MSG4 = Messages.getString("GoogleConfigurationHelper.msg3"); //$NON-NLS-1$
    private static final String MSG5 = Messages.getString("GoogleConfigurationHelper.msg4"); //$NON-NLS-1$
    private static final String MSG6 = Messages.getString("GoogleConfigurationHelper.msg5"); //$NON-NLS-1$
    private static final String MSG7 = Messages.getString("GoogleConfigurationHelper.msg6"); //$NON-NLS-1$
    private static final String MSG8 = Messages.getString("GoogleConfigurationHelper.msg7"); //$NON-NLS-1$
    private static final String CRED_URL = "https://console.cloud.google.com/apis/credentials";

    @Override
    public String getName() {
        return "Google Drive";
    }

    @Override
    public void createDialogArea(final Composite parent, final BiConsumer<JsonObject, Feedback> store, final Logger log) {
        final FormText steps = new FormText(parent, SWT.NO_FOCUS);
        steps.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        steps.setText(TEMPL.formatted(MSG1, MSG2, CRED_URL, MSG4, MSG5, MSG6, MSG7, MSG8), true, true);
        steps.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(final HyperlinkEvent event) {
                Program.launch(event.getHref().toString());
            }
        });

        final var code = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        code.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());


        final Button process = new Button(parent, SWT.PUSH);
        process.setText(Messages.getString("GoogleConfigurationHelper.btn.lbl")); //$NON-NLS-1$
        process.setEnabled(false);

        code.addModifyListener(event -> process.setEnabled(!((Text) event.widget).getText().isEmpty()));

        final var validator = new Validator(code);
        // button process clicked
        process.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> validator.validate(store)));
    }

    // ===

    private static class Validator {
        private static final String[] JSON_ENTRIES = { "client_id", "project_id", "client_secret" };

        private final Text code;

        protected Validator(final Text code) {
            this.code = code;
        }

        protected void validate(final BiConsumer<JsonObject, Feedback> store) {
            try {
                final var json = JsonParser.parseString(this.code.getText()).getAsJsonObject();
                store.accept(json, validate(json));
            } catch (final JsonSyntaxException | IllegalStateException exc) {
                store.accept(JsonParser.parseString("{}").getAsJsonObject(),
                        new Feedback(false, Messages.getString("GoogleConfigurationHelper.feedback.error")));
            }
        }

        private Feedback validate(final JsonObject json) {
            final JsonObject installed = json.get("installed").getAsJsonObject();
            if (installed == null) {
                return new Feedback(false, Messages.getString("GoogleConfigurationHelper.feedback.error"));
            }
            for (final String node : JSON_ENTRIES) {
                final JsonElement entry = installed.get(node);
                if (entry == null || entry.getAsString().isEmpty()) {
                    return new Feedback(false, Messages.getString("GoogleConfigurationHelper.feedback.error"));
                }
            }
            return new Feedback(true, Messages.getString("GoogleConfigurationHelper.feedback.ok"));
        }
    }

}
