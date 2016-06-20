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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.swt.graphics.Image;
import org.elbe.relations.RelationsImages;
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
public class BindingElementTest {

	@Mock
	private KeyController controller;

	private CommandManager commandManager;
	private Command command;

	@Before
	public void setUp() throws Exception {
		commandManager = new CommandManager();
	}

	@Test
	public void testSetImage() {
		final BindingElement binding = new BindingElement(controller);
		final Image image1 = RelationsImages.ITALIC.getImage();
		binding.setImage(image1);
		verify(controller).firePropertyChange(binding, "image", null, image1);
		assertEquals(image1, binding.getImage());

		final Image image2 = RelationsImages.BOLD.getImage();
		binding.setImage(image2);
		verify(controller).firePropertyChange(binding, "image", image1, image2);
		assertEquals(image2, binding.getImage());
	}

	@Test
	public void testSetTrigger() throws Exception {
		final BindingElement binding = new BindingElement(controller);
		final TriggerSequence trigger1 = KeySequence.getInstance("M1+A");
		binding.setTrigger(trigger1);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_TRIGGER, null, trigger1);
		assertEquals(trigger1, binding.getTrigger());

		final TriggerSequence trigger2 = KeySequence.getInstance("M1+B");
		binding.setTrigger(trigger2);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_TRIGGER, trigger1, trigger2);
		assertEquals(trigger2, binding.getTrigger());
	}

	@Test
	public void testSetContext() throws Exception {
		final BindingElement binding = new BindingElement(controller);
		final ContextElement context1 = new ContextElement(controller);
		binding.setContext(context1);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_CONTEXT, null, context1);
		assertEquals(context1, binding.getContext());

		final ContextElement context2 = new ContextElement(controller);
		binding.setContext(context2);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_CONTEXT, context1, context2);
		assertEquals(context2, binding.getContext());
	}

	@Test
	public void testSetUserDelta() throws Exception {
		final BindingElement binding = new BindingElement(controller);
		final Integer userDelta1 = Integer.valueOf(11);
		binding.setUserDelta(userDelta1);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_USER_DELTA, null, userDelta1);
		assertEquals(userDelta1, binding.getUserDelta());

		final Integer userDelta2 = Integer.valueOf(11);
		binding.setUserDelta(userDelta2);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_USER_DELTA, userDelta1, userDelta2);
		assertEquals(userDelta2, binding.getUserDelta());
	}

	@Test
	public void testSetConflict() throws Exception {
		final BindingElement binding = new BindingElement(controller);
		final Boolean conflict1 = Boolean.FALSE;
		binding.setConflict(conflict1);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_CONFLICT, null, conflict1);
		assertEquals(conflict1, binding.getConflict());

		final Boolean conflict2 = Boolean.TRUE;
		binding.setConflict(conflict2);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_CONFLICT, conflict1, conflict2);
		assertEquals(conflict2, binding.getConflict());
	}

	@Test
	public void testSetCategory() throws Exception {
		final BindingElement binding = new BindingElement(controller);
		final String category1 = "aaa";
		binding.setCategory(category1);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_CATEGORY, null, category1);
		assertEquals(category1, binding.getCategory());

		final String category2 = "aaa";
		binding.setCategory(category2);
		verify(controller).firePropertyChange(binding, BindingElement.PROP_CATEGORY, category1, category2);
		assertEquals(category2, binding.getCategory());
	}

	@Test
	public void testInit1() throws Exception {
		final BindingElement binding = new BindingElement(controller);
		command = commandManager.getCommand("aa");
		final ParameterizedCommand parametrized1 = new ParameterizedCommand(command, null);
		binding.init(parametrized1);
		assertEquals("aa", binding.getId());
		assertEquals("Undefined Command", binding.getName());
		assertEquals("", binding.getDescription());
		assertEquals("Unavailable Category", binding.getCategory());
		verify(controller).firePropertyChange(binding, BindingElement.PROP_MODEL_OBJECT, null, parametrized1);

		command.define("cmd name", "Command for testing purpose", commandManager.getCategory("test_catgory"));
		final ParameterizedCommand parametrized2 = new ParameterizedCommand(command, null);
		binding.init(parametrized2);
		assertEquals("aa", binding.getId());
		assertEquals("cmd name", binding.getName());
		assertEquals("Command for testing purpose", binding.getDescription());
		verify(controller).firePropertyChange(binding, BindingElement.PROP_MODEL_OBJECT, parametrized1, parametrized2);
	}

	@Test
	public void testInit2() throws Exception {
		final BindingElement bindingModel = new BindingElement(controller);

		command = commandManager.getCommand("aa");
		final List<MBindingContext> contexts = TestBindingContext.createContexts();
		final ContextModel context = new ContextModel(controller);
		context.init(contexts);

		final Binding binding1 = createBinding(command, "a", KeySequence.getInstance("M1+A"));
		bindingModel.init(binding1, context);
		verify(controller).firePropertyChange(bindingModel, BindingElement.PROP_MODEL_OBJECT, null, binding1);
		assertEquals(KeySequence.getInstance("M1+A").toString(), bindingModel.getTrigger().toString());
		assertNull(bindingModel.getContext());
		assertEquals(Integer.valueOf(0), bindingModel.getUserDelta());
		assertEquals(binding1, bindingModel.getModelObject());
		assertEquals("aa", bindingModel.getId());
		assertEquals("Undefined Command", bindingModel.getName());
		assertEquals("", bindingModel.getDescription());
		assertEquals("Unavailable Category", bindingModel.getCategory());

		command.define("cmd name", "Command for testing purpose", commandManager.getCategory("test_catgory"));
		final Binding binding2 = createBinding(command, "b", KeySequence.getInstance("M1+B"));
		bindingModel.init(binding2, context);
		verify(controller).firePropertyChange(bindingModel, BindingElement.PROP_MODEL_OBJECT, binding1, binding2);
		assertEquals(KeySequence.getInstance("M1+B").toString(), bindingModel.getTrigger().toString());
		assertNull(bindingModel.getContext());
		assertEquals(Integer.valueOf(0), bindingModel.getUserDelta());
		assertEquals(binding2, bindingModel.getModelObject());
		assertEquals("aa", bindingModel.getId());
		assertEquals("cmd name", bindingModel.getName());
		assertEquals("Command for testing purpose", bindingModel.getDescription());
	}

	@Test
	public void testFill1() throws Exception {
		final BindingElement binding = new BindingElement(controller);
		command = commandManager.getCommand("aa");
		final ParameterizedCommand parametrized1 = new ParameterizedCommand(command, null);
		binding.fill(parametrized1);
		assertEquals("aa", binding.getId());
		assertEquals("Undefined Command", binding.getName());
		assertEquals("", binding.getDescription());
		assertEquals("Unavailable Category", binding.getCategory());
		verify(controller).firePropertyChange(binding, BindingElement.PROP_MODEL_OBJECT, null, parametrized1);

		command.define("cmd name", "Command for testing purpose", commandManager.getCategory("test_catgory"));
		final ParameterizedCommand parametrized2 = new ParameterizedCommand(command, null);
		binding.fill(parametrized2);
		assertEquals("aa", binding.getId());
		assertEquals("cmd name", binding.getName());
		assertEquals("Command for testing purpose", binding.getDescription());
		verify(controller).firePropertyChange(binding, BindingElement.PROP_MODEL_OBJECT, parametrized1, parametrized2);
	}

	@Test
	public void testFill2() throws Exception {
		final BindingElement bindingModel = new BindingElement(controller);

		command = commandManager.getCommand("aa");
		final List<MBindingContext> contexts = TestBindingContext.createContexts();
		final ContextModel context = new ContextModel(controller);
		context.init(contexts);

		final KeyBinding binding1 = createBinding(command, "a", KeySequence.getInstance("M1+A"));
		bindingModel.fill(binding1, context);
		verify(controller).firePropertyChange(bindingModel, BindingElement.PROP_MODEL_OBJECT, null, binding1);
		assertEquals(KeySequence.getInstance("M1+A").toString(), bindingModel.getTrigger().toString());
		assertNull(bindingModel.getContext());
		assertEquals(Integer.valueOf(0), bindingModel.getUserDelta());
		assertEquals(binding1, bindingModel.getModelObject());
		assertEquals("aa", bindingModel.getId());
		assertEquals("Undefined Command", bindingModel.getName());
		assertEquals("", bindingModel.getDescription());
		assertEquals("Unavailable Category", bindingModel.getCategory());

		command.define("cmd name", "Command for testing purpose", commandManager.getCategory("test_catgory"));
		final KeyBinding binding2 = createBinding(command, "b", KeySequence.getInstance("M1+B"));
		bindingModel.fill(binding2, context);
		verify(controller).firePropertyChange(bindingModel, BindingElement.PROP_MODEL_OBJECT, binding1, binding2);
		assertEquals(KeySequence.getInstance("M1+B").toString(), bindingModel.getTrigger().toString());
		assertNull(bindingModel.getContext());
		assertEquals(Integer.valueOf(0), bindingModel.getUserDelta());
		assertEquals(binding2, bindingModel.getModelObject());
		assertEquals("aa", bindingModel.getId());
		assertEquals("cmd name", bindingModel.getName());
		assertEquals("Command for testing purpose", bindingModel.getDescription());
	}

	private KeyBinding createBinding(Command command, String contextId, KeySequence keySequence) {
		return new KeyBinding(keySequence, new ParameterizedCommand(command, null), "default", contextId, null, null,
				null, Binding.SYSTEM);
	}
}
