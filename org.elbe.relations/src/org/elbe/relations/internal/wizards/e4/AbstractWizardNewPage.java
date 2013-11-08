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
package org.elbe.relations.internal.wizards.e4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.e4.ui.workbench.swt.internal.copy.FilteredTree;
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
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.dialogs.WizardActivityFilter;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.elbe.relations.internal.wizards.e4.util.NewWizardCollectionComparator;
import org.elbe.relations.internal.wizards.e4.util.WizardContentProvider;
import org.elbe.relations.internal.wizards.e4.util.WizardPatternFilter;
import org.elbe.relations.internal.wizards.e4.util.WorkbenchLabelProvider;
import org.elbe.relations.internal.wizards.e4.util.WorkbenchWizardElement;
import org.elbe.relations.internal.wizards.e4.util.WorkbenchWizardNode;

/**
 * Base class for <code>New</code>, <code>Import</code> and <code>Export</code>
 * wizard selection tabs.
 * 
 * @author Luthiger
 * @see org.eclipse.ui.internal.dialogs.NewWizardNewPage
 */
@SuppressWarnings("restriction")
public abstract class AbstractWizardNewPage implements
		ISelectionChangedListener {

	private final static int SIZING_LISTS_HEIGHT = 200;
	private final static int SIZING_VIEWER_WIDTH = 300;

	private final AbstractExtensionWizardSelectionPage page;

	private IWizardCategory wizardCategories;
	private IWizardDescriptor[] primaryWizards;
	private boolean needShowAll;
	private final WizardActivityFilter filter = new WizardActivityFilter();
	private IDialogSettings settings;

	private FilteredTree filteredTree;
	private WizardPatternFilter filteredTreeFilter;

	private CLabel descImageCanvas;
	private final Map<ImageDescriptor, Image> imageTable = new HashMap<ImageDescriptor, Image>();

	// Keep track of the wizards we have previously selected
	private final Map<IWizardDescriptor, WorkbenchWizardNode> selectedWizards = new HashMap<IWizardDescriptor, WorkbenchWizardNode>();

	private IWizardDescriptor selectedElement;

	/**
	 * NewWizardNewPage constructor.
	 * 
	 * @param inMainPage
	 *            {@link AbstractExtensionWizardSelectionPage}
	 * @param inWizardCategories
	 *            {@link IWizardCategory}
	 * @param inPrimaryWizards
	 *            IWizardDescriptor[]
	 */
	public AbstractWizardNewPage(
			final AbstractExtensionWizardSelectionPage inMainPage,
			final IWizardCategory inWizardCategories,
			final IWizardDescriptor[] inPrimaryWizards) {
		page = inMainPage;
		wizardCategories = inWizardCategories;
		primaryWizards = inPrimaryWizards;

		trimPrimaryWizards();

		if (this.primaryWizards.length > 0) {
			if (allPrimary(inWizardCategories)) {
				wizardCategories = null; // dont bother considering the
											// categories as all wizards are
											// primary
				needShowAll = false;
			} else {
				needShowAll = !allActivityEnabled(inWizardCategories);
			}
		} else {
			needShowAll = !allActivityEnabled(inWizardCategories);
		}
	}

	/**
	 * Remove all primary wizards that are not in the wizard collection
	 */
	private void trimPrimaryWizards() {
		final ArrayList<IWizardDescriptor> lPrimaryWizards = new ArrayList<IWizardDescriptor>(
				primaryWizards.length);

		if (wizardCategories == null) {
			return;// No categories so nothing to trim
		}

		for (final IWizardDescriptor lPrimary : primaryWizards) {
			if (wizardCategories.findWizard(lPrimary.getId()) != null) {
				lPrimaryWizards.add(lPrimary);
			}
		}
		primaryWizards = lPrimaryWizards
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
		for (final IWizardDescriptor lElement : primaryWizards) {
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
	private boolean allActivityEnabled(final IWizardCategory inWizardCategories) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(
	 * org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(final SelectionChangedEvent inEvent) {
		page.setErrorMessage(null);
		page.setMessage(null);

		final Object lSelectedObject = getSingleSelection((IStructuredSelection) inEvent
				.getSelection());

		if (lSelectedObject instanceof IWizardDescriptor) {
			if (lSelectedObject == selectedElement) {
				return;
			}
			updateWizardSelection((IWizardDescriptor) lSelectedObject);
		} else {
			selectedElement = null;
			page.setHasPages(false);
			page.setCanFinishEarly(false);
			page.selectWizardNode(null);
			updateDescription(null);
		}
	}

	/**
	 * Returns the single selected object contained in the passed
	 * selectionEvent, or <code>null</code> if the selectionEvent contains
	 * either 0 or 2+ selected objects.
	 */
	protected Object getSingleSelection(final IStructuredSelection inSelection) {
		return inSelection.size() == 1 ? inSelection.getFirstElement() : null;
	}

	/**
	 * @param inSelectedObject
	 */
	private void updateWizardSelection(final IWizardDescriptor inSelectedObject) {
		selectedElement = inSelectedObject;
		WorkbenchWizardNode lSelectedNode;
		if (selectedWizards.containsKey(inSelectedObject)) {
			lSelectedNode = selectedWizards.get(inSelectedObject);
		} else {
			lSelectedNode = createNode(page, inSelectedObject);
			selectedWizards.put(inSelectedObject, lSelectedNode);
		}

		page.setCanFinishEarly(inSelectedObject.canFinishEarly());
		page.setHasPages(inSelectedObject.hasPages());
		page.selectWizardNode(lSelectedNode);

		updateDescription(inSelectedObject);
	}

	protected abstract WorkbenchWizardNode createNode(
			final AbstractExtensionWizardSelectionPage inWizardPage,
			final IWizardDescriptor inElement);

	/**
	 * @param inSettings
	 */
	public void setDialogSettings(final IDialogSettings inSettings) {
		settings = inSettings;
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

		filteredTree = createFilteredTree(lInnerContainer);
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

		filteredTreeFilter = new WizardPatternFilter();
		final FilteredTree outFilterTree = new FilteredTree(lComposite,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER,
				filteredTreeFilter, true);

		final TreeViewer lTreeViewer = outFilterTree.getViewer();
		lTreeViewer.setContentProvider(new WizardContentProvider());
		lTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
		lTreeViewer.setComparator(NewWizardCollectionComparator.getInstance());
		lTreeViewer.addSelectionChangedListener(this);

		final ArrayList<Object> lInputArray = new ArrayList<Object>(
				Arrays.asList(primaryWizards));

		boolean lExpandTop = false;

		if (wizardCategories != null) {
			if (wizardCategories.getParent() == null) {
				final IWizardCategory[] lChildren = wizardCategories
						.getCategories();
				for (int i = 0; i < lChildren.length; i++) {
					lInputArray.add(lChildren[i]);
				}
			} else {
				lExpandTop = true;
				lInputArray.add(wizardCategories);
			}
		}

		// ensure the category is expanded. If there is a remembered expansion
		// it will be set later.
		if (lExpandTop) {
			lTreeViewer.setAutoExpandLevel(2);
		}

		lTreeViewer.setInput(new AdaptableList(lInputArray));

		outFilterTree.setBackground(inParent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
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
					page.advanceToNextPageOrFinish();
				}
			}
		});

		lTreeViewer.addFilter(filter);

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
		descImageCanvas = new CLabel(inParent, SWT.NONE);
		final GridData lData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
				| GridData.VERTICAL_ALIGN_BEGINNING);
		lData.widthHint = 0;
		lData.heightHint = 0;
		descImageCanvas.setLayoutData(lData);

		// hook a listener to get rid of cached images.
		descImageCanvas.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent inEvent) {
				for (final Iterator<Image> lImages = imageTable.values()
						.iterator(); lImages.hasNext();) {
					lImages.next().dispose();
				}
				imageTable.clear();
			}
		});
	}

	private void updateDescription(final IWizardDescriptor inSelectedObject) {
		String lDescription = ""; //$NON-NLS-1$
		if (inSelectedObject != null) {
			lDescription = inSelectedObject.getDescription();
		}

		page.setDescription(lDescription);

		if (hasImage(inSelectedObject)) {
			ImageDescriptor lDescriptor = null;
			if (inSelectedObject != null) {
				lDescriptor = inSelectedObject.getDescriptionImage();
			}

			if (lDescriptor != null) {
				final GridData lData = (GridData) descImageCanvas
						.getLayoutData();
				lData.widthHint = SWT.DEFAULT;
				lData.heightHint = SWT.DEFAULT;
				Image lImage = imageTable.get(lDescriptor);
				if (lImage == null) {
					lImage = lDescriptor.createImage(false);
					imageTable.put(lDescriptor, lImage);
				}
				descImageCanvas.setImage(lImage);
			}
		} else {
			final GridData lData = (GridData) descImageCanvas.getLayoutData();
			lData.widthHint = 0;
			lData.heightHint = 0;
			descImageCanvas.setImage(null);
		}

		descImageCanvas.getParent().layout(true);
		filteredTree.getViewer().getTree().showSelection();

		final IWizardContainer lContainer = page.getWizard().getContainer();
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
