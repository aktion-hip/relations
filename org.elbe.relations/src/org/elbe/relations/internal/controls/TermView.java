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

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.preferences.LanguageService;
import org.hip.kernel.bom.AlternativeModel;

import jakarta.inject.Inject;

/**
 * View to select term items.
 *
 * @author Luthiger
 */
public class TermView extends AbstractSelectionView {
    private static final String POPUP_ID = "org.elbe.relations.view.terms.popup"; //$NON-NLS-1$
    private final IDataService data;

    @Inject
    public TermView(final Composite parent, final LanguageService languageService,
            final ESelectionService selectionService, final EHandlerService handlerService,
            final ECommandService commandService, final IDataService data) {
        super(parent, languageService, selectionService, handlerService, commandService);
        this.data = data;
    }

    @Override
    protected WritableList<AlternativeModel> getDBInput() {
        return new WritableList<>(this.data.getTerms(), ILightWeightItem.class);
    }

    @Override
    protected String getPopupID() {
        return POPUP_ID;
    }

}