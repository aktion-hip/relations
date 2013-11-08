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
package org.elbe.relations.internal.preferences.keys.model;

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
import org.eclipse.e4.ui.bindings.internal.BindingTableManager;
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
 * @author Luthiger
 */
@SuppressWarnings("restriction")
@Creatable
public class SaveHelper {
	private static final String ORIG_ATTR_TAG = "orig:";
	private static final String TMPL_ORIG_SEQUENCE = ORIG_ATTR_TAG + "%s";

	@Inject
	private MApplication application;

	@Inject
	private BindingManager manager;

	@Inject
	private BindingTableManager tableManager;

	@Inject
	private ECommandService commandService;

	@Inject
	private Logger log;

	private final Map<String, MBindingContext> bindingContexts = new HashMap<String, MBindingContext>();

	/**
	 * Resets all user changes to the original values.
	 * 
	 * @param inContexts
	 *            Set&lt;String> the context ids to look for user changes to
	 *            reset
	 * @return Collection<BindingRestoreFactory>
	 */
	public Collection<BindingRestoreHelper> setDefault(
			final Set<String> inContexts) {
		final Collection<BindingRestoreHelper> outFactories = new ArrayList<SaveHelper.BindingRestoreHelper>();
		for (final String lContext : inContexts) {
			outFactories.add(setDefault(getMTable(lContext), application,
					lContext));
		}
		return outFactories;
	}

	private BindingRestoreHelper setDefault(final MBindingTable inTable,
			final MApplication inApplication, final String inContextId) {
		final Collection<MKeyBinding> lToReset = new ArrayList<MKeyBinding>();
		for (final MKeyBinding lBinding : inTable.getBindings()) {
			final String lOrigSequence = retrieveOrigSequence(lBinding);
			if (lOrigSequence != null) {
				lToReset.add(lBinding);
			}
		}

		final BindingRestoreHelper outBindingRestoreFactory = new BindingRestoreHelper(
				inContextId, commandService, log);
		for (final MKeyBinding lBinding : lToReset) {
			final MKeyBinding lRestored = reset(inTable, lBinding,
					retrieveOrigSequence(lBinding));
			outBindingRestoreFactory.registerRestoredBinding(lRestored);
		}
		return outBindingRestoreFactory;
	}

	private MKeyBinding reset(final MBindingTable inTable,
			final MKeyBinding inOldBinding, final String inOrigSequence) {
		final MKeyBinding outNewBinding = CommandsFactoryImpl.eINSTANCE
				.createKeyBinding();
		outNewBinding.setCommand(inOldBinding.getCommand());
		outNewBinding.setKeySequence(inOrigSequence);
		outNewBinding.setContributorURI(inOldBinding.getContributorURI());

		final List<MParameter> lNewParameters = outNewBinding.getParameters();
		for (final MParameter lParameter : inOldBinding.getParameters()) {
			lNewParameters.add(lParameter);
		}

		final List<String> lNewTags = outNewBinding.getTags();
		for (final String lTag : inOldBinding.getTags()) {
			if (!lTag.startsWith(EBindingService.TYPE_ATTR_TAG + ":")
					&& !lTag.startsWith(ORIG_ATTR_TAG)) {
				lNewTags.add(lTag);
			}
		}
		final Object lOldBinding = inOldBinding.getTransientData().get(
				EBindingService.MODEL_TO_BINDING_KEY);
		if (lOldBinding != null) {
			outNewBinding.getTransientData().put(
					EBindingService.MODEL_TO_BINDING_KEY, lOldBinding);
		}
		inTable.getBindings().remove(inOldBinding);
		inTable.getBindings().add(outNewBinding);
		return outNewBinding;
	}

	private String retrieveOrigSequence(final MKeyBinding inBinding) {
		final List<String> lTags = inBinding.getTags();
		for (final String lTag : lTags) {
			if (lTag.startsWith(ORIG_ATTR_TAG)) {
				return lTag.substring(ORIG_ATTR_TAG.length());
			}
		}
		return null;
	}

	/**
	 * Save the new binding.
	 * 
	 * @param inNewBinding
	 *            {@link Binding}
	 */
	public final void addBinding(final Binding inNewBinding) {
		final MBindingTable lTable = getMTable(inNewBinding.getContextId());
		createORupdateMKeyBinding(application, lTable, inNewBinding);
	}

