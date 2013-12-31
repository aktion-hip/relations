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

package org.elbe.relations.internal.controller;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.hip.kernel.bom.AlternativeModel;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit test
 * 
 * @author lbenno
 */
@RunWith(MockitoJUnitRunner.class)
public class LastChangesControllerTest {
	private static DataHouseKeeper data;

	@BeforeClass
	public static void before() {
		data = DataHouseKeeper.INSTANCE;
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testLastChangedItems() throws Exception {
		final String[] lExpectedLastCreated = { "Text 2", "Text 1", "Foo, Joe",
		        "Term 3", "Term 2", "Term 1", "Doe, Jane" };
		final String[] lExpectedLastModified = { "Term 3", "Text 2", "Text 1",
		        "Foo, Joe", "Term 2", "Term 1", "Doe, Jane" };

		data.createPerson("Doe", "Jane");
		sleep(1000);
		data.createTerm("Term 1", "");
		sleep(1000);
		data.createTerm("Term 2", "");
		sleep(1000);
		final AbstractTerm lToModify = data.createTerm("Term 3", "");
		sleep(1000);
		data.createPerson("Foo", "Joe");
		sleep(1000);
		data.createText("Text 1", "None");
		sleep(1000);
		data.createText("Text 2", "None");

		final LastChangesController lController = new LastChangesController();
		lController.setViewState("true");
		assertCollection("last created", lExpectedLastCreated,
		        lController.getLastChangedItems());

		lController.setViewState("false");
		assertCollection("last modified 0", lExpectedLastCreated,
		        lController.getLastChangedItems());
		lToModify.save(lToModify.getTitle(), "modified");
		assertCollection("last modified 1", lExpectedLastModified,
		        lController.getLastChangedItems());
	}

	private void assertCollection(final String inLabel,
	        final String[] inExpected, final Collection<AlternativeModel> lItems) {
		int i = 0;
		for (final AlternativeModel lItem : lItems) {
			assertEquals(inLabel + " " + i, inExpected[i++], lItem.toString());
		}
	}

	private void sleep(final int inMillies) {
		final long lStart = System.currentTimeMillis();
		while (System.currentTimeMillis() < lStart + inMillies) {
			// loop
		}
	}

}
