package org.elbe.relations.internal.about.pages;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Abstract superclass of about dialog installation pages. The ProductInfoPage
 * is set up so that the page can be hosted as one of many pages in the
 * InstallationDialog, or as the only page in a ProductInfoDialog.
 */

public abstract class ProductInfoPage extends InstallationPage
        implements IShellProvider {

	private IProduct product;

	private String productName;

	protected IProduct getProduct() {
		if (product == null) {
			product = Platform.getProduct();
		}
		return product;
	}

	public String getProductName() {
		if (productName == null) {
			if (getProduct() != null) {
				productName = getProduct().getName();
			}
			if (productName == null) {
				productName = WorkbenchMessages.AboutDialog_defaultProductName;
			}
		}
		return productName;
	}

	public void setProductName(String name) {
		productName = name;
	}

	public abstract String getId();

	protected Composite createOuterComposite(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		return composite;
	}
}
