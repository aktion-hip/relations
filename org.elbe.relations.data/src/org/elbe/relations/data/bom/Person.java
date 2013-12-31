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

import org.elbe.relations.data.search.FullTextHelper;
import org.elbe.relations.data.search.IIndexable;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.utility.UniqueID;
import org.hip.kernel.exc.VException;

/**
 * The model for the person item.
 * 
 * @author Benno Luthiger Created on Sep 3, 2005
 */
@SuppressWarnings("serial")
public class Person extends AbstractPerson implements IIndexable {
	public final static String HOME_CLASS_NAME = "org.elbe.relations.data.bom.PersonHome"; //$NON-NLS-1$
	public final static String COLL_HOME_CLASS_NAME = "org.elbe.relations.data.bom.CollectablePersonHome"; //$NON-NLS-1$

	/**
	 * Person constructor.
	 */
	public Person() {
		super();
	}

	/**
	 * This Method returns the class name of the home.
	 * 
	 * @return java.lang.String
	 */
	@Override
	public String getHomeClassName() {
		return HOME_CLASS_NAME;
	}

	@Override
	public void indexContent(final IndexerHelper inIndexer) throws VException {
		final IndexerDocument lDocument = new IndexerDocument();

		final FullTextHelper lFullText = new FullTextHelper();
		lDocument.addField(getFieldUniqueID(UniqueID.getStringOf(IItem.PERSON,
		        getID())));
		lDocument.addField(getFieldItemType(String.valueOf(IItem.PERSON)));
		lDocument.addField(getFieldItemID(get(PersonHome.KEY_ID).toString()));
		lDocument.addField(getFieldTitle(lFullText
		        .add(getChecked(PersonHome.KEY_FIRSTNAME) + " " //$NON-NLS-1$
		                + get(PersonHome.KEY_NAME).toString())));
		addCreatedModified(lDocument);

		lFullText.add(getChecked(PersonHome.KEY_TEXT));
		lFullText.add(getChecked(PersonHome.KEY_FROM));
		lFullText.add(getChecked(PersonHome.KEY_TO));
		lDocument.addField(getFieldText(lFullText.getFullText()));
		inIndexer.addDocument(lDocument);
	}

}
