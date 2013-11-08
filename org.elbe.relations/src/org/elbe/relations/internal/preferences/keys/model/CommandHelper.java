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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ParameterValuesException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.internal.registry.PerspectiveParameterValues;
import org.eclipse.ui.internal.registry.PreferencePageParameterValues;
import org.eclipse.ui.internal.registry.ViewParameterValues;
import org.eclipse.ui.internal.registry.WizardParameterValues;

/**
 * Helper class for the key bindings preferences page.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class CommandHelper {

	/**
	 * <p>
	 * Generates all the possible combinations of command parameterizations for
	 * the given command. If the command has no parameters, then this is simply
	 * a parameterized version of that command. If a parameter is optional, both
	 * the included and not included cases are considered.
	 * </p>
	 * <p>
	 * If one of the parameters cannot be loaded due to a
	 * <code>ParameterValuesException</code>, then it is simply ignored.
	 * </p>
	 * 
	 * @param inCommand
	 *            {@link Command} The command for which the parameter
	 *            combinations should be generated; must not be
	 *            <code>null</code>.
	 * @return A collection of <code>ParameterizedCommand</code> instances
	 *         representing all of the possible combinations. This value is
	 *         never empty and it is never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the command is not defined.
	 */
	public static Collection<ParameterizedCommand> generateCombinations(
			final Command inCommand) throws NotDefinedException {
		final IParameter[] lParameters = inCommand.getParameters();
		if (lParameters == null) {
			return Collections.singleton(new ParameterizedCommand(inCommand,
					null));
		}

		final Collection<List<Parameterization>> lExpansion = expandParameters(
				0, lParameters);
		final Collection<ParameterizedCommand> outCombinations = new ArrayList<ParameterizedCommand>(
				lExpansion.size());
		final Iterator<List<Parameterization>> lExpansionItr = lExpansion
				.iterator();
		while (lExpansionItr.hasNext()) {
			final List<Parameterization> lCombination = lExpansionItr.next();
			if (lCombination == null) {
				outCombinations.add(new ParameterizedCommand(inCommand, null));
			} else {
				while (lCombination.remove(null)) {
					// Just keep removing while there are null entries left.
				}
				if (lCombination.isEmpty()) {
					outCombinations.add(new ParameterizedCommand(inCommand,
							null));
				} else {
					final Parameterization[] lParameterizations = lCombination
							.toArray(new Parameterization[lCombination.size()]);
					outCombinations.add(new ParameterizedCommand(inCommand,
							lParameterizations));
				}
			}
		}

		return outCombinations;
	}

	/**
	 * Generates every possible combination of parameter values for the given
	 * parameters. Parameters values that cannot be initialized are just
	 * ignored. Optional parameters are considered.
	 * 
	 * @param inStartIndex
	 *            The index in the <code>parameters</code> that we should
	 *            process. This must be a valid index.
	 * @param inParameters
	 *            The parameters in to process; must not be <code>null</code>.
	 * @return A collection (<code>Collection</code>) of combinations (
	 *         <code>List</code> of <code>Parameterization</code>).
	 */
	@SuppressWarnings("unchecked")
	private static final Collection<List<Parameterization>> expandParameters(
			final int inStartIndex, final IParameter[] inParameters) {
		final int lNextIndex = inStartIndex + 1;
		final boolean lNoMoreParameters = (lNextIndex >= inParameters.length);

		final IParameter lParameter = inParameters[inStartIndex];
		final List<List<Parameterization>> outParameterizations = new ArrayList<List<Parameterization>>();
		if (lParameter.isOptional()) {
			outParameterizations.add(null);
		}

		IParameterValues lValues = null;
		try {
			lValues = lParameter.getValues();
		}
		catch (final ParameterValuesException exc) {
			if (lNoMoreParameters) {
				return outParameterizations;
			}
			// Make recursive call
			return expandParameters(lNextIndex, inParameters);
		}
		Map<String, String> lParameterValues = Collections.EMPTY_MAP;
		if (checkE4Parameter(lValues)) {
			lParameterValues = lValues.getParameterValues();
		}
		final Iterator<Map.Entry<String, String>> lParameterValueItr = lParameterValues
				.entrySet().iterator();
		while (lParameterValueItr.hasNext()) {
			final Map.Entry<String, String> lEntry = lParameterValueItr.next();
			final Parameterization lParameterization = new Parameterization(
					lParameter, lEntry.getValue());
			outParameterizations.add(createList(lParameterization));
		}

		// Check if another iteration will produce any more names.
		final int lParameterizationCount = outParameterizations.size();
		if (lNoMoreParameters) {
			// This is it, so just return the current parameterizations.
			for (int i = 0; i < lParameterizationCount; i++) {
				final List<Parameterization> lCombination = new ArrayList<Parameterization>(
						1);
				lCombination.add(getParameterizationChecked(
						outParameterizations, i));
				outParameterizations.set(i, lCombination);
			}
			return outParameterizations;
		}

		// Make recursive call
		final Collection<List<Parameterization>> lSuffixes = expandParameters(
				lNextIndex, inParameters);
		while (lSuffixes.remove(null)) {
			// just keep deleting the darn things.
		}
		if (lSuffixes.isEmpty()) {
			// This is it, so just return the current parameterizations.
			for (int i = 0; i < lParameterizationCount; i++) {
				final List<Parameterization> lCombination = new ArrayList<Parameterization>(
						1);
				lCombination.add(getParameterizationChecked(
						outParameterizations, i));
				outParameterizations.set(i, lCombination);
			}
			return outParameterizations;
		}
		final Collection<List<Parameterization>> outValue = new ArrayList<List<Parameterization>>();
		final Iterator<List<Parameterization>> lSuffixItr = lSuffixes
				.iterator();
		while (lSuffixItr.hasNext()) {
			final List<Parameterization> lCombination = lSuffixItr.next();
			final int lCombinationSize = lCombination.size();
			for (int i = 0; i < lParameterizationCount; i++) {
				final List<Parameterization> lNewCombination = new ArrayList<Parameterization>(
						lCombinationSize + 1);
				lNewCombination.add(getParameterizationChecked(
						outParameterizations, i));
				lNewCombination.addAll(lCombination);
				outValue.add(lNewCombination);
			}
		}

		return outValue;
	}

	private static Parameterization getParameterizationChecked(
			final List<List<Parameterization>> inParameterizations,
			final int inIndex) {
		final List<Parameterization> outValues = inParameterizations
				.get(inIndex);
		return outValues == null ? null : outValues.get(0);
	}

	private static List<Parameterization> createList(
			final Parameterization inParameterization) {
		final ArrayList<Parameterization> out = new ArrayList<Parameterization>(
				1);
		out.add(inParameterization);
		return out;
	}

	/**
	 * This is an ugly workaround to prevent that
	 * <code>PlatformUI.getWorkbench()</code> is called.
	 */
	private static boolean checkE4Parameter(final IParameterValues inValue) {
		if (inValue instanceof PerspectiveParameterValues
				|| inValue instanceof PreferencePageParameterValues
				|| inValue instanceof ViewParameterValues
				|| inValue instanceof WizardParameterValues) {
			return false;
		}
		return true;
	}

}
