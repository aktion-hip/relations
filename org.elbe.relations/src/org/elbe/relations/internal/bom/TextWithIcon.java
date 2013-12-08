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
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IStyleParser;
import org.elbe.relations.data.bom.Text;
import org.elbe.relations.data.bom.TextHome;
import org.elbe.relations.data.search.RelationsIndexer;
import org.elbe.relations.data.utility.IBibliography;
import org.elbe.relations.db.IAction;
import org.elbe.relations.internal.controller.BibliographyController;
import org.elbe.relations.internal.search.RelationsIndexerWithLanguage;
import org.elbe.relations.internal.services.IItemEditWizard;
import org.elbe.relations.internal.style.StyleParser;
import org.elbe.relations.internal.wizards.TextEditWizard;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.IRelation;
import org.hip.kernel.exc.VException;

/**
 * Text model with icon.
 * 
 * @author Luthiger
 */
@SuppressWarnings({ "restriction", "serial" })
public class TextWithIcon extends Text implements IItemModel {

	private final IEclipseContext context;
	private final BibliographyController biblioController;

	/**
	 * TextWithIcon constructor.
	 * 
	 * @param inText
	 *            {@link AbstractText}
	 * @param inContext
	 *            {@link IEclipseContext}
	 * @throws VException
	 */
	public TextWithIcon(final AbstractText inText,
	        final IEclipseContext inContext) throws VException {
		context = inContext;
		biblioController = context.get(BibliographyController.class);

		set(TextHome.KEY_ID, inText.get(TextHome.KEY_ID));
		set(TextHome.KEY_TITLE, inText.get(TextHome.KEY_TITLE));
		set(TextHome.KEY_TEXT, inText.get(TextHome.KEY_TEXT));
		set(TextHome.KEY_CREATED, inText.get(TextHome.KEY_CREATED));
		set(TextHome.KEY_MODIFIED, inText.get(TextHome.KEY_MODIFIED));
		//
		set(TextHome.KEY_AUTHOR, inText.get(TextHome.KEY_AUTHOR));
		set(TextHome.KEY_COAUTHORS, inText.get(TextHome.KEY_COAUTHORS));
		set(TextHome.KEY_SUBTITLE, inText.get(TextHome.KEY_SUBTITLE));
		set(TextHome.KEY_YEAR, inText.get(TextHome.KEY_YEAR));
		set(TextHome.KEY_PUBLICATION, inText.get(TextHome.KEY_PUBLICATION));
		set(TextHome.KEY_PAGES, inText.get(TextHome.KEY_PAGES));
		set(TextHome.KEY_VOLUME, inText.get(TextHome.KEY_VOLUME));
		set(TextHome.KEY_NUMBER, inText.get(TextHome.KEY_NUMBER));
		set(TextHome.KEY_PUBLISHER, inText.get(TextHome.KEY_PUBLISHER));
		set(TextHome.KEY_PLACE, inText.get(TextHome.KEY_PLACE));
		set(TextHome.KEY_TYPE, inText.get(TextHome.KEY_TYPE));
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
		return RelationsImages.TEXT.getImage();
	}

	@Override
	public Class<? extends IItemEditWizard> getItemEditWizard() {
		return TextEditWizard.class;
	}

	@Override
	public IAction getItemDeleteAction(final Logger inLog) {
		return new IAction() {
			@Override
			public void run() {
				try {
					BOMHelper.getTextHome().deleteItem(getID());
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

	@Override
	protected IStyleParser getStyleParser() {
		return StyleParser.getInstance();
	}

	@Override
	protected IBibliography getBiblioHandler() {
		final IBibliography out = biblioController.getBibliography();
		return out == null ? super.getBiblioHandler() : out;
	}

}
