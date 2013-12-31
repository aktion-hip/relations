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
import org.elbe.relations.data.bom.LightWeightPerson;
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.db.IDataService;

/**
 * Command to create a new person item. This class uses the builder pattern to
 * get away with telescoping constructors.
 * 
 * @author Luthiger Created on 21.11.2009
 */
@SuppressWarnings("restriction")
public class NewPersonAction implements ICommand {
	private IItem person = null;
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
		final PersonHome lHome = BOMHelper.getPersonHome();
		try {
			person = lHome.newPerson(builder.name, builder.firstname,
			        builder.dateFrom, builder.dateTo, builder.textString);
			data.loadNew((LightWeightPerson) person.getLightWeight());
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	public IItem getNewItem() {
		return person;
	}

	// --- inner class builder ---

	public static class Builder {
		// mandatory
		private final String name;
		// optional
		private String firstname = ""; //$NON-NLS-1$
		private String dateFrom = ""; //$NON-NLS-1$
		private String dateTo = ""; //$NON-NLS-1$
		private String textString = ""; //$NON-NLS-1$

		public Builder(final String inName) {
			name = inName;
		}

		public Builder firstName(final String inFirstname) {
			firstname = inFirstname;
			return this;
		}

		public Builder dateFrom(final String inDateFrom) {
			dateFrom = inDateFrom;
			return this;
		}

		public Builder dateTo(final String inDateTo) {
			dateTo = inDateTo;
			return this;
		}

		public Builder text(final String inText) {
			textString = inText;
			return this;
		}

		public NewPersonAction build(final IEclipseContext inContext) {
			final NewPersonAction out = ContextInjectionFactory.make(
			        NewPersonAction.class, inContext);
			out.setBuilder(this);
			return out;
		}
	}

}
