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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author lbenno
 */
@ExtendWith(MockitoExtension.class)
public class ItemAdapterTest {
    private static DataHouseKeeper data;

    @Mock
    private Device device;

    private AbstractTerm item;
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

        this.item = data.createTerm("Item 1");
    }

    @AfterEach
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void test() throws Exception {
        final ItemAdapter lAdapted1 = new ItemAdapter(this.item, this.image, this.context);
        final ItemAdapter lAdapted2 = new ItemAdapter(this.item, this.image, this.context);
        assertTrue(lAdapted1.equals(lAdapted2));
        assertEquals(lAdapted1, lAdapted2);

        final IItem lFound = data.getTermHome().getTerm(this.item.getID());
        assertTrue(lAdapted1.equals(new ItemAdapter(lFound, this.image, this.context)));

        final IItem lNew = data.createTerm("New");
        assertFalse(lAdapted1.equals(new ItemAdapter(lNew, this.image, this.context)));
    }

}
