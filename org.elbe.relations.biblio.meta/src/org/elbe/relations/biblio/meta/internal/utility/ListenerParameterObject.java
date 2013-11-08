/*
This package is part of Relations application.
Copyright (C) 2009, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.elbe.relations.biblio.meta.internal.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Structured parameter objects that can be used by listeners during SAX parsing. 
 *
 * @author Luthiger
 * Created on 30.12.2009
 */
public class ListenerParameterObject {
	public static final String PATH_DELIMITER = "/"; //$NON-NLS-1$
	
	private Map<String, ListenerParameter> elements;
	private ListenerParameter actualParameter;

	/**
	 * Default constructor.
	 */
	public ListenerParameterObject() {
		elements = new HashMap<String, ListenerParameter>();
	}
	
	/**
	 * Adds a parameter with the specified values to this object.
	 * 
	 * @param inParameterName String the parameter name.
	 * @param inNodeName String the node name this parameter is aimed at.
	 * @param inVariant String a type variant of the node name, may be <code>null</code>.
	 * @return ListenerParameter the parameter created and added.
	 */
	public ListenerParameter addParameter(String inParameterName, String inNodeName, String inVariant) {
		ListenerParameter outParameter = new ListenerParameter(inNodeName, inVariant, null);
		elements.put(inParameterName, outParameter);
		return outParameter;
	}
	
	/**
	 * Prepares the parameter object, i.e. makes the correct parameter listening for parser input.
	 * 
	 * @param inNodeName String the node name that is actually parsed.
	 * @param inType String the type attribute's value
	 */
	public void prepare(String inNodeName, String inType) {
		if (actualParameter != null) {
			ListenerParameter lMatchingChild = actualParameter.prepare(inNodeName, inType);
			if (lMatchingChild != null) {
				actualParameter = lMatchingChild;
				return;
			}
			actualParameter = actualParameter.addVirtualChild(inNodeName);
			return;
		}
		for (ListenerParameter lParameter : elements.values()) {
			if (lParameter.matches(inNodeName, inType)) {
				actualParameter = lParameter;
				return;
			}
		}
	}

	public void unprepare(String inNodeName) {
		if (actualParameter != null) actualParameter = actualParameter.getParent();
	}

	
	/**
	 * Adds the <code>inLength</code> number of characters from the array passed to the prepared parameter.
	 * 
	 * @param inCharacters char[]
	 * @param inStart int
	 * @param inLength int
	 */
	public void addCharacters(char[] inCharacters, int inStart, int inLength) {
		if (actualParameter == null) return;
		actualParameter.addCharacters(inCharacters, inStart, inLength);
	}
	
	/**
	 * Returns the content from the specified parameter.
	 * 
	 * @param inParameterName String
	 * @return String the parameter's content.
	 */
	public String getContent(String inParameterName) {
		String[] lParts = inParameterName.split(PATH_DELIMITER);
		if (lParts.length == 1) {
			ListenerParameter lParameter = elements.get(inParameterName);
			return lParameter == null ? null : lParameter.getContent();
		}
		Stack<String> lPath = createPathStack(lParts);
		String lElementID = lPath.pop();
		ListenerParameter lParameter = elements.get(lElementID);
		return lParameter == null ? null : lParameter.getContent(lPath);
	}
	
	private Stack<String> createPathStack(String[] inPathParts) {
		Stack<String> out = new Stack<String>();
		for (int i = inPathParts.length; i > 0; i--) {
			out.push(inPathParts[i-1]);
		}
		return out;
	}

// --- inner classes ---
	
	/**
	 * A parameter element for the parameter object.
	 */
	public static class ListenerParameter {
		private String nodeName;
		private String variant;
		private StringBuilder content;
		protected Map<String, ListenerParameter> children;
		protected ListenerParameter parent;

		/**
		 * Parameter constructor.
		 * 
		 * @param inNodeName String the node name this parameter is aimed at.
		 * @param inVariant String a type variant of the node name, may be <code>null</code>.
		 */
		ListenerParameter(String inNodeName, String inVariant, ListenerParameter inParent) {
			nodeName = inNodeName;
			variant = inVariant;
			content = new StringBuilder();
			children = new HashMap<String, ListenerParameter>();
			parent = inParent;
		}
		boolean matches(String inNodeName, String inType) {
			if (!nodeName.equals(inNodeName)) return false;
			if (variant == null) {
				return inType == null;
			}
			return variant.equals(inType);
		}
		void addCharacters(char[] inCharacters, int inStart, int inLength) {
			for (int i = inStart; i < inStart + inLength; i++) {
				content.append(inCharacters[i]);
			}
		}
		String getContent() {
			return content.length() == 0 ? null : new String(content);
		}
		String getContent(Stack<String> inPath) {
			if (!inPath.isEmpty()) {
				String lElementID = inPath.pop();
				ListenerParameter lParameter = children.get(lElementID);
				return lParameter == null ? null : lParameter.getContent(inPath);
			}
			return getContent();
		}
		/**
		 * Adds a child parameter with the specified values to this object.
		 * 
		 * @param inParameterName String the parameter name.
		 * @param inNodeName String the node name this parameter is aimed at.
		 * @param inVariant String a type variant of the node name, may be <code>null</code>.
		 * @return ListenerParameter the created and added child parameter
		 */
		public ListenerParameter addChild(String inParameterName, String inNodeName, String inVariant) {
			ListenerParameter outParameter = new ListenerParameter(inNodeName, inVariant, this);
			children.put(inParameterName, outParameter);
			return outParameter;
		}
		ListenerParameter addVirtualChild(String inNodeName) {
			ListenerParameter outParameter = new VirtualParameter(inNodeName, this);
			children.put(inNodeName, outParameter);
			return outParameter;
		}		
		ListenerParameter prepare(String inNodeName, String inType) {
			for (ListenerParameter lParameter : children.values()) {
				if (lParameter.matches(inNodeName, inType)) {
					return lParameter;
				}
			}
			return null;
		}
		ListenerParameter getParent() {
			return parent;
		}
		void dispose() {
			//do nothing
		}
		@Override
		public String toString() {
			String out = nodeName;
			if (variant != null) {
				out += "@" + variant; //$NON-NLS-1$
			}
			return out + ": " + getContent(); //$NON-NLS-1$
		}
	}
	
	private static class VirtualParameter extends ListenerParameter {
		VirtualParameter(String inNodeName, ListenerParameter inParent) {
			super(inNodeName, null, inParent);
		}
		@Override
		ListenerParameter getParent() {
			ListenerParameter outParent = super.getParent();
			outParent.dispose();
			parent = null;
			return outParent;
		}
		@Override
		void dispose() {
			children.clear();
		}
	}

}
