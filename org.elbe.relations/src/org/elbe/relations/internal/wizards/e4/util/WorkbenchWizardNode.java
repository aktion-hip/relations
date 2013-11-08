package org.elbe.relations.internal.wizards.e4.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.elbe.relations.internal.wizards.e4.AbstractExtensionWizardSelectionPage;
import org.elbe.relations.internal.wizards.e4.NewWizardSelectionPage;

/**
 * A wizard node represents a "potential" wizard. Wizard nodes are used by
 * wizard selection pages to allow the user to pick from several available
 * nested wizards.
 * <p>
 * <b>Subclasses</b> simply need to override method <code>createWizard()</code>,
 * which is responsible for creating an instance of the wizard it represents AND
 * ensuring that this wizard is the "right" type of wizard (e.g.- New, Import,
 * etc.).
 * </p>
 * 
 * @author Luthiger
 * @see org.eclipse.ui.internal.dialogs.WorkbenchWizardNode
 */
@SuppressWarnings("restriction")
public abstract class WorkbenchWizardNode implements IWizardNode,
		IPluginContribution {

	protected AbstractExtensionWizardSelectionPage parentWizardPage;

	protected IWizard wizard;

	protected IWizardDescriptor wizardElement;

	/**
	 * Creates a <code>WorkbenchWizardNode</code> that holds onto a wizard
	 * element. The wizard element provides information on how to create the
	 * wizard supplied by the ISV's extension.
	 * 
	 * @param inWizardPage
	 *            {@link NewWizardSelectionPage} the wizard page
	 * @param inElement
	 *            the wizard descriptor
	 */
	public WorkbenchWizardNode(
			final AbstractExtensionWizardSelectionPage inWizardPage,
			final IWizardDescriptor inElement) {
		super();
		this.parentWizardPage = inWizardPage;
		this.wizardElement = inElement;
	}

	/**
	 * Returns the wizard represented by this wizard node. <b>Subclasses</b>
	 * must override this method.
	 * 
	 * @return {@link IWorkbenchWizard} the wizard object
	 * @throws CoreException
	 */
	public abstract IWorkbenchWizard createWizard() throws CoreException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardNode#dispose()
	 */
	@Override
	public void dispose() {
		// Do nothing since the wizard wasn't created via reflection.
	}

	/**
	 * Returns the current resource selection that is being given to the wizard.
	 */
	protected IStructuredSelection getCurrentResourceSelection() {
		return null;
		// return parentWizardPage.getCurrentResourceSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
	 */
	@Override
	public Point getExtent() {
		return new Point(-1, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	@Override
	public String getLocalId() {
		final IPluginContribution contribution = (IPluginContribution) Util
				.getAdapter(wizardElement, IPluginContribution.class);
		if (contribution != null) {
			return contribution.getLocalId();
		}
		return wizardElement.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	@Override
	public String getPluginId() {
		final IPluginContribution contribution = (IPluginContribution) Util
				.getAdapter(wizardElement, IPluginContribution.class);
		if (contribution != null) {
			return contribution.getPluginId();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardNode#getWizard()
	 */
	@Override
	public IWizard getWizard() {
		if (wizard != null) {
			return wizard; // we've already created it
		}

		final IWorkbenchWizard[] lWorkbenchWizard = new IWorkbenchWizard[1];
		final IStatus lStatuses[] = new IStatus[1];
		// Start busy indicator.
		BusyIndicator.showWhile(parentWizardPage.getShell().getDisplay(),
				new Runnable() {
					@Override
					public void run() {
						SafeRunner.run(new SafeRunnable() {
							/**
							 * Add the exception details to status is one
							 * happens.
							 */
							@Override
							public void handleException(final Throwable exc) {
								final IPluginContribution lContribution = (IPluginContribution) Util
										.getAdapter(wizardElement,
												IPluginContribution.class);
								lStatuses[0] = new Status(
										IStatus.ERROR,
										lContribution != null ? lContribution
												.getPluginId()
												: WorkbenchPlugin.PI_WORKBENCH,
										IStatus.OK,
										WorkbenchMessages.WorkbenchWizard_errorMessage,
										exc);
							}

							@Override
							public void run() {
								try {
									lWorkbenchWizard[0] = createWizard();
									// create instance of target wizard
								}
								catch (final CoreException exc) {
									final IPluginContribution lContribution = (IPluginContribution) Util
											.getAdapter(wizardElement,
													IPluginContribution.class);
									lStatuses[0] = new Status(
											IStatus.ERROR,
											lContribution != null ? lContribution
													.getPluginId()
													: WorkbenchPlugin.PI_WORKBENCH,
											IStatus.OK,
											WorkbenchMessages.WorkbenchWizard_errorMessage,
											exc);
								}
							}
						});
					}
				});

		if (lStatuses[0] != null) {
			parentWizardPage
					.setErrorMessage(WorkbenchMessages.WorkbenchWizard_errorMessage);
			final StatusAdapter lStatusAdapter = new StatusAdapter(lStatuses[0]);
			lStatusAdapter.addAdapter(Shell.class, parentWizardPage.getShell());
			lStatusAdapter.setProperty(StatusAdapter.TITLE_PROPERTY,
					WorkbenchMessages.WorkbenchWizard_errorTitle);
			StatusManager.getManager().handle(lStatusAdapter,
					StatusManager.SHOW);
			return null;
		}

		IStructuredSelection lCurrentSelection = getCurrentResourceSelection();

		// Get the adapted version of the selection that works for the
		// wizard node
		lCurrentSelection = wizardElement.adaptedSelection(lCurrentSelection);

		lWorkbenchWizard[0].init(null, lCurrentSelection);

		wizard = lWorkbenchWizard[0];
		return wizard;
	}

	/**
	 * Returns the wizard element.
	 * 
	 * @return the wizard descriptor
	 */
	public IWizardDescriptor getWizardElement() {
		return wizardElement;
	}

	// /**
	// * Returns the current workbench.
	// */
	// protected IWorkbench getWorkbench() {
	// return parentWizardPage.getWorkbench();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
	 */
	@Override
	public boolean isContentCreated() {
		return wizard != null;
	}
}
