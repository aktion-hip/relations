package org.elbe.relations.data.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * JUnit test
 * 
 * @author lbenno
 */
public class UniqueIDTest {

	@Test
	public void test() {
		final String lExpected = "1:5989";
		final long lItemID = 5989L;
		final UniqueID lID1 = new UniqueID(1, lItemID);
		final UniqueID lID2 = new UniqueID(lExpected);
		final UniqueID lID3 = new UniqueID(2, lItemID);
		final UniqueID lID4 = new UniqueID(1, lItemID + 1);

		assertEquals("equal ID 1", lID1, lID2);
		assertEquals("equal ID 2", lExpected, lID1.toString());
		assertEquals("equal to string 1", lExpected, lID2.toString());
		assertEquals("equal to string 2", lExpected,
		        UniqueID.getStringOf(1, lItemID));

		assertFalse("not equal 1", lID1.equals(lID3));
		assertFalse("not equal 2", lID1.equals(lID4));
	}

}
