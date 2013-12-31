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
package org.elbe.relations.data.internal.bom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.Vector;

import org.elbe.relations.data.bom.JoinRelatedTerm1Home;
import org.elbe.relations.data.bom.JoinRelatedTermHome;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.impl.KeyObjectImpl;
import org.hip.kernel.exc.VException;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Luthiger
 */
public class JoinRelatedTermHomeTest {

	@BeforeClass
	public static void init() {
		@SuppressWarnings("unused")
		final DataHouseKeeper lData = DataHouseKeeper.INSTANCE;
	}

	@Test
	public void testCreateTestObjects() {
		final String lExpected = "SELECT tblTerm.TERMID, tblTerm.STITLE, tblTerm.STEXT, tblTerm.DTCREATION, tblTerm.DTMUTATION, tblRelation.RELATIONID, tblRelation.NITEM1, tblRelation.NTYPE1, tblRelation.NITEM2, tblRelation.NTYPE2 FROM tblRelation INNER JOIN tblTerm ON tblRelation.NITEM1 = tblTerm.TERMID WHERE tblRelation.NITEM2 = 32 AND tblRelation.NTYPE2 = 1 AND tblRelation.NTYPE1 = 1";
		final JoinRelatedTermHome lHome = new JoinRelatedTermHomeSub();
		final Iterator<Object> lTest = lHome.getTestObjects();
		assertEquals("test sql", lExpected, lTest.next());
	}

	private class JoinRelatedTermHomeSub extends JoinRelatedTerm1Home {
		public JoinRelatedTermHomeSub() {
			super();
		}

		@Override
		public Vector<Object> createTestObjects() {
			final Vector<Object> outTest = new Vector<Object>();
			try {
				final KeyObject lKey = new KeyObjectImpl();
				lKey.setValue(RelationHome.KEY_ITEM2, new Integer(32));
				lKey.setValue(RelationHome.KEY_TYPE2, new Integer(1));
				lKey.setValue(RelationHome.KEY_TYPE1, new Integer(1));
				outTest.add(createSelectString(lKey));
			}
			catch (final VException exc) {
				fail(exc.getMessage());
			}
			return outTest;
		}
	}

}
