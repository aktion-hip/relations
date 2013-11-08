/*
 This package is part of the Relations application.
 Copyright (C) 2005, Benno Luthiger

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

package org.elbe.relations;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Accessor class for the plugins externalized strings.
 * 
 * @author Benno Luthiger
 * Created on Dec 12, 2005
 */
public class RelationsMessages {
	private static final String BUNDLE_NAME = "RelationsMessages";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Returns the translated message to the specified key.
	 * 
	 * @param inKey String Message key
	 * @return String
	 */
	public static String getString(String inKey) {
		try {
			return RESOURCE_BUNDLE.getString(inKey);
		}
		catch (MissingResourceException exc) {
			return '!' + inKey + '!';
		}
	}
	
	/**
	 * Returns the key's message translated and concatenated according to the specified parameters.
	 * 
	 * @param inKey String Message key
	 * @param inParams Object[] the parameters
	 * @return String
	 */
	public static String getString(String inKey, Object[] inParams) {
		if (inParams == null) {
			return getString(inKey);
		}
		try {
			return MessageFormat.format(getString(inKey), inParams);
		}
		catch (Exception exc) {
			return '!' + inKey + '!';
		}
	}
	
	/**
	 * Returns the key's message translated and concatenated according to the specified parameters.
	 * The message formatter uses the specified Locale for formatting.
	 * 
	 * @param inKey String
	 * @param inLocale Locale
	 * @param inParams Object[]
	 * @return String
	 */
	public static String getString(String inKey, Locale inLocale, Object[] inParams) {
		if (inParams == null) {
			return getString(inKey);
		}
		try {
			MessageFormat lFormat = new MessageFormat(getString(inKey), inLocale);
			return new String(lFormat.format(inParams, new StringBuffer(), null));
		}
		catch (Exception exc) {
			return '!' + inKey + '!';
		}		
	}
	
}