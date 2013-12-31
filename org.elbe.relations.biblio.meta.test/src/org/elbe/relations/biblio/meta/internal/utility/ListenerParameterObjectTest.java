package org.elbe.relations.biblio.meta.internal.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObject.ListenerParameter;
import org.junit.Test;

public class ListenerParameterObjectTest {
	private static final String NAME = "Einstein, A.";
	private static final String NAME_FIRST = "Albert";
	private static final String NAME_FAMILY = "Einstein";
	private static final String ROLE = "Creator";

	private static final String PART_VOLUME = "3";
	private static final String PART_NUMBER = "12";
	private static final String PAGES_START = "53";
	private static final String PAGES_END = "57";
	private static final String PART_DATE = "Jan. 2003";

	@Test
	public void testObject() throws Exception {
		final ListenerParameterObject lName = new ListenerParameterObject();
		lName.addParameter("defaultName", "namePart", null);
		lName.addParameter("familyName", "namePart", "family");
		lName.addParameter("firstName", "namePart", "given");

		lName.addParameter("role", "role", null);
		lName.addParameter("roleTerm", "roleTerm", "text");

		lName.prepare("namePart", null);
		lName.addCharacters(NAME.toCharArray(), 0, NAME.length());

		lName.unprepare("namePart");
		lName.prepare("namePart", "family");
		lName.addCharacters(NAME_FAMILY.toCharArray(), 0, NAME_FAMILY.length());

		lName.unprepare("namePart");
		lName.prepare("namePart", "given");
		lName.addCharacters(NAME_FIRST.toCharArray(), 0, NAME_FIRST.length());

		lName.unprepare("namePart");
		lName.prepare("roleTerm", "text");
		lName.addCharacters(ROLE.toCharArray(), 0, ROLE.length());

		assertEquals("defaultName", NAME, lName.getContent("defaultName"));
		assertEquals("firstName", NAME_FIRST, lName.getContent("firstName"));
		assertEquals("familyName", NAME_FAMILY, lName.getContent("familyName"));
		assertEquals("roleTerm", ROLE, lName.getContent("roleTerm"));
	}

	@Test
	public void testChildren() throws Exception {
		final ListenerParameterObject lPart = new ListenerParameterObject();
		final ListenerParameter lParameter = lPart.addParameter("part", "part",
		        null);
		final ListenerParameter lVolume = lParameter.addChild("volume",
		        "detail", "volume");
		lVolume.addChild("value", "number", null);
		final ListenerParameter lNumberIssue = lParameter.addChild("number1",
		        "detail", "issue");
		lNumberIssue.addChild("value", "number", null);
		final ListenerParameter lNumberNumber = lParameter.addChild("number2",
		        "detail", "number");
		lNumberNumber.addChild("value", "number", null);
		final ListenerParameter lPages = lParameter.addChild("pages", "extent",
		        null);
		lPages.addChild("start", "start", null);
		lPages.addChild("end", "end", null);
		lParameter.addChild("date", "date", null);

		lPart.prepare("part", null);
		lPart.prepare("detail", "volume");
		lPart.prepare("number", null);
		lPart.addCharacters(PART_VOLUME.toCharArray(), 0, PART_VOLUME.length());

		lPart.unprepare("number");
		lPart.prepare("caption", null);
		lPart.addCharacters("vol.".toCharArray(), 0, 4);

		lPart.unprepare("caption");
		lPart.unprepare("detail");
		lPart.prepare("detail", "number");
		lPart.prepare("number", null);
		lPart.addCharacters(PART_NUMBER.toCharArray(), 0, PART_NUMBER.length());

		lPart.unprepare("number");
		lPart.unprepare("detail");
		lPart.prepare("extent", null);
		lPart.prepare("start", null);
		lPart.addCharacters(PAGES_START.toCharArray(), 0, PAGES_START.length());

		lPart.unprepare("start");
		lPart.prepare("end", null);
		lPart.addCharacters(PAGES_END.toCharArray(), 0, PAGES_END.length());

		lPart.unprepare("extent");
		lPart.unprepare("end");
		lPart.prepare("date", null);
		lPart.addCharacters(PART_DATE.toCharArray(), 0, PART_DATE.length());

		assertEquals("part/volume/value", PART_VOLUME,
		        lPart.getContent("part/volume/value"));
		assertNull("part/number1/value", lPart.getContent("part/number1/value"));
		assertEquals("part/number2/value", PART_NUMBER,
		        lPart.getContent("part/number2/value"));
		assertEquals("part/pages/start", PAGES_START,
		        lPart.getContent("part/pages/start"));
		assertEquals("part/pages/end", PAGES_END,
		        lPart.getContent("part/pages/end"));
		assertEquals("part/date", PART_DATE, lPart.getContent("part/date"));
	}

	@Test
	public void testParent() throws Exception {
		final ListenerParameterObject lParameter = new ListenerParameterObject();
		final ListenerParameter lChild1 = lParameter.addParameter("child1",
		        "node", null);
		assertEquals("child 1: no children", 0, lChild1.children.size());

		lParameter.prepare("node", null);
		assertEquals("child 1: still no children", 0, lChild1.children.size());

		lParameter.prepare("childNode", null);
		assertEquals("child 1: has children", 1, lChild1.children.size());
		assertNull("child 1: no parent", lChild1.parent);

		final ListenerParameter lChild2 = lChild1.children.get("childNode");
		assertEquals(
		        "child 1 is normal",
		        "org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObject$ListenerParameter",
		        lChild1.getClass().getName());
		assertEquals(
		        "child 2 is virtual",
		        "org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObject$VirtualParameter",
		        lChild2.getClass().getName());

		assertEquals("child 2: no children", 0, lChild2.children.size());
		assertNotNull("child 2: has parent", lChild2.parent);

		lParameter.prepare("subChildNode", null);
		assertEquals("child 2: has children", 1, lChild2.children.size());

		lParameter.unprepare("-");
		assertEquals("child 2: virtual children are cleared", 0,
		        lChild2.children.size());
		assertEquals("child 1: still has children", 1, lChild1.children.size());

		lParameter.unprepare("-");
		assertEquals("child 1: normal children aren't cleared", 1,
		        lChild1.children.size());

	}

}
