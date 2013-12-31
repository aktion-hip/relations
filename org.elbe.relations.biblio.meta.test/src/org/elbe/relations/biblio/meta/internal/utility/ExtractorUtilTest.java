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

package org.elbe.relations.biblio.meta.internal.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author lbenno
 */
public class ExtractorUtilTest {
	private static final String FILE_NAME = "resources/pdfExtractorTest.pdf";
	private static final String FILE_NAME2 = "resources/dummy.txt";

	private File file;

	@Before
	public void setUp() throws Exception {
		file = new File(FILE_NAME);
		if (!file.exists())
			throw new FileNotFoundException(FILE_NAME);
	}

	@Test
	public final void testGetNumericalValueFileDataSourceIntBoolean()
	        throws IOException {
		final FileDataSource lSource = new FileDataSource(file);
		assertEquals(2445380, ExtractorUtil.getNumericalValue(lSource, 3, true));
	}

	@Test
	public final void testGetNumericalValueByteArrayBoolean() {
		final byte[] lInput = new byte[] { 1, 2, 3, 4 };
		assertEquals(16909060, ExtractorUtil.getNumericalValue(lInput, true));
	}

	@Test
	public final void testGetFixedStringValue() throws IOException {
		final FileDataSource lSource = new FileDataSource(file);
		assertEquals("%PDF-1.4", ExtractorUtil.getFixedStringValue(lSource, 8));
	}

	@Test
	public final void testCheckFileHeader() {
		assertTrue(
		        "check for pdf 1",
		        ExtractorUtil.checkFileHeader(file,
		                ExtractorUtil.toHexFilter("%PDF")));
		assertFalse("check for pdf 2", ExtractorUtil.checkFileHeader(new File(
		        FILE_NAME2), ExtractorUtil.toHexFilter("%PDF")));
	}

	@Test
	public final void testToHexFilter() {
		assertEquals("value for '%pdf'", "25 70 64 66",
		        ExtractorUtil.toHexFilter("%pdf"));
		assertEquals("value for '%pdf'", "25 50 44 46",
		        ExtractorUtil.toHexFilter("%PDF"));
	}

}
