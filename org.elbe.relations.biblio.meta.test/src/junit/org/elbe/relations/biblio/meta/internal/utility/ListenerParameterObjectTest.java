package org.elbe.relations.biblio.meta.internal.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObject.ListenerParameter;
import org.junit.jupiter.api.Test;

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

        assertEquals(NAME, lName.getContent("defaultName"));
        assertEquals(NAME_FIRST, lName.getContent("firstName"));
        assertEquals(NAME_FAMILY, lName.getContent("familyName"));
        assertEquals(ROLE, lName.getContent("roleTerm"));
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

        assertEquals(PART_VOLUME,
                lPart.getContent("part/volume/value"));
        assertNull(lPart.getContent("part/number1/value"));
        assertEquals(PART_NUMBER, lPart.getContent("part/number2/value"));
        assertEquals(PAGES_START, lPart.getContent("part/pages/start"));
        assertEquals(PAGES_END, lPart.getContent("part/pages/end"));
        assertEquals(PART_DATE, lPart.getContent("part/date"));
    }

    @Test
    public void testParent() throws Exception {
        final ListenerParameterObject lParameter = new ListenerParameterObject();
        final ListenerParameter lChild1 = lParameter.addParameter("child1",
                "node", null);
        assertEquals(0, lChild1.children.size());

        lParameter.prepare("node", null);
        assertEquals(0, lChild1.children.size());

        lParameter.prepare("childNode", null);
        assertEquals(1, lChild1.children.size());
        assertNull(lChild1.parent);

        final ListenerParameter lChild2 = lChild1.children.get("childNode");
        assertEquals("org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObject$ListenerParameter",
                lChild1.getClass().getName());
        assertEquals(
                "org.elbe.relations.biblio.meta.internal.utility.ListenerParameterObject$VirtualParameter",
                lChild2.getClass().getName());

        assertEquals(0, lChild2.children.size());
        assertNotNull(lChild2.parent);

        lParameter.prepare("subChildNode", null);
        assertEquals(1, lChild2.children.size());

        lParameter.unprepare("-");
        assertEquals(0, lChild2.children.size());
        assertEquals(1, lChild1.children.size());

        lParameter.unprepare("-");
        assertEquals(1, lChild1.children.size());

    }

}
