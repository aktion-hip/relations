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
package org.elbe.relations.internal.forms;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.elbe.relations.ICommandIds;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.dnd.ItemTransfer;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.internal.utility.ActionHelper;
import org.elbe.relations.models.IAssociationsModel;
import org.elbe.relations.models.ILightWeightModel;
import org.elbe.relations.models.ItemAdapter;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/**
 * Input form to create and modify an item's associated items.
 *
 * @author Benno Luthiger Created on 01.05.2006
 */
@SuppressWarnings("restriction")
public final class FormAssociate extends AbstractEditForm {
    private IAssociationsModel model;

    private TableViewer relatedViewer;
    private TableViewer selectionViewer;

    private Button toRelated;
    private Button toSelection;

    private Control dragSourceControl = null;

    private IAction actionAssociate;
    private IAction actionDissolve;

    @Inject
    private LanguageService languageService;

    @Inject
    private Logger log;

    @Inject
    private IDataService dataService;

    /**
     * Factory method to create instances of <code>FormAssociate</code>.
     *
     * @param inParent
     *            {@link Composite}
     * @param inContext
     *            {@link IEclipseContext}
     * @return {@link FormAssociate}
     */
    public static FormAssociate createFormAssociate(final Composite inParent,
            final IEclipseContext inContext) {
        final FormAssociate out = ContextInjectionFactory.make(
                FormAssociate.class, inContext);
        out.setEditMode(false);
        out.initialize(inParent);
        return out;
    }

    /**
     * Factory method to create instances of <code>FormAssociate</code>.
     *
     * @param inParent
     *            {@link Composite}
     * @param inModel
     *            {@link IAssociationsModel}
     * @param inContext
     *            {@link IEclipseContext}
     * @return {@link FormAssociate}
     */
    public static FormAssociate createFormAssociate(final Composite inParent,
            final IAssociationsModel inModel, final IEclipseContext inContext) {
        final FormAssociate out = createFormAssociate(inParent, inContext);
        out.loadModel(inModel);
        return out;
    }

