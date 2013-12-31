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

package org.elbe.relations.internal.models;

import static org.junit.Assert.assertEquals;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.LightWeightTerm;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.models.IItemModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.models.LightWeightAdapter;
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
public class UnsavedAssociationsModelTest {
	private static DataHouseKeeper data;

	@Mock
	private Device device;
	@Mock
	private Logger log;
	@Mock
	private IDataService dataService;

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
		context.set(Logger.class, log);
		context.set(IDataService.class, dataService);
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testGetElements() throws Exception {
		final ItemAdapter lItem = new ItemAdapter(data.createTerm("existing"),
		        image, context);
		assertEquals(1, data.getTermHome().getCount());

		final UnsavedAssociationsModel lModel = UnsavedAssociationsModel
		        .createModel(createDummy(), context, lItem);

		final Object[] lElements = lModel.getElements();
		assertEquals(1, lElements.length);
		assertEquals(0, data.getRelationHome().getCount());
		assertEquals(1, data.getTermHome().getCount());
	}

	@Test
	public void testSaveChanges() throws Exception {
		final ItemAdapter lItem = new ItemAdapter(data.createTerm("existing"),
		        image, context);
		assertEquals(1, data.getTermHome().getCount());
		assertEquals(0, data.getRelationHome().getCount());

		final UnsavedAssociationsModel lModel = UnsavedAssociationsModel
		        .createModel(createDummy(), context, lItem);

		final AbstractPerson lPerson1 = data.createPerson("person1", "1");
		final AbstractPerson lPerson2 = data.createPerson("person2", "2");
		final AbstractPerson lPerson3 = data.createPerson("person3", "3");
		final int lType = lPerson1.getItemType();
		final UniqueID[] lAdd = new UniqueID[] {
		        new UniqueID(lType, lPerson1.getID()),
		        new UniqueID(lType, lPerson2.getID()),
		        new UniqueID(lType, lPerson3.getID()) };
		lModel.addAssociations(lAdd);

		final ItemAdapter lNew = new ItemAdapter(data.createTerm("new"), image,
		        context);
		lModel.replaceCenter(lNew);
		assertEquals(2, data.getTermHome().getCount());

		lModel.saveChanges();
		assertEquals(4, data.getRelationHome().getCount());
	}

	private IItemModel createDummy() {
		final LightWeightTerm lItem = new LightWeightTerm(0, "", "", null, null);
		return new LightWeightAdapter(lItem);
	}

}
