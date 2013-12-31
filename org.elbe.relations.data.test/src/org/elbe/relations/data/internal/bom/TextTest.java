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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import org.elbe.relations.data.bom.AbstractItem;
import org.elbe.relations.data.bom.AbstractText;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.LightWeightText;
import org.elbe.relations.data.bom.Text;
import org.elbe.relations.data.bom.TextHome;
import org.elbe.relations.data.search.IndexerDocument;
import org.elbe.relations.data.search.IndexerField;
import org.elbe.relations.data.search.IndexerHelper;
import org.elbe.relations.data.test.DataHouseKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Luthiger
 */
public class TextTest {
	private static DataHouseKeeper data;

	private final static String NL = System.getProperty("line.separator");

	private final String title = "Book Title";
	private final String textText = "additional text";
	private final String author = "Riese, Adam";
	private final String coAuthor = "co";
	private final String subTitle = "sub";
	private final String year = "1887";
	private final String publication = "pub";
	private final String pages = "1-2";
	private final Integer volume = new Integer(88);
	private final Integer number = new Integer(107);
	private final String publisher = "Addison Wesley";
	private final String place = "London";
	private final Integer type = new Integer(2);

	@BeforeClass
	public static void init() {
		data = DataHouseKeeper.INSTANCE;
	}

	@Before
	public void setUp() throws Exception {
		// data.setUp();
	}

	@After
	public void tearDown() throws Exception {
		data.deleteAllInAll();
	}

	@Test
	public void testGetID() throws Exception {
		final TextHome lHome = data.getTextHome();

		assertEquals("number 0", 0, lHome.getCount());

		final AbstractItem lText = lHome.newText(title, textText, author,
				coAuthor, subTitle, year, publication, pages, volume, number,
				publisher, place, type);
		assertEquals("number 1", 1, lHome.getCount());

		final AbstractItem lText2 = lHome.getText(lText.getID());
		final LightWeightText lLightWeight = (LightWeightText) lText2
				.getLightWeight();

		assertEquals("id", lText.getID(), lLightWeight.getID());
		assertEquals("title", title, lLightWeight.title);
		assertEquals("text", textText, lLightWeight.text);
		assertEquals("author", author, lLightWeight.author);
		assertEquals("year", year, lLightWeight.year);
		assertEquals("author", author, lLightWeight.author);
		assertEquals("volume", volume.intValue(), lLightWeight.volume);
		assertEquals("number", number.intValue(), lLightWeight.number);
		assertEquals("publisher", publisher, lLightWeight.publisher);
		assertEquals("place", place, lLightWeight.place);
		assertEquals("type of text", type.intValue(), lLightWeight.type);
	}

	@Test
	public void testSave() throws Exception {
		final long lStart = System.currentTimeMillis() - 1000;

		final String lAuthor = "Newton, Isaac";
		final String lCoAuthor = "Galilei, G.";
		final String lPublisher = "O'Reilly";
		final String lPublication = "New Review";
		final int lNumber = 1;

		final TextHome lHome = data.getTextHome();
		final AbstractItem lText = lHome.newText(title, textText, author,
				coAuthor, subTitle, year, publication, pages, volume, number,
				publisher, place, type);

		final AbstractText lText2 = lHome.getText(lText.getID());
		lText2.save(title, textText, type, lAuthor, lCoAuthor, subTitle,
				lPublisher, year, lPublication, pages, volume, new Integer(
						lNumber), place);

		final AbstractItem lText3 = lHome.getText(lText.getID());
		assertEquals("title 2", title, lText3.getTitle());
		assertEquals("author 2", lAuthor, lText3.get(TextHome.KEY_AUTHOR));
		assertEquals("co-author 2", lCoAuthor,
				lText3.get(TextHome.KEY_COAUTHORS));
		assertEquals("publisher 2", lPublisher,
				lText3.get(TextHome.KEY_PUBLISHER));
		assertEquals("publication 2", lPublication,
				lText3.get(TextHome.KEY_PUBLICATION));
		assertEquals("volume 2", volume.intValue(),
				((BigDecimal) lText3.get(TextHome.KEY_VOLUME)).intValue());
		assertEquals("number 2", lNumber,
				((BigDecimal) lText3.get(TextHome.KEY_NUMBER)).intValue());

		assertTrue("compare timestamp",
				lStart < ((Timestamp) lText3.get(TextHome.KEY_MODIFIED))
						.getTime());
		final String lCreated = ((IItem) lText3).getCreated();
		assertNotNull("created string exists", lCreated);
		// the outcome of the following assertions depends on the -nl setting
		// e.g.
		// "Erzeugt:  May 6, 2007, 10:26:59 PM; Verändert: May 6, 2007, 10:26:59 PM."
		// assertTrue("Created:", lCreated.indexOf("Created:") >= 0);
		// assertTrue("Modified:", lCreated.indexOf("Modified:") >= 0);
	}

