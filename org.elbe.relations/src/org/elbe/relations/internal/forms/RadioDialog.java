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

import java.util.Collection;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Displays Dialog window to make a (radio) selection.
 * 
 * @author Luthiger Created on 14.11.2009
 */
public class RadioDialog extends Dialog {

	private final String title;
	private final String message;
	private final int[] options;
	private final String[] labels;
	private final int initialOption;
	private final Collection<Button> buttons = new Vector<Button>();
	private int result = -1;

	/**
	 * Constructor
	 * 
	 * @param inParentShell
	 *            Shell
	 * @param inTitle
	 *            String the dialog window's title
	 * @param inMessage
	 *            String the message displayed above the radio selection
	 * @param inOptions
	 *            int[] array of values to select
	 * @param inOptionInitial
	 *            int the initially selected option
	 * @param inLabels
	 *            String[] array of option labels
	 */
	public RadioDialog(final Shell inParentShell, final String inTitle,
			final String inMessage, final int[] inOptions,
			final int inOptionInitial, final String[] inLabels) {
		super(inParentShell);
		title = inTitle;
		message = inMessage;
		options = inOptions;
		initialOption = inOptionInitial;
		labels = inLabels;
	}

	@Override
	protected Control createDialogArea(final Composite inParent) {
		if (title != null) {
			getShell().setText(title);
		}
		final Composite outComposite = (Composite) super
				.createDialogArea(inParent);
		final GridLayout lLayout = new GridLayout();
		lLayout.marginWidth = 15;
		lLayout.marginHeight = 15;
		outComposite.setLayout(lLayout);
		if (message != null) {
			final Label lLabel = new Label(outComposite, SWT.WRAP);
			lLabel.setText(message + "\n"); //$NON-NLS-1$
		}
		int i = 0;
		for (final int lOption : options) {
			final Button lButton = new Button(outComposite, SWT.RADIO);
			final GridData lData = new GridData();
			lData.horizontalIndent = 15;
			lButton.setLayoutData(lData);
			lButton.setData(lOption);
			lButton.setText(labels[i++]);
			if (lOption == initialOption) {
				lButton.setSelection(true);
			}
			buttons.add(lButton);
		}
		return outComposite;
	}

	@Override
	protected void buttonPressed(final int inButtonId) {
		if (inButtonId == OK) {
			for (final Button lButton : buttons) {
				if (lButton.getSelection()) {
					result = ((Integer) lButton.getData()).intValue();
					break;
				}
			}
		}
		super.buttonPressed(inButtonId);
	}

	/**
	 * @return int the selected option.
	 */
	public int getResult() {
		return result;
	}

}
