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
package org.elbe.relations.data.bom;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Locale;

import org.elbe.relations.data.Messages;
import org.elbe.relations.data.search.AbstractSearching;
import org.elbe.relations.data.search.IndexerDateField;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.NoOpIndexer;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.bom.impl.DomainObjectImpl;
import org.hip.kernel.exc.VException;

/**
 * Abstract class providing general functionality for items.
 * 
 * @author Benno Luthiger Created on 07.05.2006
 * @see org.elbe.relations.data.bom.IItem
 */
@SuppressWarnings("serial")
public abstract class AbstractItem extends DomainObjectImpl implements IItem {
	public final static String TRUNCATION_STATE = "22001"; //$NON-NLS-1$
	public final static String TRUNCATION_MSG = Messages
			.getString("AbstractItem.error.truncation"); //$NON-NLS-1$

	/**
	 * AbstractItem constructor
	 */
	public AbstractItem() {
		super();
	}

	/**
	 * @throws VException
	 * @see IItem#getCreated()
	 */
	@Override
	public String getCreated() throws VException {
		return Messages.getString("Item.created.modified", getLocale(),
				getCreatedModified());
	}

	/**
	 * Returns the default locale <code>ENGLISH</code>. Subclasses should
	 * override.
	 * 
	 * @return {@link Locale} the locale used for localized messages.
	 */
	protected Locale getLocale() {
		return Locale.ENGLISH;
	}

	abstract protected Timestamp[] getCreatedModified() throws VException;

	/**
	 * @param inKey
	 *            String Key of value to be retrieved.
	 * @return String Value as String or empty String if value is NULL.
	 * @throws VException
	 */
	protected String getChecked(final String inKey) throws VException {
		final Object lValue = get(inKey);
		return lValue == null ? "" : lValue.toString().trim(); //$NON-NLS-1$
	}

	/**
	 * Deletes the item's search term in the search index.
	 * 
	 * @throws IOException
	 * @throws VException
	 */
	protected void deleteItemInIndex() throws IOException, VException {
		final String lUniqueID = UniqueID.getStringOf(getItemType(), getID());
		getIndexer().deleteItemInIndex(lUniqueID);
	}

	/**
	 * Refreshes the item's search term in the search index.
	 * 
	 * @throws IOException
	 * @throws BOMException
	 * @throws VException
	 */
	protected void refreshItemInIndex() throws IOException, BOMException,
			VException {
		getIndexer().refreshItemInIndex(this);
	}

	/**
	 * Returns a default implementation of the <code>RelationsIndexer</code>
	 * which does nothing. Subclasses have to override.
	 * 
	 * @return {@link RelationsIndexer}
	 */
	protected RelationsIndexer getIndexer() {
		return new NoOpIndexer();
	}

	/**
	 * Returns the item's title.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		try {
			return String.format("Item '%s'", getTitle());
		}
		catch (final Exception exc) {
			return super.toString();
		}
	}

	// various helper methods providing protected functionality for item
	// indexing
	protected IndexerField getFieldTitle(final String inTitle) {
		return new IndexerField(AbstractSearching.TITLE, inTitle,
				IndexerField.Store.YES, IndexerField.Index.ANALYZED);
	}

	protected IndexerField getFieldUniqueID(final String inItemID) {
		return new IndexerField(AbstractSearching.ITEM_ID, inItemID,
				IndexerField.Store.YES, IndexerField.Index.NOT_ANALYZED);
	}

	protected IndexerField getFieldItemType(final String inItemType) {
		return new IndexerField(AbstractSearching.ITEM_TYPE, inItemType,
				IndexerField.Store.YES, IndexerField.Index.NO);
	}

	protected IndexerField getFieldItemID(final String inItemID) {
		return new IndexerField(AbstractSearching.ITEM_ID, inItemID,
				IndexerField.Store.YES, IndexerField.Index.NO);
	}

	protected IndexerField getFieldText(final String inText) {
		return new IndexerField(AbstractSearching.CONTENT_FULL, inText,
				IndexerField.Store.NO, IndexerField.Index.ANALYZED);
	}

	protected void addCreatedModified(final IndexerDocument inDocument)
			throws VException {
		final Timestamp[] lCreatedModified = getCreatedModified();

		IndexerField lDate = new IndexerDateField(
				AbstractSearching.DATE_CREATED, lCreatedModified[0].getTime(),
				IndexerField.Store.YES, IndexerField.Index.NOT_ANALYZED,
				IndexerDateField.TimeResolution.DAY);
		inDocument.addField(lDate);

		lDate = new IndexerDateField(AbstractSearching.DATE_MODIFIED,
				lCreatedModified[1].getTime(), IndexerField.Store.YES,
				IndexerField.Index.NOT_ANALYZED,
				IndexerDateField.TimeResolution.DAY);
		inDocument.addField(lDate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hip.kernel.bom.impl.DomainObjectImpl#hashCode()
	 */
	@Override
	public int hashCode() {
		final int lPrime = 31;
		int outHash = 1;
		outHash = lPrime * outHash + getItemType();
		try {
			outHash = lPrime * outHash + (int) (getID() ^ (getID() >>> 32));
		}
		catch (final VException exc) {
			// intentionally left empty
		}
		return outHash;
	}

	/**
	 * @return <code>true</code> if ID and type are equal.
	 * @see org.hip.kernel.bom.impl.DomainObjectImpl#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object inObject) {
		if (this == inObject)
			return true;
		if (inObject == null)
			return false;
		if (inObject instanceof IItem) {
			final IItem lItem = (IItem) inObject;
			try {
				return getItemType() == lItem.getItemType()
						&& getID() == lItem.getID();
			}
			catch (final VException exc) {
				return false;
			}
		}
		return false;
	}

}