	@Test
	public void testGetBibtexFormatted() throws Exception {
		final String lExpected1 = "@BOOK{Riese:87," + NL
				+ "     AUTHOR = {Riese, Adam and co}," + NL
				+ "     TITLE = {Book Title: sub}," + NL
				+ "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		final String lExpected2 = "@ARTICLE{Riese:87," + NL
				+ "     AUTHOR = {Riese, Adam and co}," + NL
				+ "     TITLE = {Book Title}," + NL + "     JOURNAL = {pub},"
				+ NL + "     YEAR = 1887," + NL + "     VOLUME = {88}," + NL
				+ "     NUMBER = {107}," + NL + "     PAGES = {1-2}," + NL
				+ "     ADDRESS = {London}" + NL + "}";
		final String lExpected3 = "@INCOLLECTION{Riese:87," + NL
				+ "     AUTHOR = {Riese, Adam}," + NL
				+ "     TITLE = {Book Title}," + NL
				+ "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     EDITOR = {co}," + NL
				+ "     BOOKTITLE = {pub}," + NL + "     VOLUME = {88}," + NL
				+ "     ADDRESS = {London}" + NL + "}";
		final String lExpected4 = "@ARTICLE{Riese:87," + NL
				+ "     AUTHOR = {Riese, Adam and co}," + NL
				+ "     TITLE = {Book Title: sub}," + NL
				+ "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL
				+ "     JOURNAL = {\\path{<pub>} (accessed London)}," + NL
				+ "}";

		// test all types first
		final TextHome lHome = data.getTextHome();
		Text lText = (Text) lHome.newText(title, textText, author, coAuthor,
				subTitle, year, publication, pages, volume, number, publisher,
				place, new Integer(AbstractText.TYPE_BOOK));
		assertEquals("bibtex: book", lExpected1,
				lText.getBibtexFormatted(new ArrayList<String>()));

		lText = (Text) lHome.newText(title, textText, author, coAuthor,
				subTitle, year, publication, pages, volume, number, publisher,
				place, new Integer(AbstractText.TYPE_ARTICLE));
		assertEquals("bibtex: article", lExpected2,
				lText.getBibtexFormatted(new ArrayList<String>()));

		lText = (Text) lHome.newText(title, textText, author, coAuthor,
				subTitle, year, publication, pages, volume, number, publisher,
				place, new Integer(AbstractText.TYPE_CONTRIBUTION));
		assertEquals("bibtex: contribution", lExpected3,
				lText.getBibtexFormatted(new ArrayList<String>()));

		lText = (Text) lHome.newText(title, textText, author, coAuthor,
				subTitle, year, publication, pages, volume, number, publisher,
				place, new Integer(AbstractText.TYPE_WEBPAGE));
		assertEquals("bibtex: web page", lExpected4,
				lText.getBibtexFormatted(new ArrayList<String>()));

		// article with co-authors
		String lExpected = "@ARTICLE{Riese:87,"
				+ NL
				+ "     AUTHOR = {Riese, Adam and S. Ishikawa and M. Silverstein and M. Jacobson and I. Fisksdahl-King and S. Angel},"
				+ NL + "     TITLE = {Book Title}," + NL
				+ "     JOURNAL = {Research policy}," + NL
				+ "     YEAR = 1887," + NL + "     VOLUME = {88}," + NL
				+ "     NUMBER = {107}," + NL + "     PAGES = {88-93}," + NL
				+ "     ADDRESS = {London}" + NL + "}";
		lText = (Text) lHome
				.newText(
						title,
						textText,
						author,
						"S. Ishikawa, M. Silverstein, M. Jacobson, I. Fisksdahl-King, S. Angel",
						"Sub-Title", year, "Research policy", "88-93", volume,
						number, publisher, place, new Integer(
								AbstractText.TYPE_ARTICLE));
		assertEquals("article with co-authors", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));

		// contribution with editors
		lExpected = "@INCOLLECTION{Ishikawa:87,"
				+ NL
				+ "     AUTHOR = {Ishikawa, S. and M. Silverstein},"
				+ NL
				+ "     TITLE = {Book Title},"
				+ NL
				+ "     PUBLISHER = {Addison Wesley},"
				+ NL
				+ "     YEAR = 1887,"
				+ NL
				+ "     EDITOR = {Jacobson, M. and I. Fisksdahl-King and S. Angel},"
				+ NL + "     BOOKTITLE = {Research policy}," + NL
				+ "     VOLUME = {88}," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		lText = (Text) lHome.newText(title, textText,
				"Ishikawa, S., M. Silverstein",
				"Jacobson, M., I. Fisksdahl-King, S. Angel", "Sub-Title", year,
				"Research policy", "88-93", volume, number, publisher, place,
				new Integer(AbstractText.TYPE_CONTRIBUTION));
		assertEquals("contribution with editors", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));

