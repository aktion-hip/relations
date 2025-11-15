/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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
package org.elbe.relations;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

/**
 * Helper class to access the application's icons.
 *
 * @author Luthiger
 */
public enum RelationsImages {
    TERM("term.png"), //$NON-NLS-1$
    TEXT("text.png"), //$NON-NLS-1$
    PERSON("person.png"), //$NON-NLS-1$
    RELATIONS("relations.gif"), //$NON-NLS-1$
    SAVE("save.gif"), //$NON-NLS-1$
    ADD("add_obj.gif"), //$NON-NLS-1$
    SEARCH("search.gif"), //$NON-NLS-1$
    EDIT("edit.gif"), //$NON-NLS-1$
    DELETE("delete.png"), //$NON-NLS-1$
    DATA("data_open.gif"), //$NON-NLS-1$
    PRINT("print.gif"), //$NON-NLS-1$
    BOOKMARK("bkmrk_nav.gif"), //$NON-NLS-1$
    BOLD("format_bold_24dp.png"), //$NON-NLS-1$ - style_bold.gif
    ITALIC("format_italic_24dp.png"), //$NON-NLS-1$ - style_italic.gif
    UNDERLINE("format_underlined_24dp.png"), //$NON-NLS-1$ - style_underline.gif
    UNORDERED("format_list_bulleted_24dp.png"), //$NON-NLS-1$ - list_unordered.gif
    NUMBERED("format_list_numbered_24dp.png"), //$NON-NLS-1$ - list_numbered.gif
    LIST_LETTER_UPPER("list_letter_upper.gif"), //$NON-NLS-1$
    LIST_LETTER_LOWER("list_letter_lower.gif"), //$NON-NLS-1$
    WIZARD_EDIT_TERM("wiz_edit_term.png"), //$NON-NLS-1$
    WIZARD_EDIT_TEXT("wiz_edit_text.png"), //$NON-NLS-1$
    WIZARD_EDIT_PERSON("wiz_edit_person.png"), //$NON-NLS-1$
    WIZARD_NEW_TERM("wiz_new_term.png"), //$NON-NLS-1$
    WIZARD_NEW_TEXT("wiz_new_text.png"), //$NON-NLS-1$
    WIZARD_NEW_PERSON("wiz_new_person.png"), //$NON-NLS-1$
    WIZARD_NEW_DB("wiz_new_db.png"), //$NON-NLS-1$
    WIZARD_EDIT_DB("wiz_edit_db.png"), //$NON-NLS-1$
    WIZARD_NEW("new_wizban.png"), //$NON-NLS-1$
    WIZARD_IMPORT("import_wizban.png"), //$NON-NLS-1$
    WIZARD_EXPORT("export_wizban.png"), //$NON-NLS-1$
    WIZARD_IMPORT_XML("importzip_wiz.png"), //$NON-NLS-1$
    WIZARD_EXPORT_XML("exportzip_wiz.png"); //$NON-NLS-1$

    private static String ICONS_DIR = "icons/"; //$NON-NLS-1$ NOSONAR
    private final String name;

    RelationsImages(final String inName) {
        this.name = inName;
    }

    /**
     * @return {@link ImageDescriptor} this item's image descriptor
     */
    public ImageDescriptor getDescriptor() {
        return ImageDescriptor.createFromURL(Activator.getEntry(ICONS_DIR + this.name));
    }

    /**
     * @return {@link Image} this item's image
     */
    public Image getImage() {
        try {
            final ImageRegistry registry = JFaceResources.getImageRegistry();
            Image image = registry.get(this.name);
            if (image == null) {
                image = createImage(this.name);
                registry.put(this.name, image);
            }
            return image;
        }
        catch (final NullPointerException exc) {
            // provide an empty image for testing purposes
            return new Image(null, 1, 1);
        }
    }

    private static Image createImage(final String name) {
        try {
            final ImageDescriptor descriptor = ImageDescriptor.createFromURL(Activator.getEntry(ICONS_DIR + name));
            return descriptor.createImage();
        }
        catch (final Exception exc) {
            // intentionally left empty
        }
        return ImageDescriptor.getMissingImageDescriptor().createImage();
    }

}
