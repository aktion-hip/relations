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
package org.elbe.relations.internal.utility;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;

/**
 * Utility class providing convenience functions that can be used for import
 * file wizards.
 * 
 * @author Luthiger Created on 07.03.2010
 */
public class ImportDropHelper {

	/**
	 * Adds the capability to handle file drops to the specified control.
	 * 
	 * @param inInput
	 *            {@link Combo} the input field to add the file drop capability.
	 * @param inFilterExtensions
	 *            String[] e.g. <code>{"*.xml", "*.zip"}</code>
	 * @param inModifyListener
	 *            {@link ModifyListener}
	 */
	public static void wrapFileDrop(final Combo inInput,
	        final String[] inFilterExtensions,
	        final IModifyListener inModifyListener) {
		final DropTarget lTarget = new DropTarget(inInput, DND.DROP_COPY);
		final FileTransfer lFileTransfer = FileTransfer.getInstance();
		lTarget.setTransfer(new Transfer[] { lFileTransfer });
		final Collection<String> lExtensions = createExtensions(inFilterExtensions);

		lTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(final DropTargetEvent inEvent) {
				if (lFileTransfer.isSupportedType(inEvent.currentDataType)) {
					inEvent.detail = DND.DROP_COPY;
				}
			}

			@Override
			public void drop(final DropTargetEvent inEvent) {
				if (lFileTransfer.isSupportedType(inEvent.currentDataType)) {
					final String[] lFiles = (String[]) inEvent.data;
					if (lFiles.length > 1)
						return;
					final String lFile = lFiles[0];
					for (final String lExtension : lExtensions) {
						if (lFile.endsWith(lExtension)) {
							inInput.setText(lFile);
							break;
						}
					}
					inModifyListener.modifyText(lFile);
				}
			}
		});
	}

	private static Collection<String> createExtensions(
	        final String[] inFilterExtensions) {
		final Vector<String> outExtensions = new Vector<String>(
		        inFilterExtensions.length);
		for (final String lExtension : inFilterExtensions) {
			outExtensions
			        .add(lExtension.startsWith("*") ? lExtension.replaceAll("\\*", "") : lExtension); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return outExtensions;
	}

	// --- inner classes ---

	public interface IModifyListener {
		void modifyText(String inFileName);
	}

}
