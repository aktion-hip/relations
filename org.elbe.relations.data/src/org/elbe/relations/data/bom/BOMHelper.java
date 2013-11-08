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

import org.elbe.relations.data.internal.bom.Relation;
import org.elbe.relations.data.internal.bom.RelationsHomeManager;

/**
 * Helper to create homes of domain object models.
 * 
 * @author Benno Luthiger Created on Sep 25, 2004
 */
public class BOMHelper {

	public static TermHome getTermHome() {
		return (TermHome) RelationsHomeManager.INSTANCE
				.getHome(Term.HOME_CLASS_NAME);
	}

	public static TextHome getTextHome() {
		return (TextHome) RelationsHomeManager.INSTANCE
				.getHome(Text.HOME_CLASS_NAME);
	}

	public static PersonHome getPersonHome() {
		return (PersonHome) RelationsHomeManager.INSTANCE
				.getHome(Person.HOME_CLASS_NAME);
	}

	public static RelationHome getRelationHome() {
		return (RelationHome) RelationsHomeManager.INSTANCE
				.getHome(Relation.HOME_CLASS_NAME);
	}

	public static CollectableTermHome getCollectableTermHome() {
		return (CollectableTermHome) RelationsHomeManager.INSTANCE
				.getHome(Term.COLL_HOME_CLASS_NAME);
	}

	public static CollectableTextHome getCollectableTextHome() {
		return (CollectableTextHome) RelationsHomeManager.INSTANCE
				.getHome(Text.COLL_HOME_CLASS_NAME);
	}

	public static CollectablePersonHome getCollectablePersonHome() {
		return (CollectablePersonHome) RelationsHomeManager.INSTANCE
				.getHome(Person.COLL_HOME_CLASS_NAME);
	}

	public static JoinRelatedTermHome getJoinRelatedTerm1Home() {
		return (JoinRelatedTermHome) RelationsHomeManager.INSTANCE
				.getHome("org.elbe.relations.data.bom.JoinRelatedTerm1Home"); //$NON-NLS-1$
	}

	public static JoinRelatedTermHome getJoinRelatedTerm2Home() {
		return (JoinRelatedTermHome) RelationsHomeManager.INSTANCE
				.getHome("org.elbe.relations.data.bom.JoinRelatedTerm2Home"); //$NON-NLS-1$
	}

	public static JoinRelatedTextHome getJoinRelatedText1Home() {
		return (JoinRelatedTextHome) RelationsHomeManager.INSTANCE
				.getHome("org.elbe.relations.data.bom.JoinRelatedText1Home"); //$NON-NLS-1$
	}

	public static JoinRelatedTextHome getJoinRelatedText2Home() {
		return (JoinRelatedTextHome) RelationsHomeManager.INSTANCE
				.getHome("org.elbe.relations.data.bom.JoinRelatedText2Home"); //$NON-NLS-1$
	}

	public static JoinRelatedPersonHome getJoinRelatedPerson1Home() {
		return (JoinRelatedPersonHome) RelationsHomeManager.INSTANCE
				.getHome("org.elbe.relations.data.bom.JoinRelatedPerson1Home"); //$NON-NLS-1$
	}

	public static JoinRelatedPersonHome getJoinRelatedPerson2Home() {
		return (JoinRelatedPersonHome) RelationsHomeManager.INSTANCE
				.getHome("org.elbe.relations.data.bom.JoinRelatedPerson2Home"); //$NON-NLS-1$
	}

	/**
	 * Returns the <code>IItem</code> that corresponds to the specified
	 * <code>ILightWeightItem</code>.
	 * 
	 * @param inLightWeight
	 *            ILightWeightItem the item who's corresponding
	 *            <code>IItem</code> is to look up.
	 * @return IItem
	 * @throws BOMException
	 */
	public static IItem getItem(final ILightWeightItem inLightWeight)
			throws BOMException {
		IItemFactory lFactory = null;
		switch (inLightWeight.getItemType()) {
		case IItem.TERM:
			lFactory = getTermHome();
			break;
		case IItem.TEXT:
			lFactory = getTextHome();
			break;
		case IItem.PERSON:
			lFactory = getPersonHome();
			break;
		}
		return lFactory.getItem(inLightWeight.getID());
	}
}
