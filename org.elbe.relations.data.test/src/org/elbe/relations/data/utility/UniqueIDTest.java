package org.elbe.relations.data.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


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

        assertEquals(lID1, lID2);
        assertEquals(lExpected, lID1.toString());
        assertEquals(lExpected, lID2.toString());
        assertEquals(lExpected, UniqueID.getStringOf(1, lItemID));

        assertFalse(lID1.equals(lID3));
        assertFalse(lID1.equals(lID4));
    }

}
