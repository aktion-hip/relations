/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.bom.TermWithIcon;
import org.elbe.relations.internal.forms.AbstractEditForm;
import org.elbe.relations.internal.forms.FormTerm;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;
import org.elbe.relations.internal.wizards.interfaces.IItemWizardPage;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/**
 * Form to collect the information to create a new term item.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class TermNewWizardPage extends AbstractRelationsWizardPage implements
IItemWizardPage {

    @Inject
    private IEclipseContext context;

    @Inject
    private IDataService data;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private Logger log;

    private FormTerm form;

    /**
     * TermNewWizardPage
     */
    public TermNewWizardPage() {
        super("TermNewWizardPage"); //$NON-NLS-1$
        setTitle(RelationsMessages.getString("TermNewWizardPage.view.title")); //$NON-NLS-1$
        setDescription(RelationsMessages.getString("TermNewWizardPage.view.msg")); //$NON-NLS-1$
        setImageDescriptor(RelationsImages.WIZARD_EDIT_TERM.getDescriptor());
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(final Composite parent) {
        this.form = FormTerm.createFormTerm(parent, false, this.context);
        configureForm(this.form);
    }

    @Override
    protected AbstractEditForm getForm() {
        return this.form;
    }

    @Override
    public void save() throws BOMException {
        final TermHome home = BOMHelper.getTermHome();
        home.setIndexer(RelationsIndexerWithLanguage.createRelationsIndexer(this.context));
        final AbstractTerm term = home.newTerm(this.form.getTermTitle(), this.form.getTermText());
        getRelationsSaveHelper().saveWith(term);
        this.data.loadNew((LightWeightTerm) term.getLightWeight());
        try {
            this.eventBroker.send(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_MODEL,
                    new TermWithIcon(term, this.context));
        }
        catch (final VException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }
}
