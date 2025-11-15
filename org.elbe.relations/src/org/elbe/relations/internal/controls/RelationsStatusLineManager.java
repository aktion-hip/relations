/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
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

import jakarta.inject.Inject;

/** The Relations application's status line.<br>
 * This control is defined in <code>Relations.e4xmi</code>.
 *
 * @author Luthiger */
public class RelationsStatusLineManager {
    private static final int DISPLAY_PERIOD = 5; // number of seconds

    private final StatusLineContributionItem statusItemDBName;
    private final StatusLineContributionItem statusItemDBSize;

    private StatusLineManager statusLineManager;
    private IDataService dataService;

    /**
     * RelationsStatusLineManager constructor.
     */
    public RelationsStatusLineManager() {
        this.statusItemDBName = new StatusLineContributionItem(Constants.STATUS_ITEM_DB_NAME, 36);
        this.statusItemDBSize = new StatusLineContributionItem(Constants.STATUS_ITEM_DB_SIZE, 25);
    }

    @Inject
    void afterInit(final Composite parent, final IEclipseContext context, final IDataService dataService) {
        this.dataService = dataService;

        this.statusLineManager = new StatusLineManager();
        this.statusLineManager.createControl(parent);

        this.statusLineManager.prependToGroup(StatusLineManager.BEGIN_GROUP, this.statusItemDBName);
        this.statusLineManager.insertAfter(Constants.STATUS_ITEM_DB_NAME, this.statusItemDBSize);

        setData();
        final IEclipseContext wbContext = WizardHelper.getWorkbenchContext(context);
        wbContext.set(IStatusLineManager.class, this.statusLineManager);
        wbContext.set(RelationsStatusLineManager.class, this);
    }

    private void setData() {
        if (this.dataService != null) {
            setDBName(this.dataService.getDBName());
            setDBSize(this.dataService.getNumberOfItems());
        }
    }

    private void setDBName(final String inDBName) {
        this.statusItemDBName.setText(inDBName);
    }

    private void setDBSize(final int dbSize) {
        this.statusItemDBSize
        .setText(RelationsMessages
                .getString("RelationsStatusLineManager.lbl.number", new Object[] { Integer.valueOf(dbSize) })); //$NON-NLS-1$
    }

    /** Displays the specified text in the application's status line for 5 seconds.
     *
     * @param text String */
    public void showStatusLineMessage(final String text) {
        showStatusLineMessage(text, DISPLAY_PERIOD);
    }

    /** Displays the specified text in the application's status line for the specified number of seconds.
     *
     * @param text String
     * @param displayTime long Number of seconds the messages is displayed. */
    public void showStatusLineMessage(final String text, final int displayTime) {
        this.statusLineManager.setMessage(text);
        Display.getCurrent().timerExec(displayTime * 1000,
                () -> RelationsStatusLineManager.this.statusLineManager.setMessage(null));
    }

    /**
     * @return {@link IProgressMonitor} the status line's progress monitor
     */
    public IProgressMonitor getProgressMonitor() {
        return this.statusLineManager.getProgressMonitor();
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
