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
package org.elbe.relations.internal.bom;

import java.io.IOException;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.Person;
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.db.IAction;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;
import org.elbe.relations.internal.services.IItemEditWizard;
import org.elbe.relations.internal.wizards.PersonEditWizard;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.IRelation;
import org.hip.kernel.exc.VException;

/**
 * Person model with icon.
 * 
 * @author Luthiger
 */
@SuppressWarnings({ "serial", "restriction" })
public class PersonWithIcon extends Person implements IItemModel {

	private final IEclipseContext context;

	/**
	 * PersonWithIcon constructor.
	 * 
	 * @param inPerson
	 *            {@link AbstractPerson}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @throws VException
	 */
	public PersonWithIcon(final AbstractPerson inPerson,
	        final IEclipseContext inContext) throws VException {
		context = inContext;
		set(PersonHome.KEY_ID, inPerson.get(PersonHome.KEY_ID));
		set(PersonHome.KEY_TEXT, inPerson.get(PersonHome.KEY_TEXT));
		set(PersonHome.KEY_CREATED, inPerson.get(PersonHome.KEY_CREATED));
		set(PersonHome.KEY_MODIFIED, inPerson.get(PersonHome.KEY_MODIFIED));
		//
		set(PersonHome.KEY_NAME, inPerson.get(PersonHome.KEY_NAME));
		set(PersonHome.KEY_FIRSTNAME, inPerson.get(PersonHome.KEY_FIRSTNAME));
		reinitialize();
	}

	@Override
	public void addSource(final IRelation inRelation) {
		// nothing to do
	}

	@Override
	public void addTarget(final IRelation inRelation) {
		// nothing to do
	}

	@Override
	public List<IRelation> getSources() {
		// nothing to do
		return null;
	}

	@Override
	public List<IRelation> getTargets() {
		// nothing to do
		return null;
	}

	@Override
	public Image getImage() {
		return RelationsImages.PERSON.getImage();
	}

	@Override
	public Class<? extends IItemEditWizard> getItemEditWizard() {
		return PersonEditWizard.class;
	}

	@Override
	public IAction getItemDeleteAction(final Logger inLog) {
		return new IAction() {
			@Override
			public void run() {
				try {
					BOMHelper.getPersonHome().deleteItem(getID());
					deleteItemInIndex();
				}
				catch (final BOMException exc) {
					inLog.error(exc, exc.getMessage());
				}
				catch (final VException exc) {
					inLog.error(exc, exc.getMessage());
				}
				catch (final IOException exc) {
					inLog.error(exc, exc.getMessage());
				}
			}
		};
	}

	@Override
	protected RelationsIndexer getIndexer() {
		return RelationsIndexerWithLanguage.createRelationsIndexer(context);
	}

}
