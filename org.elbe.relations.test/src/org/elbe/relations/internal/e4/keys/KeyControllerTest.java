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
package org.elbe.relations.internal.e4.keys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.SchemeEvent;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.elbe.relations.internal.e4.keys.model.BindingElement;
import org.elbe.relations.internal.e4.keys.model.BindingModel;
import org.elbe.relations.internal.e4.keys.model.SaveHelper;
import org.elbe.relations.internal.e4.keys.model.SchemeElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author lbenno
 */
@ExtendWith(MockitoExtension.class)
public class KeyControllerTest {
    private static final String SCHEME_ID = "schemeID";

    private CommandManager commandManager;
    private ContextManager contextManager;
    private BindingManager bindingManager;

    @Mock
    private MApplication application;

    @Mock
    private EBindingService bindingService;

    @Mock
    private SaveHelper saveHelper;

    @Mock
    private Logger log;

    @InjectMocks
    private KeyController keyController;

    @BeforeEach
    public void setUp() throws Exception {
        this.contextManager = new ContextManager();
        this.commandManager = new CommandManager();
        this.bindingManager = new BindingManager(this.contextManager, this.commandManager);
        MockitoAnnotations.initMocks(this);

        final Scheme scheme = this.bindingManager.getScheme(SCHEME_ID);
        scheme.define("scheme:name", "scheme:description", null);
        this.bindingManager.setActiveScheme(scheme);
        this.bindingManager.schemeChanged(new SchemeEvent(scheme, true, false, false, false));
        inject(this.keyController, "commandManager", this.commandManager);
        inject(this.keyController, "bindingManagerApp", this.bindingManager);
    }

    @Test
    public void testInit() {
        this.keyController.init();
        assertTrue(this.keyController.initialized());
    }

    /**
     * Test method for
     * {@link org.elbe.relations.internal.e4.keys.KeyController#firePropertyChange(java.lang.Object, java.lang.String, java.lang.Object, java.lang.Object)}
     * .
     */
    @Test
    public void testFirePropertyChange() {
        final IPropertyChangeListener listener = new PropChangeListener();
        this.keyController.addPropertyChangeListener(listener);
        final BindingElement bindingEl = new BindingElement(this.keyController);
        this.keyController.firePropertyChange(bindingEl, BindingElement.PROP_TRIGGER, "old", "new");
        final PropertyChangeEvent event = ((PropChangeListener) listener).getEvent();
        assertEquals(bindingEl, event.getSource());
        assertEquals("old", event.getOldValue());
        assertEquals("new", event.getNewValue());
    }

    @Test
    public void testUpdateTrigger() throws ParseException {
        final BindingElement bindingEl = new BindingElement(this.keyController);
        final KeySequence oldKeys = KeySequence.getInstance(KeyStroke.getInstance("M1+A"));
        final KeySequence newKeys = KeySequence.getInstance(KeyStroke.getInstance("M1+B"));
        final Command command = this.commandManager.getCommand("aa");
        final ParameterizedCommand parametrized = new ParameterizedCommand(command, null);
        final KeyBinding model = new KeyBinding(oldKeys, parametrized, SCHEME_ID, "default", null, null, null, 0);
        bindingEl.setModelObject(model);
        this.keyController.init();
        this.keyController.setNotifying(false);
        final BindingModel bindingModel = this.keyController.getBindingModel();
        final SchemeElement scheme = new SchemeElement(this.keyController);
        scheme.setId("myScheme");
        this.keyController.getSchemeModel().setSelectedElement(scheme);
        assertTrue(bindingModel.getBindingToElement().isEmpty());
        this.keyController.updateTrigger(bindingEl, oldKeys, newKeys);
        final Map<Binding, BindingElement> map = bindingModel.getBindingToElement();
        assertEquals(1, map.size());
        final Binding binding = map.keySet().iterator().next();
        final BindingElement updatedEl = map.get(binding);
        assertEquals(bindingEl, updatedEl);
        assertEquals(newKeys, updatedEl.getTrigger());
    }

    /**
     * Test method for
     * {@link org.elbe.relations.internal.e4.keys.KeyController#exportCSV(org.eclipse.swt.widgets.Shell)}
     * .
     */
    @Test
    public void testExportCSV() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.elbe.relations.internal.e4.keys.KeyController#saveBindings()}.
     */
    @Test
    public void testSaveBindings() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.elbe.relations.internal.e4.keys.KeyController#setDefaultBindings()}
     * .
     */
    @Test
    public void testSetDefaultBindings() {
        fail("Not yet implemented");
    }

    private void inject(final KeyController controller, final String fieldName, final Object value)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final Class<? extends KeyController> clazz = controller.getClass();
        final Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    // ---

    private static class PropChangeListener implements IPropertyChangeListener {

        private PropertyChangeEvent event;

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            this.event = event;
        }

        protected PropertyChangeEvent getEvent() {
            return this.event;
        }

    }

}
