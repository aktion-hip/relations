package org.elbe.relations.help.base.internal;

import java.io.IOException;
import java.util.List;

import org.elbe.relations.help.base.Constants;
import org.osgi.framework.Bundle;
import org.xml.sax.Attributes;

/**
 * <b>The toc element</b>
 * <p>
 * The toc element is a Table of Contents that groups topics and other elements
 * defined in this file. The label identifies the table of contents to the user,
 * when it is displayed to the user.
 * </p>
 * <p>
 * The optional topic attribute is the path to a topic file describing the TOC.
 * </p>
 * <p>
 * If the sort attribute is true child topics will be sorted alphabetically.
 * </p>
 * <p>
 * The optional icon attribute allows the use of a different icon as defined by
 * a < tocIcon > element in an org.eclipse.help.toc extension.
 * </p>
 * <p>
 * The optional link_to attribute allows for linking toc from this file into
 * another toc file being higher in the navigation hierarchy. The value of the
 * link_to attribute must specify an anchor in another toc file. To link toc
 * from myapi.xml to api.xml file, specified in another plugin you would use the
 * syntax
 * </p>
 * 
 * <pre>
 * &lt;toc link_to="../anotherPlugin/api.xml#moreapi" label="My Tool API"/> 
 * ... 
 * &lt;toc />
 * </pre>
 * <p>
 * where # character separates toc file name from the anchor identifier.
 * </p>
 * 
 * @author Luthiger
 */
public class TableOfContentModel extends AbstractTocModel implements ITocModel {
	private static final String NODE_NAME_TOPIC = "topic";
	private static final String NODE_NAME_ANCHOR = "anchor";
	private static final String NODE_NAME_LINK = "link";

	// package protected constants
	static final String ATTRIBUTE_NAME_LABEL = "label";
	static final String ATTRIBUTE_NAME_TOPIC = "topic";
	static final String ATTRIBUTE_NAME_LINK_TO = "link_to";
	static final String ATTRIBUTE_NAME_SORT = "sort";
	static final String ATTRIBUTE_NAME_ICON = "icon";
	static final String ATTRIBUTE_NAME_ID = "id";
	static final String ATTRIBUTE_NAME_HREF = "href";
	static final String ATTRIBUTE_NAME_TOC = "toc";

	public TableOfContentModel(final ITocModel inParent, final Bundle inBundle,
			final BreadcrumbManager inBreadcrumbManager,
			final Attributes inAttributes) {
		super(inParent, inBundle, inBreadcrumbManager, inAttributes,
				ATTRIBUTE_NAME_LINK_TO, ATTRIBUTE_NAME_LABEL,
				ATTRIBUTE_NAME_TOPIC, ATTRIBUTE_NAME_SORT, ATTRIBUTE_NAME_ICON);
	}

	@Override
	public ITocModel addChild(final String inName, final Attributes inAttributes) {
		ITocModel out = null;
		if (NODE_NAME_TOPIC.equals(inName)) {
			out = new TopicModel(this, getBundle(), getBreadcrumbManager(),
					inAttributes);
		} else if (NODE_NAME_ANCHOR.equals(inName)) {
			out = new AnchorModel(this, getBundle(), getBreadcrumbManager(),
					inAttributes);
		} else if (NODE_NAME_LINK.equals(inName)) {
			out = new LinkModel(this, getBundle(), getBreadcrumbManager(),
					inAttributes);
		}
		addChild(out);
		return out;
	}

	@Override
	protected StringBuilder renderEntry() throws IOException {
		final String lLabel = get(TableOfContentModel.ATTRIBUTE_NAME_LABEL);
		final StringBuilder out = new StringBuilder();
		out.append("<span class=\"toc\"><a href=\"")
				.append(getHref(get(TableOfContentModel.ATTRIBUTE_NAME_TOPIC)))
				.append("\" title=\"").append(lLabel).append("\" >")
				.append(lLabel).append("</a></span>");
		return out;
	}

	@Override
	public List<ITocModel> getChildren() {
		return super.getChildren();
	}

	@Override
	public String toString() {
		return "toc: " + get(ATTRIBUTE_NAME_LABEL);
	}

	// ---

