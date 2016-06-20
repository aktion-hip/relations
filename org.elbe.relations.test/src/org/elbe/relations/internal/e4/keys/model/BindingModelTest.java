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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.elbe.relations.internal.e4.keys.KeyController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author lbenno
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BindingModelTest {
	private static final String[] COMMAND_IDS = { "aa", "bb", "cc" };

	@Mock
	private KeyController controller;

	private ContextManager contextManager;
	private CommandManager commandManager;
	private BindingManager bindingManager;

	private Command command1;
	private Command command2;
	private Command command3;

	private List<Binding> bindings;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		contextManager = new ContextManager();
		commandManager = new CommandManager();
		bindings = new ArrayList<Binding>();

		command1 = commandManager.getCommand(COMMAND_IDS[0]);
		command2 = commandManager.getCommand(COMMAND_IDS[1]);
		command3 = commandManager.getCommand(COMMAND_IDS[2]);
		bindings.add(createBinding(command1, "a", KeySequence.getInstance("M1+A")));
		bindings.add(createBinding(command2, "b", KeySequence.getInstance("M1+B")));
		bindings.add(createBinding(command3, "c", KeySequence.getInstance("M1+C")));

		bindingManager = new BindingManager(contextManager, commandManager);
		bindingManager.addBinding(bindings.get(0));
		bindingManager.addBinding(bindings.get(1));
		bindingManager.addBinding(bindings.get(2));
		final Scheme scheme = bindingManager.getScheme("default");
		scheme.define("myScheme1", "My scheme for testing", null);
		bindingManager.setActiveScheme(scheme);
	}

	private Binding createBinding(Command command, String contextId, KeySequence keySequence) {
		return new KeyBinding(keySequence, new ParameterizedCommand(command, null), "default", contextId, null, null,
				null, Binding.SYSTEM);
	}

	@Test
	public void testInit() {
		final ContextModel context = new ContextModel(controller);
		context.init(TestBindingContext.createContexts());

		final BindingModel bindingModel = new BindingModel(controller);
		bindingModel.init(bindingManager, commandManager, null, context);

		final Set<BindingElement> bindingElements = bindingModel.getBindings();
		assertEquals(3, bindingElements.size());
		final List<String> commandIds = Arrays.asList(COMMAND_IDS);
		for (final BindingElement bindingEl : bindingElements) {
			assertTrue(commandIds.contains(bindingEl.getId()));
			System.out.println(bindingEl.getTrigger().toString());
		}

		final Map<Binding, BindingElement> map = bindingModel.getBindingToElement();
		for (final Binding binding : bindings) {
			assertEquals(binding.getParameterizedCommand().getId(), map.get(binding).getId());
		}
	}

	@Test
	public void testRefresh() throws Exception {
		final BindingModel bindingModel = new BindingModel(controller);

		final List<MBindingContext> contexts = TestBindingContext.createContexts();
		final ContextModel context1 = new ContextModel(controller);
		context1.init(contexts);
		bindingModel.init(bindingManager, commandManager, null, context1).refresh(context1);
		Set<BindingElement> bindingElements = bindingModel.getBindings();
		assertEquals(3, bindingElements.size());

		contexts.add(TestBindingContext.createBindingContext("new"));
		bindingManager
				.addBinding(createBinding(commandManager.getCommand("NN"), "nn", KeySequence.getInstance("M1+N")));
		bindingModel.refresh(new ContextModel(controller).init(contexts));
		bindingElements = bindingModel.getBindings();
		assertEquals(4, bindingElements.size());
	}

}
