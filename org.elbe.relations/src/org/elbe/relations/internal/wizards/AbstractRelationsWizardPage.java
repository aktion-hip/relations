/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.wizard.WizardPage;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.internal.forms.AbstractEditForm;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.wizards.interfaces.IRelationsSaveHelper;

/**
 * This class provides general functionality for the wizard pages to create new
 * items and edit items respectively.
 *
 * @author Benno Luthiger
 */
public abstract class AbstractRelationsWizardPage extends WizardPage {
	private IRelationsSaveHelper relationsSave;

	/**
	 * AbstractRelationsWizardPage
	 *
	 * @param inPageName
	 *            String
	 */
	public AbstractRelationsWizardPage(final String inPageName) {
		super(inPageName);

		// empty implementation of the IRelationsSaveHelper
		relationsSave = new IRelationsSaveHelper() {
			@Override
			public void saveWith(final IItem inItem) throws BOMException {
				// intentionally left empty
			}
		};
	}

	/**
	 * Configures this wizard's input form.
	 *
	 * @param inForm
	 *            AbstractEditForm
	 */
	protected void configureForm(final AbstractEditForm inForm) {
		inForm.initialize();
		setControl(inForm.getControl());
	}

	/**
	 * Makes this wizard page updating on events sent by the form. The messages
	 * and button statuses are modified according to the statuses reported.
	 *
	 * @param inStatus
	 *            {@link IStatus}
	 */
	@Inject
	@Optional
	public void updateHandler(
	        @UIEventTopic(RelationsConstants.TOPIC_WIZARD_PAGE_STATUS) final IStatus inStatus) {
		setErrorMessage(FormUtility.getErrorMessage(inStatus));
		setPageComplete(getPageComplete());
	}

	private boolean getPageComplete() {
		return getForm().getPageComplete();
	}

	@Override
	public void dispose() {
		if (getForm() != null) {
			getForm().dispose();
		}
		super.dispose();
	}

	/**
	 * Returns the field content checked.
	 *
	 * @param inFieldContent
	 *            Object the result of model.get(KEY)
	 * @return String maybe an empty string if the object provided is null
	 */
	protected String getField(final Object inFieldContent) {
		if (inFieldContent == null) {
			return ""; //$NON-NLS-1$
		}
		return inFieldContent.toString();
	}

	/**
	 * Functionality implementing the <code>IRelationsSaveHelper</code> is
	 * processed after the new item is saved and, thus, initialized by a unique
	 * item ID. This item ID is needed to save the item's relations.
	 *
	 * @param inHelper
	 *            IRelationsSaveHelper
	 */
	public void setRelationsSaveHelper(final IRelationsSaveHelper inHelper) {
		relationsSave = inHelper;
	}

	protected IRelationsSaveHelper getRelationsSaveHelper() {
		return relationsSave;
	}

	/**
	 * Subclasses must override: Access to the concrete dialog's input form.
	 *
	 * @return AbstractEditForm
	 */
	protected abstract AbstractEditForm getForm();

}
