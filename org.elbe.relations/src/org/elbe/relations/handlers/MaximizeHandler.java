/**
 *
 */
package org.elbe.relations.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;

/**
 * Handler to maximize the active part using M1+M shortcut.
 *
 * @author lbenno
 */
public class MaximizeHandler {
	private static String MAXIMIZED = IPresentationEngine.MAXIMIZED;

	@Execute
	public void execute(
	        @Named(IServiceConstants.ACTIVE_PART) final MPart activePart) {
		final MUIElement part = activePart instanceof MElementContainer
		        ? activePart : activePart.getParent();
		final List<String> tags = part.getTags();
		if (tags.contains(MAXIMIZED)) {
			tags.remove(MAXIMIZED);
		} else {
			tags.add(MAXIMIZED);
		}
	}

}