    private void initialize(final Composite inParent) {
        this.container = createComposite(inParent, 3, 7);

        // first column
        final Composite lRelatedFill = createTableContainer(this.container);
        createLabel(
                RelationsMessages.getString("FormAssociate.list.related"), lRelatedFill); //$NON-NLS-1$
        this.relatedViewer = new TableViewer(lRelatedFill, SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.BORDER);
        this.relatedViewer.getTable().setLayoutData(createListLayoutData());
        this.relatedViewer.setContentProvider(createRelatedContentProvider());
        this.relatedViewer.setLabelProvider(createRelatedLabelProvider());
        this.relatedViewer.setComparator(new ViewerComparator(this.languageService.getContentLanguage()));

        // second column
        createArrowButtons(this.container);

        // third column
        final Composite lSelectionFill = createTableContainer(this.container);
        createLabel(
                RelationsMessages.getString("FormAssociate.list.selection"), lSelectionFill); //$NON-NLS-1$
        this.selectionViewer = new TableViewer(lSelectionFill, SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        this.selectionViewer.getTable().setLayoutData(createListLayoutData());
        this.selectionViewer.setContentProvider(createContentProvider());
        this.selectionViewer.setLabelProvider(createLabelProvider());
        this.selectionViewer.setComparator(new ViewerComparator(this.languageService.getContentLanguage()));

        createMenusAndToolbars();
    }

    public void loadModel(final IAssociationsModel inModel) {
        this.model = inModel;

        try {
            this.relatedViewer.setInput(this.model);
        }
        catch (final Exception exc) {
            this.log.error(exc, exc.getMessage());
        }
        this.selectionViewer.addFilter(new RelationsFilter());
    }

    private void createMenusAndToolbars() {
        makeActions();
        hookContextMenu();
        hookDragnDrop();
        hookDoubleClickAction();
    }

    private void hookDoubleClickAction() {
        this.selectionViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(final DoubleClickEvent inEvent) {
                FormAssociate.this.actionAssociate.run();
            }
        });
        this.relatedViewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(final DoubleClickEvent inEvent) {
                FormAssociate.this.actionDissolve.run();
            }
        });
    }

    private void makeActions() {
        this.actionAssociate = new Action(
                RelationsMessages.getString("FormAssociate.action.associate")) { //$NON-NLS-1$
            @Override
            public void run() {
                final IStructuredSelection lSelected = (IStructuredSelection) FormAssociate.this.selectionViewer
                        .getSelection();
                if (!lSelected.isEmpty()) {
                    FormAssociate.this.model.addAssociations(lSelected.toArray());
                    FormAssociate.this.relatedViewer.refresh(false);
                    FormAssociate.this.selectionViewer.refresh(false);
                }
            }
        };
        ActionHelper
        .initializeAction(
                this.actionAssociate,
                "associateItemsActionID", RelationsMessages.getString("FormAssociate.action.assiciate.msg"), ICommandIds.CMD_ASSOCIATIONS_ADD, null); //$NON-NLS-1$ //$NON-NLS-2$
        this.actionAssociate.setEnabled(true);

        this.actionDissolve = new Action(
                RelationsMessages.getString("FormAssociate.action.dissolve")) { //$NON-NLS-1$
            @Override
            public void run() {
                final IStructuredSelection lSelected = (IStructuredSelection) FormAssociate.this.relatedViewer
                        .getSelection();
                if (!lSelected.isEmpty()) {
                    FormAssociate.this.model.removeAssociations(lSelected.toArray());
                    FormAssociate.this.relatedViewer.refresh(false);
                    FormAssociate.this.selectionViewer.refresh(false);
                }
            }
        };
        ActionHelper
        .initializeAction(
                this.actionDissolve,
                "dissolveAssociationsActionID", RelationsMessages.getString("FormAssociate.action.dissolve.msg"), ICommandIds.CMD_ASSOCIATIONS_REMOVE, null); //$NON-NLS-1$ //$NON-NLS-2$
        this.actionDissolve.setEnabled(true);
    }

    private void hookContextMenu() {
        MenuManager lMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        lMenuManager.setRemoveAllWhenShown(true);
        lMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final IMenuManager inManager) {
                inManager.add(FormAssociate.this.actionAssociate);
                inManager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });

        Control lControl = this.selectionViewer.getControl();
        lControl.setMenu(lMenuManager.createContextMenu(lControl));

        lMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        lMenuManager.setRemoveAllWhenShown(true);
        lMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final IMenuManager inManager) {
                inManager.add(FormAssociate.this.actionDissolve);
                inManager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });

        lControl = this.relatedViewer.getControl();
        lControl.setMenu(lMenuManager.createContextMenu(lControl));
    }

    private void hookDragnDrop() {
        final ItemTransfer lItemTransfer = ItemTransfer.getInstance(this.log);

        addDropSupport(this.relatedViewer, lItemTransfer, new IAction1() {
            @Override
            public void run(final UniqueID[] inItems) {
                FormAssociate.this.model.addAssociations(inItems);
            }
        });
        addDragSupport(this.relatedViewer, lItemTransfer, new IAction2() {
            @Override
            public UniqueID[] createIDArray(final Object[] inItems) {
                try {
                    final UniqueID[] outIDs = new UniqueID[inItems.length];
                    for (int i = 0; i < inItems.length; i++) {
                        final ItemAdapter lItem = (ItemAdapter) inItems[i];
                        outIDs[i] = new UniqueID(lItem.getItemType(), lItem
                                .getID());
                    }
                    return outIDs;
                }
                catch (final VException exc) {
                    FormAssociate.this.log.error(exc, exc.getMessage());
                }
                return new UniqueID[0];
            }
        });

        addDragSupport(this.selectionViewer, lItemTransfer, new IAction2() {
            @Override
            public UniqueID[] createIDArray(final Object[] inItems) {
                final UniqueID[] outIDs = new UniqueID[inItems.length];
                for (int i = 0; i < inItems.length; i++) {
                    final ILightWeightItem lItem = (ILightWeightItem) inItems[i];
                    outIDs[i] = new UniqueID(lItem.getItemType(), lItem.getID());
                }
                return outIDs;
            }
        });
        addDropSupport(this.selectionViewer, lItemTransfer, new IAction1() {
            @Override
            public void run(final UniqueID[] inItems) {
                FormAssociate.this.model.removeAssociations(inItems);
            }
        });
    }

    private Composite createTableContainer(final Composite inParent) {
        final Composite outComposite = new Composite(inParent, SWT.NULL);
        final GridLayout lLayout = new GridLayout(1, false);
        lLayout.marginWidth = 0;
        outComposite.setLayout(lLayout);
        outComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                1, SWT.NULL));
        return outComposite;
    }

    private void addDropSupport(final TableViewer inViewer,
            final ItemTransfer inItemTransfer,
            final IAction1 inHandleAssociations) {

        inViewer.addDropSupport(DND.DROP_MOVE | DND.DROP_DEFAULT,
                new Transfer[] { inItemTransfer }, new DropTargetAdapter() {
            @Override
            public void dragEnter(final DropTargetEvent inEvent) {
                if (inViewer.getControl().equals(FormAssociate.this.dragSourceControl)) {
                    inEvent.detail = DND.DROP_NONE;
                    return;
                }

                if (inEvent.detail == DND.DROP_DEFAULT) {
                    if ((inEvent.operations & DND.DROP_MOVE) != 0) {
                        inEvent.detail = DND.DROP_MOVE;
                    } else {
                        inEvent.detail = DND.DROP_NONE;
                    }
                }
            }

            @Override
            public void drop(final DropTargetEvent inEvent) {
                if (inEvent.data != null) {
                    inHandleAssociations.run((UniqueID[]) inEvent.data);
                    FormAssociate.this.relatedViewer.refresh(false);
                    FormAssociate.this.selectionViewer.refresh(false);
                }
            }
        });
    }

    private void addDragSupport(final TableViewer inViewer,
            final ItemTransfer inItemTransfer, final IAction2 inHandleTransfer) {

        inViewer.addDragSupport(DND.DROP_MOVE | DND.DROP_DEFAULT,
                new Transfer[] { inItemTransfer }, new DragSourceAdapter() {
            @Override
            public void dragSetData(final DragSourceEvent inEvent) {
                final IStructuredSelection lSelected = (IStructuredSelection) inViewer
                        .getSelection();
                if (!lSelected.isEmpty()) {
                    inEvent.data = inHandleTransfer
                            .createIDArray(lSelected.toArray());
                }
            }

            @Override
            public void dragStart(final DragSourceEvent inEvent) {
                FormAssociate.this.dragSourceControl = inViewer.getControl();
                super.dragStart(inEvent);
            }
        });
    }

    /**
     * @see org.elbe.relations.forms.AbstractEditForm#initialize()
     */
    @Override
    public void initialize() {
        this.selectionViewer.setInput(this.dataService.getAll());
        this.selectionViewer.getTable().setFocus();
    }

    private IContentProvider createContentProvider() {
        return new IStructuredContentProvider() {
            @Override
            @SuppressWarnings("rawtypes")
            public Object[] getElements(final Object inElement) {
                if (inElement == null) {
                    return null;
                }
                return ((List) inElement).toArray();
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(final Viewer inViewer, final Object inOld,
                    final Object inNew) {
                // e.g. set listener to new input or remove listener from old
                // input.
            }
        };
    }

    private IContentProvider createRelatedContentProvider() {
        return new IStructuredContentProvider() {
            @Override
            public Object[] getElements(final Object inElement) {
                return inElement == null ? null
                        : ((IAssociationsModel) inElement).getElements();
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(final Viewer inViewer,
                    final Object inOldInput, final Object inNewInput) {
                // e.g. set listener to new input or remove listener from old
                // input.
            }
        };
    }

    private IBaseLabelProvider createLabelProvider() {
        return new LabelProvider() {
            @Override
            public Image getImage(final Object inElement) {
                return ((ILightWeightModel) inElement).getImage();
            }

            @Override
            public String getText(final Object inElement) {
                return inElement.toString();
            }
        };
    }

    private IBaseLabelProvider createRelatedLabelProvider() {
        return new LabelProvider() {
            @Override
            public Image getImage(final Object inElement) {
                return ((ItemAdapter) inElement).getImage();
            }

            @Override
            public String getText(final Object inElement) {
                try {
                    return ((ItemAdapter) inElement).getTitle();
                }
                catch (final VException exc) {
                    FormAssociate.this.log.error(exc, exc.getMessage());
                }
                return RelationsMessages.getString("FormAssociate.error"); //$NON-NLS-1$
            }
        };
    }

    private Composite createArrowButtons(final Composite inParent) {
        final Composite outArrowButtons = new Composite(inParent, SWT.NONE);
        final GridLayout lLayout = new GridLayout(1, true);
        outArrowButtons.setLayout(lLayout);
        this.toRelated = new Button(outArrowButtons, SWT.ARROW | SWT.LEFT);
        this.toRelated.setToolTipText(RelationsMessages
                .getString("FormAssociate.tool.associate")); //$NON-NLS-1$
        this.toRelated.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent inEvent) {
                FormAssociate.this.actionAssociate.run();
            }
        });

        this.toSelection = new Button(outArrowButtons, SWT.ARROW | SWT.RIGHT);
        this.toSelection.setToolTipText(RelationsMessages
                .getString("FormAssociate.tool.remove")); //$NON-NLS-1$
        this.toSelection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent inEvent) {
                FormAssociate.this.actionDissolve.run();
            }
        });
        return outArrowButtons;
    }

    private GridData createListLayoutData() {
        final GridData outData = new GridData(SWT.FILL, SWT.FILL, true, true);
        outData.heightHint = 300;
        outData.widthHint = 200;
        return outData;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.elbe.relations.wizards.AbstractEditForm#getStatuses()
     */
    @Override
    protected IStatus[] getStatuses() {
        return new IStatus[] {};
    }

    /**
     * Save all changes after OK is clicked.
     *
     * @throws BOMException
     */
    public void saveChanges() throws BOMException {
        this.model.saveChanges();
    }

    /**
     * Undo all changes after CANCEL is clicked.
     *
     * @throws BOMException
     */
    public void undoChanges() throws BOMException {
        this.model.undoChanges();
    }

    /**
     * @see org.elbe.relations.forms.AbstractEditForm#dispose()
     */
    @Override
    public void dispose() {
        this.dragSourceControl = null;
        super.dispose();
    }

    @Override
    public boolean getPageComplete() {
        return true;
    }

    // ---- inner classes ----

    /**
     * Filters all items that are associated yet.
     */
    private class RelationsFilter extends ViewerFilter {
        public RelationsFilter() {
            super();
        }

        /**
         * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        @Override
        public boolean select(final Viewer inViewer,
                final Object inParentElement, final Object inElement) {
            return FormAssociate.this.model.select((ILightWeightItem) inElement);
        }

    }

    private interface IAction1 {
        void run(UniqueID[] inItems);
    }

    private interface IAction2 {
        UniqueID[] createIDArray(Object[] inItems);
    }

}
