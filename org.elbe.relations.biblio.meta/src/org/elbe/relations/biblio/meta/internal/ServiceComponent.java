/*
This package is part of Relations application.
Copyright (C) 2009, Benno Luthiger

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
package org.elbe.relations.biblio.meta.internal;

import org.elbe.relations.biblio.meta.internal.coins.COinSProvider;
import org.elbe.relations.biblio.meta.internal.extract.ExcelExtractor;
import org.elbe.relations.biblio.meta.internal.extract.GifExtractor;
import org.elbe.relations.biblio.meta.internal.extract.JpgExtractor;
import org.elbe.relations.biblio.meta.internal.extract.OOExtractor;
import org.elbe.relations.biblio.meta.internal.extract.OfficeXMLExtractor;
import org.elbe.relations.biblio.meta.internal.extract.PdfExtractor;
import org.elbe.relations.biblio.meta.internal.extract.PowerPointExtractor;
import org.elbe.relations.biblio.meta.internal.extract.WordExtractor;
import org.elbe.relations.biblio.meta.internal.html.HmtlRDFaProvider;
import org.elbe.relations.biblio.meta.internal.unapi.UnAPIProvider;
import org.elbe.relations.services.IBibliographyPackage;
import org.elbe.relations.services.IBibliographyProvider;
import org.elbe.relations.services.IExtractorAdapter;
import org.elbe.relations.services.IExtractorPackage;

/**
 * This component's implementation class for the
 * <code>IBibliographyPackage</code> and <code>IExtractorPackage</code> service.
 * This component can dynamically provide various
 * <code>IBibliographyProvider</code> implementations. These are registered
 * using the <code>IBibliographyPackage</code> service.
 * 
 * @author Luthiger Created on 29.12.2009
 */
public class ServiceComponent implements IExtractorPackage,
		IBibliographyPackage {

	@Override
	public IBibliographyProvider[] getBibliographyProviders() {
		return new IBibliographyProvider[] { new COinSProvider(),
				new UnAPIProvider(), new HmtlRDFaProvider() };
	}

	@Override
	public IExtractorAdapter[] getExtractorAdapters() {
		return new IExtractorAdapter[] { new JpgExtractor(),
				new GifExtractor(), new PdfExtractor(), new OOExtractor(),
				new OfficeXMLExtractor(), new WordExtractor(),
				new ExcelExtractor(), new PowerPointExtractor() };
	}

}
