package org.elbe.relations.help.base.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.elbe.relations.help.base.Constants;
import org.osgi.framework.Bundle;
import org.xml.sax.Attributes;

/**
 * Base class for toc model objects.
 * 
 * @author Luthiger
 */
public abstract class AbstractTocModel implements ITocModel {
	private ITocModel parent;
	private final Map<String, String> attributes = new HashMap<String, String>();
	private final List<ITocModel> children;
	private final Bundle bundle;
	private final BreadcrumbManager breadcrumbManager;

	public AbstractTocModel(final ITocModel inParent, final Bundle inBundle,
			final BreadcrumbManager inBreadcrumbManager,
			final Attributes inAttributes, final String... inKeys) {
		parent = inParent;
		bundle = inBundle;
		breadcrumbManager = inBreadcrumbManager;
		children = new ArrayList<ITocModel>();

		for (final String lKey : inKeys) {
			put(lKey, inAttributes.getValue(lKey));
		}
	}

	protected Bundle getBundle() {
		return bundle;
	}

	protected BreadcrumbManager getBreadcrumbManager() {
		return breadcrumbManager;
	}

	protected void addChild(final ITocModel inChild) {
		if (inChild != null) {
			children.add(inChild);
		}
	}

	@Override
	public ITocModel getParent() {
		return parent;
	}

	@Override
	public void setParent(final ITocModel inNewParent) {
		parent = inNewParent;
	}

	private void put(final String inKey, final String inValue) {
		if (inValue != null && !inValue.isEmpty()) {
			attributes.put(inKey, inValue);
		}
	}

	@Override
	public String get(final String inKey) {
		final String out = attributes.get(inKey);
		return out == null ? "" : out;
	}

	@Override
	public String render() throws IOException {
		final StringBuilder out = new StringBuilder();
		out.append(renderEntry());
		if (!children.isEmpty()) {
			out.append("<ul>");
			for (final ITocModel lChild : children) {
				final boolean lNoList = lChild instanceof TableOfContentModel.LinkModel;
				out.append(lNoList ? "" : Constants.LI_START);
				out.append(lChild.render());
				out.append(lNoList ? "" : Constants.LI_END);
			}
			out.append("</ul>");
		}
		return new String(out);
	}

	protected abstract StringBuilder renderEntry() throws IOException;

	/**
	 * Transforms the specified relative path in a bundle to file system path.
	 * 
	 * @param inPath
	 *            String in relative path
	 * @return String the file system path
	 * @throws IOException
	 */
	protected String getHref(final String inPath) throws IOException {
		final Path lPath = new Path(inPath);
		return FileLocator.toFileURL(FileLocator.find(bundle, lPath, null))
				.toExternalForm();
	}

	@Override
	public void processLink(final List<HelpEntry> inEntries) {
		for (final ITocModel lChild : children) {
			lChild.processLink(inEntries);
		}
	}

	protected List<ITocModel> getChildren() {
		return children;
	}

	@Override
	public void processBreadcrumbs() throws IOException {
		// first manage the item's breadcrumb
		breadcrumbManager.registerBreadcrumb(this);
		// then recurse
		for (final ITocModel lChild : children) {
			lChild.processBreadcrumbs();
		}
	}

	@Override
	public String renderBreadcrumb() throws IOException {
		return String.format("<a href=\"%s\">%s</a>",
				getHref(get(getHrefAttributeName())),
				get(TableOfContentModel.ATTRIBUTE_NAME_LABEL));
	}

	protected String getHrefAttributeName() {
		return TableOfContentModel.ATTRIBUTE_NAME_TOPIC;
	}

	@Override
	public String getId() throws IOException {
		final String lUrl = getHref(get(getHrefAttributeName()));
		return HelpUtil.getBreadcrumbId(bundle.getSymbolicName(), lUrl);
	}

}
