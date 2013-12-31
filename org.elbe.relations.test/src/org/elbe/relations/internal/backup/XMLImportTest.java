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

package org.elbe.relations.internal.backup;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit test
 * 
 * @author lbenno
 */
@RunWith(MockitoJUnitRunner.class)
public class XMLImportTest {
	private static final String IMPORT_NAME = "/resources/export.xml";

	private static DataHouseKeeper data;

	@Mock
	private IProgressMonitor monitor;

	private XMLImport importer;

	@BeforeClass
	public static void before() {
		data = DataHouseKeeper.INSTANCE;
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testImport() throws Exception {
		// before import
		assertEquals(0, data.getTextHome().getCount());
		assertEquals(0, data.getTermHome().getCount());
		assertEquals(0, data.getPersonHome().getCount());
		assertEquals(0, data.getRelationHome().getCount());

		importer = new XMLImport(getPath());
		final int lImported = importer.processFile(monitor, false);
		assertEquals(4, lImported);

		// after import
		assertEquals(1, data.getTextHome().getCount());
		assertEquals(1, data.getTermHome().getCount());
		assertEquals(1, data.getPersonHome().getCount());
		assertEquals(1, data.getRelationHome().getCount());
	}

	private String getPath() {
		final URL lUrl = XMLImportTest.class.getResource(IMPORT_NAME);
		return lUrl.getPath();
	}

}
