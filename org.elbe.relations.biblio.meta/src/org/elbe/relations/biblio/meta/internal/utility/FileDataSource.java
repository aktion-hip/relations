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
package org.elbe.relations.biblio.meta.internal.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Helper class for metadata extracting.
 * This class helps to extract data from a <code>File</code>.
 *
 * @author Luthiger
 * Created on 14.01.2010
 * @see File
 */
public class FileDataSource {
	
	private File file;
	private RandomAccessFile randomAccess;

	public FileDataSource(File inFile) {
		file = inFile;
	}

	/**
	 * Close this data source.
	 * 
	 * @throws IOException 
	 */
	public void close() throws IOException {
		getRandomAccess().close();
		randomAccess = null;
	}

	private RandomAccessFile getRandomAccess() throws FileNotFoundException {
		if (randomAccess == null) {
			randomAccess = new RandomAccessFile(file, "r"); //$NON-NLS-1$
		}
		return randomAccess;
	}

	/**
	 * Returns data from the data source.
	 * Reads from the current file pointer.
	 * 
	 * @param inSize int the amount of data to read (if available)
	 * @return byte[] the data read from the source.
	 * @throws IOException
	 */
	public byte[] getData(int inSize) throws IOException {
		RandomAccessFile lInput = getRandomAccess();
		long lActualAmount = lInput.length() - lInput.getFilePointer();
		if (inSize < lActualAmount) {
			lActualAmount = inSize;
		}
		
		byte[] outData = new byte[(int)lActualAmount];
		lInput.read(outData);
		return outData;
	}

	/**
	 * Gets the position of the file pointer.
	 * 
	 * @return long the current file pointer.
	 * @throws IOException
	 */
	public long getPosition() throws IOException {
		return getRandomAccess().getFilePointer();
	}

	/**
	 * Sets the position of the file pointer.
	 * 
	 * @param inPosition long the new position.
	 * @throws IOException
	 */
	public void setPosition(long inPosition) throws IOException {
		getRandomAccess().seek(inPosition);
	}

}