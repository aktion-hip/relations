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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.utility.RequiredText;
import org.xml.sax.SAXException;

/**
 * Input form to enter or edit the data of a person item.
 * 
 * @author Benno Luthiger
 */
public class FormPerson extends AbstractEditForm {
	private RequiredText nameText;
	private Label nameLabel;
	private Text firstnameText;
	private Text fromText;
	private Label fromLabel;
	private Text toText;
	private Label toLabel;
	private IStatus nameFieldStatus;
	private boolean initialized = false;

	/**
	 * Factory method to create instances of <code>FormPerson</code>.
	 * 
	 * @param inParent
	 *            {@link Composite}
	 * @param inEditMode
	 *            boolean <code>true</code> if an existing item is to be edited,
	 *            <code>false</code> to create the content of a new item
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link FormPerson}
	 */
	public static FormPerson createFormPerson(final Composite inParent,
			final boolean inEditMode, final IEclipseContext inContext) {
		final FormPerson out = ContextInjectionFactory.make(FormPerson.class,
				inContext);
		out.setEditMode(inEditMode);
		out.initialize(inParent);
		return out;
	}

	private void initialize(final Composite inParent) {
		final int lNumColumns = 4;
		container = createComposite(inParent, lNumColumns, 7);
		final int lLabelWidth = convertWidthInCharsToPixels(container, 6);

		final Composite lNameFill = createNameContainers(container, 2);
		nameLabel = createLabel(
				RelationsMessages.getString("FormPerson.lbl.name"), lNameFill); //$NON-NLS-1$
		nameText = new RequiredText(lNameFill, 1);
		nameText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				nameFieldStatus = Status.OK_STATUS;
				nameText.setErrorDecoration(false);
				notifyAboutUpdate(getStatuses());
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				if (((Text) inEvent.widget).getText().length() == 0) {
					nameFieldStatus = new Status(Status.ERROR, Activator
							.getSymbolicName(), 1, RelationsMessages
							.getString("FormPerson.missing.name"), null); //$NON-NLS-1$
					nameText.setErrorDecoration(true);
				}
				notifyAboutUpdate(getStatuses());
			}
		});
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				if (!initialized)
					return;
				if (((Text) inEvent.widget).getText().length() != 0) {
					notifyAboutUpdate(getStatuses());
				}
			}
		});
		checkDirtyService.register(nameText);

		final Composite lFirstnameFill = createNameContainers(container, 2);
		createLabel(
				RelationsMessages.getString("FormPerson.lbl.firstname"), lFirstnameFill); //$NON-NLS-1$
		firstnameText = createText(lFirstnameFill, 1);
		checkDirtyService.register(firstnameText);

		WidgetCreator lCreator = new WidgetCreator(
				RelationsMessages.getString("FormPerson.lbl.from"), container, 2); //$NON-NLS-1$
		fromLabel = lCreator.getLabel();
		setWidth(fromLabel, lLabelWidth);
		fromText = lCreator.getText();
		checkDirtyService.register(fromText);

		lCreator = new WidgetCreator(
				RelationsMessages.getString("FormPerson.lbl.to"), container, 2); //$NON-NLS-1$
		toLabel = lCreator.getLabel();
		setWidth(toLabel, lLabelWidth);
		toText = lCreator.getText();
		checkDirtyService.register(toText);

		styledText = createStyledText(container, lNumColumns, 70);
		checkDirtyService.register(styledText);
		nameFieldStatus = Status.OK_STATUS;

		// we have to align some fields
		final int lIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		((GridData) nameLabel.getLayoutData()).horizontalIndent = lIndent;
		((GridData) fromLabel.getLayoutData()).horizontalIndent = lIndent;
		setIndent(lIndent);

		addCreatedLabel(container, lIndent, lNumColumns);
	}

	private Composite createNameContainers(final Composite inParent,
			final int inNumColumns) {
		final Composite outComposite = new Composite(inParent, SWT.NULL);
		final GridLayout lLayout = new GridLayout(1, false);
		lLayout.marginWidth = 0;
		outComposite.setLayout(lLayout);
		outComposite.setLayoutData(new GridData(SWT.FILL, SWT.NULL, false,
				false, inNumColumns, SWT.NULL));
		return outComposite;
	}

	/**
	 * Initialize the input fields with the values to edit.
	 * 
	 * @param inName
	 *            String
	 * @param inFirstName
	 *            String
	 * @param inFrom
	 *            String
	 * @param inTo
	 *            String
	 * @param inText
	 *            String
	 * @param inCreated
	 *            String
	 * @throws SAXException
	 * @throws IOException
	 */
	public void initialize(final String inName, final String inFirstName,
			final String inFrom, final String inTo, final String inText,
			final String inCreated) throws IOException, SAXException {
		nameText.setText(inName);
		firstnameText.setText(inFirstName);
		fromText.setText(inFrom);
		toText.setText(inTo);
		styledText.setTaggedText(inText);
		setCreatedInfo(inCreated);
		initialize();
	}

	@Override
	public void initialize() {
		nameText.setFocus();
		checkDirtyService.freeze();
		initialized = true;
	}

	public String getTextText() {
		return styledText.getTaggedText();
	}

	public String getPersonName() {
		return nameText.getText();
	}

	public String getPersonFirstname() {
		return firstnameText.getText();
	}

	public String getPersonFrom() {
		return fromText.getText();
	}

	public String getPersonTo() {
		return toText.getText();
	}

	@Override
	protected IStatus[] getStatuses() {
		return new IStatus[] { nameFieldStatus };
	}

	@Override
	public boolean getPageComplete() {
		return nameText.getText().length() != 0;
	}

}
