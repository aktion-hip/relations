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
package org.elbe.relations.internal.controls;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.Constants;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.utility.WizardHelper;

/**
 * The Relations application's status line.
 * 
 * @author Luthiger
 */
public class RelationsStatusLineManager {
	private final static int DISPLAY_PERIOD = 5; // number of seconds

	private final StatusLineContributionItem statusItemDBName;
	private final StatusLineContributionItem statusItemDBSize;

	private StatusLineManager statusLineManager;
	private IDataService dataService;

	/**
	 * RelationsStatusLineManager constructor.
	 */
	public RelationsStatusLineManager() {
		statusItemDBName = new StatusLineContributionItem(
		        Constants.STATUS_ITEM_DB_NAME, 36);
		statusItemDBSize = new StatusLineContributionItem(
		        Constants.STATUS_ITEM_DB_SIZE, 25);
	}

	@PostConstruct
	void afterInit(final Composite inParent, final IEclipseContext inContext,
	        final IDataService inDataService) {
		dataService = inDataService;

		statusLineManager = new StatusLineManager();
		statusLineManager.createControl(inParent);

		statusLineManager.prependToGroup(StatusLineManager.BEGIN_GROUP,
		        statusItemDBName);
		statusLineManager.insertAfter(Constants.STATUS_ITEM_DB_NAME,
		        statusItemDBSize);

		setData();
		final IEclipseContext lContext = WizardHelper
		        .getWorkbenchContext(inContext);
		lContext.set(IStatusLineManager.class, statusLineManager);
		lContext.set(RelationsStatusLineManager.class, this);
	}

	private void setData() {
		setDBName(dataService.getDBName());
		setDBSize(dataService.getNumberOfItems());
	}

	private void setDBName(final String inDBName) {
		statusItemDBName.setText(inDBName);
	}

	private void setDBSize(final int inDBSize) {
		statusItemDBSize
		        .setText(RelationsMessages
		                .getString(
		                        "RelationsStatusLineManager.lbl.number", new Object[] { new Integer(inDBSize) })); //$NON-NLS-1$
	}

	/**
	 * Displays the specified text in the application's status line for 5
	 * seconds.
	 * 
	 * @param inText
	 *            String
	 */
	public void showStatusLineMessage(final String inText) {
		showStatusLineMessage(inText, DISPLAY_PERIOD);
	}

	/**
	 * Displays the specified text in the application's status line for the
	 * specified number of seconds.
	 * 
	 * @param inText
	 *            String
	 * @param inDisplayTime
	 *            long Number of seconds the messages is displayed.
	 */
	public void showStatusLineMessage(final String inText,
	        final int inDisplayTime) {
		statusLineManager.setMessage(inText);
		Display.getCurrent().timerExec(inDisplayTime * 1000, new Runnable() {
			@Override
			public void run() {
				statusLineManager.setMessage(null);
			}
		});
	}

	/**
	 * @return {@link IProgressMonitor} the status line's progress monitor
	 */
	public IProgressMonitor getProgressMonitor() {
		return statusLineManager.getProgressMonitor();
	}

	/**
	 * Updates the status line information after a db change.
	 * 
	 * @param inEvent
	 *            String
	 */
	@Inject
	@Optional
	void updateCounter(
	        @UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_RELOAD) final String inEvent) {
		setData();
	}

	@Inject
	@Optional
	void updateDB(
	        @UIEventTopic(RelationsConstants.TOPIC_DB_CHANGED_INITIALZED) final String inEvent) {
		setData();
	}

}
