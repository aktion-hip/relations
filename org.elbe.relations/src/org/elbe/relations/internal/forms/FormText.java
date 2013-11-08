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

import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.internal.utility.RequiredText;
import org.xml.sax.SAXException;

/**
 * Input form to enter or edit the data of a text item.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class FormText extends AbstractEditForm {
	private Combo typeCombo;
	private RequiredText authorText;
	private Text coauthorText;
	private Label coauthorLabel;
	private RequiredText titleText;
	private Text subtitleText;
	private Text yearText;
	private StyledText journalText;
	private Label journalLabel;
	private Text pagesText;
	private Text volumeText;
	private Text numberText;
	private Text publisherText;
	private Text locationText;
	private Label locationLabel;

	private IStatus titleFieldStatus;
	private IStatus authorFieldStatus;

	private StyledFieldHelper journalTextHelper;

	private final IStatus authorEmpty = createErrorStatus(RelationsMessages
			.getString("FormText.missing.author")); //$NON-NLS-1$
	private final IStatus titleEmpty = createErrorStatus(RelationsMessages
			.getString("FormText.missing.title")); //$NON-NLS-1$

	private boolean initialized = false;

	@Inject
	private Logger log;

	/**
	 * Factory method to create instances of <code>FormText</code>.
	 * 
	 * @param inParent
	 *            {@link Composite}
	 * @param inEditMode
	 *            boolean <code>true</code> if an existing item is to be edited,
	 *            <code>false</code> to create the content of a new item
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link FormText}
	 */
	public static FormText createFormText(final Composite inParent,
			final boolean inEditMode, final IEclipseContext inContext) {
		final FormText out = ContextInjectionFactory.make(FormText.class,
				inContext);
		out.setEditMode(inEditMode);
		out.initialize(inParent);
		return out;
	}

	private void initialize(final Composite inParent) {
		final int lNumColumns = 6;
		container = createComposite(inParent, lNumColumns, 3);

		createLabel(RelationsMessages.getString("FormText.lbl.type"), container); //$NON-NLS-1$
		typeCombo = createTypeCombo(container, lNumColumns);
		checkDirtyService.register(typeCombo);

		authorText = new RequiredText(
				RelationsMessages.getString("FormText.lbl.author"), container, lNumColumns); //$NON-NLS-1$
		authorText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				authorFieldStatus = Status.OK_STATUS;
				authorText.setErrorDecoration(false);
				notifyAboutUpdate(getStatuses());
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				if (((Text) inEvent.widget).getText().length() == 0) {
					authorFieldStatus = authorEmpty;
					authorText.setErrorDecoration(true);
				}
				notifyAboutUpdate(getStatuses());
			}
		});
		authorText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				if (!initialized)
					return;
				if (((Text) inEvent.widget).getText().length() != 0) {
					notifyAboutUpdate(getStatuses());
				}
			}
		});
		checkDirtyService.register(authorText);

		WidgetCreator lCreator = new WidgetCreator(
				RelationsMessages.getString("FormText.lbl.coauthor"), container, lNumColumns); //$NON-NLS-1$
		coauthorLabel = lCreator.getLabel();
		coauthorText = lCreator.getText();
		checkDirtyService.register(coauthorText);

		titleText = new RequiredText(
				RelationsMessages.getString("FormText.lbl.title"), container, lNumColumns); //$NON-NLS-1$
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

		lCreator = new WidgetCreator(
				RelationsMessages.getString("FormText.lbl.subtitle"), container, lNumColumns); //$NON-NLS-1$
		subtitleText = lCreator.getText();
		checkDirtyService.register(subtitleText);

		lCreator = new WidgetCreator(
				RelationsMessages.getString("FormText.lbl.year"), container, lNumColumns); //$NON-NLS-1$
		yearText = lCreator.getText();
		checkDirtyService.register(yearText);

		final StyledTextCreator lSTCreator = new StyledTextCreator(
				RelationsMessages.getString("FormText.lbl.journal"), container, lNumColumns); //$NON-NLS-1$
		journalLabel = lSTCreator.getLabel();
		journalText = lSTCreator.getText();
		journalTextHelper = new StyledFieldHelper(journalText, log);
		checkDirtyService.register(journalText);

		lCreator = new WidgetCreator(
				RelationsMessages.getString("FormText.lbl.pages"), container, 2); //$NON-NLS-1$
		pagesText = lCreator.getText();
		checkDirtyService.register(pagesText);
		lCreator = new WidgetCreator(
				RelationsMessages.getString("FormText.lbl.vol"), container, 2); //$NON-NLS-1$
		volumeText = lCreator.getText();
		checkDirtyService.register(volumeText);
		volumeText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(final VerifyEvent inEvent) {
				verifyNumeric(inEvent);
			}
		});
		lCreator = new WidgetCreator(
				RelationsMessages.getString("FormText.lbl.no"), container, 2); //$NON-NLS-1$
		numberText = lCreator.getText();
		checkDirtyService.register(numberText);
		numberText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(final VerifyEvent inEvent) {
				verifyNumeric(inEvent);
			}
		});
		lCreator = new WidgetCreator(
				RelationsMessages.getString("FormText.lbl.publisher"), container, lNumColumns); //$NON-NLS-1$
		publisherText = lCreator.getText();
		checkDirtyService.register(publisherText);
		lCreator = new WidgetCreator(
				RelationsMessages.getString("FormText.lbl.location"), container, lNumColumns); //$NON-NLS-1$
		locationLabel = lCreator.getLabel();
		locationText = lCreator.getText();
		checkDirtyService.register(locationText);

		styledText = createStyledText(container, lNumColumns, 70);
		checkDirtyService.register(styledText);
		authorFieldStatus = Status.OK_STATUS;
		titleFieldStatus = Status.OK_STATUS;

		// we have to align some fields
		final int lIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		((GridData) typeCombo.getLayoutData()).horizontalIndent = lIndent;
		((GridData) coauthorText.getLayoutData()).horizontalIndent = lIndent;
		((GridData) subtitleText.getLayoutData()).horizontalIndent = lIndent;
		((GridData) yearText.getLayoutData()).horizontalIndent = lIndent;
		((GridData) journalText.getLayoutData()).horizontalIndent = lIndent;
		((GridData) pagesText.getLayoutData()).horizontalIndent = lIndent;
		((GridData) publisherText.getLayoutData()).horizontalIndent = lIndent;
		((GridData) locationText.getLayoutData()).horizontalIndent = lIndent;

		addCreatedLabel(container, 0, lNumColumns);

		container.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				authorText.setFocus();
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				// do nothing
			}
		});
	}

	private Combo createTypeCombo(final Composite inContainer,
			final int inNumColumns) {
		final Combo lCombo = new Combo(inContainer, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		lCombo.setItems(new String[] {
				RelationsMessages.getString("FormText.entry.book"), RelationsMessages.getString("FormText.entry.article"), RelationsMessages.getString("FormText.entry.contribution"), RelationsMessages.getString("FormText.entry.webpage") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		lCombo.select(0);
		lCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				setInitState();
				setDisablePattern(((Combo) inEvent.getSource())
						.getSelectionIndex());
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent inEvent) {
				// Nothing to do.
			}
		});

		lCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final Label lLabel = new Label(inContainer, SWT.NULL);
		final GridData lData = new GridData();
		lData.horizontalSpan = inNumColumns - 2;
		lLabel.setLayoutData(lData);

		return lCombo;
	}

	private void setInitState() {
		// journalText.setEnabled(true);
		journalTextHelper.setEditable(true);
		pagesText.setEnabled(true);
		volumeText.setEnabled(true);
		numberText.setEnabled(true);
		publisherText.setEnabled(true);
		locationText.setEnabled(true);
		coauthorLabel.setText(RelationsMessages
				.getString("FormText.lbl.coauthor")); //$NON-NLS-1$
		journalLabel.setText(RelationsMessages
				.getString("FormText.lbl.journal")); //$NON-NLS-1$
		locationLabel.setText(RelationsMessages
				.getString("FormText.lbl.location")); //$NON-NLS-1$
	}

	private void setDisablePattern(final int inIndex) {
		switch (inIndex) {
		case AbstractText.TYPE_BOOK:
			// journalText.setEnabled(false);
			journalTextHelper.setEditable(false);
			pagesText.setEnabled(false);
			volumeText.setEnabled(false);
			numberText.setEnabled(false);
			journalTextHelper.removeListeners();
			break;
		case AbstractText.TYPE_ARTICLE:
			publisherText.setEnabled(false);
			locationText.setEnabled(false);
			journalTextHelper.removeListeners();
			break;
		case AbstractText.TYPE_CONTRIBUTION:
			pagesText.setEnabled(false);
			volumeText.setEnabled(false);
			numberText.setEnabled(false);
			coauthorLabel.setText(RelationsMessages
					.getString("FormText.lbl.editors")); //$NON-NLS-1$
			journalLabel.setText(RelationsMessages
					.getString("FormText.lbl.booktitle")); //$NON-NLS-1$
			journalTextHelper.removeListeners();
			break;
		case AbstractText.TYPE_WEBPAGE:
			volumeText.setEnabled(false);
			numberText.setEnabled(false);
			publisherText.setEnabled(false);
			journalLabel.setText(RelationsMessages
					.getString("FormText.lbl.webpage")); //$NON-NLS-1$
			locationLabel.setText(RelationsMessages
					.getString("FormText.lbl.accessed")); //$NON-NLS-1$
			journalTextHelper.addListeners();
			break;
		default:
			// journalText.setEnabled(false);
			journalTextHelper.setEditable(false);
			pagesText.setEnabled(false);
			volumeText.setEnabled(false);
			numberText.setEnabled(false);
			journalTextHelper.removeListeners();
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.forms.AbstractEditForm#getStatuses()
	 */
	@Override
	protected IStatus[] getStatuses() {
		return new IStatus[] { authorFieldStatus, titleFieldStatus };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.forms.AbstractEditForm#getPageComplete()
	 */
	@Override
	public boolean getPageComplete() {
		return authorText.getText().length() * titleText.getText().length() != 0;
	}

	private void verifyNumeric(final VerifyEvent inEvent) {
		if (inEvent.keyCode < 32) {
			inEvent.doit = true;
			return;
		}

		final String lOld = ((Text) inEvent.widget).getText();
		final String lNew = lOld.substring(0, inEvent.start) + inEvent.text
				+ lOld.substring(inEvent.end);
		try {
			Integer.parseInt(lNew);
			inEvent.doit = true;
		}
		catch (final Exception exc) {
			inEvent.doit = false;
		}
	}

	/**
	 * Initialize the input fields with the values to edit.
	 * 
	 * @param inType
	 *            int
	 * @param inTitle
	 *            String
	 * @param inText
	 *            String
	 * @param inAuthor
	 *            String
	 * @param inCoAuthor
	 *            String
	 * @param inSubTitle
	 *            String
	 * @param inYear
	 *            String
	 * @param inJournal
	 *            String
	 * @param inPages
	 *            String
	 * @param inVolume
	 *            String
	 * @param inNumber
	 *            String
	 * @param inPublisher
	 *            String
	 * @param inLocation
	 *            String
	 * @param inCreated
	 *            String
	 * @throws SAXException
	 * @throws IOException
	 */
	public void initialize(final int inType, final String inTitle,
			final String inText, final String inAuthor,
			final String inCoAuthor, final String inSubTitle,
			final String inYear, final String inJournal, final String inPages,
			final String inVolume, final String inNumber,
			final String inPublisher, final String inLocation,
			final String inCreated) throws IOException, SAXException {
		setInitState();
		typeCombo.select(inType);
		authorText.setText(inAuthor);
		coauthorText.setText(inCoAuthor);
		titleText.setText(inTitle);
		subtitleText.setText(inSubTitle);
		yearText.setText(inYear);
		journalText.setText(inJournal);
		pagesText.setText(inPages);
		volumeText.setText(inVolume);
		numberText.setText(inNumber);
		publisherText.setText(inPublisher);
		locationText.setText(inLocation);
		styledText.setTaggedText(inText);
		setCreatedInfo(inCreated);
		initialize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.internal.forms.AbstractEditForm#initialize()
	 */
	@Override
	public void initialize() {
		setDisablePattern(typeCombo.getSelectionIndex());
		checkDirtyService.freeze();
		initialized = true;
	}

	public String getTextText() {
		return styledText.getTaggedText();
	}

	public String getTextTitle() {
		return titleText.getText();
	}

	public String getSubTitle() {
		return subtitleText.getText();
	}

	public String getAuthorName() {
		return authorText.getText();
	}

	public String getCoAuthorName() {
		return coauthorText.getText();
	}

	public String getYear() {
		return yearText.getText();
	}

	public String getJournal() {
		return journalText.getText();
	}

	public String getPages() {
		return pagesText.getText();
	}

	public int getArticleVolume() {
		try {
			return Integer.parseInt(volumeText.getText());
		}
		catch (final NumberFormatException exc) {
			return 0;
		}
	}

	public int getArticleNumber() {
		try {
			return Integer.parseInt(numberText.getText());
		}
		catch (final NumberFormatException exc) {
			return 0;
		}
	}

	public String getPublisher() {
		return publisherText.getText();
	}

	public String getLocation() {
		return locationText.getText();
	}

	public int getTextType() {
		return typeCombo.getSelectionIndex();
	}

}
