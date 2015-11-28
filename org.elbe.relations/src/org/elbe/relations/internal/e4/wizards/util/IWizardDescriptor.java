/**
 *
 */
package org.elbe.relations.internal.e4.wizards.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbenchPartDescriptor;

/**
 * Base interface for all wizards defined via workbench extension points.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @author lbenno<br />
 *         see org.eclipse.ui.wizards.IWizardDescriptor
 */
public interface IWizardDescriptor
        extends IWorkbenchPartDescriptor, IAdaptable {

	/**
	 * Answer the selection for the reciever based on whether the it can handle
	 * the selection. If it can return the selection. If it can handle the
	 * adapted to IResource value of the selection. If it satisfies neither of
	 * these conditions return an empty IStructuredSelection.
	 *
	 * @return IStructuredSelection
	 * @param selection
	 *            IStructuredSelection
	 */
	IStructuredSelection adaptedSelection(IStructuredSelection selection);

	/**
	 * Return the description.
	 *
	 * @return the description
	 */
	String getDescription();

	/**
	 * Return the tags associated with this wizard.
	 *
	 * @return the tags associated with this wizard
	 */
	String[] getTags();

	/**
	 * Create a wizard.
	 *
	 * @return the wizard
	 * @throws CoreException
	 *             thrown if there is a problem creating the wizard
	 */
	IWizard createWizard() throws CoreException;

	/**
	 * Return the description image for this wizard.
	 *
	 * @return the description image for this wizard or <code>null</code>
	 */
	public ImageDescriptor getDescriptionImage();

	/**
	 * Return the help system href for this wizard.
	 *
	 * @return the help system href for this wizard or <code>null</code>
	 */
	String getHelpHref();

	/**
	 * Return the category for this wizard.
	 *
	 * @return the category or <code>null</code>
	 */
	IWizardCategory getCategory();

	/**
	 * Answer <code>true</code> if this wizard is able to finish without loading
	 * any pages. This is a hint to any
	 * {@link org.eclipse.jface.wizard.WizardSelectionPage} or container that
	 * may contain this wizard to allow the finish button to be pressed without
	 * actually entering the wizard. If this occurs the
	 * {@link org.eclipse.jface.wizard.IWizard#performFinish()} method should be
	 * invoked by the containing wizard without creating any pages.
	 *
	 * @return <code>true</code> if this wizard can finish immediately
	 */
	boolean canFinishEarly();

	/**
	 * Answer <code>true</code> if this wizard has any pages. This is a hint to
	 * any {@link org.eclipse.jface.wizard.WizardSelectionPage} or container
	 * that may contain this wizard that they should enable the "Next" button,
	 * if appropriate.
	 *
	 * @return <code>true</code> if this wizard has wizard pages
	 */
	boolean hasPages();

}
