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
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.LightWeightPerson;
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.bom.PersonWithIcon;
import org.elbe.relations.internal.forms.AbstractEditForm;
import org.elbe.relations.internal.forms.FormPerson;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;
import org.elbe.relations.internal.wizards.interfaces.IItemWizardPage;
import org.hip.kernel.exc.VException;

/**
 * Form to collect the information to create a new person item.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class PersonNewWizardPage extends AbstractRelationsWizardPage implements
		IItemWizardPage {
	private FormPerson form;

	@Inject
	private IEclipseContext context;

	@Inject
	private IDataService data;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private Logger log;

	/**
	 * PersonNewWizardPage constructor.
	 */
	public PersonNewWizardPage() {
		super("PersonWizardPage"); //$NON-NLS-1$
		setTitle(RelationsMessages.getString("PersonNewWizardPage.view.title")); //$NON-NLS-1$
		setDescription(RelationsMessages
				.getString("PersonNewWizardPage.view.msg")); //$NON-NLS-1$
		setImageDescriptor(RelationsImages.WIZARD_NEW_PERSON.getDescriptor());
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
		form = FormPerson.createFormPerson(inParent, false, context);
		configureForm(form);
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
		final PersonHome lHome = BOMHelper.getPersonHome();
		lHome.setIndexer(RelationsIndexerWithLanguage
				.createRelationsIndexer(context));
		final AbstractPerson lPerson = lHome.newPerson(form.getPersonName(),
				form.getPersonFirstname(), form.getPersonFrom(),
				form.getPersonTo(), form.getTextText());
		getRelationsSaveHelper().saveWith(lPerson);
		data.loadNew((LightWeightPerson) lPerson.getLightWeight());
		try {
			eventBroker.post(
					RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_MODEL,
					new PersonWithIcon(lPerson, context));
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
	}

}
