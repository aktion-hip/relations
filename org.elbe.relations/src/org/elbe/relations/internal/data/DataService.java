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
package org.elbe.relations.internal.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.ICollectableHome;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.bom.LightWeightPerson;
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.data.bom.LightWeightText;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.bom.LightWeightPersonWithIcon;
import org.elbe.relations.internal.bom.LightWeightTermWithIcon;
import org.elbe.relations.internal.bom.LightWeightTextWithIcon;
import org.elbe.relations.internal.bom.PersonWithIcon;
import org.elbe.relations.internal.bom.TermWithIcon;
import org.elbe.relations.internal.bom.TextWithIcon;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.ILightWeightModel;
import org.hip.kernel.bom.AlternativeModel;
import org.hip.kernel.bom.AlternativeModelFactory;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.impl.AlternativeQueryResult;
import org.hip.kernel.exc.VException;

/**
 * Implementation of the <code>IDataService</code>. This class is put in the
 * context and, therefore, is intended to be injected to models and views.
 * <p>
 * An instance of this object can be injected into clients (as
 * <code>IDataService</code>), thus giving them the possibility to access
 * information about the actual data access.
 * </p>
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class DataService implements IDataService {
    private static final String RELOAD = "reload";

    private static Collection<AlternativeModel> terms = new ArrayList<>();
    private static Collection<AlternativeModel> texts = new ArrayList<>();
    private static Collection<AlternativeModel> persons = new ArrayList<>();
    private static Collection<AlternativeModel> combined;

    private final IEventBroker eventBroker;
    private final Logger log;
    private final UISynchronize jobManager;
    private final IEclipseContext context;
    private final DBSettings dbSettings;

    @Inject
    public DataService(final IEventBroker eventBroker, final Logger log, final UISynchronize jobManager,
            final IEclipseContext context, final DBSettings dbSettings) {
        this.eventBroker = eventBroker;
        this.log = log;
        this.jobManager = jobManager;
        this.context = context;
        this.dbSettings = dbSettings;
    }

    @Override
    public Collection<AlternativeModel> getTerms() {
        return terms;
    }

    @Override
    public Collection<AlternativeModel> getTexts() {
        return texts;
    }

    @Override
    public Collection<AlternativeModel> getPersons() {
        return persons;
    }

    @Override
    public Collection<AlternativeModel> getAll() {
        return combined;
    }

    /** Adds the newly created term item to the relevant collections and sends a notification.
     *
     * @param term {@link LightWeightTerm} */
    @Override
    public void loadNew(final LightWeightTerm term) {
        final ILightWeightModel ligthTerm = term instanceof final ILightWeightModel termModel ? termModel
                : new LightWeightTermWithIcon(term);
        terms.add(ligthTerm);
        combined.add(ligthTerm);
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_RELOAD, RELOAD);
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_CREATED, new UniqueID(IItem.TERM, ligthTerm.getID()));
    }

    /** Adds the newly created text item to the relevant collections and sends a notification.
     *
     * @param text {@link LightWeightText} */
    @Override
    public void loadNew(final LightWeightText text) {
        final ILightWeightModel lightText = text instanceof final ILightWeightModel textModel ? textModel
                : new LightWeightTextWithIcon(text);
        texts.add(lightText);
        combined.add(lightText);
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_RELOAD, RELOAD);
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_CREATED,
                new UniqueID(IItem.TEXT, lightText.getID()));
    }

    /** Adds the newly created person item to the relevant collections and sends a notification.
     *
     * @param person {@link LightWeightPerson} */
    @Override
    public void loadNew(final LightWeightPerson person) {
        final ILightWeightModel lightPerson = person instanceof final ILightWeightModel personModel ? personModel
                : new LightWeightPersonWithIcon(person);
        persons.add(lightPerson);
        combined.add(lightPerson);
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_RELOAD, RELOAD);
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_CREATED,
                new UniqueID(IItem.PERSON, lightPerson.getID()));
    }

    @Override
    public IItemModel retrieveItem(final UniqueID inID) throws BOMException {
        try {
            switch (inID.itemType) {
                case IItem.TERM:
                    return new TermWithIcon((AbstractTerm) BOMHelper.getTermHome()
                            .getItem(inID.itemID), this.context);
                case IItem.TEXT:
                    return new TextWithIcon((AbstractText) BOMHelper.getTextHome()
                            .getItem(inID.itemID), this.context);
                case IItem.PERSON:
                    return new PersonWithIcon((AbstractPerson) BOMHelper
                            .getPersonHome().getItem(inID.itemID), this.context);
                default:
                    return new TermWithIcon((AbstractTerm) BOMHelper.getTermHome()
                            .getItem(inID.itemID), this.context);
            }
        }
        catch (final VException exc) {
            throw new BOMException(exc);
        }
    }

    /** Loads the data from the configured data store.
     *
     * @param eventTopic String the event topic to post after data loading has been done */
    @Override
    public void loadData(final String eventTopic) {
        this.jobManager.asyncExec(() -> {
            try {
                terms = retrieveData(BOMHelper.getCollectableTermHome(),
                        new AlternativeFactory.TermModelFactory());
                texts = retrieveData(BOMHelper.getCollectableTextHome(),
                        new AlternativeFactory.TextModelFactory());
                persons = retrieveData(
                        BOMHelper.getCollectablePersonHome(),
                        new AlternativeFactory.PersonModelFactory());
                combined = new ArrayList<>();
                combined.addAll(terms);
                combined.addAll(texts);
                combined.addAll(persons);
            } catch (final BOMException exc) {
                terms = new ArrayList<>();
                texts = new ArrayList<>();
                persons = new ArrayList<>();
                combined = new ArrayList<>();
                DataService.this.log.error(exc, exc.getMessage());
            }
            DataService.this.eventBroker.post(eventTopic, "initialized"); //$NON-NLS-1$
        });
    }

    @Override
    public int getNumberOfItems() {
        return combined == null ? 0 : combined.size();
    }

    @Override
    public int getNumberOfRelations() {
        try {
            return BOMHelper.getRelationHome().getCount();
        }
        catch (org.hip.kernel.bom.BOMException | SQLException exc) {
            DataService.this.log.error(exc, exc.getMessage());
        }
        return 0;
    }

    @Override
    public int getNumberOfEvents() {
        try {
            return BOMHelper.getEventStoreHome().getCount();
        }
        catch (org.hip.kernel.bom.BOMException | SQLException exc) {
            DataService.this.log.error(exc, exc.getMessage());
        }
        return 0;
    }

    @Override
    public String getDBName() {
        return this.dbSettings.getDBName();
    }

    private Collection<AlternativeModel> retrieveData(final DomainObjectHome home,
            final AlternativeModelFactory factory) throws BOMException {
        if (home instanceof final ICollectableHome collectableHome) {
            collectableHome.setFactory(factory);
        }
        try {
            final QueryResult result = home.select(factory);
            if (result instanceof final AlternativeQueryResult altResult) {
                return altResult.getAlternativeModels();
            }
            return new ArrayList<>();
        }
        catch (VException | SQLException exc) {
            throw new BOMException(exc.getMessage());
        }

    }

    @Override
    public void removeDeleted(final ILightWeightItem item) {
        combined.remove(item);
        switch (item.getItemType()) {
            case IItem.TERM:
                terms.remove(item);
                break;
            case IItem.TEXT:
                texts.remove(item);
                break;
            case IItem.PERSON:
                persons.remove(item);
                break;
        }
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_RELOAD, RELOAD);
        this.eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_DELETED,
                new UniqueID(item.getItemType(), item.getID()));
    }

    @Inject
    @Optional
    void changeDB(
            @EventTopic(RelationsConstants.TOPIC_DB_CHANGED_DB) final String event) {
        loadData(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED);
    }

}
