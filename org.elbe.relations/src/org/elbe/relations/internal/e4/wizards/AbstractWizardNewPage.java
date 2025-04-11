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
package org.elbe.relations.internal.e4.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardContainer2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.dialogs.WizardActivityFilter;
import org.eclipse.ui.model.AdaptableList;
import org.elbe.relations.internal.e4.wizards.util.IWizardCategory;
import org.elbe.relations.internal.e4.wizards.util.IWizardDescriptor;
import org.elbe.relations.internal.e4.wizards.util.NewWizardCollectionComparator;
import org.elbe.relations.internal.e4.wizards.util.WizardContentProvider;
import org.elbe.relations.internal.e4.wizards.util.WizardPatternFilter;
import org.elbe.relations.internal.e4.wizards.util.WorkbenchLabelProvider;
import org.elbe.relations.internal.e4.wizards.util.WorkbenchWizardElement;
import org.elbe.relations.internal.e4.wizards.util.WorkbenchWizardNode;

/**
 * Base class for <code>New</code>, <code>Import</code> and <code>Export</code>
 * wizard selection tabs.
 *
 * @author Luthiger <br />
 *         see org.eclipse.ui.internal.dialogs.NewWizardNewPage
 */
@SuppressWarnings("restriction")
public abstract class AbstractWizardNewPage
implements ISelectionChangedListener {

    private final static int SIZING_LISTS_HEIGHT = 200;
    private final static int SIZING_VIEWER_WIDTH = 300;

    private final AbstractExtensionWizardSelectionPage page;

    private IWizardCategory wizardCategories;
    private IWizardDescriptor[] primaryWizards;
    private final WizardActivityFilter filter = new WizardActivityFilter();
    private FilteredTree filteredTree;
    private WizardPatternFilter filteredTreeFilter;

    private CLabel descImageCanvas;
    private final Map<ImageDescriptor, Image> imageTable = new HashMap<ImageDescriptor, Image>();

    // Keep track of the wizards we have previously selected
    private final Map<IWizardDescriptor, WorkbenchWizardNode> selectedWizards = new HashMap<IWizardDescriptor, WorkbenchWizardNode>();

    private IWizardDescriptor selectedElement;
    private final IEclipseContext context;

    /**
     * NewWizardNewPage constructor.
     *
     * @param inMainPage
     *            {@link AbstractExtensionWizardSelectionPage}
     * @param inContext
     *            {@link IEclipseContext}
     * @param inWizardCategories
     *            {@link IWizardCategory}
     * @param inPrimaryWizards
     *            IWizardDescriptor[]
     */
    public AbstractWizardNewPage(
            final AbstractExtensionWizardSelectionPage inMainPage,
            final IEclipseContext inContext, final IWizardCategory inWizardCategories,
            final IWizardDescriptor[] inPrimaryWizards) {
        this.page = inMainPage;
        this.context = inContext;
        this.wizardCategories = inWizardCategories;
        this.primaryWizards = inPrimaryWizards;

        trimPrimaryWizards();

        if (this.primaryWizards.length > 0) {
            if (allPrimary(inWizardCategories)) {
                this.wizardCategories = null; // dont bother considering the
                // categories as all wizards are
                // primary
            } else {
                allActivityEnabled(inWizardCategories);
            }
        } else {
            allActivityEnabled(inWizardCategories);
        }
    }

    /**
     * Remove all primary wizards that are not in the wizard collection
     */
    private void trimPrimaryWizards() {
        final ArrayList<IWizardDescriptor> lPrimaryWizards = new ArrayList<IWizardDescriptor>(
                this.primaryWizards.length);

        if (this.wizardCategories == null) {
            return;// No categories so nothing to trim
        }

        for (final IWizardDescriptor lPrimary : this.primaryWizards) {
            if (this.wizardCategories.findWizard(lPrimary.getId()) != null) {
                lPrimaryWizards.add(lPrimary);
            }
        }
        this.primaryWizards = lPrimaryWizards
                .toArray(new IWizardDescriptor[lPrimaryWizards.size()]);
    }

    /**
     * @return boolean whether all wizards in the category are considered
     *         primary
     */
    private boolean allPrimary(final IWizardCategory inWizardCategories) {
        final IWizardDescriptor[] lWizards = inWizardCategories.getWizards();
        for (int i = 0; i < lWizards.length; i++) {
            final IWizardDescriptor lWizard = lWizards[i];
            if (!isPrimary(lWizard)) {
                return false;
            }
        }

        final IWizardCategory[] lChildren = inWizardCategories.getCategories();
        for (int i = 0; i < lChildren.length; i++) {
            if (!allPrimary(lChildren[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param inWizard
     * @return whether the given wizard is primary
     */
    private boolean isPrimary(final IWizardDescriptor inWizard) {
        for (final IWizardDescriptor lElement : this.primaryWizards) {
            if (lElement.equals(inWizard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param inWizardCategories
     *            the wizard category
     * @return whether all of the wizards in the category are enabled via
     *         activity filtering
     */
    private boolean allActivityEnabled(
            final IWizardCategory inWizardCategories) {
        final IWizardDescriptor[] lWizards = inWizardCategories.getWizards();
        for (int i = 0; i < lWizards.length; i++) {
            final IWizardDescriptor lWizard = lWizards[i];
            if (WorkbenchActivityHelper.filterItem(lWizard)) {
                return false;
            }
        }

        final IWizardCategory[] lChildren = inWizardCategories.getCategories();
        for (int i = 0; i < lChildren.length; i++) {
            if (!allActivityEnabled(lChildren[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void selectionChanged(final SelectionChangedEvent inEvent) {
        this.page.setErrorMessage(null);
        this.page.setMessage(null);

        final Object lSelectedObject = getSingleSelection(
                (IStructuredSelection) inEvent.getSelection());

        if (lSelectedObject instanceof IWizardDescriptor) {
            if (lSelectedObject == this.selectedElement) {
                return;
            }
            updateWizardSelection((IWizardDescriptor) lSelectedObject);
        } else {
            this.selectedElement = null;
            this.page.setHasPages(false);
            this.page.setCanFinishEarly(false);
            this.page.selectWizardNode(null);
            updateDescription(null);
        }
    }

    /**
     * Returns the single selected object contained in the passed
     * selectionEvent, or <code>null</code> if the selectionEvent contains
     * either 0 or 2+ selected objects.
     */
    protected Object getSingleSelection(
            final IStructuredSelection inSelection) {
        return inSelection.size() == 1 ? inSelection.getFirstElement() : null;
    }

    /**
     * @param inSelectedObject
     */
    private void updateWizardSelection(
            final IWizardDescriptor inSelectedObject) {
        this.selectedElement = inSelectedObject;
        WorkbenchWizardNode lSelectedNode;
        if (this.selectedWizards.containsKey(inSelectedObject)) {
            lSelectedNode = this.selectedWizards.get(inSelectedObject);
        } else {
            lSelectedNode = createNode(this.page, inSelectedObject, this.context);
            this.selectedWizards.put(inSelectedObject, lSelectedNode);
        }

        this.page.setCanFinishEarly(inSelectedObject.canFinishEarly());
        this.page.setHasPages(inSelectedObject.hasPages());
        this.page.selectWizardNode(lSelectedNode);

        updateDescription(inSelectedObject);
    }

    protected abstract WorkbenchWizardNode createNode(
            final AbstractExtensionWizardSelectionPage inWizardPage,
            final IWizardDescriptor inElement, IEclipseContext inContext);

    /**
     * @param inSettings
     */
    public void setDialogSettings(final IDialogSettings inSettings) {
    }

    protected Control createControl(final Composite inParent) {
        final Font lWizardFont = inParent.getFont();
        // top level group
        final Composite lOuterContainer = new Composite(inParent, SWT.NONE);
        GridLayout lLayout = new GridLayout();
        lOuterContainer.setLayout(lLayout);

        final Label lWizardLabel = new Label(lOuterContainer, SWT.NONE);
        GridData lData = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
        lOuterContainer.setLayoutData(lData);
        lWizardLabel.setFont(lWizardFont);
        lWizardLabel.setText(WorkbenchMessages.NewWizardNewPage_wizardsLabel);

        final Composite lInnerContainer = new Composite(lOuterContainer,
                SWT.NONE);
        lLayout = new GridLayout(2, false);
        lLayout.marginHeight = 0;
        lLayout.marginWidth = 0;
        lInnerContainer.setLayout(lLayout);
        lInnerContainer.setFont(lWizardFont);
        lData = new GridData(SWT.FILL, SWT.FILL, true, true);
        lInnerContainer.setLayoutData(lData);

        this.filteredTree = createFilteredTree(lInnerContainer);
        createImage(lInnerContainer);
        updateDescription(null);

        // // wizard actions pane...create SWT table directly to
        // // get single selection mode instead of multi selection.
        // restoreWidgetValues();

        return lOuterContainer;
    }

    protected FilteredTree createFilteredTree(final Composite inParent) {
        final Composite lComposite = new Composite(inParent, SWT.NONE);
        final GridLayout lLayout = new GridLayout();
        lLayout.marginHeight = 0;
        lLayout.marginWidth = 0;
        lComposite.setLayout(lLayout);

        final GridData lData = new GridData(SWT.FILL, SWT.FILL, true, true);
        lData.widthHint = SIZING_VIEWER_WIDTH;
        lData.horizontalSpan = 2;
        lData.grabExcessHorizontalSpace = true;
        lData.grabExcessVerticalSpace = true;

        final boolean lNeedsHint = DialogUtil.inRegularFontMode(inParent);

        // Only give a height hint if the dialog is going to be too small
        if (lNeedsHint) {
            lData.heightHint = SIZING_LISTS_HEIGHT;
        }
        lComposite.setLayoutData(lData);

        this.filteredTreeFilter = new WizardPatternFilter();
        final FilteredTree outFilterTree = new FilteredTree(lComposite,
                SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER,
                this.filteredTreeFilter, true, true);

        final TreeViewer lTreeViewer = outFilterTree.getViewer();
        lTreeViewer.setContentProvider(new WizardContentProvider());
        lTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
        lTreeViewer.setComparator(NewWizardCollectionComparator.getInstance());
        lTreeViewer.addSelectionChangedListener(this);

        final ArrayList<Object> lInputArray = new ArrayList<Object>(
                Arrays.asList(this.primaryWizards));

        boolean lExpandTop = false;

        if (this.wizardCategories != null) {
            if (this.wizardCategories.getParent() == null) {
                final IWizardCategory[] lChildren = this.wizardCategories
                        .getCategories();
                for (int i = 0; i < lChildren.length; i++) {
                    lInputArray.add(lChildren[i]);
                }
            } else {
                lExpandTop = true;
                lInputArray.add(this.wizardCategories);
            }
        }

        // ensure the category is expanded. If there is a remembered expansion
        // it will be set later.
        if (lExpandTop) {
            lTreeViewer.setAutoExpandLevel(2);
        }

        lTreeViewer.setInput(new AdaptableList(lInputArray));

        outFilterTree.setBackground(inParent.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        lTreeViewer.getTree().setFont(inParent.getFont());

        lTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            /*
             * (non-Javadoc)
             *
             * @see
             * org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org
             * .eclipse.jface.viewers.DoubleClickEvent)
             */
            @Override
            public void doubleClick(final DoubleClickEvent inEvent) {
                final IStructuredSelection lSelection = (IStructuredSelection) inEvent
                        .getSelection();
                selectionChanged(new SelectionChangedEvent(inEvent.getViewer(),
                        lSelection));

                final Object lElement = lSelection.getFirstElement();
                if (lTreeViewer.isExpandable(lElement)) {
                    lTreeViewer.setExpandedState(lElement,
                            !lTreeViewer.getExpandedState(lElement));
                } else if (lElement instanceof WorkbenchWizardElement) {
                    AbstractWizardNewPage.this.page.advanceToNextPageOrFinish();
                }
            }
        });

        lTreeViewer.addFilter(this.filter);

        Dialog.applyDialogFont(outFilterTree);
        return outFilterTree;
    }

    /**
     * Create the image controls.
     *
     * @param inParent
     *            the parent <code>Composite</code>.
     * @since 3.0
     */
    private void createImage(final Composite inParent) {
        this.descImageCanvas = new CLabel(inParent, SWT.NONE);
        final GridData lData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
                | GridData.VERTICAL_ALIGN_BEGINNING);
        lData.widthHint = 0;
        lData.heightHint = 0;
        this.descImageCanvas.setLayoutData(lData);

        // hook a listener to get rid of cached images.
        this.descImageCanvas.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent inEvent) {
                for (final Iterator<Image> lImages = AbstractWizardNewPage.this.imageTable.values()
                        .iterator(); lImages.hasNext();) {
                    lImages.next().dispose();
                }
                AbstractWizardNewPage.this.imageTable.clear();
            }
        });
    }

    private void updateDescription(final IWizardDescriptor inSelectedObject) {
        String lDescription = ""; //$NON-NLS-1$
        if (inSelectedObject != null) {
            lDescription = inSelectedObject.getDescription();
        }

        this.page.setDescription(lDescription);

        if (hasImage(inSelectedObject)) {
            ImageDescriptor lDescriptor = null;
            if (inSelectedObject != null) {
                lDescriptor = inSelectedObject.getDescriptionImage();
            }

            if (lDescriptor != null) {
                final GridData lData = (GridData) this.descImageCanvas
                        .getLayoutData();
                lData.widthHint = SWT.DEFAULT;
                lData.heightHint = SWT.DEFAULT;
                Image lImage = this.imageTable.get(lDescriptor);
                if (lImage == null) {
                    lImage = lDescriptor.createImage(false);
                    this.imageTable.put(lDescriptor, lImage);
                }
                this.descImageCanvas.setImage(lImage);
            }
        } else {
            final GridData lData = (GridData) this.descImageCanvas.getLayoutData();
            lData.widthHint = 0;
            lData.heightHint = 0;
            this.descImageCanvas.setImage(null);
        }

        this.descImageCanvas.getParent().layout(true);
        this.filteredTree.getViewer().getTree().showSelection();

        final IWizardContainer lContainer = this.page.getWizard().getContainer();
        if (lContainer instanceof IWizardContainer2) {
            ((IWizardContainer2) lContainer).updateSize();
        }
    }

    /**
     * Tests whether the given wizard has an associated image.
     *
     * @param inSelectedObject
     *            the wizard to test
     * @return whether the given wizard has an associated image
     */
    private boolean hasImage(final IWizardDescriptor inSelectedObject) {
        if (inSelectedObject == null) {
            return false;
        }
        if (inSelectedObject.getDescriptionImage() != null) {
            return true;
        }
        return false;
    }

}
