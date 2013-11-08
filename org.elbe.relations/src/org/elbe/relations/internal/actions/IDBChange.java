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
package org.elbe.relations.internal.actions;

import org.elbe.relations.internal.data.IDBSettings;
import org.elbe.relations.internal.utility.DBPreconditionException;

/**
 * Interface for actions to change the DB.
 * 
 * @author Luthiger
 */
public interface IDBChange extends ICommand {

	/**
	 * Set temporary DB settings.
	 * 
	 * @param inDBSettings
	 *            {@link IDBSettings}
	 */
	void setTemporarySettings(IDBSettings inDBSettings);

	/**
	 * Checks whether the preconditions to perform the action concerning the
	 * database are met. E.g. for an embedded DB we have to check that the
	 * catalog does not exist, or for an external DB we have to check whether we
	 * can connect with the provided settings.
	 * 
	 * @throws DBPreconditionException
	 */
	void checkPreconditions() throws DBPreconditionException;

	/**
	 * Restore the original DB settings, e.g. after an exception occurred.
	 */
	void restore();

}
