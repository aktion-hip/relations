/*
 * Copyright (c) 2025 Benno Luthiger
 */
package org.elbe.relations.cloud.google;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author lbenno
 */
public class Messages {
    private static final String BUNDLE_NAME = "org.elbe.relations.cloud.google.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    /** @param key String the message key
     * @return String the localized message */
    public static String getString(final String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (final MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
