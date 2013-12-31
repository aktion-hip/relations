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
package org.elbe.relations.internal.search;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.preferences.LanguageService;

/**
 * Language service for the Relations application. We distinguish between the
 * application language, used for the labels, window titles etc. and the content
 * language, used for the indexer to process the content stored in the data
 * base.
 * 
 * @author Luthiger
 */
public class RelationsIndexerWithLanguage extends RelationsIndexer {

	private final String language;

	/**
	 * RelationsIndexerWithLanguage constructor. <b>Note:</b> Use the factory
	 * method
	 * {@link RelationsIndexerWithLanguage#createRelationsIndexer(IEclipseContext)}
	 * instead of this constructor.
	 */
	public RelationsIndexerWithLanguage(final String inIndexDir,
	        final String inLanguage) {
		super(inIndexDir);
		language = inLanguage;
	}

	@Override
	protected String getLanguage() {
		return language;
	}

	/**
	 * Factory method: creates an instance of
	 * <code>RelationsIndexerWithLanguage</code> initialized with the values
	 * from the specivied eclipse context.
	 * 
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @return {@link RelationsIndexerWithLanguage}
	 */
	public static RelationsIndexerWithLanguage createRelationsIndexer(
	        final IEclipseContext inContext) {
		final DBSettings lDBSettings = inContext.get(DBSettings.class);
		return new RelationsIndexerWithLanguage(lDBSettings.getCatalog(),
		        LanguageService.getContentLocale().getLanguage());
	}

}
