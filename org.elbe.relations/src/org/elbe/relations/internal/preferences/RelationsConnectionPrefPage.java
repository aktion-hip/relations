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
package org.elbe.relations.internal.preferences;

import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.elbe.relations.internal.forms.FormDBConnection;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.wizards.IUpdateListener;

/**
 * Display and manipulation of the Relations preferences concerning the database
 * connection.<br />
 * This is an Eclipse 3 preference page. To make it e4, let the values for the
 * annotated field be injected (instead of using the method init()).
 *
 * @author Luthiger
 */
public class RelationsConnectionPrefPage extends AbstractPreferencePage
        implements IUpdateListener {
	private FormDBConnection dbform;

	@Inject
	private IEclipseContext context;

	public RelationsConnectionPrefPage() {
		super();
	}

	/**
	 * @param inTitle
	 */
	public RelationsConnectionPrefPage(final String inTitle) {
		super(inTitle);
	}

	/**
	 * @param inTitle
	 * @param inImage
	 */
	public RelationsConnectionPrefPage(final String inTitle,
	        final ImageDescriptor inImage) {
		super(inTitle, inImage);
	}

	@Override
	protected Control createContents(final Composite inParent) {
		final Composite outComposite = new Composite(inParent, SWT.NONE);

		final int lColumns = 2;
		setLayout(outComposite, lColumns);
		outComposite.setFont(inParent.getFont());

		// DB entries
		dbform = FormDBConnection.createFormDBConnection(outComposite,
				lColumns, context);
		dbform.addUpdateListener(this);

		return outComposite;
	}

	@Override
	protected void performDefaults() {
		dbform.setDefaults();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		return dbform == null ? true : dbform.saveChanges();
	}

	@Override
	public void onUpdate(final IStatus inStatus) {
		setErrorMessage(FormUtility.getErrorMessage(inStatus));
		setValid(dbform.getPageComplete());
	}

	@Override
	public void dispose() {
		if (dbform != null) {
			dbform.removeUpdateListener(this);
			dbform.dispose();
		}
		super.dispose();
	}

}
