package org.elbe.relations.internal.utility;

import static org.junit.Assert.assertEquals;

import org.elbe.relations.data.bom.AbstractItem;
import org.elbe.relations.data.bom.AbstractPerson;
import org.elbe.relations.data.bom.AbstractTerm;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test
 * 
 * @author lbenno
 */
public class RelatedItemHelperTest {

	private static DataHouseKeeper data;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		data = DataHouseKeeper.INSTANCE;
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
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
		assertEquals("number 0", 0, lHome.getCount());
		lHome.newRelation(lTerm3, lTerm1);
		lHome.newRelation(lTerm3, lTerm2);
		lHome.newRelation(lTerm3, lPerson);
		lHome.newRelation(lTerm3, lText);
		lHome.newRelation(lTerm5, lTerm3);
		lHome.newRelation(lTerm6, lTerm3);
		lHome.newRelation(lPerson, lText);
		lHome.newRelation(lTerm2, lText);
		lHome.newRelation(lTerm4, lTerm2);
		assertEquals("number 1", 9, lHome.getCount());

		assertEquals("number of related terms 1", 4, RelatedItemHelper
		        .getRelatedTerms(lTerm3).toArray().length);
		assertEquals("number of related terms 2", 2, RelatedItemHelper
		        .getRelatedTerms(lTerm2).toArray().length);
		assertEquals("number of related persons", 1, RelatedItemHelper
		        .getRelatedPersons(lTerm3).toArray().length);
		assertEquals("number of related texts 1", 1, RelatedItemHelper
		        .getRelatedTexts(lTerm3).toArray().length);
		assertEquals("number of related texts 2", 1, RelatedItemHelper
		        .getRelatedTexts(lTerm2).toArray().length);
		assertEquals("number of related 1", 6, RelatedItemHelper
		        .getRelatedItems(lTerm3).toArray().length);
		assertEquals("number of related 2", 3, RelatedItemHelper
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
