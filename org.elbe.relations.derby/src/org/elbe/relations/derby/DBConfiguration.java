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
package org.elbe.relations.derby;

import org.elbe.relations.data.db.IDBObjectCreator;
import org.elbe.relations.services.IDBConnectionConfig;

/**
 * The OSGi provider for the
 * <code>org.elbe.relations.services.IDBCreateService</code>.
 * 
 * @author Luthiger
 */
public class DBConfiguration implements IDBConnectionConfig {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.services.IDBCreateService#getName()
	 */
	@Override
	public String getName() {
		return "org.apache.derby.jdbc.EmbeddedDriver/Derby (embedded)/10.5.1.1"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.services.IDBCreateService#getJDBCDriverClass()
	 */
	@Override
	public String getJDBCDriverClass() {
		return "org.apache.derby.jdbc.EmbeddedDriver";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.services.IDBCreateService#getSubprotocol()
	 */
	@Override
	public String getSubprotocol() {
		return "derby";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.services.IDBCreateService#isEmbedded()
	 */
	@Override
	public boolean isEmbedded() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.services.IDBCreateService#getCreator()
	 */
	@Override
	public IDBObjectCreator getCreator() {
		return new DerbyCreator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.elbe.relations.services.IDBCreateService#canSetIdentityField()
	 */
	@Override
	public boolean canSetIdentityField() {
		return false;
	}

}
