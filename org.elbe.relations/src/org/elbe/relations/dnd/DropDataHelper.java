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
 * @author Luthiger Created on 20.12.2009
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

		TransferDropHandler(final Transfer inTransfer,
		        final IDropHandler inHandler) {
			transfer = inTransfer;
			handler = inHandler;
		}

		public boolean isSupportedType(final TransferData inCurrentDataType) {
			return transfer.isSupportedType(inCurrentDataType);
		}

		public IDropHandler getHandler() {
			return handler;
		}
	}

	// prevent class instantiation
	private DropDataHelper() {
	}

	/**
	 * Factory method: returns the appropriate drop handler to process the
	 * <code>TransferData</code>.
	 *
	 * @param inEvent
	 *            {@link DropTargetEvent}
	 * @return IDropHandler
	 */
	public static IDropHandler getDropHandler(final DropTargetEvent inEvent) {
		for (final TransferDropHandler lHandler : TransferDropHandler
		        .values()) {
			if (lHandler.isSupportedType(inEvent.currentDataType)) {
				return lHandler.getHandler();
			}
		}
		return null;
	}

	private static void addAsRelations(final UniqueID[] inItemIDs,
	        final IAssociationsModel inModel, final IEclipseContext inContext) {
		if (inItemIDs.length == 0) {
			return;
		}
		final RelateCommand lCommand = RelateCommand
		        .createRelateCommand(inModel, inItemIDs, inContext);
		lCommand.execute();
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
		 * @param inEventData
		 *            Object the <code>DropTargetEvent</code>s data
		 * @param inModel
		 *            {@link IAssociationsModel}
		 * @param inContext
		 *            {@link IEclipseContext}
		 */
		void handleDrop(Object inEventData, IAssociationsModel inModel,
		        IEclipseContext inContext);
	}

	/**
	 * Handling ItemTransfer
	 */
	private static class ItemTransferDropHandler implements IDropHandler {
		@Override
		public void handleDrop(final Object inEventData,
		        final IAssociationsModel inModel,
		        final IEclipseContext inContext) {
			if (inEventData instanceof UniqueID[]) {
				addAsRelations((UniqueID[]) inEventData, inModel, inContext);
			}
		}
	}

	/**
	 * Handling FileTransfer
	 */
	private static class FileTransferDropHandler implements IDropHandler {

		@Override
		public void handleDrop(final Object inEventData,
		        final IAssociationsModel inModel,
		        final IEclipseContext inContext) {
			try {
				final String lFileName = ((String[]) inEventData)[0];
				final IMetadataExtractor lMetadataExtractor = inContext
				        .get(IMetadataExtractor.class);
				final UniqueID lID = lMetadataExtractor
				        .extract(new File(lFileName));
				addAsRelations(new UniqueID[] { lID }, inModel, inContext);
			}
			catch (final Exception exc) {
				final Logger lLog = inContext.get(Logger.class);
				if (lLog != null) {
					lLog.error(exc, exc.getMessage());
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
		public void handleDrop(final Object inEventData,
		        final IAssociationsModel inModel,
		        final IEclipseContext inContext) {
			final String lURL = (String) inEventData;
			try {
				final IWebPageParser lWebPageParser = inContext
				        .get(IWebPageParser.class);
				ContextInjectionFactory.inject(lWebPageParser, inContext);
				final WebDropResult lWebResult = lWebPageParser.parse(lURL);
				final int lItemType = showItemSelectionDialog(lWebResult);
				if (lItemType == -1) {
					return;
				}
				final UniqueID lID = createItemOfType(lItemType, lWebResult,
				        inContext);
				addAsRelations(new UniqueID[] { lID }, inModel, inContext);
			}
			catch (final Exception exc) {
				final Logger lLog = inContext.get(Logger.class);
				Display.getCurrent().beep();
				final RelationsStatusLineManager lStatusLine = inContext
				        .get(RelationsStatusLineManager.class);
				lStatusLine.showStatusLineMessage(RelationsMessages
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
						final UniqueID textId = createMinText(inModel,
						        inContext, lURL, lLog);
						if (textId != null) {
							final IEventBroker broker = inContext
							        .get(IEventBroker.class);
							final Map<String, Object> data = new HashMap<String, Object>();
							data.put(IEventBroker.DATA, textId);
							data.put(ShowTextItemForm.PARAM_CONTEXT, inContext);
							broker.post(ShowTextItemForm.TOPIC, data);
						}
					}
				} else {
					if (lLog != null) {
						lLog.error(exc, msg);
					}
				}
			}
		}

		private UniqueID createMinText(IAssociationsModel model,
		        IEclipseContext context, String url, Logger log) {
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

		private boolean is403Error(String errMsg) {
			return errMsg.contains("HTTP") && errMsg.contains("response") //$NON-NLS-1$ //$NON-NLS-2$
			        && errMsg.contains("code") && errMsg.contains("403"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Shows the dialog where the user can select the type of item to
		 * create.
		 *
		 * @param inWebResult
		 *            WebDropResult
		 * @return int the item type, or 4 for extended bibliography, or -1 if
		 *         cancelled.
		 */
		private int showItemSelectionDialog(final WebDropResult inWebResult) {
			RadioDialog lDialog = null;
			if (inWebResult.hasBibliography()) {
				lDialog = new RadioDialog(new Shell(Display.getCurrent()),
				        RelationsMessages
				                .getString("DropDataHelper.view.title.drop"), //$NON-NLS-1$
				        RelationsMessages
				                .getString("DropDataHelper.view.msg.drop"), //$NON-NLS-1$
				        OPTIONS_EXT, ITEM_TEXT_EXT, LABELS_EXT);
			} else {
				lDialog = new RadioDialog(new Shell(Display.getCurrent()),
				        RelationsMessages
				                .getString("DropDataHelper.view.title.drop"), //$NON-NLS-1$
				        RelationsMessages
				                .getString("DropDataHelper.view.msg.drop"), //$NON-NLS-1$
				        OPTIONS_DFT, IItem.TEXT, LABELS_DFT);
			}
			if (lDialog.open() == RadioDialog.OK) {
				return lDialog.getResult();
			}
			return -1;
		}

		/**
		 * Create the requested item.
		 *
		 * @param inItemType
		 *            int
		 * @param inWebResult
		 *            WebDropResult
		 * @param inContext
		 * @return UniqueID
		 * @throws VException
		 */
		private UniqueID createItemOfType(final int inItemType,
		        final WebDropResult inWebResult,
		        final IEclipseContext inContext) throws VException {
			IItem lItem = null;
			switch (inItemType) {
			case IItem.TERM:
				lItem = createTerm(inWebResult, inContext);
				break;
			case IItem.PERSON:
				lItem = createPerson(inWebResult, inContext);
				break;
			case IItem.TEXT:
				lItem = createText(inWebResult.getNewTextAction());
				break;
			case ITEM_TEXT_EXT:
				lItem = createText(inWebResult.getNewBiblioAction());
				break;
			}
			return lItem == null ? UniqueID.createUniqueID(null)
			        : new UniqueID(lItem.getItemType(), lItem.getID());
		}

		private IItem createPerson(final WebDropResult inWebResult,
		        final IEclipseContext inContext) {
			final NewPersonAction lAction = new NewPersonAction.Builder(
			        inWebResult.getTitle()).text(inWebResult.getText())
			                .build(inContext);
			lAction.execute();
			return lAction.getNewItem();
		}

		private IItem createText(final NewTextAction inAction) {
			inAction.execute();
			return inAction.getNewItem();
		}

		private IItem createTerm(final WebDropResult inWebResult,
		        final IEclipseContext inContext) {
			final NewTermAction lAction = new NewTermAction.Builder(
			        inWebResult.getTitle()).text(inWebResult.getText())
			                .build(inContext);
			lAction.execute();
			return lAction.getNewItem();
		}
	}

}
