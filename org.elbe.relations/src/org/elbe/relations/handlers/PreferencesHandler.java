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
package org.elbe.relations.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.dialogs.EmptyPreferencePage;

/**
 * The handler for the preference menu item.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class PreferencesHandler {
	public static final String PREFS_PAGE_XP = "org.eclipse.ui.preferencePages";
	private static final String ELEMENT_PAGE = "page"; // $NON-NLS-1$
	private static final String ATTR_ID = "id"; // $NON-NLS-1$
	private static final String ATTR_CATEGORY = "category"; // $NON-NLS-1$
	private static final String ATTR_CLASS = "class"; // $NON-NLS-1$
	private static final String ATTR_NAME = "name"; // $NON-NLS-1$

	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;

	@Inject
	private IExtensionRegistry registry;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	protected Shell shell;

	@Execute
	public void execute() {
		final PreferenceManager lPrefManager = configurePreferences();
		final PreferenceDialog lDialog = new PreferenceDialog(shell,
				lPrefManager);
		// dialog.setPreferenceStore(InstanceScope.INSTANCE
		// .getNode(RelationsConstants.PREFERENCE_NODE));
		lDialog.create();
		lDialog.getTreeViewer().setComparator(new ViewerComparator());
		lDialog.getTreeViewer().expandAll();
		lDialog.open();
	}

	private PreferenceManager configurePreferences() {
		final PreferenceManager outPrefManager = new PreferenceManager();
		final IContributionFactory lFactory = context
				.get(IContributionFactory.class);

		for (final IConfigurationElement lElement : registry
				.getConfigurationElementsFor(PREFS_PAGE_XP)) {
			if (!lElement.getName().equals(ELEMENT_PAGE)) {
				log.warn("unexpected element: {0}", lElement.getName());
				continue;
			} else if (isEmpty(lElement.getAttribute(ATTR_ID))
					|| isEmpty(lElement.getAttribute(ATTR_NAME))) {
				log.warn("missing id and/or name: {}",
						lElement.getNamespaceIdentifier());
				continue;
			}
			PreferenceNode lNode = null;
			if (lElement.getAttribute(ATTR_CLASS) != null) {
				IPreferencePage lPage = null;
				try {
					final String lPrefPageURI = getClassURI(
							lElement.getNamespaceIdentifier(),
							lElement.getAttribute(ATTR_CLASS));
					final Object lPrefObject = lFactory.create(lPrefPageURI,
							context);
					if (!(lPrefObject instanceof IPreferencePage)) {
						log.error("Expected instance of IPreferencePage: {0}",
								lElement.getAttribute(ATTR_CLASS));
						continue;
					}
					lPage = (IPreferencePage) lPrefObject;
				}
				catch (final ClassNotFoundException e) {
					log.error(e);
					continue;
				}
				ContextInjectionFactory.inject(lPage, context);
				if ((lPage.getTitle() == null || lPage.getTitle().isEmpty())
						&& lElement.getAttribute(ATTR_NAME) != null) {
					lPage.setTitle(lElement.getAttribute(ATTR_NAME));
				}
				lNode = new PreferenceNode(lElement.getAttribute(ATTR_ID),
						lPage);
			} else {
				lNode = new PreferenceNode(lElement.getAttribute(ATTR_ID),
						new EmptyPreferencePage());
			}
			if (isEmpty(lElement.getAttribute(ATTR_CATEGORY))) {
				outPrefManager.addToRoot(lNode);
			} else {
				final IPreferenceNode lParent = findNode(outPrefManager,
						lElement.getAttribute(ATTR_CATEGORY));
				if (lParent == null) {
					outPrefManager.addToRoot(lNode);
				} else {
					lParent.add(lNode);
				}
			}
		}
		return outPrefManager;
	}

	private IPreferenceNode findNode(final PreferenceManager inManager,
			final String inCategoryId) {
		for (final Object lElement : inManager
				.getElements(PreferenceManager.POST_ORDER)) {
			if (lElement instanceof IPreferenceNode
					&& ((IPreferenceNode) lElement).getId()
							.equals(inCategoryId)) {
				return (IPreferenceNode) lElement;
			}
		}
		return null;
	}

	private String getClassURI(final String inBundleId, final String inSpec)
			throws ClassNotFoundException {
		if (inSpec.startsWith("bundleclass://")) { // $NON-NLS-1$
			return inSpec;
		} else if (inSpec.startsWith("platform:")) { // $NON-NLS-1$
			return inSpec;
		}
		return "bundleclass://" + inBundleId + '/' + inSpec;
	}

	private boolean isEmpty(final String inValue) {
		return inValue == null || inValue.trim().isEmpty();
	}

}