		// web page with URL
		lExpected = "@ARTICLE{Ishikawa:87,"
				+ NL
				+ "     AUTHOR = {Ishikawa, S. and M. Silverstein and Jacobson and M. and I. Fisksdahl-King and S. Angel},"
				+ NL
				+ "     TITLE = {Book Title: Sub-Title},"
				+ NL
				+ "     PUBLISHER = {Addison Wesley},"
				+ NL
				+ "     YEAR = 1887,"
				+ NL
				+ "     JOURNAL = {\\path{<http://www.oreillynet.com/pub/wlg/7996>} (accessed 20.10.1999)},"
				+ NL + "}";
		lText = (Text) lHome.newText(title, textText,
				"Ishikawa, S., M. Silverstein",
				"Jacobson, M., I. Fisksdahl-King, S. Angel", "Sub-Title", year,
				"http://www.oreillynet.com/pub/wlg/7996", "88-93", volume,
				number, publisher, "20.10.1999", new Integer(
						AbstractText.TYPE_WEBPAGE));
		assertEquals("web page with URL", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));

		// book with subtitle
		lExpected = "@BOOK{Riese:87," + NL
				+ "     AUTHOR = {Riese, Adam and co}," + NL
				+ "     TITLE = {Book Title: Sub-Title}," + NL
				+ "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		lText = (Text) lHome.newText(title, textText, author, coAuthor,
				"Sub-Title", year, publication, pages, volume, number,
				publisher, place, new Integer(AbstractText.TYPE_BOOK));
		assertEquals("book with subtitle", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));

		// uniqueness of label
		final String lExpected1a = "@BOOK{Riese:87a," + NL
				+ "     AUTHOR = {Riese, Adam and co}," + NL
				+ "     TITLE = {Book Title (1): sub}," + NL
				+ "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		final String lExpected1b = "@BOOK{Riese:87b," + NL
				+ "     AUTHOR = {Riese, Adam and co}," + NL
				+ "     TITLE = {Book Title (2): sub}," + NL
				+ "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		final Collection<String> lUnique = new ArrayList<String>();
		lText = (Text) lHome.newText(title, textText, author, coAuthor,
				subTitle, year, publication, pages, volume, number, publisher,
				place, new Integer(AbstractText.TYPE_BOOK));
		assertEquals("uniqueness of label 0", lExpected1,
				lText.getBibtexFormatted(lUnique));
		lText = (Text) lHome.newText(title + " (1)", textText, author,
				coAuthor, subTitle, year, publication, pages, volume, number,
				publisher, place, new Integer(AbstractText.TYPE_BOOK));
		assertEquals("uniqueness of label 1", lExpected1a,
				lText.getBibtexFormatted(lUnique));
		lText = (Text) lHome.newText(title + " (2)", textText, author,
				coAuthor, subTitle, year, publication, pages, volume, number,
				publisher, place, new Integer(AbstractText.TYPE_BOOK));
		assertEquals("uniqueness of label 2", lExpected1b,
				lText.getBibtexFormatted(lUnique));

		// quotations in title
		lExpected = "@BOOK{Riese:87,"
				+ NL
				+ "     AUTHOR = {Riese, Adam and co},"
				+ NL
				+ "     TITLE = {Book with quoted title: \"`Whole title quoted.\"'},"
				+ NL + "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		lText = (Text) lHome.newText("Book with quoted title", textText,
				author, coAuthor, "\"Whole title quoted.\"", year, publication,
				pages, volume, number, publisher, place, new Integer(
						AbstractText.TYPE_BOOK));
		assertEquals("quotations in title 1", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));

		lExpected = "@BOOK{Riese:87,"
				+ NL
				+ "     AUTHOR = {Riese, Adam and co},"
				+ NL
				+ "     TITLE = {Book with quoted title: a \"`feel-good\"' novel},"
				+ NL + "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		lText = (Text) lHome.newText("Book with quoted title", textText,
				author, coAuthor, "a \"feel-good\" novel", year, publication,
				pages, volume, number, publisher, place, new Integer(
						AbstractText.TYPE_BOOK));
		assertEquals("quotations in title 2", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));

		// umlaut in author's name
		lExpected = "@BOOK{Mueller:87," + NL
				+ "     AUTHOR = {Müller, P. and co}," + NL
				+ "     TITLE = {Umlaut in author's name: sub}," + NL
				+ "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		lText = (Text) lHome.newText("Umlaut in author's name", textText,
				"Müller, P.", coAuthor, subTitle, year, publication, pages,
				volume, number, publisher, place, new Integer(
						AbstractText.TYPE_BOOK));
		assertEquals("umlaut in author's name", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));

		// text with ampersand somewhere
		lExpected = "@BOOK{Riese:87," + NL
				+ "     AUTHOR = {Riese, Adam and co}," + NL
				+ "     TITLE = {text with ampersand somewhere: sub}," + NL
				+ "     PUBLISHER = {Harper \\& Row}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		lText = (Text) lHome.newText("text with ampersand somewhere", textText,
				author, coAuthor, subTitle, year, publication, pages, volume,
				number, "Harper & Row", place, new Integer(
						AbstractText.TYPE_BOOK));
		assertEquals("text with ampersand somewhere", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));

		// author in curley brackets
		lExpected = "@BOOK{Open_Source_Initiative:87," + NL
				+ "     AUTHOR = {{Open Source Initiative} and co}," + NL
				+ "     TITLE = {author in curley brackets: sub}," + NL
				+ "     PUBLISHER = {Addison Wesley}," + NL
				+ "     YEAR = 1887," + NL + "     ADDRESS = {London}" + NL
				+ "}";
		lText = (Text) lHome.newText("author in curley brackets", textText,
				"{Open Source Initiative}", coAuthor, subTitle, year,
				publication, pages, volume, number, publisher, place,
				new Integer(AbstractText.TYPE_BOOK));
		assertEquals("author in curley brackets", lExpected,
				lText.getBibtexFormatted(new ArrayList<String>()));
	}

	@Test
	public void testIndexContent() throws Exception {
		final IndexerHelper lIndexer = new IndexerHelper();

		final TextHome lHome = data.getTextHome();
		final AbstractText lText = lHome.newText(title, textText, author,
				coAuthor, subTitle, year, publication, pages, volume, number,
				publisher, place, type);
		((Text) lText).indexContent(lIndexer);

		assertEquals("number of index docs", 1, lIndexer.getDocuments().size());
		final IndexerDocument lDocument = lIndexer.getDocuments().iterator()
				.next();
		final Collection<IndexerField> lFields = lDocument.getFields();

		assertEquals("number of index fields", 7, lFields.size());
		final Collection<String> lFieldNames = new ArrayList<String>();
		final Collection<String> lFieldFull = new ArrayList<String>();
		for (final IndexerField lField : lDocument.getFields()) {
			lFieldNames.add(lField.getFieldName());
			lFieldFull.add(lField.toString());
		}
		assertTrue("contains itemID", lFieldNames.contains("itemID"));
		assertTrue("contains itemType", lFieldNames.contains("itemType"));
		assertTrue("contains itemTitle", lFieldNames.contains("itemTitle"));
		assertTrue("contains itemDateCreated",
				lFieldNames.contains("itemDateCreated"));
		assertTrue("contains itemDateModified",
				lFieldNames.contains("itemDateModified"));
		assertTrue("contains itemFull", lFieldNames.contains("itemFull"));
		assertTrue("contains full 'itemType: 2'",
				lFieldFull.contains("itemType: 2"));
		assertTrue("contains full 'itemTitle: Book Title'",
				lFieldFull.contains("itemTitle: Book Title"));
	}

}
