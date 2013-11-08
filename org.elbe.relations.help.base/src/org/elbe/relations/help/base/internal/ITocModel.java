package org.elbe.relations.help.base.internal;

import java.io.IOException;
import java.util.List;

import org.xml.sax.Attributes;

/**
 * The interface of a help topic model.
 * 
 * @author Luthiger
 */
public interface ITocModel {

	/**
	 * @return {@link ITocModel} the parent model, <code>null</code> indicates
	 *         the root model
	 */
	ITocModel getParent();

	/**
	 * In case of link models, we have to overwrite the parent.
	 * 
	 * @param inParent
	 *            {@link ITocModel} the new parent
	 */
	void setParent(ITocModel inParent);

	/**
	 * Adds a new child to this model.
	 * 
	 * @param inName
	 *            String the node name
	 * @param inAttributes
	 *            {@link Attributes} the node's attributes
	 * @return {@link ITocModel} the newly created child model
	 */
	ITocModel addChild(String inName, Attributes inAttributes);

	/**
	 * Returns the value of the specified key.
	 * 
	 * @param inKey
	 *            String
	 * @return String the value, may be empty
	 */
	String get(String inKey);

	/**
	 * @return String the rendered toc as html
	 * @throws IOException
	 */
	String render() throws IOException;

	/**
	 * Link this model to the appropriate entry in the passed list.
	 * 
	 * @param inEntries
	 *            List&lt;HelpEntry>
	 */
	void processLink(final List<HelpEntry> inEntries);

	/**
	 * Do breadcrumb management;
	 * 
	 * @throws IOException
	 */
	void processBreadcrumbs() throws IOException;

	/**
	 * @return String the model's entry in the breadcrumb, e.g.
	 *         <code>&lt;a href=\"path\">Relations Help&lt;/a></code>
	 * @throws IOException
	 */
	String renderBreadcrumb() throws IOException;

	/**
	 * @return String the model's id
	 * @throws IOException
	 */
	String getId() throws IOException;

}
