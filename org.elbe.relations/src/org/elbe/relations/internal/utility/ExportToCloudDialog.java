/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2018, Benno Luthiger
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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.services.ICloudProviderConfig;

/**
 * Dialog to start the export to the cloud action.
 *
 * @author lbenno
 */
public class ExportToCloudDialog extends Dialog {

	private final ICloudProviderConfig cloudProviderConfig;
	private final boolean hasEvents;
	private boolean incrementalFlag;

	/**
	 * ExportToCloudDialog constructor.
	 *
	 * @param cloudProviderConfig
	 *            {@link ICloudProviderConfig}
	 * @param hasEvents
	 *            boolean <code>true</code> if the event store contains entries
	 */
	public ExportToCloudDialog(final ICloudProviderConfig cloudProviderConfig,
			final boolean hasEvents) {
		super(Display.getDefault().getActiveShell());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.cloudProviderConfig = cloudProviderConfig;
		this.hasEvents = hasEvents;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		parent.getShell().setText(RelationsMessages.getString("ExportToCloudDialog.dlg.title")); //$NON-NLS-1$
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(
				GridLayoutFactory.swtDefaults().numColumns(2)
				.extendedMargins(12, 3, 5, 3).create());

		final Label message = new Label(composite, SWT.NONE);
		message.setLayoutData(
				GridDataFactory.swtDefaults().span(2, 1).create());
		message.setText(String.format(RelationsMessages.getString("ExportToCloudDialog.dlg.msg"), //$NON-NLS-1$
				this.cloudProviderConfig.getName()));

		final Label synchType = new Label(composite, SWT.NONE);
		synchType.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.TOP).create());
		synchType.setText(RelationsMessages.getString("ExportToCloudDialog.sync.type.lbl")); //$NON-NLS-1$

		final Composite group = new Composite(composite, SWT.NONE);
		group.setLayoutData(
				GridDataFactory.fillDefaults().grab(true, false).create());
		group.setLayout(new RowLayout(SWT.VERTICAL));

		final Button btnIncremental = new Button(group, SWT.RADIO);
		btnIncremental.setText(RelationsMessages.getString("ExportToCloudDialog.btn.incr.lbl")); //$NON-NLS-1$
		btnIncremental.addSelectionListener(widgetSelectedAdapter(e -> {
			this.incrementalFlag = true;
		}));

		final Button btnFull = new Button(group, SWT.RADIO);
		btnFull.setText(RelationsMessages.getString("ExportToCloudDialog.btn.full.lbl")); //$NON-NLS-1$
		btnFull.addSelectionListener(widgetSelectedAdapter(e -> {
			this.incrementalFlag = false;
		}));

		if (this.hasEvents) {
			btnIncremental.setSelection(true);
			this.incrementalFlag = true;
		} else {
			btnFull.setSelection(true);
			btnIncremental.setSelection(false);
			btnIncremental.setEnabled(false);
			this.incrementalFlag = false;
		}

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, RelationsMessages.getString("ExportToCloudDialog.bnt.export.lbl"), true); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(340, 180);
	}

	/**
	 * @return boolean <code>true</code> in case of <i>incremental</i>
	 *         synchronization is selected, <code>false</code> in case of full
	 *         data synchronization
	 */
	public boolean isIncremental() {
		return this.incrementalFlag;
	}

}
