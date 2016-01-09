/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.internal.e4.keys.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.elbe.relations.internal.e4.keys.KeyController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author lbenno
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextModelTest {
	private static final String CONTEXT_ID_ACTION_SETS = "org.eclipse.ui.contexts.actionSet"; //$NON-NLS-1$
	private static final String CONTEXT_ID_INTERNAL = ".internal."; //$NON-NLS-1$

	@Mock
	private KeyController controller;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testInit() {
		final List<MBindingContext> contexts = TestBindingContext.createContexts();

		final ContextModel context = new ContextModel(controller);
		context.init(contexts);
		assertEquals(6, context.getContexts().size());
		assertEquals(TestBindingContext.CONTEXT_IDS[4], context.getContextIdToElement().get("1").getId());
		assertEquals(TestBindingContext.CONTEXT_IDS[5], context.getContextIdToElement().get("2").getId());
		assertEquals(TestBindingContext.CONTEXT_IDS[0], context.getContextIdToElement().get("1.1").getId());
		assertEquals(TestBindingContext.CONTEXT_IDS[1], context.getContextIdToElement().get("1.2").getId());
		assertEquals(TestBindingContext.CONTEXT_IDS[2], context.getContextIdToElement().get("2.1").getId());
		assertEquals(TestBindingContext.CONTEXT_IDS[3], context.getContextIdToElement().get("2.2").getId());
	}

	@Test
	public void testFilter() throws Exception {
		final List<MBindingContext> contexts = TestBindingContext.createContexts();
		contexts.get(0).getChildren().get(0).setElementId(CONTEXT_ID_ACTION_SETS);
		contexts.get(0).getChildren().get(1).setElementId(CONTEXT_ID_ACTION_SETS);
		contexts.get(1).getChildren().get(0).setElementId(CONTEXT_ID_INTERNAL);

		final ContextModel contextModel = new ContextModel(controller);
		contextModel.init(contexts);
		assertEquals(6, contextModel.getContexts().size());
		contextModel.filterContexts(true, true);
		assertEquals(4, contextModel.getContexts().size());
		contextModel.filterContexts(true, false);
		assertEquals(4, contextModel.getContexts().size());
		contextModel.filterContexts(false, true);
		assertEquals(4, contextModel.getContexts().size());
		contextModel.filterContexts(false, false);
		assertEquals(5, contextModel.getContexts().size());
	}

}
