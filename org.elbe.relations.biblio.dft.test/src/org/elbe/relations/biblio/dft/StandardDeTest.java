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
package org.elbe.relations.biblio.dft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

/**
 * @author lbenno
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class StandardDeTest {
    private final static String NL = System.getProperty("line.separator");

    @Mock
    private Text text;

    @BeforeEach
    public void setUp() throws Exception {
        when(this.text.getCoAuthor()).thenReturn("");
        when(this.text.getYear()).thenReturn("");
        when(this.text.getSubtitle()).thenReturn("");
        when(this.text.getPlace()).thenReturn("");
        when(this.text.getPublisher()).thenReturn("");
        when(this.text.getPublication()).thenReturn("");
        when(this.text.getVolume()).thenReturn("");
        when(this.text.getNumber()).thenReturn("");
        when(this.text.getPages()).thenReturn("");
    }

    @Test
    public void testRenderTitle() throws Exception {
        when(this.text.getTitle()).thenReturn("Title of document");
        when(this.text.getText()).thenReturn("Remarks to text item");
        when(this.text.getAuthor()).thenReturn("Doe, Jane");

        // book
        when(this.text.getType()).thenReturn(AbstractText.TYPE_BOOK);
        final StandardDe lBiblio = new StandardDe();
        assertEquals("Doe, Jane" + NL + "Title of document.",
                lBiblio.render(this.text));

        // article
        when(this.text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
        assertEquals("Doe, Jane" + NL + "\"Title of document\".",
                lBiblio.render(this.text));

        // contribution
        when(this.text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
        assertEquals("Doe, Jane" + NL + "\"Title of document\",",
                lBiblio.render(this.text));

        // web page
        when(this.text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
        assertEquals("Doe, Jane" + NL + "\"Title of document\",",
                lBiblio.render(this.text));
    }

    @Test
    public void testRenderAuthorsYear() throws Exception {
        when(this.text.getTitle()).thenReturn("Title of document");
        when(this.text.getText()).thenReturn("Remarks to text item");
        when(this.text.getAuthor()).thenReturn("Doe, Jane");
        when(this.text.getCoAuthor()).thenReturn("Albert Einstein");
        when(this.text.getYear()).thenReturn("2006");

        // book
        when(this.text.getType()).thenReturn(AbstractText.TYPE_BOOK);
        final StandardDe lBiblio = new StandardDe();
        assertEquals("Doe, Jane und Albert Einstein" + NL
                + "2006. Title of document.", lBiblio.render(this.text));

        // article
        when(this.text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
        assertEquals("Doe, Jane und Albert Einstein" + NL
                + "2006. \"Title of document\".", lBiblio.render(this.text));

        // contribution
        when(this.text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
        assertEquals("Doe, Jane" + NL
                + "2006. \"Title of document\", Hrsg. Albert Einstein,",
                lBiblio.render(this.text));

        // web page
        when(this.text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
        assertEquals("Doe, Jane und Albert Einstein" + NL
                + "2006. \"Title of document\",", lBiblio.render(this.text));
    }

    @Test
    public void testRenderPublicationPlace() throws Exception {
        when(this.text.getTitle()).thenReturn("Title of document");
        when(this.text.getText()).thenReturn("Remarks to text item");
        when(this.text.getAuthor()).thenReturn("Doe, Jane");
        when(this.text.getCoAuthor()).thenReturn("Albert Einstein");
        when(this.text.getYear()).thenReturn("2006");
        when(this.text.getPublication()).thenReturn("Scientific American");
        when(this.text.getPlace()).thenReturn("London");
        when(this.text.getPublisher()).thenReturn("Publisher");

        // book
        when(this.text.getType()).thenReturn(AbstractText.TYPE_BOOK);
        final StandardDe lBiblio = new StandardDe();
        assertEquals("Doe, Jane und Albert Einstein" + NL
                + "2006. Title of document. London: Publisher.",
                lBiblio.render(this.text));

        // article
        when(this.text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
        assertEquals("Doe, Jane und Albert Einstein" + NL
                + "2006. \"Title of document\". Scientific American.",
                lBiblio.render(this.text));

        // contribution
        when(this.text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
        assertEquals(
                "Doe, Jane"
                        + NL
                        + "2006. \"Title of document\", in Scientific American. Hrsg. Albert Einstein, London: Publisher.",
                        lBiblio.render(this.text));

        // web page
        when(this.text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
        assertEquals(
                "Doe, Jane und Albert Einstein"
                        + NL
                        + "2006. \"Title of document\", Scientific American. (Zugriff London)",
                        lBiblio.render(this.text));
    }

    @Test
    public void testRenderSubtitle() throws Exception {
        when(this.text.getTitle()).thenReturn("Title of document");
        when(this.text.getText()).thenReturn("Remarks to text item");
        when(this.text.getAuthor()).thenReturn("Doe, Jane");
        when(this.text.getCoAuthor()).thenReturn("Albert Einstein");
        when(this.text.getYear()).thenReturn("2006");
        when(this.text.getPublication()).thenReturn("Scientific American");
        when(this.text.getPlace()).thenReturn("London");
        when(this.text.getPublisher()).thenReturn("Publisher");
        when(this.text.getSubtitle()).thenReturn("The document's subtitle");
        when(this.text.getPages()).thenReturn("55-67");

        // book
        when(this.text.getType()).thenReturn(AbstractText.TYPE_BOOK);
        final StandardDe lBiblio = new StandardDe();
        assertEquals(
                "Doe, Jane und Albert Einstein"
                        + NL
                        + "2006. Title of document. The document's subtitle. London: Publisher.",
                        lBiblio.render(this.text));

        // article
        when(this.text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
        assertEquals("Doe, Jane und Albert Einstein" + NL
                + "2006. \"Title of document\". Scientific American 55-67.",
                lBiblio.render(this.text));

        // contribution
        when(this.text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
        assertEquals(
                "Doe, Jane"
                        + NL
                        + "2006. \"Title of document\", in Scientific American. Hrsg. Albert Einstein, pp. 55-67. London: Publisher.",
                        lBiblio.render(this.text));

        // web page
        when(this.text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
        assertEquals(
                "Doe, Jane und Albert Einstein"
                        + NL
                        + "2006. \"Title of document. The document's subtitle\", Scientific American. (Zugriff London)",
                        lBiblio.render(this.text));
    }

    @Test
    public void testRenderVolume() throws Exception {
        when(this.text.getTitle()).thenReturn("Title of document");
        when(this.text.getText()).thenReturn("Remarks to text item");
        when(this.text.getAuthor()).thenReturn("Doe, Jane");
        when(this.text.getCoAuthor()).thenReturn("Albert Einstein");
        when(this.text.getYear()).thenReturn("2006");
        when(this.text.getPublication()).thenReturn("Scientific American");
        when(this.text.getPlace()).thenReturn("London");
        when(this.text.getPublisher()).thenReturn("Publisher");
        when(this.text.getSubtitle()).thenReturn("The document's subtitle");
        when(this.text.getPages()).thenReturn("55-67");
        when(this.text.getVolume()).thenReturn("90");
        when(this.text.getNumber()).thenReturn("4");

        // book
        when(this.text.getType()).thenReturn(AbstractText.TYPE_BOOK);
        final StandardDe lBiblio = new StandardDe();
        assertEquals(
                "Doe, Jane und Albert Einstein"
                        + NL
                        + "2006. Title of document. The document's subtitle. London: Publisher.",
                        lBiblio.render(this.text));

        // article
        when(this.text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
        assertEquals(
                "Doe, Jane und Albert Einstein"
                        + NL
                        + "2006. \"Title of document\". Scientific American 90:4, 55-67.",
                        lBiblio.render(this.text));

        // contribution
        when(this.text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
        assertEquals(
                "Doe, Jane"
                        + NL
                        + "2006. \"Title of document\", in Scientific American. Hrsg. Albert Einstein, pp. 55-67. London: Publisher.",
                        lBiblio.render(this.text));

        // web page
        when(this.text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
        assertEquals(
                "Doe, Jane und Albert Einstein"
                        + NL
                        + "2006. \"Title of document. The document's subtitle\", Scientific American. (Zugriff London)",
                        lBiblio.render(this.text));
    }

}
