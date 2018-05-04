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
package org.elbe.relations.utility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.gson.JsonObject;

/**
 * Base class for classes implementing the <code>ICloudProviderConfig</code>
 * interface. This class provides basic functionality to render configuration
 * components on the preference page.
 *
 * @author lbenno
 */
public abstract class AbstractCloudProviderConfig {
	private static final int WIDTH_HINT = 75; // width hint for labels

	/**
	 * Creates a label text field in a two column grid layout.
	 *
	 * @param parent
	 *            {@link Composite} the parent component
	 * @param label
	 *            String the label text
	 * @return {@link Text} the created text component
	 */
	protected Text createLabelText(final Composite parent, final String label) {
		createLabel(parent, label);
		return createText(parent);
	}

	private Text createText(final Composite parent) {
		final Text outText = new Text(parent, SWT.BORDER | SWT.SINGLE);
		outText.setLayoutData(createGridData());
		return outText;
	}

	/**
	 * Utility method that creates a label instance and sets the default layout
	 * data.
	 *
	 * @param parent
	 *            the inParent for the new label
	 * @param label
	 *            the text for the new label
	 * @return the new label
	 */
	protected Label createLabel(final Composite parent, final String label) {
		final Label outLabel = new Label(parent, SWT.LEFT);
		outLabel.setText(label);
		final GridData lData = new GridData();
		lData.horizontalAlignment = GridData.FILL;
		lData.widthHint = getWidthHint();
		outLabel.setLayoutData(lData);
		return outLabel;
	}

	protected GridData createGridData() {
		return new GridData(SWT.FILL, SWT.CENTER, true, false);
	}

	/**
	 * @return int the widht hint for the labels, defaults to
	 *         <code>WIDTH_HINT</code>
	 */
	protected int getWidthHint() {
		return WIDTH_HINT;
	}

	/**
	 * Initializes the passed text widget with the values passed as JsonObject.
	 *
	 * @param key
	 *            String
	 * @param values
	 *            {@link JsonObject} (possibly) containing the value
	 * @param field
	 *            {@link Text} the text widget to initialize
	 */
	protected void setChecked(final String key, final JsonObject values, final Text field) {
		if (values.has(key)) {
			field.setText(values.get(key).getAsString());
		}
	}

}