	/**
	 * <b>The topic element</b>
	 * <p>
	 * All help topic element are contributed as part of the toc container
	 * element. They can have a hierarchical structure, or can be listed as a
	 * flat list.
	 * </p>
	 * <p>
	 * The topic element is the workhorse of structure of Table of Contents.
	 * There are two typical uses for the topic element:
	 * </p>
	 * <p>
	 * 1.&nbsp; To provide a link to a documentation file - usually an HTML
	 * file. <br>
	 * 2.&nbsp; To act as a container for other toc, either in the same manifest
	 * or another.
	 * </p>
	 * <p>
	 * <b><i>1.&nbsp; Topics as links</i></b> <br>
	 * The simplest use of a topic is as a link to a documentation file.
	 * </p>
	 * <p>
	 * <tt>&lt;topic label="Some concept file" href="concepts/some_file.html"/></tt>
	 * </p>
	 * <p>
	 * The href attribute is relative to the plug-in that the manifest file
	 * belongs to.&nbsp; If you need to access a file in another plug-in, you
	 * can use the syntax
	 * </p>
	 * <p>
	 * <tt>&lt;topic label="topic in another plug-in" href="../other.plugin.id/concepts/some_other_file.html"/></tt>
	 * </p>
	 * <p>
	 * <b><i>2.&nbsp; Topics as containers</i></b> <br>
	 * The next most common use of a topic is to use it as a container for other
	 * toc.&nbsp; The container topic itself can always refer to a particular
	 * file as well.
	 * </p>
	 * <p>
	 * <tt>&lt;topic label="Integrated Development Environment" href="concepts/ciover.htm"></tt>
	 * <br>
	 * <tt>&nbsp; &lt;topic label="Starting the IDE" href="concepts/blah.htm"/></tt>
	 * <br>
	 * <tt>&nbsp; ...</tt> <br>
	 * <tt>&lt;/topic></tt>
	 * <p>
	 * If the sort attribute is true child topics will be sorted alphabetically.
	 * </p>
	 * <p>
	 * The optional icon attribute allows the use of a different icon as defined
	 * by a &lt; tocIcon &gt; element in an org.eclipse.help.toc extension.
	 * </p>
	 * 
	 * @author Luthiger
	 */
	public static class TopicModel extends AbstractTocModel implements
			ITocModel {

		public TopicModel(final ITocModel inParent, final Bundle inBundle,
				final BreadcrumbManager inBreadcrumbManager,
				final Attributes inAttributes) {
			super(inParent, inBundle, inBreadcrumbManager, inAttributes,
					ATTRIBUTE_NAME_LABEL, ATTRIBUTE_NAME_HREF,
					ATTRIBUTE_NAME_SORT, ATTRIBUTE_NAME_ICON);
		}

		@Override
		public ITocModel addChild(final String inName,
				final Attributes inAttributes) {
			ITocModel out = null;
			if (NODE_NAME_TOPIC.equals(inName)) {
				out = new TopicModel(this, getBundle(), getBreadcrumbManager(),
						inAttributes);
			} else if (NODE_NAME_ANCHOR.equals(inName)) {
				out = new AnchorModel(this, getBundle(),
						getBreadcrumbManager(), inAttributes);
			} else if (NODE_NAME_LINK.equals(inName)) {
				out = new LinkModel(this, getBundle(), getBreadcrumbManager(),
						inAttributes);
			}
			addChild(out);
			return out;
		}

		@Override
		protected StringBuilder renderEntry() throws IOException {
			final String lLabel = get(TableOfContentModel.ATTRIBUTE_NAME_LABEL);
			final StringBuilder out = new StringBuilder();
			final String lCssClass = getChildren().isEmpty() ? "page"
					: "chapter";
			out.append("<span class=\"")
					.append(lCssClass)
					.append("\"><a href=\"")
					.append(getHref(get(TableOfContentModel.ATTRIBUTE_NAME_HREF)))
					.append("\" title=\"").append(lLabel).append("\" >")
					.append(lLabel).append("</a></span>");
			return out;
		}

		@Override
		protected String getHrefAttributeName() {
			return TableOfContentModel.ATTRIBUTE_NAME_HREF;
		}

		@Override
		public String toString() {
			return "topic: " + get(ATTRIBUTE_NAME_LABEL);
		}

	}

