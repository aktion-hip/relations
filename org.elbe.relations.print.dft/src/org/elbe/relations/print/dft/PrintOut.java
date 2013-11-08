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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.elbe.relations.services.IPrintOut;
import org.elbe.relations.utility.AbstractPrintOut;

/**
 * Print out items to a simple text file.
 * 
 * @author Luthiger Created on 16.01.2007
 */
public class PrintOut extends AbstractPrintOut implements IPrintOut {
	private final static String XSL_CONTENT = "print_dft.xsl"; //$NON-NLS-1$
	private final static String XSL_DOCBODY = "print_dftBody.xsl"; //$NON-NLS-1$

	private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	private RandomAccessFile outputFile = null;

	/**
	 * This plug-in returns always true.
	 * 
	 * @see org.elbe.relations.print.IPrintOut#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return true;
	}

	private void appendText(final String inText) throws IOException {
		if (outputFile == null)
			return;
		try {
			outputFile.seek(outputFile.length());
			outputFile.writeBytes(inText);
		}
		catch (final FileNotFoundException exc) {
			throw createIOException(exc);
		}
	}

	protected String getXSLNameBody() {
		return XSL_DOCBODY;
	}

	protected String getXSLNameContent() {
		return XSL_CONTENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.print.AbstractPrintOut#manageAfterOpenNew(java.io.
	 * File)
	 */
	protected void manageAfterOpenNew(final File inPrintOut) throws IOException {
		if (inPrintOut.createNewFile()) {
			if (!inPrintOut.canRead() || !inPrintOut.canWrite()) {
				throw new IOException(
						"Could not open file for read/write: " + inPrintOut.getName()); //$NON-NLS-1$
			}
			outputFile = new RandomAccessFile(inPrintOut, "rw"); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.print.AbstractPrintOut#insertSection(java.lang.String)
	 */
	protected void insertSection(final String inSection) throws IOException {
		final String lSection = inSection.replaceAll("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		appendText(lSection + NL + NL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.print.AbstractPrintOut#insertDocBody(java.lang.String)
	 */
	protected void insertDocBody(final String inXML) throws IOException {
		appendText(inXML);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.print.AbstractPrintOut#manageAfterReopen(java.io.File)
	 */
	protected void manageAfterReopen(final File inPrintOut) throws IOException {
		try {
			outputFile = new RandomAccessFile(inPrintOut, "rw"); //$NON-NLS-1$
		}
		catch (final FileNotFoundException exc) {
			throw createIOException(exc);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elbe.relations.print.AbstractPrintOut#manageBeforeClose(java.io.File)
	 */
	protected void manageBeforeClose(final File inPrintOut) throws IOException {
		if (outputFile != null) {
			outputFile.close();
		}
	}

}
