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
package org.elbe.relations.print.dft;

import org.elbe.relations.services.IPrintOut;
import org.elbe.relations.services.IPrintService;

/**
 * This bundle's provider for the OSGi <code>IPrintService</code>.
 *
 * @author Luthiger
 */
public class PrinterComponent implements IPrintService {

	@Override
	public String getName() {
		return "Simple text file"; //$NON-NLS-1$
	}

	@Override
	public String getFileType() {
		return "*.txt"; //$NON-NLS-1$
	}

	@Override
	public String getFileTypeName() {
		return "Text file (.txt)"; //$NON-NLS-1$
	}

	@Override
	public IPrintOut getPrinter() {
		return new PrintOut();
	}

}
