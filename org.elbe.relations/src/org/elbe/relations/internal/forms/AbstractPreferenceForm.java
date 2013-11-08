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
package org.elbe.relations.internal.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsConstants;

/**
 * Abstract class providing basic functionalities for forms displayed on
 * preference pages.
 * 
 * @author Luthiger Created on 05.11.2006
 */
public abstract class AbstractPreferenceForm {

	/**
	 * Utility method that creates a label instance and sets the default layout
	 * data.
	 * 
	 * @param inParent
	 *            the inParent for the new label
	 * @param inText
	 *            the text for the new label
	 * @return the new label
	 */
	protected Label createLabel(final Composite inParent, final String inText) {
		final Label outLabel = new Label(inParent, SWT.LEFT);
		outLabel.setText(inText);
		final GridData lData = new GridData();
		lData.horizontalAlignment = GridData.FILL;
		outLabel.setLayoutData(lData);
		return outLabel;
	}

	protected Button createLabelCheckbox(final Composite inParent,
			final int inColumns, final String inLabel) {
		createLabel(inParent, inLabel);
		return createCheckbox(inParent);
	}

	protected Button createCheckbox(final Composite inParent) {
		final Button outCheckbox = new Button(inParent, SWT.CHECK);
		outCheckbox.setLayoutData(createGridData());
		return outCheckbox;
	}

	protected Combo createLabelCombo(final Composite inParent,
			final int inColumns, final String inLabel, final String[] inItems) {
		createLabel(inParent, inLabel);
		return createCombo(inParent, inItems, inColumns - 1);
	}

	protected Combo createCombo(final Composite inParent,
			final String[] inItems, final int inSpan) {
		final Combo outCombo = new Combo(inParent, SWT.DROP_DOWN
				| SWT.READ_ONLY | SWT.SIMPLE);
		outCombo.setItems(inItems);
		final GridData lGrid = createGridData();
		lGrid.horizontalSpan = inSpan;
		outCombo.setLayoutData(lGrid);
		return outCombo;
	}

	protected Text createLabeText(final Composite inParent,
			final int inColumns, final String inLabel, final int inStyle) {
		createLabel(inParent, inLabel);
		final Text outText = new Text(inParent, inStyle);
		final GridData lGrid = createGridData();
		lGrid.horizontalSpan = inColumns - 1;
		outText.setLayoutData(lGrid);
		return outText;
	}

	protected GridData createGridData() {
		return new GridData(SWT.FILL, SWT.CENTER, true, false);
	}

	protected void createSeparator(final Composite inParent, final int inColumns) {
		final Label lSeparator = new Label(inParent, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		lSeparator.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false,
				inColumns, 1));
	}

	/**
	 * Returns the index of the specified language ISO Code within the array of
	 * content languages.
	 * 
	 * @param inLanguageISO
	 *            String
	 * @param inLanguages
	 *            String[] available languages
	 * @return int index of the selected language within the array of selected
	 *         languages
	 */
	public static int getLanguageIndex(final String inLanguageISO,
			final String[] inLanguages) {
		int i = 0;
		int outDefault = 0;
		for (final String lLanguage : inLanguages) {
			if (inLanguageISO.equals(lLanguage)) {
				return i;
			}
			if (RelationsConstants.DFT_LANGUAGE.equals(lLanguage)) {
				outDefault = i;
			}
			i++;
		}
		return outDefault;
	}

}
