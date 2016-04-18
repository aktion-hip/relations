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
package org.elbe.relations.internal.e4.keys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.Category;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.workbench.swt.internal.copy.FilteredTree;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.commands.ICommandImageService;
import org.elbe.relations.internal.e4.keys.model.BindingElement;
import org.elbe.relations.internal.e4.keys.model.BindingModel;
import org.elbe.relations.internal.e4.keys.model.CommonModel;
import org.elbe.relations.internal.e4.keys.model.ConflictModel;
import org.elbe.relations.internal.e4.keys.model.ContextElement;
import org.elbe.relations.internal.e4.keys.model.ContextModel;
import org.elbe.relations.internal.e4.keys.model.ModelElement;
import org.elbe.relations.internal.e4.keys.model.SchemeElement;
import org.elbe.relations.internal.e4.keys.model.SchemeModel;
import org.elbe.relations.internal.preferences.AbstractPreferencePage;

/**
 * <p>
 * A preference page that is capable of displaying and editing the bindings
 * between commands and user input events. These are typically things like
 * keyboard shortcuts.
 * </p>
 * <p>
 * This preference page has four general types of methods. Create methods are
 * called when the page is first made visible. They are responsible for creating
 * all of the widgets, and laying them out within the preference page. Fill
 * methods populate the contents of the widgets that contain collections of data
 * from which items can be selected. The select methods respond to selection
 * events from the user, such as a button press or a table selection. The update
 * methods update the contents of various widgets based on the current state of
 * the user interface. For example, the command name label will always try to
 * match the current select in the binding table.
 * </p>
 *
 * @author Luthiger <br />
 *         see org.eclipse.ui.internal.keys.NewKeysPreferencePage
 */
@SuppressWarnings("restriction")
public class RelationsKeysPreferencePage extends AbstractPreferencePage {
	public final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	/**
	 * The number of items to show in the bindings table tree.
	 */
	private static final int ITEMS_TO_SHOW = 7;

	private static final int COMMAND_NAME_COLUMN = 0;
	private static final int KEY_SEQUENCE_COLUMN = 1;
	private static final int CONTEXT_COLUMN = 2;
	private static final int CATEGORY_COLUMN = 3;
	private static final int USER_DELTA_COLUMN = 4;
	private static int NUM_OF_COLUMNS = USER_DELTA_COLUMN + 1;

	private Category defaultCategory;

	private ComboViewer schemeCombo;
	private CategoryPatternFilter patternFilter;
	private CategoryFilterTree filteredTree;
	private Label commandNameValueLabel;
	private Text descriptionText;
	private Text bindingText;
	private KeySequenceText keySequenceText;
	private ComboViewer whenCombo;
	private TableViewer conflictViewer;
	private boolean filterActionSetContexts = true;
	private boolean filterInternalContexts = true;
	private boolean initialized = false;

	@Inject
	private ECommandService commandService;

	@Inject
	private KeyController keyController;

	@Inject
	@Optional
	private KeyBindingDispatcher dispatcher;

	@Inject
	@Optional
	private ICommandImageService commandImageService;

	@PostConstruct
	public void init() {
		defaultCategory = commandService.getCategory(null);
		keyController.init();
		defaultCategory = commandService.getCategory(null);
		initialized = true;
	}

	@Override
	protected Control createContents(final Composite inParent) {
		if (!initialized) {
			init();
		}

		final Composite outPage = new Composite(inParent, SWT.NONE);
		final GridLayout lLayout = new GridLayout(1, false);
		lLayout.marginWidth = 0;
		outPage.setLayout(lLayout);

		patternFilter = new CategoryPatternFilter(true,
		        commandService.getCategory(null));

		createSchemeControls(outPage);
		createTree(outPage);
		createTreeControls(outPage);
		createDataControls(outPage);
		createButtonBar(outPage);

		fill();
		applyDialogFont(outPage);

		// we want the description text control to span four lines, but because
		// we need the dialog's font for this information, we have to set it
		// here after the dialog font has been applied
		final GC lGC = new GC(descriptionText);
		lGC.setFont(descriptionText.getFont());
		final FontMetrics lMetrics = lGC.getFontMetrics();
		lGC.dispose();
		final int lHeight = lMetrics.getHeight() * 4;

		final GridData lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.horizontalAlignment = SWT.FILL;
		lGridData.horizontalSpan = 2;
		lGridData.heightHint = lHeight;
		descriptionText.setLayoutData(lGridData);

		return outPage;
	}

	private void fill() {
		schemeCombo.setInput(keyController.getSchemeModel());
		schemeCombo.setSelection(new StructuredSelection(
		        keyController.getSchemeModel().getSelectedElement()));

		// Apply context filters
		keyController.filterContexts(filterActionSetContexts,
		        filterInternalContexts);
		whenCombo.setInput(keyController.getContextModel());

		filteredTree.filterCategories(patternFilter.isFilteringCategories());
		filteredTree.getViewer().setInput(keyController.getBindingModel());
	}

