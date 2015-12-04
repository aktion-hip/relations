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
package org.elbe.relations.internal.wizards.interfaces;

import org.eclipse.jface.wizard.IWizard;

/**
 * Interface for import wizards.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in a wizard contributed to the workbench's import wizard extension point
 * (named <code>"org.eclipse.ui.importWizards"</code>). For example, the
 * plug-in's XML markup might contain:
 *
 * <pre>
 * &LT;extension point="org.eclipse.ui.importWizards"&GT;
 *   &LT;wizard
 *       id="com.example.myplugin.blob"
 *       name="Blob File"
 *       class="com.example.myplugin.BlobFileImporter"
 *       icon="icons/import_blob_wiz.gif"&GT;
 *     &LT;description&GT;Import resources from a BLOB file&LT;/description&GT;
 *     &LT;selection class="org.eclipse.core.resources.IResource" /&GT;
 *   &LT;/wizard&GT;
 * &LT;/extension&GT;
 * </pre>
 * </p>
 *
 * @see org.eclipse.jface.wizard.IWizard
 */
public interface IImportWizard extends IWizard {
}
