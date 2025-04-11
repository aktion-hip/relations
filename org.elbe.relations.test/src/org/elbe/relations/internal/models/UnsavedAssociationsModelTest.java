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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * JUnit Plug-in test
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
@ExtendWith(MockitoExtension.class)
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

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        data = DataHouseKeeper.INSTANCE;
    }

    @BeforeEach
    public void setUp() throws Exception {
        this.image = new Image(this.device, 1, 1);

        this.context = EclipseContextFactory.create("test context");
        this.context.set(Logger.class, this.log);
        this.context.set(IDataService.class, this.dataService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void testGetElements() throws Exception {
        final ItemAdapter lItem = new ItemAdapter(data.createTerm("existing"),
                this.image, this.context);
        assertEquals(1, data.getTermHome().getCount());

        final UnsavedAssociationsModel lModel = UnsavedAssociationsModel
                .createModel(createDummy(), this.context, lItem);

        final Object[] lElements = lModel.getElements();
        assertEquals(1, lElements.length);
        assertEquals(0, data.getRelationHome().getCount());
        assertEquals(1, data.getTermHome().getCount());
    }

    @Test
    public void testSaveChanges() throws Exception {
        final ItemAdapter lItem = new ItemAdapter(data.createTerm("existing"),
                this.image, this.context);
        assertEquals(1, data.getTermHome().getCount());
        assertEquals(0, data.getRelationHome().getCount());

        final UnsavedAssociationsModel lModel = UnsavedAssociationsModel
                .createModel(createDummy(), this.context, lItem);

        final AbstractPerson lPerson1 = data.createPerson("person1", "1");
        final AbstractPerson lPerson2 = data.createPerson("person2", "2");
        final AbstractPerson lPerson3 = data.createPerson("person3", "3");
        final int lType = lPerson1.getItemType();
        final UniqueID[] lAdd = new UniqueID[] {
                new UniqueID(lType, lPerson1.getID()),
                new UniqueID(lType, lPerson2.getID()),
                new UniqueID(lType, lPerson3.getID()) };
        lModel.addAssociations(lAdd);

        final ItemAdapter lNew = new ItemAdapter(data.createTerm("new"), this.image,
                this.context);
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
