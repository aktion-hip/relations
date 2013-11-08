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
package org.elbe.relations.internal.parsing;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.internal.actions.NewTermAction;
import org.elbe.relations.internal.services.IMetadataExtractor;
import org.elbe.relations.parsing.ExtractedData;
import org.elbe.relations.services.IExtractorAdapter;
import org.elbe.relations.services.IExtractorPackage;
import org.hip.kernel.exc.VException;

/**
 * Enumeration singleton to extract metadata from dropped files.
 * 
 * @author Luthiger 
 */
public class MetadataExtractor implements IMetadataExtractor {

	private final List<IExtractorAdapter> extractorAdapters = new Vector<IExtractorAdapter>();

	@Inject
	private IEclipseContext context;

	/**
	 * OSGi DS bind method.
	 * 
	 * @param inExtractorPackage
	 *            {@link IExtractorPackage}
	 */
	public void setExtractorAdapters(final IExtractorPackage inExtractorPackage) {
		for (final IExtractorAdapter lAdapter : inExtractorPackage
				.getExtractorAdapters()) {
			extractorAdapters.add(lAdapter);
		}
	}

	/**
	 * OSGi DS unbind method.
	 * 
	 * @param inExtractorPackage
	 *            {@link IExtractorPackage}
	 */
	public void unsetExtractorAdapters(
			final IExtractorPackage inExtractorPackage) {
		for (final IExtractorAdapter lAdapter : inExtractorPackage
				.getExtractorAdapters()) {
			extractorAdapters.remove(lAdapter);
		}
	}

	/**
	 * Extracts the metadata from the dropped file, create a term item and
	 * returns the new item's <code>UniqueID</code>.
	 * 
	 * @param inDrop
	 *            File
	 * @return UniqueID the newly created item's ID.
	 * @throws VException
	 * @throws IOException
	 */
	@Override
	public UniqueID extract(final File inDrop) throws VException, IOException {
		// we first try the specialized adapters for metadata extraction.
		for (final IExtractorAdapter lAdapter : extractorAdapters) {
			if (lAdapter.acceptsFile(inDrop)) {
				return processFile(inDrop, lAdapter);
			}
		}

		// if everything failed, we at least have the file name.
		final String lFileName = inDrop.getName();
		return createNewTerm(new NewTermAction.Builder(lFileName).text(
				inDrop.getAbsolutePath()).build(context));
	}

	private UniqueID processFile(final File inDrop,
			final IExtractorAdapter inAdapter) throws VException, IOException {
		final ExtractedData lMetadata = inAdapter.process(inDrop);
		return createNewTerm(new NewTermAction.Builder(lMetadata.getTitle())
				.text(lMetadata.getText()).build(context));
	}

	/**
	 * @param lAction
	 * @return
	 * @throws VException
	 */
	private UniqueID createNewTerm(final NewTermAction lAction)
			throws VException {
		lAction.execute();
		final IItem lItem = lAction.getNewItem();
		return new UniqueID(lItem.getItemType(), lItem.getID());
	}

	/**
	 * Implementations of <code>IMetadataAdapter</code>s are registered here.
	 * 
	 * @param inExtractorAdapter
	 *            IMetadataAdapter
	 */
	public void registerAdapter(final IExtractorAdapter inExtractorAdapter) {
		extractorAdapters.add(inExtractorAdapter);
	}

	/**
	 * OSGi DS allows to unregister <code>IMetadataAdapter</code>s.
	 * 
	 * @param inExtractorAdapter
	 *            IMetadataAdapter
	 */
	public void unregisterAdapter(final IExtractorAdapter inExtractorAdapter) {
		extractorAdapters.remove(inExtractorAdapter);
	}

}
