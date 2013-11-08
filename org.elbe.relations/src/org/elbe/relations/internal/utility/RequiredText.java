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
package org.elbe.relations.internal.utility;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsMessages;

/**
 * Utility class wrapping a required <code>Text</code>. The text field shows a
 * decoration <code>FieldDecorationRegistry.DEC_REQUIRED</code> indicating input
 * is required. In case of leaving of focus with empty field, an error mark (
 * <code>FieldDecorationRegistry.DEC_ERROR</code>) is displayed.
 * 
 * @author Luthiger 
 * @see Text
 */
public class RequiredText {
	private Text text;
	private ControlDecoration requiredDeco;
	private ControlDecoration errorDeco;

	/**
	 * RequiredText constructor
	 * 
	 * @param inContainer
	 *            Composite
	 * @param inNumColumns
	 *            int Number of columns the widget spans.
	 */
	public RequiredText(final Composite inContainer, final int inNumColumns) {
		init(inContainer, inNumColumns);
	}

	/**
	 * RequiredText constructor creating a label followed with a required text
	 * field.
	 * 
	 * @param inLabelValue
	 *            String
	 * @param inContainer
	 *            Composite
	 * @param inNumColumns
	 *            int Number of columns the widget spans.
	 */
	public RequiredText(final String inLabelValue, final Composite inContainer,
			final int inNumColumns) {
		createLabel(inLabelValue, inContainer);
		init(inContainer, inNumColumns - 1);
	}

	private void init(final Composite inContainer, final int inNumColumns) {
		text = new Text(inContainer, SWT.BORDER | SWT.SINGLE);
		final GridData lLayout = new GridData(SWT.FILL, SWT.NULL, true, false,
				inNumColumns, SWT.NULL);
		lLayout.horizontalIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		text.setLayoutData(lLayout);
		requiredDeco = FormUtility.addDecorationRequired(text);
		errorDeco = FormUtility.addDecorationError(text);
		errorDeco.hide();
		errorDeco.setDescriptionText(RelationsMessages
				.getString("RequiredText.deco.notempty")); //$NON-NLS-1$
	}

	public void addFocusListener(final FocusListener inListener) {
		text.addFocusListener(inListener);
	}

	public void addModifyListener(final ModifyListener inListener) {
		text.addModifyListener(inListener);
	}

	public void setText(final String inText) {
		text.setText(inText);
	}

	public String getText() {
		return text == null || text.isDisposed() ? "" : text.getText();
	}

	public boolean setFocus() {
		return text.setFocus();
	}

	/**
	 * Shows or hides error decoration on this field.
	 * 
	 * @param inShow
	 *            boolean
	 */
	public void setErrorDecoration(final boolean inShow) {
		if (inShow) {
			requiredDeco.hide();
			errorDeco.show();
		} else {
			errorDeco.hide();
			requiredDeco.show();
		}
	}

	private Label createLabel(final String inLabelValue,
			final Composite inContainer) {
		final Label outLabel = new Label(inContainer, SWT.NULL);
		outLabel.setText(inLabelValue);

		final GridData lData = new GridData(SWT.FILL, SWT.NULL, false, false,
				1, SWT.NULL);
		lData.widthHint = (int) (outLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x * 1.2);
		outLabel.setLayoutData(lData);
		return outLabel;
	}

}
