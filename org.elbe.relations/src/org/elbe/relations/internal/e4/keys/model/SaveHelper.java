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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.ui.internal.keys.BindingPersistence;

/**
 * Helper class for persisting the changed key bindings.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
@Creatable
public class SaveHelper {
	private static final String ORIG_ATTR_TAG = "orig:";
	private static final String TMPL_ORIG_SEQUENCE = ORIG_ATTR_TAG + "%s";

	@Inject
	private MApplication application;

	@Inject
	private ECommandService commandService;

	@Inject
	private Logger log;

	@Inject
	private BindingManager manager;

	private final Map<String, MBindingContext> bindingContexts = new HashMap<String, MBindingContext>();

	/**
	 * Resets all user changes to the original values.
	 *
	 * @param contexts
	 *            Set&lt;String> the context ids to look for user changes to
	 *            reset
	 * @return Collection&lt;BindingRestoreHelper>
	 */
	public Collection<BindingRestoreHelper> setDefault(Set<String> contexts) {
		final Collection<BindingRestoreHelper> outFactories = new ArrayList<SaveHelper.BindingRestoreHelper>();
		for (final String context : contexts) {
			outFactories
			        .add(setDefault(getMTable(context), application, context));
		}
		return outFactories;
	}

	private BindingRestoreHelper setDefault(final MBindingTable table,
	        final MApplication inApplication, final String contextId) {
		final Collection<MKeyBinding> toReset = new ArrayList<MKeyBinding>();
		for (final MKeyBinding binding : table.getBindings()) {
			final String origSequence = retrieveOrigSequence(binding);
			if (origSequence != null) {
				toReset.add(binding);
			}
		}

		final BindingRestoreHelper outBindingRestoreFactory = new BindingRestoreHelper(
		        contextId, commandService, log);
		for (final MKeyBinding binding : toReset) {
			final MKeyBinding lRestored = reset(table, binding,
			        retrieveOrigSequence(binding));
			outBindingRestoreFactory.registerRestoredBinding(lRestored);
		}
		return outBindingRestoreFactory;
	}

	private String retrieveOrigSequence(final MKeyBinding binding) {
		final List<String> tags = binding.getTags();
		for (final String tag : tags) {
			if (tag.startsWith(ORIG_ATTR_TAG)) {
				return tag.substring(ORIG_ATTR_TAG.length());
			}
		}
		return null;
	}

	private MKeyBinding reset(final MBindingTable table,
	        final MKeyBinding oldBinding, final String origSequence) {
		final MKeyBinding outNewBinding = CommandsFactoryImpl.eINSTANCE
		        .createKeyBinding();
		outNewBinding.setCommand(oldBinding.getCommand());
		outNewBinding.setKeySequence(origSequence);
		outNewBinding.setContributorURI(oldBinding.getContributorURI());

		final List<MParameter> newParameters = outNewBinding.getParameters();
		for (final MParameter parameter : oldBinding.getParameters()) {
			newParameters.add(parameter);
		}

		final List<String> newTags = outNewBinding.getTags();
		for (final String tag : oldBinding.getTags()) {
			if (!tag.startsWith(EBindingService.TYPE_ATTR_TAG + ":")
			        && !tag.startsWith(ORIG_ATTR_TAG)) {
				newTags.add(tag);
			}
		}
		final Object obj = oldBinding.getTransientData()
		        .get(EBindingService.MODEL_TO_BINDING_KEY);
		if (obj != null) {
			outNewBinding.getTransientData()
			        .put(EBindingService.MODEL_TO_BINDING_KEY, obj);
		}
		table.getBindings().remove(oldBinding);
		table.getBindings().add(outNewBinding);
		return outNewBinding;
	}

	/**
	 * Save the new binding.
	 *
	 * @param newBinding
	 *            {@link Binding}
	 * @return {@link MKeyBinding} the created or updated key binding instance
	 */
	public MKeyBinding addBinding(Binding newBinding) {
		final MBindingTable table = getMTable(newBinding.getContextId());
		return createORupdateMKeyBinding(application, table, newBinding);
	}

	private MKeyBinding createORupdateMKeyBinding(MApplication inApplication,
	        MBindingTable table, Binding newBinding) {
		boolean lAddToTable = false;

		final ParameterizedCommand parmetrizedCmd = newBinding
		        .getParameterizedCommand();

		final String lCmdId = parmetrizedCmd.getId();
		MCommand lCmd = null;
		for (final MCommand appCommand : inApplication.getCommands()) {
			if (lCmdId.equals(appCommand.getElementId())) {
				lCmd = appCommand;
				break;
			}
		}
		if (lCmd == null) {
			return null;
		}

		MKeyBinding outNewBinding = null;
		MKeyBinding toReplace = null;
		for (final MKeyBinding existingMBinding : table.getBindings()) {
			final Binding lBinding = (Binding) existingMBinding
			        .getTransientData()
			        .get(EBindingService.MODEL_TO_BINDING_KEY);
			if (newBinding.equals(lBinding)) {
				outNewBinding = existingMBinding;
				break;
			}
			if (isSameBinding(existingMBinding, lCmd, newBinding)) {
				outNewBinding = existingMBinding;
				break;
			}
			if (lCmdId.equals(existingMBinding.getCommand().getElementId())) {
				toReplace = existingMBinding;
			}
		}

		if (outNewBinding == null) {
			lAddToTable = true;
			outNewBinding = CommandsFactoryImpl.eINSTANCE.createKeyBinding();
			outNewBinding.setCommand(lCmd);
			outNewBinding
			        .setKeySequence(newBinding.getTriggerSequence().toString());
			outNewBinding.setContributorURI(toReplace.getContributorURI());

			for (final Object obj : parmetrizedCmd.getParameterMap()
			        .entrySet()) {
				@SuppressWarnings({ "unchecked" })
				final Map.Entry<String, String> entry = (Map.Entry<String, String>) obj;

				final String paramID = entry.getKey();
				if (paramID == null) {
					continue;
				}
				final List<MParameter> bindingParams = outNewBinding
				        .getParameters();
				MParameter parameter = null;
				for (final MParameter lParam : bindingParams) {
					if (paramID.equals(lParam.getElementId())) {
						parameter = lParam;
						break;
					}
				}
				if (parameter == null) {
					parameter = CommandsFactoryImpl.eINSTANCE.createParameter();
					parameter.setElementId(entry.getKey());
					outNewBinding.getParameters().add(parameter);
				}
				parameter.setName(entry.getKey());
				parameter.setValue(entry.getValue());
			}

			final List<String> tags = outNewBinding.getTags();
			// just add the 'schemeId' tag if the binding is for anything other
			// than the default scheme
			if (newBinding.getSchemeId() != null && !newBinding.getSchemeId()
			        .equals(BindingPersistence.getDefaultSchemeId())) {
				tags.add(EBindingService.SCHEME_ID_ATTR_TAG + ":" //$NON-NLS-1$
				        + newBinding.getSchemeId());
			}
			if (newBinding.getLocale() != null) {
				tags.add(EBindingService.LOCALE_ATTR_TAG + ":" //$NON-NLS-1$
				        + newBinding.getLocale());
			}
			if (newBinding.getPlatform() != null) {
				tags.add(EBindingService.PLATFORM_ATTR_TAG + ":" //$NON-NLS-1$
				        + newBinding.getPlatform());
			}
			// just add the 'type' tag if it's a user binding
			if (newBinding.getType() == Binding.USER) {
				tags.add(EBindingService.TYPE_ATTR_TAG + ":user"); //$NON-NLS-1$
				tags.add(String.format(TMPL_ORIG_SEQUENCE,
				        getOrigSequence(toReplace)));
			}
		}

		outNewBinding.getTransientData()
		        .put(EBindingService.MODEL_TO_BINDING_KEY, newBinding);
		if (lAddToTable) {
			table.getBindings().remove(toReplace);
			table.getBindings().add(outNewBinding);
		}
		return outNewBinding;
	}

	private String getOrigSequence(MKeyBinding toReplace) {
		final String oldSequence = toReplace.getKeySequence();
		final List<String> oldTags = toReplace.getTags();
		for (final String tag : oldTags) {
			if (tag.startsWith(ORIG_ATTR_TAG)) {
				return tag.substring(ORIG_ATTR_TAG.length());
			}
		}
		return oldSequence;
	}

	private boolean isSameBinding(MKeyBinding existingBinding, MCommand cmd,
	        Binding newBinding) {
		// see org.eclipse.jface.bindings.Binding#equals(final Object object)
		if (!cmd.equals(existingBinding.getCommand())) {
			return false;
		}
		final String lExistingKeySequence = existingBinding.getKeySequence();
		if (lExistingKeySequence == null) {
			return false;
		}
		try {
			final KeySequence lExistingSequence = KeySequence
			        .getInstance(lExistingKeySequence);
			if (!lExistingSequence.equals(newBinding.getTriggerSequence())) {
				return false;
			}
		}
		catch (final ParseException exc) {
			return false;
		}

		// tags to look for:
		final List<String> lModelTags = existingBinding.getTags();

		final String lSchemeId = newBinding.getSchemeId();
		if (lSchemeId != null
		        && !lSchemeId.equals(BindingPersistence.getDefaultSchemeId())) {
			if (!lModelTags.contains(
			        EBindingService.SCHEME_ID_ATTR_TAG + ":" + lSchemeId)) {
				return false;
			}
		}
		final String lLocale = newBinding.getLocale();
		if (lLocale != null) {
			if (!lModelTags.contains(
			        EBindingService.LOCALE_ATTR_TAG + ":" + lLocale)) {
				return false;
			}
		}
		final String lPlatform = newBinding.getPlatform();
		if (lPlatform != null) {
			if (!lModelTags.contains(
			        EBindingService.PLATFORM_ATTR_TAG + ":" + lPlatform)) {
				return false;
			}
		}
		if (newBinding.getType() == Binding.USER) {
			if (!lModelTags.contains(EBindingService.TYPE_ATTR_TAG + ":user")) {
				return false;
			}
		}
		return true;
	}

	private MBindingTable getMTable(String contextId) {
		for (final MBindingTable outBindingTable : application
		        .getBindingTables()) {
			if (outBindingTable.getBindingContext().getElementId()
			        .equals(contextId)) {
				return outBindingTable;
			}
		}
		// create a new table if we couldn't find one
		final MBindingTable outTable = CommandsFactoryImpl.eINSTANCE
		        .createBindingTable();
		outTable.setBindingContext(getBindingContext(contextId));
		outTable.setElementId(contextId);
		application.getBindingTables().add(outTable);
		return outTable;
	}

	private MBindingContext getBindingContext(String contextId) {
		// cache
		MBindingContext lResult = bindingContexts.get(contextId);
		if (lResult == null) {
			// search
			lResult = searchContexts(contextId, application.getRootContext());
			if (lResult == null) {
				// create
				lResult = MCommandsFactory.INSTANCE.createBindingContext();
				lResult.setElementId(contextId);
				lResult.setName("Auto::" + contextId); //$NON-NLS-1$
				application.getRootContext().add(lResult);
			}
			if (lResult != null) {
				bindingContexts.put(contextId, lResult);
			}
		}
		return lResult;
	}

	private MBindingContext searchContexts(final String contextId,
	        final List<MBindingContext> rootContext) {
		for (final MBindingContext context : rootContext) {
			if (context.getElementId().equals(contextId)) {
				return context;
			}
			final MBindingContext childContext = searchContexts(contextId,
			        context.getChildren());
			if (childContext != null) {
				return childContext;
			}
		}
		return null;
	}

	/**
	 * Retrieves the scheme for the specified ID.
	 *
	 * @param inSchemeId
	 *            String
	 * @return {@link Scheme}
	 */
	public Scheme getScheme(final String inSchemeId) {
		return manager.getScheme(inSchemeId);
	}

	// --- inner classes ---

	public static class BindingRestoreHelper {
		private final Logger log;
		private final ECommandService commandService;
		private final String contextId;
		private final Collection<MKeyBinding> bindings;

		BindingRestoreHelper(final String inContextId,
		        final ECommandService inCommandService, final Logger inLog) {
			bindings = new ArrayList<MKeyBinding>();
			contextId = inContextId;
			commandService = inCommandService;
			log = inLog;
		}

		void registerRestoredBinding(final MKeyBinding inBinding) {
			bindings.add(inBinding);
		}

		private KeyBinding createKeyBinding(final MKeyBinding inBinding,
		        final String inSchemeId) throws ParseException {
			return new KeyBinding(
			        KeySequence.getInstance(inBinding.getKeySequence()),
			        commandService.createCommand(
			                inBinding.getCommand().getElementId(), null),
			        inSchemeId, contextId, null, null, null, Binding.SYSTEM);
		}

		public void activateBindings(final EBindingService inBindingService,
		        final String inSchemeId) {
			for (final MKeyBinding lBinding : bindings) {
				try {
					inBindingService.activateBinding(
					        createKeyBinding(lBinding, inSchemeId));
				}
				catch (final ParseException exc) {
					log.error(exc, "Unable to restore binding \""
					        + lBinding.getKeySequence() + "\" correctly!");
				}
			}
		}
	}

}
