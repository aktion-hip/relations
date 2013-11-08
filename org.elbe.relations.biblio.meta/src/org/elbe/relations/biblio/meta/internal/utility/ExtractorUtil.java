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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Helper class for metadata extraction from files.
 *
 * @author Luthiger
 * Created on 14.01.2010
 */
public class ExtractorUtil {
	
	/**
	 * Returns a numerical value from a datasource
	 * 
	 * @param inDataSource FileDataSource
	 * @param inSize number of bytes to read
	 * @param inGetUpper true if the bytes read are from upper end.
	 * @return long The number read from the datasource.
	 * @throws IOException
	 */
	public static long getNumericalValue(FileDataSource inDataSource, int inSize, boolean inGetUpper) throws IOException {
		byte[] lBytes = inDataSource.getData(inSize);
		return getNumericalValue(lBytes, inGetUpper);
	}

	/**
	 * returns an integer that represents the bytes passed in
	 * 
	 * @param inBytes
	 * @param inGetUpper true if the bytes read are from upper end.
	 * @return The numerical value of the bytes.
	 */
	public static long getNumericalValue(byte[] inBytes, boolean inGetUpper) {
		long out = -1;
		if (!inGetUpper) {
			out = getLowerEnd(inBytes, 0, inBytes.length);
		} 
		else {
			out = getUpperEnd(inBytes, 0, inBytes.length);
		}
		return out;
	}
	

	/**
	 * Takes some bytes and returns the Little Endian version of those bytes as an integer.
	 * 
	 * @param inBytes byte[] the block of data containing the number
	 * @param inOffset int the offset to start processing from
	 * @param inSize int the sizer of the number in bytes
	 * @return long
	 */
	protected static long getLowerEnd(final byte[] inBytes, final int inOffset, final int inSize) {
		long outResult = 0;

		for (int j = inOffset + inSize - 1; j >= inOffset; j--) {
			outResult <<= 8;
			outResult |= 0xff & inBytes[j];
		}
		return outResult;
	}

	/**
	 * Takes some bytes and returns the Big Endian version of those bytes as an integer.
	 * 
	 * @param inBytes byte[] the block of data containing the number
	 * @param inOffset int the offset to start processing from
	 * @param inSize int the sizer of the number in bytes
	 * @return long
	 */
	protected static long getUpperEnd(final byte[] inBytes, final int inOffset, final int inSize) {
		long outResult = 0;

		for (int j = inOffset; j < inOffset + inSize; j++) {
			outResult <<= 8;
			outResult |= 0xff & inBytes[j];
		}
		return outResult;
	}

	/**
	 * Returns a string of a fixed length from a datasource.
	 * 
	 * @param inSource FileDataSource
	 * @param inLength int
	 * @return String
	 * @throws IOException
	 */
	public static String getFixedStringValue(FileDataSource inSource, int inLength) throws IOException {
		return new String(inSource.getData(inLength));
	}
	
	/**
	 * Helper method for testing a file header.
	 * 
	 * @param inFile {@link File} The file to test.
	 * @param inTest {@link String} A space separated string of hex bytes that will be at the
	 *        start of a file. To skip bytes in the header, use "xx". As an
	 *        example, the WaveAdapter uses the string "52 49 46 46 xx xx xx
	 *        xx 57 41 56 45 66 6D 74 20".
	 * @return boolean <code>true</code> if the string is at the start of the file. 
	 * 			<code>false</code> is it isn't or if there are any exceptions accessing/parsing the file.
	 */
	public static boolean checkFileHeader(File inFile, String inTest) {
		int lLength = (inTest.length() + 1) / 3;
		int[] lValue = new int[lLength];
		int[] lMask = new int[lLength];

		StringTokenizer lTokenizer = new StringTokenizer(inTest, " "); //$NON-NLS-1$
		String lToken = null;

		// Split the string into an array of integers.
		for (int i = 0; lTokenizer.hasMoreTokens(); i++) {
			lToken = lTokenizer.nextToken();

			// If the token is xx, set the mask value to zeros so the test
			// of this byte is always successful.
			if ("xx".equals(lToken)) { //$NON-NLS-1$
				lValue[i] = 0;
				lMask[i] = 0;
			}

			// If the token is not xx, set the mask to 0xFF and set the test
			// character.
			else {
				lValue[i] = Integer.parseInt(lToken, 16);
				lMask[i] = 0xFF;
			}
		}

		// Call the method with the calculated arrays.
		return checkFileHeader(inFile, lValue, lMask);
	}

	
	private static boolean checkFileHeader(File inFile, int[] inTest, int[] inMask) {
		FileInputStream lStream = null;

		int lLength = inTest.length;
		byte[] lBuffer = new byte[lLength];

		try {
			// Read the file.
			lStream = new FileInputStream(inFile);
			lStream.read(lBuffer);

			// Loop through all the bytes.
			for (int i = 0; i < inTest.length; i++) {
				// Mask and test the bytes.
				if (!((lBuffer[i] & inMask[i]) == inTest[i])) {
					return false;
				}
			}
			return true;
		} 
		catch (IOException exc) {
			//do nothing
		} 
		finally {
			try {
				lStream.close();
			} 
			catch (Exception exc) {}
		}
		return false;
	}

	/**
	 * 
	 * @param inAscii {@link String}
	 * @return String
	 */
	public static String toHexFilter(String inAscii) { 
		StringBuilder out = new StringBuilder();
		
		char[] lChars = inAscii.toCharArray();
		for(int i=0; i<lChars.length; i++) { 
			out.append(Integer.toHexString((int) lChars[i]));
			if(i < (lChars.length-1)) { 
				out.append(" "); //$NON-NLS-1$
			}
		}
		return new String(out);
	}
	
}
