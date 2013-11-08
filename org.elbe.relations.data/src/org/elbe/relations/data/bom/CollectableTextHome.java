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
package org.elbe.relations.data.bom;

import java.sql.SQLException;

import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.bom.impl.AlternativeQueryResult;
import org.hip.kernel.bom.impl.AlternativeQueryStatement;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;

/**
 * Specialized implementation of <code>TextHome</code> to create
 * <code>LightWeightText</code>s.
 * 
 * @author Benno Luthiger Created on Sep 23, 2004
 */
public class CollectableTextHome extends TextHome {

	/**
	 * 
	 */
	public CollectableTextHome() {
		super();
	}

	/**
	 * Overrides the super class method to create an AlternativeQueryStatement.
	 * 
	 * @return org.hip.kernel.bom.QueryStatement
	 */
	@Override
	public QueryStatement createQueryStatement() {
		return new AlternativeQueryStatement(this);
	}

	/**
	 * Overrides the super class method to create an AlternativeQueryResult.
	 * 
	 * @param inStatement
	 *            QueryStatement
	 * @return QueryResult
	 * @throws SQLException
	 */
	@Override
	public QueryResult select(final QueryStatement inStatement)
			throws SQLException {
		if (VSys.assertNotNull(this, "select(QueryStatement)", inStatement) == Assert.FAILURE) //$NON-NLS-1$
			return new AlternativeQueryResult(null, null, null);

		final QueryResult outResult = inStatement.executeQuery();
		return outResult;
	}
}
