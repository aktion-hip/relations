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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elbe.relations.data.bom.JoinRelatedText1Home;
import org.elbe.relations.data.bom.JoinRelatedTextHome;
import org.elbe.relations.data.bom.RelationHome;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.impl.KeyObjectImpl;
import org.hip.kernel.exc.VException;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Luthiger
 */
public class JoinRelatedTextHomeTest {

	@BeforeClass
	public static void init() {
	}

	@Test
	public void testCreateTestObjects() {
		final String lExpected = "SELECT tblText.TEXTID, tblText.STITLE, tblText.STEXT, tblText.SAUTHOR, tblText.SCOAUTHORS, tblText.SSUBTITLE, tblText.SYEAR, tblText.SPUBLICATION, tblText.SPAGES, tblText.NVOLUME, tblText.NNUMBER, tblText.SPUBLISHER, tblText.SPLACE, tblText.NTYPE, tblText.DTCREATION, tblText.DTMUTATION, tblRelation.RELATIONID, tblRelation.NITEM1, tblRelation.NTYPE1, tblRelation.NITEM2, tblRelation.NTYPE2 FROM tblRelation INNER JOIN tblText ON tblRelation.NITEM1 = tblText.TEXTID WHERE tblRelation.NITEM2 = 32 AND tblRelation.NTYPE2 = 1 AND tblRelation.NTYPE1 = 1";
		final JoinRelatedTextHome lHome = new JoinRelatedTextHomeSub();
		final Iterator<Object> lTest = lHome.getTestObjects();
		assertEquals("test sql", lExpected, lTest.next());
	}

	private class JoinRelatedTextHomeSub extends JoinRelatedText1Home {
		public JoinRelatedTextHomeSub() {
			super();
		}

		@Override
		public List<Object> createTestObjects() {
			final List<Object> outTest = new ArrayList<Object>();
			try {
				final KeyObject lKey = new KeyObjectImpl();
				lKey.setValue(RelationHome.KEY_ITEM2, new Integer(32));
				lKey.setValue(RelationHome.KEY_TYPE2, new Integer(1));
				lKey.setValue(RelationHome.KEY_TYPE1, new Integer(1));
				outTest.add(createSelectString(lKey));
			} catch (final VException exc) {
				fail(exc.getMessage());
			}
			return outTest;
		}
	}

}
