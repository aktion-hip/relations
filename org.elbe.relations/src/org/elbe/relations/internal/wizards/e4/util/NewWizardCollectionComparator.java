package org.elbe.relations.internal.wizards.e4.util;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * A Viewer element sorter that sorts Elements by their name attribute. Note
 * that capitalization differences are not considered by this sorter, so a < B <
 * c.
 * 
 * NOTE one exception to the above: an element with the system's reserved name
 * for base Wizards will always be sorted such that it will ultimately be placed
 * at the beginning of the sorted result.
 * 
 * @see org.eclipse.ui.internal.dialogs.NewWizardCollectionComparator
 */
@SuppressWarnings("restriction")
public class NewWizardCollectionComparator extends ViewerComparator {

	/**
	 * Creates an instance of <code>NewWizardCollectionSorter</code>. Since this
	 * is a stateless sorter, it is only accessible as a singleton; the private
	 * visibility of this constructor ensures this.
	 */
	private NewWizardCollectionComparator() {
		super();
	}

	/**
	 * Factory method.
	 * 
	 * @return {@link NewWizardCollectionComparator}
	 */
	public static NewWizardCollectionComparator getInstance() {
		return new NewWizardCollectionComparator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	@Override
	public int category(final Object element) {
		if (element instanceof WorkbenchWizardElement) {
			return -1;
		}
		if (element instanceof WizardCollectionElement) {
			final String id = ((WizardCollectionElement) element).getId();
			if (WizardsRegistryReader.GENERAL_WIZARD_CATEGORY.equals(id)) {
				return 1;
			}
			if (WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY.equals(id)) {
				return 3;
			}
			if (WizardsRegistryReader.FULL_EXAMPLES_WIZARD_CATEGORY.equals(id)) {
				return 4;
			}
			return 2;
		}
		return super.category(element);
	}

	/**
	 * Return true if this sorter is affected by a property change of
	 * propertyName on the specified element.
	 */
	@Override
	public boolean isSorterProperty(final Object object, final String propertyId) {
		return propertyId.equals(IBasicPropertyConstants.P_TEXT);
	}
}
