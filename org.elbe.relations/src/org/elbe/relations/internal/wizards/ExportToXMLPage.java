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
package org.elbe.relations.internal.wizards;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.swt.widgets.FileDialog;
import org.elbe.relations.RelationsImages;
import org.elbe.relations.RelationsMessages;

/**
 * Page displaying the input field to enter the file name for the data backup.
 *
 * @author Luthiger
 */
public class ExportToXMLPage extends BackupEmbeddedPage {

	protected ExportToXMLPage(final String inName) {
		super(inName);
		setImageDescriptor(RelationsImages.WIZARD_EXPORT_XML.getDescriptor());
	}

	@Override
	protected String getLabelText() {
		return RelationsMessages.getString("ExportToXMLPage.lbl.text"); //$NON-NLS-1$
	}

	@Override
	protected void setFilterForDialog(final FileDialog inDialog) {
		inDialog.setFilterExtensions(new String[] { "*.xml", "*.zip" }); //$NON-NLS-1$ //$NON-NLS-2$
		inDialog.setFilterNames(
		        new String[] {
		                RelationsMessages.getString(
		                        "ExportToXMLPage.filter.plain"), //$NON-NLS-1$
		        RelationsMessages.getString("ExportToXMLPage.filter.zipped") }); //$NON-NLS-1$
		final DateFormat lFormat = new SimpleDateFormat(
		        "'Relations_full_export_'yyyy-MM-dd"); //$NON-NLS-1$
		inDialog.setFileName(lFormat.format(Calendar.getInstance().getTime()));
	}

	@Override
	protected boolean checkFileEndingCondition(final String inFileName) {
		final String lFileName = inFileName.toLowerCase();
		return lFileName.endsWith(".zip") || lFileName.endsWith(".xml"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected String postProcessFileName(final String inFileName) {
		if (inFileName.endsWith(".zip") || inFileName.endsWith(".xml")) { //$NON-NLS-1$ //$NON-NLS-2$
			return inFileName; // $NON-NLS-1$ //$NON-NLS-2$
		}
		return inFileName + ".xml"; //$NON-NLS-1$
	}

}
