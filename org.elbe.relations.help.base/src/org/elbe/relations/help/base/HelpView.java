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
package org.elbe.relations.help.base;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;
import org.elbe.relations.help.base.internal.HelpManager;

/**
 * View to display the browser window containing the help pages.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class HelpView {
	private final Map<String, HelpManager> helpManagers = new HashMap<String, HelpManager>();

	@Inject
	private MTrimmedWindow window;

	@Inject
	private Logger log;

	@Inject
	public HelpView(final Composite inParent,
			final IExtensionRegistry inRegistry) {

		final IConfigurationElement[] lHelps = inRegistry
				.getConfigurationElementsFor(Constants.HELP_PLUGIN_ID);
		for (final IConfigurationElement lHelp : lHelps) {
			if (HelpManager.TOC_MARKER.equals(lHelp.getName())) {
				final HelpManager lManager = getHelpManager(lHelp
						.getNamespaceIdentifier());
				lManager.process(lHelp);
			}
		}

		try {
			for (final HelpManager lManager : helpManagers.values()) {
				lManager.consolidate();
			}

			final Browser lBrowser = new Browser(inParent, SWT.NONE);
			lBrowser.setUrl(getPlatformResource(Constants.URL_INDEX));
			new HelpNavigationJS(lBrowser, "tocRenderer", helpManagers.values());
			lBrowser.addProgressListener(new ProgressListener() {

				@Override
				public void completed(final ProgressEvent inEvent) {
					final String lUrl = (String) lBrowser
							.evaluate("return parent.ContentFrame.location.href;");
					if (lUrl != null) {
						lBrowser.execute(getBreadcrumb(getBreadcrumbItems(lUrl,
								helpManagers)));
					}
				}

				@Override
				public void changed(final ProgressEvent inEvent) {
					// nothing to do
				}
			});
		}
		catch (final IOException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	private String getBreadcrumbItems(final String inUrl,
			final Map<String, HelpManager> inHelpManagers) {

		HelpManager lManager = null;
		String lNamespace = "";
		for (final Entry<String, HelpManager> lEntry : inHelpManagers
				.entrySet()) {
			final String lNamespacePart = Constants.PATH_DELIM
					+ lEntry.getKey() + Constants.PATH_DELIM;
			if (inUrl.contains(lNamespacePart)) {
				lManager = lEntry.getValue();
				lNamespace = lEntry.getKey();
				break;
			}
		}
		return lManager == null ? "" : lManager.getBreadcrumbs(lNamespace,
				inUrl);
	}

	private String getBreadcrumb(final String inBreadcrumbItems) {
		final StringBuilder out = new StringBuilder(
				"var frame = parent.ContentFrame.document;");
		out.append("var frame = parent.ContentFrame.document;");
		out.append("var body = frame.getElementsByTagName('body')[0];");
		out.append("var breadcrumb = frame.createElement('div');");
		out.append("breadcrumb.setAttribute('class', 'help_breadcrumbs');");
		out.append("breadcrumb.innerHTML = '").append(inBreadcrumbItems)
				.append("';");
		out.append("body.insertBefore(breadcrumb, body.firstChild);");
		return new String(out);
	}

	private String getPlatformResource(final String inURL) throws IOException {
		final URL lUrl = new URL(inURL);
		return FileLocator.toFileURL(FileLocator.find(lUrl)).toString();
	}

	private HelpManager getHelpManager(final String inHelpIdentifier) {
		HelpManager out = helpManagers.get(inHelpIdentifier);
		if (out == null) {
			out = new HelpManager(inHelpIdentifier, log);
			helpManagers.put(inHelpIdentifier, out);
		}
		return out;
	}

	// ---

	/**
	 * JavaScript callback class to insert the html for the navigation into the
	 * navigation frame.
	 */
	class HelpNavigationJS extends BrowserFunction {
		private final Collection<HelpManager> helpTocs;

		public HelpNavigationJS(final Browser inBrowser, final String inName,
				final Collection<HelpManager> inHelpTocs) {
			super(inBrowser, inName);
			helpTocs = inHelpTocs;
		}

		@Override
		public Object function(final Object[] inArguments) {
			final StringBuilder out = new StringBuilder();
			try {
				for (final HelpManager lHelp : helpTocs) {
					out.append(Constants.LI_START).append(lHelp.renderToc())
							.append(Constants.LI_END);
				}
			}
			catch (final IOException exc) {
				log.error(exc, exc.getMessage());
			}
			return new String(out);
		}
	}

}
