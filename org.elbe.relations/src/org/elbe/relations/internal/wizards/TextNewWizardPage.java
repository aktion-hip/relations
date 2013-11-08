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
package org.elbe.relations.internal.wizards;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.LightWeightText;
import org.elbe.relations.data.bom.TextHome;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.bom.TextWithIcon;
import org.elbe.relations.internal.forms.AbstractEditForm;
import org.elbe.relations.internal.forms.FormText;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;
import org.elbe.relations.internal.wizards.interfaces.IItemWizardPage;
import org.hip.kernel.exc.VException;

/**
 * Form to collect the information to create a new text item.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class TextNewWizardPage extends AbstractRelationsWizardPage implements
		IItemWizardPage {

	@Inject
	private IEclipseContext context;

	@Inject
	private IDataService data;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private Logger log;

	private FormText form;

	public TextNewWizardPage() {
		super("TextWizardPage"); //$NON-NLS-1$
		setTitle(RelationsMessages.getString("TextNewWizardPage.view.title")); //$NON-NLS-1$
		setDescription(RelationsMessages
				.getString("TextNewWizardPage.view.msg")); //$NON-NLS-1$
		setImageDescriptor(RelationsImages.WIZARD_NEW_TEXT.getDescriptor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createControl(final Composite inParent) {
		getShell().setText(
				RelationsMessages.getString("TextNewWizardPage.shell.title")); //$NON-NLS-1$
		form = FormText.createFormText(inParent, false, context);
		configureForm(form);
		setPageComplete(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.AbstractRelationsWizardPage#getForm()
	 */
	@Override
	protected AbstractEditForm getForm() {
		return form;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.internal.wizards.interfaces.IItemWizardPage#save()
	 */
	@Override
	public void save() throws BOMException {
		final TextHome lHome = BOMHelper.getTextHome();
		lHome.setIndexer(RelationsIndexerWithLanguage
				.createRelationsIndexer(context));
		final AbstractText lText = lHome.newText(form.getTextTitle(),
				form.getTextText(), form.getAuthorName(),
				form.getCoAuthorName(), form.getSubTitle(), form.getYear(),
				form.getJournal(), form.getPages(),
				new Integer(form.getArticleVolume()),
				new Integer(form.getArticleNumber()), form.getPublisher(),
				form.getLocation(), new Integer(form.getTextType()));
		getRelationsSaveHelper().saveWith(lText);
		data.loadNew((LightWeightText) lText.getLightWeight());
		try {
			eventBroker.post(
					RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_MODEL,
					new TextWithIcon(lText, context));
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
	}

}
