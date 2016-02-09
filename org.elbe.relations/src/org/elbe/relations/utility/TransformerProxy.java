/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2016, Benno Luthiger
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
package org.elbe.relations.utility;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.elbe.relations.RelationsMessages;
import org.xml.sax.InputSource;

/**
 * Proxy object for xsl transformation. An instance of this class holds the
 * information needed to process a transformation.
 *
 * @author Luthiger Created on 12.08.2007
 */
public class TransformerProxy {
	protected static Hashtable<String, Templates> cTemplates = new Hashtable<String, Templates>(
	        53);

	protected URL xslFile;
	protected String xmlToTransform;
	protected HashMap<String, Object> stylesheetParameters;

	/**
	 *
	 * @param inXSLFile
	 *            File
	 * @param inXML
	 *            String
	 * @param inStylesheetParameters
	 *            HashMap<String, Object>
	 */
	public TransformerProxy(final URL inXSLFile, final String inXML,
	        final HashMap<String, Object> inStylesheetParameters) {
		xslFile = inXSLFile;
		xmlToTransform = inXML;
		stylesheetParameters = inStylesheetParameters;
	}

	private synchronized Transformer getTransformer()
	        throws TransformerConfigurationException, IOException {
		if (xslFile == null) {
			throw new Error(
			        RelationsMessages.getString("TransformerProxy.error.msg")); //$NON-NLS-1$
		}

		Transformer outTransformer;
		Templates templates;
		if ((templates = cTemplates.get(xslFile.getFile())) == null) {
			final StreamSource lSource = new StreamSource(xslFile.openStream());
			templates = TransformerFactory.newInstance().newTemplates(lSource);
			cTemplates.put(xslFile.getFile(), templates);
		}
		outTransformer = templates.newTransformer();
		return outTransformer;

	}

	/**
	 * Performs the transformation to the specified output stream.
	 *
	 * @param inWriter
	 *            Writer
	 * @throws TransformerException
	 * @throws IOException
	 */
	public void renderToStream(final Writer inWriter)
	        throws TransformerException, IOException {
		final Source xmlSource = new SAXSource(
		        new InputSource(new StringReader(xmlToTransform)));
		final StreamResult result = new StreamResult(inWriter);
		final Transformer transformer = getTransformer();
		setStylesheetParameters(transformer, stylesheetParameters);
		transformer.transform(xmlSource, result);
	}

	/**
	 * Sets the stylesheet parameters to the specified <code>Transformer</code>.
	 *
	 * @param inTransformer
	 *            Transformer
	 * @param inStylesheetParameters
	 *            HashMap<String, Object>
	 */
	protected void setStylesheetParameters(final Transformer inTransformer,
	        final HashMap<String, Object> inStylesheetParameters) {
		if (inStylesheetParameters == null) {
			return;
		}

		for (final String key : inStylesheetParameters.keySet()) {
			inTransformer.setParameter(key, inStylesheetParameters.get(key));
		}
	}

}
