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
package org.elbe.relations.internal.actions;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.data.bom.TermHome;
import org.elbe.relations.db.IDataService;

/**
 * Command to create a new term item.
 * 
 * @author Luthiger Created on 13.11.2009
 */
@SuppressWarnings("restriction")
public class NewTermAction implements ICommand {
	private IItem term = null;
	private Builder builder;

	@Inject
	private Logger log;

	@Inject
	private IDataService data;

	/**
	 * Called by Builder.build().
	 * 
	 * @param inBuilder
	 *            {@link Builder}
	 */
	private void setBuilder(final Builder inBuilder) {
		builder = inBuilder;
	}

	@Override
	public void execute() {
		final TermHome lHome = BOMHelper.getTermHome();
		try {
			term = lHome.newTerm(builder.term, builder.textString);
			data.loadNew((LightWeightTerm) term.getLightWeight());
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Called after command execution, returns the newly created term item.
	 * 
	 * @return IItem
	 */
	public IItem getNewItem() {
		return term;
	}

	// --- inner class builder ---

	public static class Builder {
		// mandatory
		private final String term;
		// optional
		private String textString = ""; //$NON-NLS-1$

		public Builder(final String inTerm) {
			term = inTerm;
		}

		public Builder text(final String inText) {
			textString = inText;
			return this;
		}

		public NewTermAction build(final IEclipseContext inContext) {
			final NewTermAction out = ContextInjectionFactory.make(
			        NewTermAction.class, inContext);
			out.setBuilder(this);
			return out;
		}
	}

}
