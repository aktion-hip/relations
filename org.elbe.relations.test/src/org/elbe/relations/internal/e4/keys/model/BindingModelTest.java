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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author lbenno
 */
@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    public void setUp() throws Exception {
        this.contextManager = new ContextManager();
        this.commandManager = new CommandManager();
        this.bindings = new ArrayList<Binding>();

        this.command1 = this.commandManager.getCommand(COMMAND_IDS[0]);
        this.command2 = this.commandManager.getCommand(COMMAND_IDS[1]);
        this.command3 = this.commandManager.getCommand(COMMAND_IDS[2]);
        this.bindings.add(createBinding(this.command1, "a", KeySequence.getInstance("M1+A")));
        this.bindings.add(createBinding(this.command2, "b", KeySequence.getInstance("M1+B")));
        this.bindings.add(createBinding(this.command3, "c", KeySequence.getInstance("M1+C")));

        this.bindingManager = new BindingManager(this.contextManager, this.commandManager);
        this.bindingManager.addBinding(this.bindings.get(0));
        this.bindingManager.addBinding(this.bindings.get(1));
        this.bindingManager.addBinding(this.bindings.get(2));
        final Scheme scheme = this.bindingManager.getScheme("default");
        scheme.define("myScheme1", "My scheme for testing", null);
        this.bindingManager.setActiveScheme(scheme);
    }

    private Binding createBinding(final Command command, final String contextId, final KeySequence keySequence) {
        return new KeyBinding(keySequence, new ParameterizedCommand(command, null), "default", contextId, null, null,
                null, Binding.SYSTEM);
    }

    @Test
    public void testInit() {
        final ContextModel context = new ContextModel(this.controller);
        context.init(TestBindingContext.createContexts());

        final BindingModel bindingModel = new BindingModel(this.controller);
        bindingModel.init(this.bindingManager, this.commandManager, null, context);

        final Set<BindingElement> bindingElements = bindingModel.getBindings();
        assertEquals(3, bindingElements.size());
        final List<String> commandIds = Arrays.asList(COMMAND_IDS);
        for (final BindingElement bindingEl : bindingElements) {
            assertTrue(commandIds.contains(bindingEl.getId()));
            System.out.println(bindingEl.getTrigger().toString());
        }

        final Map<Binding, BindingElement> map = bindingModel.getBindingToElement();
        for (final Binding binding : this.bindings) {
            assertEquals(binding.getParameterizedCommand().getId(), map.get(binding).getId());
        }
    }

    @Test
    public void testRefresh() throws Exception {
        final BindingModel bindingModel = new BindingModel(this.controller);

        final List<MBindingContext> contexts = TestBindingContext.createContexts();
        final ContextModel context1 = new ContextModel(this.controller);
        context1.init(contexts);
        bindingModel.init(this.bindingManager, this.commandManager, null, context1).refresh(context1);
        Set<BindingElement> bindingElements = bindingModel.getBindings();
        assertEquals(3, bindingElements.size());

        contexts.add(TestBindingContext.createBindingContext("new"));
        this.bindingManager
        .addBinding(createBinding(this.commandManager.getCommand("NN"), "nn", KeySequence.getInstance("M1+N")));
        bindingModel.refresh(new ContextModel(this.controller).init(contexts));
        bindingElements = bindingModel.getBindings();
        assertEquals(4, bindingElements.size());
    }

}
