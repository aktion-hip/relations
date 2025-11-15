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
package org.elbe.relations.internal.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ParameterValuesException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.elbe.relations.Activator;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.internal.style.ResizeFontControl;
import org.elbe.relations.internal.style.StyledTextComponent;
import org.elbe.relations.internal.style.Styles;
import org.elbe.relations.internal.style.Styles.Style;
import org.elbe.relations.internal.style.Styles.StyleParameter;
import org.elbe.relations.internal.utility.CheckDirtyService;
import org.elbe.relations.internal.utility.CheckDirtyServiceNoop;

import jakarta.inject.Inject;

/** Base class for the edit forms to edit or create items, i.e. term, text or person items.
 *
 * @author Luthiger */
@SuppressWarnings("restriction")
public abstract class AbstractEditForm {
    private static final String MUI_ID_STYLING_TOOLBAR = "relations.toolbar:text.styling"; //$NON-NLS-1$

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 80;

    private FontMetrics fontMetrics;

    protected Composite container;
    protected StyledTextComponent styledText;
    private Composite styledContainer;
    protected CheckDirtyService checkDirtyService;

    private String viewTitle = ""; //$NON-NLS-1$
    private boolean editMode = false;
    private Label labelCreated = null;

    @Inject
    private IEclipseContext context;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private MApplication application;

    @Inject
    private EModelService modelService;

    @Inject
    private EBindingService bindingService;

    /** Subclasses have to call immediately after object creation.
     *
     * @param editMode boolean <code>true</code> if this form is in edit mode, <code>false</code> for new mode */
    protected void setEditMode(final boolean editMode) {
        this.editMode = editMode;
        this.checkDirtyService = editMode ? new CheckDirtyService(this) : new CheckDirtyServiceNoop();
    }

    public abstract void initialize();

    /** Notifies the observers.
     *
     * @param statuses IStatus[] */
    protected void notifyAboutUpdate(final IStatus[] statuses) {
        final MultiStatus multi = new MultiStatus(Activator.getSymbolicName(), 1, statuses, "", null); //$NON-NLS-1$
        this.eventBroker.post(RelationsConstants.TOPIC_WIZARD_PAGE_STATUS, multi);
    }

    /** Subclasses must implement.
     *
     * @return IStatus[] Array of status information. */
    protected abstract IStatus[] getStatuses();

    protected Label createLabel(final String labelValue, final Composite container) {
        return createLabel(labelValue, container, 1);
    }

    protected Label createLabel(final String labelValue, final Composite container, final int numColumns) {
        final Label label = new Label(container, SWT.NULL);
        label.setText(labelValue);

        final GridData data = new GridData(SWT.FILL, SWT.NULL, false, false, numColumns, SWT.NULL);
        data.widthHint = (int) (label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x * 1.2);
        label.setLayoutData(data);
        return label;
    }

