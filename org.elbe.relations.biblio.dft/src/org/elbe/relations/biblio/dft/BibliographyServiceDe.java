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
package org.elbe.relations.biblio.dft;

import org.elbe.relations.data.utility.IBibliography;
import org.elbe.relations.services.IBibliographyService;

/**
 * The default implementation of the <code>IBibliographyService</code> (de).
 * 
 * @author Luthiger
 */
public class BibliographyServiceDe implements IBibliographyService {

	@Override
	public String getId() {
		return "relations.bibliography.standard.de"; //$NON-NLS-1$
	}

	@Override
	public String getBibliographyName() {
		return "Standard (de)"; //$NON-NLS-1$
	}

	@Override
	public IBibliography getBiblioService() {
		return new StandardDe();
	}

}
