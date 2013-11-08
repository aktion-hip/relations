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
package org.elbe.relations.internal.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.data.utility.IBibliography;
import org.elbe.relations.services.IBibliographyService;

/**
 * The OSGi service client for the <code>IBibliographyService</code>.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class BibliographyController {
	private final List<IBibliographyService> bibliographies = new ArrayList<IBibliographyService>();
	private String biblioSelection;

	@Inject
	@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_BIBLIO_SCHEMA)
	private final String biblioID = RelationsConstants.DFT_BIBLIO_SCHEMA_ID;

	private IBibliography actualBiblio;

	/**
	 * OSGi bind method.
	 * 
	 * @param inService
	 *            {@link IBibliographyService}
	 */
	public void register(final IBibliographyService inService) {
		bibliographies.add(inService);
	}

	/**
	 * OSGi unbind method.
	 * 
	 * @param inService
	 *            {@link IBibliographyService}
	 */
	public void unregister(final IBibliographyService inService) {
		bibliographies.remove(inService);
	}

	// ---

	/**
	 * Returns the selected bibliography formatter.
	 * 
	 * @return {@link IBibliography} the actually selected formatter, may be
	 *         <code>null</code>
	 */
	public IBibliography getBibliography() {
		if (actualBiblio == null) {
			for (final IBibliographyService lService : bibliographies) {
				if (lService.getId().equals(biblioID)) {
					actualBiblio = lService.getBiblioService();
					break;
				}
			}
		}
		return actualBiblio;
	}

	/**
	 * @return String[] array of bibliography names (labels) of the registered
	 *         bibliography configurations
	 */
	public String[] getBiblioNames() {
		final String[] outBiblioNames = new String[bibliographies.size()];
		int i = 0;
		for (final IBibliographyService lService : bibliographies) {
			outBiblioNames[i++] = lService.getBibliographyName();
		}
		return outBiblioNames;
	}

	/**
	 * @return int index of the selected bibliography configuration in the list
	 */
	public int getSelectedIndex() {
		final String lSelected = (biblioSelection == null || biblioSelection
				.isEmpty()) ? RelationsConstants.DFT_BIBLIO_SCHEMA_ID
				: biblioSelection;

		int i = 0;
		for (final IBibliographyService lService : bibliographies) {
			if (lSelected.equals(lService.getId())) {
				return i;
			}
			i++;
		}
		return 0;
	}

	/**
	 * Returns the bibliography with the specified index in the list.
	 * 
	 * @param inSelectionIndex
	 * @return IBibliographyService
	 */
	public IBibliographyService getBibliography(final int inSelectionIndex) {
		return bibliographies.get(inSelectionIndex);
	}

	// ---

	@Inject
	void trachBiblioSelection(
			@Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_BIBLIO_SCHEMA) final String inBiblioSelection) {
		biblioSelection = inBiblioSelection;
	}

}