	/**
	 * <b>The link element</b>
	 * <p>
	 * The link element allows to link Table of Contents defined in another toc
	 * file.&nbsp; All the topics from the toc file specified in the toc
	 * attribute will appear in the table of contents as if they were defined
	 * directly in place of the link element.&nbsp; To include toc from api.xml
	 * file you could write
	 * </p>
	 * <p>
	 * <tt>&lt;topic label="References" ></tt> <br>
	 * <tt>&nbsp; ...</tt> <br>
	 * <tt>&nbsp; &lt;link toc="api.xml" /></tt> <br>
	 * <tt>&nbsp; ...</tt> <br>
	 * <tt>&lt;/topic></tt>
	 * </p>
	 * 
	 * @author Luthiger
	 */
	public static class LinkModel extends AbstractTocModel implements ITocModel {

		public LinkModel(final ITocModel inParent, final Bundle inBundle,
				final BreadcrumbManager inBreadcrumbManager,
				final Attributes inAttributes) {
			super(inParent, inBundle, inBreadcrumbManager, inAttributes,
					ATTRIBUTE_NAME_TOC);
		}

		@Override
		public ITocModel addChild(final String inName,
				final Attributes inAttributes) {
			return new NoOpModel(this);
		}

		@Override
		protected StringBuilder renderEntry() throws IOException {
			return new StringBuilder();
		}

		@Override
		public void processLink(final List<HelpEntry> inEntries) {
			final String lTocName = get(ATTRIBUTE_NAME_TOC);
			HelpEntry lReference = null;
			for (final HelpEntry lHelpEntry : inEntries) {
				if (lTocName.equals(lHelpEntry.getFile())) {
					lReference = lHelpEntry;
					break;
				}
			}
			if (lReference != null) {
				for (final ITocModel lChild : lReference.getTocChildren()) {
					super.addChild(lChild);
					lChild.setParent(getParent());
				}
			}
		}

		@Override
		public String render() throws IOException {
			final StringBuilder out = new StringBuilder();
			for (final ITocModel lChild : getChildren()) {
				out.append(Constants.LI_START);
				out.append(lChild.render());
				out.append(Constants.LI_END);
			}
			return new String(out);
		}

		@Override
		public String toString() {
			return "link: (to " + get(ATTRIBUTE_NAME_TOC) + ")";
		}
	}

	/**
	 * <b>The anchor element</b>
	 * <p>
	 * The anchor element defines a point that will allow linking other toc
	 * files to this navigation, and extending it, without using the link
	 * element and referencing other toc files from here.&nbsp; To allow
	 * inserting Table of Contents with more topics after the "ZZZ" document you
	 * would define an anchor as follows:
	 * </p>
	 * <p>
	 * <tt>...</tt> <br>
	 * <tt>&lt;topic label="zzz" href="zzz.html" /></tt> <br>
	 * <tt>&lt;anchor id="moreapi" /></tt> <br>
	 * <tt>...</tt>
	 * </p>
	 * 
	 * @author Luthiger
	 */
	public static class AnchorModel extends AbstractTocModel implements
			ITocModel {

		public AnchorModel(final ITocModel inParent, final Bundle inBundle,
				final BreadcrumbManager inBreadcrumbManager,
				final Attributes inAttributes) {
			super(inParent, inBundle, inBreadcrumbManager, inAttributes,
					ATTRIBUTE_NAME_ID);
		}

		@Override
		public ITocModel addChild(final String inName,
				final Attributes inAttributes) {
			return new NoOpModel(this);
		}

		@Override
		protected StringBuilder renderEntry() throws IOException {
			return new StringBuilder();
		}

	}

	public static class NoOpModel implements ITocModel {
		private ITocModel parent;

		public NoOpModel(final ITocModel inParent) {
			parent = inParent;
		}

		@Override
		public ITocModel getParent() {
			return parent;
		}

		@Override
		public ITocModel addChild(final String inName,
				final Attributes inAttributes) {
			return new NoOpModel(this);
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
			// do nothing
		}

		@Override
		public String renderBreadcrumb() {
			return "";
		}

		@Override
		public String getId() throws IOException {
			return "noop";
		}

		@Override
		public void processBreadcrumbs() throws IOException {
			// do nothing
		}

		@Override
		public void setParent(final ITocModel inParent) {
			parent = inParent;
		}
	}

}
