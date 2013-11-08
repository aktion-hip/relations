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
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.internal.forms.AbstractEditForm;
import org.elbe.relations.internal.forms.FormPerson;
import org.elbe.relations.internal.wizards.interfaces.IItemWizardPage;
import org.elbe.relations.models.ItemAdapter;
import org.hip.kernel.bom.DomainObject;

/**
 * Page in the person's edit dialog.
 * 
 * @author Benno Luthiger Created on 27.04.2006
 */
@SuppressWarnings("restriction")
public class PersonEditWizardPage extends AbstractRelationsWizardPage implements
		IItemWizardPage {
	private FormPerson form;
	private ItemAdapter model;

	@Inject
	private IEclipseContext context;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private Logger log;

	/**
	 * PersonEditWizardPage constructor.
	 */
	public PersonEditWizardPage() {
		super("PersonEditWizardPage"); //$NON-NLS-1$
		setTitle(RelationsMessages.getString("PersonEditWizardPage.view.title")); //$NON-NLS-1$
		setDescription(RelationsMessages
				.getString("PersonEditWizardPage.view.msg")); //$NON-NLS-1$
		setImageDescriptor(RelationsImages.WIZARD_EDIT_PERSON.getDescriptor());
	}

	/**
	 * Friendly model setter.
	 * 
	 * @param inModel
	 *            {@link ItemAdapter}
	 */
	void setModel(final ItemAdapter inModel) {
		model = inModel;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(final Composite inParent) {
		form = FormPerson.createFormPerson(inParent, true, context);
		configureForm(form);

		try {
			final DomainObject lModel = (DomainObject) model.getItem();
			form.initialize(getField(lModel.get(PersonHome.KEY_NAME)),
					getField(lModel.get(PersonHome.KEY_FIRSTNAME)),
					getField(lModel.get(PersonHome.KEY_FROM)),
					getField(lModel.get(PersonHome.KEY_TO)),
					getField(lModel.get(PersonHome.KEY_TEXT)),
					model.getCreated());
		}
		catch (final Exception exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * @see IItemWizardPage#save()
	 */
	@Override
	public void save() throws BOMException {
		if (!form.getDirty())
			return;

		((AbstractPerson) model.getItem()).save(form.getPersonName(),
				form.getPersonFirstname(), form.getTextText(),
				form.getPersonFrom(), form.getPersonTo());

		eventBroker.post(
				RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT,
				model);
	}

	@Override
	protected AbstractEditForm getForm() {
		return form;
	}

}
