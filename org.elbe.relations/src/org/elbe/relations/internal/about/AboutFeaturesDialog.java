package org.elbe.relations.internal.about;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.about.AboutBundleGroupData;
import org.elbe.relations.internal.about.pages.AboutFeaturesPage;

/**
 * Displays information about the product plugins.
 *
 * PRIVATE This class is internal to the workbench and must not be called
 * outside the workbench.
 */
public class AboutFeaturesDialog extends ProductInfoDialog {
	/**
	 * Constructor for AboutFeaturesDialog.
	 *
	 * @param parentShell
	 *            the parent shell
	 * @param productName
	 *            the product name
	 * @param bundleGroupInfos
	 *            the bundle info
	 */
	public AboutFeaturesDialog(Shell parentShell, String productName,
	        AboutBundleGroupData[] bundleGroupInfos,
	        AboutBundleGroupData initialSelection) {
		super(parentShell);
		final AboutFeaturesPage page = new AboutFeaturesPage();
		page.setProductName(productName);
		page.setBundleGroupInfos(bundleGroupInfos);
		page.setInitialSelection(initialSelection);
		String title;
		if (productName != null) {
			title = NLS.bind(WorkbenchMessages.AboutFeaturesDialog_shellTitle,
			        productName);
		} else {
			title = WorkbenchMessages.AboutFeaturesDialog_SimpleTitle;
		}
		initializeDialog(page, title,
		        IWorkbenchHelpContextIds.ABOUT_FEATURES_DIALOG);
	}
}