	/**
	 * @param inParent
	 */
	private Control createButtonBar(final Composite inParent) {
		GridLayout lLayout;
		GridData lGridData;
		int lWidthHint;

		// Create the composite to house the button bar.
		final Composite outButtonBar = new Composite(inParent, SWT.NONE);
		lLayout = new GridLayout(2, false);
		lLayout.marginWidth = 0;
		outButtonBar.setLayout(lLayout);
		lGridData = new GridData();
		lGridData.horizontalAlignment = SWT.END;
		outButtonBar.setLayoutData(lGridData);

		// Advanced button.
		final Button lFiltersButton = new Button(outButtonBar, SWT.PUSH);
		lGridData = new GridData();
		lWidthHint = convertHorizontalDLUsToPixels(
		        IDialogConstants.BUTTON_WIDTH);
		lFiltersButton.setText(NewKeysPreferenceMessages.FiltersButton_Text);
		lGridData.widthHint = Math.max(lWidthHint,
		        lFiltersButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x)
		        + 5;
		lFiltersButton.setLayoutData(lGridData);
		lFiltersButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent inEvent) {
			}

			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				final KeysPreferenceFiltersDialog lDialog = new KeysPreferenceFiltersDialog(
		                getShell());
				lDialog.setFilterActionSet(filterActionSetContexts);
				lDialog.setFilterInternal(filterInternalContexts);
				lDialog.setFilterUncategorized(
		                filteredTree.isFilteringCategories());
				if (lDialog.open() == Window.OK) {
					filterActionSetContexts = lDialog.getFilterActionSet();
					filterInternalContexts = lDialog.getFilterInternal();
					filteredTree
		                    .filterCategories(lDialog.getFilterUncategorized());

					// Apply context filters
					keyController.filterContexts(filterActionSetContexts,
		                    filterInternalContexts);
					final ISelection lCurrentContextSelection = whenCombo
		                    .getSelection();
					whenCombo.setInput(keyController.getContextModel());
					whenCombo.setSelection(lCurrentContextSelection);
				}
			}
		});

		// Export bindings to CSV
		final Button lExportButton = new Button(outButtonBar, SWT.PUSH);
		// gridData = new GridData();
		lWidthHint = convertHorizontalDLUsToPixels(
		        IDialogConstants.BUTTON_WIDTH);
		lExportButton.setText(NewKeysPreferenceMessages.ExportButton_Text);
		lGridData.widthHint = Math.max(lWidthHint,
		        lExportButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x)
		        + 5;
		lExportButton.setLayoutData(lGridData);
		lExportButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent inEvent) {
			}

			@Override
			public void widgetSelected(final SelectionEvent inEvent) {
				keyController
		                .exportCSV(((Button) inEvent.getSource()).getShell());
			}

		});

		return outButtonBar;
	}

	/**
	 * @param inParent
	 */
	@SuppressWarnings("unchecked")
	private void createDataControls(final Composite inParent) {
		GridLayout lLayout;
		GridData lGridData;

		// Creates the data area.
		final Composite lDataArea = new Composite(inParent, SWT.NONE);
		lLayout = new GridLayout(2, true);
		lLayout.marginWidth = 0;
		lDataArea.setLayout(lLayout);
		lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.horizontalAlignment = SWT.FILL;
		lDataArea.setLayoutData(lGridData);

		// LEFT DATA AREA
		// Creates the left data area.
		final Composite lLeftDataArea = new Composite(lDataArea, SWT.NONE);
		lLayout = new GridLayout(3, false);
		lLeftDataArea.setLayout(lLayout);

		lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.verticalAlignment = SWT.TOP;
		lGridData.horizontalAlignment = SWT.FILL;
		lLeftDataArea.setLayoutData(lGridData);

		// line 1
		// The command name label.
		final Label lCommandNameLabel = new Label(lLeftDataArea, SWT.NONE);
		lCommandNameLabel
		        .setText(NewKeysPreferenceMessages.CommandNameLabel_Text);

		// The current command name.
		commandNameValueLabel = new Label(lLeftDataArea, SWT.NONE);
		lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.horizontalSpan = 2;
		lGridData.horizontalAlignment = SWT.FILL;
		commandNameValueLabel.setLayoutData(lGridData);

		// line 2
		final Label lCommandDescriptionlabel = new Label(lLeftDataArea,
		        SWT.LEAD);
		lCommandDescriptionlabel.setText(
		        NewKeysPreferenceMessages.CommandDescriptionLabel_Text);
		lGridData = new GridData();
		lGridData.verticalAlignment = SWT.BEGINNING;
		lCommandDescriptionlabel.setLayoutData(lGridData);

		descriptionText = new Text(lLeftDataArea,
		        SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY);

		// line 3
		// The binding label.
		final Label lBindingLabel = new Label(lLeftDataArea, SWT.NONE);
		lBindingLabel.setText(NewKeysPreferenceMessages.BindingLabel_Text);

		// The key sequence entry widget.
		bindingText = new Text(lLeftDataArea, SWT.BORDER);
		lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.horizontalAlignment = SWT.FILL;
		lGridData.widthHint = 200;
		bindingText.setLayoutData(lGridData);

		bindingText.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent inEvent) {
				if (dispatcher != null) {
					dispatcher.getKeyDownFilter().setEnabled(false);
				}
			}

			@Override
			public void focusLost(final FocusEvent inEvent) {
				if (dispatcher != null) {
					dispatcher.getKeyDownFilter().setEnabled(true);
				}
			}
		});
		bindingText.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent inEvent) {
				if (dispatcher != null) {
					dispatcher.getKeyDownFilter().setEnabled(true);
				}
			}
		});

		keySequenceText = new KeySequenceText(bindingText);
		keySequenceText.setKeyStrokeLimit(4);
		keySequenceText
		        .addPropertyChangeListener(new IPropertyChangeListener() {
			        @Override
			        public final void propertyChange(
		                    final PropertyChangeEvent inEvent) {
				        if (!inEvent.getOldValue()
		                        .equals(inEvent.getNewValue())) {
					        final KeySequence lKeySequence = keySequenceText
		                            .getKeySequence();
					        if (!lKeySequence.isComplete()) {
						        return;
					        }

					        final BindingElement lActiveBinding = (BindingElement) keyController
		                            .getBindingModel().getSelectedElement();
					        if (lActiveBinding != null) {
						        lActiveBinding.setTrigger(lKeySequence);
					        }
					        bindingText
		                            .setSelection(bindingText.getTextLimit());
				        }
			        }
		        });

		// Button for adding trapped key strokes
		final Button lAddKeyButton = new Button(lLeftDataArea,
		        SWT.LEFT | SWT.ARROW);
		lAddKeyButton.setToolTipText(
		        NewKeysPreferenceMessages.AddKeyButton_ToolTipText);
		lGridData = new GridData();
		lGridData.heightHint = schemeCombo.getCombo().getTextHeight();
		lAddKeyButton.setLayoutData(lGridData);

		// Arrow buttons aren't normally added to the tab list. Let's fix that.
		final Control[] lTabStops = lDataArea.getTabList();
		final ArrayList<Control> lNewTabStops = new ArrayList<Control>();
		for (int i = 0; i < lTabStops.length; i++) {
			final Control lTabStop = lTabStops[i];
			lNewTabStops.add(lTabStop);
			if (bindingText.equals(lTabStop)) {
				lNewTabStops.add(lAddKeyButton);
			}
		}
		final Control[] lNewTabStopArray = lNewTabStops
		        .toArray(new Control[lNewTabStops.size()]);
		lDataArea.setTabList(lNewTabStopArray);

		// Construct the menu to attach to the above button.
		final Menu lAddKeyMenu = new Menu(lAddKeyButton);
		final Iterator<KeyStroke> lTrappedKeyItr = KeySequenceText.TRAPPED_KEYS
		        .iterator();
		while (lTrappedKeyItr.hasNext()) {
			final KeyStroke lTrappedKey = lTrappedKeyItr.next();
			final MenuItem lMenuItem = new MenuItem(lAddKeyMenu, SWT.PUSH);
			lMenuItem.setText(lTrappedKey.format());
			lMenuItem.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(final SelectionEvent inEvent) {
					keySequenceText.insert(lTrappedKey);
					bindingText.setFocus();
					bindingText.setSelection(bindingText.getTextLimit());
				}
			});
		}
		lAddKeyButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent inSelectionEvent) {
				Point lButtonLocation = lAddKeyButton.getLocation();
				lButtonLocation = lDataArea.toDisplay(lButtonLocation.x,
		                lButtonLocation.y);
				final Point lButtonSize = lAddKeyButton.getSize();
				lAddKeyMenu.setLocation(lButtonLocation.x,
		                lButtonLocation.y + lButtonSize.y);
				lAddKeyMenu.setVisible(true);
			}
		});

		// The when label.
		final Label lWhenLabel = new Label(lLeftDataArea, SWT.NONE);
		lWhenLabel.setText(NewKeysPreferenceMessages.WhenLabel_Text);

		// The when combo.
		whenCombo = new ComboViewer(lLeftDataArea);
		lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.horizontalAlignment = SWT.FILL;
		lGridData.horizontalSpan = 2;
		final ViewerComparator lComparator = new ViewerComparator();
		whenCombo.setComparator(lComparator);
		whenCombo.getCombo().setVisibleItemCount(ITEMS_TO_SHOW);
		whenCombo.getCombo().setLayoutData(lGridData);
		whenCombo.setContentProvider(new ModelContentProvider());
		whenCombo.setLabelProvider(new ListLabelProvider());
		whenCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public final void selectionChanged(
		            final SelectionChangedEvent inEvent) {
				final ContextElement lContext = (ContextElement) ((IStructuredSelection) inEvent
		                .getSelection()).getFirstElement();
				if (lContext != null) {
					keyController.getContextModel()
		                    .setSelectedElement(lContext);
				}
			}
		});
		final IPropertyChangeListener lWhenListener = new IPropertyChangeListener() {
			// Sets the combo selection when a new keybinding is selected?
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() == keyController.getContextModel()
		                && CommonModel.PROP_SELECTED_ELEMENT
		                        .equals(inEvent.getProperty())) {
					final Object newVal = inEvent.getNewValue();
					final StructuredSelection structuredSelection = newVal == null
		                    ? null : new StructuredSelection(newVal);
					whenCombo.setSelection(structuredSelection, true);
				}
			}
		};
		keyController.addPropertyChangeListener(lWhenListener);

		// RIGHT DATA AREA
		// Creates the right data area.
		final Composite lRightDataArea = new Composite(lDataArea, SWT.NONE);
		lLayout = new GridLayout(1, false);
		lRightDataArea.setLayout(lLayout);
		lGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		lRightDataArea.setLayoutData(lGridData);

		new Label(lRightDataArea, SWT.NONE); // filler

		// The description label.
		final Label descriptionLabel = new Label(lRightDataArea, SWT.NONE);
		descriptionLabel.setText(NewKeysPreferenceMessages.ConflictsLabel_Text);
		lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.horizontalAlignment = SWT.FILL;
		descriptionLabel.setLayoutData(lGridData);

		conflictViewer = new TableViewer(lRightDataArea,
		        SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		final Table lTable = conflictViewer.getTable();
		lTable.setHeaderVisible(true);
		final TableColumn lBindingNameColumn = new TableColumn(lTable,
		        SWT.LEAD);
		lBindingNameColumn
		        .setText(NewKeysPreferenceMessages.CommandNameColumn_Text);
		lBindingNameColumn.setWidth(150);
		final TableColumn lBindingContextNameColumn = new TableColumn(lTable,
		        SWT.LEAD);
		lBindingContextNameColumn
		        .setText(NewKeysPreferenceMessages.WhenColumn_Text);
		lBindingContextNameColumn.setWidth(150);
		lGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		// gridData.horizontalIndent = 10;
		lTable.setLayoutData(lGridData);
		final TableLayout lTableLayout = new TableLayout();
		lTableLayout.addColumnData(new ColumnWeightData(60));
		lTableLayout.addColumnData(new ColumnWeightData(40));
		lTable.setLayout(lTableLayout);
		conflictViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(final Object inInputElement) {
				if (inInputElement instanceof Collection) {
					return ((Collection<?>) inInputElement).toArray();
				}
				return new Object[0];
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput,
		            final Object newInput) {
			}
		});
		conflictViewer.setLabelProvider(new BindingElementLabelProvider() {
			@Override
			public String getColumnText(final Object inObj, final int inIndex) {
				final BindingElement lElement = (BindingElement) inObj;
				if (inIndex == 0) {
					return lElement.getName();
				}
				return lElement.getContext() == null ? "" //$NON-NLS-1$
		                : lElement.getContext().getName();
			}
		});
		conflictViewer
		        .addSelectionChangedListener(new ISelectionChangedListener() {
			        // When the conflict viewer's selection changes, update the
		            // model's current selection
			        @Override
			        public void selectionChanged(
		                    final SelectionChangedEvent inEvent) {
				        final ModelElement lBinding = (ModelElement) ((IStructuredSelection) inEvent
		                        .getSelection()).getFirstElement();
				        final BindingModel lBindingModel = keyController
		                        .getBindingModel();
				        if (lBinding != null && lBinding != lBindingModel
		                        .getSelectedElement()) {
					        final StructuredSelection lSelection = new StructuredSelection(
		                            lBinding);
					        lBindingModel.setSelectedElement(lBinding);
					        conflictViewer.setSelection(lSelection);

					        boolean lSelectionVisible = false;
					        final TreeItem[] lItems = filteredTree.getViewer()
		                            .getTree().getItems();
					        for (int i = 0; i < lItems.length; i++) {
						        if (lItems[i].getData().equals(lBinding)) {
							        lSelectionVisible = true;
							        break;
						        }
					        }
					        if (!lSelectionVisible) {
						        filteredTree.getFilterControl().setText(""); //$NON-NLS-1$
						        filteredTree.getViewer().refresh();
						        lBindingModel.setSelectedElement(lBinding);
						        conflictViewer.setSelection(lSelection);
					        }
				        }
			        }
		        });

		final IPropertyChangeListener lConflictsListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() == keyController.getConflictModel()
		                && CommonModel.PROP_SELECTED_ELEMENT
		                        .equals(inEvent.getProperty())) {
					if (keyController.getConflictModel()
		                    .getConflicts() != null) {
						final Object lNewVal = inEvent.getNewValue();
						final StructuredSelection structuredSelection = lNewVal == null
		                        ? null : new StructuredSelection(lNewVal);
						conflictViewer.setSelection(structuredSelection, true);
					}
				} else if (ConflictModel.PROP_CONFLICTS
		                .equals(inEvent.getProperty())) {
					conflictViewer.setInput(inEvent.getNewValue());
				} else if (ConflictModel.PROP_CONFLICTS_ADD
		                .equals(inEvent.getProperty())) {
					conflictViewer.add(inEvent.getNewValue());
				} else if (ConflictModel.PROP_CONFLICTS_REMOVE
		                .equals(inEvent.getProperty())) {
					conflictViewer.remove(inEvent.getNewValue());
				}
			}
		};
		keyController.addPropertyChangeListener(lConflictsListener);

		final IPropertyChangeListener lDataUpdateListener = new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				BindingElement lBindingElement = null;
				boolean lWeCare = false;
				if (inEvent.getSource() == keyController.getBindingModel()
		                && CommonModel.PROP_SELECTED_ELEMENT
		                        .equals(inEvent.getProperty())) {
					lBindingElement = (BindingElement) inEvent.getNewValue();
					lWeCare = true;
				} else if (inEvent.getSource() == keyController
		                .getBindingModel().getSelectedElement()
		                && ModelElement.PROP_MODEL_OBJECT
		                        .equals(inEvent.getProperty())) {
					lBindingElement = (BindingElement) inEvent.getSource();
					lWeCare = true;
				}
				if (lBindingElement == null && lWeCare) {
					commandNameValueLabel.setText(""); //$NON-NLS-1$
					descriptionText.setText(""); //$NON-NLS-1$
					bindingText.setText(""); //$NON-NLS-1$
				} else if (lBindingElement != null) {
					commandNameValueLabel.setText(lBindingElement.getName());
					final String desc = lBindingElement.getDescription();
					descriptionText.setText(desc == null ? "" : desc); //$NON-NLS-1$
					final KeySequence lTrigger = (KeySequence) lBindingElement
		                    .getTrigger();
					keySequenceText.setKeySequence(lTrigger);
				}
			}
		};
		keyController.addPropertyChangeListener(lDataUpdateListener);
	}

	/**
	 * @param inParent
	 */
	private Control createTreeControls(final Composite inParent) {
		GridLayout lLayout;
		GridData lGridData;
		int lWidthHint;

		// Creates controls related to the tree.
		final Composite outTreeControls = new Composite(inParent, SWT.NONE);
		lLayout = new GridLayout(4, false);
		lLayout.marginWidth = 0;
		outTreeControls.setLayout(lLayout);
		lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.horizontalAlignment = SWT.FILL;
		outTreeControls.setLayoutData(lGridData);

		final Button lAddBindingButton = new Button(outTreeControls, SWT.PUSH);
		lGridData = new GridData();
		lWidthHint = convertHorizontalDLUsToPixels(
		        IDialogConstants.BUTTON_WIDTH);
		lAddBindingButton
		        .setText(NewKeysPreferenceMessages.AddBindingButton_Text);
		lGridData.widthHint = Math.max(lWidthHint,
		        lAddBindingButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x)
		        + 5;
		lAddBindingButton.setLayoutData(lGridData);
		lAddBindingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public final void widgetSelected(final SelectionEvent inEvent) {
				keyController.getBindingModel().copy();
			}
		});

		final Button lRemoveBindingButton = new Button(outTreeControls,
		        SWT.PUSH);
		lGridData = new GridData();
		lWidthHint = convertHorizontalDLUsToPixels(
		        IDialogConstants.BUTTON_WIDTH);
		lRemoveBindingButton
		        .setText(NewKeysPreferenceMessages.RemoveBindingButton_Text);
		lGridData.widthHint = Math.max(lWidthHint, lRemoveBindingButton
		        .computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		lRemoveBindingButton.setLayoutData(lGridData);
		lRemoveBindingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public final void widgetSelected(final SelectionEvent inEvent) {
				keyController.getBindingModel().remove();
			}
		});

		final Button lRestore = new Button(outTreeControls, SWT.PUSH);
		lGridData = new GridData();
		lWidthHint = convertHorizontalDLUsToPixels(
		        IDialogConstants.BUTTON_WIDTH);
		lRestore.setText(NewKeysPreferenceMessages.RestoreBindingButton_Text);
		lGridData.widthHint = Math.max(lWidthHint,
		        lRestore.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		lRestore.setLayoutData(lGridData);
		lRestore.addSelectionListener(new SelectionAdapter() {
			@Override
			public final void widgetSelected(final SelectionEvent event) {
				try {
					filteredTree.setRedraw(false);
					final BindingModel bindingModel = keyController
		                    .getBindingModel();
					bindingModel
		                    .restoreBinding(keyController.getContextModel());
				}
				finally {
					filteredTree.setRedraw(true);
				}
			}
		});

		return outTreeControls;
	}

	/**
	 * @param inParent
	 */
	private void createTree(final Composite inParent) {
		patternFilter = new CategoryPatternFilter(true, defaultCategory);
		patternFilter.filterCategories(true);

		GridData lGridData;

		filteredTree = new CategoryFilterTree(inParent,
		        SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER, patternFilter);
		final GridLayout lLayout = new GridLayout(1, false);
		lLayout.marginWidth = 0;
		filteredTree.setLayout(lLayout);
		lGridData = new GridData();
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.grabExcessVerticalSpace = true;
		lGridData.horizontalAlignment = SWT.FILL;
		lGridData.verticalAlignment = SWT.FILL;
		filteredTree.setLayoutData(lGridData);

		final TreeViewer lViewer = filteredTree.getViewer();
		// Make sure the filtered tree has a height of ITEMS_TO_SHOW
		final Tree lTree = lViewer.getTree();
		lTree.setHeaderVisible(true);
		final Object lLayoutData = lTree.getLayoutData();
		if (lLayoutData instanceof GridData) {
			lGridData = (GridData) lLayoutData;
			final int lItemHeight = lTree.getItemHeight();
			if (lItemHeight > 1) {
				lGridData.heightHint = ITEMS_TO_SHOW * lItemHeight;
			}
		}

		final BindingModelComparator lComparator = new BindingModelComparator();
		lViewer.setComparator(lComparator);

		final TreeColumn lCommandNameColumn = new TreeColumn(lTree, SWT.LEFT,
		        COMMAND_NAME_COLUMN);
		lCommandNameColumn
		        .setText(NewKeysPreferenceMessages.CommandNameColumn_Text);
		lTree.setSortColumn(lCommandNameColumn);
		lTree.setSortDirection(lComparator.isAscending() ? SWT.UP : SWT.DOWN);
		lCommandNameColumn.addSelectionListener(new ResortColumn(lComparator,
		        lCommandNameColumn, lViewer, COMMAND_NAME_COLUMN));

		final TreeColumn lTriggerSequenceColumn = new TreeColumn(lTree,
		        SWT.LEFT, KEY_SEQUENCE_COLUMN);
		lTriggerSequenceColumn
		        .setText(NewKeysPreferenceMessages.TriggerSequenceColumn_Text);
		lTriggerSequenceColumn
		        .addSelectionListener(new ResortColumn(lComparator,
		                lTriggerSequenceColumn, lViewer, KEY_SEQUENCE_COLUMN));

		final TreeColumn lWhenColumn = new TreeColumn(lTree, SWT.LEFT,
		        CONTEXT_COLUMN);
		lWhenColumn.setText(NewKeysPreferenceMessages.WhenColumn_Text);
		lWhenColumn.addSelectionListener(new ResortColumn(lComparator,
		        lWhenColumn, lViewer, CONTEXT_COLUMN));

		final TreeColumn lCategoryColumn = new TreeColumn(lTree, SWT.LEFT,
		        CATEGORY_COLUMN);
		lCategoryColumn.setText(NewKeysPreferenceMessages.CategoryColumn_Text);
		lCategoryColumn.addSelectionListener(new ResortColumn(lComparator,
		        lCategoryColumn, lViewer, CATEGORY_COLUMN));

		final TreeColumn lUserMarker = new TreeColumn(lTree, SWT.LEFT,
		        USER_DELTA_COLUMN);
		lUserMarker.setText(NewKeysPreferenceMessages.UserColumn_Text);
		lUserMarker.addSelectionListener(new ResortColumn(lComparator,
		        lUserMarker, lViewer, USER_DELTA_COLUMN));

		lViewer.setContentProvider(new ModelContentProvider());
		lViewer.setLabelProvider(new BindingElementLabelProvider());

		filteredTree.getPatternFilter().setIncludeLeadingWildcard(true);

		final TreeColumn[] lColumns = lViewer.getTree().getColumns();
		lColumns[COMMAND_NAME_COLUMN].setWidth(240);
		lColumns[KEY_SEQUENCE_COLUMN].setWidth(130);
		lColumns[CONTEXT_COLUMN].setWidth(130);
		lColumns[CATEGORY_COLUMN].setWidth(130);
		lColumns[USER_DELTA_COLUMN].setWidth(50);

		lViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			// When the viewer changes selection, update the model's current
		    // selection
			@Override
			public void selectionChanged(final SelectionChangedEvent inEvent) {
				final ModelElement lBinding = (ModelElement) ((IStructuredSelection) inEvent
		                .getSelection()).getFirstElement();
				keyController.getBindingModel().setSelectedElement(lBinding);
			}
		});

		final IPropertyChangeListener lTreeUpdateListener = new IPropertyChangeListener() {
			// When the model changes a property, update the viewer
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() == keyController.getBindingModel()
		                && CommonModel.PROP_SELECTED_ELEMENT
		                        .equals(inEvent.getProperty())) {
					final Object lNewVal = inEvent.getNewValue();
					final StructuredSelection structuredSelection = lNewVal == null
		                    ? null : new StructuredSelection(lNewVal);
					lViewer.setSelection(structuredSelection, true);
				} else if (inEvent.getSource() instanceof BindingElement
		                && ModelElement.PROP_MODEL_OBJECT
		                        .equals(inEvent.getProperty())) {
					lViewer.update(inEvent.getSource(), null);
				} else if (BindingElement.PROP_CONFLICT
		                .equals(inEvent.getProperty())) {
					lViewer.update(inEvent.getSource(), null);
				} else if (BindingModel.PROP_BINDINGS
		                .equals(inEvent.getProperty())) {
					lViewer.refresh();
				} else if (BindingModel.PROP_BINDING_ADD
		                .equals(inEvent.getProperty())) {
					lViewer.add(keyController.getBindingModel(),
		                    inEvent.getNewValue());
				} else if (BindingModel.PROP_BINDING_REMOVE
		                .equals(inEvent.getProperty())) {
					lViewer.remove(inEvent.getNewValue());
				} else if (BindingModel.PROP_BINDING_FILTER
		                .equals(inEvent.getProperty())) {
					lViewer.refresh();
				}
			}
		};
		keyController.addPropertyChangeListener(lTreeUpdateListener);
	}

	/**
	 * @param inParent
	 */
	private void createSchemeControls(final Composite inParent) {
		final Composite lSchemeControls = new Composite(inParent, SWT.NONE);
		final GridLayout lLayout = new GridLayout(3, false);
		lLayout.marginWidth = 0;
		lSchemeControls.setLayout(lLayout);

		final Label lSchemeLabel = new Label(lSchemeControls, SWT.NONE);
		lSchemeLabel.setText(NewKeysPreferenceMessages.SchemeLabel_Text);

		schemeCombo = new ComboViewer(lSchemeControls);
		schemeCombo.setLabelProvider(new ListLabelProvider());
		schemeCombo.setContentProvider(new ModelContentProvider());
		final GridData lGridData = new GridData();
		lGridData.widthHint = 150;
		lGridData.horizontalAlignment = SWT.FILL;
		schemeCombo.getCombo().setLayoutData(lGridData);
		schemeCombo
		        .addSelectionChangedListener(new ISelectionChangedListener() {
			        @Override
			        public void selectionChanged(
		                    final SelectionChangedEvent inEvent) {
				        BusyIndicator.showWhile(
		                        filteredTree.getViewer().getTree().getDisplay(),
		                        new Runnable() {
					        @Override
					        public void run() {
						        final SchemeElement lScheme = (SchemeElement) ((IStructuredSelection) inEvent
		                                .getSelection()).getFirstElement();
						        keyController.getSchemeModel()
		                                .setSelectedElement(lScheme);
					        }
				        });
			        }
		        });
		final IPropertyChangeListener lListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent inEvent) {
				if (inEvent.getSource() == keyController.getSchemeModel()
		                && CommonModel.PROP_SELECTED_ELEMENT
		                        .equals(inEvent.getProperty())) {
					final Object lNewVal = inEvent.getNewValue();
					final StructuredSelection structuredSelection = lNewVal == null
		                    ? null : new StructuredSelection(lNewVal);
					schemeCombo.setSelection(structuredSelection, true);
				}
			}
		};
		keyController.addPropertyChangeListener(lListener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (keyController.initialized()) {
			keyController.saveBindings();
		}
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		final String lTitle = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxText;
		final String lMessage = NewKeysPreferenceMessages.RestoreDefaultsMessageBoxMessage;
		final boolean lConfirm = MessageDialog.open(MessageDialog.CONFIRM,
		        getShell(), lTitle, lMessage, SWT.SHEET);
		if (lConfirm) {
			filteredTree.setRedraw(false);
			BusyIndicator.showWhile(
			        filteredTree.getViewer().getTree().getDisplay(),
			        new Runnable() {
				        @Override
				        public void run() {
					        keyController.setDefaultBindings();
				        }
			        });
			filteredTree.setRedraw(true);
		}

		super.performDefaults();
	}

	// --- inner classes ---

	/**
	 * A FilteredTree that provides a combo which is used to organize and
	 * display elements in the tree according to the selected criteria.
	 *
	 */
	protected class CategoryFilterTree extends FilteredTree {
		private final CategoryPatternFilter filter;

		/**
		 * Constructor for PatternFilteredTree.
		 *
		 * @param inParent
		 * @param treeStyle
		 * @param inFilter
		 */
		protected CategoryFilterTree(final Composite inParent,
		        final int treeStyle, final CategoryPatternFilter inFilter) {
			super(inParent, treeStyle, inFilter, true);
			filter = inFilter;
		}

		public void filterCategories(final boolean inFilterCategories) {
			filter.filterCategories(inFilterCategories);
			textChanged();
		}

		public boolean isFilteringCategories() {
			return filter.isFilteringCategories();
		}
	}

	/**
	 * Comparator.
	 */
	private final class BindingModelComparator extends ViewerComparator {
		private final LinkedList<Integer> sortColumns = new LinkedList<Integer>();
		private boolean ascending = true;

		public BindingModelComparator() {
			for (int i = 0; i < NUM_OF_COLUMNS; i++) {
				sortColumns.add(new Integer(i));
			}
		}

		public int getSortColumn() {
			return sortColumns.getFirst().intValue();
		}

		public void setSortColumn(final int inColumn) {
			if (inColumn == getSortColumn()) {
				return;
			}
			final Integer lSortColumn = new Integer(inColumn);
			sortColumns.remove(lSortColumn);
			sortColumns.addFirst(lSortColumn);
		}

		/**
		 * @return Returns the ascending.
		 */
		public boolean isAscending() {
			return ascending;
		}

		/**
		 * @param ascending
		 *            The ascending to set.
		 */
		public void setAscending(final boolean ascending) {
			this.ascending = ascending;
		}

		@Override
		public final int compare(final Viewer inViewer, final Object inA,
		        final Object inB) {
			int lResult = 0;
			final Iterator<Integer> lIterator = sortColumns.iterator();
			while (lIterator.hasNext() && lResult == 0) {
				final int lColumn = lIterator.next().intValue();
				lResult = compareColumn(inViewer, inA, inB, lColumn);
			}
			return ascending ? lResult : (-1) * lResult;
		}

		@SuppressWarnings("unchecked")
		private int compareColumn(final Viewer inViewer, final Object inA,
		        final Object inB, final int inColumnNumber) {
			if (inColumnNumber == USER_DELTA_COLUMN) {
				return sortUser(inA, inB);
			}
			final IBaseLabelProvider lBaseLabel = ((TreeViewer) inViewer)
			        .getLabelProvider();
			if (lBaseLabel instanceof ITableLabelProvider) {
				final ITableLabelProvider lTableProvider = (ITableLabelProvider) lBaseLabel;
				final String lProvider1 = lTableProvider.getColumnText(inA,
				        inColumnNumber);
				final String lProvider2 = lTableProvider.getColumnText(inB,
				        inColumnNumber);
				if (lProvider1 != null && lProvider2 != null) {
					return getComparator().compare(lProvider1, lProvider2);
				}
			}
			return 0;
		}

		private int sortUser(final Object inA, final Object inB) {
			final int lTypeA = ((BindingElement) inA).getUserDelta().intValue();
			final int lTypeB = ((BindingElement) inB).getUserDelta().intValue();
			final int outResult = lTypeA - lTypeB;
			return outResult;
		}
	}

	/**
	 * Resort column helper.
	 */
	private final class ResortColumn extends SelectionAdapter {
		private final BindingModelComparator comparator;
		private final TreeColumn treeColumn;
		private final TreeViewer viewer;
		private final int column;

		private ResortColumn(final BindingModelComparator inComparator,
		        final TreeColumn inTreeColumn, final TreeViewer inViewer,
		        final int column) {
			comparator = inComparator;
			treeColumn = inTreeColumn;
			viewer = inViewer;
			this.column = column;
		}

		@Override
		public void widgetSelected(final SelectionEvent inEvent) {
			if (comparator.getSortColumn() == column) {
				comparator.setAscending(!comparator.isAscending());
				viewer.getTree().setSortDirection(
				        comparator.isAscending() ? SWT.UP : SWT.DOWN);
			} else {
				viewer.getTree().setSortColumn(treeColumn);
				comparator.setSortColumn(column);
			}
			try {
				viewer.getTree().setRedraw(false);
				viewer.refresh();
			}
			finally {
				viewer.getTree().setRedraw(true);
			}
		}
	}

	/**
	 * Helper class.
	 */
	private class BindingElementLabelProvider extends LabelProvider
	        implements ITableLabelProvider {
		/**
		 * A resource manager for this preference page.
		 */
		private final LocalResourceManager localResourceManager = new LocalResourceManager(
		        JFaceResources.getResources());

		@Override
		public final void dispose() {
			super.dispose();
			localResourceManager.dispose();
		}

		@Override
		public String getText(final Object inElement) {
			String lText = getColumnText(inElement, 0);
			if (lText == null) {
				lText = super.getText(inElement);
			}
			final StringBuilder outText = new StringBuilder(
			        lText == null ? "" : lText); //$NON-NLS-1$
			for (int i = 1; i < USER_DELTA_COLUMN; i++) {
				final String text = getColumnText(inElement, i);
				if (text != null) {
					outText.append(' ');
					outText.append(text);
				}
			}
			return outText.toString();
		}

		@Override
		public String getColumnText(final Object inElement, final int inIndex) {
			final BindingElement bindingElement = ((BindingElement) inElement);
			switch (inIndex) {
			case COMMAND_NAME_COLUMN: // name
				return bindingElement.getName();
			case KEY_SEQUENCE_COLUMN: // keys
				final TriggerSequence lSequence = bindingElement.getTrigger();
				return lSequence == null ? ZERO_LENGTH_STRING
				        : lSequence.format();
			case CONTEXT_COLUMN: // when
				final ModelElement lContext = bindingElement.getContext();
				return lContext == null ? ZERO_LENGTH_STRING
				        : lContext.getName();
			case CATEGORY_COLUMN: // category
				return bindingElement.getCategory();
			case USER_DELTA_COLUMN: // user
				if (bindingElement.getUserDelta().intValue() == Binding.USER) {
					if (bindingElement.getConflict().equals(Boolean.TRUE)) {
						return "CU"; //$NON-NLS-1$
					}
					return " U"; //$NON-NLS-1$
				}
				if (bindingElement.getConflict().equals(Boolean.TRUE)) {
					return "C "; //$NON-NLS-1$
				}
				return "  "; //$NON-NLS-1$
			}
			return null;
		}

		@Override
		public Image getColumnImage(final Object inElement, final int inIndex) {
			switch (inIndex) {
			case COMMAND_NAME_COLUMN:
				// TODO
				// final String lCommandId = lBinding.getId();
				// final ImageDescriptor lImageDescriptor = null;
				// final ImageDescriptor lImageDescriptor = commandImageService
				// .getImageDescriptor(lCommandId);
				// if (lImageDescriptor == null) {
				// return null;
				// }
				// try {
				// return localResourceManager.createImage(lImageDescriptor);
				// }
				// catch (final DeviceResourceException exc) {
				// final String lMessage = "Problem retrieving image for a
				// command '" //$NON-NLS-1$
				// + lCommandId + '\'';
				// final IStatus lStatus = new Status(IStatus.ERROR,
				// Activator.getSymbolicName(), 0, lMessage, exc);
				// log.error(exc, exc.getMessage());
				// }
				return null;
			}
			return null;
		}
	}

	/**
	 * Helper class.
	 */
	class ModelContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getChildren(final Object inParentElement) {
			if (inParentElement instanceof BindingModel) {
				return ((BindingModel) inParentElement).getBindings().toArray();
			}
			if (inParentElement instanceof ContextModel) {
				return ((ContextModel) inParentElement).getContexts().toArray();
			}
			if (inParentElement instanceof SchemeModel) {
				return ((SchemeModel) inParentElement).getSchemes().toArray();
			}
			return new Object[0];
		}

		@Override
		public Object getParent(final Object inElement) {
			return ((ModelElement) inElement).getParent();
		}

		@Override
		public boolean hasChildren(final Object inElement) {
			return (inElement instanceof BindingModel)
			        || (inElement instanceof ContextModel)
			        || (inElement instanceof SchemeModel);
		}

		@Override
		public Object[] getElements(final Object inInputElement) {
			return getChildren(inInputElement);
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(final Viewer inViewer, final Object oldInput,
		        final Object newInput) {
		}
	}

	private static class ListLabelProvider extends LabelProvider {
		@Override
		public String getText(final Object inElement) {
			return ((ModelElement) inElement).getName();
		}
	}

}
