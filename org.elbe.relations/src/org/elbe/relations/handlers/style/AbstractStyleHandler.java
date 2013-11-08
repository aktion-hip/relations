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
package org.elbe.relations.handlers.style;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.style.Styles;
import org.elbe.relations.internal.style.Styles.Style;
import org.elbe.relations.internal.style.Styles.StyleParameter;

/**
 * Base class for style handlers.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractStyleHandler {
	private static final String SELECTION_STATE = "selection.state";

	@Inject
	private IEventBroker eventBroker;

	/**
	 * Listener for events sent by the styled text widget. This method updates
	 * the button's toggle state (e.g. whether it should be displayed pushed).
	 * 
	 * @param inStyleParameter
	 *            {@link StyleParameter}
	 * @param inApplication
	 * @param inModel
	 */
	@Inject
	@Optional
	void setButtonState(
			@EventTopic(RelationsConstants.TOPIC_STYLE_CHANGED_INSPECTOR) final StyleParameter inStyleParameter,
			final MApplication inApplication, final EModelService inModel) {
		final Boolean lState = inStyleParameter.getIsToggeled(getStyle());
		final MHandledToolItem lItem = getToolItem(inApplication, inModel);
		lItem.setSelected(lState);
		lItem.getTransientData().put(SELECTION_STATE, lState);
	}

	/**
	 * Style handler executor method to apply the selected style on the text in
	 * the widget.
	 * 
	 * @param inApplyStyleFlag
	 *            String "true" to apply the selected style, "false" to remove
	 *            it. A value <code>null</code> signals the active styled text
	 *            widget is in the inspector view.
	 * @param inApplication
	 *            {@link MApplication}
	 * @param inModel
	 *            {@link EModelService}
	 */
	@Execute
	public void execute(
			@Optional @Named(RelationsConstants.PN_COMMAND_STYLE_SELECTION) final String inApplyStyleFlag,
			final MApplication inApplication, final EModelService inModel) {
		boolean lApply = true;
		if (inApplyStyleFlag == null) {
			// inspector
			lApply = getStyleApplyForInspector(inApplication, inModel);
		} else {
			// form
			lApply = Boolean.parseBoolean(inApplyStyleFlag);
		}
		eventBroker
				.post(RelationsConstants.TOPIC_STYLE_CHANGE_FORM, Styles
						.createStyleEvent(getStyle(), inApplyStyleFlag != null,
								lApply));
	}

	@CanExecute
	boolean canExecute(final IEclipseContext inContext) {
		return inContext.get(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE) != null;
	}

	private boolean getStyleApplyForInspector(final MApplication inApplication,
			final EModelService inModel) {
		final MHandledToolItem lItem = getToolItem(inApplication, inModel);
		final Boolean outOldState = (Boolean) lItem.getTransientData().get(
				SELECTION_STATE);
		lItem.getTransientData().put(SELECTION_STATE, !outOldState);
		return !outOldState;
	}

	private MHandledToolItem getToolItem(final MApplication inApplication,
			final EModelService inModel) {
		return inModel.findElements(inApplication, getStyle().getItemID(),
				MHandledToolItem.class, null).get(0);
	}

	abstract protected Style getStyle();

}
