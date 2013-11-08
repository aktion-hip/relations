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
package org.elbe.relations;

import java.util.Locale;

/**
 * Constants used by the Relations application.
 * 
 * @author Benno Luthiger Created on Oct 13, 2004
 */
public final class RelationsConstants {

	private RelationsConstants() {
	};

	// The application's update site
	public final static String UPDATE_SITE = "http://relations-rcp.sourceforge.net/updates/"; //$NON-NLS-1$

	// Keys used in the preference store
	public final static String KEY_LANGUAGE_CONTENT = "uiContentLanguage"; //$NON-NLS-1$
	public final static String KEY_BIBLIO_SCHEMA = "activeBiblioSchemaID"; //$NON-NLS-1$
	public final static String KEY_DB_PLUGIN_ID = "activeDBPluginID"; //$NON-NLS-1$
	public final static String KEY_DB_HOST = "org.hip.vif.db.driver"; //$NON-NLS-1$
	public final static String KEY_DB_CATALOG = "org.hip.vif.db.url"; //$NON-NLS-1$
	public final static String KEY_DB_USER_NAME = "org.hip.vif.db.userId"; //$NON-NLS-1$
	public final static String KEY_DB_PASSWORD = "org.hip.vif.db.password"; //$NON-NLS-1$
	public final static String KEY_TEXT_FONT_SIZE = "fontSizeText"; //$NON-NLS-1$
	public final static String KEY_MAX_SEARCH_HITS = "maxSearchHits"; //$NON-NLS-1$
	public final static String KEY_MAX_LAST_CHANGED = "maxLastChangedEntries"; //$NON-NLS-1$
	public final static String KEY_DB_EMBEDDED_CATALOG = "dbEmbeddedCatalog"; //$NON-NLS-1$

	public final static String KEY_PRINT_OUT_PLUGIN_ID = "activePrintOutPluginID"; //$NON-NLS-1$

	public final static Locale[] LOCALES = { Locale.ENGLISH, Locale.GERMAN };

	public final static int ITEM_HEIGHT = 30;
	public final static int ITEM_WIDTH = 120;
	public final static int RADIUS = 140;

	public final static String DERBY_STORE = "data"; //$NON-NLS-1$
	public final static String LUCENE_STORE = "index"; //$NON-NLS-1$

	public final static int DIALOG_HISTORY_LENGTH = 11;

	// IDs for the status line items
	public static final String STATUS_ITEM_DB_NAME = "org.elbe.relations.status.dbName"; //$NON-NLS-1$
	public static final String STATUS_ITEM_DB_SIZE = "org.elbe.relations.status.dbSize"; //$NON-NLS-1$
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
	public static final String PREFERENCE_NODE = "org.elbe.relations"; //$NON-NLS-1$
	public static final String PREFERENCE_NODE_DIALOG = "org.elbe.relations.dialogsettings"; //$NON-NLS-1$
	public final static String DFT_LANGUAGE = Locale.ENGLISH.getLanguage(); //$NON-NLS-1$
	public final static String DFT_DB_EMBEDDED = "default_db"; //$NON-NLS-1$
	public static final String DFT_BROWSER_PLUGIN_ID = "org.elbe.relations.defaultBrowser"; //$NON-NLS-1$
	public static final String DFT_BROWSER_PLUGIN_CLASS = "org.elbe.relations.defaultbrowser.internal.DefaultBrowserPart"; //$NON-NLS-1$
	public static final String DFT_DBCONFIG_PLUGIN_ID = "org.apache.derby.jdbc.EmbeddedDriver/Derby (embedded)/10.5.1.1"; //$NON-NLS-1$
	public static final String DFT_BIBLIO_SCHEMA_ID = "relations.bibliography.standard.en"; //$NON-NLS-1$
	public static final String DFT_PRINT_OUT_PLUGIN_ID = "org.elbe.relations.print.dft"; //$NON-NLS-1$
	public static final int DFT_MAX_SEARCH_HITS = 100;
	public static final int DFT_MAX_LAST_CHANGED = 20;

	// ids of model elements
	public static final String PART_STACK_TOOLS = "tool.views";
	public static final String TRIM_STACK_TOOLS = "tool.views(null)";
	public static final String PART_STACK_BROWSERS = "relations.views";
	public static final String CONTAINER_PARTS = "relations.parts.container";
	public static final String PART_INSPECTOR = "inspector.view.part";
	public static final String PART_SEARCH = "tool.view.search";
	public static final String WINDOW_HELP = "relations.help";
	public static final String WINDOW_MAIN = "org.elbe.relations.window.main";
	public static final String RELATIONS_CONTRIBUTOR_URI = "platform:/plugin/org.elbe.relations";

	// database object creation
	public static final String NODE_NAME_CREATED_OBJECT = "CreateObject"; //$NON-NLS-1$

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

	// eclipse context variable
	public static final String DB_ACCESS_HANDLER = "relations.dbAccessHandler";

	// eclipse event topics
	public static final String TOPIC_WIZARD_PAGE_STATUS = "relations/topic/wizard/page/status/update";
	public static final String TOPIC_STYLE_CHANGE_INSPECTOR = "relations/topic/style/change/inspector";
	public static final String TOPIC_STYLE_CHANGE_FORM = "relations/topic/style/change/form";
	public static final String TOPIC_STYLE_CHANGED_INSPECTOR = "relations/topic/style/changed/inspector";
	public static final String TOPIC_STYLE_CHANGED_FORM = "relations/topic/style/changed/form";
	public static final String TOPIC_STYLE_ITEMS_INSPECTOR = "relations/topic/style/items/inspector";
	public static final String TOPIC_STYLE_ITEMS_FORM = "relations/topic/style/items/form";

