package org.elbe.relations.help.base.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.help.base.Activator;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;

/**
 * Manages the help content provided by a help bundle. To achieve this, the
 * bundle's <code>plugin.xml</code> is evaluated and every toc entry of the
 * <code>org.eclipse.help.toc</code> extension point is processed.
 * 
 * @author Luthiger
 */
@SuppressWarnings("restriction")
public class HelpManager {
	public static final String TOC_MARKER = "toc";

	private final List<HelpEntry> entries;
	private final String helpID;
	private HelpEntry primary;
	private final Bundle bundle;
	private HelpEntry entry;
	private final Logger log;
	private final BreadcrumbManager breadcrumbManager;

	/**
	 * HelpManager constructor.
	 * 
	 * @param inHelpIdentifier
	 *            String the help plugin's namespace identifier
	 * @param inLog
	 *            {@link Logger}
	 */
	public HelpManager(final String inHelpIdentifier, final Logger inLog) {
		helpID = inHelpIdentifier;
		entries = new ArrayList<HelpEntry>();
		bundle = getHelpBundle();
		log = inLog;
		breadcrumbManager = new BreadcrumbManager();
	}

	/**
	 * Processes the specified configuration element within the
	 * <code>org.eclipse.help.toc</code> extension point.
	 * 
	 * @param inConfigurationElement
	 *            {@link IConfigurationElement}
	 */
	public void process(final IConfigurationElement inConfigurationElement) {
		final String lFile = inConfigurationElement
				.getAttribute(HelpEntry.AN_FILE);
		final String lPrimary = inConfigurationElement
				.getAttribute(HelpEntry.AN_PRIMARY);
		if (lPrimary == null) {
			entry = new HelpEntry(lFile, bundle, breadcrumbManager);
			entries.add(entry);
		} else {
			entry = new HelpEntry(lFile, bundle, breadcrumbManager,
					Boolean.parseBoolean(lPrimary));
			entries.add(entry);
			if (entry.isPrimary()) {
				primary = entry;
			}
		}
		try {
			entry.parse(bundle);
		}
		catch (final ParserConfigurationException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final SAXException exc) {
			log.error(exc, exc.getMessage());
		}
		catch (final IOException exc) {
			log.error(exc, exc.getMessage());
		}
	}

	/**
	 * Consolidates a help plugin's processed entries, i.e. links the entry
	 * instances together.
	 * 
	 * @throws IOException
	 */
	public void consolidate() throws IOException {
		primary.consolidate(entries);
	}

	private Bundle getHelpBundle() {
		Bundle out = null;
		for (final Bundle lCandidate : Activator.getContext().getBundles()) {
			if (lCandidate.getSymbolicName().equals(helpID)) {
				if (out == null
						|| out.getVersion().compareTo(lCandidate.getVersion()) < 0) {
					out = lCandidate;
				}
			}
		}
		return out;
	}

	/**
	 * @return String the help plugin's namespace identifier
	 */
	public String getHelpIdentifier() {
		return helpID;
	}

	/**
	 * @return {@link HelpEntry} the primary help entry, may be
	 *         <code>null</code>
	 */
	public HelpEntry getPrimary() {
		return primary;
	}

	/**
	 * @return {@link URL} the primary topic's path
	 * @throws IOException
	 */
	public URL getPrimaryTopicPath() throws IOException {
		final Path lPath = new Path(primary.getIndexTopicPath());
		return FileLocator.toFileURL(FileLocator.find(bundle, lPath, null));
	}

	/**
	 * <pre>
	 * &lt;span class="toc">&lt;a href="file:/.../html/index.html">XYZ Help&lt;/a>&lt;/span>
	 * &lt;ul>
	 * &lt;li>&lt;span class="toc">&lt;a href="file:/.../html/a.html">sub a&lt;/a>&lt;/span>&lt;/li>
	 * &lt;li>&lt;span class="toc">&lt;a href="file:/.../html/b.html">sub b&lt;/a>&lt;/span>&lt;/li>
	 * &lt;li>&lt;span class="toc">&lt;a href="file:/.../html/c.html">sub c&lt;/a>&lt;/span>&lt;/li>
	 * &lt;/ul>
	 * </pre>
	 * 
	 * @return String the rendered toc as html
	 * @throws IOException
	 */
	public String renderToc() throws IOException {
		final StringBuilder out = new StringBuilder();
		if (primary != null) {
			out.append(primary.renderToc());
		}
		return new String(out);
	}

	/**
	 * @param inNamespace
	 *            String the help bundle's namespace
	 * @param inUrl
	 *            String the actual url to process
	 * @return String the breadcrumb items (html) for the page with the
	 *         specified id
	 */
	public String getBreadcrumbs(final String inNamespace, final String inUrl) {
		return breadcrumbManager.getBreadcrumbs(HelpUtil.getBreadcrumbId(
				inNamespace, inUrl));
	}

}
