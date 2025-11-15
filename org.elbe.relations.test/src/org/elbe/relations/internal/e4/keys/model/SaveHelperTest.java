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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.internal.util.PrefUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author lbenno
 */
@ExtendWith(MockitoExtension.class)
class SaveHelperTest {
    private static final String BINDING_CONTEXT_ID = "ctxID";

    private CommandManager commandManager;
    private Command command1;
    private Command command2;
    private MBindingTable bindingTable;

    @Mock
    private MApplication application;

    @Mock
    private ECommandService commandService;

    @Mock
    private Logger log;

    @Mock
    private BindingManager manager;

    private SaveHelper helper;

    @BeforeEach
    public void setUp() throws Exception {
        this.helper = new SaveHelper(this.application, this.commandService, this.log, this.manager);
        this.commandManager = new CommandManager();
        this.bindingTable = new TestBindingTable();
    }

    @SuppressWarnings("restriction")
    @Test
    void testAddBinding() throws Exception {
        this.command1 = this.commandManager.getCommand("aa");
        final Binding binding = createBinding(this.command1, BINDING_CONTEXT_ID, KeySequence.getInstance("M1+A"));
        MKeyBinding keyBinding = this.helper.addBinding(binding);
        assertNull(keyBinding);

        final List<MCommand> commands = new ArrayList<MCommand>();
        commands.add(createCommand("aa"));
        commands.add(createCommand("bb"));
        when(this.application.getCommands()).thenReturn(commands);

        when(this.application.getBindingTables()).thenReturn(Arrays.asList(this.bindingTable));
        this.bindingTable.setBindingContext(TestBindingContext.createBindingContext(BINDING_CONTEXT_ID));
        final TestKeyBinding keyBinding1 = new TestKeyBinding();
        keyBinding1.setElementId("cc");
        keyBinding1.getTransientData().put(EBindingService.MODEL_TO_BINDING_KEY, binding);
        final TestCommand mcmd = new TestCommand();
        mcmd.setElementId("bb");
        keyBinding1.setCommand(mcmd);
        ((TestBindingTable) this.bindingTable).addBinding(keyBinding1);

        keyBinding = this.helper.addBinding(binding);
        assertEquals(keyBinding1, keyBinding);
        assertNull(keyBinding.getTags());

        this.command2 = this.commandManager.getCommand("bb");
        final Binding binding2 = createBinding(this.command2, BINDING_CONTEXT_ID, KeySequence.getInstance("M1+1"));

        // ---
        PrefUtil.setUICallback(new PrefUtil.ICallback() {
            @Override
            public IPreferenceStore getPreferenceStore() {
                return new PreferenceStore();
            }

            @Override
            public void savePreferences() {
                // do nothing
            }
        });

        keyBinding = this.helper.addBinding(binding2);
        assertEquals(KeySequence.getInstance("M1+1").toString(), keyBinding.getKeySequence());
        assertEquals(1, keyBinding.getTags().size());
        assertEquals("schemeId:default", keyBinding.getTags().get(0));

        // back to default
        final Set<String> defaultCtx = new HashSet<String>(1);
        defaultCtx.add(BINDING_CONTEXT_ID);
        this.helper.setDefault(defaultCtx);
    }

    private KeyBinding createBinding(final Command command, final String contextId, final KeySequence keySequence) {
        return new KeyBinding(keySequence, new ParameterizedCommand(command, null), "default", contextId, null, null,
                null, Binding.SYSTEM);
    }

    private MCommand createCommand(final String elementId) {
        final TestCommand out = new TestCommand();
        out.setElementId(elementId);
        return out;
    }

}
