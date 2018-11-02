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
import java.util.List;

import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;

/**
 * Helper class to fix key bindings duplicates.
 *
 * @author lbenno
 */
@SuppressWarnings("restriction")
public class DuplicateFixer {
	private static final String TAG_USER = EBindingService.TYPE_ATTR_TAG
	        + ":user"; //$NON-NLS-1$
	private static final String TAG_ORIG = "orig:"; //$NON-NLS-1$

	private final Collection<String> keys = new ArrayList<String>();
	private final Collection<String> duplicates = new ArrayList<String>();

	/**
	 *
	 */
	public DuplicateFixer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param keySequence
	 */
	public void add(String keySequence) {
		if (keys.contains(keySequence)) {
			duplicates.add(keySequence);
		} else {
			keys.add(keySequence);
		}
	}

	/**
	 * @return <code>true</code> if binding table contains duplicates
	 */
	public boolean hasDuplicates() {
		return !duplicates.isEmpty();
	}

	/**
	 * @param bindingTable
	 */
	public void fixDuplicates(MBindingTable bindingTable) {
		for (final String sequence : duplicates) {
			fixDuplicate(bindingTable, sequence);
		}
	}

	private void fixDuplicate(MBindingTable bindingTable, String sequence) {
		for (final MKeyBinding binding : bindingTable.getBindings()) {
			if (sequence.equals(binding.getKeySequence())) {
				if (!isValid(binding)) {
					setDeleted(binding);
				}
			}
		}
	}

	/**
	 * @param binding
	 */
	private void setDeleted(MKeyBinding binding) {
		binding.getTags().add(EBindingService.DELETED_BINDING_TAG);
	}

	/**
	 * @param binding
	 *            {@link MKeyBinding}
	 * @return boolean <code>true</code> if specified binding is the valid and
	 *         active binding, i.e. it contains one of the following tags:
	 *         [type:user, orig:M3+T, deleted]
	 */
	private boolean isValid(MKeyBinding binding) {
		final List<String> tags = binding.getTags();
		if (tags.isEmpty()) {
			return false;
		}
		for (final String tag : tags) {
			if (TAG_USER.equals(tag)) {
				return true;
			}
			if (EBindingService.DELETED_BINDING_TAG.equals(tag)) {
				return true;
			}
			if (tag.startsWith(TAG_ORIG)) {
				return true;
			}
		}
		return false;
	}

}
