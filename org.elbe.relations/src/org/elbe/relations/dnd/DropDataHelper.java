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
package org.elbe.relations.dnd;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.handlers.ShowTextItemForm;
import org.elbe.relations.internal.actions.NewPersonAction;
import org.elbe.relations.internal.actions.NewTermAction;
import org.elbe.relations.internal.actions.RelateCommand;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.forms.RadioDialog;
import org.elbe.relations.internal.services.IMetadataExtractor;
import org.elbe.relations.internal.services.IWebPageParser;
import org.elbe.relations.models.IAssociationsModel;
import org.elbe.relations.parsing.WebPageParser.WebDropResult;
import org.elbe.relations.utility.NewTextAction;
import org.hip.kernel.exc.VException;

/**
 * Helper class to provide consistent handler functionality for data drops in
 * relations browsers.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public final class DropDataHelper {
	public static final Transfer[] DRAG_TYPES = new Transfer[] {
			ItemTransfer.getInstance(null) };
	public static final Transfer[] DROP_TYPES = new Transfer[] {
			ItemTransfer.getInstance(null), FileTransfer.getInstance(),
			URLTransfer.getInstance() };

	public enum TransferDropHandler {
		ITEM_TRANSFER(ItemTransfer.getInstance(null),
				new ItemTransferDropHandler()), FILE_TRANSFER(
						FileTransfer.getInstance(),
						new FileTransferDropHandler()), URL_TRANSFER(
								URLTransfer.getInstance(),
								new URLTransferDropHandler());

		private Transfer transfer;
		private IDropHandler handler;

		TransferDropHandler(final Transfer transfer,
				final IDropHandler handler) {
			this.transfer = transfer;
			this.handler = handler;
		}

		public boolean isSupportedType(final TransferData currentDataType) {
			return this.transfer.isSupportedType(currentDataType);
		}

		public IDropHandler getHandler() {
			return this.handler;
		}
	}

	// prevent class instantiation
	private DropDataHelper() {
	}

	/**
	 * Factory method: returns the appropriate drop handler to process the
	 * <code>TransferData</code>.
	 *
	 * @param event
	 *            {@link DropTargetEvent}
	 * @return IDropHandler
	 */
	public static IDropHandler getDropHandler(final DropTargetEvent event) {
		for (final TransferDropHandler handler : TransferDropHandler
				.values()) {
			if (handler.isSupportedType(event.currentDataType)) {
				return handler.getHandler();
			}
		}
		return null;
	}

	private static void addAsRelations(final UniqueID[] itemIDs,
			final IAssociationsModel model, final IEclipseContext context) {
		if (itemIDs.length == 0) {
			return;
		}
		final RelateCommand command = RelateCommand
				.createRelateCommand(model, itemIDs, context);
		command.execute();
	}

	// --- private classes ---

	/**
	 * Interface for classes that can handle drop events on items in the
	 * relations browser view.
	 */
	public interface IDropHandler {
		/**
		 * Handle the drop event, i.e. add the adequate relation.
		 *
		 * @param eventData
		 *            Object the <code>DropTargetEvent</code>s data
		 * @param model
		 *            {@link IAssociationsModel}
		 * @param context
		 *            {@link IEclipseContext}
		 */
		void handleDrop(Object eventData, IAssociationsModel model,
				IEclipseContext context);
	}

	/**
	 * Handling ItemTransfer
	 */
	private static class ItemTransferDropHandler implements IDropHandler {
		@Override
		public void handleDrop(final Object eventData,
				final IAssociationsModel model, final IEclipseContext context) {
			if (eventData instanceof UniqueID[]) {
				addAsRelations((UniqueID[]) eventData, model, context);
			}
		}
	}

	/**
	 * Handling FileTransfer
	 */
	private static class FileTransferDropHandler implements IDropHandler {

		@Override
		public void handleDrop(final Object eventData,
				final IAssociationsModel model, final IEclipseContext context) {
			try {
				final String fileName = ((String[]) eventData)[0];
				final IMetadataExtractor metadataExtractor = context
						.get(IMetadataExtractor.class);
				final UniqueID id = metadataExtractor
						.extract(new File(fileName));
				addAsRelations(new UniqueID[] { id }, model, context);
			}
			catch (final Exception exc) {
				final Logger log = context.get(Logger.class);
				if (log != null) {
					log.error(exc, exc.getMessage());
				}
			}
		}
	}

	/**
	 * Handling URLTransfer
	 */
	private static class URLTransferDropHandler implements IDropHandler {
		private static final int ITEM_TEXT_EXT = 4;
		private static final int[] OPTIONS_DFT = new int[] { IItem.TERM,
				IItem.TEXT, IItem.PERSON };
		private static final int[] OPTIONS_EXT = new int[] { IItem.TERM,
				IItem.TEXT, ITEM_TEXT_EXT, IItem.PERSON };
		private static final String[] LABELS_DFT = new String[] {
				RelationsMessages.getString("DropDataHelper.lbl.dft.term"), //$NON-NLS-1$
				RelationsMessages.getString("DropDataHelper.lbl.dft.text"), //$NON-NLS-1$
				RelationsMessages.getString("DropDataHelper.lbl.dft.person") }; //$NON-NLS-1$
		private static final String[] LABELS_EXT = new String[] {
				RelationsMessages.getString("DropDataHelper.lbl.ext.term"), //$NON-NLS-1$
				RelationsMessages.getString("DropDataHelper.lbl.ext.text"), //$NON-NLS-1$
				RelationsMessages.getString("DropDataHelper.lbl.ext.biblio"), //$NON-NLS-1$
				RelationsMessages.getString("DropDataHelper.lbl.ext.person") }; //$NON-NLS-1$

		@Override
		public void handleDrop(final Object eventData,
				final IAssociationsModel model, final IEclipseContext context) {
			final String url = (String) eventData;
			try {
				final IWebPageParser webPageParser = context
						.get(IWebPageParser.class);
				ContextInjectionFactory.inject(webPageParser, context);
				final WebDropResult webResult = webPageParser.parse(url);
				final int itemType = showItemSelectionDialog(webResult);
				if (itemType == -1) {
					return;
				}
				final UniqueID id = createItemOfType(itemType, webResult,
						context);
				addAsRelations(new UniqueID[] { id }, model, context);
			}
			catch (final Exception exc) {
				final Logger log = context.get(Logger.class);
				Display.getCurrent().beep();
				final RelationsStatusLineManager statusLine = context
						.get(RelationsStatusLineManager.class);
				statusLine.showStatusLineMessage(RelationsMessages
						.getString("DropDataHelper.msg.parsing.error")); //$NON-NLS-1$

				final String msg = exc.getMessage();
				if (is403Error(msg)) {
					// ask user to create an minimal text item
					if (MessageDialog
							.openQuestion(Display.getCurrent().getActiveShell(),
									RelationsMessages.getString(
											"Dialog.msg.title.problem"), //$NON-NLS-1$
									RelationsMessages.getString(
											"DropDataHelper.msg.response.403"))) { //$NON-NLS-1$
						final UniqueID textId = createMinText(model, context,
								url, log);
						if (textId != null) {
							final IEventBroker broker = context
									.get(IEventBroker.class);
							final Map<String, Object> data = new HashMap<>();
							data.put(IEventBroker.DATA, textId);
							data.put(ShowTextItemForm.PARAM_CONTEXT, context);
							broker.post(ShowTextItemForm.TOPIC, data);
						}
					}
				} else {
					if (log != null) {
						log.error(exc, msg);
					}
				}
			}
		}

		private UniqueID createMinText(final IAssociationsModel model,
				final IEclipseContext context, final String url, final Logger log) {
			try {
				final String undefined = RelationsMessages
						.getString("TextEditWizardPage.uncomplete.data"); //$NON-NLS-1$
				final NewTextAction action = new NewTextAction.Builder(
						undefined, undefined).type(AbstractText.TYPE_WEBPAGE)
						.publication(url).build(context);
				final IItem item = createText(action);
				final UniqueID id = new UniqueID(item.getItemType(),
						item.getID());
				addAsRelations(new UniqueID[] { id }, model, context);
				return id;
			}
			catch (final VException exc) {
				log.error(exc);
			}
			return null;
		}

		private boolean is403Error(final String errMsg) {
			return errMsg.contains("HTTP") && errMsg.contains("response") //$NON-NLS-1$ //$NON-NLS-2$
					&& errMsg.contains("code") && errMsg.contains("403"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Shows the dialog where the user can select the type of item to
		 * create.
		 *
		 * @param webResult
		 *            WebDropResult
		 * @return int the item type, or 4 for extended bibliography, or -1 if
		 *         cancelled.
		 */
		private int showItemSelectionDialog(final WebDropResult webResult) {
			RadioDialog dialog = null;
			if (webResult.hasBibliography()) {
				dialog = new RadioDialog(new Shell(Display.getCurrent()),
						RelationsMessages
						.getString("DropDataHelper.view.title.drop"), //$NON-NLS-1$
						RelationsMessages
						.getString("DropDataHelper.view.msg.drop"), //$NON-NLS-1$
						OPTIONS_EXT, ITEM_TEXT_EXT, LABELS_EXT);
			} else {
				dialog = new RadioDialog(new Shell(Display.getCurrent()),
						RelationsMessages
						.getString("DropDataHelper.view.title.drop"), //$NON-NLS-1$
						RelationsMessages
						.getString("DropDataHelper.view.msg.drop"), //$NON-NLS-1$
						OPTIONS_DFT, IItem.TEXT, LABELS_DFT);
			}
			if (dialog.open() == RadioDialog.OK) {
				return dialog.getResult();
			}
			return -1;
		}

		/**
		 * Create the requested item.
		 *
		 * @param itemType
		 *            int
		 * @param webResult
		 *            WebDropResult
		 * @param context
		 * @return UniqueID
		 * @throws VException
		 */
		private UniqueID createItemOfType(final int itemType,
				final WebDropResult webResult, final IEclipseContext context)
						throws VException {
			IItem item = null;
			switch (itemType) {
			case IItem.TERM:
				item = createTerm(webResult, context);
				break;
			case IItem.PERSON:
				item = createPerson(webResult, context);
				break;
			case IItem.TEXT:
				item = createText(webResult.getNewTextAction());
				break;
			case ITEM_TEXT_EXT:
				item = createText(webResult.getNewBiblioAction());
				break;
			}
			return item == null ? UniqueID.createUniqueID(null)
					: new UniqueID(item.getItemType(), item.getID());
		}

		private IItem createPerson(final WebDropResult webResult,
				final IEclipseContext context) {
			final NewPersonAction action = new NewPersonAction.Builder(
					webResult.getTitle()).text(webResult.getText())
					.build(context);
			action.execute();
			return action.getNewItem();
		}

		private IItem createText(final NewTextAction action) {
			action.execute();
			return action.getNewItem();
		}

		private IItem createTerm(final WebDropResult webResult,
				final IEclipseContext context) {
			final NewTermAction action = new NewTermAction.Builder(
					webResult.getTitle()).text(webResult.getText())
					.build(context);
			action.execute();
			return action.getNewItem();
		}
	}

}
