/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.print.doc;

import org.elbe.relations.services.IPrintOut;
import org.elbe.relations.services.IPrintService;

/**
 * The component to output to <code>docs</code> files.
 *
 * @author lbenno
 */
public class PrinterComponent implements IPrintService {

	@Override
	public String getName() {
		return "MSWord document"; //$NON-NLS-1$
	}

	@Override
	public String getFileType() {
		return "*.docx"; //$NON-NLS-1$
	}

	@Override
	public String getFileTypeName() {
		return "Microsoft Word (.docx)"; //$NON-NLS-1$
	}

	@Override
	public IPrintOut getPrinter() {
		return new PrintOut();
	}

}
