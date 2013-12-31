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

package org.elbe.relations.biblio.meta.internal.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import org.elbe.relations.parsing.ExtractedData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author lbenno
 */
public class ExcelExtractorTest {

	private static final String NL = System.getProperty("line.separator");
	private static final String FILE_NAME = "resources/excelExtractorTest.xls";
	private static final String FILE_NAME2 = "resources/dummy.txt";

	private File file;
	private Locale localeOld;

	@Before
	public void setUp() throws Exception {
		localeOld = Locale.getDefault();
		Locale.setDefault(Locale.US);

		file = new File(FILE_NAME);
		if (!file.exists())
			throw new FileNotFoundException(FILE_NAME);
	}

	@After
	public void tearDown() throws Exception {
		Locale.setDefault(localeOld);
	}

	@Test
	public void testAcceptsFile() throws Exception {
		final ExcelExtractor lExtractor = new ExcelExtractor();
		assertTrue("can process", lExtractor.acceptsFile(file));

		assertFalse("can't process dummy",
		        lExtractor.acceptsFile(new File(FILE_NAME2)));
	}

	@Test
	public void testProcess() throws Exception {
		final AbstractMSOfficeExtractor lExtractor = new ExcelExtractor();
		final ExtractedData lExtacted = lExtractor.process(file);

		assertEquals("extracted title", "Metadata Extractor Test Workbook",
		        lExtacted.getTitle());

		lExtacted.setFilePath("");
		final String lExpected = "This Excel Workbook is for testing only."
		        + NL + "[<i>Author: Luthiger;" + NL + "Size: 16.00 kB;" + NL
		        + "Type: application/vnd.ms-excel;" + NL
		        + "Created: September 6, 2007, 12:24:19 AM CEST;" + NL
		        + "Last Modified: December 15, 2013, 11:02:21 PM CET</i>]";
		assertEquals("extracted comment", lExpected, lExtacted.getText());
	}

}
