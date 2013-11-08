/*
This package is part of Relations application.
Copyright (C) 2010, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.elbe.relations.biblio.meta.internal.extract;

import org.elbe.relations.services.IExtractorAdapter;

/**
 * Adapter to extract metadata from a OfficeXML file (e.g. docx).
 * 
 * @author Luthiger Created on 23.01.2010
 */
public class OfficeXMLExtractor extends AbstractCompressedXMLContainerExtractor
		implements IExtractorAdapter {
	private static final String TAG_TITLE = "dc:title".intern(); //$NON-NLS-1$
	private static final String TAG_DESCRIPTION = "dc:description".intern(); //$NON-NLS-1$
	private static final String TAG_SUBJECT = "dc:subject".intern(); //$NON-NLS-1$
	private static final String TAG_CREATION = "dcterms:created".intern(); //$NON-NLS-1$
	private static final String TAG_CREATOR = "dc:creator".intern(); //$NON-NLS-1$
	private static final String TAG_KEYWORD = "cp:keywords".intern(); //$NON-NLS-1$

	private static final String OFFICE_META = "docprops/core.xml"; //$NON-NLS-1$

	@Override
	protected String getMetaEntryName() {
		return OFFICE_META;
	}

	@Override
	protected String getInputType() {
		return "application/ms-office-1.x"; //$NON-NLS-1$
	}

	@Override
	protected ParserListener getParserListener(final String inTagName) {
		if (inTagName == TAG_TITLE) {
			return ParserListener.TITLE;
		} else if (inTagName == TAG_DESCRIPTION) {
			return ParserListener.DESCRIPTION;
		} else if (inTagName == TAG_SUBJECT) {
			return ParserListener.SUBJECT;
		} else if (inTagName == TAG_CREATOR) {
			return ParserListener.AUTHOR;
		} else if (inTagName == TAG_KEYWORD) {
			return ParserListener.KEYWORD;
		} else if (inTagName == TAG_CREATION) {
			return ParserListener.CREATION;
		}
		return ParserListener.NOOP;
	}

}
