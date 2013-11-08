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
package org.elbe.relations.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Base class for preference pages used in Relations.
 * 
 * @author Luthiger
 */
public abstract class AbstractPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public AbstractPreferencePage() {
		super();
	}

	public AbstractPreferencePage(final String inTitle) {
		super(inTitle);
	}

	public AbstractPreferencePage(final String inTitle,
			final ImageDescriptor inImage) {
		super(inTitle, inImage);
	}

	/**
	 * @see PreferencePage#doGetPreferenceStore
	 */
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return null;
		// return
		// InstanceScope.INSTANCE.getNode(RelationsConstants.PREFERENCE_NODE);
	}

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
		lData.widthHint = convertWidthInCharsToPixels(22);
		outLabel.setLayoutData(lData);
		return outLabel;
	}

	protected Combo createCombo(final Composite inParent, final String[] inItems) {
		final Combo outCombo = new Combo(inParent, SWT.DROP_DOWN
				| SWT.READ_ONLY | SWT.SIMPLE);
		outCombo.setItems(inItems);
		outCombo.setLayoutData(createGridData());
		return outCombo;
	}

	protected GridData createGridData() {
		return new GridData(SWT.FILL, SWT.CENTER, true, false);
	}

	protected Combo createLabelCombo(final Composite inParent,
			final String inLabel, final String[] inItems) {
		createLabel(inParent, inLabel);
		return createCombo(inParent, inItems);
	}

	protected Text createLabelText(final Composite inParent,
			final String inLabel) {
		createLabel(inParent, inLabel);
		return createText(inParent);
	}

	private Text createText(final Composite inParent) {
		final Text outText = new Text(inParent, SWT.BORDER | SWT.SINGLE);
		outText.setTextLimit(5);
		outText.setLayoutData(createGridData());
		return outText;
	}

	protected void createSeparator(final Composite inParent, final int inColumns) {
		final Label lSeparator = new Label(inParent, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		lSeparator.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false,
				inColumns, 1));
	}

	/**
	 * @param inComposite
	 * @param inColumns
	 */
	protected void setLayout(final Composite inComposite, final int inColumns) {
		final GridLayout lLayout = new GridLayout();
		lLayout.numColumns = inColumns;
		lLayout.marginHeight = 0;
		lLayout.marginWidth = 0;
		inComposite.setLayout(lLayout);
	}

}
