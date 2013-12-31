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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.Text;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author lbenno
 */
@RunWith(MockitoJUnitRunner.class)
public class StandardEnTest {
	private final static String NL = System.getProperty("line.separator");

	@Mock
	private Text text;

	@Before
	public void setUp() throws Exception {
		when(text.getCoAuthor()).thenReturn("");
		when(text.getYear()).thenReturn("");
		when(text.getSubtitle()).thenReturn("");
		when(text.getPlace()).thenReturn("");
		when(text.getPublisher()).thenReturn("");
		when(text.getPublication()).thenReturn("");
		when(text.getVolume()).thenReturn("");
		when(text.getNumber()).thenReturn("");
		when(text.getPages()).thenReturn("");
	}

	@Test
	public void testRenderTitle() throws Exception {
		when(text.getTitle()).thenReturn("Title of document");
		when(text.getText()).thenReturn("Remarks to text item");
		when(text.getAuthor()).thenReturn("Doe, Jane");

		// book
		when(text.getType()).thenReturn(AbstractText.TYPE_BOOK);
		final StandardEn lBiblio = new StandardEn();
		assertEquals("Doe, Jane" + NL + "Title of document.",
		        lBiblio.render(text));

		// article
		when(text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
		assertEquals("Doe, Jane" + NL + "\"Title of document\".",
		        lBiblio.render(text));

		// contribution
		when(text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
		assertEquals("Doe, Jane" + NL + "\"Title of document\",",
		        lBiblio.render(text));

		// web page
		when(text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
		assertEquals("Doe, Jane" + NL + "\"Title of document\",",
		        lBiblio.render(text));
	}

	@Test
	public void testRenderAuthorsYear() throws Exception {
		when(text.getTitle()).thenReturn("Title of document");
		when(text.getText()).thenReturn("Remarks to text item");
		when(text.getAuthor()).thenReturn("Doe, Jane");
		when(text.getCoAuthor()).thenReturn("Albert Einstein");
		when(text.getYear()).thenReturn("2006");

		// book
		when(text.getType()).thenReturn(AbstractText.TYPE_BOOK);
		final StandardEn lBiblio = new StandardEn();
		assertEquals("Doe, Jane and Albert Einstein" + NL
		        + "2006. Title of document.", lBiblio.render(text));

		// article
		when(text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
		assertEquals("Doe, Jane and Albert Einstein" + NL
		        + "2006. \"Title of document\".", lBiblio.render(text));

		// contribution
		when(text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
		assertEquals("Doe, Jane" + NL
		        + "2006. \"Title of document\", Eds. Albert Einstein,",
		        lBiblio.render(text));

		// web page
		when(text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
		assertEquals("Doe, Jane and Albert Einstein" + NL
		        + "2006. \"Title of document\",", lBiblio.render(text));
	}

	@Test
	public void testRenderPublicationPlace() throws Exception {
		when(text.getTitle()).thenReturn("Title of document");
		when(text.getText()).thenReturn("Remarks to text item");
		when(text.getAuthor()).thenReturn("Doe, Jane");
		when(text.getCoAuthor()).thenReturn("Albert Einstein");
		when(text.getYear()).thenReturn("2006");
		when(text.getPublication()).thenReturn("Scientific American");
		when(text.getPlace()).thenReturn("London");
		when(text.getPublisher()).thenReturn("Publisher");

		// book
		when(text.getType()).thenReturn(AbstractText.TYPE_BOOK);
		final StandardEn lBiblio = new StandardEn();
		assertEquals("Doe, Jane and Albert Einstein" + NL
		        + "2006. Title of document. London: Publisher.",
		        lBiblio.render(text));

		// article
		when(text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
		assertEquals("Doe, Jane and Albert Einstein" + NL
		        + "2006. \"Title of document\". Scientific American.",
		        lBiblio.render(text));

		// contribution
		when(text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
		assertEquals(
		        "Doe, Jane"
		                + NL
		                + "2006. \"Title of document\", in Scientific American. Eds. Albert Einstein, London: Publisher.",
		        lBiblio.render(text));

		// web page
		when(text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
		assertEquals(
		        "Doe, Jane and Albert Einstein"
		                + NL
		                + "2006. \"Title of document\", Scientific American. (accessed London)",
		        lBiblio.render(text));
	}

	@Test
	public void testRenderSubtitle() throws Exception {
		when(text.getTitle()).thenReturn("Title of document");
		when(text.getText()).thenReturn("Remarks to text item");
		when(text.getAuthor()).thenReturn("Doe, Jane");
		when(text.getCoAuthor()).thenReturn("Albert Einstein");
		when(text.getYear()).thenReturn("2006");
		when(text.getPublication()).thenReturn("Scientific American");
		when(text.getPlace()).thenReturn("London");
		when(text.getPublisher()).thenReturn("Publisher");
		when(text.getSubtitle()).thenReturn("The document's subtitle");
		when(text.getPages()).thenReturn("55-67");

		// book
		when(text.getType()).thenReturn(AbstractText.TYPE_BOOK);
		final StandardEn lBiblio = new StandardEn();
		assertEquals(
		        "Doe, Jane and Albert Einstein"
		                + NL
		                + "2006. Title of document. The document's subtitle. London: Publisher.",
		        lBiblio.render(text));

		// article
		when(text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
		assertEquals("Doe, Jane and Albert Einstein" + NL
		        + "2006. \"Title of document\". Scientific American 55-67.",
		        lBiblio.render(text));

		// contribution
		when(text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
		assertEquals(
		        "Doe, Jane"
		                + NL
		                + "2006. \"Title of document\", in Scientific American. Eds. Albert Einstein, pp. 55-67. London: Publisher.",
		        lBiblio.render(text));

		// web page
		when(text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
		assertEquals(
		        "Doe, Jane and Albert Einstein"
		                + NL
		                + "2006. \"Title of document. The document's subtitle\", Scientific American. (accessed London)",
		        lBiblio.render(text));
	}

	@Test
	public void testRenderVolume() throws Exception {
		when(text.getTitle()).thenReturn("Title of document");
		when(text.getText()).thenReturn("Remarks to text item");
		when(text.getAuthor()).thenReturn("Doe, Jane");
		when(text.getCoAuthor()).thenReturn("Albert Einstein");
		when(text.getYear()).thenReturn("2006");
		when(text.getPublication()).thenReturn("Scientific American");
		when(text.getPlace()).thenReturn("London");
		when(text.getPublisher()).thenReturn("Publisher");
		when(text.getSubtitle()).thenReturn("The document's subtitle");
		when(text.getPages()).thenReturn("55-67");
		when(text.getVolume()).thenReturn("90");
		when(text.getNumber()).thenReturn("4");

		// book
		when(text.getType()).thenReturn(AbstractText.TYPE_BOOK);
		final StandardEn lBiblio = new StandardEn();
		assertEquals(
		        "Doe, Jane and Albert Einstein"
		                + NL
		                + "2006. Title of document. The document's subtitle. London: Publisher.",
		        lBiblio.render(text));

		// article
		when(text.getType()).thenReturn(AbstractText.TYPE_ARTICLE);
		assertEquals(
		        "Doe, Jane and Albert Einstein"
		                + NL
		                + "2006. \"Title of document\". Scientific American 90:4, 55-67.",
		        lBiblio.render(text));

		// contribution
		when(text.getType()).thenReturn(AbstractText.TYPE_CONTRIBUTION);
		assertEquals(
		        "Doe, Jane"
		                + NL
		                + "2006. \"Title of document\", in Scientific American. Eds. Albert Einstein, pp. 55-67. London: Publisher.",
		        lBiblio.render(text));

		// web page
		when(text.getType()).thenReturn(AbstractText.TYPE_WEBPAGE);
		assertEquals(
		        "Doe, Jane and Albert Einstein"
		                + NL
		                + "2006. \"Title of document. The document's subtitle\", Scientific American. (accessed London)",
		        lBiblio.render(text));
	}

}
