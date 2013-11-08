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

/**
 * Interface defining the application's command IDs. Key bindings can be defined
 * for specific commands. To associate an action with a command, use
 * IAction.setActionDefinitionId(commandId).
 * 
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public interface ICommandIds {
	public static final String CMD_ITEM_CENTER = "org.elbe.relations.command.item.center"; //$NON-NLS-1$
	public static final String CMD_ITEM_EDIT = "org.elbe.relations.command.item.edit"; //$NON-NLS-1$
	public static final String CMD_ITEM_SHOW = "org.elbe.relations.command.item.browse"; //$NON-NLS-1$
	public static final String CMD_RELATION_REMOVE = "org.elbe.relations.command.item.relation.remove"; //$NON-NLS-1$
	public static final String CMD_SEARCH = "org.elbe.relations.handler.reindex"; //$NON-NLS-1$

	// old
	public static final String CMD_EXIT = "org.elbe.relations.cmd.exit"; //$NON-NLS-1$
	public static final String CMD_DATA_OPEN = "org.elbe.relations.cmd.dbopen"; //$NON-NLS-1$
	public static final String CMD_DATA_DELETE = "org.elbe.relations.cmd.dbdelete"; //$NON-NLS-1$
	public static final String CMD_INDEX_DB = "org.elbe.relations.cmd.reindex"; //$NON-NLS-1$
	public static final String CMD_ITEM_NEW = "org.elbe.relations.cmd.new"; //$NON-NLS-1$
	public static final String CMD_TERM_NEW = "org.elbe.relations.cmd.newterm"; //$NON-NLS-1$
	public static final String CMD_TEXT_NEW = "org.elbe.relations.cmd.newtext"; //$NON-NLS-1$
	public static final String CMD_PERSON_NEW = "org.elbe.relations.cmd.newperson"; //$NON-NLS-1$
	public static final String CMD_ITEM_FIND = "org.elbe.relations.cmd.find"; //$NON-NLS-1$
	//	public static final String CMD_ITEM_SHOW = "org.elbe.relations.cmd.itemdisplay"; //$NON-NLS-1$
	public static final String CMD_ITEM_SHOW2 = "org.elbe.relations.cmd.itemdisplay2"; //$NON-NLS-1$

	public static final String CMD_ITEM_DELETEB = "org.elbe.relations.cmd.itemdeleteB"; //$NON-NLS-1$
	public static final String CMD_ITEM_DELETES = "org.elbe.relations.cmd.itemdeleteS"; //$NON-NLS-1$
	public static final String CMD_RELATIONS_EDIT = "org.elbe.relations.cmd.relationsedit"; //$NON-NLS-1$
	public static final String CMD_ITEM_PRINT = "org.elbe.relations.cmd.print"; //$NON-NLS-1$
	public static final String CMD_IMPORT = "org.elbe.relations.cmd.import"; //$NON-NLS-1$
	public static final String CMD_EXPORT = "org.elbe.relations.cmd.export"; //$NON-NLS-1$
	public static final String CMD_RELATION_ADD = "org.elbe.relations.cmd.relationadd"; //$NON-NLS-1$
	public static final String CMD_RELATION_DELETE = "org.elbe.relations.cmd.relationdelete"; //$NON-NLS-1$
	public static final String CMD_ITEM_BOOKMARK = "org.elbe.relations.cmd.bookmark"; //$NON-NLS-1$

	public static final String CMD_EDIT_SAVE = "org.elbe.relations.inspector.save"; //$NON-NLS-1$
	public static final String CMD_EDIT_UNDO = "org.elbe.relations.inspector.undo"; //$NON-NLS-1$

	public static final String CMD_STYLE = "org.elbe.relations.style"; //$NON-NLS-1$
	public static final String CMD_STYLE2 = "org.elbe.relations.style2"; //$NON-NLS-1$
	public static final String PARAMETER_STYLE = "org.elbe.relations.style.parameter"; //$NON-NLS-1$
	public static final String CMD_STYLE_BOLD = "bold"; //$NON-NLS-1$
	public static final String CMD_STYLE_ITALIC = "italic"; //$NON-NLS-1$
	public static final String CMD_STYLE_UNDERLINE = "underline"; //$NON-NLS-1$
	public static final String CMD_STYLE_UNORDERED = "unordered"; //$NON-NLS-1$
	public static final String CMD_STYLE_NUMBERED = "numbered"; //$NON-NLS-1$
	public static final String CMD_STYLE_LETTER_UPPER = "letter.upper"; //$NON-NLS-1$
	public static final String CMD_STYLE_LETTER_LOWER = "letter.lower"; //$NON-NLS-1$

	public static final String CMD_ASSOCIATIONS_ADD = "org.elbe.relations.associations.add"; //$NON-NLS-1$
	public static final String CMD_ASSOCIATIONS_REMOVE = "org.elbe.relations.associations.remove"; //$NON-NLS-1$

	public static final String CMD_HISTORY_BACK = "org.elbe.relations.cmd.history.back"; //$NON-NLS-1$
	public static final String CMD_HISTORY_FORWARD = "org.elbe.relations.cmd.history.forward"; //$NON-NLS-1$

	public static final String SCOPE_INSPECTOR = "relations.inspectorScope"; //$NON-NLS-1$
	public static final String SCOPE_BROWSER = "relations.browserScope"; //$NON-NLS-1$
	public static final String SCOPE_SELECTION = "relations.selectionScope"; //$NON-NLS-1$
	public static final String SCOPE_SEARCH = "relations.searchScope"; //$NON-NLS-1$
	public static final String SCOPE_ASSOCIATIONS = "relations.associationsScope"; //$NON-NLS-1$
	public static final String SCOPE_STYLED_TEXT = "relations.styledTextScope"; //$NON-NLS-1$
	public static final String SCOPE_BOOKMARKS = "relations.bookmarksScope"; //$NON-NLS-1$
	public static final String SCOPE_LAST_CHANGES = "relations.lastChangesScope"; //$NON-NLS-1$

}
