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
package org.elbe.relations.internal.controller;

import java.util.ArrayList;
import java.util.List;

import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.services.IPrintOut;
import org.elbe.relations.services.IPrintService;
import org.osgi.framework.FrameworkUtil;

/**
 * OSGi service client to manage instances of <code>IPrintService</code>.
 * 
 * @author Luthiger
 */
public class PrintServiceController {
	private final List<IPrintService> printers = new ArrayList<IPrintService>();
	private IPrintService selectedPrinter;

	/**
	 * OSGi bind method.
	 * 
	 * @param inPrinter
	 *            {@link IPrintService}
	 */
	public void register(final IPrintService inPrinter) {
		printers.add(inPrinter);
	}

	/**
	 * OSGi unbind method.
	 * 
	 * @param inPrinter
	 *            {@link IPrintService}
	 */
	public void unregister(final IPrintService inPrinter) {
		printers.remove(inPrinter);
	}

	// ---

	/**
	 * Sets the print out configuration with the specified id as selected.
	 * 
	 * @param inSelectionIndex
	 *            int
	 */
	public void setSelected(final int inSelectionIndex) {
		selectedPrinter = printers.get(inSelectionIndex);
	}

	/**
	 * @return String the namespace ID (aka plug-in ID) of the selected print
	 *         out plug-in.
	 */
	public String getSelectedPrintOutPluginID() {
		if (selectedPrinter == null) {
			return RelationsConstants.DFT_PRINT_OUT_PLUGIN_ID;
		}
		return FrameworkUtil.getBundle(selectedPrinter.getClass())
				.getSymbolicName();
	}

	/**
	 * @return String[] Array initialized with values to fill
	 *         FileDialog.setFilterExtensions()
	 */
	public String[] getFilterExtensions() {
		final String[] outExtensions = new String[2];
		outExtensions[0] = selectedPrinter.getFileType();
		outExtensions[1] = "*.*"; //$NON-NLS-1$
		return outExtensions;
	}

	/**
	 * @return String[] Array initialized with values to fill
	 *         FileDialog.setFilterNames()
	 */
	public String[] getFilterNames() {
		final String[] outNames = new String[2];
		outNames[0] = selectedPrinter.getFileTypeName();
		outNames[1] = RelationsMessages.getString("PrintOutConfigHelper.all"); //$NON-NLS-1$
		return outNames;
	}

	/**
	 * @return {@link IPrintOut} the actual printer selection
	 */
	public IPrintOut getSelectedPrinter() {
		return selectedPrinter.getPrinter();
	}

	/**
	 * @return String[] the possible items of a printer selection combo
	 */
	public String[] getTextProcessorNames() {
		final String[] outNames = new String[printers.size()];
		int i = 0;
		for (final IPrintService lPrinter : printers) {
			outNames[i++] = lPrinter.getName();
		}
		return outNames;
	}

	/**
	 * @return int the index of the selected printer
	 */
	public int getSelected() {
		int i = 0;
		for (final IPrintService lPrinter : printers) {
			if (getSelectedPrintOutPluginID().equals(
					FrameworkUtil.getBundle(lPrinter.getClass())
							.getSymbolicName())) {
				return i;
			}
			i++;
		}
		return 0;
	}

}
