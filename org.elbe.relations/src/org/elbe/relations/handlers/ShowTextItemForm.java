/**
 * 
 */
package org.elbe.relations.handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.dnd.DropDataHelper;
import org.elbe.relations.handlers.item.ItemEditHandler;
import org.elbe.relations.models.ItemAdapter;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * A handler to show the text edit dialog.<br />
 * We need this handler to help the user to complete the text item after he has
 * dropped a web page causing a parsing error (because of a 403 status code).
 *
 * @author lbenno
 * @see DropDataHelper
 */
@SuppressWarnings("restriction")
public class ShowTextItemForm implements EventHandler {
	public static final String TOPIC = "relations/show/text/form"; //$NON-NLS-1$
	public static final String PARAM_CONTEXT = "eclipse_context"; //$NON-NLS-1$

	@Override
	public void handleEvent(Event event) {
		Object textId = event.getProperty(IEventBroker.DATA);
		if (textId instanceof UniqueID) {
			IEclipseContext context = (IEclipseContext) event
			        .getProperty(PARAM_CONTEXT);
			try {
				IDataService dataService = context.get(IDataService.class);
				ItemEditHandler.openEditDialog(new ItemAdapter(
				        dataService.retrieveItem((UniqueID) textId), context),
				        context);
			}
			catch (BOMException exc) {
				context.get(Logger.class).error(exc);
			}
		}
	}
}
