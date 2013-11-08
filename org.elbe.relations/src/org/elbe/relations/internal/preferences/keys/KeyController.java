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
package org.elbe.relations.internal.preferences.keys;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.util.Util;
import org.elbe.relations.internal.preferences.keys.model.BindingElement;
import org.elbe.relations.internal.preferences.keys.model.BindingModel;
import org.elbe.relations.internal.preferences.keys.model.CommonModel;
import org.elbe.relations.internal.preferences.keys.model.ConflictModel;
import org.elbe.relations.internal.preferences.keys.model.ContextElement;
import org.elbe.relations.internal.preferences.keys.model.ContextModel;
import org.elbe.relations.internal.preferences.keys.model.ModelElement;
import org.elbe.relations.internal.preferences.keys.model.SaveHelper;
import org.elbe.relations.internal.preferences.keys.model.SaveHelper.BindingRestoreHelper;
import org.elbe.relations.internal.preferences.keys.model.SchemeElement;
import org.elbe.relations.internal.preferences.keys.model.SchemeModel;

/**
 * Controller for the keys preferences page.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
@Creatable
public class KeyController {
	private static final String DELIMITER = ","; //$NON-NLS-1$
	private static final String ESCAPED_QUOTE = "\""; //$NON-NLS-1$
	private static final String REPLACEMENT = "\"\""; //$NON-NLS-1$

	/**
	 * The resource bundle from which translations can be retrieved.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(RelationsKeysPreferencePage.class.getName());

	private ListenerList eventManager = null;
	private boolean notifying = true;
	private ContextModel contextModel;
	private SchemeModel schemeModel;
	private BindingModel bindingModel;
	private ConflictModel conflictModel;
	private Map<ParameterizedCommand, Binding> sessionChanges;
	private BindingManager bindingManager;

	@Inject
	private MApplication application;

	@Inject
	private EBindingService bindingService;

	@Inject
	private CommandManager commandManager;

	@Inject
	private BindingManager bindingManagerApp;

	@Inject
	private BindingTableManager bindingTable;

	@Inject
	private SaveHelper saveHelper;

	@Inject
	private Logger log;

	/**
	 * Initializes the controller instance.
	 */
	public void init() {
		getEventManager().clear();
		sessionChanges = new HashMap<ParameterizedCommand, Binding>();
		bindingManager = loadModelBackend();

		contextModel = new ContextModel(this);
		contextModel.init(application.getBindingContexts());
		schemeModel = new SchemeModel(this);
		schemeModel.init(bindingManager, log);
		bindingModel = new BindingModel(this);
		bindingModel.init(bindingManager, commandManager,
				application.getBindingTables(), contextModel);
		conflictModel = new ConflictModel(this);
		conflictModel.init(bindingModel, bindingManager);

		addSetContextListener();
		addSetBindingListener();
		addSetConflictListener();
		addSetKeySequenceListener();
		addSetSchemeListener();
		addSetModelObjectListener();
	}

	/**
	 * @return boolean <code>true</code> if the page has been initialized
	 */
	public boolean initialized() {
		return sessionChanges != null;
	}

	private BindingManager loadModelBackend() {
		final BindingManager outManager = new BindingManager(
				new ContextManager(), new CommandManager());
		final Scheme[] lDefinedSchemes = bindingManagerApp.getDefinedSchemes();
		try {
			Scheme lModelActiveScheme = null;
			for (int i = 0; i < lDefinedSchemes.length; i++) {
				final Scheme lScheme = lDefinedSchemes[i];
				final Scheme lCopy = outManager.getScheme(lScheme.getId());
				lCopy.define(lScheme.getName(), lScheme.getDescription(),
						lScheme.getParentId());
				if (lDefinedSchemes[i].getId().equals(
						bindingManagerApp.getActiveScheme().getId())) {
					lModelActiveScheme = lCopy;
				}
			}
			outManager.setActiveScheme(lModelActiveScheme);
		}
		catch (final NotDefinedException exc) {
			log.error(exc, exc.getMessage());
		}
		outManager.setLocale(bindingManagerApp.getLocale());
		outManager.setPlatform(bindingManagerApp.getPlatform());
		// fill bindings from bindingService to bindingManager instance
		outManager.setBindings(getBindingsFromSrc());
		return outManager;
	}

	/**
	 * Fill bindings from bindingService to bindingManager instance.
	 * 
	 * @return Binding[]
	 */
	private Binding[] getBindingsFromSrc() {
		final Collection<Binding> lBindingsSrc = bindingService
				.getActiveBindings();
		final Binding[] outBindings = new Binding[lBindingsSrc.size()];
		int i = 0;
		for (final Binding lBinding : lBindingsSrc) {
			outBindings[i++] = lBinding;
		}
		return outBindings;
	}

	private void addSetSchemeListener() {
		addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() == schemeModel
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(inEvent
								.getProperty())) {
					changeScheme((SchemeElement) inEvent.getOldValue(),
							(SchemeElement) inEvent.getNewValue());
				}
			}
		});
	}

	protected void changeScheme(final SchemeElement inOldScheme,
			final SchemeElement inNewScheme) {
		if (inNewScheme == null
				|| inNewScheme.getModelObject() == bindingManager
						.getActiveScheme()) {
			return;
		}
		try {
			bindingManager.setActiveScheme((Scheme) inNewScheme
					.getModelObject());
			bindingModel.refresh(getContextModel());
			bindingModel.setSelectedElement(null);
		}
		catch (final NotDefinedException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	private ListenerList getEventManager() {
		if (eventManager == null) {
			eventManager = new ListenerList(ListenerList.IDENTITY);
		}
		return eventManager;
	}

	public void addPropertyChangeListener(
			final IPropertyChangeListener inListener) {
		getEventManager().add(inListener);
	}

	public void removePropertyChangeListener(
			final IPropertyChangeListener inListener) {
		getEventManager().remove(inListener);
	}

	/**
	 * @return {@link SchemeModel}
	 */
	public SchemeModel getSchemeModel() {
		return schemeModel;
	}

	/**
	 * @return {@link BindingModel}
	 */
	public BindingModel getBindingModel() {
		return bindingModel;
	}

	/**
	 * Updates listeners about property changes in the model.
	 * 
	 * @param inSource
	 *            Object
	 * @param inPropId
	 *            String
	 * @param inOld
	 *            Object
	 * @param inNew
	 *            Object
	 */
	public void firePropertyChange(final Object inSource,
			final String inPropId, final Object inOld, final Object inNew) {
		if (!isNotifying()) {
			return;
		}
		if (Util.equals(inOld, inNew)) {
			return;
		}

		final Object[] lListeners = getEventManager().getListeners();
		final PropertyChangeEvent lEvent = new PropertyChangeEvent(inSource,
				inPropId, inOld, inNew);
		for (int i = 0; i < lListeners.length; i++) {
			((IPropertyChangeListener) lListeners[i]).propertyChange(lEvent);
		}
	}

	public boolean isNotifying() {
		return notifying;
	}

	public void setNotifying(final boolean inNotifying) {
		notifying = inNotifying;
	}

	/**
	 * @return {@link ContextModel}
	 */
	public ContextModel getContextModel() {
		return contextModel;
	}

	/**
	 * Filters contexts for the When Combo.
	 * 
	 * @param inActionSets
	 *            <code>true</code> to filter action set contexts
	 * @param inInternal
	 *            <code>false</code> to filter internal contexts
	 */
	public void filterContexts(final boolean inActionSets,
			final boolean inInternal) {
		contextModel.filterContexts(inActionSets, inInternal);
	}

	/**
	 * @return {@link ConflictModel}
	 */
	public ConflictModel getConflictModel() {
		return conflictModel;
	}

	private void addSetContextListener() {
		addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() == contextModel
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(inEvent
								.getProperty())) {
					updateBindingContext((ContextElement) inEvent.getNewValue());
				}
			}
		});
	}

	private void addSetBindingListener() {
		addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() == bindingModel
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(inEvent
								.getProperty())) {
					final BindingElement lBinding = (BindingElement) inEvent
							.getNewValue();
					if (lBinding == null) {
						conflictModel.setSelectedElement(null);
						return;
					}
					conflictModel.setSelectedElement(lBinding);
					final ContextElement lContext = lBinding.getContext();
					if (lContext != null) {
						contextModel.setSelectedElement(lContext);
					}
				}
			}
		});
	}

	private void addSetConflictListener() {
		addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() == conflictModel
						&& CommonModel.PROP_SELECTED_ELEMENT.equals(inEvent
								.getProperty())) {
					if (inEvent.getNewValue() != null) {
						bindingModel.setSelectedElement((ModelElement) inEvent
								.getNewValue());
					}
				}
			}
		});
	}

	private void addSetKeySequenceListener() {
		addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (BindingElement.PROP_TRIGGER.equals(inEvent.getProperty())) {
					updateTrigger((BindingElement) inEvent.getSource(),
							(KeySequence) inEvent.getOldValue(),
							(KeySequence) inEvent.getNewValue());
				}
			}
		});
	}

	private void addSetModelObjectListener() {
		addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() instanceof BindingElement
						&& ModelElement.PROP_MODEL_OBJECT.equals(inEvent
								.getProperty())) {
					if (inEvent.getNewValue() != null) {
						final BindingElement lElement = (BindingElement) inEvent
								.getSource();
						final Object lOldValue = inEvent.getOldValue();
						final Object lNewValue = inEvent.getNewValue();
						if (lOldValue instanceof Binding
								&& lNewValue instanceof Binding) {
							conflictModel.updateConflictsFor(lElement,
									((Binding) lOldValue).getTriggerSequence(),
									((Binding) lNewValue).getTriggerSequence(),
									false);
						} else {
							conflictModel.updateConflictsFor(lElement, false);
						}

						final ContextElement lContext = lElement.getContext();
						if (lContext != null) {
							contextModel.setSelectedElement(lContext);
						}
					}
				}
			}
		});
	}

	private void updateBindingContext(final ContextElement inContext) {
		if (inContext == null) {
			return;
		}
		final BindingElement lActiveBinding = (BindingElement) bindingModel
				.getSelectedElement();
		if (lActiveBinding == null) {
			return;
		}
		final String lActiveSchemeId = schemeModel.getSelectedElement().getId();
		final Object lObj = lActiveBinding.getModelObject();
		if (lObj instanceof KeyBinding) {
			final KeyBinding lKeyBinding = (KeyBinding) lObj;
			if (!lKeyBinding.getContextId().equals(inContext.getId())) {
				final KeyBinding lBinding = new KeyBinding(
						lKeyBinding.getKeySequence(),
						lKeyBinding.getParameterizedCommand(), lActiveSchemeId,
						inContext.getId(), null, null, null, Binding.USER);
				if (lKeyBinding.getType() == Binding.USER) {
					bindingManager.removeBinding(lKeyBinding);
				} else {
					final Map<String, String> lAttributes = new HashMap<String, String>();
					lAttributes.put(EBindingService.TYPE_ATTR_TAG, "user");
					final Binding lNewBinding = bindingService.createBinding(
							lKeyBinding.getKeySequence(), null,
							lKeyBinding.getContextId(), lAttributes);
					bindingManager.addBinding(lNewBinding);
				}
				bindingModel.getBindingToElement().remove(
						lActiveBinding.getModelObject());

				bindingManager.addBinding(lBinding);
				toSessionChanges(lBinding);
				lActiveBinding.fill(lBinding, contextModel);
				bindingModel.getBindingToElement()
						.put(lBinding, lActiveBinding);
			}
		}
	}

	private void toSessionChanges(final Binding inBinding) {
		sessionChanges.put(inBinding.getParameterizedCommand(), inBinding);
	}

	/**
	 * @param inActiveBinding
	 * @param inOldSequence
	 * @param inNewSequence
	 */
	public void updateTrigger(final BindingElement inActiveBinding,
			final KeySequence inOldSequence, final KeySequence inNewSequence) {
		if (inActiveBinding == null) {
			return;
		}
		final Object lObj = inActiveBinding.getModelObject();
		// binding init KeyBinding
		if (lObj instanceof KeyBinding) {
			final KeyBinding lOldBinding = (KeyBinding) lObj;
			final ParameterizedCommand lCommand = lOldBinding
					.getParameterizedCommand();
			if (!lOldBinding.getKeySequence().equals(inNewSequence)) {
				if (inNewSequence == null || inNewSequence.isEmpty()) {
					// case empty key sequence: user cleared the input
					bindingModel.getBindingToElement().remove(lOldBinding);
					if (lOldBinding.getType() == Binding.USER) {
						bindingManager.removeBinding(lOldBinding);
					} else {
						bindingManager.addBinding(new KeyBinding(lOldBinding
								.getKeySequence(), null, lOldBinding
								.getSchemeId(), lOldBinding.getContextId(),
								null, null, null, Binding.USER));
					}
					inActiveBinding.fill(lCommand);
				} else {
					final String lActiveSchemeId = schemeModel
							.getSelectedElement().getId();
					final ModelElement lActiveContext = contextModel
							.getSelectedElement();
					final String lActiveContextId = lActiveContext == null ? IContextService.CONTEXT_ID_WINDOW
							: lActiveContext.getId();

					final KeyBinding lNewBinding = new KeyBinding(
							inNewSequence, lCommand, lActiveSchemeId,
							lActiveContextId, null, null, null, Binding.USER);
					final Map<Binding, BindingElement> lBindingToElement = bindingModel
							.getBindingToElement();
					lBindingToElement.remove(lOldBinding);
					if (lOldBinding.getType() == Binding.USER) {
						bindingManager.removeBinding(lOldBinding);
					} else {
						bindingManager.addBinding(new KeyBinding(lOldBinding
								.getKeySequence(), null, lOldBinding
								.getSchemeId(), lOldBinding.getContextId(),
								null, null, null, Binding.USER));
					}
					bindingManager.addBinding(lNewBinding);
					toSessionChanges(lNewBinding);

					inActiveBinding.fill(lNewBinding, contextModel);
					bindingModel.getBindingToElement().put(lNewBinding,
							inActiveBinding);

					// Remove binding for any system conflicts
					bindingModel.setSelectedElement(inActiveBinding);
				}
			}
		} else if (lObj instanceof ParameterizedCommand) {
			// binding init ParameterizedCommand
			final ParameterizedCommand lCommand = (ParameterizedCommand) lObj;
			if (inNewSequence != null && !inNewSequence.isEmpty()) {
				final String lActiveSchemeId = schemeModel.getSelectedElement()
						.getId();
				final ModelElement lSelectedElement = contextModel
						.getSelectedElement();
				final String lActiveContextId = lSelectedElement == null ? IContextService.CONTEXT_ID_WINDOW
						: lSelectedElement.getId();
				final KeyBinding lBinding = new KeyBinding(inNewSequence,
						lCommand, lActiveSchemeId, lActiveContextId, null,
						null, null, Binding.USER);
				bindingManager.addBinding(lBinding);
				toSessionChanges(lBinding);
				inActiveBinding.fill(lBinding, contextModel);
				bindingModel.getBindingToElement().put(lBinding,
						inActiveBinding);
			}
		}
	}

	/**
	 * Exports the key bindings to a CSV file.
	 * 
	 * @param inShell
	 *            Shell
	 */
	public void exportCSV(final Shell inShell) {
		final FileDialog lFileDialog = new FileDialog(inShell, SWT.SAVE
				| SWT.SHEET);
		lFileDialog.setFilterExtensions(new String[] { "*.csv" }); //$NON-NLS-1$
		lFileDialog.setFilterNames(new String[] { Util.translateString(
				RESOURCE_BUNDLE, "csvFilterName") }); //$NON-NLS-1$
		lFileDialog.setOverwrite(true);
		final String lFilePath = lFileDialog.open();
		if (lFilePath == null) {
			return;
		}

		final SafeRunnable lRunnable = new SafeRunnable() {
			@Override
			public final void run() throws IOException {
				Writer lFileWriter = null;
				try {
					lFileWriter = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(lFilePath), "UTF-8")); //$NON-NLS-1$
					final Object[] lBindingElements = bindingModel
							.getBindings().toArray();
					for (int i = 0; i < lBindingElements.length; i++) {
						final BindingElement lElement = (BindingElement) lBindingElements[i];
						if (lElement.getTrigger() == null
								|| lElement.getTrigger().isEmpty()) {
							continue;
						}
						final StringBuilder lBuffer = new StringBuilder();
						lBuffer.append(ESCAPED_QUOTE)
								.append(Util.replaceAll(lElement.getCategory(),
										ESCAPED_QUOTE, REPLACEMENT))
								.append(ESCAPED_QUOTE).append(DELIMITER);
						lBuffer.append(ESCAPED_QUOTE)
								.append(lElement.getName())
								.append(ESCAPED_QUOTE).append(DELIMITER);
						lBuffer.append(ESCAPED_QUOTE)
								.append(lElement.getTrigger().format())
								.append(ESCAPED_QUOTE).append(DELIMITER);
						lBuffer.append(ESCAPED_QUOTE)
								.append(lElement.getContext() == null ? ""
										: lElement.getContext().getName())
								.append(ESCAPED_QUOTE);
						lBuffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
						lFileWriter.write(lBuffer.toString());
					}
				} finally {
					if (lFileWriter != null) {
						try {
							lFileWriter.close();
						}
						catch (final IOException e) {
							// At least I tried.
						}
					}

				}
			}
		};
		SafeRunner.run(lRunnable);
	}

	/**
	 * Replaces all the current bindings with the bindings in the local copy of
	 * the binding manager.
	 */
	public void saveBindings() {
		final Collection<Binding> toAdd = new ArrayList<Binding>();
		for (final Entry<ParameterizedCommand, Binding> lEntry : sessionChanges
				.entrySet()) {
			final Collection<Binding> lBindings = bindingService
					.getBindingsFor(lEntry.getKey());
			for (final Binding lToReplace : lBindings) {
				bindingService.deactivateBinding(lToReplace);
			}
			toAdd.add(lEntry.getValue());
		}
		for (final Binding lBinding : toAdd) {
			saveHelper.addBinding(lBinding);
		}
	}

	/**
	 * Sets the bindings to default.
	 */
	public void setDefaultBindings() {
		// Fix the scheme in the local changes.
		// final String defaultSchemeId = bindingService.getDefaultSchemeId();
		// final Scheme defaultScheme =
		// bindingService.getScheme(defaultSchemeId);
		// try {
		// bindingService.setActiveScheme(defaultScheme);
		// }
		// catch (final NotDefinedException e) {
		// // At least we tried....
		// }

		final Collection<Binding> lToRemove = new ArrayList<Binding>();
		final Set<String> lContexts = new HashSet<String>();
		final Binding[] lBindings = bindingManager.getBindings();
		for (final Binding lBinding : lBindings) {
			if (lBinding.getType() == Binding.USER) {
				lContexts.add(lBinding.getContextId());
				lToRemove.add(lBinding);
			}
		}
		final Collection<BindingRestoreHelper> lActivationHelpers = saveHelper
				.setDefault(lContexts);
		for (final Binding lBinding : lToRemove) {
			bindingService.deactivateBinding(lBinding);
		}
		final String lSchemeId = bindingManager.getActiveScheme().getId();
		for (final BindingRestoreHelper lFactory : lActivationHelpers) {
			lFactory.activateBindings(bindingService, lSchemeId);
		}

		// first we clear the model
		bindingManager.setBindings(new Binding[] {});
		bindingModel.refresh(contextModel);
		// then we reinitialize the model
		bindingManager.setBindings(getBindingsFromSrc());
		bindingModel.refresh(contextModel);
	}

}
