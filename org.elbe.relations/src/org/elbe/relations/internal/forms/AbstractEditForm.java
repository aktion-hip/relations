/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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

import javax.inject.Inject;

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
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
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

/**
 * Base class for the edit forms to edit or create items, i.e. term, text or
 * person items.
 *
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public abstract class AbstractEditForm {
    private static final String MUI_ID_STYLING_TOOLBAR = "relations.toolbar:text.styling"; //$NON-NLS-1$

    private final static int DEFAULT_WIDTH = 100;
    private final static int DEFAULT_HEIGHT = 80;

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

    /**
     * Subclasses have to call immediately after object creation.
     *
     * @param inEditMode
     *            boolean <code>true</code> if this form is in edit mode,
     *            <code>false</code> for new mode
     */
    protected void setEditMode(final boolean inEditMode) {
        this.editMode = inEditMode;
        init(inEditMode);
    }

    private void init(final boolean inEditMode) {
        if (inEditMode) {
            this.checkDirtyService = new CheckDirtyService(this);
        } else {
            this.checkDirtyService = new CheckDirtyServiceNoop();
        }
    }

    abstract public void initialize();

    /**
     * Notifies the observers.
     *
     * @param inStatuses
     *            IStatus[]
     */
    protected void notifyAboutUpdate(final IStatus[] inStatuses) {
        final MultiStatus lMulti = new MultiStatus(Activator.getSymbolicName(),
                1, inStatuses, "", null); //$NON-NLS-1$
        this.eventBroker.post(RelationsConstants.TOPIC_WIZARD_PAGE_STATUS, lMulti);
    }

    /**
     * Subclasses must implement.
     *
     * @return IStatus[] Array of status information.
     */
    protected abstract IStatus[] getStatuses();

    protected Label createLabel(final String inLabelValue,
            final Composite inContainer) {
        return createLabel(inLabelValue, inContainer, 1);
    }

    protected Label createLabel(final String inLabelValue,
            final Composite inContainer, final int inNumColumns) {
        final Label outLabel = new Label(inContainer, SWT.NULL);
        outLabel.setText(inLabelValue);

        final GridData lData = new GridData(SWT.FILL, SWT.NULL, false, false,
                inNumColumns, SWT.NULL);
        lData.widthHint = (int) (outLabel.computeSize(SWT.DEFAULT,
                SWT.DEFAULT).x * 1.2);
        outLabel.setLayoutData(lData);
        return outLabel;
    }

    protected Text createText(final Composite inContainer,
            final int inNumColumns) {
        final Text outText = createText(inContainer);
        outText.setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false,
                inNumColumns, SWT.NULL));
        return outText;
    }

    protected Text createText(final Composite inContainer) {
        final Text outText = new Text(inContainer, SWT.BORDER | SWT.SINGLE);
        return outText;
    }

    protected StyledTextComponent createStyledText(
            final Composite inContainer) {
        return createStyledText(inContainer,
                new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    protected StyledTextComponent createStyledText(final Composite inContainer,
            final int inNumColumns, final int inHeight) {
        final GridData lData = new GridData(SWT.FILL, SWT.FILL, true, true,
                inNumColumns, SWT.NULL);
        lData.heightHint = inHeight;
        return createStyledText(inContainer, lData);
    }

    /**
     * Creates <code>StyledTextComponent</code> for the edit form. The widget is
     * complete with a toolbar above displaying the style controls and a popup
     * menu (displaying the style controls too).
     *
     * @param inContainer
     *            Composite parent widget
     * @param inData
     *            GridData layout data
     * @return StyledTextComponent
     */
    private StyledTextComponent createStyledText(final Composite inContainer,
            final GridData inData) {
        this.styledContainer = new Composite(inContainer, SWT.NONE);
        final GridLayout lLayout = new GridLayout(1, true);
        lLayout.marginWidth = 0;
        lLayout.marginTop = 0;
        lLayout.verticalSpacing = 2;
        this.styledContainer.setLayout(lLayout);
        setDefaultSize(inData);
        this.styledContainer.setLayoutData(inData);

        final STKeyListener lSTListener = new STKeyListener(this.application);
        final ContributionItemsFactory lStyleBarHelper = new ContributionItemsFactory(
                this.application, this.modelService, this.bindingService);
        final ToolBarManager lToolBarManager = new ToolBarManager(
                SWT.FLAT | SWT.TRAIL);
        addStyleControls(this.styledContainer, lToolBarManager, lStyleBarHelper,
                lSTListener);

        final StyledTextComponent outStyled = StyledTextComponent
                .createStyledText(this.styledContainer, this.context);
        outStyled.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent inEvent) {
                enableStylesMenu(true);
            }

            @Override
            public void focusLost(final FocusEvent inEvent) {
                enableStylesMenu(false);
            }
        });
        outStyled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        outStyled.setFormStyle(true);
        hookContextMenu(outStyled, lStyleBarHelper);

        lSTListener.unsetKeyBinding((StyledText) outStyled.getControl());
        outStyled.getControl().addListener(SWT.KeyDown, lSTListener);
        return outStyled;
    }

    private void hookContextMenu(final StyledTextComponent inStyledText,
            final ContributionItemsFactory inMenuHelper) {
        final MenuManager lMenuManager = new MenuManager("#PopupMenuST"); //$NON-NLS-1$
        lMenuManager.setRemoveAllWhenShown(true);
        lMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final IMenuManager inManager) {
                inManager.add(new Separator("style")); //$NON-NLS-1$
                for (final StyleContributionItem lItem : inMenuHelper
                        .getItems()) {
                    inManager.add(lItem);
                }
                inManager.add(
                        new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        inStyledText.setMenu(
                lMenuManager.createContextMenu(inStyledText.getControl()));

    }

    private void addStyleControls(final Composite inParent,
            final ToolBarManager inManager,
            final ContributionItemsFactory inToolBarHelper,
            final STKeyListener inSTListener) {
        final ToolBar lToolBar = inManager.createControl(inParent);

        final ResizeFontContributionItem lResizeItem = new ResizeFontContributionItem();
        ContextInjectionFactory.inject(lResizeItem, this.context);
        inManager.add(lResizeItem);
        lResizeItem.fill(lToolBar, 0);

        for (final StyleContributionItem lItem : inToolBarHelper.getItems()) {
            inManager.add(lItem);
            lItem.fill(lToolBar, inSTListener);
        }
    }

    private void enableStylesMenu(final boolean inIsEnabled) {
        this.eventBroker.post(RelationsConstants.TOPIC_STYLE_ITEMS_FORM,
                inIsEnabled ? Boolean.TRUE : Boolean.FALSE);
    }

    private void setDefaultSize(final GridData inData) {
        if (inData.widthHint == -1) {
            inData.widthHint = DEFAULT_WIDTH;
        }
        if (inData.heightHint == -1) {
            inData.heightHint = DEFAULT_HEIGHT;
        }
    }

    protected Font createBoldFont() {
        final FontData[] lBoldData = JFaceResources.getBannerFont()
                .getFontData();
        for (int i = 0; i < lBoldData.length; i++) {
            lBoldData[i].setHeight(lBoldData[i].getHeight() - 2);
        }
        return new Font(Display.getCurrent(), lBoldData);
    }

    protected Composite createComposite(final Composite inParent,
            final int inNumColumns, final int inVerticalSpacing) {
        final GridLayout lLayout = new GridLayout();
        lLayout.numColumns = inNumColumns;
        lLayout.verticalSpacing = inVerticalSpacing;
        return createComposite(inParent, lLayout);
    }

    protected Composite createComposite(final Composite inParent,
            final int inNumColumns) {
        final GridLayout lLayout = new GridLayout();
        lLayout.numColumns = inNumColumns;
        return createComposite(inParent, lLayout);
    }

    private Composite createComposite(final Composite inParent,
            final GridLayout inLayout) {
        final Composite outContainer = new Composite(inParent, SWT.NULL);
        outContainer.setLayout(inLayout);
        return outContainer;
    }

    /**
     * @see org.elbe.relations.wizards.IEditForm#setHeight(int)
     */
    public void setHeight(final int inHeight) {
        ((GridData) this.styledContainer.getLayoutData()).heightHint = inHeight;
    }

    protected void setIndent(final int inIndent) {
        ((GridData) this.styledContainer
                .getLayoutData()).horizontalIndent = inIndent;
    }

    protected void setWidth(final Control inControl, final int inWidth) {
        ((GridData) inControl.getLayoutData()).widthHint = inWidth;
    }

    protected int convertWidthInCharsToPixels(final Control inTestControl,
            final int inCharsNumber) {
        if (this.fontMetrics == null) {
            final GC lGC = new GC(inTestControl);
            lGC.setFont(JFaceResources.getDialogFont());
            this.fontMetrics = lGC.getFontMetrics();
            lGC.dispose();
        }
        return Dialog.convertWidthInCharsToPixels(this.fontMetrics, inCharsNumber);
    }

    /**
     * Returns the form's dirty status.
     *
     * @return boolean <code>true</code> if at least on widget on the form is
     *         dirty.
     */
    public boolean getDirty() {
        return this.checkDirtyService.isDirty();
    }

    /**
     * Signals if the page can be completed.
     *
     * @return boolean <code>true</code> if the page is complete.
     */
    public abstract boolean getPageComplete();

    /**
     * @return {@link Control}
     */
    public Control getControl() {
        return this.container;
    }

    /**
     * Disposes of the operating system resources associated with the receiver
     * and all its descendants.
     */
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

    /**
     * Notifies the user about this form's dirty status.
     *
     * @param inIsDirty
     *            boolean <code>true</code> if at least one widget on this form
     *            is dirty.
     */
    public void notifyDirtySwitch(final boolean inIsDirty) {
        if (inIsDirty) {
            getControl().getShell().setText("*" + getViewTitle()); //$NON-NLS-1$
        } else {
            getControl().getShell().setText(getViewTitle());
        }
    }

    private String getViewTitle() {
        if (this.viewTitle.length() == 0) {
            this.viewTitle = getControl().getShell().getText();
        }
        return this.viewTitle;
    }

    protected IStatus createErrorStatus(final String inMsg) {
        return new Status(IStatus.ERROR, Activator.getSymbolicName(), 1, inMsg,
                null);
    }

    protected void addCreatedLabel(final Composite inParent, final int inIndent,
            final int inColspan) {
        if (this.editMode) {
            this.labelCreated = new Label(inParent, SWT.NONE);
            final GridData lLayout = new GridData(SWT.FILL, SWT.NULL, true,
                    false, inColspan, SWT.NULL);
            lLayout.horizontalIndent = inIndent;
            this.labelCreated.setLayoutData(lLayout);
        }
    }

    protected void setCreatedInfo(final String inCreated) {
        if (this.editMode) {
            this.labelCreated.setText(inCreated);
        }
    }

    // ---

    /**
     * Private class to create the contribution items for the form's style bar
     * and the styled text's context menu. The data for the contribution items
     * is extracted from the application's style bar definition (see
     * <code>MUI_ID_STYLING_TOOLBAR</code>).
     */
    private static class ContributionItemsFactory {
        private final List<StyleContributionItem> items;

        ContributionItemsFactory(final MApplication inApplication,
                final EModelService inModelService,
                final EBindingService inBindingService) {
            this.items = new ArrayList<StyleContributionItem>();

            final IEclipseContext lContext = inApplication.getContext();
            final CommandManager lCommandManager = lContext
                    .get(CommandManager.class);
            final ISWTResourceUtilities lResourceUtility = (ISWTResourceUtilities) lContext
                    .get(IResourceUtilities.class.getName());
            final EHandlerService lHandlerService = lContext
                    .get(EHandlerService.class);

            final MToolBar lToolbar = (MToolBar) inModelService
                    .find(MUI_ID_STYLING_TOOLBAR, inApplication);
            for (final MToolBarElement lElement : lToolbar.getChildren()) {
                if (lElement instanceof MHandledToolItem) {
                    final StyleContributionItem lItem = new StyleContributionItem(
                            (MHandledToolItem) lElement, lResourceUtility,
                            inBindingService, lCommandManager, lHandlerService);
                    ContextInjectionFactory.inject(lItem, lContext);
                    this.items.add(lItem);
                }
            }
        }

        List<StyleContributionItem> getItems() {
            return this.items;
        }
    }

    /**
     * A special contribution item for the form's style bar and the styled
     * text's context/popup menu.
     */
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

        protected StyleContributionItem(final MHandledToolItem inElement,
                final ISWTResourceUtilities inUtility,
                final EBindingService inBindingService,
                final CommandManager inCommandManager,
                final EHandlerService inHandlerService) {
            super(inElement.getElementId());
            this.handlerService = inHandlerService;
            this.icon = inUtility.imageDescriptorFromURI(
                    URI.createURI(inElement.getIconURI()));
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
                this.menuItemListener = new Listener() {
                    @Override
                    public void handleEvent(final Event inEvent) {
                        switch (inEvent.type) {
                            case SWT.Dispose:
                                handleWidgetDispose(inEvent);
                                break;
                            case SWT.Selection:
                                if (inEvent.widget != null) {
                                    handleWidgetSelection(inEvent);
                                }
                                break;
                        }
                    }
                };
            }
            return this.menuItemListener;
        }

        protected void handleWidgetSelection(final Event inEvent) {
            final Map<String, String> lParameters = new HashMap<String, String>();
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
            }
            catch (final NotDefinedException exc) {
                // intentionally left empty
            }
            return out;
        }

        protected void handleWidgetDispose(final Event inEvent) {
            if (inEvent.widget == this.widgetMenu) {
                this.widgetMenu.removeListener(SWT.Selection, getItemListener());
                this.widgetMenu.removeListener(SWT.Dispose, getItemListener());
                this.widgetMenu.getImage().dispose();
                this.widgetMenu = null;
            }
            if (inEvent.widget == this.widgetToolBar) {
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
                @UIEventTopic(RelationsConstants.TOPIC_STYLE_ITEMS_FORM) final Boolean inEnable) {
            if (this.widgetToolBar != null) {
                this.widgetToolBar.setEnabled(inEnable.booleanValue());
                if (inEnable) {
                    this.context.set(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE,
                            "active"); //$NON-NLS-1$
                }
            }
            if (!inEnable && this.context != null) {
                this.context.remove(RelationsConstants.FLAG_STYLED_TEXT_ACTIVE);
            }
        }

        @Inject
        @Optional
        public void updateToggleState(
                @UIEventTopic(RelationsConstants.TOPIC_STYLE_CHANGED_FORM) final StyleParameter inStyleParameter) {
            final Boolean lSelected = inStyleParameter.getIsToggeled(this.style);
            if (this.widgetToolBar != null) {
                this.widgetToolBar.setSelection(lSelected);
            }
            if (this.widgetMenu != null) {
                this.widgetMenu.setSelection(lSelected);
            }
        }

        boolean getSelection() {
            return this.widgetToolBar.getSelection();
        }

        Command getCommand() {
            return this.command.getCommand();
        }
    }

    /**
     * The contribution item to change the font size in the styled text field.
     * This item delegates to <code>ResizeFontControl</code>.
     */
    private static class ResizeFontContributionItem extends ContributionItem {

        @Inject
        private IEclipseContext context;

        private ToolItem toolItem;

        @Override
        public void fill(final ToolBar inParent, final int inIndex) {
            this.toolItem = new ToolItem(inParent, SWT.SEPARATOR, inIndex);
            this.context.set(Composite.class, inParent);
            final ResizeFontControl control = ContextInjectionFactory
                    .make(ResizeFontControl.class, this.context);
            control.createWidget(inParent);
            this.toolItem.setWidth(control.getControl().computeSize(SWT.DEFAULT,
                    SWT.DEFAULT, true).x);
            this.toolItem.setControl(control.getControl());
        }
    }

    /**
     * Helper class to create a label - text widget.
     *
     * @author Luthiger
     */
    protected class WidgetCreator {
        private final Label label;
        private final Text text;

        public WidgetCreator(final String inLabelValue,
                final Composite inContainer, final int inNumColumns) {
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

    /**
     * Helper class to create a label - styled text widget.
     *
     * @author Luthiger
     */
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

    /**
     * Listener to process key binding.
     *
     * @author Luthiger
     */
    private static class STKeyListener implements Listener {
        private KeySequence state = KeySequence.getInstance();

        private final Map<TriggerSequence, StyleContributionItem> commands = new HashMap<TriggerSequence, StyleContributionItem>();
        private final EHandlerService handlerService;

        STKeyListener(final MApplication inApplication) {
            this.handlerService = inApplication.getContext()
                    .get(EHandlerService.class);
        }

        /**
         * @param inToolItem
         *            {@link StyleContributionItem}
         */
        void registerItem(final StyleContributionItem inToolItem) {
            this.commands.put(inToolItem.getSequence(), inToolItem);
        }

        @Override
        public void handleEvent(final Event inEvent) {
            /*
             * Only process key strokes containing natural keys to trigger key
             * bindings.
             */
            if ((inEvent.keyCode & SWT.MODIFIER_MASK) != 0) {
                return;
            }

            final List<KeyStroke> lKeyStrokes = KeyBindingDispatcher
                    .generatePossibleKeyStrokes(inEvent);
            if (lKeyStrokes.isEmpty()) {
                return;
            }

            if (processStrokes(lKeyStrokes)) {
                inEvent.doit = false;
            }
            inEvent.type = SWT.NONE;
        }

        private boolean processStrokes(final List<KeyStroke> strokes) {
            final KeySequence sequenceBeforeStroke = this.state;
            for (final KeyStroke keyStroke : strokes) {
                final KeySequence sequenceAfterStroke = KeySequence
                        .getInstance(sequenceBeforeStroke, keyStroke);
                final StyleContributionItem toolItem = this.commands
                        .get(sequenceAfterStroke);
                if (toolItem != null) {
                    final Map<String, String> parameters = new HashMap<String, String>();
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

        void unsetKeyBinding(final StyledText inText) {
            for (final TriggerSequence lTrigger : this.commands.keySet()) {
                inText.setKeyBinding(getKeyCode((KeySequence) lTrigger), 1);
            }
        }

        private int getKeyCode(final KeySequence inSequence) {
            int out = 0;
            if (inSequence == null) {
                return out;
            }
            for (final KeyStroke lStroke : inSequence.getKeyStrokes()) {
                out |= lStroke.getModifierKeys() | lStroke.getNaturalKey();
            }
            return out;
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
