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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.TextHome;
import org.elbe.relations.internal.forms.AbstractEditForm;
import org.elbe.relations.internal.forms.FormText;
import org.elbe.relations.internal.wizards.interfaces.IItemWizardPage;
import org.elbe.relations.models.ItemAdapter;
import org.hip.kernel.bom.DomainObject;

import jakarta.inject.Inject;

/**
 * Page in the text's edit dialog.
 *
 * @author Benno Luthiger Created on 25.04.2006
 */
@SuppressWarnings("restriction")
public class TextEditWizardPage extends AbstractRelationsWizardPage implements
IItemWizardPage {
    private FormText form;
    private ItemAdapter model;

    @Inject
    private IEclipseContext context;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private Logger log;

    /**
     * TextEditWizardPage constructor.
     */
    public TextEditWizardPage() {
        super("TextEditWizardPage"); //$NON-NLS-1$
        setTitle(RelationsMessages.getString("TextEditWizardPage.view.title")); //$NON-NLS-1$
        setDescription(RelationsMessages
                .getString("TextEditWizardPage.view.msg")); //$NON-NLS-1$
        setImageDescriptor(RelationsImages.WIZARD_EDIT_TEXT.getDescriptor());
    }

    /**
     * Friendly model setter.
     *
     * @param inModel
     *            {@link ItemAdapter}
     */
    void setModel(final ItemAdapter inModel) {
        this.model = inModel;
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(final Composite inParent) {
        this.form = FormText.createFormText(inParent, true, this.context);
        configureForm(this.form);

        try {
            final DomainObject lModel = (DomainObject) this.model.getItem();
            this.form.initialize(
                    Integer.parseInt(lModel.get(TextHome.KEY_TYPE).toString()),
                    this.model.getTitle(), getField(lModel.get(TextHome.KEY_TEXT)),
                    getField(lModel.get(TextHome.KEY_AUTHOR)),
                    getField(lModel.get(TextHome.KEY_COAUTHORS)),
                    getField(lModel.get(TextHome.KEY_SUBTITLE)),
                    getField(lModel.get(TextHome.KEY_YEAR)),
                    getField(lModel.get(TextHome.KEY_PUBLICATION)),
                    getField(lModel.get(TextHome.KEY_PAGES)),
                    getField(lModel.get(TextHome.KEY_VOLUME)),
                    getField(lModel.get(TextHome.KEY_NUMBER)),
                    getField(lModel.get(TextHome.KEY_PUBLISHER)),
                    getField(lModel.get(TextHome.KEY_PLACE)),
                    this.model.getCreated());
        }
        catch (final Exception exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

    /**
     * @see IItemWizardPage#save()
     */
    @Override
    public void save() throws BOMException {
        if (!this.form.getDirty()) {
            return;
        }

        ((AbstractText) this.model.getItem()).save(this.form.getTextTitle(),
                this.form.getTextText(), Integer.valueOf(this.form.getTextType()),
                this.form.getAuthorName(), this.form.getCoAuthorName(),
                this.form.getSubTitle(), this.form.getPublisher(), this.form.getYear(),
                this.form.getJournal(), this.form.getPages(),
                Integer.valueOf(this.form.getArticleVolume()),
                Integer.valueOf(this.form.getArticleNumber()), this.form.getLocation());

        this.eventBroker.post(
                RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT,
                this.model);
    }

    @Override
    protected AbstractEditForm getForm() {
        return this.form;
    }

}
