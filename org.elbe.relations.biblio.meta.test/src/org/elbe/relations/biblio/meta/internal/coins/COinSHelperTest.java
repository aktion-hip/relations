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

package org.elbe.relations.biblio.meta.internal.coins;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.db.IDataService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit Plug-in test
 * 
 * @author lbenno
 */
@SuppressWarnings("restriction")
@RunWith(MockitoJUnitRunner.class)
public class COinSHelperTest {

	private static final String COINS_BOOK1 = "ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Abook&amp;rft.genre=book&amp;rft.btitle=Hallo+Welt&amp;rft.title=Hallo+Welt&amp;rft.aulast=Mustermann&amp;rft.aufirst=Max&amp;rft.au=Max+Mustermann&amp;rft.date=2005&amp;rft.pub=Wikipedia-Press&amp;rft.place=Musterstadt";
	private static final String COINS_BOOK2 = "ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Abook&amp;rft.genre=book&amp;rft.btitle=Hallo+Welt&amp;rft.title=Hallo+Welt&amp;rft.aulast=Mustermann&amp;rft.aufirst=Max&amp;rft.au=Max+Mustermann&amp;rft.au=Jane+Doe&amp;rft.date=2005&amp;rft.pub=Wikipedia-Press&amp;rft.place=Musterstadt";
	private static final String COINS_BOOK3 = "ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Abook&amp;rft.genre=book&amp;rft.btitle=Hallo+Welt&amp;rft.title=Hallo+Welt&amp;rft.aulast=Mustermann&amp;rft.aufirst=Max&amp;rft.au=Max+Mustermann&amp;rft.au=Jane+Doe&amp;rft.au=Adam+Riese&amp;rft.date=2005&amp;rft.pub=Wikipedia-Press&amp;rft.place=Musterstadt";
	private static final String COINS_BOOK4 = "ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Abook&amp;rft.genre=book&amp;rft.btitle=Hallo+Welt&amp;rft.title=Hallo+Welt&amp;rft.aulast=Mustermann&amp;rft.aufirst=Max&amp;rft.au=Max+Mustermann&amp;rft.date=2005&amp;rft.pub=Wikipedia-Press&amp;rft.place=Musterstadt&amp;rft.pages=1-3,6&amp;rft.place=New+York&amp;rft.tpages=392&amp;rft.issn=1041-5653";

	private static final String COINS_ARTICLE1 = "ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Ajournal&amp;rft.genre=issue&amp;rft.atitle=Hallo+Artikel&amp;rft.title=Hallo+Artikel&amp;rft.aulast=Mustermann&amp;rft.aufirst=Max&amp;rft.au=Max+Mustermann&amp;rft.date=2005&amp;rft.jtitle=Wikipedia-Press&amp;rft.issue=1998";
	private static final String COINS_ARTICLE2 = "ctx_ver=Z39.88-2004&amp;rft_val_fmt=info%3Aofi%2Ffmt%3Akev%3Ajournal&amp;rft.genre=issue&amp;rft.atitle=Hallo+Artikel&amp;rft.title=Hallo+Artikel&amp;rft.aulast=Mustermann&amp;rft.aufirst=Max&amp;rft.au=Max+Mustermann&amp;rft.date=2005&amp;rft.jtitle=Wikipedia-Press&amp;rft.issue=1998&amp;rft.volume=124&amp;rft.chron=1st quater";
	private Locale localeOld;

	private IEclipseContext context;

	@Mock
	private Logger log;

	@Mock
	private IDataService data;

	@Before
	public void setUp() throws Exception {
		localeOld = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);

		context = EclipseContextFactory.create("test context");
		context.set(Logger.class, log);
		context.set(IDataService.class, data);
	}

	@After
	public void tearDown() throws Exception {
		Locale.setDefault(localeOld);
	}

	@Test
	public void testBook() throws Exception {
		String lExpected = "title=Hallo Welt&author=Mustermann, Max&coAuthor=&subTitle=&year=&publication=&pages=&volume=0&number=0&publisher=Wikipedia-Press&place=Musterstadt&type=0&text=Date: 2005, Genre: book";
		COinSHelper lHelper = new COinSHelper(COINS_BOOK1, context);
		assertEquals("extract COinS book 1", lExpected, lHelper.getAction()
		        .toString());

		lExpected = "title=Hallo Welt&author=Mustermann, Max&coAuthor=Jane Doe&subTitle=&year=&publication=&pages=&volume=0&number=0&publisher=Wikipedia-Press&place=Musterstadt&type=0&text=Date: 2005, Genre: book";
		lHelper = new COinSHelper(COINS_BOOK2, context);
		assertEquals("extract COinS book 2", lExpected, lHelper.getAction()
		        .toString());

		lExpected = "title=Hallo Welt&author=Mustermann, Max&coAuthor=Jane Doe, Adam Riese&subTitle=&year=&publication=&pages=&volume=0&number=0&publisher=Wikipedia-Press&place=Musterstadt&type=0&text=Date: 2005, Genre: book";
		lHelper = new COinSHelper(COINS_BOOK3, context);
		assertEquals("extract COinS book 3", lExpected, lHelper.getAction()
		        .toString());

		lExpected = "title=Hallo Welt&author=Mustermann, Max&coAuthor=&subTitle=&year=&publication=&pages=1-3,6&volume=0&number=0&publisher=Wikipedia-Press&place=Musterstadt@New York&type=0&text=Total pages: 392, Date: 2005, ISSN: 1041-5653, Genre: book";
		lHelper = new COinSHelper(COINS_BOOK4, context);
		assertEquals("extract COinS book 4", lExpected, lHelper.getAction()
		        .toString());
	}

	@Test
	public void testArticle() throws Exception {
		String lExpected = "title=Hallo Artikel&author=Mustermann, Max&coAuthor=&subTitle=&year=&publication=Wikipedia-Press&pages=&volume=0&number=1998&publisher=&place=&type=1&text=Date: 2005, Genre: issue";
		COinSHelper lHelper = new COinSHelper(COINS_ARTICLE1, context);
		assertEquals("extract COinS article 1", lExpected, lHelper.getAction()
		        .toString());

		lExpected = "title=Hallo Artikel&author=Mustermann, Max&coAuthor=&subTitle=&year=&publication=Wikipedia-Press&pages=&volume=124&number=1998&publisher=&place=&type=1&text=Chronology: 1st quater, Date: 2005, Genre: issue";
		lHelper = new COinSHelper(COINS_ARTICLE2, context);
		assertEquals("extract COinS article 2", lExpected, lHelper.getAction()
		        .toString());
		// System.out.println(lHelper.getAction().toString());
	}

}
