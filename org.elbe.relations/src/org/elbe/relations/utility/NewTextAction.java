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
package org.elbe.relations.utility;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.LightWeightText;
import org.elbe.relations.data.bom.TextHome;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.actions.ICommand;

/**
 * Command to create a new text item. This class uses the builder pattern to get
 * away with telescoping constructors.
 * 
 * @author Luthiger Created on 21.11.2009
 */
@SuppressWarnings("restriction")
public class NewTextAction implements ICommand {
	private IItem text = null;
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
		final TextHome lHome = BOMHelper.getTextHome();
		try {
			text = lHome.newText(builder.title, builder.textString,
			        builder.author, builder.coAuthor, builder.subTitle,
			        builder.year, builder.publication, builder.pages,
			        builder.volume, builder.number, builder.publisher,
			        builder.place, builder.type);
			data.loadNew((LightWeightText) text.getLightWeight());
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Called after command execution, returns the newly created text item.
	 * 
	 * @return IItem
	 */
	public IItem getNewItem() {
		return text;
	}

	@Override
	public String toString() {
		final String lFormat = "title=%s&author=%s&coAuthor=%s&subTitle=%s&year=%s&publication=%s&pages=%s&volume=%s&number=%s&publisher=%s&place=%s&type=%s&text=%s"; //$NON-NLS-1$
		return String.format(lFormat, builder.title, builder.author,
		        builder.coAuthor, builder.subTitle, builder.year,
		        builder.publication, builder.pages, builder.volume,
		        builder.number, builder.publisher, builder.place, builder.type,
		        builder.textString);
	}

	// --- inner class builder ---

	public static class Builder {
		// mandatory
		private final String title;
		private final String author;
		// optional
		private String textString = ""; //$NON-NLS-1$
		private String coAuthor = ""; //$NON-NLS-1$
		private String subTitle = ""; //$NON-NLS-1$
		private String year = ""; //$NON-NLS-1$
		private String publication = ""; //$NON-NLS-1$
		private String pages = ""; //$NON-NLS-1$
		private int volume = 0;
		private int number = 0;
		private String publisher = ""; //$NON-NLS-1$
		private String place = ""; //$NON-NLS-1$
		private int type = AbstractText.TYPE_WEBPAGE;

		public Builder(final String inTitle, final String inAuthor) {
			title = inTitle;
			author = inAuthor;
		}

		public Builder text(final String inText) {
			textString = inText;
			return this;
		}

		public Builder coAuthor(final String inCoAuthor) {
			coAuthor = inCoAuthor;
			return this;
		}

		public Builder subTitle(final String inSubTitle) {
			subTitle = inSubTitle;
			return this;
		}

		public Builder year(final String inYear) {
			year = inYear;
			return this;
		}

		public Builder publication(final String inPublication) {
			publication = inPublication;
			return this;
		}

		public Builder pages(final String inPages) {
			pages = inPages;
			return this;
		}

		public Builder volume(final int inVolume) {
			volume = inVolume;
			return this;
		}

		public Builder number(final int inNumber) {
			number = inNumber;
			return this;
		}

		public Builder publisher(final String inPublisher) {
			publisher = inPublisher;
			return this;
		}

		public Builder place(final String inPlace) {
			place = inPlace;
			return this;
		}

		public Builder type(final int inType) {
			type = inType;
			return this;
		}

		public NewTextAction build(final IEclipseContext inContext) {
			final NewTextAction out = ContextInjectionFactory.make(
			        NewTextAction.class, inContext);
			out.setBuilder(this);
			return out;
		}
	}

}
