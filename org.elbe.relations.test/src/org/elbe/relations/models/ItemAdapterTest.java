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

package org.elbe.relations.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author lbenno
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemAdapterTest {
	private static DataHouseKeeper data;

	@Mock
	private Device device;

	private AbstractTerm item;
	private IEclipseContext context;
	private Image image;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		data = DataHouseKeeper.INSTANCE;
	}

	@Before
	public void setUp() throws Exception {
		image = new Image(device, 1, 1);

		context = EclipseContextFactory.create("test context");

		item = data.createTerm("Item 1");
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void test() throws Exception {
		final ItemAdapter lAdapted1 = new ItemAdapter(item, image, context);
		final ItemAdapter lAdapted2 = new ItemAdapter(item, image, context);
		assertTrue("equals 0", lAdapted1.equals(lAdapted2));
		assertEquals("equals 1", lAdapted1, lAdapted2);

		final IItem lFound = data.getTermHome().getTerm(item.getID());
		assertTrue("equals 2",
		        lAdapted1.equals(new ItemAdapter(lFound, image, context)));

		final IItem lNew = data.createTerm("New");
		assertFalse("not equal",
		        lAdapted1.equals(new ItemAdapter(lNew, image, context)));
	}

}
