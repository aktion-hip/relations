package org.elbe.relations.help.base.internal;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>HelpEntry</code> is responsible for parsing the toc file specified in
 * the configuration element of a <code>org.eclipse.help.toc</code> plugin.
 * 
 * @author Luthiger
 */
public class HelpEntry extends DefaultHandler {
	static String AN_FILE = "file";
	static String AN_PRIMARY = "primary";

	private final String file;
	private final Bundle bundle;
	private final boolean primary;
	private ITocModel actModel;
	private final BreadcrumbManager breadcrumbManager;

	/**
	 * HelpEntry constructor.
	 * 
	 * @param inFile
	 *            String the file
	 * @param inBundle
	 *            {@link Bundle} the bundle this entry belongs to
	 * @param inBreadcrumbManager
	 *            {@link BreadcrumbManager}
	 */
	public HelpEntry(final String inFile, final Bundle inBundle,
			final BreadcrumbManager inBreadcrumbManager) {
		this(inFile, inBundle, inBreadcrumbManager, false);
	}

	/**
	 * HelpEntry constructor.
	 * 
	 * @param inFile
	 *            String the file
	 * @param inBundle
	 *            {@link Bundle} the bundle this entry belongs to
	 * @param inBreadcrumbManager
	 *            {@link BreadcrumbManager}
	 * @param inPrimary
	 *            boolean
	 */
	public HelpEntry(final String inFile, final Bundle inBundle,
			final BreadcrumbManager inBreadcrumbManager, final boolean inPrimary) {
		file = inFile;
		bundle = inBundle;
		breadcrumbManager = inBreadcrumbManager;
		primary = inPrimary;
	}

	public boolean isPrimary() {
		return primary;
	}

	public String getFile() {
		return file;
	}

	/**
	 * Parse this entry's toc.
	 * 
	 * @param inBundle
	 *            {@link Bundle} the help bundle containing this entry
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public void parse(final Bundle inBundle)
			throws ParserConfigurationException, SAXException, IOException {
		final URL lUrl = FileLocator.find(inBundle, new Path(file), null);
		final SAXParser lParser = SAXParserFactory.newInstance().newSAXParser();
		lParser.parse(lUrl.openStream(), this);
	}

	/**
	 * @return String the help index' topic attribute, i.e. the relative path of
	 *         the help's index file
	 */
	public String getIndexTopicPath() {
		return getToc().get(TableOfContentModel.ATTRIBUTE_NAME_TOPIC);
	}

	/**
	 * @return String the toc rendered as html
	 * @throws IOException
	 */
	public String renderToc() throws IOException {
		return getToc().render();
	}

	/**
	 * For each link in actModel: retrieve the linked entry from the passed
	 * entries and add it to the actual model.
	 * 
	 * @param inEntries
	 *            List&lt;HelpEntry>
	 * @throws IOException
	 */
	public void consolidate(final List<HelpEntry> inEntries) throws IOException {
		getToc().processLink(inEntries);
		getToc().processBreadcrumbs();
	}

	/**
	 * @return {@link ITocModel} this entries root model
	 */
	public ITocModel getToc() {
		return ((RootModel) actModel).getToc();
	}

	/**
	 * @return List&lt;ITocModel> the toc root's child elements
	 */
	public List<ITocModel> getTocChildren() {
		return ((RootModel) actModel).getChildren();
	}

	@Override
	public String toString() {
		return String.format("%s%s", file, primary ? " (primary)" : "");
	}

	// --- SAX handler methods ---

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		actModel = new RootModel(bundle, breadcrumbManager);
	}

	@Override
	public void startElement(final String inUri, final String inLocalName,
			final String inQName, final Attributes inAttributes)
			throws SAXException {
		actModel = actModel.addChild(inQName, inAttributes);
	}

	@Override
	public void endElement(final String inArg0, final String inArg1,
			final String inArg2) throws SAXException {
		actModel = actModel.getParent();
	}

	/**
	 * Special model class representing the root of the xml document.
	 */
	private static class RootModel implements ITocModel {
		private final Bundle bundle;
		private TableOfContentModel toc;
		private final BreadcrumbManager breadcrumbManager;

		RootModel(final Bundle inBundle,
				final BreadcrumbManager inBreadcrumbManager) {
			bundle = inBundle;
			breadcrumbManager = inBreadcrumbManager;
		}

		@Override
		public ITocModel addChild(final String inName,
				final Attributes inAttributes) {
			final TableOfContentModel out = new TableOfContentModel(this,
					bundle, breadcrumbManager, inAttributes);
			toc = out;
			return out;
		}

		public ITocModel getToc() {
			return toc;
		}

		@Override
		public ITocModel getParent() {
			return null;
		}

		@Override
		public void setParent(final ITocModel inParent) {
			// do nothing
		}

		@Override
		public String get(final String inKey) {
			return "";
		}

		@Override
		public String render() {
			return "";
		}

		@Override
		public void processLink(final List<HelpEntry> inEntries) {
			toc.processLink(inEntries);
		}

		@Override
		public void processBreadcrumbs() throws IOException {
			toc.processBreadcrumbs();
		}

		public List<ITocModel> getChildren() {
			return toc.getChildren();
		}

		@Override
		public String renderBreadcrumb() {
			return "";
		}

		@Override
		public String getId() throws IOException {
			return "root";
		}
	}

}