	static public MKeyBinding createORupdateMKeyBinding(
			final MApplication inApplication, final MBindingTable inTable,
			final Binding inNewBinding) {
		boolean lAddToTable = false;

		final ParameterizedCommand lParmetrizedCmd = inNewBinding
				.getParameterizedCommand();

		final String lCmdId = lParmetrizedCmd.getId();
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
		MKeyBinding lToReplace = null;
		for (final MKeyBinding lExistingMBinding : inTable.getBindings()) {
			final Binding lBinding = (Binding) lExistingMBinding
					.getTransientData().get(
							EBindingService.MODEL_TO_BINDING_KEY);
			if (inNewBinding.equals(lBinding)) {
				outNewBinding = lExistingMBinding;
				break;
			}
			if (isSameBinding(lExistingMBinding, lCmd, inNewBinding)) {
				outNewBinding = lExistingMBinding;
				break;
			}
			if (lCmdId.equals(lExistingMBinding.getCommand().getElementId())) {
				lToReplace = lExistingMBinding;
			}
		}

		if (outNewBinding == null) {
			lAddToTable = true;
			outNewBinding = CommandsFactoryImpl.eINSTANCE.createKeyBinding();
			outNewBinding.setCommand(lCmd);
			outNewBinding.setKeySequence(inNewBinding.getTriggerSequence()
					.toString());
			outNewBinding.setContributorURI(lToReplace.getContributorURI());

			for (final Object lObj : lParmetrizedCmd.getParameterMap()
					.entrySet()) {
				@SuppressWarnings({ "unchecked" })
				final Map.Entry<String, String> lEntry = (Map.Entry<String, String>) lObj;

				final String lParamID = lEntry.getKey();
				if (lParamID == null) {
					continue;
				}
				final List<MParameter> lBindingParams = outNewBinding
						.getParameters();
				MParameter lParameter = null;
				for (final MParameter lParam : lBindingParams) {
					if (lParamID.equals(lParam.getElementId())) {
						lParameter = lParam;
						break;
					}
				}
				if (lParameter == null) {
					lParameter = CommandsFactoryImpl.eINSTANCE
							.createParameter();
					lParameter.setElementId(lEntry.getKey());
					outNewBinding.getParameters().add(lParameter);
				}
				lParameter.setName(lEntry.getKey());
				lParameter.setValue(lEntry.getValue());
			}

			final List<String> lTags = outNewBinding.getTags();
			// just add the 'schemeId' tag if the binding is for anything other
			// than the default scheme
			if (inNewBinding.getSchemeId() != null
					&& !inNewBinding.getSchemeId().equals(
							BindingPersistence.getDefaultSchemeId())) {
				lTags.add(EBindingService.SCHEME_ID_ATTR_TAG
						+ ":" + inNewBinding.getSchemeId()); //$NON-NLS-1$
			}
			if (inNewBinding.getLocale() != null) {
				lTags.add(EBindingService.LOCALE_ATTR_TAG
						+ ":" + inNewBinding.getLocale()); //$NON-NLS-1$
			}
			if (inNewBinding.getPlatform() != null) {
				lTags.add(EBindingService.PLATFORM_ATTR_TAG
						+ ":" + inNewBinding.getPlatform()); //$NON-NLS-1$
			}
			// just add the 'type' tag if it's a user binding
			if (inNewBinding.getType() == Binding.USER) {
				lTags.add(EBindingService.TYPE_ATTR_TAG + ":user"); //$NON-NLS-1$
				lTags.add(String.format(TMPL_ORIG_SEQUENCE,
						getOrigSequence(lToReplace)));
			}
		}

		outNewBinding.getTransientData().put(
				EBindingService.MODEL_TO_BINDING_KEY, inNewBinding);
		if (lAddToTable) {
			inTable.getBindings().remove(lToReplace);
			inTable.getBindings().add(outNewBinding);
		}
		return outNewBinding;
	}

	private static String getOrigSequence(final MKeyBinding inToReplace) {
		final String lOldSequence = inToReplace.getKeySequence();
		final List<String> lOldTags = inToReplace.getTags();
		for (final String lTag : lOldTags) {
			if (lTag.startsWith(ORIG_ATTR_TAG)) {
				return lTag.substring(ORIG_ATTR_TAG.length());
			}
		}
		return lOldSequence;
	}

