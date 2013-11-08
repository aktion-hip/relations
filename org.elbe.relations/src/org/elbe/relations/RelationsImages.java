/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
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
	BOLD("style_bold.gif"), //$NON-NLS-1$
	ITALIC("style_italic.gif"), //$NON-NLS-1$
	UNDERLINE("style_underline.gif"), //$NON-NLS-1$
	UNORDERED("list_unordered.gif"), //$NON-NLS-1$
	NUMBERED("list_numbered.gif"), //$NON-NLS-1$
	LIST_LETTER_UPPER("list_letter_upper.gif"), //$NON-NLS-1$
	LIST_LETTER_LOWER("list_letter_lower.gif"), //$NON-NLS-1$
	WIZARD_EDIT_TERM("wiz_edit_term.png"), //$NON-NLS-1$
	WIZARD_EDIT_TEXT("wiz_edit_text.png"), //$NON-NLS-1$
	WIZARD_EDIT_PERSON("wiz_edit_person.png"), //$NON-NLS-1$
	WIZARD_NEW_TERM("wiz_new_term.png"), //$NON-NLS-1$
	WIZARD_NEW_TEXT("wiz_new_text.png"), //$NON-NLS-1$
	WIZARD_NEW_PERSON("wiz_new_person.png"), //$NON-NLS-1$
	WIZARD_NEW_DB("wiz_new_db.png"), //$NON-NLS-1$
	WIZARD_EDIT_DB("wiz_edit_db.png"); //$NON-NLS-1$

	private static String ICONS_DIR = "icons/"; //$NON-NLS-1$
	private final String name;

	RelationsImages(final String inName) {
		name = inName;
	}

	/**
	 * @return {@link ImageDescriptor} this item's image descriptor
	 */
	public ImageDescriptor getDescriptor() {
		return ImageDescriptor.createFromURL(Activator.getEntry(ICONS_DIR
				+ name));
	}

	/**
	 * @return {@link Image} this item's image
	 */
	public Image getImage() {
		try {
			final ImageRegistry lRegistry = JFaceResources.getImageRegistry();
			Image outImage = lRegistry.get(name);
			if (outImage == null) {
				outImage = createImage(name, getDescriptor());
				lRegistry.put(name, outImage);
			}
			return outImage;
		}
		catch (final NullPointerException exc) {
			// provide an empty image for testing purposes
			return new Image(null, 1, 1);
		}
	}

	private static Image createImage(final String inName,
			final ImageDescriptor inDescriptor) {
		ImageDescriptor lDescriptor = null;
		try {
			lDescriptor = ImageDescriptor.createFromURL(Activator
					.getEntry(ICONS_DIR + inName));
		}
		catch (final Exception exc) {
			lDescriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		return lDescriptor.createImage();
	}

}
