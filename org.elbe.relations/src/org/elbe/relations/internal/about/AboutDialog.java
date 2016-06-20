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
package org.elbe.relations.internal.about;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ProductProperties;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.about.AboutBundleGroupData;
import org.eclipse.ui.internal.about.AboutFeaturesButtonManager;
import org.eclipse.ui.internal.about.AboutItem;
import org.eclipse.ui.internal.about.AboutTextManager;
import org.eclipse.ui.internal.about.InstallationDialog;

/**
 * Displays information about the product.
 *
 * @author lbenno <br />
 *         see org.eclipse.ui.internal.dialogs.AboutDialog
 */
public class AboutDialog extends TrayDialog {
	private final static int MAX_IMAGE_WIDTH_FOR_TEXT = 250;
	private final static int DETAILS_ID = IDialogConstants.CLIENT_ID + 1;

	private final IProduct product;
	private String productName;
	private final ArrayList<Image> images = new ArrayList<Image>();
	private StyledText text;
	private final AboutBundleGroupData[] bundleGroupInfos;
	private final AboutFeaturesButtonManager buttonManager = new AboutFeaturesButtonManager();

	/**
	 * @param shell
	 */
	public AboutDialog(Shell shell) {
		super(shell);

		product = Platform.getProduct();
		if (product != null) {
			productName = product.getName();
		}
		if (productName == null) {
			productName = WorkbenchMessages.AboutDialog_defaultProductName;
		}

		// create a descriptive object for each BundleGroup
		final IBundleGroupProvider[] providers = Platform
		        .getBundleGroupProviders();
		final LinkedList<AboutBundleGroupData> groups = new LinkedList<AboutBundleGroupData>();
		if (providers != null) {
			for (final IBundleGroupProvider provider : providers) {
				final IBundleGroup[] bundleGroups = provider.getBundleGroups();
				for (final IBundleGroup bundleGroup : bundleGroups) {
					groups.add(new AboutBundleGroupData(bundleGroup));
				}
			}
		}
		bundleGroupInfos = groups.toArray(new AboutBundleGroupData[0]);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case DETAILS_ID:
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				@Override
				public void run() {
					final InstallationDialog dialog = new InstallationDialog(
			                getShell(),
			                RelationsServiceLocator.createServiceLocator(null));
					dialog.setModalParent(AboutDialog.this);
					dialog.open();
				}
			});
			break;
		default:
			super.buttonPressed(buttonId);
			break;
		}
	}

	@Override
	public boolean close() {
		// dispose all images
		for (int i = 0; i < images.size(); ++i) {
			final Image image = images.get(i);
			image.dispose();
		}

		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(NLS.bind(WorkbenchMessages.AboutDialog_shellTitle,
		        productName));
	}

	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * Subclasses should override.
	 *
	 * @param parent
	 *            the button bar composite
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButton(parent, DETAILS_ID,
		        WorkbenchMessages.AboutDialog_DetailsButton, false);

		final Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;

		final Button b = createButton(parent, IDialogConstants.OK_ID,
		        IDialogConstants.OK_LABEL, true);
		b.setFocus();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// brand the about box if there is product info
		Image aboutImage = null;
		AboutItem item = null;
		if (product != null) {
			final ImageDescriptor imageDescriptor = ProductProperties
			        .getAboutImage(product);
			if (imageDescriptor != null) {
				aboutImage = imageDescriptor.createImage();
			}

			// if the about image is small enough, then show the text
			if (aboutImage == null || aboutImage
			        .getBounds().width <= MAX_IMAGE_WIDTH_FOR_TEXT) {
				final String aboutText = ProductProperties
				        .getAboutText(product);
				if (aboutText != null) {
					item = AboutTextManager.scan(aboutText);
				}
			}

			if (aboutImage != null) {
				images.add(aboutImage);
			}
		}

		// create a composite which is the parent of the top area and the bottom
		// button bar, this allows there to be a second child of this composite
		// with
		// a banner background on top but not have on the bottom
		final Composite workArea = new Composite(parent, SWT.NONE);
		final GridLayout workLayout = new GridLayout();
		workLayout.marginHeight = 0;
		workLayout.marginWidth = 0;
		workLayout.verticalSpacing = 0;
		workLayout.horizontalSpacing = 0;
		workArea.setLayout(workLayout);
		workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

		// page group
		final Color background = JFaceColors
		        .getBannerBackground(parent.getDisplay());
		final Color foreground = JFaceColors
		        .getBannerForeground(parent.getDisplay());
		final Composite top = (Composite) super.createDialogArea(workArea);

		// override any layout inherited from createDialogArea
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		top.setBackground(background);
		top.setForeground(foreground);

		// the image & text
		final Composite topContainer = new Composite(top, SWT.NONE);
		topContainer.setBackground(background);
		topContainer.setForeground(foreground);

		layout = new GridLayout();
		layout.numColumns = (aboutImage == null || item == null ? 1 : 2);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		topContainer.setLayout(layout);

		final GC gc = new GC(parent);
		// arbitrary default
		int topContainerHeightHint = 100;
		try {
			// default height enough for 6 lines of text
			topContainerHeightHint = Math.max(topContainerHeightHint,
			        gc.getFontMetrics().getHeight() * 6);
		}
		finally {
			gc.dispose();
		}

		// image on left side of dialog
		if (aboutImage != null) {
			final Label imageLabel = new Label(topContainer, SWT.NONE);
			imageLabel.setBackground(background);
			imageLabel.setForeground(foreground);

			final GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			data.verticalAlignment = GridData.BEGINNING;
			data.grabExcessHorizontalSpace = false;
			imageLabel.setLayoutData(data);
			imageLabel.setImage(aboutImage);
			topContainerHeightHint = Math.max(topContainerHeightHint,
			        aboutImage.getBounds().height);
		}

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = topContainerHeightHint;
		topContainer.setLayoutData(data);

		if (item != null) {
			final int minWidth = 400; // This value should really be calculated
			// from the computeSize(SWT.DEFAULT,
			// SWT.DEFAULT) of all the
			// children in infoArea excluding the
			// wrapped styled text
			// There is no easy way to do this.
			final ScrolledComposite scroller = new ScrolledComposite(
			        topContainer, SWT.V_SCROLL | SWT.H_SCROLL);
			data = new GridData(GridData.FILL_BOTH);
			data.widthHint = minWidth;
			scroller.setLayoutData(data);

			final Composite textComposite = new Composite(scroller, SWT.NONE);
			textComposite.setBackground(background);

			layout = new GridLayout();
			textComposite.setLayout(layout);

			text = new StyledText(textComposite,
			        SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);

			// Don't set caret to 'null' as this causes
			// https://bugs.eclipse.org/293263.
			// text.setCaret(null);

			text.setFont(parent.getFont());
			text.setText(item.getText());
			text.setCursor(null);
			text.setBackground(background);
			text.setForeground(foreground);

			// aboutTextManager = new AboutTextManager(text);
			// aboutTextManager.setItem(item);

			createTextMenu();

			final GridData gd = new GridData();
			gd.verticalAlignment = GridData.BEGINNING;
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			text.setLayoutData(gd);

			// Adjust the scrollbar increments
			scroller.getHorizontalBar().setIncrement(20);
			scroller.getVerticalBar().setIncrement(20);

			final boolean[] inresize = new boolean[1]; // flag to stop
			                                           // unneccesary
			// recursion
			textComposite.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					if (inresize[0]) {
						return;
					}
					inresize[0] = true;
					// required because of bugzilla report 4579
					textComposite.layout(true);
					// required because you want to change the height that the
			        // scrollbar will scroll over when the width changes.
					final int width = textComposite.getClientArea().width;
					final Point p = textComposite.computeSize(width,
			                SWT.DEFAULT);
					scroller.setMinSize(minWidth, p.y);
					inresize[0] = false;
				}
			});

			scroller.setExpandHorizontal(true);
			scroller.setExpandVertical(true);
			final Point p = textComposite.computeSize(minWidth, SWT.DEFAULT);
			textComposite.setSize(p.x, p.y);
			scroller.setMinWidth(minWidth);
			scroller.setMinHeight(p.y);

			scroller.setContent(textComposite);
		}

		// horizontal bar
		Label bar = new Label(workArea, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		bar.setLayoutData(data);

		// add image buttons for bundle groups that have them
		final Composite bottom = (Composite) super.createDialogArea(workArea);
		// override any layout inherited from createDialogArea
		layout = new GridLayout();
		bottom.setLayout(layout);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		data.grabExcessHorizontalSpace = true;

		bottom.setLayoutData(data);

		createFeatureImageButtonRow(bottom);

		// spacer
		bar = new Label(bottom, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		bar.setLayoutData(data);

		return workArea;
	}

	/**
	 * Create the context menu for the text widget.
	 *
	 * @since 3.4
	 */
	private void createTextMenu() {
		// final MenuManager textManager = new MenuManager();
		// textManager.add(new CommandContributionItem(
		// new CommandContributionItemParameter(PlatformUI.getWorkbench(),
		// null, IWorkbenchCommandConstants.EDIT_COPY,
		// CommandContributionItem.STYLE_PUSH)));
		// textManager.add(new CommandContributionItem(
		// new CommandContributionItemParameter(PlatformUI.getWorkbench(),
		// null, IWorkbenchCommandConstants.EDIT_SELECT_ALL,
		// CommandContributionItem.STYLE_PUSH)));
		// text.setMenu(textManager.createContextMenu(text));
		// text.addDisposeListener(new DisposeListener() {
		//
		// @Override
		// public void widgetDisposed(DisposeEvent e) {
		// textManager.dispose();
		// }
		// });

	}

	private void createFeatureImageButtonRow(Composite parent) {
		final Composite featureContainer = new Composite(parent, SWT.NONE);
		final RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = true;
		featureContainer.setLayout(rowLayout);
		final GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		featureContainer.setLayoutData(data);

		for (final AboutBundleGroupData bundleGroupInfo : bundleGroupInfos) {
			createFeatureButton(featureContainer, bundleGroupInfo);
		}
	}

	private Button createFeatureButton(Composite parent,
	        final AboutBundleGroupData info) {
		if (!buttonManager.add(info)) {
			return null;
		}

		final ImageDescriptor desc = info.getFeatureImage();
		Image featureImage = null;

		final Button button = new Button(parent, SWT.FLAT | SWT.PUSH);
		button.setData(info);
		featureImage = desc.createImage();
		images.add(featureImage);
		button.setImage(featureImage);
		button.setToolTipText(info.getProviderName());

		button.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = info.getProviderName();
			}
		});
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				final AboutBundleGroupData[] groupInfos = buttonManager
		                .getRelatedInfos(info);
				final AboutBundleGroupData selection = (AboutBundleGroupData) event.widget
		                .getData();

				final AboutFeaturesDialog d = new AboutFeaturesDialog(
		                getShell(), productName, groupInfos, selection);
				d.open();
			}
		});

		return button;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

}
