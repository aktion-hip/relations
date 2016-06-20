package org.elbe.relations.internal.about.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ConfigureColumns;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.about.AboutBundleGroupData;
import org.eclipse.ui.internal.about.AboutData;
import org.eclipse.ui.internal.about.AboutTextManager;
import org.eclipse.ui.internal.about.AboutUtils;
import org.eclipse.ui.internal.dialogs.AboutPluginsDialog;
import org.osgi.framework.Bundle;

/**
 * Displays information about the product plugins.
 *
 * PRIVATE This class is internal to the workbench and must not be called
 * outside the workbench.
 */
public class AboutFeaturesPage extends ProductInfoPage {

	// used as the page id when this page is launched in its own dialog
	private static final String ID = "productInfo.features"; //$NON-NLS-1$
	/**
	 * Table height in dialog units (value 150).
	 */
	private static final int TABLE_HEIGHT = 150;

	private static final int INFO_HEIGHT = 100;

	private final static int MORE_ID = IDialogConstants.CLIENT_ID + 1;

	private final static int PLUGINS_ID = IDialogConstants.CLIENT_ID + 2;

	private final static int COLUMNS_ID = IDialogConstants.CLIENT_ID + 3;

	private Table table;

	private Label imageLabel;

	private StyledText text;

	private AboutTextManager textManager;

	private Composite infoArea;

	private final Map cachedImages = new HashMap();

	private AboutBundleGroupData[] bundleGroupInfos;

	private final String columnTitles[] = {
	        WorkbenchMessages.AboutFeaturesDialog_provider,
	        WorkbenchMessages.AboutFeaturesDialog_featureName,
	        WorkbenchMessages.AboutFeaturesDialog_version,
	        WorkbenchMessages.AboutFeaturesDialog_featureId, };

	private int lastColumnChosen = 0; // initially sort by provider

	private boolean reverseSort = false; // initially sort ascending

	private AboutBundleGroupData lastSelection = null;

	private Button pluginsButton, moreButton;

	private static Map featuresMap;

	public void setBundleGroupInfos(AboutBundleGroupData[] bundleGroupInfos) {
		this.bundleGroupInfos = bundleGroupInfos;
	}

	@Override
	public String getId() {
		return ID;
	}

	private void initializeBundleGroupInfos() {
		if (bundleGroupInfos == null) {
			final IBundleGroupProvider[] providers = Platform
			        .getBundleGroupProviders();

			// create a descriptive object for each BundleGroup
			final LinkedList groups = new LinkedList();
			if (providers != null) {
				for (int i = 0; i < providers.length; ++i) {
					final IBundleGroup[] bundleGroups = providers[i]
					        .getBundleGroups();
					for (int j = 0; j < bundleGroups.length; ++j) {
						groups.add(new AboutBundleGroupData(bundleGroups[j]));
					}
				}
			}
			bundleGroupInfos = (AboutBundleGroupData[]) groups
			        .toArray(new AboutBundleGroupData[0]);
		} else {
			// the order of the array may be changed due to sorting, so create a
			// copy, since the client set this value.
			final AboutBundleGroupData[] clientArray = bundleGroupInfos;
			bundleGroupInfos = new AboutBundleGroupData[clientArray.length];
			System.arraycopy(clientArray, 0, bundleGroupInfos, 0,
			        clientArray.length);
		}
		AboutData.sortByProvider(reverseSort, bundleGroupInfos);
	}

	/**
	 * The Plugins button was pressed. Open an about dialog on the plugins for
	 * the selected feature.
	 */
	private void handlePluginInfoPressed() {
		final TableItem[] items = table.getSelection();
		if (items.length <= 0) {
			return;
		}

		final AboutBundleGroupData info = (AboutBundleGroupData) items[0]
		        .getData();
		final IBundleGroup bundleGroup = info.getBundleGroup();
		final Bundle[] bundles = bundleGroup == null ? new Bundle[0]
		        : bundleGroup.getBundles();

		final AboutPluginsDialog d = new AboutPluginsDialog(getShell(),
		        getProductName(), bundles,
		        WorkbenchMessages.AboutFeaturesDialog_pluginInfoTitle,
		        NLS.bind(
		                WorkbenchMessages.AboutFeaturesDialog_pluginInfoMessage,
		                bundleGroup.getIdentifier()),
		        IWorkbenchHelpContextIds.ABOUT_FEATURES_PLUGINS_DIALOG);
		d.open();
	}

