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
package org.elbe.relations.internal.forms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Control;

/**
 * Helper class to manage the field stati of registered widgets.
 * 
 * @author Luthiger Created on 12.10.2008
 */
public class FieldStatusManager {
	private final Hashtable<Control, IStatus> register = new Hashtable<Control, IStatus>();

	/**
	 * Initialize the manager with the specified field. The status for this
	 * field is set to <code>Status.OK_STATUS</code>.
	 * 
	 * @param inField
	 *            Control
	 */
	public void initialize(final Control inField) {
		register.put(inField, Status.OK_STATUS);
	}

	/**
	 * Set the specified's field to the specified status.
	 * 
	 * @param inField
	 *            Control
	 * @param inStatus
	 *            IStatus
	 */
	public void set(final Control inField, final IStatus inStatus) {
		register.put(inField, inStatus);
	}

	/**
	 * @return IStatus[] the stati of the regiestered form fields.
	 */
	public IStatus[] getStati() {
		final IStatus[] outStati = new IStatus[register.size()];
		int i = 0;
		final Enumeration<IStatus> lValues = register.elements();
		while (lValues.hasMoreElements()) {
			outStati[i++] = lValues.nextElement();
		}
		return outStati;
	}

	/**
	 * Set all registered form fields to <code>Status.OK_STATUS</code>.
	 */
	public void reset() {
		for (final Entry<Control, IStatus> lEntry : register.entrySet()) {
			lEntry.setValue(Status.OK_STATUS);
		}
	}

	/**
	 * Set all registered form fields except the specified to
	 * <code>Status.OK_STATUS</code>.
	 * 
	 * @param inField
	 *            Control the field excepted from resetting.
	 */
	public void resetExcept(final Control inField) {
		for (final Entry<Control, IStatus> lEntry : register.entrySet()) {
			if (!lEntry.getKey().equals(inField)) {
				lEntry.setValue(Status.OK_STATUS);
			}
		}
	}

}
