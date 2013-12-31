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

package org.elbe.relations.internal.actions;

import static org.junit.Assert.assertEquals;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.db.IDataService;
import org.hip.kernel.bom.DomainObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit Plug-in test
 * 
 * @author lbenno
 */
@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class NewPersonActionTest {
	private static DataHouseKeeper data;

	@Mock
	private Logger log;
	@Mock
	private IDataService dataService;

	private IEclipseContext context;

	@BeforeClass
	public static void before() {
		data = DataHouseKeeper.INSTANCE;
	}

	@Before
	public void setUp() throws Exception {
		context = EclipseContextFactory.create("test context");
		context.set(Logger.class, log);
		context.set(IDataService.class, dataService);
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testExecute() throws Exception {
		final PersonHome lHome = data.getPersonHome();
		assertEquals(0, lHome.getCount());

		final String lFirst = "Jane";
		final String lLast = "Doe";
		final NewPersonAction lAction = new NewPersonAction.Builder(lLast)
		        .firstName(lFirst).build(context);
		lAction.execute();
		assertEquals("count 1", 1, lHome.getCount());

		final IItem lItem = lAction.getNewItem();
		final DomainObject lModel = lHome.findByKey(ActionTestHelper
		        .retrieve(lItem.getID()));
		assertEquals(lFirst, lModel.get(PersonHome.KEY_FIRSTNAME));
		assertEquals(lLast, lModel.get(PersonHome.KEY_NAME));
	}

}
