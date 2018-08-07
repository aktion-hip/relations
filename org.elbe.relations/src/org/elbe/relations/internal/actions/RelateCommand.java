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
package org.elbe.relations.internal.actions;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.models.IAssociationsModel;

/**
 * Command encapsulating the code to execute the action that relates the
 * specified items with a target item. This command can be used in a
 * drag'an'drop or triggered by a menu action.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class RelateCommand implements ICommand {
	IAssociationsModel model;
	UniqueID[] ids;

	@Inject
	private Logger log;

	@Inject
	private RelationsStatusLineManager statusLine;

	/**
	 * RelateCommand constructor, should not be called directly.
	 */
	RelateCommand() {
		super();
	}

	/**
	 * @param model
	 *            IAssociationsModel the target item
	 * @param ids
	 *            UniqueID[] the items to relate to the target item.
	 */
	void setParamters(final IAssociationsModel model, final UniqueID[] ids) {
		this.model = model;
		this.ids = ids;
	}

	/**
	 * Factory method to create instances of <code>RelateCommand</code> with
	 * properly injected values.
	 *
	 * @param model
	 *            {@link IAssociationsModel} the target item
	 * @param ids
	 *            UniqueID[] the items to relate to the target item
	 * @param context
	 *            {@link IEclipseContext}
	 * @return {@link RelateCommand}
	 */
	public static RelateCommand createRelateCommand(
	        final IAssociationsModel model, final UniqueID[] ids,
	        final IEclipseContext context) {
		final RelateCommand out = ContextInjectionFactory.make(
		        RelateCommand.class, context);
		out.setParamters(model, ids);
		return out;
	}

	/**
	 * Execute the command, i.e. relate the specified items with the target item
	 */
	@Override
	public void execute() {
		try {
			if (!this.model.isAssociated(this.ids)) {
				this.model.addAssociations(this.ids);
				this.model.saveChanges();
				Display.getCurrent().beep();
				this.statusLine.showStatusLineMessage(RelationsMessages
						.getString("RelateCommand.status.msg")); //$NON-NLS-1$
			}
		}
		catch (final BOMException exc) {
			this.log.error(exc, exc.getMessage());
		}
	}

}
