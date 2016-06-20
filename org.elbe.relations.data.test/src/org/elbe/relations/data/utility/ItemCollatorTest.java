package org.elbe.relations.data.utility;

import static org.junit.Assert.assertEquals;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.elbe.relations.data.bom.AbstractItem;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.Person;
import org.elbe.relations.data.bom.PersonHome;
import org.elbe.relations.data.bom.Term;
import org.elbe.relations.data.bom.TermHome;
import org.hip.kernel.exc.VException;
import org.junit.Test;

/**
 * JUnit test
 *
 * @author lbenno
 */
public class ItemCollatorTest {
	private final static String[] UNSORTED = new String[] { "Item 'Zuberbühler'", "Item 'Zuber'", "Item 'MMM'",
			"Item 'Müller'", "Item 'mmm'" };
	private final static String[] SORTED = new String[] { "Item 'mmm'", "Item 'MMM'", "Item 'Müller'", "Item 'Zuber'",
			"Item 'Zuberbühler'" };

	@Test
	public void testSort() throws Exception {
		final List<IItem> lTest = new ArrayList<IItem>();
		lTest.add(createPerson("Zuberbühler", ""));
		lTest.add(createTerm("Zuber"));
		lTest.add(createTerm("MMM"));
		lTest.add(createPerson("Müller", ""));
		lTest.add(createTerm("mmm"));
		assertEqualList("unsorted", UNSORTED, lTest);

		final Collator lCollator = new ItemCollator();
		lCollator.setStrength(Collator.SECONDARY);
		Collections.sort(lTest, lCollator);
		assertEqualList("sorted", SORTED, lTest);
	}

	private IItem createPerson(final String inName, final String inFirstname) throws VException {
		final AbstractItem outPerson = new Person();
		outPerson.set(PersonHome.KEY_NAME, inName);
		outPerson.set(PersonHome.KEY_FIRSTNAME, inFirstname);
		return outPerson;
	}

	private IItem createTerm(final String inTitle) throws VException {
		final AbstractItem outTerm = new Term();
		outTerm.set(TermHome.KEY_TITLE, inTitle);
		return outTerm;
	}

	private void assertEqualList(final String inMessage, final String[] inArray, final List<IItem> inList) {
		int i = 0;
		for (final IItem lItem : inList) {
			assertEquals(inMessage + " " + i, inArray[i++], lItem.toString());
		}
	}

}
