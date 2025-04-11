package org.elbe.relations.internal.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.elbe.relations.data.bom.AbstractItem;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * JUnit test
 *
 * @author lbenno
 */
public class RelatedItemHelperTest {

    private static DataHouseKeeper data;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        data = DataHouseKeeper.INSTANCE;
    }

    @AfterEach
    public void tearDown() throws Exception {
        data.deleteAllInAll();
    }

    @Test
    public void test() throws Exception {
        final AbstractTerm lTerm1 = data.createTerm("Term 1");
        final AbstractTerm lTerm2 = data.createTerm("Term 2");
        final AbstractTerm lTerm3 = data.createTerm("Term 3");
        final AbstractTerm lTerm4 = data.createTerm("Term 4");
        final AbstractTerm lTerm5 = data.createTerm("Term 5");
        final AbstractTerm lTerm6 = data.createTerm("Term 6");
        final AbstractPerson lPerson = data.createPerson("Pan", "Peter");
        final AbstractItem lText = data.createText("The Book", "Smith");

        final RelationHome lHome = data.getRelationHome();
        assertEquals(0, lHome.getCount());
        lHome.newRelation(lTerm3, lTerm1);
        lHome.newRelation(lTerm3, lTerm2);
        lHome.newRelation(lTerm3, lPerson);
        lHome.newRelation(lTerm3, lText);
        lHome.newRelation(lTerm5, lTerm3);
        lHome.newRelation(lTerm6, lTerm3);
        lHome.newRelation(lPerson, lText);
        lHome.newRelation(lTerm2, lText);
        lHome.newRelation(lTerm4, lTerm2);
        assertEquals(9, lHome.getCount());

        assertEquals(4, RelatedItemHelper
                .getRelatedTerms(lTerm3).toArray().length);
        assertEquals(2, RelatedItemHelper
                .getRelatedTerms(lTerm2).toArray().length);
        assertEquals(1, RelatedItemHelper
                .getRelatedPersons(lTerm3).toArray().length);
        assertEquals(1, RelatedItemHelper
                .getRelatedTexts(lTerm3).toArray().length);
        assertEquals(1, RelatedItemHelper
                .getRelatedTexts(lTerm2).toArray().length);
        assertEquals(6, RelatedItemHelper
                .getRelatedItems(lTerm3).toArray().length);
        assertEquals(3, RelatedItemHelper
                .getRelatedItems(lText).toArray().length);
    }

    @Test
    public void testGetRelatedSort() throws Exception {
        final AbstractTerm lTerm1 = data.createTerm("Term 1");
        final AbstractTerm lTerm2 = data.createTerm("Term 2");
        final AbstractTerm lTerm3 = data.createTerm("Term 3");
        final AbstractTerm lTerm5 = data.createTerm("Term 5");
        final AbstractTerm lTerm6 = data.createTerm("Term 6");
        final AbstractPerson lPerson = data.createPerson("Pan", "Peter");
        final AbstractItem lText = data.createText("The Book", "Smith");

        final RelationHome lHome = data.getRelationHome();
        lHome.newRelation(lTerm3, lTerm1);
        lHome.newRelation(lTerm3, lTerm2);
        lHome.newRelation(lTerm3, lPerson);
        lHome.newRelation(lTerm3, lText);
        lHome.newRelation(lTerm5, lTerm3);
        lHome.newRelation(lTerm6, lTerm3);

        final Object[] lSorted = RelatedItemHelper.getRelatedItems(lTerm3)
                .toArray();
        final String[] lExpected = new String[] { "Item 'Pan, Peter'",
                "Item 'Term 1'", "Item 'Term 2'", "Item 'Term 5'",
                "Item 'Term 6'", "Item 'The Book'" };
        for (int i = 0; i < lSorted.length; i++) {
            assertEquals("sorted item " + i, lExpected[i],
                    lSorted[i].toString());
        }
    }

}
