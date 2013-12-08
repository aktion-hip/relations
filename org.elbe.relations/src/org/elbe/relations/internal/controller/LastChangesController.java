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
package org.elbe.relations.internal.controller;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.internal.search.RetrievedChronologicalItem;
import org.hip.kernel.bom.AlternativeModel;
import org.hip.kernel.bom.AlternativeModelFactory;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.impl.OrderObjectImpl;
import org.hip.kernel.exc.VException;

/**
 * Controller for view that displays the last changes.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
@Creatable
@Singleton
public class LastChangesController {
	public static final String LAST_CHANGES_VIEW_TYPE = "relations.last.changes.view.type"; //$NON-NLS-1$

	private LastChangesType lastChangesType;
	private int maxEntries = 0;

	public enum LastChangesType {
		LAST_CREATED(
				TermHome.KEY_CREATED,
				"lastCreated", RelationsMessages.getString("LastChangesView.title.last.created"), new SortOnCreationDate()), //$NON-NLS-1$ //$NON-NLS-2$
		LAST_MODIFIED(
				TermHome.KEY_MODIFIED,
				"lastModified", RelationsMessages.getString("LastChangesView.title.last.modified"), new SortOnMutationDate()); //$NON-NLS-1$ //$NON-NLS-2$

		private final String fieldName;
		private final String value;
		private final String title;
		private final Comparator<AlternativeModel> comparator;

		LastChangesType(final String inFieldName, final String inValue,
				final String inTitle,
				final Comparator<AlternativeModel> inComparator) {
			fieldName = inFieldName;
			value = inValue;
			title = inTitle;
			comparator = inComparator;
		}

		public String getFieldName() {
			return fieldName;
		}

		public String getValue() {
			return value;
		}

		public boolean checkValue(final String inValue) {
			return value.equals(inValue);
		}

		public String getTitle() {
			return title;
		}
	};

	/**
	 * Returns the set of last changed items. Depending on the controller's
	 * actual <code>LastChangesType</code>, this set might be sorted
	 * <code>last changed</code> or <code>last modified</code>.
	 * 
	 * @return Collection<AlternativeModel> sorted set of last changed items.
	 * @throws VException
	 * @throws SQLException
	 */
	public Collection<AlternativeModel> getLastChangedItems()
			throws VException, SQLException {
		final int lMaxEntries = getMaxEntries();

		final List<AlternativeModel> outItems = new Vector<AlternativeModel>();
		outItems.addAll(getTerms(lastChangesType.getFieldName(), lMaxEntries));
		outItems.addAll(getTexts(lastChangesType.getFieldName(), lMaxEntries));
		outItems.addAll(getPersons(lastChangesType.getFieldName(), lMaxEntries));

		Collections.sort(outItems, lastChangesType.comparator);
		if (outItems.size() <= lMaxEntries) {
			return outItems;
		}

		// we have to resize the collection
		final List<AlternativeModel> outResized = new Vector<AlternativeModel>(
				lMaxEntries);
		int i = 0;
		for (final Iterator<AlternativeModel> lItems = outItems.iterator(); lItems
				.hasNext() && i++ < lMaxEntries;) {
			outResized.add(lItems.next());
		}
		return outResized;
	}

	@Inject
	void setViewState(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = LastChangesController.LAST_CHANGES_VIEW_TYPE) final String inLastChangeState) {
		if (inLastChangeState == null || inLastChangeState.isEmpty()) {
			lastChangesType = LastChangesType.LAST_CREATED;
		} else {
			lastChangesType = Boolean.parseBoolean(inLastChangeState) ? LastChangesType.LAST_CREATED
					: LastChangesType.LAST_MODIFIED;
		}
	}

	@Inject
	void setMaxEntries(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_MAX_LAST_CHANGED) final int inMaxEntries) {
		maxEntries = inMaxEntries;
	}

	/**
	 * We catch the <code>NullPointerException</code> for testing purposes.
	 */
	private int getMaxEntries() {
		try {
			return maxEntries == 0 ? RelationsConstants.DFT_MAX_LAST_CHANGED
					: maxEntries;
		}
		catch (final NullPointerException exc) {
			return RelationsConstants.DFT_MAX_LAST_CHANGED;
		}
	}

	private Collection<AlternativeModel> getTerms(final String inOrderKey,
			final int inMaxEntries) throws VException, SQLException {
		final QueryResult lResult = BOMHelper.getCollectableTermHome().select(
				createOrderObject(inOrderKey));
		return lResult.load(
				new ModelFactory(IItem.TERM, "TermID"), inMaxEntries); //$NON-NLS-1$
	}

	private Collection<AlternativeModel> getTexts(final String inOrderKey,
			final int inMaxEntries) throws VException, SQLException {
		final QueryResult lResult = BOMHelper.getCollectableTextHome().select(
				createOrderObject(inOrderKey));
		return lResult.load(
				new ModelFactory(IItem.TEXT, "TextID"), inMaxEntries); //$NON-NLS-1$
	}

	private Collection<AlternativeModel> getPersons(final String inOrderKey,
			final int inMaxEntries) throws VException, SQLException {
		final QueryResult lResult = BOMHelper.getCollectablePersonHome()
				.select(createOrderObject(inOrderKey));
		return lResult.load(
				new PersonModelFactory(IItem.PERSON, "PersonID"), inMaxEntries); //$NON-NLS-1$
	}

	private OrderObject createOrderObject(final String inKey) throws VException {
		final OrderObject outOrder = new OrderObjectImpl();
		outOrder.setValue(inKey, true, 0);
		return outOrder;
	}

	/**
	 * Updates the controlled type according to the new type specified.
	 * 
	 * @param inTypeName
	 *            String
	 * @return {@link LastChangesType} the matching type
	 */
	public LastChangesType updateType(final String inTypeName) {
		if (lastChangesType.checkValue(inTypeName)) {
			return lastChangesType;
		}

		for (final LastChangesType lType : LastChangesType.values()) {
			if (lType.checkValue(inTypeName)) {
				lastChangesType = lType;
				return lastChangesType;
			}
		}
		lastChangesType = LastChangesType.LAST_CREATED;
		return lastChangesType;
	}

	// --- private classes ---

	private static class ModelFactory implements AlternativeModelFactory {
		private final int itemType;
		private final String fieldID;

		public ModelFactory(final int inItemType, final String inFieldID) {
			itemType = inItemType;
			fieldID = inFieldID;
		}

		@Override
		public AlternativeModel createModel(final ResultSet inResultSet)
				throws SQLException {
			return new RetrievedChronologicalItem(new UniqueID(itemType,
					inResultSet.getLong(fieldID)), getTitle(inResultSet),
					inResultSet.getTimestamp("dtCreation"), //$NON-NLS-1$
					inResultSet.getTimestamp("dtMutation")); //$NON-NLS-1$
		}

		protected String getTitle(final ResultSet inResultSet)
				throws SQLException {
			return inResultSet.getString("sTitle"); //$NON-NLS-1$
		}
	}

	private static class PersonModelFactory extends ModelFactory {
		public PersonModelFactory(final int inItemType, final String inFieldID) {
			super(inItemType, inFieldID);
		}

		@Override
		protected String getTitle(final ResultSet inResultSet)
				throws SQLException {
			final String lName = inResultSet.getString("sName"); //$NON-NLS-1$
			final String lFistname = inResultSet.getString("sFirstname"); //$NON-NLS-1$
			if ((lFistname != null) && (lFistname.length() > 0))
				return String.format("%s, %s", lName, lFistname); //$NON-NLS-1$
			return lName;
		}
	}

	@SuppressWarnings("serial")
	private static class SortOnMutationDate implements
			Comparator<AlternativeModel>, Serializable {
		@Override
		public int compare(final AlternativeModel inItem1,
				final AlternativeModel inItem2) {
			return ((RetrievedChronologicalItem) inItem2).getMutationDate()
					.compareTo(
							((RetrievedChronologicalItem) inItem1)
									.getMutationDate());
		}
	}

	@SuppressWarnings("serial")
	private static class SortOnCreationDate implements
			Comparator<AlternativeModel>, Serializable {
		@Override
		public int compare(final AlternativeModel inItem1,
				final AlternativeModel inItem2) {
			return ((RetrievedChronologicalItem) inItem2).getCreationDate()
					.compareTo(
							((RetrievedChronologicalItem) inItem1)
									.getCreationDate());
		}
	}

}
