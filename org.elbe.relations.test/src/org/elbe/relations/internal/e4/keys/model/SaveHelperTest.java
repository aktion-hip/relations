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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author lbenno
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SaveHelperTest {
	private static final String BINDING_CONTEXT_ID = "ctxID";

	private CommandManager commandManager;
	private Command command1;
	private Command command2;
	private MBindingTable bindingTable;

	@Mock
	private MApplication application;

	@InjectMocks
	private SaveHelper helper;

	@Before
	public void setUp() throws Exception {
		commandManager = new CommandManager();
		MockitoAnnotations.initMocks(this);

		bindingTable = new TestBindingTable();
	}

	@Test
	public void testAddBinding() throws Exception {
		command1 = commandManager.getCommand("aa");
		final Binding binding = createBinding(command1, BINDING_CONTEXT_ID, KeySequence.getInstance("M1+A"));
		MKeyBinding keyBinding = helper.addBinding(binding);
		assertNull(keyBinding);

		final List<MCommand> commands = new ArrayList<MCommand>();
		commands.add(createCommand("aa"));
		commands.add(createCommand("bb"));
		when(application.getCommands()).thenReturn(commands);

		when(application.getBindingTables()).thenReturn(Arrays.asList(bindingTable));
		bindingTable.setBindingContext(TestBindingContext.createBindingContext(BINDING_CONTEXT_ID));
		final TestKeyBinding keyBinding1 = new TestKeyBinding();
		keyBinding1.setElementId("cc");
		keyBinding1.getTransientData().put(EBindingService.MODEL_TO_BINDING_KEY, binding);
		final TestCommand mcmd = new TestCommand();
		mcmd.setElementId("bb");
		keyBinding1.setCommand(mcmd);
		((TestBindingTable) bindingTable).addBinding(keyBinding1);

		keyBinding = helper.addBinding(binding);
		assertEquals(keyBinding1, keyBinding);
		assertNull(keyBinding.getTags());

		command2 = commandManager.getCommand("bb");
		final Binding binding2 = createBinding(command2, BINDING_CONTEXT_ID, KeySequence.getInstance("M1+1"));

		// ---
		PrefUtil.setUICallback(new PrefUtil.ICallback() {
			@Override
			public IPreferenceStore getPreferenceStore() {
				return new ScopedPreferenceStore(DefaultScope.INSTANCE, "test");
			}

			@Override
			public void savePreferences() {
				// do nothing
			}
		});

		keyBinding = helper.addBinding(binding2);
		assertEquals(KeySequence.getInstance("M1+1").toString(), keyBinding.getKeySequence());
		assertEquals(1, keyBinding.getTags().size());
		assertEquals("schemeId:default", keyBinding.getTags().get(0));

		// back to default
		final Set<String> defaultCtx = new HashSet<String>(1);
		defaultCtx.add(BINDING_CONTEXT_ID);
		helper.setDefault(defaultCtx);
	}

	private KeyBinding createBinding(Command command, String contextId, KeySequence keySequence) {
		return new KeyBinding(keySequence, new ParameterizedCommand(command, null), "default", contextId, null, null,
				null, Binding.SYSTEM);
	}

	private MCommand createCommand(String elementId) {
		final TestCommand out = new TestCommand();
		out.setElementId(elementId);
		return out;
	}

}
