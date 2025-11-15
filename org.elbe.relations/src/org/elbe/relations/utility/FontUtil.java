/*
 * Copyright (c) 2025 Benno Luthiger
 */
package org.elbe.relations.utility;

import java.util.Optional;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/** Wrapper around <code>JFaceResources</code> font registry.
 *
 * @author lbenno */
public class FontUtil {
    private static final String FONT_NAME_TMPL = "Relations_Font_%s";

    /** Retrieves the font with the specified size (pt) from the JFace font registry. If the font does not exist yet, it
     * is created and put to the registry for later use.
     *
     * @param size int font size in pt
     * @return Optional&lt;Font> the font with specified size */
    public static Optional<Font> createOrGetFont(final int size) {
        final String fontName = FONT_NAME_TMPL.formatted(size);
        final Font font = JFaceResources.getFontRegistry().get(fontName);
        if (font != null && font.getFontData()[0].getHeight() == size) {
            return Optional.of(font);
        }

        final Font dftFont = JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
        if (!dftFont.isDisposed()) {
            final FontData data = dftFont.getFontData()[0];
            data.setHeight(size);
            final Font newFont = new Font(Display.getCurrent(), data);
            JFaceResources.getFontRegistry().put(fontName, new FontData[] { data });
            return Optional.of(newFont);
        }
        return Optional.empty();
    }

    private FontUtil() {
        // prevent instantiation
    }

}
