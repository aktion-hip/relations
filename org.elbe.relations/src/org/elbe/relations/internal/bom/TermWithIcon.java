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
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.Term;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.db.IAction;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;
import org.elbe.relations.internal.services.IItemEditWizard;
import org.elbe.relations.internal.wizards.TermEditWizard;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.IRelation;
import org.hip.kernel.exc.VException;

/**
 * Term model with icon.
 * 
 * @author Luthiger
 */
@SuppressWarnings({ "restriction", "serial" })
public class TermWithIcon extends Term implements IItemModel {

	private final IEclipseContext context;

	/**
	 * TermWithIcon constructor.
	 * 
	 * @param inTerm
	 *            {@link AbstractTerm}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @throws VException
	 */
	public TermWithIcon(final AbstractTerm inTerm,
	        final IEclipseContext inContext) throws VException {
		context = inContext;
		set(TermHome.KEY_ID, inTerm.get(TermHome.KEY_ID));
		set(TermHome.KEY_TITLE, inTerm.get(TermHome.KEY_TITLE));
		set(TermHome.KEY_TEXT, inTerm.get(TermHome.KEY_TEXT));
		set(TermHome.KEY_CREATED, inTerm.get(TermHome.KEY_CREATED));
		set(TermHome.KEY_MODIFIED, inTerm.get(TermHome.KEY_MODIFIED));
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
		return RelationsImages.TERM.getImage();
	}

	@Override
	public Class<? extends IItemEditWizard> getItemEditWizard() {
		return TermEditWizard.class;
	}

	@Override
	public IAction getItemDeleteAction(final Logger inLog) {
		return new IAction() {
			@Override
			public void run() {
				try {
					BOMHelper.getTermHome().deleteItem(getID());
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
