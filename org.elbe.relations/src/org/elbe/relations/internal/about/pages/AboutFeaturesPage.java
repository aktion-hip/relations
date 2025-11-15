package org.elbe.relations.internal.about.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

    private final Map<ImageDescriptor, Image> cachedImages = new HashMap<>();

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

    private static Map<String, String> featuresMap;

    public void setBundleGroupInfos(final AboutBundleGroupData[] bundleGroupInfos) {
        this.bundleGroupInfos = bundleGroupInfos;
    }

    @Override
    public String getId() {
        return ID;
    }

    private void initializeBundleGroupInfos() {
        if (this.bundleGroupInfos == null) {
            final IBundleGroupProvider[] providers = Platform
                    .getBundleGroupProviders();

            // create a descriptive object for each BundleGroup
            final LinkedList<AboutData> groups = new LinkedList<>();
            if (providers != null) {
                for (int i = 0; i < providers.length; ++i) {
                    final IBundleGroup[] bundleGroups = providers[i]
                            .getBundleGroups();
                    for (int j = 0; j < bundleGroups.length; ++j) {
                        groups.add(new AboutBundleGroupData(bundleGroups[j]));
                    }
                }
            }
            this.bundleGroupInfos = groups
                    .toArray(new AboutBundleGroupData[0]);
        } else {
            // the order of the array may be changed due to sorting, so create a
            // copy, since the client set this value.
            final AboutBundleGroupData[] clientArray = this.bundleGroupInfos;
            this.bundleGroupInfos = new AboutBundleGroupData[clientArray.length];
            System.arraycopy(clientArray, 0, this.bundleGroupInfos, 0,
                    clientArray.length);
        }
        AboutData.sortByProvider(this.reverseSort, this.bundleGroupInfos);
    }

    /**
     * The Plugins button was pressed. Open an about dialog on the plugins for
     * the selected feature.
     */
    private void handlePluginInfoPressed() {
        final TableItem[] items = this.table.getSelection();
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
    public void createPageButtons(final Composite parent) {
        this.moreButton = createButton(parent, MORE_ID,
                WorkbenchMessages.AboutFeaturesDialog_moreInfo);
        this.pluginsButton = createButton(parent, PLUGINS_ID,
                WorkbenchMessages.AboutFeaturesDialog_pluginsInfo);
        createButton(parent, COLUMNS_ID,
                WorkbenchMessages.AboutFeaturesDialog_columns);
        final TableItem[] items = this.table.getSelection();
        if (items.length > 0) {
            updateButtons((AboutBundleGroupData) items[0].getData());
        }
    }

    @Override
    public void createControl(final Composite parent) {
        initializeDialogUnits(parent);
        parent.getShell().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent arg0) {
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
    protected void createInfoArea(final Composite parent) {
        final Font font = parent.getFont();

        this.infoArea = new Composite(parent, SWT.BORDER);
        this.infoArea.setBackground(this.infoArea.getDisplay()
                .getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        this.infoArea.setBackgroundMode(SWT.INHERIT_FORCE);
        GridData data = new GridData(GridData.FILL, GridData.FILL, true, false);
        // need to provide space for arbitrary feature infos, not just the
        // one selected by default
        data.heightHint = convertVerticalDLUsToPixels(INFO_HEIGHT);
        this.infoArea.setLayoutData(data);

        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        this.infoArea.setLayout(layout);

        this.imageLabel = new Label(this.infoArea, SWT.NONE);
        data = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
        data.widthHint = 32;
        data.heightHint = 32;
        this.imageLabel.setLayoutData(data);
        this.imageLabel.setFont(font);

        // text on the right
        this.text = new StyledText(this.infoArea,
                SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
        this.text.setAlwaysShowScrollBars(false);

        // Don't set caret to 'null' as this causes
        // https://bugs.eclipse.org/293263.
        // text.setCaret(null);

        this.text.setFont(parent.getFont());
        data = new GridData(GridData.FILL, GridData.FILL, true, true);
        this.text.setLayoutData(data);
        this.text.setFont(font);
        this.text.setCursor(null);

        this.textManager = new AboutTextManager(this.text);

        final TableItem[] items = this.table.getSelection();
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
    protected void createTable(final Composite parent) {

        initializeBundleGroupInfos();

        this.table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.BORDER);

        final GridData gridData = new GridData(GridData.FILL, GridData.FILL,
                true, true);
        gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
        this.table.setLayoutData(gridData);
        this.table.setHeaderVisible(true);

        this.table.setLinesVisible(true);
        this.table.setFont(parent.getFont());
        this.table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
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

        for (int i = 0; i < this.columnTitles.length; i++) {
            final TableColumn tableColumn = new TableColumn(this.table, SWT.NULL);
            tableColumn.setWidth(columnWidths[i]);
            tableColumn.setText(this.columnTitles[i]);
            final int columnIndex = i;
            tableColumn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    sort(columnIndex);
                }
            });
        }

        // create a table row for each bundle group
        final String selId = this.lastSelection == null ? null
                : this.lastSelection.getId();
        int sel = 0;
        for (int i = 0; i < this.bundleGroupInfos.length; i++) {
            if (this.bundleGroupInfos[i].getId().equals(selId)) {
                sel = i;
            }

            final TableItem item = new TableItem(this.table, SWT.NULL);
            item.setText(createRow(this.bundleGroupInfos[i]));
            item.setData(this.bundleGroupInfos[i]);
        }

        // if an item was specified during construction, it should be
        // selected when the table is created
        if (this.bundleGroupInfos.length > 0) {
            this.table.setSelection(sel);
            this.table.showSelection();
        }
    }

    private void disposeImages() {
        final Iterator<Image> iter = this.cachedImages.values().iterator();
        while (iter.hasNext()) {
            final Image image = iter.next();
            image.dispose();
        }
    }

    /**
     * Update the button enablement
     */
    private void updateButtons(final AboutBundleGroupData info) {
        if (info == null) {
            this.moreButton.setEnabled(false);
            this.pluginsButton.setEnabled(false);
            return;
        }

        // Creating the feature map is too much just to determine enablement, so
        // if
        // it doesn't already exist, just enable the buttons. If this was the
        // wrong
        // choice, then when the button is actually pressed an dialog will be
        // opened.
        if (featuresMap == null) {
            this.moreButton.setEnabled(true);
            this.pluginsButton.setEnabled(true);
            return;
        }

        this.moreButton.setEnabled(info.getLicenseUrl() != null);
        this.pluginsButton.setEnabled(true);
    }

    /**
     * Update the info area
     */
    private void updateInfoArea(final AboutBundleGroupData info) {
        if (info == null) {
            this.imageLabel.setImage(null);
            this.text.setText(""); //$NON-NLS-1$
            return;
        }

        final ImageDescriptor desc = info.getFeatureImage();
        Image image = this.cachedImages.get(desc);
        if (image == null && desc != null) {
            image = desc.createImage();
            this.cachedImages.put(desc, image);
        }
        this.imageLabel.setImage(image);

        final String aboutText = info.getAboutText();
        this.textManager.setItem(null);
        if (aboutText != null) {
            this.textManager.setItem(AboutUtils.scan(aboutText));
        }

        if (this.textManager.getItem() == null) {
            this.text.setText(WorkbenchMessages.AboutFeaturesDialog_noInformation);
        }
    }

    /**
     * Select the initial selection
     *
     * @param info
     *            the info
     */
    public void setInitialSelection(final AboutBundleGroupData info) {
        this.lastSelection = info;
    }

    /**
     * Sort the rows of the table based on the selected column.
     *
     * @param column
     *            index of table column selected as sort criteria
     */
    private void sort(final int column) {
        if (this.lastColumnChosen == column) {
            this.reverseSort = !this.reverseSort;
        } else {
            this.reverseSort = false;
            this.lastColumnChosen = column;
        }

        if (this.table.getItemCount() <= 1) {
            return;
        }

        // Remember the last selection
        final int sel = this.table.getSelectionIndex();
        if (sel != -1) {
            this.lastSelection = this.bundleGroupInfos[sel];
        }

        switch (column) {
            case 0:
                AboutData.sortByProvider(this.reverseSort, this.bundleGroupInfos);
                break;
            case 1:
                AboutData.sortByName(this.reverseSort, this.bundleGroupInfos);
                break;
            case 2:
                AboutData.sortByVersion(this.reverseSort, this.bundleGroupInfos);
                break;
            case 3:
                AboutData.sortById(this.reverseSort, this.bundleGroupInfos);
                break;
        }
        // set the sort column and directional indicator
        this.table.setSortColumn(this.table.getColumn(column));
        this.table.setSortDirection(this.reverseSort ? SWT.DOWN : SWT.UP);

        refreshTable();
    }

    /**
     * Refresh the rows of the table based on the selected column. Maintain
     * selection from before sort action request.
     */
    private void refreshTable() {
        final TableItem[] items = this.table.getItems();

        // create new order of table items
        for (int i = 0; i < items.length; i++) {
            items[i].setText(createRow(this.bundleGroupInfos[i]));
            items[i].setData(this.bundleGroupInfos[i]);
        }

        // Maintain the original selection
        int sel = -1;
        if (this.lastSelection != null) {
            final String oldId = this.lastSelection.getId();
            for (int k = 0; k < this.bundleGroupInfos.length; k++) {
                if (oldId.equalsIgnoreCase(this.bundleGroupInfos[k].getId())) {
                    sel = k;
                }
            }

            this.table.setSelection(sel);
            this.table.showSelection();
        }

        updateInfoArea(this.lastSelection);
    }

    /**
     * Return an array of strings containing the argument's information in the
     * proper order for this table's columns.
     *
     * @param info
     *            the source information for the new row, must not be null
     */
    private static String[] createRow(final AboutBundleGroupData info) {
        return new String[] { info.getProviderName(), info.getName(),
                info.getVersion(), info.getId() };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.internal.about.TableListPage#getSelectionValue()
     */
    protected Collection<Object> getSelectionValue() {
        if (this.table == null || this.table.isDisposed()) {
            return null;
        }
        final TableItem[] items = this.table.getSelection();
        if (items.length <= 0) {
            return null;
        }
        final List<Object> list = new ArrayList<>(1);
        list.add(items[0].getData());
        return list;
    }

    private void handleColumnsPressed() {
        ConfigureColumns.forTable(this.table, this);
    }

    /**
     * The More Info button was pressed. Open a browser with the license for the
     * selected item or an information dialog if there is no license, or the
     * browser cannot be opened.
     */
    private void handleMoreInfoPressed() {
        final TableItem[] items = this.table.getSelection();
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
    protected void buttonPressed(final int buttonId) {
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
