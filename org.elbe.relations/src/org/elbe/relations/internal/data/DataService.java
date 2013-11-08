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
package org.elbe.relations.internal.data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

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
	private static Collection<ILightWeightModel> terms = new ArrayList<ILightWeightModel>();
	private static Collection<ILightWeightModel> texts = new ArrayList<ILightWeightModel>();
	private static Collection<ILightWeightModel> persons = new ArrayList<ILightWeightModel>();
	private static Collection<ILightWeightModel> combined;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private Logger log;

	@Inject
	private UISynchronize jobManager;

	@Inject
	private IEclipseContext context;

	@Inject
	private DBSettings dbSettings;

	@Override
	public Collection<ILightWeightModel> getTerms() {
		return terms;
	}

	@Override
	public Collection<ILightWeightModel> getTexts() {
		return texts;
	}

	@Override
	public Collection<ILightWeightModel> getPersons() {
		return persons;
	}

	@Override
	public Collection<ILightWeightModel> getAll() {
		return combined;
	}

	/**
	 * Adds the newly created term item to the relevant collections and sends a
	 * notification.
	 * 
	 * @param inTerm
	 *            {@link LightWeightTerm}
	 */
	@Override
	public void loadNew(final LightWeightTerm inTerm) {
		final ILightWeightModel lTerm = (inTerm instanceof ILightWeightModel) ? (ILightWeightModel) inTerm
				: new LightWeightTermWithIcon(inTerm);
		terms.add(lTerm);
		combined.add(lTerm);
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_RELOAD, "reload");
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_CREATED,
				new UniqueID(IItem.TERM, lTerm.getID()));
	}

	/**
	 * Adds the newly created text item to the relevant collections and sends a
	 * notification.
	 * 
	 * @param inText
	 *            {@link LightWeightText}
	 */
	@Override
	public void loadNew(final LightWeightText inText) {
		final ILightWeightModel lText = (inText instanceof ILightWeightModel) ? (ILightWeightModel) inText
				: new LightWeightTextWithIcon(inText);
		texts.add(lText);
		combined.add(lText);
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_RELOAD, "reload");
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_CREATED,
				new UniqueID(IItem.TEXT, lText.getID()));
	}

	/**
	 * Adds the newly created person item to the relevant collections and sends
	 * a notification.
	 * 
	 * @param inPerson
	 *            {@link LightWeightPerson}
	 */
	@Override
	public void loadNew(final LightWeightPerson inPerson) {
		final ILightWeightModel lPerson = (inPerson instanceof ILightWeightModel) ? (ILightWeightModel) inPerson
				: new LightWeightPersonWithIcon(inPerson);
		persons.add(lPerson);
		combined.add(lPerson);
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_RELOAD, "reload");
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_CREATED,
				new UniqueID(IItem.PERSON, lPerson.getID()));
	}

	@Override
	public IItemModel retrieveItem(final UniqueID inID) throws BOMException {
		try {
			switch (inID.itemType) {
			case IItem.TERM:
				return new TermWithIcon((AbstractTerm) BOMHelper.getTermHome()
						.getItem(inID.itemID), context);
			case IItem.TEXT:
				return new TextWithIcon((AbstractText) BOMHelper.getTextHome()
						.getItem(inID.itemID), context);
			case IItem.PERSON:
				return new PersonWithIcon((AbstractPerson) BOMHelper
						.getPersonHome().getItem(inID.itemID), context);
			default:
				return new TermWithIcon((AbstractTerm) BOMHelper.getTermHome()
						.getItem(inID.itemID), context);
			}
		}
		catch (final VException exc) {
			throw new BOMException(exc);
		}
	}

	/**
	 * Loads the data from the configured data store.
	 * 
	 * @param inEventTopic
	 *            String the event topic to post after data loading has been
	 *            done
	 */
	@Override
	public void loadData(final String inEventTopic) {
		jobManager.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					terms = retrieveData(BOMHelper.getCollectableTermHome(),
							new AlternativeFactory.TermModelFactory());
					texts = retrieveData(BOMHelper.getCollectableTextHome(),
							new AlternativeFactory.TextModelFactory());
					persons = retrieveData(
							BOMHelper.getCollectablePersonHome(),
							new AlternativeFactory.PersonModelFactory());
					combined = new ArrayList<ILightWeightModel>();
					combined.addAll(terms);
					combined.addAll(texts);
					combined.addAll(persons);
				}
				catch (final BOMException exc) {
					terms = Collections.emptyList();
					texts = Collections.emptyList();
					persons = Collections.emptyList();
					combined = Collections.emptyList();
					log.error(exc, exc.getMessage());
				}
				eventBroker.post(inEventTopic, "initialized");
			}
		});
	}

	@Override
	public int getNumberOfItems() {
		return combined == null ? 0 : combined.size();
	}

	@Override
	public String getDBName() {
		return dbSettings.getDBName();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<ILightWeightModel> retrieveData(
			final DomainObjectHome inHome,
			final AlternativeModelFactory inFactory) throws BOMException {
		try {
			final QueryResult lResult = inHome.select();
			final Collection<AlternativeModel> out = lResult.load(inFactory);
			return (Collection) out;
		}
		catch (final VException exc) {
			throw new BOMException(exc.getMessage());
		}
		catch (final SQLException exc) {
			throw new BOMException(exc.getMessage());
		}
	}

	@Override
	public void removeDeleted(final ILightWeightItem inItem) {
		combined.remove(inItem);
		switch (inItem.getItemType()) {
		case IItem.TERM:
			terms.remove(inItem);
			break;
		case IItem.TEXT:
			texts.remove(inItem);
			break;
		case IItem.PERSON:
			persons.remove(inItem);
			break;
		}
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_RELOAD, "reload");
		eventBroker.post(RelationsConstants.TOPIC_DB_CHANGED_DELETED,
				new UniqueID(inItem.getItemType(), inItem.getID()));
	}

	@Inject
	@Optional
	void changeDB(
			@EventTopic(RelationsConstants.TOPIC_DB_CHANGED_DB) final String inEvent) {
		loadData(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED);
	}

}