	static private boolean isSameBinding(final MKeyBinding inExistingBinding,
			final MCommand inCmd, final Binding inBinding) {
		// see org.eclipse.jface.bindings.Binding#equals(final Object object)
		if (!inCmd.equals(inExistingBinding.getCommand()))
			return false;
		final String lExistingKeySequence = inExistingBinding.getKeySequence();
		if (lExistingKeySequence == null)
			return false;
		try {
			final KeySequence lExistingSequence = KeySequence
					.getInstance(lExistingKeySequence);
			if (!lExistingSequence.equals(inBinding.getTriggerSequence()))
				return false;
		}
		catch (final ParseException exc) {
			return false;
		}

		// tags to look for:
		final List<String> lModelTags = inExistingBinding.getTags();

		final String lSchemeId = inBinding.getSchemeId();
		if (lSchemeId != null
				&& !lSchemeId.equals(BindingPersistence.getDefaultSchemeId())) {
			if (!lModelTags.contains(EBindingService.SCHEME_ID_ATTR_TAG
					+ ":" + lSchemeId)) //$NON-NLS-1$
				return false;
		}
		final String lLocale = inBinding.getLocale();
		if (lLocale != null) {
			if (!lModelTags.contains(EBindingService.LOCALE_ATTR_TAG
					+ ":" + lLocale)) //$NON-NLS-1$
				return false;
		}
		final String lPlatform = inBinding.getPlatform();
		if (lPlatform != null) {
			if (!lModelTags.contains(EBindingService.PLATFORM_ATTR_TAG
					+ ":" + lPlatform)) //$NON-NLS-1$
				return false;
		}
		if (inBinding.getType() == Binding.USER) {
			if (!lModelTags.contains(EBindingService.TYPE_ATTR_TAG + ":user")) //$NON-NLS-1$
				return false;
		}
		return true;
	}

	private MBindingTable getMTable(final String inContextId) {
		for (final MBindingTable outBindingTable : application
				.getBindingTables()) {
			if (outBindingTable.getBindingContext().getElementId()
					.equals(inContextId)) {
				return outBindingTable;
			}
		}
		// create a new table if we couldn't find one
		final MBindingTable outTable = CommandsFactoryImpl.eINSTANCE
				.createBindingTable();
		outTable.setBindingContext(getBindingContext(inContextId));
		outTable.setElementId(inContextId);
		application.getBindingTables().add(outTable);
		return outTable;

	}

	public MBindingContext getBindingContext(final String inId) {
		// cache
		MBindingContext lResult = bindingContexts.get(inId);
		if (lResult == null) {
			// search
			lResult = searchContexts(inId, application.getRootContext());
			if (lResult == null) {
				// create
				lResult = MCommandsFactory.INSTANCE.createBindingContext();
				lResult.setElementId(inId);
				lResult.setName("Auto::" + inId); //$NON-NLS-1$
				application.getRootContext().add(lResult);
			}
			if (lResult != null) {
				bindingContexts.put(inId, lResult);
			}
		}
		return lResult;
	}

	private MBindingContext searchContexts(final String inId,
			final List<MBindingContext> inRootContext) {
		for (final MBindingContext lContext : inRootContext) {
			if (lContext.getElementId().equals(inId)) {
				return lContext;
			}
			final MBindingContext lResult = searchContexts(inId,
					lContext.getChildren());
			if (lResult != null) {
				return lResult;
			}
		}
		return null;
	}

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
			return new KeyBinding(KeySequence.getInstance(inBinding
					.getKeySequence()), commandService.createCommand(inBinding
					.getCommand().getElementId(), null), inSchemeId, contextId,
					null, null, null, Binding.SYSTEM);
		}

		public void activateBindings(final EBindingService inBindingService,
				final String inSchemeId) {
			for (final MKeyBinding lBinding : bindings) {
				try {
					inBindingService.activateBinding(createKeyBinding(lBinding,
							inSchemeId));
				}
				catch (final ParseException exc) {
					log.error(
							exc,
							"Unable to restore binding \""
									+ lBinding.getKeySequence()
									+ "\" correctly!");
				}
			}
		}
	}

}
