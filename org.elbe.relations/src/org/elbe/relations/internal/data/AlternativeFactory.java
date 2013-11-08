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
package org.elbe.relations.internal.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.elbe.relations.internal.bom.LightWeightPersonWithIcon;
import org.elbe.relations.internal.bom.LightWeightTermWithIcon;
import org.elbe.relations.internal.bom.LightWeightTextWithIcon;
import org.hip.kernel.bom.AlternativeModel;
import org.hip.kernel.bom.AlternativeModelFactory;

/**
 * Utility class providing the alternative factory classes.
 * 
 * @author Luthiger
 */
public final class AlternativeFactory {

	private AlternativeFactory() {
	}

	static class TermModelFactory implements AlternativeModelFactory {
		@Override
		public AlternativeModel createModel(final ResultSet inResultSet)
				throws SQLException {
			return new LightWeightTermWithIcon(inResultSet.getLong("TermID"), //$NON-NLS-1$
					inResultSet.getString("sTitle"), //$NON-NLS-1$
					inResultSet.getString("sText"), //$NON-NLS-1$
					inResultSet.getTimestamp("dtCreation"), //$NON-NLS-1$
					inResultSet.getTimestamp("dtMutation")); //$NON-NLS-1$
		}
	}

	static class TextModelFactory implements AlternativeModelFactory {
		@Override
		public AlternativeModel createModel(final ResultSet inResultSet)
				throws SQLException {
			return new LightWeightTextWithIcon(inResultSet.getLong("TextID"), //$NON-NLS-1$
					inResultSet.getString("sTitle"), //$NON-NLS-1$
					inResultSet.getString("sText"), //$NON-NLS-1$
					inResultSet.getString("sAuthor"), //$NON-NLS-1$
					inResultSet.getString("sCoAuthors"), //$NON-NLS-1$
					inResultSet.getString("sSubtitle"), //$NON-NLS-1$
					inResultSet.getString("sYear"), //$NON-NLS-1$
					inResultSet.getString("sPublication"), //$NON-NLS-1$
					inResultSet.getString("sPages"), //$NON-NLS-1$
					inResultSet.getInt("nVolume"), //$NON-NLS-1$
					inResultSet.getInt("nNumber"), //$NON-NLS-1$
					inResultSet.getString("sPublisher"), //$NON-NLS-1$
					inResultSet.getString("sPlace"), //$NON-NLS-1$
					inResultSet.getInt("nType"), //$NON-NLS-1$
					inResultSet.getTimestamp("dtCreation"), //$NON-NLS-1$
					inResultSet.getTimestamp("dtMutation")); //$NON-NLS-1$
		}
	}

	static class PersonModelFactory implements AlternativeModelFactory {
		@Override
		public AlternativeModel createModel(final ResultSet inResultSet)
				throws SQLException {
			return new LightWeightPersonWithIcon(
					inResultSet.getLong("PersonID"), //$NON-NLS-1$
					inResultSet.getString("sName"), //$NON-NLS-1$
					inResultSet.getString("sFirstname"), //$NON-NLS-1$
					inResultSet.getString("sText"), //$NON-NLS-1$
					inResultSet.getString("sFrom"), //$NON-NLS-1$
					inResultSet.getString("sTo"), //$NON-NLS-1$
					inResultSet.getTimestamp("dtCreation"), //$NON-NLS-1$
					inResultSet.getTimestamp("dtMutation")); //$NON-NLS-1$
		}
	}

}
