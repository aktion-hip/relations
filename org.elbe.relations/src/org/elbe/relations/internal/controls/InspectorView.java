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

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.internal.services.ISelectedTextProvider;
import org.elbe.relations.internal.style.StyledTextComponent;
import org.elbe.relations.internal.utility.CheckDirtyService;
import org.elbe.relations.internal.utility.FormUtility;
import org.elbe.relations.internal.utility.InspectorViewVisitor;
import org.elbe.relations.models.CentralAssociationsModel;
import org.elbe.relations.models.ItemAdapter;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;
import org.xml.sax.SAXException;

/**
 * View for the inspector part, i.e. view to display the content of the selected
 * item. This view at the same time allows to edit items (or at least a part of
 * them).
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class InspectorView implements ISelectedTextProvider {
	public static final String PREF_SWITCH_VALUE = "relations.inspector.view.menu.switch"; //$NON-NLS-1$
	private static final String SWITCH_VALUE_BIBLIO = "bibliography"; // "content" //$NON-NLS-1$

	private enum DisplayType {
		NORMAL(new DisplayNormal()), DISABLED(new DisplayNone()), PERSON(
		        new DisplayPerson()), TEXT_BIBLIO(new DisplayTextBiblio()), TEXT_CONTENT(
		        new DisplayTextContent());

		private IDisplay display;

		DisplayType(final IDisplay inDisplay) {
			display = inDisplay;
		}

		void refresh(final Text inText, final StyledTextComponent inStyled,
		        final InspectorViewVisitor inVisitor) throws IOException,
		        SAXException {
			display.refresh(inText, inStyled, inVisitor);
		}
	}

	private Composite inspector;
	private CheckDirtyServiceInspector checkDirtyService;
	private Text title;
	private ControlDecoration errorDeco;
	private StyledTextComponent styledText;
	private boolean initialized = false;
	private ItemAdapter item;
	private boolean isSending = false;
	private boolean isSaving = false;
	private DisplayType displayType = DisplayType.DISABLED;
	private String switchValue;
	private final IEclipseContext context;

	@Inject
	private MDirtyable dirty;

	@Inject
	private Logger log;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private ESelectionService selectionService;

	@Inject
	EPartService partService;

	@Inject
	public InspectorView(final Composite inParent,
	        final IEclipseContext inContext) {
		context = inContext;
		initialize(inParent);
	}

	private void initialize(final Composite inParent) {
		inspector = new Composite(inParent, SWT.NULL);

		checkDirtyService = new CheckDirtyServiceInspector();

		title = new Text(inspector, SWT.BORDER | SWT.SINGLE);

		errorDeco = new ControlDecoration(title, SWT.LEFT | SWT.TOP);
		final int lIndent = FieldDecorationRegistry.getDefault()
		        .getMaximumDecorationWidth();
		errorDeco.setImage(FormUtility.IMG_ERROR);
		errorDeco.setDescriptionText(RelationsMessages
		        .getString("InspectorView.deco.empty")); //$NON-NLS-1$
		errorDeco.hide();

		final GridData lGrid = new GridData(GridData.FILL_HORIZONTAL);
		lGrid.horizontalIndent = lIndent;
		title.setLayoutData(lGrid);

		title.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent inEvent) {
				if (!initialized) {
					return;
				}
				final int lLength = ((Text) inEvent.widget).getText().length();
				if (lLength == 0) {
					errorDeco.show();
				} else {
					errorDeco.hide();
				}
			}
		});
		title.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				handleFocusGained();
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				handleFocusLost(inEvent);
			}
		});

		checkDirtyService.register(title);

		styledText = StyledTextComponent.createStyledText(inspector, context);
		styledText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				handleFocusGained();
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				handleFocusLost(inEvent);
			}
		});
		checkDirtyService.register(styledText);
		// aligning widget to indent caused by decoration in title
		styledText.getLayoutData().horizontalIndent = lIndent;

		final GridLayout lLayout = new GridLayout(1, true);
		lLayout.marginWidth = 2;
		lLayout.verticalSpacing = 0;
		inspector.setLayout(lLayout);

		title.setEditable(false);
		styledText.setDisabled();
	}

	private void handleFocusGained() {
		context.set(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE, "active"); //$NON-NLS-1$
		eventBroker.post(RelationsConstants.TOPIC_STYLE_ITEMS_FORM,
		        Boolean.TRUE);
	}

	private void handleFocusLost(final FocusEvent inEvent) {
		sendSelectionChecked(inEvent);
		context.remove(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE);
		eventBroker.post(RelationsConstants.TOPIC_STYLE_ITEMS_FORM,
		        Boolean.FALSE);
		// if the focus moved outside of the part, we ask for saving pending
		// changes
		if (checkDirtyService.isDirty()) {
			if (partService.getActivePart() != partService
			        .findPart(RelationsConstants.PART_INSPECTOR)) {
				isSaving = true;
				if (MessageDialog
				        .openQuestion(
				                Display.getCurrent().getActiveShell(),
				                RelationsMessages
				                        .getString("InspectorView.dialog.title"), RelationsMessages.getString("InspectorView.dialog.msg"))) { //$NON-NLS-1$ //$NON-NLS-2$
					saveChanges();
				}
				isSaving = false;
			}
		}
	}

	private void sendSelectionChecked(final FocusEvent inEvent) {
		final Widget lWidget = inEvent.widget;
		String lSelection = ""; //$NON-NLS-1$
		if (lWidget instanceof Text) {
			lSelection = ((Text) lWidget).getSelectionText();
		} else if (lWidget instanceof StyledText) {
			lSelection = ((StyledText) lWidget).getSelectionText();
		}
		if (!lSelection.isEmpty()) {
			selectionService.setSelection(lSelection);
		}
	}

	@Override
	public String getSelection() {
		String outSelection = ""; //$NON-NLS-1$
		if (title.isFocusControl()) {
			outSelection = title.getSelectionText();
		}
		if (outSelection.isEmpty()) {
			outSelection = styledText.getSelectionText();
		}
		return outSelection;
	}

	@PostConstruct
	void afterInit(
	        final EMenuService inService,
	        final EPartService inPartService,
	        @Preference(value = RelationsConstants.ACTIVE_BROWSER_ID) @Optional final String inBrowserId) {
		inService
		        .registerContextMenu(title, RelationsConstants.POPUP_INSPECTOR);
		inService.registerContextMenu(styledText.getControl(),
		        RelationsConstants.POPUP_INSPECTOR);

		// work around to have the application's focus on the browser stack
		if (inBrowserId != null) {
			final MPart lBrowser = inPartService.findPart(inBrowserId);
			if (lBrowser != null) {
				inPartService.activate(lBrowser, true);
			}
		}
	}

	@Focus
	void onFocus() {
		title.setFocus();
	}

	/**
	 * We changed the selection in the browser, therefore, show the newly
	 * selected item in the inspector.
	 * 
	 * @param inModel
	 *            {@link ItemAdapter}
	 */
	@Inject
	@Optional
	public void setSelected(
	        @UIEventTopic(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED) final SelectedItemChangeEvent inEvent) {
		if (!isSaving) {
			setSelected(inEvent.getItem());
		}
	}

	private void setSelected(final ItemAdapter inItem) {
		initialized = true;
		try {
			context.remove(RelationsConstants.FLAG_INSPECTOR_TEXT_ACTIVE);
			if (inItem == null) {
				item = null;
				displayType = DisplayType.DISABLED;
				displayType.refresh(title, styledText, null);
				clearDirty();
				errorDeco.hide();
			} else {
				displayType = getDisplayType(inItem);
				refreshDisplay(inItem);
			}
		}
		catch (final IOException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SAXException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	private void refreshDisplay(final ItemAdapter inModel) throws VException,
	        IOException, SAXException {
		item = inModel;
		if (item == null) {
			return;
		}

		final InspectorViewVisitor lVisitor = new InspectorViewVisitor();
		item.visit(lVisitor);
		displayType.refresh(title, styledText, lVisitor);
		clearDirty();
	}

	private DisplayType getDisplayType(final ItemAdapter inModel) {
		DisplayType out = DisplayType.NORMAL;
		if (inModel.getItemType() == IItem.PERSON) {
			out = DisplayType.PERSON;
		} else if (inModel.getItemType() == IItem.TEXT) {
			context.set(RelationsConstants.FLAG_INSPECTOR_TEXT_ACTIVE, "active"); //$NON-NLS-1$
			out = SWITCH_VALUE_BIBLIO.equals(switchValue) ? DisplayType.TEXT_BIBLIO
			        : DisplayType.TEXT_CONTENT;
		}
		return out;
	}

	@Inject
	@Optional
	public void setSelected(
	        @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL) final CentralAssociationsModel inModel) {
		setSelected(inModel == null ? null : inModel.getCenter());
	}

	/**
	 * We have edited the item in the edit wizard, therefore, we have to
	 * synchronize the inspector content.
	 * 
	 * @param inItem
	 *            {@link ItemAdapter}
	 */
	@Inject
	@Optional
	void updateEditChanges(
	        @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT) final ItemAdapter inItem) {
		if (isSending) {
			return;
		}
		if (inItem == null || !inItem.equals(item)) {
			return;
		}

		final InspectorViewVisitor lVisitor = new InspectorViewVisitor();
		try {
			item.visit(lVisitor);
			displayType.refresh(title, styledText, lVisitor);
		}
		catch (final VException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final IOException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SAXException exc) {
			log.error(exc, exc.getMessage());
		}
		clearDirty();
	}

	private void clearDirty() {
		checkDirtyService.freeze();
		dirty.setDirty(false);
	}

	public String getTitleText() {
		return title.getText();
	}

	public String getContentText() {
		return styledText.getTaggedText();
	}

	@Inject
	void trackViewMenuSwitch(
	        @Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = PREF_SWITCH_VALUE) final String inSwitchValue) {
		if (inSwitchValue == null) {
			return;
		}
		switchValue = inSwitchValue;
		displayType = SWITCH_VALUE_BIBLIO.equals(switchValue) ? DisplayType.TEXT_BIBLIO
		        : DisplayType.TEXT_CONTENT;
		if (initialized && !title.isDisposed() && !styledText.isDisposed()) {
			try {
				refreshDisplay(item);
			}
			catch (final VException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final IOException exc) {
				log.error(exc, exc.getMessage());
			}
			catch (final SAXException exc) {
				log.error(exc, exc.getMessage());
			}
		}
	}

	@Inject
	void trackFontSize(
	        @Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_TEXT_FONT_SIZE) final Integer inFontSize) {
		if (!title.isDisposed()) {
			final Font lFont = title.getFont();
			final FontData lData = lFont.getFontData()[0];
			lData.setHeight(inFontSize);
			final Font lNewFont = new Font(Display.getCurrent(), lData);
			title.setFont(lNewFont);
		}
	}

	@Persist
	void saveChanges() {
		try {
			item.saveTitleText(getTitleText(), getContentText());
			isSending = true;
			eventBroker.post(
			        RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT,
			        item);
			isSending = false;

			clearDirty();
			dirty.setDirty(false);
		}
		catch (final BOMException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	public void undoChanges() {
		checkDirtyService.undo();
		clearDirty();
		dirty.setDirty(false);
	}

	// --- private classes ---

	private class CheckDirtyServiceInspector extends CheckDirtyService {
		public CheckDirtyServiceInspector() {
			super(null);
		}

		@Override
		public void notifyDirtySwitch(final boolean inIsDirty) {
			if (isDirty ^ inIsDirty) {
				// if there is a switch in one element, check whether this was
				// the first clean or last dirty element
				final boolean lIsDirty = getDirty();
				if (isDirty ^ lIsDirty) {
					// the dialog's dirty status switched -> notification
					isDirty = lIsDirty;
					dirty.setDirty(inIsDirty);
				}
			}
		}
	}

	private static interface IDisplay {
		void refresh(final Text inText, final StyledTextComponent inStyled,
		        InspectorViewVisitor inVisitor) throws IOException,
		        SAXException;
	}

	private static class DisplayNormal implements IDisplay {
		@Override
		public void refresh(final Text inText,
		        final StyledTextComponent inStyled,
		        final InspectorViewVisitor inVisitor) throws IOException,
		        SAXException {
			inText.setText(inVisitor.getTitle());
			inText.setEditable(true);
			inStyled.setTaggedText(inVisitor.getText());
			inStyled.setEditable(true);
		}
	}

	private static class DisplayNone implements IDisplay {
		@Override
		public void refresh(final Text inText,
		        final StyledTextComponent inStyled,
		        final InspectorViewVisitor inVisitor) throws IOException,
		        SAXException {
			inText.setText(""); //$NON-NLS-1$
			inText.setEditable(false);
			inStyled.setText(""); //$NON-NLS-1$
			inStyled.setEditable(false);
		}
	}

	private static class DisplayPerson implements IDisplay {
		@Override
		public void refresh(final Text inText,
		        final StyledTextComponent inStyled,
		        final InspectorViewVisitor inVisitor) throws IOException,
		        SAXException {
			inText.setText(inVisitor.getTitle());
			inText.setEditable(false);
			inStyled.setTaggedText(inVisitor.getText());
			inStyled.setEditable(true);
		}
	}

	private static class DisplayTextBiblio implements IDisplay {
		@Override
		public void refresh(final Text inText,
		        final StyledTextComponent inStyled,
		        final InspectorViewVisitor inVisitor) throws IOException,
		        SAXException {
			inText.setText(inVisitor.getTitle());
			inText.setEditable(true);
			inStyled.setTaggedText(inVisitor.getText());
			inStyled.setEditable(false);
		}
	}

	private static class DisplayTextContent implements IDisplay {
		@Override
		public void refresh(final Text inText,
		        final StyledTextComponent inStyled,
		        final InspectorViewVisitor inVisitor) throws IOException,
		        SAXException {
			inText.setText(inVisitor.getTitle());
			inText.setEditable(true);
			inStyled.setTaggedText(inVisitor.getRealText());
			inStyled.setEditable(true);
		}
	}

}