	@Override
	public void createPageButtons(Composite parent) {
		moreButton = createButton(parent, MORE_ID,
		        WorkbenchMessages.AboutFeaturesDialog_moreInfo);
		pluginsButton = createButton(parent, PLUGINS_ID,
		        WorkbenchMessages.AboutFeaturesDialog_pluginsInfo);
		createButton(parent, COLUMNS_ID,
		        WorkbenchMessages.AboutFeaturesDialog_columns);
		final TableItem[] items = table.getSelection();
		if (items.length > 0) {
			updateButtons((AboutBundleGroupData) items[0].getData());
		}
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		parent.getShell().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				disposeImages();
			}
		});
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
		// IWorkbenchHelpContextIds.ABOUT_FEATURES_DIALOG);

		final Composite outer = createOuterComposite(parent);

		createTable(outer);
		createInfoArea(outer);
		setControl(outer);
	}

	/**
	 * Create the info area containing the image and text
	 */
	protected void createInfoArea(Composite parent) {
		final Font font = parent.getFont();

		infoArea = new Composite(parent, SWT.BORDER);
		infoArea.setBackground(infoArea.getDisplay()
		        .getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		infoArea.setBackgroundMode(SWT.INHERIT_FORCE);
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false);
		// need to provide space for arbitrary feature infos, not just the
		// one selected by default
		data.heightHint = convertVerticalDLUsToPixels(INFO_HEIGHT);
		infoArea.setLayoutData(data);

		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		infoArea.setLayout(layout);

		imageLabel = new Label(infoArea, SWT.NONE);
		data = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		data.widthHint = 32;
		data.heightHint = 32;
		imageLabel.setLayoutData(data);
		imageLabel.setFont(font);

		// text on the right
		text = new StyledText(infoArea,
		        SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
		text.setAlwaysShowScrollBars(false);

		// Don't set caret to 'null' as this causes
		// https://bugs.eclipse.org/293263.
		// text.setCaret(null);

		text.setFont(parent.getFont());
		data = new GridData(GridData.FILL, GridData.FILL, true, true);
		text.setLayoutData(data);
		text.setFont(font);
		text.setCursor(null);

		textManager = new AboutTextManager(text);

		final TableItem[] items = table.getSelection();
		if (items.length > 0) {
			updateInfoArea((AboutBundleGroupData) items[0].getData());
		}
	}

	/**
	 * Create the table part of the dialog.
	 *
	 * @param parent
	 *            the parent composite to contain the dialog area
	 */
	protected void createTable(Composite parent) {

		initializeBundleGroupInfos();

		table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
		        | SWT.FULL_SELECTION | SWT.BORDER);

		final GridData gridData = new GridData(GridData.FILL, GridData.FILL,
		        true, true);
		gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);

		table.setLinesVisible(true);
		table.setFont(parent.getFont());
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// If there is no item, nothing we can do.
		        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=266177
				if (e.item == null) {
					return;
				}
				final AboutBundleGroupData info = (AboutBundleGroupData) e.item
		                .getData();
				updateInfoArea(info);
				updateButtons(info);
			}
		});

		final int[] columnWidths = { convertHorizontalDLUsToPixels(120),
		        convertHorizontalDLUsToPixels(120),
		        convertHorizontalDLUsToPixels(70),
		        convertHorizontalDLUsToPixels(130) };

		for (int i = 0; i < columnTitles.length; i++) {
			final TableColumn tableColumn = new TableColumn(table, SWT.NULL);
			tableColumn.setWidth(columnWidths[i]);
			tableColumn.setText(columnTitles[i]);
			final int columnIndex = i;
			tableColumn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sort(columnIndex);
				}
			});
		}

		// create a table row for each bundle group
		final String selId = lastSelection == null ? null
		        : lastSelection.getId();
		int sel = 0;
		for (int i = 0; i < bundleGroupInfos.length; i++) {
			if (bundleGroupInfos[i].getId().equals(selId)) {
				sel = i;
			}

			final TableItem item = new TableItem(table, SWT.NULL);
			item.setText(createRow(bundleGroupInfos[i]));
			item.setData(bundleGroupInfos[i]);
		}

		// if an item was specified during construction, it should be
		// selected when the table is created
		if (bundleGroupInfos.length > 0) {
			table.setSelection(sel);
			table.showSelection();
		}
	}

	private void disposeImages() {
		final Iterator iter = cachedImages.values().iterator();
		while (iter.hasNext()) {
			final Image image = (Image) iter.next();
			image.dispose();
		}
	}

	/**
	 * Update the button enablement
	 */
	private void updateButtons(AboutBundleGroupData info) {
		if (info == null) {
			moreButton.setEnabled(false);
			pluginsButton.setEnabled(false);
			return;
		}

		// Creating the feature map is too much just to determine enablement, so
		// if
		// it doesn't already exist, just enable the buttons. If this was the
		// wrong
		// choice, then when the button is actually pressed an dialog will be
		// opened.
		if (featuresMap == null) {
			moreButton.setEnabled(true);
			pluginsButton.setEnabled(true);
			return;
		}

		moreButton.setEnabled(info.getLicenseUrl() != null);
		pluginsButton.setEnabled(true);
	}

	/**
	 * Update the info area
	 */
	private void updateInfoArea(AboutBundleGroupData info) {
		if (info == null) {
			imageLabel.setImage(null);
			text.setText(""); //$NON-NLS-1$
			return;
		}

		final ImageDescriptor desc = info.getFeatureImage();
		Image image = (Image) cachedImages.get(desc);
		if (image == null && desc != null) {
			image = desc.createImage();
			cachedImages.put(desc, image);
		}
		imageLabel.setImage(image);

		final String aboutText = info.getAboutText();
		textManager.setItem(null);
		if (aboutText != null) {
			textManager.setItem(AboutUtils.scan(aboutText));
		}

		if (textManager.getItem() == null) {
			text.setText(WorkbenchMessages.AboutFeaturesDialog_noInformation);
		}
	}

	/**
	 * Select the initial selection
	 *
	 * @param info
	 *            the info
	 */
	public void setInitialSelection(AboutBundleGroupData info) {
		lastSelection = info;
	}

	/**
	 * Sort the rows of the table based on the selected column.
	 *
	 * @param column
	 *            index of table column selected as sort criteria
	 */
	private void sort(int column) {
		if (lastColumnChosen == column) {
			reverseSort = !reverseSort;
		} else {
			reverseSort = false;
			lastColumnChosen = column;
		}

		if (table.getItemCount() <= 1) {
			return;
		}

		// Remember the last selection
		final int sel = table.getSelectionIndex();
		if (sel != -1) {
			lastSelection = bundleGroupInfos[sel];
		}

		switch (column) {
		case 0:
			AboutData.sortByProvider(reverseSort, bundleGroupInfos);
			break;
		case 1:
			AboutData.sortByName(reverseSort, bundleGroupInfos);
			break;
		case 2:
			AboutData.sortByVersion(reverseSort, bundleGroupInfos);
			break;
		case 3:
			AboutData.sortById(reverseSort, bundleGroupInfos);
			break;
		}
		// set the sort column and directional indicator
		table.setSortColumn(table.getColumn(column));
		table.setSortDirection(reverseSort ? SWT.DOWN : SWT.UP);

		refreshTable();
	}

	/**
	 * Refresh the rows of the table based on the selected column. Maintain
	 * selection from before sort action request.
	 */
	private void refreshTable() {
		final TableItem[] items = table.getItems();

		// create new order of table items
		for (int i = 0; i < items.length; i++) {
			items[i].setText(createRow(bundleGroupInfos[i]));
			items[i].setData(bundleGroupInfos[i]);
		}

		// Maintain the original selection
		int sel = -1;
		if (lastSelection != null) {
			final String oldId = lastSelection.getId();
			for (int k = 0; k < bundleGroupInfos.length; k++) {
				if (oldId.equalsIgnoreCase(bundleGroupInfos[k].getId())) {
					sel = k;
				}
			}

			table.setSelection(sel);
			table.showSelection();
		}

		updateInfoArea(lastSelection);
	}

	/**
	 * Return an array of strings containing the argument's information in the
	 * proper order for this table's columns.
	 *
	 * @param info
	 *            the source information for the new row, must not be null
	 */
	private static String[] createRow(AboutBundleGroupData info) {
		return new String[] { info.getProviderName(), info.getName(),
		        info.getVersion(), info.getId() };
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.internal.about.TableListPage#getSelectionValue()
	 */
	protected Collection getSelectionValue() {
		if (table == null || table.isDisposed()) {
			return null;
		}
		final TableItem[] items = table.getSelection();
		if (items.length <= 0) {
			return null;
		}
		final ArrayList list = new ArrayList(1);
		list.add(items[0].getData());
		return list;
	}

	private void handleColumnsPressed() {
		ConfigureColumns.forTable(table, this);
	}

	/**
	 * The More Info button was pressed. Open a browser with the license for the
	 * selected item or an information dialog if there is no license, or the
	 * browser cannot be opened.
	 */
	private void handleMoreInfoPressed() {
		final TableItem[] items = table.getSelection();
		if (items.length <= 0) {
			return;
		}

		final AboutBundleGroupData info = (AboutBundleGroupData) items[0]
		        .getData();
		if (info == null
		        || !AboutUtils.openBrowser(getShell(), info.getLicenseUrl())) {
			MessageDialog.openInformation(getShell(),
			        WorkbenchMessages.AboutFeaturesDialog_noInfoTitle,
			        WorkbenchMessages.AboutFeaturesDialog_noInformation);
		}
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case MORE_ID:
			handleMoreInfoPressed();
			break;
		case PLUGINS_ID:
			handlePluginInfoPressed();
			break;
		case COLUMNS_ID:
			handleColumnsPressed();
			break;
		default:
			super.buttonPressed(buttonId);
			break;
		}
	}
}