    protected Text createText(final Composite container, final int numColumns) {
        final Text text = createText(container);
        text.setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false, numColumns, SWT.NULL));
        return text;
    }

    protected Text createText(final Composite container) {
        return new Text(container, SWT.BORDER | SWT.SINGLE);
    }

    protected StyledTextComponent createStyledText(final Composite container) {
        return createStyledText(container, new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    protected StyledTextComponent createStyledText(final Composite container, final int numColumns,
            final int height) {
        final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, numColumns, SWT.NULL);
        data.heightHint = height;
        return createStyledText(container, data);
    }

    /** Creates <code>StyledTextComponent</code> for the edit form. The widget is complete with a toolbar above
     * displaying the style controls and a popup menu (displaying the style controls too).
     *
     * @param container Composite parent widget
     * @param data GridData layout data
     * @return StyledTextComponent */
    private StyledTextComponent createStyledText(final Composite container, final GridData data) {
        this.styledContainer = new Composite(container, SWT.NONE);
        final GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 0;
        layout.marginTop = 0;
        layout.verticalSpacing = 2;
        this.styledContainer.setLayout(layout);
        setDefaultSize(data);
        this.styledContainer.setLayoutData(data);

        final STKeyListener listener = new STKeyListener(this.application);
        final ContributionItemsFactory styleBarHelper = new ContributionItemsFactory(this.application,
                this.modelService, this.bindingService);
        final ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.TRAIL);
        addStyleControls(this.styledContainer, toolBarManager, styleBarHelper, listener);

        final StyledTextComponent styled = StyledTextComponent.createStyledText(this.styledContainer, this.context);
        styled.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent event) {
                enableStylesMenu(true);
            }

            @Override
            public void focusLost(final FocusEvent event) {
                enableStylesMenu(false);
            }
        });
        styled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        styled.setFormStyle(true);
        hookContextMenu(styled, styleBarHelper);

        listener.unsetKeyBinding((StyledText) styled.getControl());
        styled.getControl().addListener(SWT.KeyDown, listener);
        return styled;
    }

    private void hookContextMenu(final StyledTextComponent styledText, final ContributionItemsFactory menuHelper) {
        final MenuManager menuManager = new MenuManager("#PopupMenuST"); //$NON-NLS-1$
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(manager -> {
            manager.add(new Separator("style")); //$NON-NLS-1$
            for (final StyleContributionItem item : menuHelper.getItems()) {
                manager.add(item);
            }
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        });
        styledText.setMenu(menuManager.createContextMenu(styledText.getControl()));
    }

    private void addStyleControls(final Composite parent, final ToolBarManager manager,
            final ContributionItemsFactory toolBarHelper, final STKeyListener stListener) {
        final ToolBar lToolBar = manager.createControl(parent);

        final ResizeFontContributionItem resizeItem = ContextInjectionFactory.make(ResizeFontContributionItem.class,
                this.context);
        manager.add(resizeItem);
        resizeItem.fill(lToolBar, 0);

        for (final StyleContributionItem item : toolBarHelper.getItems()) {
            manager.add(item);
            item.fill(lToolBar, stListener);
        }
    }

    private void enableStylesMenu(final boolean isEnabled) {
        this.eventBroker.post(RelationsConstants.TOPIC_STYLE_ITEMS_FORM, isEnabled ? Boolean.TRUE : Boolean.FALSE);
    }

    private void setDefaultSize(final GridData data) {
        if (data.widthHint == -1) {
            data.widthHint = DEFAULT_WIDTH;
        }
        if (data.heightHint == -1) {
            data.heightHint = DEFAULT_HEIGHT;
        }
    }

    protected Font createBoldFont() {
        final FontData[] boldData = JFaceResources.getBannerFont().getFontData();
        for (int i = 0; i < boldData.length; i++) {
            boldData[i].setHeight(boldData[i].getHeight() - 2);
        }
        return new Font(Display.getCurrent(), boldData);
    }

    protected Composite createComposite(final Composite parent, final int numColumns, final int verticalSpacing) {
        final GridLayout layout = new GridLayout();
        layout.numColumns = numColumns;
        layout.verticalSpacing = verticalSpacing;
        return createComposite(parent, layout);
    }

    protected Composite createComposite(final Composite parent, final int numColumns) {
        final GridLayout layout = new GridLayout();
        layout.numColumns = numColumns;
        return createComposite(parent, layout);
    }

    private Composite createComposite(final Composite parent, final GridLayout layout) {
        final Composite created = new Composite(parent, SWT.NULL);
        created.setLayout(layout);
        return created;
    }

    /** @see org.elbe.relations.wizards.IEditForm#setHeight(int) */
    public void setHeight(final int height) {
        ((GridData) this.styledContainer.getLayoutData()).heightHint = height;
    }

    protected void setIndent(final int indent) {
        ((GridData) this.styledContainer.getLayoutData()).horizontalIndent = indent;
    }

    protected void setWidth(final Control control, final int width) {
        ((GridData) control.getLayoutData()).widthHint = width;
    }

    protected int convertWidthInCharsToPixels(final Control testControl, final int charsNumber) {
        if (this.fontMetrics == null) {
            final GC gc = new GC(testControl);
            gc.setFont(JFaceResources.getDialogFont());
            this.fontMetrics = gc.getFontMetrics();
            gc.dispose();
        }
        return Dialog.convertWidthInCharsToPixels(this.fontMetrics, charsNumber);
    }

    /** Returns the form's dirty status.
     *
     * @return boolean <code>true</code> if at least on widget on the form is dirty. */
    public boolean getDirty() {
        return this.checkDirtyService.isDirty();
    }

    /** Signals if the page can be completed.
     *
     * @return boolean <code>true</code> if the page is complete. */
    public abstract boolean getPageComplete();

    /** @return {@link Control} */
    public Control getControl() {
        return this.container;
    }

    /** Disposes of the operating system resources associated with the receiver and all its descendants. */
    public void dispose() {
        this.checkDirtyService.dispose();
        if (this.styledText != null && !this.styledText.isDisposed()) {
            this.styledText.dispose();
            this.styledText = null;
        }
        if (this.styledContainer != null && !this.styledContainer.isDisposed()) {
            this.styledContainer.dispose();
            this.styledContainer = null;
        }
        this.container.dispose();
    }

    /** Notifies the user about this form's dirty status.
     *
     * @param isDirty boolean <code>true</code> if at least one widget on this form is dirty. */
    public void notifyDirtySwitch(final boolean isDirty) {
        if (isDirty) {
            getControl().getShell().setText("*" + getViewTitle()); //$NON-NLS-1$
        } else {
            getControl().getShell().setText(getViewTitle());
        }
    }

    private String getViewTitle() {
        if (this.viewTitle.isEmpty()) {
            this.viewTitle = getControl().getShell().getText();
        }
        return this.viewTitle;
    }

    protected IStatus createErrorStatus(final String message) {
        return new Status(IStatus.ERROR, Activator.getSymbolicName(), 1, message, null);
    }

    protected void addCreatedLabel(final Composite parent, final int indent, final int colspan) {
        if (this.editMode) {
            this.labelCreated = new Label(parent, SWT.NONE);
            final GridData layout = new GridData(SWT.FILL, SWT.NULL, true, false, colspan, SWT.NULL);
            layout.horizontalIndent = indent;
            this.labelCreated.setLayoutData(layout);
        }
    }

    protected void setCreatedInfo(final String created) {
        if (this.editMode) {
            this.labelCreated.setText(created);
        }
    }

    // ---

    /** Private class to create the contribution items for the form's style bar and the styled text's context menu. The
     * data for the contribution items is extracted from the application's style bar definition (see
     * <code>MUI_ID_STYLING_TOOLBAR</code>). */
    private static class ContributionItemsFactory {
        private final List<StyleContributionItem> items;

        ContributionItemsFactory(final MApplication application, final EModelService modelService,
                final EBindingService bindingService) {
            this.items = new ArrayList<>();

            final IEclipseContext context = application.getContext();
            final CommandManager commandManager = context.get(CommandManager.class);
            final ISWTResourceUtilities resourceUtility = (ISWTResourceUtilities) context
                    .get(IResourceUtilities.class.getName());
            final EHandlerService handlerService = context.get(EHandlerService.class);

            final MToolBar toolbar = (MToolBar) modelService.find(MUI_ID_STYLING_TOOLBAR, application);
            for (final MToolBarElement element : toolbar.getChildren()) {
                if (element instanceof final MHandledToolItem toolItem) {
                    final StyleContributionItem item = new StyleContributionItem(toolItem, resourceUtility,
                            bindingService, commandManager, handlerService);
                    ContextInjectionFactory.inject(item, context);
                    this.items.add(item);
                }
            }
        }

        List<StyleContributionItem> getItems() {
            return this.items;
        }
    }

    /** A special contribution item for the form's style bar and the styled text's context/popup menu. */
    private static class StyleContributionItem extends ContributionItem {
        private final ImageDescriptor icon;
        private final String tooltip;
        private ToolItem widgetToolBar;
        private MenuItem widgetMenu;
        private final ParameterizedCommand command;
        private final TriggerSequence sequence;
        private Listener menuItemListener;
        private final EHandlerService handlerService;
        private final Style style;

        @Inject
        @Optional
        private IEclipseContext context;

        protected StyleContributionItem(final MHandledToolItem inElement, final ISWTResourceUtilities inUtility,
                final EBindingService inBindingService, final CommandManager inCommandManager,
                final EHandlerService inHandlerService) {
            super(inElement.getElementId());
            this.handlerService = inHandlerService;
            this.icon = inUtility.imageDescriptorFromURI(URI.createURI(inElement.getIconURI()));
            this.tooltip = inElement.getLocalizedTooltip();
            this.command = createCommand(inElement.getCommand(), inCommandManager);
            this.sequence = inBindingService.getBestSequenceFor(this.command);
            this.style = Styles.getStyle(inElement.getCommand().getElementId());
        }

        void fill(final ToolBar inParent, final STKeyListener inSTListener) {
            if (this.command == null) {
                return;
            }
            if (this.widgetToolBar != null || inParent == null) {
                return;
            }
            this.widgetToolBar = new ToolItem(inParent, SWT.CHECK);
            this.widgetToolBar.setData(this);
            this.widgetToolBar.setImage(this.icon.createImage());
            this.widgetToolBar.addListener(SWT.Dispose, getItemListener());
            this.widgetToolBar.addListener(SWT.Selection, getItemListener());

            if (this.sequence == null) {
                this.widgetToolBar.setToolTipText(this.tooltip);
            } else {
                this.widgetToolBar.setToolTipText(String.format("%s (%s)", this.tooltip, //$NON-NLS-1$
                        this.sequence.format()));
            }
            this.widgetToolBar.setEnabled(false);
            inSTListener.registerItem(this);
        }

        TriggerSequence getSequence() {
            return this.sequence;
        }

        @Override
        public void fill(final Menu inParent, final int inIndex) {
            if (this.command == null) {
                return;
            }
            if (this.widgetMenu != null || inParent == null) {
                return;
            }
            this.widgetMenu = new MenuItem(inParent, SWT.CHECK);
            this.widgetMenu.setData(this);
            this.widgetMenu.setImage(this.icon.createImage());
            this.widgetMenu.addListener(SWT.Dispose, getItemListener());
            this.widgetMenu.addListener(SWT.Selection, getItemListener());
            if (this.widgetToolBar != null) {
                this.widgetMenu.setSelection(this.widgetToolBar.getSelection());
            }

            if (this.sequence == null) {
                this.widgetMenu.setText(this.tooltip);
            } else {
                this.widgetMenu.setText(this.tooltip + '\t' + this.sequence.format());
            }
        }

        private ParameterizedCommand createCommand(final MCommand inCommand,
                final CommandManager inCommandManager) {
            return new ParameterizedCommand(
                    inCommandManager.getCommand(inCommand.getElementId()),
                    null);
        }

        private Listener getItemListener() {
            if (this.menuItemListener == null) {
                this.menuItemListener = event -> {
                    switch (event.type) {
                        case SWT.Dispose:
                            handleWidgetDispose(event);
                            break;
                        case SWT.Selection:
                            if (event.widget != null) {
                                handleWidgetSelection(event);
                            }
                            break;
                    }
                };
            }
            return this.menuItemListener;
        }

        protected void handleWidgetSelection(final Event inEvent) {
            final Map<String, String> lParameters = new HashMap<>();
            if (inEvent.widget instanceof ToolItem) {
                // click triggered on tool bar item
                lParameters.put(RelationsConstants.PN_COMMAND_STYLE_SELECTION,
                        this.widgetToolBar.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                // click triggered on popup menu item
                lParameters.put(RelationsConstants.PN_COMMAND_STYLE_SELECTION,
                        this.widgetToolBar.getSelection() ? "false" : "true"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            this.handlerService.executeHandler(ParameterizedCommand
                    .generateCommand(adaptCommand(this.command), lParameters));
        }

        private Command adaptCommand(final ParameterizedCommand inCommand) {
            final Command out = inCommand.getCommand();
            try {
                if (out.getParameters() == null) {
                    out.define(out.getName(), out.getDescription(),
                            out.getCategory(),
                            new IParameter[] { new ToolBarParameter() });
                }
            } catch (final NotDefinedException exc) {
                // intentionally left empty
            }
            return out;
        }

        protected void handleWidgetDispose(final Event event) {
            if (event.widget == this.widgetMenu) {
                this.widgetMenu.removeListener(SWT.Selection, getItemListener());
                this.widgetMenu.removeListener(SWT.Dispose, getItemListener());
                this.widgetMenu.getImage().dispose();
                this.widgetMenu = null;
            }
            if (event.widget == this.widgetToolBar) {
                this.widgetToolBar.removeListener(SWT.Selection, getItemListener());
                this.widgetToolBar.removeListener(SWT.Dispose, getItemListener());
                this.widgetToolBar.getImage().dispose();
                this.widgetToolBar = null;
                ContextInjectionFactory.uninject(this, this.context);
            }
        }

        @Inject
        @Optional
        public void updateEnablement(
                @UIEventTopic(RelationsConstants.TOPIC_STYLE_ITEMS_FORM) final boolean enable) {
            if (this.widgetToolBar != null) {
                this.widgetToolBar.setEnabled(enable);
                if (enable) {
                    this.context.set(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE, "active"); //$NON-NLS-1$
                }
            }
            if (!enable && this.context != null) {
                this.context.remove(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE);
            }
        }

        @Inject
        @Optional
        public void updateToggleState(
                @UIEventTopic(RelationsConstants.TOPIC_STYLE_CHANGED_FORM) final StyleParameter styleParameter) {
            final Boolean selected = styleParameter.getIsToggeled(this.style);
            if (this.widgetToolBar != null) {
                this.widgetToolBar.setSelection(selected);
            }
            if (this.widgetMenu != null) {
                this.widgetMenu.setSelection(selected);
            }
        }

        boolean getSelection() {
            return this.widgetToolBar.getSelection();
        }

        Command getCommand() {
            return this.command.getCommand();
        }
    }

    /** The contribution item to change the font size in the styled text field. This item delegates to
     * <code>ResizeFontControl</code>. */
    private static class ResizeFontContributionItem extends ContributionItem {

        private final IEclipseContext context;

        @Inject
        public ResizeFontContributionItem(final IEclipseContext context) {
            this.context = context;
        }

        @Override
        public void fill(final ToolBar parent, final int index) {
            final ToolItem toolItem = new ToolItem(parent, SWT.SEPARATOR, index);
            this.context.set(Composite.class, parent);
            final ResizeFontControl control = ContextInjectionFactory.make(ResizeFontControl.class, this.context);
            toolItem.setWidth(control.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
            toolItem.setControl(control.getControl());
        }
    }

    /** Helper class to create a label - text widget.
     *
     * @author Luthiger */
    protected class WidgetCreator {
        private final Label label;
        private final Text text;

        public WidgetCreator(final String inLabelValue, final Composite inContainer, final int inNumColumns) {
            this.label = createLabel(inLabelValue, inContainer);
            this.text = createText(inContainer, inNumColumns - 1);
        }

        public Label getLabel() {
            return this.label;
        }

        public Text getText() {
            return this.text;
        }
    }

    /** Helper class to create a label - styled text widget.
     *
     * @author Luthiger */
    protected class StyledTextCreator {
        private final Label label;
        private final StyledText text;

        public StyledTextCreator(final String inLabelValue,
                final Composite inContainer, final int inNumColumns) {
            this.label = createLabel(inLabelValue, inContainer);
            this.text = new StyledText(inContainer, SWT.BORDER | SWT.SINGLE);
            this.text.setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false,
                    inNumColumns - 1, SWT.NULL));
        }

        public Label getLabel() {
            return this.label;
        }

        public StyledText getText() {
            return this.text;
        }
    }

    /** Listener to process key binding.
     *
     * @author Luthiger */
    private static class STKeyListener implements Listener {
        private KeySequence state = KeySequence.getInstance();

        private final Map<TriggerSequence, StyleContributionItem> commands = new HashMap<>();
        private final EHandlerService handlerService;

        STKeyListener(final MApplication application) {
            this.handlerService = application.getContext().get(EHandlerService.class);
        }

        /** @param inToolItem {@link StyleContributionItem} */
        void registerItem(final StyleContributionItem inToolItem) {
            this.commands.put(inToolItem.getSequence(), inToolItem);
        }

        @Override
        public void handleEvent(final Event event) {
            /*
             * Only process key strokes containing natural keys to trigger key bindings.
             */
            if ((event.keyCode & SWT.MODIFIER_MASK) != 0) {
                return;
            }

            final List<KeyStroke> keyStrokes = KeyBindingDispatcher.generatePossibleKeyStrokes(event);
            if (keyStrokes.isEmpty()) {
                return;
            }

            if (processStrokes(keyStrokes)) {
                event.doit = false;
            }
            event.type = SWT.NONE;
        }

        private boolean processStrokes(final List<KeyStroke> strokes) {
            final KeySequence sequenceBeforeStroke = this.state;
            for (final KeyStroke keyStroke : strokes) {
                final KeySequence sequenceAfterStroke = KeySequence
                        .getInstance(sequenceBeforeStroke, keyStroke);
                final StyleContributionItem toolItem = this.commands
                        .get(sequenceAfterStroke);
                if (toolItem != null) {
                    final Map<String, String> parameters = new HashMap<>();
                    parameters.put(
                            RelationsConstants.PN_COMMAND_STYLE_SELECTION,
                            toolItem.getSelection() ? "false" : "true"); //$NON-NLS-1$ //$NON-NLS-2$
                    final ParameterizedCommand command = ParameterizedCommand.generateCommand(toolItem.getCommand(),
                            parameters);
                    if (command == null) {
                        return false;
                    } else {
                        this.handlerService.executeHandler(command);
                    }
                    this.state = KeySequence.getInstance();
                    return true;
                }
                this.state = sequenceAfterStroke;
            }
            this.state = KeySequence.getInstance();
            return !sequenceBeforeStroke.isEmpty();
        }

        void unsetKeyBinding(final StyledText text) {
            for (final TriggerSequence trigger : this.commands.keySet()) {
                text.setKeyBinding(getKeyCode((KeySequence) trigger), 1);
            }
        }

        private int getKeyCode(final KeySequence sequence) {
            int key = 0;
            if (sequence == null) {
                return key;
            }
            for (final KeyStroke lStroke : sequence.getKeyStrokes()) {
                key |= lStroke.getModifierKeys() | lStroke.getNaturalKey();
            }
            return key;
        }
    }

    private static class ToolBarParameter implements IParameter {

        @Override
        public String getId() {
            return RelationsConstants.PN_COMMAND_STYLE_SELECTION;
        }

        @Override
        public String getName() {
            return "style.parameter"; //$NON-NLS-1$
        }

        @Override
        public IParameterValues getValues() throws ParameterValuesException {
            return null;
        }

        @Override
        public boolean isOptional() {
            return true;
        }
    }

}