	// eclipse event topics: data service
	public static final String TOPIC_DB_CHANGED_RELOAD = "relations/topic/db/changed/reload";
	public static final String TOPIC_DB_CHANGED_INITIALZED = "relations/topic/db/changed/initialized";
	public static final String TOPIC_DB_CHANGED_CREATED = "relations/topic/db/changed/created";
	public static final String TOPIC_DB_CHANGED_DELETED = "relations/topic/db/changed/deleted";
	public static final String TOPIC_DB_CHANGED_DB = "relations/topic/db/changed/db";

	// eclipse event topics: sent to browser manager
	public static final String TOPIC_TO_BROWSER_MANAGER_SET_MODEL = "relations/topic/to/browser/manager/set/model";
	public static final String TOPIC_TO_BROWSER_MANAGER_SET_TITLE = "relations/topic/to/browser/manager/set/title";
	public static final String TOPIC_TO_BROWSER_MANAGER_SEND_CENTER_ID = "relations/topic/to/browser/manager/send/center/id";
	public static final String TOPIC_TO_BROWSER_MANAGER_SEND_CENTER_MODEL = "relations/topic/to/browser/manager/send/center/model";
	public static final String TOPIC_TO_BROWSER_MANAGER_SET_SELECTED = "relations/topic/to/browser/manager/set/selected";
	// eclipse event topics: sent from browser manager
	public static final String TOPIC_FROM_BROWSER_MANAGER_SYNC_SELECTED = "relations/topic/from/browser/manager/sync/selected";
	public static final String TOPIC_FROM_BROWSER_MANAGER_SYNC_CONTENT = "relations/topic/from/browser/manager/sync/content";
	public static final String TOPIC_FROM_BROWSER_MANAGER_CLOSE = "relations/topic/from/browser/manager/close";
	public static final String TOPIC_FROM_BROWSER_MANAGER_CLEAR = "relations/topic/from/browser/manager/clear";
	public static final String TOPIC_FROM_BROWSER_MANAGER_SET_FONT_SIZE = "relations/topic/from/browser/manager/set/font/size";
	public static final String TOPIC_FROM_BROWSER_MANAGER_SEND_CENTER_MODEL = "relations/topic/from/browser/manager/send/center/model";

	// styled text variable ids and style command ids in Relations.e4xmi
	public static final String STYLED_TEXT_ITEM_BOLD = "relations.toolbar:text.styling.bold";
	public static final String STYLED_TEXT_ITEM_ITALIC = "relations.toolbar:text.styling.italic";
	public static final String STYLED_TEXT_ITEM_UNDERLINE = "relations.toolbar:text.styling.underline";
	public static final String STYLED_TEXT_ITEM_LIST_BULLET = "relations.toolbar:text.styling.list.bullet";
	public static final String STYLED_TEXT_ITEM_LIST_NUMBERED = "relations.toolbar:text.styling.list.number";
	public static final String STYLED_TEXT_ITEM_LIST_UPPER = "relations.toolbar:text.styling.list.chars.upper";
	public static final String STYLED_TEXT_ITEM_LIST_LOWER = "relations.toolbar:text.styling.list.chars.lower";

	public static final String STYLED_TEXT_COMMAND_BOLD = "org.elbe.relations.command.text.style.bold";
	public static final String STYLED_TEXT_COMMAND_ITALIC = "org.elbe.relations.command.text.style.italic";
	public static final String STYLED_TEXT_COMMAND_UNDERLINE = "org.elbe.relations.command.text.style.underline";
	public static final String STYLED_TEXT_COMMAND_LIST_BULLET = "org.elbe.relations.command.text.list.unordered";
	public static final String STYLED_TEXT_COMMAND_LIST_NUMBERED = "org.elbe.relations.command.text.list.ordered";
	public static final String STYLED_TEXT_COMMAND_LIST_UPPER = "org.elbe.relations.command.text.list.upper";
	public static final String STYLED_TEXT_COMMAND_LIST_LOWER = "org.elbe.relations.command.text.list.lower";

	public static final String FLAG_STYLED_TEXT_ACTIVE = "org.elbe.relations.flag.style.active";
	public static final String FLAG_INSPECTOR_TEXT_ACTIVE = "org.elbe.relations.flag.inspector.text";

	// names of command parameters
	public static final String PN_COMMAND_STYLE_SELECTION = "org.elbe.relations.command.text.style.selection";

	// popup menu ids
	public static final String POPUP_INSPECTOR = "org.elbe.relations.inspector.popup";
	public static final String POPUP_SELECTION_VIEW = "org.elbe.relations.selection.view.popup";
	public static final String POPUP_TOOLS_SEARCH = "org.elbe.relations.view.tools.popup.search";
	public static final String POPUP_TOOLS_BOOKMARKS = "org.elbe.relations.view.tools.popup.bookmarks";
	public static final String POPUP_TOOLS_CHANGES = "org.elbe.relations.view.tools.popup.changes";

	// command parameters
	public static final String PARAMETER_LAST_CHANGES = "org.elbe.relations.last.changes.parameter";
	public static final String PARAMETER_INSPECTOR_TEXT_SWITCH = "org.elbe.relations.inspector.text.switch.parameter";

}
