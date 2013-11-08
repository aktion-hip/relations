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
package org.elbe.relations.data;

import java.util.Locale;

/**
 * Constants used by the Relations application.
 * 
 * @author Benno Luthiger Created on Oct 13, 2004
 */
public interface Constants {
	// The application's update site
	public final static String UPDATE_SITE = "http://relations-rcp.sourceforge.net/updates/"; //$NON-NLS-1$

	public final static String MAIN_ID = "org.elbe.relations"; //$NON-NLS-1$

	// Keys used in the preference store
	public final static String KEY_LANGUAGE_CONTENT = "uiContentLanguage"; //$NON-NLS-1$
	public final static String KEY_BIBLIO_SCHEMA = "activeBiblioSchemaID"; //$NON-NLS-1$
	public final static String KEY_DB_PLUGIN_ID = "activeDBPluginID"; //$NON-NLS-1$
	public final static String KEY_DB_HOST = "org.hip.vif.db.driver"; //$NON-NLS-1$
	public final static String KEY_DB_CATALOG = "org.hip.vif.db.url"; //$NON-NLS-1$
	public final static String KEY_DB_USER_NAME = "org.hip.vif.db.userId"; //$NON-NLS-1$
	public final static String KEY_DB_PASSWORD = "org.hip.vif.db.password"; //$NON-NLS-1$
	public final static String KEY_DB_EMBEDDED_CATALOG = "dbEmbeddedCatalog"; //$NON-NLS-1$
	public final static String KEY_TEXT_FONT_SIZE = "fontSizeText"; //$NON-NLS-1$
	public final static String KEY_MAX_SEARCH_HITS = "maxSearchHits"; //$NON-NLS-1$
	public final static String KEY_MAX_LAST_CHANGED = "maxLastChangedEntries"; //$NON-NLS-1$

	public final static String KEY_PRINT_OUT_PLUGIN_ID = "activePrintOutPluginID"; //$NON-NLS-1$

	public final static Locale[] LOCALES = { Locale.ENGLISH, Locale.GERMAN };

	public final static int ITEM_HEIGHT = 30;
	public final static int ITEM_WIDTH = 120;
	public final static int RADIUS = 140;

	public final static String DERBY_STORE = "data"; //$NON-NLS-1$
	public final static String LUCENE_STORE = "index"; //$NON-NLS-1$

	public final static int DIALOG_HISTORY_LENGTH = 11;

	// IDs for the status line items
	public static final String STATUS_ITEM_DB_NAME = "StatusItemDBName"; //$NON-NLS-1$
	public static final String STATUS_ITEM_DB_SIZE = "StatusItemDBSize"; //$NON-NLS-1$
	public static final String STATUS_ITEM_MSG = "StatusItemMessage"; //$NON-NLS-1$
	public static final String STATUS_ITEM_KEYB = "StatusItemKeyboardStatus"; //$NON-NLS-1$

	// IDs for global coolbars
	public static final String COOLBAR_STYLE = "org.elbe.relations.toolbar.style"; //$NON-NLS-1$

	// IDs of extension points
	public static final String EXTENSION_POINT_ID_BROWSERS = "browsers"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_ID_CONFIGURATION = "configuration"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_ID_BIBLIOGRAPHY = "bibliography"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_ID_PRINT_OUT = "printOut"; //$NON-NLS-1$

	// Default values for the application's preference store
	public final static String DFT_LANGUAGE = "en"; //$NON-NLS-1$
	public final static String DFT_DB_EMBEDDED = "default_db"; //$NON-NLS-1$
	public static final String DFT_BROWSER_PLUGIN_ID = "org.elbe.relations.defaultBrowser"; //$NON-NLS-1$
	public static final String DFT_DBCONFIG_PLUGIN_ID = "org.elbe.relations.derby"; //$NON-NLS-1$
	public static final String DFT_BIBLIO_SCHEMA_ID = "org.elbe.relations.biblio.dft:standard.en"; //$NON-NLS-1$
	public static final String DFT_PRINT_OUT_PLUGIN_ID = "org.elbe.relations.print.dft"; //$NON-NLS-1$
	public static final int DFT_MAX_SEARCH_HITS = 100;
	public static final int DFT_MAX_LAST_CHANGED = 20;

	// database object creation
	public static final String NODE_NAME_CREATED_OBJECT = "CreateObject"; //$NON-NLS-1$
	public static final String XML_CREATE_OBJECTS = "dbCreateObjects.xml"; //$NON-NLS-1$

	// variables for evaluation context in RelationsBrowserManager
	public static final String BROWSER_ITEM_SELECTED = "selectedItem"; //$NON-NLS-1$
	public static final String BROWSER_ITEM_CENTER = "centerItem"; //$NON-NLS-1$
	public static final String BROWSER_EVENT = "relationsBrowserEvent"; //$NON-NLS-1$
	public static final String SELECTION_EVENT = "relationsSelectionEvent"; //$NON-NLS-1$

	public static final String HISTORY_EVENT = "relationsHistoryEvent"; //$NON-NLS-1$
	public static final String HISTORY_BACK = "historyBack"; //$NON-NLS-1$
	public static final String HISTORY_FORWARD = "historyForward"; //$NON-NLS-1$

	public static final int DFT_TEXT_FONT_SIZE = 8;
	public static final String[] INIT_SIZES = new String[] {
			"6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$
}
