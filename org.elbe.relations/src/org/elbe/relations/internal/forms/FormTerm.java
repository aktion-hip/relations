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

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.RequiredText;
import org.xml.sax.SAXException;

/**
 * Input form to enter or edit the data of a term item.
 * 
 * @author Benno Luthiger
 */
public final class FormTerm extends AbstractEditForm {
	private RequiredText titleText;
	private IStatus titleFieldStatus;
	private final IStatus titleEmpty = createErrorStatus(RelationsMessages
			.getString("FormTerm.error.msg")); //$NON-NLS-1$
	private boolean initialized = false;

	/**
	 * Factory method to create instances of <code>FormTerm</code>.
	 * 
	 * @param inParent
	 *            {@link Composite}
	 * @param inEditMode
	 *            boolean <code>true</code> if an existing item is to be edited,
	 *            <code>false</code> to create the content of a new item
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link FormTerm}
	 */
	public static FormTerm createFormTerm(final Composite inParent,
			final boolean inEditMode, final IEclipseContext inContext) {
		final FormTerm out = ContextInjectionFactory.make(FormTerm.class,
				inContext);
		out.setEditMode(inEditMode);
		out.initialize(inParent);
		return out;
	}

	private void initialize(final Composite inParent) {
		final int lNumColumns = 1;
		container = createComposite(inParent, lNumColumns, 9);

		titleText = new RequiredText(container, lNumColumns);
		titleText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				titleFieldStatus = Status.OK_STATUS;
				titleText.setErrorDecoration(false);
				notifyAboutUpdate(getStatuses());
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				if (((Text) inEvent.widget).getText().length() == 0) {
					titleFieldStatus = titleEmpty;
					titleText.setErrorDecoration(true);
				}
				notifyAboutUpdate(getStatuses());
			}
		});
		titleText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				if (!initialized)
					return;
				if (((Text) inEvent.widget).getText().length() != 0) {
					notifyAboutUpdate(getStatuses());
				}
			}
		});
		checkDirtyService.register(titleText);

		styledText = createStyledText(container);

		// we add indentation to align the text area with the required titleText
		final int lIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		setIndent(lIndent);

		titleFieldStatus = Status.OK_STATUS;
		checkDirtyService.register(styledText);

		addCreatedLabel(container, lIndent, lNumColumns);
	}

	public String getTermTitle() {
		return titleText.getText();
	}

	public String getTermText() {
		return styledText.getTaggedText();
	}

	/**
	 * @see org.elbe.relations.wizards.IEditForm#initialize()
	 */
	@Override
	public void initialize() {
		titleText.setFocus();
		checkDirtyService.freeze();
		initialized = true;
	}

	/**
	 * Initialize the input fields with the values to edit.
	 * 
	 * @param inTitle
	 *            String
	 * @param inText
	 *            String
	 * @param inCreated
	 *            String
	 * @throws SAXException
	 * @throws IOException
	 */
	public void initialize(final String inTitle, final String inText,
			final String inCreated) throws IOException, SAXException {
		titleText.setText(inTitle);
		styledText.setTaggedText(inText);
		setCreatedInfo(inCreated);
		initialize();
	}

	@Override
	protected IStatus[] getStatuses() {
		return new IStatus[] { titleFieldStatus };
	}

	@Override
	public boolean getPageComplete() {
		return titleText.getText().length() != 0;
	}

}
