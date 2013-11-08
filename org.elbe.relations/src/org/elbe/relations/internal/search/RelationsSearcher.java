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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.search.AbstractSearching;
import org.elbe.relations.data.search.RetrievedItem;
import org.elbe.relations.handlers.ReindexHandler;
import org.elbe.relations.internal.data.DBSettings;
import org.elbe.relations.internal.preferences.LanguageService;
import org.hip.kernel.exc.VException;

/**
 * Class for searching a Lucene index of a Relations database.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
@Creatable
public class RelationsSearcher extends AbstractSearching {
	private final String language;

	@Inject
	@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_MAX_SEARCH_HITS)
	private final int maxSearchHits = RelationsConstants.DFT_MAX_SEARCH_HITS;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private ECommandService commandService;

	/**
	 * RelationsSearcher constructor, used for DI. Note: clients must not create
	 * instances of RelationsSearcher using this constructor.
	 * 
	 * @param inIndexDir
	 * @param inLanguage
	 */
	public RelationsSearcher(final String inIndexDir, final String inLanguage) {
		super(inIndexDir);
		language = inLanguage;
	}

	/**
	 * Factory method to create instances of <code>RelationsSearcher</code>.
	 * 
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @param inDbSettings
	 *            {@link DBSettings}
	 * @return {@link RelationsSearcher}
	 */
	public static RelationsSearcher createRelationsSearcher(
			final IEclipseContext inContext, final DBSettings inDbSettings) {
		final RelationsSearcher out = new RelationsSearcher(
				inDbSettings.getCatalog(), LanguageService.getContentLocale()
						.getLanguage());
		ContextInjectionFactory.inject(out, inContext);
		return out;
	}

	/**
	 * Do the search with the specified query and return the collection of items
	 * found.
	 * 
	 * @param inQueryTerm
	 *            String
	 * @return List<RetrievedItem>
	 * @throws IOException
	 * @throws VException
	 */
	public List<RetrievedItem> search(final String inQueryTerm)
			throws IOException, VException {
		final File lIndexDir = getIndexDir();
		if (lIndexDir.list().length == 0) {
			if (MessageDialog.openQuestion(shell, RelationsMessages
					.getString("RelationsSearcher.warning.title"),
					RelationsMessages
							.getString("RelationsSearcher.warning.message"))) {
				handlerService.activateHandler(ICommandIds.CMD_SEARCH,
						new ReindexHandler());
				handlerService.executeHandler(commandService.createCommand(
						ICommandIds.CMD_SEARCH, null));
			}
			return Collections.emptyList();
		}
		return getIndexer().search(inQueryTerm, getIndexDir(), language,
				maxSearchHits);
	}

}
