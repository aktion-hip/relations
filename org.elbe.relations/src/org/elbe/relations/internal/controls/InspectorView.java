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

import java.io.IOException;

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
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.elbe.relations.utility.FontUtil;
import org.elbe.relations.utility.SelectedItemChangeEvent;
import org.hip.kernel.exc.VException;
import org.xml.sax.SAXException;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

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
        // @formatter:off
        NORMAL(new DisplayNormal()),
        DISABLED(new DisplayNone()),
        PERSON(new DisplayPerson()),
        TEXT_BIBLIO(new DisplayTextBiblio()),
        TEXT_CONTENT(new DisplayTextContent());
        // @formatter:on

        private final IDisplay display;

        DisplayType(final IDisplay display) {
            this.display = display;
        }

        void refresh(final Text text, final StyledTextComponent styled, final InspectorViewVisitor visitor)
                throws IOException, SAXException {
            this.display.refresh(text, styled, visitor);
        }
    }

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
    private final java.util.Optional<MDirtyable> dirty;
    private final IEventBroker eventBroker;
    private final ESelectionService selectionService;
    private final EPartService partService;
    private final Logger log;

    @Inject
    public InspectorView(final Composite parent, final IEclipseContext context, final MDirtyable dirty,
            final EPartService partService, final ESelectionService selectionService, final IEventBroker eventBroker,
            final Logger log) {
        this.context = context;
        this.dirty = java.util.Optional.ofNullable(dirty);
        this.partService = partService;
        this.selectionService = selectionService;
        this.eventBroker = eventBroker;
        this.log = log;

        initialize(parent);
    }

    private void initialize(final Composite parent) {
        final Composite inspector = new Composite(parent, SWT.NULL);

        this.checkDirtyService = new CheckDirtyServiceInspector(this.dirty);

        this.title = new Text(inspector, SWT.BORDER | SWT.SINGLE);

        this.errorDeco = new ControlDecoration(this.title, SWT.LEFT | SWT.TOP);
        final int indent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
        this.errorDeco.setImage(FormUtility.IMG_ERROR);
        this.errorDeco.setDescriptionText(RelationsMessages.getString("InspectorView.deco.empty")); //$NON-NLS-1$
        this.errorDeco.hide();

        final GridData grid = new GridData(GridData.FILL_HORIZONTAL);
        grid.horizontalIndent = indent;
        this.title.setLayoutData(grid);

        this.title.addModifyListener(event -> {
            if (!InspectorView.this.initialized) {
                return;
            }
            if (((Text) event.widget).getText().isEmpty()) {
                InspectorView.this.errorDeco.show();
            } else {
                InspectorView.this.errorDeco.hide();
            }
        });
        this.title.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                handleFocusGained(false);
            }

            @Override
            public void focusLost(final FocusEvent event) {
                handleFocusLost(event);
            }
        });

        this.checkDirtyService.register(this.title);

        this.styledText = StyledTextComponent.createStyledText(inspector, this.context);
        this.styledText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                handleFocusGained(true);
            }

            @Override
            public void focusLost(final FocusEvent event) {
                handleFocusLost(event);
            }
        });
        this.checkDirtyService.register(this.styledText);
        // aligning widget to indent caused by decoration in title
        this.styledText.getLayoutData().horizontalIndent = indent;

        final GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 2;
        layout.verticalSpacing = 0;
        inspector.setLayout(layout);

        this.title.setEditable(false);
        this.styledText.setDisabled();
    }

    /**
     * @param isTextField <code>true</code> styled text field, <code>false</code> title field
     */
    private void handleFocusGained(final boolean isTextField) {
        if (isTextField) {
            this.context.set(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE, "active"); //$NON-NLS-1$
        }
        this.eventBroker.post(RelationsConstants.TOPIC_STYLE_ITEMS_FORM, Boolean.TRUE);
    }

    private void handleFocusLost(final FocusEvent event) {
        sendSelectionChecked(event);
        this.context.remove(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE);
        this.eventBroker.post(RelationsConstants.TOPIC_STYLE_ITEMS_FORM, Boolean.FALSE);
        // if the focus moved outside of the part, we ask for saving pending changes
        if (this.checkDirtyService.isDirty() && this.partService.getActivePart() != this.partService // NOPMD
                .findPart(RelationsConstants.PART_INSPECTOR)) {
            this.isSaving = true;
            if (MessageDialog.openQuestion(
                    Display.getCurrent().getActiveShell(),
                    RelationsMessages
                    .getString("InspectorView.dialog.title"), RelationsMessages.getString("InspectorView.dialog.msg"))) { //$NON-NLS-1$ //$NON-NLS-2$
                saveChanges();
            }
            this.isSaving = false;
        }
    }

    private void sendSelectionChecked(final FocusEvent event) {
        final Widget widget = event.widget;
        String selection = ""; //$NON-NLS-1$
        if (widget instanceof final Text txtWidget) {
            selection = txtWidget.getSelectionText();
        } else if (widget instanceof final StyledText styledWidget) {
            selection = styledWidget.getSelectionText();
        }
        if (!selection.isEmpty()) {
            this.selectionService.setSelection(selection);
        }
    }

    @Override
    public String getSelection() {
        String outSelection = ""; //$NON-NLS-1$
        if (this.title.isFocusControl()) {
            outSelection = this.title.getSelectionText();
        }
        if (outSelection.isEmpty()) {
            outSelection = this.styledText.getSelectionText();
        }
        return outSelection;
    }

    @PostConstruct
    void afterInit(final EMenuService service, final EPartService partService,
            @Preference(value = RelationsConstants.ACTIVE_BROWSER_ID) @Optional final String browserId) {
        service.registerContextMenu(this.styledText.getControl(), RelationsConstants.POPUP_INSPECTOR);

        // work around to have the application's focus on the browser stack
        if (browserId != null) {
            final MPart browser = partService.findPart(browserId);
            if (browser != null) {
                partService.activate(browser, true);
            }
        }
    }

    @Focus
    void onFocus() {
        this.title.setFocus();
    }

    /** We changed the selection in the browser, therefore, show the newly selected item in the inspector.
     *
     * @param event {@link SelectedItemChangeEvent} */
    @Inject
    @Optional
    public void setSelected(
            @UIEventTopic(RelationsConstants.TOPIC_TO_BROWSER_MANAGER_SET_SELECTED) final SelectedItemChangeEvent event) {
        if (!this.isSaving) {
            setSelected(event.getItem());
        }
    }

    private void setSelected(final ItemAdapter item) {
        this.initialized = true;
        try {
            this.context.remove(RelationsConstants.FLAG_INSPECTOR_TEXT_ACTIVE);
            if (item == null) {
                this.item = null;
                this.displayType = DisplayType.DISABLED;
                this.displayType.refresh(this.title, this.styledText, null);
                clearDirty();
                this.errorDeco.hide();
            } else {
                this.displayType = getDisplayType(item);
                refreshDisplay(item);
            }
        } catch (IOException | SAXException | VException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

    private void refreshDisplay(final ItemAdapter model) throws VException, IOException, SAXException {
        this.item = model;
        if (this.item == null) {
            return;
        }

        final InspectorViewVisitor visitor = new InspectorViewVisitor();
        this.item.visit(visitor);
        this.displayType.refresh(this.title, this.styledText, visitor);
        clearDirty();
    }

    private DisplayType getDisplayType(final ItemAdapter model) {
        DisplayType type = DisplayType.NORMAL;
        if (model.getItemType() == IItem.PERSON) {
            type = DisplayType.PERSON;
        } else if (model.getItemType() == IItem.TEXT) {
            this.context.set(RelationsConstants.FLAG_INSPECTOR_TEXT_ACTIVE, "active"); //$NON-NLS-1$
            type = SWITCH_VALUE_BIBLIO.equals(this.switchValue) ? DisplayType.TEXT_BIBLIO
                    : DisplayType.TEXT_CONTENT;
        }
        return type;
    }

    @Inject
    @Optional
    public void setSelected(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL) final CentralAssociationsModel inModel) {
        setSelected(inModel == null ? null : inModel.getCenter());
    }

    /** We have edited the item in the edit wizard, therefore, we have to synchronize the inspector content.
     *
     * @param item {@link ItemAdapter} */
    @Inject
    @Optional
    void updateEditChanges(
            @UIEventTopic(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT) final ItemAdapter item) {
        if (this.isSending) {
            return;
        }
        if (item == null || !item.equals(this.item)) {
            return;
        }

        try (InspectorViewVisitor visitor = new InspectorViewVisitor()) {
            this.item.visit(visitor);
            this.displayType.refresh(this.title, this.styledText, visitor);
        } catch (VException | IOException | SAXException exc) {
            this.log.error(exc, exc.getMessage());
        }
        clearDirty();
    }

    private void clearDirty() {
        this.checkDirtyService.freeze();
        this.dirty.ifPresent(d -> d.setDirty(false));
    }

    public String getTitleText() {
        return this.title.getText();
    }

    public String getContentText() {
        return this.styledText.getTaggedText();
    }

    @Inject
    void trackViewMenuSwitch(
            @Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = PREF_SWITCH_VALUE) final String switchValue) {
        if (switchValue == null) {
            return;
        }
        this.switchValue = switchValue;
        this.displayType = SWITCH_VALUE_BIBLIO.equals(this.switchValue) ? DisplayType.TEXT_BIBLIO
                : DisplayType.TEXT_CONTENT;
        if (this.initialized && !this.title.isDisposed() && !this.styledText.isDisposed()) {
            try {
                refreshDisplay(this.item);
            } catch (VException | IOException | SAXException exc) {
                this.log.error(exc, exc.getMessage());
            }
        }
    }

    @Inject
    void trackFontSize(
            @Preference(nodePath = RelationsConstants.PREFERENCE_NODE, value = RelationsConstants.KEY_TEXT_FONT_SIZE) final Integer fontSize) {
        if (!this.title.isDisposed() && !getTitleText().isEmpty()) {
            final FontData data = this.title.getFont().getFontData()[0];
            if (fontSize != data.getHeight()) {
                FontUtil.createOrGetFont(fontSize).ifPresent(f -> this.title.setFont(f));
            }
        }
    }

    @Persist
    void saveChanges() {
        try {
            this.item.saveTitleText(getTitleText(), getContentText());
            this.isSending = true;
            this.eventBroker.post(RelationsConstants.TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT, this.item);
            this.isSending = false;

            clearDirty();
            this.dirty.ifPresent(d -> d.setDirty(false));
        } catch (final BOMException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

    public void undoChanges() {
        this.checkDirtyService.undo();
        clearDirty();
        this.dirty.ifPresent(d -> d.setDirty(false));
    }

    // --- private classes ---

    /** CheckDirtyService for Inspector class. */
    private class CheckDirtyServiceInspector extends CheckDirtyService {
        private final java.util.Optional<MDirtyable> dirty;

        public CheckDirtyServiceInspector(final java.util.Optional<MDirtyable> dirty) {
            super(null);
            this.dirty = dirty;
        }

        @Override
        public void notifyDirtySwitch(final boolean isDirty) {
            if (this.isDirty ^ isDirty) {
                // if there is a switch in one element, check whether this was
                // the first clean or last dirty element
                final boolean localDirty = getDirty();
                if (this.isDirty ^ localDirty) {
                    // the dialog's dirty status switched -> notification
                    this.isDirty = localDirty;
                    this.dirty.ifPresent(d -> d.setDirty(isDirty));
                }
            }
        }
    }

    /** Interface definition. */
    interface IDisplay {
        void refresh(final Text text, final StyledTextComponent styled, InspectorViewVisitor visitor)
                throws IOException, SAXException;
    }

    /** <code>IDisplay</code> implementations. */
    private static class DisplayNormal implements IDisplay {
        @Override
        public void refresh(final Text text, final StyledTextComponent styled, final InspectorViewVisitor visitor)
                throws IOException, SAXException {
            text.setText(visitor.getTitle());
            text.setEditable(true);
            styled.setTaggedText(visitor.getText());
            styled.setEditable(true);
        }
    }

    private static class DisplayNone implements IDisplay {
        @Override
        public void refresh(final Text text, final StyledTextComponent styled, final InspectorViewVisitor visitor)
                throws IOException, SAXException {
            text.setText(""); //$NON-NLS-1$
            text.setEditable(false);
            styled.setText(""); //$NON-NLS-1$
            styled.setEditable(false);
        }
    }

    private static class DisplayPerson implements IDisplay {
        @Override
        public void refresh(final Text text, final StyledTextComponent styled, final InspectorViewVisitor visitor)
                throws IOException, SAXException {
            text.setText(visitor.getTitle());
            text.setEditable(false);
            styled.setTaggedText(visitor.getText());
            styled.setEditable(true);
        }
    }

    private static class DisplayTextBiblio implements IDisplay {
        @Override
        public void refresh(final Text text, final StyledTextComponent styled, final InspectorViewVisitor visitor)
                throws IOException, SAXException {
            text.setText(visitor.getTitle());
            text.setEditable(true);
            styled.setTaggedText(visitor.getText());
            styled.setEditable(false);
        }
    }

    private static class DisplayTextContent implements IDisplay {
        @Override
        public void refresh(final Text text, final StyledTextComponent styled, final InspectorViewVisitor visitor)
                throws IOException, SAXException {
            text.setText(visitor.getTitle());
            text.setEditable(true);
            styled.setTaggedText(visitor.getRealText());
            styled.setEditable(true);
        }
    }

}
